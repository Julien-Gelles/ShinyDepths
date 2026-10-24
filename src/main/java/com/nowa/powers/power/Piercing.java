package com.nowa.powers.power;

import com.nowa.mixin.PersistentProjectileAccessor;
import com.nowa.powers.GemPower;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * La perforation (sugilite) : les flèches du joueur traversent les créatures, et perdent une part de
 * leurs dégâts à chaque corps traversé — rien au niveau V. C'est le perçage du jeu (celui de
 * l'enchantement Perforation de l'arbalète), posé à la flèche quand elle entre dans le monde ; une
 * arbalète qui perce déjà davantage garde son perçage. Le trident n'est pas une flèche.
 */
public final class Piercing {
    /** Le nombre de créatures qu'une flèche traverse : elle en touche une de plus. */
    private static final PowerValue PIERCED = PowerValue.of("piercing.pierced", 1, 2, 3, 4, 5);
    /** La part de ses dégâts qu'une flèche garde après chaque créature traversée. */
    private static final PowerValue KEPT = PowerValue.of("piercing.kept", 0.60, 0.70, 0.80, 0.90, 1.00);
    /** La part gardée par chaque flèche perforante du joueur. */
    private static final Map<PersistentProjectileEntity, Double> ARROWS = new WeakHashMap<>();

    private Piercing() {
    }

    /** Une entité entre dans le monde : si c'est une flèche que le joueur vient de tirer, elle perce. Côté serveur. */
    public static void loaded(Entity entity) {
        if (!(entity instanceof PersistentProjectileEntity arrow) || entity instanceof TridentEntity || arrow.age > 0
                || !(arrow.getOwner() instanceof PlayerEntity shooter)) {
            return;
        }
        int level = Powers.of(shooter).level(GemPower.PIERCING);
        if (level <= 0) {
            return;
        }
        int pierced = (int) PIERCED.at(level);
        if (arrow.getPierceLevel() < pierced) {
            ((PersistentProjectileAccessor) arrow).shinyDepths$setPierceLevel((byte) pierced);
        }
        ARROWS.put(arrow, KEPT.at(level));
    }

    /** Cette flèche vient de blesser une créature : la suivante prendra moins. */
    public static void hit(PersistentProjectileEntity arrow) {
        Double kept = ARROWS.get(arrow);
        if (kept != null && kept < 1.0) {
            arrow.setDamage(arrow.getDamage() * kept);
        }
    }
}
