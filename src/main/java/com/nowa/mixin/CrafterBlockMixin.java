package com.nowa.mixin;

import com.nowa.lapidary.LapidaryCrafting;
import net.minecraft.block.BlockState;
import net.minecraft.block.CrafterBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Le générateur automatique tient son métier du dernier joueur à y avoir eu les mains — celui qui
 * l'a posé, puis chacun de ceux qui referment son écran (voir {@code ScreenHandlerMixin}) : il ne
 * sait fabriquer l'outillage du lapidaire que si ce joueur-là sait le faire. Casser le bloc efface
 * cette mémoire, et le bloc récupéré s'empile comme n'importe quel autre.
 *
 * <p>La recette se cherche dans une méthode statique, qui ne sait rien du bloc qui l'appelle : on
 * note donc la position le temps de la fabrication, au même endroit que le jeu la connaît. L'aperçu
 * de l'écran passe par la même recherche (voir {@code CrafterScreenHandlerMixin}) : ce que la
 * machine refuserait de fabriquer n'y apparaît donc pas non plus.
 */
@Mixin(CrafterBlock.class)
public class CrafterBlockMixin {
    @Inject(method = "onPlaced", at = @At("TAIL"))
    private void nowasDepths$rememberOwner(World world, BlockPos pos, BlockState state, LivingEntity placer,
                                           ItemStack stack, CallbackInfo info) {
        LapidaryCrafting.remember(world, pos, placer);
    }

    @Inject(method = "craft", at = @At("HEAD"))
    private void nowasDepths$beginCraft(BlockState state, ServerWorld world, BlockPos pos, CallbackInfo info) {
        LapidaryCrafting.enterCrafter(pos);
    }

    @Inject(method = "craft", at = @At("RETURN"))
    private void nowasDepths$endCraft(BlockState state, ServerWorld world, BlockPos pos, CallbackInfo info) {
        LapidaryCrafting.leaveCrafter();
    }

    @Inject(method = "getCraftingRecipe", at = @At("RETURN"), cancellable = true)
    private static void nowasDepths$someoneKnowsTheTrade(World world, CraftingRecipeInput input,
                                                         CallbackInfoReturnable<Optional<RecipeEntry<CraftingRecipe>>> info) {
        BlockPos pos = LapidaryCrafting.currentCrafter();
        Optional<RecipeEntry<CraftingRecipe>> found = info.getReturnValue();
        if (pos == null || found.isEmpty() || !(world instanceof ServerWorld server)) {
            return;
        }
        ItemStack result = found.get().value().getResult(world.getRegistryManager());
        if (!LapidaryCrafting.crafterCanCraft(server, pos, result)) {
            info.setReturnValue(Optional.empty());
        }
    }
}
