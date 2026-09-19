package com.nowa.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

public class ModBlocks {

    public static final Block RUBY_BLOCK = registerBlock("ruby_block", new Block(AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK)));
    public static final Block RUBY_ORE = registerBlock("ruby_ore", new ExperienceDroppingBlock(UniformIntProvider.create(3, 7), AbstractBlock.Settings.copy(Blocks.EMERALD_ORE)));
    public static final Block DEEPSLATE_RUBY_ORE = registerBlock("deepslate_ruby_ore", new ExperienceDroppingBlock(UniformIntProvider.create(3, 7), AbstractBlock.Settings.copy(Blocks.DEEPSLATE_EMERALD_ORE)));

    // Tables de lapidaire : niveau 2 (fer), 3 (diamant), 4 (netherite)
    public static final Block IRON_LAPIDARY_TABLE = registerBlock("iron_lapidary_table", new LapidaryTableBlock(2, AbstractBlock.Settings.copy(Blocks.SMITHING_TABLE)));
    public static final Block DIAMOND_LAPIDARY_TABLE = registerBlock("diamond_lapidary_table", new LapidaryTableBlock(3, AbstractBlock.Settings.copy(Blocks.SMITHING_TABLE)));
    public static final Block NETHERITE_LAPIDARY_TABLE = registerBlock("netherite_lapidary_table", new LapidaryTableBlock(4, AbstractBlock.Settings.copy(Blocks.SMITHING_TABLE)));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block){
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name),
               new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks(){
        LOGGER.info("└─ Registering Mod Blocks for " + MOD_ID);
    }
}
