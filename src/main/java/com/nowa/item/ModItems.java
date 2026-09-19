package com.nowa.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

public class ModItems {
    public static final Item RUBY = registerItem("ruby", new Item(new Item.Settings()));

    public static final Item POLISHER = registerItem("polisher", new PolisherItem(2, new Item.Settings().maxDamage(64)));

    public static final Item RAW_PLATINUM = registerItem("raw_platinum", new Item(new Item.Settings()));
    public static final Item PLATINUM_INGOT = registerItem("platinum_ingot", new Item(new Item.Settings()));
    public static final Item RAW_PYRITE = registerItem("raw_pyrite", new Item(new Item.Settings()));
    public static final Item PYRITE_INGOT = registerItem("pyrite_ingot", new Item(new Item.Settings()));
    public static final Item RAW_COBALT = registerItem("raw_cobalt", new Item(new Item.Settings()));
    public static final Item COBALT_INGOT = registerItem("cobalt_ingot", new Item(new Item.Settings()));
    public static final Item RAW_TITANIUM = registerItem("raw_titanium", new Item(new Item.Settings()));
    public static final Item TITANIUM_INGOT = registerItem("titanium_ingot", new Item(new Item.Settings()));
    public static final Item RAW_BISMUTH = registerItem("raw_bismuth", new Item(new Item.Settings()));
    public static final Item BISMUTH_INGOT = registerItem("bismuth_ingot", new Item(new Item.Settings()));

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

    public static final Item RAW_RUBY = registerItem("raw_ruby", new Item(new Item.Settings()));
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

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name), item);
    }

    public static void registerModItems() {
        LOGGER.info("├─ Registering Mod Items for " + MOD_ID);
    }
}