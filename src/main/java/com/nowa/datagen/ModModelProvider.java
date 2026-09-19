package com.nowa.datagen;

import com.nowa.block.ModBlocks;
import com.nowa.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.data.client.VariantsBlockStateSupplier;
import net.minecraft.util.Identifier;

import java.util.Optional;

import static com.nowa.ShinyDepths.MOD_ID;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.RUBY_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.RUBY_ORE);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.DEEPSLATE_RUBY_ORE);

        registerLapidaryTable(blockStateModelGenerator, ModBlocks.IRON_LAPIDARY_TABLE, "iron");
        registerLapidaryTable(blockStateModelGenerator, ModBlocks.DIAMOND_LAPIDARY_TABLE, "diamond");
        registerLapidaryTable(blockStateModelGenerator, ModBlocks.NETHERITE_LAPIDARY_TABLE, "netherite");
    }

    // Modèle commun (resources/.../models/block/lapidary_table_base.json) : côtés de la table de forgeron + petite scie ;
    // chaque table ne fournit que son dessus.
    private static final Model LAPIDARY_TABLE = new Model(Optional.of(Identifier.of(MOD_ID, "block/lapidary_table_base")), Optional.empty(), TextureKey.TOP);

    /** Bloc orienté comme un four ; seul le dessus change selon le matériau. */
    private static void registerLapidaryTable(BlockStateModelGenerator generator, Block block, String material) {
        TextureMap textures = new TextureMap().put(TextureKey.TOP, Identifier.of(MOD_ID, "block/" + material + "_lapidary_table_top"));
        Identifier model = LAPIDARY_TABLE.upload(block, textures, generator.modelCollector);
        generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block, BlockStateVariant.create().put(VariantSettings.MODEL, model))
                .coordinate(BlockStateModelGenerator.createNorthDefaultHorizontalRotationStates()));

    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItems.RUBY, Models.GENERATED);
        itemModelGenerator.register(ModItems.POLISHER, Models.HANDHELD);

        itemModelGenerator.register(ModItems.RAW_PLATINUM, Models.GENERATED);
        itemModelGenerator.register(ModItems.PLATINUM_INGOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_PYRITE, Models.GENERATED);
        itemModelGenerator.register(ModItems.PYRITE_INGOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_COBALT, Models.GENERATED);
        itemModelGenerator.register(ModItems.COBALT_INGOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_TITANIUM, Models.GENERATED);
        itemModelGenerator.register(ModItems.TITANIUM_INGOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_BISMUTH, Models.GENERATED);
        itemModelGenerator.register(ModItems.BISMUTH_INGOT, Models.GENERATED);

        itemModelGenerator.register(ModItems.MOONSTONE, Models.GENERATED);
        itemModelGenerator.register(ModItems.ONYX, Models.GENERATED);
        itemModelGenerator.register(ModItems.GARNET, Models.GENERATED);
        itemModelGenerator.register(ModItems.AMBER, Models.GENERATED);
        itemModelGenerator.register(ModItems.PERIDOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.JADE, Models.GENERATED);
        itemModelGenerator.register(ModItems.BLUE_TOPAZ, Models.GENERATED);
        itemModelGenerator.register(ModItems.TANZANITE, Models.GENERATED);
        itemModelGenerator.register(ModItems.ROSE_QUARTZ, Models.GENERATED);
        itemModelGenerator.register(ModItems.SUGILITE, Models.GENERATED);
        itemModelGenerator.register(ModItems.TOURMALINE, Models.GENERATED);
        itemModelGenerator.register(ModItems.SAPPHIRE, Models.GENERATED);

        itemModelGenerator.register(ModItems.RAW_RUBY, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_MOONSTONE, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_ONYX, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_GARNET, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_AMBER, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_PERIDOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_JADE, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_BLUE_TOPAZ, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_TANZANITE, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_ROSE_QUARTZ, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_SUGILITE, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_TOURMALINE, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_SAPPHIRE, Models.GENERATED);
    }
}
