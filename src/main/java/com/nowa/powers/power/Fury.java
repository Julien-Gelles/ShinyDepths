package com.nowa.powers.power;

import com.nowa.powers.GemPower;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.minecraft.entity.player.PlayerEntity;

/**
 * La fureur (rubis) : plus on a perdu de vie, plus on frappe fort à la mêlée. Rien à pleine vie ; le
 * bonus entier à un cœur ou moins, et entre les deux à proportion de la vie perdue.
 */
public final class Fury {
    /** Le bonus de dégâts, entier, à un cœur. */
    private static final PowerValue BONUS = PowerValue.of("fury.max_bonus", 0.10, 0.20, 0.30, 0.40, 0.50);
    /** La vie où le bonus est entier : un cœur. */
    private static final float FULL_AT = 2.0f;

    private Fury() {
    }

    /** La part de vie perdue qui compte, de 0 (pleine vie) à 1 (un cœur ou moins). */
    private static float rage(PlayerEntity player) {
        float max = player.getMaxHealth();
        if (max <= FULL_AT) {
            return player.getHealth() < max ? 1.0f : 0.0f;
        }
        return Math.clamp((max - player.getHealth()) / (max - FULL_AT), 0.0f, 1.0f);
    }

    /** Les dégâts d'un coup de mêlée du joueur, grossis par sa fureur. */
    public static float damage(PlayerEntity player, float amount) {
        int level = Powers.of(player).level(GemPower.FURY);
        return level > 0 ? amount * (1.0f + (float) BONUS.at(level) * rage(player)) : amount;
    }

    /** La fureur ne paraît parmi les effets que tant qu'elle donne quelque chose : dès qu'on est blessé. */
    public static boolean shown(PlayerEntity player) {
        return rage(player) > 0.0f;
    }
}
