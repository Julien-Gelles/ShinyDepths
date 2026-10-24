package com.nowa.block.entity;

import com.nowa.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.nowa.NowasDepths.LOGGER;
import static com.nowa.NowasDepths.MOD_ID;

public class ModBlockEntities {
    // Un seul type pour les trois tables : le niveau est lu sur le bloc
    public static final BlockEntityType<LapidaryTableBlockEntity> LAPIDARY_TABLE = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(MOD_ID, "lapidary_table"),
            BlockEntityType.Builder.create(LapidaryTableBlockEntity::new,
                    ModBlocks.NETHERITE_LAPIDARY_TABLE).build(null));

    // La vitrine à pierres : ce qu'elle expose, et qui l'a posée
    public static final BlockEntityType<GemShowcaseBlockEntity> GEM_SHOWCASE = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(MOD_ID, "gem_showcase"),
            BlockEntityType.Builder.create(GemShowcaseBlockEntity::new, ModBlocks.GEM_SHOWCASE).build(null));

    // L'établi de sertisseur : l'objet que porte son mannequin
    public static final BlockEntityType<FusionCrucibleBlockEntity> FUSION_CRUCIBLE = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(MOD_ID, "fusion_crucible"),
            BlockEntityType.Builder.create(FusionCrucibleBlockEntity::new, ModBlocks.FUSION_CRUCIBLE).build(null));

    public static final BlockEntityType<SocketingBenchBlockEntity> SOCKETING_BENCH = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(MOD_ID, "socketing_bench"),
            BlockEntityType.Builder.create(SocketingBenchBlockEntity::new, ModBlocks.SOCKETING_BENCH).build(null));

    public static void registerBlockEntities() {
        LOGGER.info("├─ Registering Mod Block Entities for " + MOD_ID);
    }
}
