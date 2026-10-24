package com.nowa.world;

import com.mojang.serialization.Codec;
import com.nowa.block.PurityBlock;
import com.nowa.item.Purity;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nowa.NowasDepths.LOGGER;
import static com.nowa.NowasDepths.MOD_ID;

/**
 * La pureté des blocs de pierres polies posés dans le monde : ceux du mod, et les blocs de diamant
 * et d'émeraude du jeu.
 *
 * <p>Une entité de bloc aurait pu la retenir, mais le jeu refuse de pousser au piston un bloc qui
 * en a une, et on ne peut pas en donner aux blocs du jeu. La pureté est donc notée à côté, dans le
 * chunk, à la position du bloc. Elle y est écrite quand on pose le bloc, relue quand il lâche son
 * butin, et elle suit le bloc quand un piston le pousse.
 */
public final class GemBlockPurity {
    /** Par chunk : la position du bloc (un entier long, écrit en texte) et sa classe de pureté. */
    private static final AttachmentType<Map<String, Purity>> PLACED = AttachmentRegistry.create(
            Identifier.of(MOD_ID, "gem_block_purity"),
            builder -> builder.initializer(Map::of).persistent(Codec.unboundedMap(Codec.STRING, Purity.CODEC)));

    private GemBlockPurity() {
    }

    public static void register() {
        LOGGER.info("├─ Registering gem block purity for " + MOD_ID);
        // en créatif rien ne tombe, donc rien ne relit la pureté : on l'efface ici
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (player.isCreative() && isGemBlock(state)) {
                set(world, pos, null);
            }
        });
    }

    /** Un bloc de pierres polies : ceux du mod, et le diamant et l'émeraude du jeu. */
    public static boolean isGemBlock(BlockState state) {
        return state.getBlock() instanceof PurityBlock || state.isOf(Blocks.DIAMOND_BLOCK)
                || state.isOf(Blocks.EMERALD_BLOCK);
    }

    /** La pureté du bloc posé là, ou {@code null} s'il n'en a pas. */
    @Nullable
    public static Purity get(World world, BlockPos pos) {
        Map<String, Purity> placed = world.getChunk(pos).getAttached(PLACED);
        return placed == null ? null : placed.get(key(pos));
    }

    /** Note la pureté du bloc posé là, ou l'efface si {@code purity} est {@code null}. */
    public static void set(World world, BlockPos pos, @Nullable Purity purity) {
        if (world.isClient()) {
            return;
        }
        Chunk chunk = world.getChunk(pos);
        Map<String, Purity> placed = chunk.getAttached(PLACED);
        boolean known = placed != null && placed.containsKey(key(pos));
        if (purity == null && !known) {
            return;
        }
        Map<String, Purity> next = placed == null ? new HashMap<>() : new HashMap<>(placed);
        if (purity == null) {
            next.remove(key(pos));
        } else {
            next.put(key(pos), purity);
        }
        if (next.isEmpty()) {
            chunk.removeAttached(PLACED);
        } else {
            chunk.setAttached(PLACED, Map.copyOf(next));
        }
    }

    /**
     * Un piston va déplacer ces blocs d'un pas dans cette direction : les puretés notées les suivent.
     * On les ramasse toutes avant d'en reposer une, sans quoi deux blocs purs qui se suivent
     * s'écraseraient l'un l'autre.
     */
    public static void shift(World world, List<BlockPos> moved, Direction motion) {
        if (world.isClient()) {
            return;
        }
        Map<BlockPos, Purity> carried = new HashMap<>();
        for (BlockPos pos : moved) {
            Purity purity = isGemBlock(world.getBlockState(pos)) ? get(world, pos) : null;
            if (purity != null) {
                carried.put(pos, purity);
            }
        }
        carried.keySet().forEach(pos -> set(world, pos, null));
        carried.forEach((pos, purity) -> set(world, pos.offset(motion), purity));
    }

    /** Lit la pureté et l'efface : c'est ce que fait le bloc en lâchant son butin. */
    @Nullable
    public static Purity take(World world, BlockPos pos) {
        Purity purity = get(world, pos);
        if (purity != null) {
            set(world, pos, null);
        }
        return purity;
    }

    private static String key(BlockPos pos) {
        return Long.toString(pos.asLong());
    }
}
