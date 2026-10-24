package com.nowa.powers.power;

import com.nowa.powers.GemPower;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.minecraft.entity.player.PlayerEntity;

/** La sobriété (tanzanite) : la faim descend moins vite — tout ce qui fatigue le joueur pèse moins. */
public final class Sobriety {
    /** La part de fatigue retirée. */
    private static final PowerValue SAVED = PowerValue.of("sobriety.saved", 0.15, 0.30, 0.45, 0.60, 0.75);

    private Sobriety() {
    }

    /** La fatigue que le joueur va subir : ce qui en reste. Côté serveur. */
    public static float exhaustion(PlayerEntity player, float amount) {
        if (player.getWorld().isClient) {
            return amount;
        }
        int level = Powers.of(player).level(GemPower.SOBRIETY);
        return level > 0 ? amount * (float) (1.0 - SAVED.at(level)) : amount;
    }
}
