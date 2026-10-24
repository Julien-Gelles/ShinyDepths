package com.nowa.datagen;

import com.nowa.block.ModBlocks;
import com.nowa.block.ModOres;
import com.nowa.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import com.nowa.item.ModDataComponents;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.CopyComponentsLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    /** Toucher de soie rend le bloc, sinon un seul nodule, sans bonus de Fortune. */
    private LootTable.Builder nodule(Block block) {
        return dropsWithSilkTouch(block, applyExplosionDecay(block, ItemEntry.builder(ModItems.GEODE)));
    }

    @Override
    public void generate() {
        // les blocs de pierres polies se lâchent eux-mêmes ; leur classe de pureté, notée à leur
        // position, est reportée sur l'objet par GemBlockPurity
        ModBlocks.gemBlocks().values().forEach(this::addDrop);
        ModBlocks.compactBlocks().values().forEach(this::addDrop);
        // les minerais lâchent leur matière brute : c'est le polissage qui donne la pierre finale
        ModOres.ores().forEach(ore -> addDrop(ore.block(), ore.drop() == ModItems.GEODE
                ? nodule(ore.block())                      // un nodule est entier : Fortune ne le multiplie pas
                : oreDrops(ore.block(), ore.drop())));
        // les minerais de diamant et d'émeraude du jeu aussi : leur butin est remplacé par le brut
        for (Block ore : List.of(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE)) {
            addDrop(ore, oreDrops(ore, ModItems.RAW_DIAMOND));
        }
        for (Block ore : List.of(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE)) {
            addDrop(ore, oreDrops(ore, ModItems.RAW_EMERALD));
        }

        addDrop(ModBlocks.COBALT_LANTERN);
        addDrop(ModBlocks.COBALT_CHAIN);
        addBench(ModBlocks.SOCKETING_BENCH);
        addBench(ModBlocks.FUSION_CRUCIBLE);
        addDrop(ModBlocks.GEM_SHOWCASE);
        addBench(ModBlocks.NETHERITE_LAPIDARY_TABLE);
    }


    /** Un établi fait quatre blocs : l'objet ne tombe que de sa base, sinon on en récolterait quatre. */
    private void addBench(net.minecraft.block.Block bench) {
        addDrop(bench, dropsWithProperty(bench, com.nowa.block.BenchBlock.PART,
                com.nowa.block.BenchBlock.Part.BASE));
    }
}

