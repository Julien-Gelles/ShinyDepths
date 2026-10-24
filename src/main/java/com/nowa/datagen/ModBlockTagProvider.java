package com.nowa.datagen;

import com.nowa.block.ModBlocks;
import com.nowa.block.ModOres;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import com.nowa.block.ModBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {

        ModBlocks.gemBlocks().values().forEach(block -> {
            getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(block);
            getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL).add(block);
        });
        ModBlocks.compactBlocks().values().forEach(block -> {
            getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(block);
            getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL).add(block);
        });

        // les minerais : toujours à la pioche, avec le niveau exigé par la dureté de la pierre
        ModOres.ores().forEach(ore -> {
            getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(ore.block());
            getOrCreateTagBuilder(ore.tool().tag).add(ore.block());
            // tags conventionnels : nos minerais se comportent comme ceux du jeu pour les autres mods
            getOrCreateTagBuilder(ConventionalBlockTags.ORES).add(ore.block());
            getOrCreateTagBuilder(ModBlockTags.ORES).add(ore.block());
            TagKey<Block> ground = switch (ore.rock()) {
                case STONE -> ConventionalBlockTags.ORES_IN_GROUND_STONE;
                case DEEPSLATE -> ConventionalBlockTags.ORES_IN_GROUND_DEEPSLATE;
                case NETHERRACK -> ConventionalBlockTags.ORES_IN_GROUND_NETHERRACK;
                case CALCITE, END_STONE -> null;              // aucun tag conventionnel pour ces roches
            };
            if (ground != null) {
                getOrCreateTagBuilder(ground).add(ore.block());
            }
        });

        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(ModBlocks.RUBY_BLOCK);

        // ferronnerie : à la pioche, sans palier, comme la lanterne et la chaîne du jeu
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(ModBlocks.COBALT_LANTERN)
                .add(ModBlocks.COBALT_CHAIN);

        // Tables de lapidaire : minables à la hache, comme la table de forgeron
        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(ModBlocks.SOCKETING_BENCH)
                .add(ModBlocks.FUSION_CRUCIBLE)
                .add(ModBlocks.GEM_SHOWCASE)
                .add(ModBlocks.NETHERITE_LAPIDARY_TABLE);

        getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.RUBY_BLOCK);
    }
}
