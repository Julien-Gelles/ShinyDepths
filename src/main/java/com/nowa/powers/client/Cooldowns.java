package com.nowa.powers.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Le voile gris des effets de pouvoir, qui tourne comme l'aiguille d'une montre. Il dit deux choses :
 * <ul>
 *   <li><b>une recharge</b> : l'effet vient de servir ; il est tout gris, et le gris se retire à
 *       mesure qu'il redevient disponible ;</li>
 *   <li><b>une durée</b> : l'effet va s'éteindre ; il est net, et le gris le gagne à mesure que son
 *       temps s'écoule.</li>
 * </ul>
 * Le serveur annonce les recharges ; la durée se lit sur l'effet lui-même.
 */
public final class Cooldowns {
    /** Pour chaque effet en recharge : le tic du monde où elle a commencé, et celui où elle finit. */
    private static final Map<Identifier, long[]> RUNNING = new HashMap<>();
    /** Pour chaque effet à durée : la plus longue qu'on lui ait vue, c'est-à-dire celle qu'il avait au départ. */
    private static final Map<Identifier, Integer> LENGTH = new HashMap<>();
    /** Le voile gris, et le côté d'une icône d'effet. */
    private static final int VEIL = 0xC0303030, ICON = 18;

    private Cooldowns() {
    }

    /** L'effet vient de servir : il recharge pendant {@code ticks} tics. 0 : il est de nouveau prêt. */
    static void start(Identifier effect, int ticks) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (ticks <= 0 || client.world == null) {
            RUNNING.remove(effect);
        } else {
            long now = client.world.getTime();
            RUNNING.put(effect, new long[]{now, now + ticks});
        }
    }

    static void clear() {
        RUNNING.clear();
        LENGTH.clear();
    }

    /** Oublie la durée de départ des effets que le joueur n'a plus : s'ils reviennent, ils repartiront nets. */
    static void prune(Collection<StatusEffectInstance> current) {
        if (LENGTH.isEmpty()) {
            return;
        }
        Set<Identifier> present = new HashSet<>();
        for (StatusEffectInstance effect : current) {
            effect.getEffectType().getKey().ifPresent(key -> present.add(key.getValue()));
        }
        LENGTH.keySet().retainAll(present);
    }

    /** La part de recharge qui manque à cet effet, de 1 à 0. */
    private static float missing(Identifier effect) {
        MinecraftClient client = MinecraftClient.getInstance();
        long[] span = RUNNING.get(effect);
        if (span == null || client.world == null) {
            return 0.0f;
        }
        float now = client.world.getTime() + client.getRenderTickCounter().getTickDelta(false);
        if (now >= span[1] || now < span[0] - 1) {
            RUNNING.remove(effect);
            return 0.0f;
        }
        return Math.clamp((span[1] - now) / (span[1] - span[0]), 0.0f, 1.0f);
    }

    /** La part de sa durée que cet effet a déjà écoulée, de 0 à 1 ; 0 pour un effet sans durée. */
    private static float spent(Identifier id, StatusEffectInstance effect) {
        if (effect.isInfinite()) {
            LENGTH.remove(id);
            return 0.0f;
        }
        int length = LENGTH.merge(id, effect.getDuration(), Math::max);
        return length <= 0 ? 0.0f : Math.clamp(1.0f - (float) effect.getDuration() / length, 0.0f, 1.0f);
    }

    /** Grise l'icône d'un effet : un carré de dix-huit pixels, dont {@code x}, {@code y} est le coin. */
    static void draw(DrawContext context, int x, int y, StatusEffectInstance effect, boolean ofPower) {
        draw(context, x, y, ICON, ICON, 0, effect, ofPower);
    }

    /**
     * Grise un rectangle pour un effet — son icône, ou tout l'intérieur de son encadré dans
     * l'inventaire. Dans un rectangle allongé, l'aiguille tourne comme s'il était carré : elle passe
     * par les coins aux huitièmes de tour, au lieu de filer le long des grands côtés.
     *
     * @param corner  le pan coupé des coins, en pixels : on ne les grise pas
     * @param ofPower l'effet témoigne-t-il d'un pouvoir ? Seuls ceux-là montrent leur durée qui s'écoule
     */
    static void draw(DrawContext context, int x, int y, int width, int height, int corner, StatusEffectInstance effect,
                     boolean ofPower) {
        Identifier id = effect.getEffectType().getKey().map(key -> key.getValue()).orElse(null);
        if (id == null) {
            return;
        }
        float missing = missing(id);
        if (missing > 0.0f) {
            sweep(context, x, y, width, height, corner, 1.0f - missing, 1.0f);    // le gris se retire
        } else if (ofPower) {
            float spent = spent(id, effect);
            if (spent > 0.0f) {
                sweep(context, x, y, width, height, corner, 0.0f, spent);          // le gris gagne
            }
        }
    }

    /** Grise la part du rectangle que l'aiguille parcourt entre {@code from} et {@code to} tours, en partant du haut. */
    private static void sweep(DrawContext context, int x, int y, int width, int height, int corner, float from, float to) {
        for (int row = 0; row < height; row++) {
            int start = -1;                                   // le début de la suite de pixels gris en cours
            for (int column = 0; column <= width; column++) {
                boolean grey = column < width
                        && Math.min(column, width - 1 - column) + Math.min(row, height - 1 - row) >= corner;
                if (grey) {
                    double turn = turn((column + 0.5) / width - 0.5, (row + 0.5) / height - 0.5);
                    grey = turn >= from && turn < to;
                }
                if (grey && start < 0) {
                    start = column;
                } else if (!grey && start >= 0) {
                    context.fill(x + start, y + row, x + column, y + row + 1, VEIL);
                    start = -1;
                }
            }
        }
    }

    /** L'angle de ce point autour du centre, en tours : 0 en haut, puis dans le sens des aiguilles d'une montre. */
    private static double turn(double dx, double dy) {
        double turn = Math.atan2(dx, -dy) / (2.0 * Math.PI);
        return turn < 0.0 ? turn + 1.0 : turn;
    }
}
