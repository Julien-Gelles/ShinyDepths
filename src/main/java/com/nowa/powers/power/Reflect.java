package com.nowa.powers.power;

import com.nowa.powers.GemPower;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * Le reflet (topaze bleue) : un projectile qui allait toucher celui qui porte la bague a une chance de
 * repartir vers son tireur, à la même vitesse. Il change alors de camp : c'est le porteur qui l'a
 * renvoyé, et c'est à lui que le coup compte. Sans tireur (un distributeur), il repart d'où il venait.
 * Vaut pour un joueur comme pour un mannequin d'entraînement.
 */
public final class Reflect {
    /** La chance de renvoyer un projectile. */
    private static final PowerValue CHANCE = PowerValue.of("reflect.chance", 0.10, 0.20, 0.30, 0.40, 0.50);

    /** Le renvoi : vers les yeux du tireur, qui est encore le propriétaire du projectile à ce moment. */
    private static final ProjectileDeflection TO_SHOOTER = (projectile, deflector, random) -> {
        double speed = Math.max(projectile.getVelocity().length(), 0.5);
        Entity shooter = projectile.getOwner();
        Vec3d direction = shooter != null && shooter != deflector
                ? shooter.getEyePos().subtract(projectile.getPos()).normalize()
                : projectile.getVelocity().multiply(-1.0).normalize();
        projectile.setVelocity(direction.multiply(speed));
        projectile.velocityDirty = true;
    };

    private Reflect() {
    }

    /**
     * Ce projectile va toucher cette entité : le renvoie-t-elle ? Si oui, c'est fait, et l'on rend le
     * renvoi au jeu pour qu'il arrête là le vol de ce tic ; {@code null} sinon. Côté serveur.
     */
    @Nullable
    public static ProjectileDeflection tryReflect(ProjectileEntity projectile, Entity target) {
        if (projectile.getWorld().isClient || projectile instanceof FishingBobberEntity
                || !(target instanceof LivingEntity wearer) || projectile.getOwner() == wearer) {
            return null;
        }
        int level = Powers.of(wearer).level(GemPower.REFLECT);
        if (level <= 0 || wearer.getRandom().nextDouble() >= CHANCE.at(level)) {
            return null;
        }
        // le renvoi d'abord, tant que le tireur est encore le propriétaire ; puis le porteur le devient
        projectile.deflect(TO_SHOOTER, wearer, wearer, false);
        wearer.getWorld().playSound(null, wearer.getX(), wearer.getY(), wearer.getZ(), SoundEvents.ITEM_SHIELD_BLOCK,
                SoundCategory.PLAYERS, 0.8f, 1.4f);
        return TO_SHOOTER;
    }
}
