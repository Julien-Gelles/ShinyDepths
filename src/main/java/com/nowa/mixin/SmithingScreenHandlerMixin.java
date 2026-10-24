package com.nowa.mixin;

import com.nowa.lapidary.LapidaryCrafting;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Le même verrou qu'à l'établi, pour la table de forgeron : la table de lapidaire en netherite s'y
 * améliore, et elle demande le niveau comme les autres.
 *
 * <p>La classe hérite du gabarit de forge pour voir {@code player} et {@code output}, que le jeu y
 * déclare ; le constructeur n'est là que pour la compilation et ne sert jamais.
 */
@Mixin(SmithingScreenHandler.class)
public abstract class SmithingScreenHandlerMixin extends ForgingScreenHandler {
    public SmithingScreenHandlerMixin(ScreenHandlerType<?> type, int syncId, PlayerInventory inventory,
                                      ScreenHandlerContext context) {
        super(type, syncId, inventory, context);
    }

    @Inject(method = "updateResult", at = @At("RETURN"))
    private void shinyDepths$lapidaryLevel(CallbackInfo info) {
        ItemStack crafted = this.output.getStack(0);
        if (crafted.isEmpty() || LapidaryCrafting.canCraft(this.player, crafted)) {
            return;
        }
        this.output.setStack(0, ItemStack.EMPTY);
        if (this.player instanceof ServerPlayerEntity served) {
            LapidaryCrafting.explain(served, crafted);
        }
    }
}
