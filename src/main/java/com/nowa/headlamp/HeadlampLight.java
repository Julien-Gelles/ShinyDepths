package com.nowa.headlamp;

import com.nowa.config.ModConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * La lumière de la lampe frontale : une source posée à la hauteur de la tête du joueur, qui éclaire
 * tout autour de lui et le suit quand il se déplace.
 *
 * <p>Le jeu ne sait pas éclairer depuis une entité. On pose donc un vrai bloc de lumière
 * ({@link HeadlampLightBlock}) dans la case de la tête, et on le déplace avec le joueur. Comme c'est une vraie lumière, tout le monde la voit, et elle
 * compte pour le jeu : aucune créature hostile n'apparaît dans la zone éclairée.
 *
 * <p>Les poses se font sans prévenir les voisins : un observateur ou un rail ne doit pas réagir au
 * passage d'un mineur.
 */
public final class HeadlampLight {
    /** Prévenir les clients, mais ni les voisins ni leurs formes. */
    static final int FLAGS = Block.NOTIFY_LISTENERS | Block.FORCE_STATE;

    /** Tous les combien de tics la lumière suit le joueur. */
    private static int interval = 2;

    /** La lumière de chaque joueur, là où elle est posée. */
    private record Lit(RegistryKey<World> world, BlockPos pos) {
    }

    private static final Map<UUID, Lit> LIT = new HashMap<>();

    static {
        ModConfig.onApply(HeadlampLight::applyConfig);
    }

    private HeadlampLight() {
    }

    private static void applyConfig() {
        interval = Math.max(1, ModConfig.i("headlamp.light.interval_ticks", 2));
    }

    static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % interval != 0) {
                return;
            }
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                follow(player);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> extinguish(handler.player));
    }

    /** Une lampe la réclame-t-elle encore ? */
    static boolean isHeld(World world, BlockPos pos) {
        Lit wanted = new Lit(world.getRegistryKey(), pos);
        return LIT.containsValue(wanted);
    }

    private static void follow(ServerPlayerEntity player) {
        if (player.isSpectator() || GemSense.worn(player).isEmpty()) {
            extinguish(player);
            return;
        }
        ServerWorld world = player.getServerWorld();
        BlockPos target = target(player, world);
        Lit before = LIT.get(player.getUuid());
        Lit after = target == null ? null : new Lit(world.getRegistryKey(), target);
        if (after != null && after.equals(before)) {
            return;                                        // le joueur n'a pas changé de case
        }
        extinguish(player);
        if (after != null) {
            LIT.put(player.getUuid(), after);
            if (!world.getBlockState(target).isOf(Headlamp.LIGHT)) {
                world.setBlockState(target, Headlamp.LIGHT.getDefaultState(), FLAGS);
                world.scheduleBlockTick(target, Headlamp.LIGHT, HeadlampLightBlock.CHECK_TICKS);
            }
        }
    }

    /** Éteint la lumière de ce joueur, à moins qu'une autre lampe n'éclaire la même case. */
    private static void extinguish(ServerPlayerEntity player) {
        Lit lit = LIT.remove(player.getUuid());
        if (lit == null || LIT.containsValue(lit)) {
            return;
        }
        ServerWorld world = player.getServer().getWorld(lit.world());
        if (world != null && world.isChunkLoaded(lit.pos()) && world.getBlockState(lit.pos()).isOf(Headlamp.LIGHT)) {
            world.setBlockState(lit.pos(), Blocks.AIR.getDefaultState(), FLAGS);
        }
    }

    /**
     * La case éclairée : celle de la tête, comme une torche qu'on porterait au front. Si la tête est
     * dans autre chose que de l'air — sous l'eau, dans une herbe haute —, on essaie la case des
     * pieds ; s'il n'y a d'air nulle part, rien ne s'allume.
     */
    private static @Nullable BlockPos target(ServerPlayerEntity player, ServerWorld world) {
        BlockPos head = BlockPos.ofFloored(player.getEyePos());
        if (holds(world, head)) {
            return head;
        }
        BlockPos feet = player.getBlockPos();
        return holds(world, feet) ? feet : null;
    }

    private static boolean holds(World world, BlockPos pos) {
        if (!world.isInBuildLimit(pos) || !world.isChunkLoaded(pos)) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        return state.isAir() || state.isOf(Headlamp.LIGHT);
    }
}
