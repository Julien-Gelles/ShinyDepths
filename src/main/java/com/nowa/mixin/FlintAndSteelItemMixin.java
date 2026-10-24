package com.nowa.mixin;

import com.nowa.dynamite.Dynamite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Briquet dans une main, dynamite dans l'autre : le clic allume la mèche. Quand le briquet est dans
 * la main principale et qu'on vise un bloc, le jeu sert d'abord le briquet et mettrait le feu au
 * sol ; on allume la dynamite à la place. (Visé dans le vide, ou dynamite en main principale, c'est
 * la dynamite elle-même qui reçoit le clic.)
 */
@Mixin(FlintAndSteelItem.class)
public class FlintAndSteelItemMixin {
    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void shinyDepths$lightDynamite(ItemUsageContext context, CallbackInfoReturnable<ActionResult> info) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return;
        }
        Hand other = context.getHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (player.getStackInHand(other).isOf(Dynamite.ITEM) && !Dynamite.isLit(player.getStackInHand(other))) {
            Dynamite.light(context.getWorld(), player, other);
            info.setReturnValue(ActionResult.success(context.getWorld().isClient()));
        }
    }
}
