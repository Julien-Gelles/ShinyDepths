package com.nowa.powers.power;

import com.nowa.powers.GemPower;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Le pêcheur (tourmaline) : le poisson mord plus vite, et la chance de la prise monte — plus de
 * trésors, moins de déchets, comme la Chance de la mer, à laquelle elle s'ajoute.
 */
public final class Fishing {
    /** La part de l'attente retirée, avant que l'Appât n'ôte la sienne. */
    private static final PowerValue SHORTER = PowerValue.of("fishing.shorter_wait", 0.15, 0.30, 0.45, 0.60, 0.75);
    /** La chance ajoutée à la prise. */
    private static final PowerValue LUCK = PowerValue.of("fishing.luck", 1, 1, 2, 2, 3);

    private Fishing() {
    }

    /** La chance de la prise pour ce pêcheur, celle de la canne comprise. */
    public static int luck(@Nullable PlayerEntity fisher, int luck) {
        return fisher == null ? luck : luck + (int) LUCK.at(Powers.of(fisher).level(GemPower.FISHING));
    }

    /**
     * Le jeu vient de tirer l'attente avant la touche ({@code wait} tics) et va ôter celle de l'Appât
     * ({@code reduction}) : ce qu'il faut ôter en tout.
     */
    public static int reduction(@Nullable PlayerEntity fisher, int wait, int reduction) {
        if (fisher == null) {
            return reduction;
        }
        return reduction + (int) Math.round(wait * SHORTER.at(Powers.of(fisher).level(GemPower.FISHING)));
    }
}
