package com.nowa.mixin;

import com.nowa.lapidary.LapidaryCrafting;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.CrafterScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * La fermeture de l'écran du générateur automatique : c'est le dernier joueur à le refermer que la
 * machine retient. L'écran du générateur ne redéfinit pas sa fermeture ; on l'attrape donc ici, là
 * où tous les écrans passent, et on ne s'occupe que du sien.
 */
@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @Inject(method = "onClosed", at = @At("HEAD"))
    private void shinyDepths$crafterClosed(PlayerEntity player, CallbackInfo info) {
        if ((Object) this instanceof CrafterScreenHandler crafter && player instanceof ServerPlayerEntity served
                && crafter.getInputInventory() instanceof CrafterBlockEntity block) {
            LapidaryCrafting.closed(served, block);
        }
    }
}
