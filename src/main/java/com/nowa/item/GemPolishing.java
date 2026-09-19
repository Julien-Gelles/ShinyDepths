package com.nowa.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;

/**
 * Pierres brutes polissables : pierre polie obtenue et niveau de dureté requis.
 * 1 = polissoir, 2 = table de lapidaire en fer, 3 = en diamant, 4 = en netherite.
 */
public final class GemPolishing {
    public record Entry(Item polished, int tier) {}

    private static Map<Item, Entry> entries;

    private GemPolishing() {}

    private static Map<Item, Entry> entries() {
        if (entries == null) {
            entries = Map.ofEntries(
                    // niveau 1 : pierres tendres (Mohs 2 à 6,5)
                    Map.entry(ModItems.RAW_AMBER, new Entry(ModItems.AMBER, 1)),
                    Map.entry(ModItems.RAW_SUGILITE, new Entry(ModItems.SUGILITE, 1)),
                    Map.entry(ModItems.RAW_MOONSTONE, new Entry(ModItems.MOONSTONE, 1)),
                    Map.entry(ModItems.RAW_JADE, new Entry(ModItems.JADE, 1)),
                    // niveau 2 : Mohs 6,5 à 7
                    Map.entry(ModItems.RAW_ONYX, new Entry(ModItems.ONYX, 2)),
                    Map.entry(ModItems.RAW_PERIDOT, new Entry(ModItems.PERIDOT, 2)),
                    Map.entry(ModItems.RAW_TANZANITE, new Entry(ModItems.TANZANITE, 2)),
                    Map.entry(ModItems.RAW_ROSE_QUARTZ, new Entry(ModItems.ROSE_QUARTZ, 2)),
                    // niveau 3 : Mohs 7 à 8
                    Map.entry(ModItems.RAW_GARNET, new Entry(ModItems.GARNET, 3)),
                    Map.entry(ModItems.RAW_TOURMALINE, new Entry(ModItems.TOURMALINE, 3)),
                    Map.entry(ModItems.RAW_BLUE_TOPAZ, new Entry(ModItems.BLUE_TOPAZ, 3)),
                    // niveau 4 : Mohs 9
                    Map.entry(ModItems.RAW_SAPPHIRE, new Entry(ModItems.SAPPHIRE, 4)),
                    Map.entry(ModItems.RAW_RUBY, new Entry(ModItems.RUBY, 4))
            );
        }
        return entries;
    }

    /** Niveau de dureté de la pierre brute, ou -1 si ce n'est pas une pierre polissable. */
    public static int tierOf(ItemStack raw) {
        Entry entry = entries().get(raw.getItem());
        return entry == null ? -1 : entry.tier();
    }

    public static boolean canPolish(ItemStack raw, int level) {
        int tier = tierOf(raw);
        return tier > 0 && tier <= level;
    }

    /** Pierre polie correspondante, ou null si l'outil n'est pas d'un niveau suffisant. */
    public static Item polishedFor(ItemStack raw, int level) {
        return canPolish(raw, level) ? entries().get(raw.getItem()).polished() : null;
    }
}
