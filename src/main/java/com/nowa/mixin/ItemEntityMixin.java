package com.nowa.mixin;

import com.nowa.dynamite.Dynamite;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Une dynamite allumée qu'on laisse tomber au sol — touche de jet, mort, inventaire plein — continue
 * de brûler : arrivée au bout de sa mèche, elle explose là où elle gît. Sans cela, la lâcher
 * suffirait à l'éteindre.
 *
 * <p>Et les pierres tombées au sol survivent à l'explosion d'une dynamite.
 */
@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void nowasDepths$burningDynamite(CallbackInfo info) {
        ItemEntity item = (ItemEntity) (Object) this;
        if (item.getWorld().isClient() || !Dynamite.isDue(item.getStack(), item.getWorld())) {
            return;
        }
        Dynamite.spend(item.getStack());
        if (item.getStack().isEmpty()) {
            item.discard();
            info.cancel();
        }
        Dynamite.explode(item.getWorld(), null,
                item.getOwner() instanceof net.minecraft.entity.LivingEntity owner ? owner : null, item.getPos());
    }

    /** Les pierres et les minerais tombés au sol ne craignent pas la dynamite (voir {@link Dynamite#spares}). */
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void nowasDepths$dynamiteProof(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (Dynamite.spares(((ItemEntity) (Object) this).getStack())) {
            info.setReturnValue(false);
        }
    }
}
