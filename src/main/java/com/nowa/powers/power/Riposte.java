package com.nowa.powers.power;

import com.nowa.powers.GemPower;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * La riposte (grenat) : un coup encaissé — d'une créature ou d'un joueur, à la mêlée ou de loin — arme
 * la riposte pour quelques secondes ; le prochain coup de mêlée du joueur la consomme et fait plus mal.
 * Un coup paré au bouclier n'a pas été encaissé.
 */
public final class Riposte {
    /** Le bonus de dégâts du coup qui riposte. */
    private static final PowerValue BONUS = PowerValue.of("riposte.bonus", 0.15, 0.30, 0.45, 0.60, 0.75);
    /** Combien de temps, en secondes, la riposte reste armée. */
    private static final PowerValue SECONDS = PowerValue.of("riposte.seconds", 3.0, 3.0, 3.0, 3.0, 3.0);
    /** Pour chaque joueur armé : l'heure du monde où la riposte retombe. */
    private static final Map<PlayerEntity, Long> ARMED = new WeakHashMap<>();

    private Riposte() {
    }

    /** Le joueur vient d'encaisser {@code taken} points de ce coup. */
    public static void hurt(PlayerEntity player, DamageSource source, float taken) {
        int level = Powers.of(player).level(GemPower.RIPOSTE);
        if (level > 0 && taken > 0.0f && source.getAttacker() instanceof LivingEntity attacker && attacker != player) {
            ARMED.put(player, player.getWorld().getTime() + Math.round(SECONDS.at(level) * 20.0));
        }
    }

    /** Les tics qu'il reste à la riposte armée du joueur ; 0 si elle ne l'est pas. */
    public static int remaining(PlayerEntity player) {
        Long until = ARMED.get(player);
        return until == null ? 0 : (int) Math.max(0L, until - player.getWorld().getTime());
    }

    /** Les dégâts d'un coup de mêlée du joueur : s'il riposte, la riposte les grossit et retombe. */
    public static float damage(PlayerEntity player, float amount) {
        int level = Powers.of(player).level(GemPower.RIPOSTE);
        if (level <= 0 || remaining(player) <= 0) {
            return amount;
        }
        ARMED.remove(player);
        return amount * (1.0f + (float) BONUS.at(level));
    }
}
