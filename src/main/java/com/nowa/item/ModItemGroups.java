package com.nowa.item;

import com.nowa.block.ModBlocks;
import com.nowa.block.ModOres;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

public class ModItemGroups {
    /**
     * L'onglet du lapidaire, le polissoir pour emblème : ses outils et ses meubles, les pierres
     * brutes et polies, leurs blocs, et les bagues où on les sertit.
     */
    public static final ItemGroup LAPIDARY = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(MOD_ID, "lapidary"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.shiny-depths.lapidary"))
                    .icon(() -> new ItemStack(ModItems.POLISHER)).entries((displayContext, entries) -> {
                        entries.add(ModItems.POLISHER);
                        entries.add(ModItems.PYRITE_POLISHER);
                        entries.add(ModItems.COBALT_POLISHER);
                        entries.add(ModItems.LOUPE);
                        entries.add(ModItems.STONE_POUCH);
                        ModItems.DYED_STONE_POUCHES.values().forEach(entries::add);
                        entries.add(ModBlocks.NETHERITE_LAPIDARY_TABLE);
                        entries.add(ModBlocks.SOCKETING_BENCH);
                        entries.add(ModBlocks.FUSION_CRUCIBLE);
                        entries.add(ModBlocks.GEM_SHOWCASE);
                        // pierres brutes
                        entries.add(ModItems.RAW_MOONSTONE);
                        entries.add(ModItems.RAW_DIAMOND);
                        entries.add(ModItems.RAW_EMERALD);
                        entries.add(ModItems.RAW_ONYX);
                        entries.add(ModItems.RAW_GARNET);
                        entries.add(ModItems.RAW_AMBER);
                        entries.add(ModItems.RAW_PERIDOT);
                        entries.add(ModItems.RAW_JADE);
                        entries.add(ModItems.RAW_BLUE_TOPAZ);
                        entries.add(ModItems.RAW_SAPPHIRE);
                        entries.add(ModItems.RAW_TANZANITE);
                        entries.add(ModItems.RAW_SUGILITE);
                        entries.add(ModItems.RAW_TOURMALINE);
                        entries.add(ModItems.RAW_ROSE_QUARTZ);
                        entries.add(ModItems.RAW_RUBY);
                        entries.add(ModItems.GEODE);
                        // pierres polies
                        entries.add(ModItems.MOONSTONE);
                        entries.add(ModItems.ONYX);
                        entries.add(ModItems.GARNET);
                        entries.add(ModItems.AMBER);
                        entries.add(ModItems.PERIDOT);
                        entries.add(ModItems.JADE);
                        entries.add(ModItems.BLUE_TOPAZ);
                        entries.add(ModItems.SAPPHIRE);
                        entries.add(ModItems.TANZANITE);
                        entries.add(ModItems.SUGILITE);
                        entries.add(ModItems.TOURMALINE);
                        entries.add(ModItems.ROSE_QUARTZ);
                        entries.add(ModItems.RUBY);
                        // blocs de pierres polies
                        entries.add(ModBlocks.MOONSTONE_BLOCK);
                        entries.add(ModBlocks.ONYX_BLOCK);
                        entries.add(ModBlocks.GARNET_BLOCK);
                        entries.add(ModBlocks.AMBER_BLOCK);
                        entries.add(ModBlocks.PERIDOT_BLOCK);
                        entries.add(ModBlocks.JADE_BLOCK);
                        entries.add(ModBlocks.BLUE_TOPAZ_BLOCK);
                        entries.add(ModBlocks.SAPPHIRE_BLOCK);
                        entries.add(ModBlocks.TANZANITE_BLOCK);
                        entries.add(ModBlocks.SUGILITE_BLOCK);
                        entries.add(ModBlocks.TOURMALINE_BLOCK);
                        entries.add(ModBlocks.ROSE_QUARTZ_BLOCK);
                        entries.add(ModBlocks.RUBY_BLOCK);
                        // bijoux : forme par forme, pour que les cinq bagues se suivent
                        for (ModJewelry.Kind kind : ModJewelry.Kind.values()) {
                            ModJewelry.allOf(kind).forEach(entries::add);
                        }
                    }).build());

    /**
     * L'onglet de ce qui touche à la mine, et au futur métier de mineur : la lampe frontale pour
     * emblème, la dynamite, les métaux et leurs blocs, les minerais dans la roche, les panoplies et
     * la ferronnerie de cobalt.
     */
    public static final ItemGroup MINER = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(MOD_ID, "miner"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.shiny-depths.miner"))
                    .icon(() -> new ItemStack(com.nowa.headlamp.Headlamp.HEADLAMP)).entries((displayContext, entries) -> {
                        entries.add(com.nowa.headlamp.Headlamp.HEADLAMP);
                        entries.add(com.nowa.dynamite.Dynamite.ITEM);
                        entries.add(ModBlocks.COBALT_LANTERN);
                        entries.add(ModBlocks.COBALT_CHAIN);
                        // minerais bruts
                        entries.add(ModItems.RAW_PLATINUM);
                        entries.add(ModItems.RAW_TITANIUM);
                        entries.add(ModItems.RAW_PYRITE);
                        entries.add(ModItems.RAW_COBALT);
                        // lingots
                        entries.add(ModItems.PLATINUM_INGOT);
                        entries.add(ModItems.TITANIUM_INGOT);
                        entries.add(ModItems.PYRITE_INGOT);
                        entries.add(ModItems.COBALT_INGOT);
                        // pépites
                        entries.add(ModItems.PYRITE_NUGGET);
                        entries.add(ModItems.COBALT_NUGGET);
                        // blocs de minerai brut
                        entries.add(ModBlocks.RAW_PLATINUM_BLOCK);
                        entries.add(ModBlocks.RAW_TITANIUM_BLOCK);
                        entries.add(ModBlocks.RAW_PYRITE_BLOCK);
                        entries.add(ModBlocks.RAW_COBALT_BLOCK);
                        // blocs de lingots
                        entries.add(ModBlocks.PLATINUM_BLOCK);
                        entries.add(ModBlocks.TITANIUM_BLOCK);
                        entries.add(ModBlocks.PYRITE_BLOCK);
                        entries.add(ModBlocks.COBALT_BLOCK);
                        // minerais dans la roche, groupés par roche hôte
                        entries.add(ModOres.PYRITE_ORE);
                        entries.add(ModOres.TITANIUM_ORE);
                        entries.add(ModOres.PLATINUM_ORE);
                        entries.add(ModOres.RUBY_ORE);
                        entries.add(ModOres.GARNET_ORE);
                        entries.add(ModOres.AMBER_ORE);
                        entries.add(ModOres.JADE_ORE);
                        entries.add(ModOres.BLUE_TOPAZ_ORE);
                        entries.add(ModOres.TOURMALINE_ORE);
                        entries.add(ModOres.MOONSTONE_ORE);
                        entries.add(ModOres.DEEPSLATE_TITANIUM_ORE);
                        entries.add(ModOres.DEEPSLATE_PLATINUM_ORE);
                        entries.add(ModOres.DEEPSLATE_SAPPHIRE_ORE);
                        entries.add(ModOres.DEEPSLATE_RUBY_ORE);
                        entries.add(ModOres.DEEPSLATE_BLUE_TOPAZ_ORE);
                        entries.add(ModOres.DEEPSLATE_TOURMALINE_ORE);
                        entries.add(ModOres.PERIDOT_ORE);
                        entries.add(ModOres.COBALT_ORE);
                        entries.add(ModOres.ROSE_QUARTZ_ORE);
                        entries.add(ModOres.ONYX_ORE);
                        entries.add(ModOres.SUGILITE_ORE);
                        entries.add(ModOres.TANZANITE_ORE);
                        entries.add(ModOres.GEODE_ORE);
                        entries.add(ModOres.DEEPSLATE_GEODE_ORE);
                        entries.add(ModOres.CALCITE_GEODE_ORE);
                        // panoplies : outils puis armure, métal par métal
                        ModGear.sets().forEach(set -> set.all().forEach(entries::add));
                    }).build());

    public static void registerItemGroups() {
        LOGGER.info("├─ Registering Mod Item Groups for " + MOD_ID);
    }
}