package com.nowa.powers.power;

import com.nowa.block.ModBlockTags;
import com.nowa.powers.GemPower;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Le filon (diamant) : casser un minerai casse aussi les minerais identiques qui le touchent — par une
 * face, une arête ou un coin — et ainsi de suite, jusqu'à la limite, les plus proches d'abord.
 * Accroupi, on ne casse que le bloc visé.
 *
 * <p>Chaque bloc en plus est cassé comme un vrai coup de pioche du joueur : l'outil s'use, Fortune et
 * Toucher de soie jouent, la faim se creuse, et le butin tombe là où était le bloc. Si l'outil casse,
 * le filon s'arrête.
 */
public final class Vein {
    /** Les blocs cassés en plus du premier. */
    private static final PowerValue EXTRA = PowerValue.of("vein.extra_blocks", 2, 4, 6, 8, 10);
    /** Vrai pendant qu'on casse le filon : ces blocs-là n'en ouvrent pas un autre. */
    private static boolean breaking;

    private Vein() {
    }

    /** Le joueur vient de casser ce bloc. Côté serveur. */
    public static void broken(World world, PlayerEntity player, BlockPos origin, BlockState state) {
        if (breaking || world.isClient || player.isSneaking() || !(player instanceof ServerPlayerEntity server)
                || !(state.isIn(ConventionalBlockTags.ORES) || state.isIn(ModBlockTags.ORES)) || !player.canHarvest(state)) {
            return;
        }
        int level = Powers.of(player).level(GemPower.VEIN);
        if (level <= 0) {
            return;
        }
        int left = (int) EXTRA.at(level);
        Block ore = state.getBlock();
        ItemStack tool = player.getMainHandStack();
        Set<BlockPos> seen = new HashSet<>();
        Deque<BlockPos> next = new ArrayDeque<>();
        seen.add(origin);
        next.add(origin);
        breaking = true;
        try {
            while (!next.isEmpty() && left > 0) {
                BlockPos at = next.poll();
                for (BlockPos near : BlockPos.iterate(at.add(-1, -1, -1), at.add(1, 1, 1))) {
                    if (left <= 0 || tool.isEmpty() || player.getMainHandStack() != tool) {
                        return;                                    // l'outil a cassé, ou n'est plus en main
                    }
                    BlockPos pos = near.toImmutable();
                    if (!seen.add(pos) || !world.getBlockState(pos).isOf(ore)) {
                        continue;
                    }
                    if (server.interactionManager.tryBreakBlock(pos)) {
                        left--;
                        next.add(pos);
                    }
                }
            }
        } finally {
            breaking = false;
        }
    }
}
