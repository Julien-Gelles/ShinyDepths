package com.nowa.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.Registries;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.nowa.NowasDepths.MOD_ID;

/**
 * Pierres brutes polissables : pierre polie obtenue et niveau de dureté requis.
 * 1 = polissoir, 2 = table de lapidaire en fer, 3 = en diamant, 4 = en netherite.
 */
public final class GemPolishing {
    public record Entry(Item polished, int tier) {
    }

    private static Map<Item, Entry> entries;

    private GemPolishing() {}

    private static Map<Item, Entry> entries() {
        if (entries == null) {
            // l'ordre de déclaration est celui de la grappe affichée par les tables
            entries = new LinkedHashMap<>();
            // dureté 1 : pierres tendres
            entries.put(ModItems.RAW_AMBER, new Entry(ModItems.AMBER, 1));
            entries.put(ModItems.RAW_SUGILITE, new Entry(ModItems.SUGILITE, 1));
            entries.put(ModItems.RAW_MOONSTONE, new Entry(ModItems.MOONSTONE, 1));
            // les deux pierres du jeu : communes, donc tendres à tailler — le polissoir y suffit
            entries.put(ModItems.RAW_DIAMOND, new Entry(Items.DIAMOND, 1));
            entries.put(ModItems.RAW_EMERALD, new Entry(Items.EMERALD, 1));
            // dureté 2
            entries.put(ModItems.RAW_PERIDOT, new Entry(ModItems.PERIDOT, 2));
            entries.put(ModItems.RAW_TANZANITE, new Entry(ModItems.TANZANITE, 2));
            entries.put(ModItems.RAW_ROSE_QUARTZ, new Entry(ModItems.ROSE_QUARTZ, 2));
            // dureté 3
            entries.put(ModItems.RAW_GARNET, new Entry(ModItems.GARNET, 3));
            entries.put(ModItems.RAW_TOURMALINE, new Entry(ModItems.TOURMALINE, 3));
            entries.put(ModItems.RAW_BLUE_TOPAZ, new Entry(ModItems.BLUE_TOPAZ, 3));
            entries.put(ModItems.RAW_JADE, new Entry(ModItems.JADE, 3));
            // dureté 4
            entries.put(ModItems.RAW_ONYX, new Entry(ModItems.ONYX, 4));
            entries.put(ModItems.RAW_SAPPHIRE, new Entry(ModItems.SAPPHIRE, 4));
            entries.put(ModItems.RAW_RUBY, new Entry(ModItems.RUBY, 4));
        }
        return entries;
    }

    /** La table en fer est la première : elle hérite aussi des pierres tendres, que nulle table avant elle ne prenait. */
    private static final int FIRST_TABLE = 2;
    /** Une géode demande le même temps qu'une pierre de dureté 3, et la même table. */
    public static final int GEODE_TIER = 3;

    /**
     * Les pierres brutes qu'une table de ce niveau sait tailler et que la précédente ne savait pas.
     * C'est ce que l'écran affiche en grappe : sept pierres pour la table en fer, cinq pour celle en
     * diamant, trois pour celle en netherite — la réponse à « pourquoi mon rubis ne passe pas ici »,
     * donnée avant qu'on pose la question.
     */
    public static List<Item> tableGems(int level) {
        int from = level <= FIRST_TABLE ? 1 : level;
        Stream<Item> stones = entries().entrySet().stream()
                .filter(entry -> entry.getValue().tier() >= from && entry.getValue().tier() <= level)
                .sorted(Comparator.comparingInt(entry -> entry.getValue().tier()))
                .map(Map.Entry::getKey);
        Stream<Item> geode = GEODE_TIER >= from && GEODE_TIER <= level
                ? Stream.of(ModItems.GEODE) : Stream.<Item>empty();
        return Stream.concat(stones, geode).toList();
    }

    /** Niveau de dureté de la pierre brute, ou -1 si ce n'est pas une pierre polissable. */
    public static int tierOf(ItemStack raw) {
        Entry entry = entries().get(raw.getItem());
        return entry == null ? -1 : entry.tier();
    }

    /** Dureté d'une pierre <em>polie</em>, ou -1 : c'est elle qui décide du niveau requis pour la sertir. */
    public static int polishedTier(Item polished) {
        return entries().values().stream()
                .filter(entry -> entry.polished() == polished)
                .mapToInt(Entry::tier)
                .min()
                .orElse(-1);
    }

    public static boolean canPolish(ItemStack raw, int level) {
        int tier = tierOf(raw);
        return tier > 0 && tier <= level;
    }

    /**
     * Cette pierre polie vient-elle du jeu plutôt que du mod ? Il en traîne alors partout sans
     * pureté — coffres, villageois, vieilles réserves — et celles-là restent sans pureté tant
     * qu'un lapidaire ne les a pas examinées.
     */
    public static boolean isForeign(Item polished) {
        return !Registries.ITEM.getId(polished).getNamespace().equals(MOD_ID);
    }

    /** Une pile de pierres polies qui attendent leur estimation, telles que la table les rend. */
    public static ItemStack unappraised(Item polished, int count) {
        ItemStack stack = new ItemStack(polished, count);
        if (isForeign(polished)) {
            stack.set(ModDataComponents.UNAPPRAISED, net.minecraft.util.Unit.INSTANCE);
        }
        return stack;
    }

    /**
     * Une pierre polie qui sort de la taille et dont la pureté n'a pas encore été révélée : le sachet
     * l'estimera. Une pierre du mod sans pureté est toujours dans ce cas ; un diamant ou une
     * émeraude, seulement s'ils portent la marque de la table.
     *
     * <p>Un diamant trouvé dans la nature — coffre, villageois, butin — n'a <em>pas</em> de pureté
     * et n'en reçoit pas tout seul : il faudra le faire examiner par un lapidaire, et d'ici là il ne
     * peut pas être serti.
     */
    public static boolean needsAppraisal(ItemStack stack) {
        return isPolished(stack.getItem()) && !stack.contains(ModDataComponents.PURITY)
                && (!isForeign(stack.getItem()) || stack.contains(ModDataComponents.UNAPPRAISED));
    }

    /**
     * Une pierre polie sans pureté, d'où qu'elle vienne : celle de la table comme le diamant d'un
     * coffre. C'est ce que la loupe sait estimer.
     */
    public static boolean lacksPurity(ItemStack stack) {
        return isPolished(stack.getItem()) && !stack.contains(ModDataComponents.PURITY);
    }

    /** Est-ce une pierre polie — du mod, ou le diamant et l'émeraude du jeu ? */
    public static boolean isPolished(Item item) {
        return entries().values().stream().anyMatch(entry -> entry.polished() == item);
    }

    /** Toutes les pierres polies du mod, une fois chacune, dans l'ordre des duretés. */
    public static List<Item> polishedGems() {
        return entries().values().stream().map(Entry::polished).distinct().toList();
    }

    /** Pierre polie avec sa classe de pureté et le reflet qui va avec. */
    public static ItemStack polishedStack(Item polished, Purity purity) {
        ItemStack stack = new ItemStack(polished);
        stack.set(ModDataComponents.PURITY, purity);
        applyGlint(stack);
        return stack;
    }

    /**
     * Pose ou retire le reflet selon la classe : les deux plus hautes brillent, les autres non.
     * La pure prend le miroitement violet de l'enchantement, la philosophale le même en doré —
     * c'est le rendu, côté client, qui change la couleur.
     */
    public static void applyGlint(ItemStack stack) {
        Purity purity = stack.get(ModDataComponents.PURITY);
        if (purity == Purity.PURE || purity == Purity.PHILOSOPHAL) {
            stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            stack.remove(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        }
    }

    /** Pierre polie correspondante, quelle que soit sa dureté : c'est le polissoir à la main. */
    public static Item polishedFor(ItemStack raw) {
        Entry entry = entries().get(raw.getItem());
        return entry == null ? null : entry.polished();
    }

    /** Pierre polie correspondante, ou null si l'outil n'est pas d'un niveau suffisant. */
    public static Item polishedFor(ItemStack raw, int level) {
        return canPolish(raw, level) ? entries().get(raw.getItem()).polished() : null;
    }
}
