package com.nowa.headlamp;

import com.nowa.config.ModConfig;
import com.nowa.headlamp.HeadlampNetworking.LanternSightPayload;
import com.nowa.world.LanternBeacons;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Ce que la lampe frontale montre des lanternes de cobalt : toutes celles qui se trouvent à portée
 * luisent à travers la roche. On en pose en descendant, on les suit pour remonter.
 *
 * <p>C'est un <em>battement</em>, le même que celui des pierres et au même rythme : toutes les cinq
 * secondes les lanternes s'allument, puis s'éteignent doucement. Il ne demande aucune pierre : porter
 * la lampe suffit.
 */
public final class LanternSight {
    /** Jusqu'où la lampe voit une lanterne, en blocs. */
    private static int radius = 64;
    /** Garde-fou : au-delà, le paquet grossirait pour rien. */
    private static int maxShown = 256;

    static {
        ModConfig.onApply(() -> {
            radius = Math.max(0, ModConfig.i("headlamp.lanterns.radius", 64));
            maxShown = Math.max(1, ModConfig.i("headlamp.lanterns.max_shown", 256));
        });
    }

    private LanternSight() {
    }

    /** La portée annoncée dans les infobulles de la lampe et de la lanterne. */
    public static int radius() {
        return radius;
    }

    static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // le même tic que le battement des pierres de ce joueur : tout s'allume ensemble
                if ((server.getTicks() + player.getId()) % GemSense.interval() != 0 || GemSense.worn(player).isEmpty()) {
                    continue;
                }
                List<BlockPos> lanterns = LanternBeacons.near(player.getServerWorld(), player.getBlockPos(), radius, maxShown);
                if (!lanterns.isEmpty()) {
                    ServerPlayNetworking.send(player, new LanternSightPayload(GemSense.glowTicks(), lanterns));
                }
            }
        });
    }
}
