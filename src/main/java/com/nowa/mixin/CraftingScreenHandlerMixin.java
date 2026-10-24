package com.nowa.mixin;

import com.nowa.lapidary.LapidaryCrafting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * L'équipement du lapidaire ne se forge qu'à partir d'un certain niveau. C'est le seul verrou du
 * métier : une fois l'outil fait, il sert sans condition, et quiconque le ramasse s'en sert aussi.
 *
 * <p>Le résultat est vidé plutôt que refusé au dernier moment, et un message dit lequel manque —
 * sans quoi l'établi resterait vide sans raison apparente.
 */
@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {
    @Inject(method = "updateResult", at = @At("RETURN"))
    private static void shinyDepths$lapidaryLevel(ScreenHandler handler, World world, PlayerEntity player,
                                                  RecipeInputInventory input, CraftingResultInventory result,
                                                  RecipeEntry<CraftingRecipe> recipe, CallbackInfo info) {
        ItemStack crafted = result.getStack(0);
        if (crafted.isEmpty() || LapidaryCrafting.canCraft(player, crafted)) {
            return;
        }
        result.setStack(0, ItemStack.EMPTY);
        handler.sendContentUpdates();
        if (player instanceof ServerPlayerEntity served) {
            LapidaryCrafting.explain(served, crafted);
        }
    }
}
