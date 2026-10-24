package com.nowa.powers.power;

import com.nowa.powers.GemPower;
import com.nowa.powers.PowerCooldowns;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.TeleportTarget;

/**
 * Le rappel (pierre de lune) : un clic droit qui ne fait rien d'autre ramène le joueur à son point de
 * réapparition — son lit, son ancre de réapparition — même depuis une autre dimension ; à défaut, au
 * point d'apparition du monde. C'est la place que le jeu lui donnerait s'il mourait, sans user la
 * charge d'une ancre. Puis une recharge.
 */
public final class Recall {
    /** La recharge, en minutes. */
    private static final PowerValue COOLDOWN = PowerValue.of("recall.cooldown_minutes", 10, 8, 6, 4, 2);

    private Recall() {
    }

    /** Le joueur a fait un clic droit qui n'a rien fait d'autre. */
    public static void use(ServerPlayerEntity player) {
        int level = Powers.of(player).level(GemPower.RECALL);
        if (level <= 0 || player.isSpectator()
                || !PowerCooldowns.trigger(player, GemPower.RECALL, (int) Math.round(COOLDOWN.at(level) * 60.0 * 20.0))) {
            return;
        }
        effects(player);
        player.teleportTo(player.getRespawnTarget(true, Recall::effects));
        player.fallDistance = 0.0f;
    }

    /** Le départ et l'arrivée : les particules et le bruit d'une perle de l'End. */
    private static void effects(Entity entity) {
        if (entity.getWorld() instanceof ServerWorld world) {
            world.spawnParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 1.0, entity.getZ(), 40, 0.4, 0.8, 0.4, 0.2);
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_PLAYER_TELEPORT,
                    SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }
}
