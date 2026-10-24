package com.nowa.powers.power;

import com.nowa.powers.GemPower;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * L'érudition (saphir) : plus d'expérience gagnée, d'où qu'elle vienne. Ce qui se dépense —
 * enchanter, réparer à l'enclume — n'est pas touché.
 */
public final class Scholar {
    /** Le bonus d'expérience. */
    private static final PowerValue BONUS = PowerValue.of("scholar.bonus", 0.20, 0.40, 0.60, 0.80, 1.00);
    /** La part de point que chaque joueur a gagnée sans qu'elle fasse encore un point entier. */
    private static final Map<PlayerEntity, Double> OWED = new WeakHashMap<>();

    private Scholar() {
    }

    /** Le joueur va gagner {@code amount} points d'expérience : ce qu'il gagne vraiment. Côté serveur. */
    public static int experience(PlayerEntity player, int amount) {
        int level = Powers.of(player).level(GemPower.SCHOLAR);
        if (level <= 0 || amount <= 0 || player.getWorld().isClient) {
            return amount;
        }
        double total = amount * (1.0 + BONUS.at(level)) + OWED.getOrDefault(player, 0.0);
        int whole = (int) Math.floor(total);
        OWED.put(player, total - whole);
        return whole;
    }
}
