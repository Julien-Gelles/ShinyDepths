package com.nowa.block;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import static com.nowa.NowasDepths.MOD_ID;

/** Tags de blocs du mod. */
public final class ModBlockTags {
    /** Tous les minerais du mod, pratique pour /locate ore #nowas-depths:ores. */
    public static final TagKey<Block> ORES = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "ores"));

    private ModBlockTags() {
    }
}
