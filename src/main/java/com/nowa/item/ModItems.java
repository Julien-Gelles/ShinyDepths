package com.nowa.item;

import com.nowa.config.ModConfig;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

public class ModItems {
    public static final Item RUBY = registerItem("ruby", new Item(new Item.Settings()));

    public static final Item STONE_POUCH = registerItem("stone_pouch", new StonePouchItem(null, new Item.Settings().maxCount(16)));
    /** Sachets teints (sachet + colorant), comme les bundles colorés. */
    public static final Map<DyeColor, Item> DYED_STONE_POUCHES = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            DYED_STONE_POUCHES.put(color, registerItem(color.getName() + "_stone_pouch",
                    new StonePouchItem(color, new Item.Settings().maxCount(16))));
        }
    }

    public static final Item POLISHER = registerItem("polisher", new PolisherItem(net.minecraft.item.Items.FLINT,
            1, new Item.Settings().maxDamage(ModConfig.i("polisher.durability", 64))));

    public static final Item RAW_PLATINUM = registerItem("raw_platinum", new Item(new Item.Settings()));
    public static final Item PLATINUM_INGOT = registerItem("platinum_ingot", new Item(new Item.Settings()));
    public static final Item RAW_PYRITE = registerItem("raw_pyrite", new Item(new Item.Settings()));
    public static final Item PYRITE_INGOT = registerItem("pyrite_ingot", new Item(new Item.Settings()));
    public static final Item RAW_COBALT = registerItem("raw_cobalt", new Item(new Item.Settings()));
    public static final Item COBALT_INGOT = registerItem("cobalt_ingot", new Item(new Item.Settings()));
    public static final Item RAW_TITANIUM = registerItem("raw_titanium", new Item(new Item.Settings()));
    public static final Item TITANIUM_INGOT = registerItem("titanium_ingot", new Item(new Item.Settings()));
    /** Deux métaux seulement se divisent en pépites : ceux dont on a quelque chose à faire à la pièce. */
    public static final Item PYRITE_NUGGET = registerItem("pyrite_nugget", new Item(new Item.Settings()));
    public static final Item COBALT_NUGGET = registerItem("cobalt_nugget", new Item(new Item.Settings()));

    /**
     * Polissoirs en métal : même geste, mais plus vif. La pyrite s'use en un rien de temps et
     * s'effrite au lieu de disparaître, le cobalt tient longtemps.
     */
    public static final Item PYRITE_POLISHER = registerItem("pyrite_polisher", new PolisherItem(PYRITE_INGOT,
            ModConfig.i("polisher.pyrite.speed", 2),
            new Item.Settings().maxDamage(ModConfig.i("polisher.pyrite.durability", 32))));
    public static final Item COBALT_POLISHER = registerItem("cobalt_polisher", new PolisherItem(COBALT_INGOT,
            ModConfig.i("polisher.cobalt.speed", 2),
            new Item.Settings().maxDamage(ModConfig.i("polisher.cobalt.durability", 96))));

    /** Loupe de lapidaire : estime les pierres polies sans pureté, une usure par pierre. */
    public static final Item LOUPE = registerItem("loupe",
            new LoupeItem(new Item.Settings().maxDamage(ModConfig.i("loupe.durability", 128))));

    public static final Item MOONSTONE = registerItem("moonstone", new Item(new Item.Settings()));
    public static final Item ONYX = registerItem("onyx", new Item(new Item.Settings()));
    public static final Item GARNET = registerItem("garnet", new Item(new Item.Settings()));
    public static final Item AMBER = registerItem("amber", new Item(new Item.Settings()));
    public static final Item PERIDOT = registerItem("peridot", new Item(new Item.Settings()));
    public static final Item JADE = registerItem("jade", new Item(new Item.Settings()));
    public static final Item BLUE_TOPAZ = registerItem("blue_topaz", new Item(new Item.Settings()));
    public static final Item TANZANITE = registerItem("tanzanite", new Item(new Item.Settings()));
    public static final Item ROSE_QUARTZ = registerItem("rose_quartz", new Item(new Item.Settings()));
    public static final Item SUGILITE = registerItem("sugilite", new Item(new Item.Settings()));
    public static final Item TOURMALINE = registerItem("tourmaline", new Item(new Item.Settings()));
    public static final Item SAPPHIRE = registerItem("sapphire", new Item(new Item.Settings()));

    /** Nodule fermé : la table de lapidaire en diamant l'ouvre et en tire un sachet de quatre pierres. */
    public static final Item GEODE = registerItem("geode", new Item(new Item.Settings()));

    public static final Item RAW_RUBY = registerItem("raw_ruby", new Item(new Item.Settings()));
    /**
     * Le diamant et l'émeraude du jeu sont des pierres comme les autres : leurs minerais lâchent ces
     * bruts, et c'est le polissage qui en fait le diamant et l'émeraude du jeu, avec une pureté.
     */
    public static final Item RAW_DIAMOND = registerItem("raw_diamond", new Item(new Item.Settings()));
    public static final Item RAW_EMERALD = registerItem("raw_emerald", new Item(new Item.Settings()));
    public static final Item RAW_MOONSTONE = registerItem("raw_moonstone", new Item(new Item.Settings()));
    public static final Item RAW_ONYX = registerItem("raw_onyx", new Item(new Item.Settings()));
    public static final Item RAW_GARNET = registerItem("raw_garnet", new Item(new Item.Settings()));
    public static final Item RAW_AMBER = registerItem("raw_amber", new Item(new Item.Settings()));
    public static final Item RAW_PERIDOT = registerItem("raw_peridot", new Item(new Item.Settings()));
    public static final Item RAW_JADE = registerItem("raw_jade", new Item(new Item.Settings()));
    public static final Item RAW_BLUE_TOPAZ = registerItem("raw_blue_topaz", new Item(new Item.Settings()));
    public static final Item RAW_TANZANITE = registerItem("raw_tanzanite", new Item(new Item.Settings()));
    public static final Item RAW_ROSE_QUARTZ = registerItem("raw_rose_quartz", new Item(new Item.Settings()));
    public static final Item RAW_SUGILITE = registerItem("raw_sugilite", new Item(new Item.Settings()));
    public static final Item RAW_TOURMALINE = registerItem("raw_tourmaline", new Item(new Item.Settings()));
    public static final Item RAW_SAPPHIRE = registerItem("raw_sapphire", new Item(new Item.Settings()));

    public static Item dyedStonePouch(DyeColor color) {
        return DYED_STONE_POUCHES.get(color);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name), item);
    }

    public static void registerModItems() {
        LOGGER.info("├─ Registering Mod Items for " + MOD_ID);
    }
}