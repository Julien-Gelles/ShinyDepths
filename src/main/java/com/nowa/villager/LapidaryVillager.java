package com.nowa.villager;

import com.google.common.collect.ImmutableSet;
import com.nowa.block.GemShowcaseBlock;
import com.nowa.block.ModBlocks;
import com.nowa.config.ModConfig;
import com.nowa.item.GemPolishing;
import com.nowa.item.GemRarity;
import com.nowa.item.ModDataComponents;
import com.nowa.item.ModItems;
import com.nowa.item.Purity;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.ComponentPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Le villageois lapidaire. Il prend son métier devant une vitrine à pierres. Il vend des pierres
 * polies contre des émeraudes, et rachète celles du joueur.
 *
 * <p>Depuis le 2026-10-19, ses pierres vont des plus médiocres aux plus belles : ébréchées et ternes
 * à bas prix, <em>sans pureté</em> un peu plus cher — un pari, que la loupe tranchera —, puis
 * limpides et pures, de plus en plus chères. Plus il monte en niveau, plus ses pierres sont rares et
 * pures. Il ne vend jamais de philosophale.
 *
 * <p>Les prix se calculent, ils ne s'écrivent pas : une pierre vaut sa rareté fois sa pureté, et
 * cette valeur fois le taux du villageois donne son prix en émeraudes. Changer un chiffre
 * rééquilibre donc tous les échanges.
 */
public final class LapidaryVillager {
    public static final Identifier ID = Identifier.of(MOD_ID, "lapidary");
    public static final RegistryKey<PointOfInterestType> JOB_SITE_KEY = RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, ID);

    /**
     * Son poste de travail : la vitrine à pierres, un bloc fait pour cela. La table de lapidaire et
     * l'établi de sertisseur sont des meubles de plusieurs blocs, réservés au joueur.
     */
    public static final PointOfInterestType JOB_SITE = PointOfInterestHelper.register(ID, 1, 1, jobSiteStates());

    public static final VillagerProfession PROFESSION = Registry.register(Registries.VILLAGER_PROFESSION, ID,
            new VillagerProfession("lapidary",
                    site -> site.matchesKey(JOB_SITE_KEY), site -> site.matchesKey(JOB_SITE_KEY),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.BLOCK_GRINDSTONE_USE));

    /** Ce que vaut une pierre selon sa rareté : c'est ce qui fait son prix. */
    private static final Map<GemRarity, Double> WORTH = new EnumMap<>(Map.of(
            GemRarity.UNCOMMON, 1.0, GemRarity.RARE, 2.0, GemRarity.VERY_RARE, 5.0));
    private static final Map<GemRarity, Double> DEFAULT_WORTH = Map.copyOf(WORTH);

    /**
     * Ce que vaut une pierre selon sa pureté. « Sans pureté » se place entre la terne et la
     * limpide : on paie un peu plus que la terne pour la chance d'avoir mieux.
     */
    private static final Map<Purity, Double> PURITY_WORTH = new EnumMap<>(Map.of(
            Purity.CHIPPED, 0.5, Purity.DULL, 1.0, Purity.CLEAR, 2.5, Purity.PURE, 4.5));
    private static final Map<Purity, Double> DEFAULT_PURITY_WORTH = Map.copyOf(PURITY_WORTH);
    private static double unappraisedWorth = 1.5;
    /** Émeraudes demandées par point de valeur : une peu commune terne coûte 2 émeraudes. */
    private static double emeraldsPerWorth = 2.0;
    /** Émeraudes données par point de valeur quand il rachète : la moitié de son prix de vente. */
    private static double buyEmeraldsPerWorth = 1.0;

    static {
        ModConfig.onApply(() -> {
            for (GemRarity rarity : GemRarity.values()) {
                WORTH.put(rarity, Math.max(0.1, ModConfig.d("villager.lapidary.worth." + rarity.key(),
                        DEFAULT_WORTH.get(rarity))));
            }
            for (Purity purity : DEFAULT_PURITY_WORTH.keySet()) {
                PURITY_WORTH.put(purity, Math.max(0.1, ModConfig.d("villager.lapidary.purity_worth." + purity.asString(),
                        DEFAULT_PURITY_WORTH.get(purity))));
            }
            unappraisedWorth = Math.max(0.1, ModConfig.d("villager.lapidary.purity_worth.unappraised", 1.5));
            emeraldsPerWorth = Math.max(0.1, ModConfig.d("villager.lapidary.emeralds_per_worth", 2.0));
            buyEmeraldsPerWorth = Math.max(0.05, ModConfig.d("villager.lapidary.buy_emeralds_per_worth", 1.0));
        });
    }

    private LapidaryVillager() {
    }

    public static void register() {
        LOGGER.info("├─ Registering Lapidary villager for " + MOD_ID);
        // niveau 1 : le nécessaire du débutant, les pierres les plus médiocres, et le rachat des ternes
        offers(1, 2,
                buy(Items.EMERALD, 2, ModItems.POLISHER, 1, 12),
                buyWithGems(GemRarity.UNCOMMON, 2, Items.EMERALD, 1, 16),
                buyGem(GemRarity.UNCOMMON, Purity.DULL),
                sellGem(GemRarity.UNCOMMON, Purity.CHIPPED),
                sellGem(GemRarity.UNCOMMON, Purity.DULL));
        // niveau 2 : il rachète les pierres rares et les limpides, et vend ses premiers paris
        offers(2, 10,
                buyWithGems(GemRarity.UNCOMMON, 3, ModItems.STONE_POUCH, 1, 12),
                buyWithGems(GemRarity.RARE, 2, Items.EMERALD, 3, 12),
                buyWithGems(GemRarity.UNCOMMON, 8, ModItems.GEODE, 1, 6),
                buyGem(GemRarity.UNCOMMON, Purity.CLEAR),
                buyGem(GemRarity.RARE, Purity.DULL),
                sellGem(GemRarity.UNCOMMON, null),
                sellGem(GemRarity.RARE, Purity.CHIPPED),
                sellGem(GemRarity.RARE, Purity.DULL));
        // niveau 3 : les premières limpides, et les pierres rares sans pureté
        offers(3, 20,
                buyWithGems(GemRarity.VERY_RARE, 1, Items.EMERALD, 4, 8),
                buyGem(GemRarity.UNCOMMON, Purity.PURE),
                buyGem(GemRarity.RARE, Purity.CLEAR),
                buyGem(GemRarity.VERY_RARE, Purity.DULL),
                sellGem(GemRarity.UNCOMMON, Purity.CLEAR),
                sellGem(GemRarity.RARE, null),
                sellGem(GemRarity.VERY_RARE, Purity.DULL));
        // niveau 4 : les premières pures, et les très rares sans pureté
        offers(4, 30,
                buyGem(GemRarity.RARE, Purity.PURE),
                buyGem(GemRarity.VERY_RARE, Purity.CLEAR),
                sellGem(GemRarity.UNCOMMON, Purity.PURE),
                sellGem(GemRarity.RARE, Purity.CLEAR),
                sellGem(GemRarity.VERY_RARE, null));
        // niveau 5 : ce qu'il a de plus beau, et le rachat des plus belles
        offers(5, 30,
                buyGem(GemRarity.VERY_RARE, Purity.PURE),
                sellGem(GemRarity.RARE, Purity.PURE),
                sellGem(GemRarity.VERY_RARE, Purity.CLEAR),
                sellGem(GemRarity.VERY_RARE, Purity.PURE));

    }

    /** Un échange, tel que le villageois le proposera : il lui reste à dire l'expérience qu'il rapporte. */
    private interface Deal {
        TradeOffer offer(Random random, int experience);
    }

    private static void offers(int level, int experience, Deal... deals) {
        TradeOfferHelper.registerVillagerOffers(PROFESSION, level, factories -> {
            for (Deal deal : deals) {
                TradeOffers.Factory factory = (entity, random) -> deal.offer(random, experience);
                factories.add(factory);
            }
        });
    }

    /**
     * Le Négoce de l'arbre du lapidaire : ce villageois demande moins à qui l'a appris — moins
     * d'émeraudes quand il vend, moins de pierres quand il achète. Posé quand il montre ses offres
     * au joueur, effacé avec les autres remises quand l'échange se ferme ; arrondi au plus proche.
     */
    public static void tradeDiscount(net.minecraft.entity.player.PlayerEntity player, Iterable<TradeOffer> offers) {
        double discount = com.nowa.lapidary.LapidaryBonuses.tradeDiscount(player);
        if (discount <= 0.0) {
            return;
        }
        for (TradeOffer offer : offers) {
            int off = (int) Math.round(discount * offer.getOriginalFirstBuyItem().getCount());
            if (off > 0) {
                offer.increaseSpecialPrice(-off);
            }
        }
    }

    /** Un objet contre un autre, sans tirage. */
    private static Deal buy(ItemConvertible price, int count, ItemConvertible sold, int soldCount, int maxUses) {
        return (random, experience) -> new TradeOffer(new TradedItem(price, count),
                new ItemStack(sold, soldCount), maxUses, experience, 0.05f);
    }

    /** Un objet payé en pierres de cette rareté — une sorte tirée au hasard, de n'importe quelle pureté. */
    private static Deal buyWithGems(GemRarity rarity, int count, ItemConvertible sold, int soldCount, int maxUses) {
        return (random, experience) -> new TradeOffer(new TradedItem(pick(rarity, random, null), count),
                new ItemStack(sold, soldCount), maxUses, experience, 0.05f);
    }

    /**
     * Une pierre vendue contre des émeraudes : une sorte tirée au hasard dans sa rareté, de cette
     * pureté — ou sans pureté si {@code purity} est nul. Le prix découle des valeurs ; plus la pierre
     * est chère, moins il en a en stock.
     */
    private static Deal sellGem(GemRarity rarity, Purity purity) {
        return (random, experience) -> {
            Item sold = pick(rarity, random, null);
            int price = price(rarity, purity);
            ItemStack stack = purity == null ? GemPolishing.unappraised(sold, 1) : GemPolishing.polishedStack(sold, purity);
            int maxUses = price <= 4 ? 12 : price <= 16 ? 8 : 4;
            return new TradeOffer(new TradedItem(Items.EMERALD, price), stack, maxUses, experience, 0.05f);
        };
    }

    /**
     * Le rachat d'une pierre de cette pureté exacte contre des émeraudes : une sorte tirée au hasard
     * dans sa rareté. Il paie la moitié de son prix de vente ; une pierre qui vaut moins d'une
     * émeraude se rachète par lots.
     */
    private static Deal buyGem(GemRarity rarity, Purity purity) {
        return (random, experience) -> {
            Item paid = pick(rarity, random, null);
            double each = WORTH.get(rarity) * PURITY_WORTH.get(purity) * buyEmeraldsPerWorth;
            int count = Math.clamp((int) Math.ceil(1.0 / each), 1, 64);
            int emeralds = Math.clamp((int) Math.floor(count * each), 1, 64);
            TradedItem price = new TradedItem(paid.getRegistryEntry(), count,
                    ComponentPredicate.builder().add(ModDataComponents.PURITY, purity).build());
            int maxUses = emeralds <= 2 ? 16 : emeralds <= 5 ? 12 : 8;
            return new TradeOffer(price, new ItemStack(Items.EMERALD, emeralds), maxUses, experience, 0.05f);
        };
    }

    /** Le prix en émeraudes d'une pierre de cette rareté et de cette pureté (nulle : sans pureté). */
    private static int price(GemRarity rarity, Purity purity) {
        double worth = purity == null ? unappraisedWorth : PURITY_WORTH.get(purity);
        return Math.clamp((int) Math.ceil(WORTH.get(rarity) * worth * emeraldsPerWorth), 1, 64);
    }

    /** Une pierre de cette rareté, autre que celle-là. */
    private static Item pick(GemRarity rarity, Random random, Item except) {
        List<Item> choices = new ArrayList<>(rarity.gems());
        choices.remove(except);
        return choices.get(random.nextInt(choices.size()));
    }

    private static List<BlockState> jobSiteStates() {
        // une vitrine qui porte les pierres d'un joueur n'est pas à prendre, ni une vitrine double
        return ModBlocks.GEM_SHOWCASE.getStateManager().getStates().stream()
                .filter(state -> !state.get(GemShowcaseBlock.STOCKED)
                        && state.get(GemShowcaseBlock.HALF) == GemShowcaseBlock.Half.SINGLE).toList();
    }
}
