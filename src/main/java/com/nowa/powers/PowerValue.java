package com.nowa.powers;

import com.nowa.config.ModConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Une valeur de pouvoir, donnée niveau par niveau : « 4 / 8 / 12 / 16 / 20 % ». Elle se règle dans
 * le fichier de configuration, sous {@code powers.<pouvoir>.<nom>}, en une liste de cinq nombres —
 * un par pureté, de l'ébréchée à la philosophale.
 */
public final class PowerValue {
    /** Le nombre de niveaux : un par pureté. */
    public static final int LEVELS = 5;
    private static final List<PowerValue> ALL = new ArrayList<>();

    static {
        ModConfig.onApply(() -> ALL.forEach(PowerValue::read));
    }

    private final String key;
    private final double[] defaults;
    private double[] values;

    private PowerValue(String key, double[] defaults) {
        this.key = "powers." + key;
        this.defaults = defaults;
        read();
        ALL.add(this);
    }

    /** Une valeur, de l'ébréchée (niveau 1) à la philosophale (niveau 5). */
    public static PowerValue of(String key, double... levels) {
        if (levels.length != LEVELS) {
            throw new IllegalArgumentException(key + " : " + LEVELS + " niveaux attendus");
        }
        return new PowerValue(key, levels);
    }

    private void read() {
        double[] read = ModConfig.doubles(key, defaults);
        values = read.length == 0 ? defaults : read;
    }

    /** La valeur à ce niveau ; 0 sans pouvoir. Au-delà du dernier niveau donné, c'est le dernier qui vaut. */
    public double at(int level) {
        return level <= 0 ? 0.0 : values[Math.min(level, values.length) - 1];
    }
}
