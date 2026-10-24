package com.nowa.world;

import com.nowa.config.ModConfig;
import com.nowa.item.GemPolishing;
import com.nowa.item.GemRarity;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;

import java.util.Map;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Les coffres des structures du jeu — villages, temples, donjons, mines, épaves, avant-postes… —
 * cachent parfois quelques pierres polies du mod, comme ils cachent déjà diamants et émeraudes.
 * Elles sortent sans pureté : il faut une loupe (ou un lapidaire) pour savoir ce qu'elles valent.
 *
 * <p>Une pioche par coffre : une chance d'y trouver une pierre, et alors une sorte tirée selon sa
 * rareté, en un petit nombre d'exemplaires. Le diamant et l'émeraude n'y sont pas, les coffres en
 * ont déjà. Seul le coffre bonus du début de partie est laissé tel quel.
 *
 * <p>Les tables se reconstruisent à chaque rechargement des données ({@code /reload}) : c'est là
 * que les réglages de la configuration sont relus.
 */
public final class ChestGems {
    private static final String CHESTS = "chests/";
    private static final String SPAWN_BONUS = "chests/spawn_bonus_chest";

    private static final Map<GemRarity, Integer> DEFAULT_WEIGHT = Map.of(
            GemRarity.UNCOMMON, 6, GemRarity.RARE, 3, GemRarity.VERY_RARE, 1);

    private ChestGems() {
    }

    public static void register() {
        LOGGER.info("├─ Registering chest gems for " + MOD_ID);
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            String path = key.getValue().getPath();
            if (!source.isBuiltin() || !key.getValue().getNamespace().equals("minecraft")
                    || !path.startsWith(CHESTS) || path.equals(SPAWN_BONUS)) {
                return;
            }
            float chance = (float) Math.clamp(ModConfig.d("loot.chest_gems.chance", 0.25), 0.0, 1.0);
            int min = Math.max(1, ModConfig.i("loot.chest_gems.min", 1));
            int max = Math.max(min, ModConfig.i("loot.chest_gems.max", 2));
            if (chance <= 0) {
                return;
            }
            LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .conditionally(RandomChanceLootCondition.builder(chance));
            for (GemRarity rarity : GemRarity.values()) {
                int weight = Math.max(0, ModConfig.i("loot.chest_gems.weight." + rarity.key(),
                        DEFAULT_WEIGHT.get(rarity)));
                for (Item gem : rarity.gems()) {
                    if (weight > 0 && !GemPolishing.isForeign(gem)) {
                        pool.with(ItemEntry.builder(gem).weight(weight)
                                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(min, max))));
                    }
                }
            }
            tableBuilder.pool(pool);
        });
    }
}
