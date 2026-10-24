package com.nowa.powers.power;

import com.nowa.powers.GemPower;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;
import java.util.WeakHashMap;

/** Le vol de vie (quartz rose) : une part des dégâts de mêlée infligés soigne celui qui frappe. */
public final class Lifesteal {
    /** La part des dégâts rendue en vie. */
    private static final PowerValue SHARE = PowerValue.of("lifesteal.share", 0.04, 0.08, 0.12, 0.16, 0.20);
    /** La vie (absorption comprise) qu'avait chaque victime juste avant le coup d'un voleur de vie. */
    private static final Map<LivingEntity, Float> BEFORE = new WeakHashMap<>();

    private Lifesteal() {
    }

    /** Le joueur va frapper cette victime à la mêlée : on note sa vie, au cas où le coup la tuerait. */
    public static void before(PlayerEntity player, LivingEntity victim) {
        if (Powers.of(player).level(GemPower.LIFESTEAL) > 0) {
            BEFORE.put(victim, victim.getHealth() + victim.getAbsorptionAmount());
        }
    }

    /** Le coup de mêlée du joueur vient de tuer : il a infligé toute la vie qui restait. */
    public static void killed(PlayerEntity player, LivingEntity victim) {
        Float left = BEFORE.remove(victim);
        if (left != null) {
            struck(player, left);
        }
    }

    /** Le joueur vient d'infliger {@code dealt} points de dégâts à la mêlée. */
    public static void struck(PlayerEntity player, float dealt) {
        int level = Powers.of(player).level(GemPower.LIFESTEAL);
        if (level > 0 && dealt > 0.0f && player.isAlive()) {
            player.heal((float) (dealt * SHARE.at(level)));
        }
    }
}
