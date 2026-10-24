package com.nowa.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nowa.powers.power.Fishing;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * L'attente avant la touche : le jeu la tire entre 5 et 30 secondes, puis en ôte celle de l'Appât. Le
 * pouvoir Pêcheur en ôte une part de plus, lue au moment où le jeu lit celle de l'Appât.
 */
@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin {
    @Shadow
    private int waitCountdown;

    @ModifyExpressionValue(method = "tickFishingLogic", at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;waitTimeReductionTicks:I"))
    private int nowasDepths$shorterWait(int reduction) {
        return Fishing.reduction(((FishingBobberEntity) (Object) this).getPlayerOwner(), waitCountdown, reduction);
    }
}
