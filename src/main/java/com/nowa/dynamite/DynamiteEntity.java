package com.nowa.dynamite;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/**
 * Un bâton de dynamite en vol. Il se lance comme une boule de neige, mais plus lourd : il part moins
 * vite et retombe plus tôt.
 *
 * <p>Allumé, il explose au premier contact — bloc, joueur ou créature — ou en plein vol si sa mèche
 * arrive au bout avant. Éteint, il n'explose jamais : il retombe en objet, à ramasser.
 *
 * <p>Il porte l'objet lancé, mèche comprise : c'est lui que le jeu dessine, si bien que l'animation
 * de la mèche continue en vol sans rien de plus.
 */
public class DynamiteEntity extends ThrownItemEntity {
    public DynamiteEntity(EntityType<? extends DynamiteEntity> type, World world) {
        super(type, world);
    }

    public DynamiteEntity(World world, LivingEntity owner) {
        super(Dynamite.ENTITY, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Dynamite.ITEM;
    }

    @Override
    protected double getGravity() {
        return Dynamite.gravity();
    }

    @Override
    public void tick() {
        super.tick();
        World world = getWorld();
        if (world.isClient()) {
            if (Dynamite.isLit(getStack())) {
                world.addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.25, getZ(), 0.0, 0.0, 0.0);
            }
        } else if (!isRemoved() && Dynamite.isDue(getStack(), world)) {
            explode();
        }
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (getWorld().isClient() || isRemoved()) {
            return;
        }
        if (Dynamite.isLit(getStack())) {
            explode();
        } else {
            dropStack(getStack().copy());                 // éteinte : elle retombe, on la ramasse
            discard();
        }
    }

    private void explode() {
        discard();
        Dynamite.explode(getWorld(), this, getOwner() instanceof LivingEntity thrower ? thrower : null, getPos());
    }
}
