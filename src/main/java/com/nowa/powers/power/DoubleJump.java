package com.nowa.powers.power;

import com.nowa.powers.GemPower;
import com.nowa.powers.PowerCooldowns;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

/**
 * Le double saut (péridot) : un saut de plus, en l'air, puis une recharge. C'est le client qui déplace
 * le joueur : c'est donc lui qui voit la touche pressée en l'air et qui saute (voir
 * {@code PowersClient}), puis il le dit au serveur, qui vérifie qu'il y avait droit.
 */
public final class DoubleJump {
    /** La recharge, en secondes. */
    private static final PowerValue COOLDOWN = PowerValue.of("double_jump.cooldown", 10, 8, 6, 4, 2);

    private DoubleJump() {
    }

    /** La recharge à ce niveau, en tics ; 0 sans le pouvoir. Lue aussi par le client. */
    public static int cooldownTicks(int level) {
        return level <= 0 ? 0 : (int) Math.round(COOLDOWN.at(level) * 20.0);
    }

    /**
     * Le client vient de sauter une seconde fois. Le mouvement est le sien ; le serveur vérifie qu'il y
     * avait droit, remet la chute à zéro — on tombe désormais de là — et le fait entendre.
     */
    public static void jumped(ServerPlayerEntity player) {
        int cooldown = cooldownTicks(Powers.of(player).level(GemPower.DOUBLE_JUMP));
        if (cooldown == 0 || player.isOnGround() || !PowerCooldowns.trigger(player, GemPower.DOUBLE_JUMP, cooldown)) {
            return;
        }
        player.fallDistance = 0.0f;
        ServerWorld world = player.getServerWorld();
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_BREEZE_JUMP,
                SoundCategory.PLAYERS, 0.6f, 1.4f);
        world.spawnParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 8, 0.25, 0.0, 0.25, 0.02);
    }
}
