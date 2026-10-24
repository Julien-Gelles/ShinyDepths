package com.nowa.item;

import com.nowa.config.ModConfig;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * La couleur de chaque pierre polie, celle du nuancier de docs/minerais.md. Elle ne sert qu'à une
 * chose : teinter la pastille qu'un sachet porte quand il ne contient qu'une seule sorte de pierre.
 *
 */
public final class GemColors {
    private static final Map<Item, String> DEFAULTS = defaults();
    private static Map<Item, Integer> colors = Map.of();

    static {
        ModConfig.onApply(GemColors::applyConfig);
    }

    private GemColors() {
    }

    /** La couleur d'une pierre, en 0xAARRGGBB opaque ; blanc si la pierre est inconnue. */
    public static int of(Item gem) {
        return colors.getOrDefault(gem, 0xFFFFFFFF);
    }

    private static void applyConfig() {
        Map<Item, Integer> read = new LinkedHashMap<>();
        DEFAULTS.forEach((gem, hex) -> {
            String value = ModConfig.text("pouch.gem_color." + Registries.ITEM.getId(gem).getPath(), hex);
            read.put(gem, 0xFF000000 | parse(value, hex));
        });
        colors = Map.copyOf(read);
    }

    private static int parse(String value, String fallback) {
        try {
            return Integer.parseInt(value.startsWith("#") ? value.substring(1) : value, 16);
        } catch (NumberFormatException malformed) {
            return Integer.parseInt(fallback, 16);
        }
    }

    private static Map<Item, String> defaults() {
        Map<Item, String> table = new LinkedHashMap<>();
        table.put(ModItems.MOONSTONE, "FFFFFF");
        table.put(net.minecraft.item.Items.DIAMOND, "4AEDD9");
        table.put(net.minecraft.item.Items.EMERALD, "17DD62");
        table.put(ModItems.ONYX, "1A1A1A");
        table.put(ModItems.GARNET, "803119");
        table.put(ModItems.AMBER, "E27A12");
        table.put(ModItems.PERIDOT, "92E212");
        table.put(ModItems.JADE, "1D580B");
        table.put(ModItems.BLUE_TOPAZ, "1292E2");
        table.put(ModItems.TANZANITE, "5712E2");
        table.put(ModItems.SUGILITE, "9612E2");
        table.put(ModItems.TOURMALINE, "E212DE");
        table.put(ModItems.SAPPHIRE, "1030A8");
        table.put(ModItems.ROSE_QUARTZ, "F69CE6");
        table.put(ModItems.RUBY, "E2124D");
        return table;
    }
}
