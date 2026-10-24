package com.nowa.powers;

import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Les recharges des pouvoirs qui servent à la demande (double saut, rappel, poche de l'End), côté
 * serveur. Rien n'est sauvegardé : une recharge ne survit ni à la déconnexion ni au redémarrage.
 */
public final class PowerCooldowns {
    /** Pour chaque joueur : l'heure du monde où chaque pouvoir est de nouveau prêt. */
    private static final Map<PlayerEntity, Map<GemPower, Long>> READY = new WeakHashMap<>();

    private PowerCooldowns() {
    }

    /**
     * Le pouvoir est-il prêt ? Si oui, on lance sa recharge de {@code ticks} tics, on le dit au client
     * — son icône se grise — et on répond vrai : celui qui demande déclenche le pouvoir.
     */
    public static boolean trigger(PlayerEntity player, GemPower power, int ticks) {
        long now = player.getWorld().getTime();
        Map<GemPower, Long> own = READY.computeIfAbsent(player, key -> new EnumMap<>(GemPower.class));
        Long until = own.get(power);
        if (until != null && now < until) {
            return false;
        }
        own.put(power, now + ticks);
        PowerEffects.cooldown(player, power, ticks);
        return true;
    }
}
