package com.nowa.mixin;

import com.nowa.item.ModDataComponents;
import com.nowa.world.GemBlockPurity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Un bloc de pierres polies qu'on pose garde la pureté de son objet (voir
 * {@link GemBlockPurity}). Poser un bloc sans pureté efface celle qu'un bloc précédent aurait
 * laissée au même endroit.
 */
@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "postPlacement", at = @At("HEAD"))
    private void shinyDepths$keepGemBlockPurity(BlockPos pos, World world, PlayerEntity player, ItemStack stack,
                                                BlockState state, CallbackInfoReturnable<Boolean> info) {
        if (!world.isClient() && GemBlockPurity.isGemBlock(state)) {
            GemBlockPurity.set(world, pos, stack.get(ModDataComponents.PURITY));
        }
    }
}
