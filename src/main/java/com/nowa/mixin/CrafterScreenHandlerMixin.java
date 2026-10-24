package com.nowa.mixin;

import com.nowa.lapidary.CrafterPreview;
import com.nowa.lapidary.LapidaryCrafting;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.screen.CrafterScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * L'aperçu du générateur automatique. L'écran cherche sa recette par le même chemin que la machine,
 * mais sans dire de quel bloc il s'agit : l'objet s'affichait donc en sortie même quand la machine
 * aurait refusé de le fabriquer. On donne la position le temps de la recherche, et l'aperçu peut
 * être recalculé quand le joueur retenu par la machine change.
 */
@Mixin(CrafterScreenHandler.class)
public abstract class CrafterScreenHandlerMixin extends ScreenHandler implements CrafterPreview {
    @Shadow
    @Final
    private RecipeInputInventory inputInventory;

    protected CrafterScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow
    private void updateResult() {
    }

    @Inject(method = "updateResult", at = @At("HEAD"))
    private void nowasDepths$beginPreview(CallbackInfo info) {
        // côté serveur, l'inventaire de l'écran est le bloc lui-même
        if (inputInventory instanceof CrafterBlockEntity crafter) {
            LapidaryCrafting.enterCrafter(crafter.getPos());
        }
    }

    @Inject(method = "updateResult", at = @At("RETURN"))
    private void nowasDepths$endPreview(CallbackInfo info) {
        LapidaryCrafting.leaveCrafter();
    }

    @Override
    public void nowasDepths$refreshPreview() {
        updateResult();
        sendContentUpdates();
    }
}
