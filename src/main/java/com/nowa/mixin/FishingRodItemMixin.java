package com.nowa.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nowa.powers.power.Fishing;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Le bouchon lancé emporte la chance de la canne : le pouvoir Pêcheur y ajoute la sienne. */
@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {
    @WrapOperation(method = "use", at = @At(value = "NEW",
            target = "(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;II)Lnet/minecraft/entity/projectile/FishingBobberEntity;"))
    private FishingBobberEntity nowasDepths$fishingLuck(PlayerEntity thrower, World world, int luck, int waitReduction,
                                                       Operation<FishingBobberEntity> original) {
        return original.call(thrower, world, Fishing.luck(thrower, luck), waitReduction);
    }
}
