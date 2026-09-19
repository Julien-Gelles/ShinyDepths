package com.nowa.block.entity;

import com.nowa.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

public class ModBlockEntities {
    // Un seul type pour les trois tables : le niveau est lu sur le bloc
    public static final BlockEntityType<LapidaryTableBlockEntity> LAPIDARY_TABLE = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(MOD_ID, "lapidary_table"),
            BlockEntityType.Builder.create(LapidaryTableBlockEntity::new,
                    ModBlocks.IRON_LAPIDARY_TABLE, ModBlocks.DIAMOND_LAPIDARY_TABLE, ModBlocks.NETHERITE_LAPIDARY_TABLE).build(null));

    public static void registerBlockEntities() {
        LOGGER.info("├─ Registering Mod Block Entities for " + MOD_ID);
    }
}
