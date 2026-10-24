package com.nowa.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nowa.powers.power.Piercing;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Une flèche vient de blesser une créature : le jeu le lui dit ({@code onHit}, où une flèche à effets
 * pose ses effets). Une flèche du pouvoir Perforation y perd une part de ses dégâts pour la suivante.
 */
@Mixin(PersistentProjectileEntity.class)
public class PersistentProjectileEntityMixin {
    @WrapOperation(method = "onEntityHit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;onHit(Lnet/minecraft/entity/LivingEntity;)V"))
    private void nowasDepths$piercing(PersistentProjectileEntity arrow, LivingEntity target, Operation<Void> original) {
        original.call(arrow, target);
        Piercing.hit(arrow);
    }
}
