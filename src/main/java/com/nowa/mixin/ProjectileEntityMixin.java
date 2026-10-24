package com.nowa.mixin;

import com.nowa.powers.power.Reflect;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Un projectile va toucher quelque chose : c'est là que le jeu décide s'il est dévié (le Breeze, un
 * coup donné au projectile) avant de le laisser frapper. Le pouvoir Reflet s'y glisse en premier.
 */
@Mixin(ProjectileEntity.class)
public class ProjectileEntityMixin {
    @Inject(method = "hitOrDeflect", at = @At("HEAD"), cancellable = true)
    private void nowasDepths$reflect(HitResult hit, CallbackInfoReturnable<ProjectileDeflection> info) {
        if (hit instanceof EntityHitResult entityHit) {
            ProjectileDeflection reflected = Reflect.tryReflect((ProjectileEntity) (Object) this, entityHit.getEntity());
            if (reflected != null) {
                info.setReturnValue(reflected);
            }
        }
    }
}
