package com.nowa.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.nowa.world.GemBlockPurity;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Un bloc de pierres polies poussé par un piston emporte sa pureté. Elle est notée à la
 * position du bloc (voir {@link GemBlockPurity}) : quand le piston a décidé de ce qu'il déplace, on
 * déplace les notes du même pas, avant que les blocs ne bougent.
 */
@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Inject(method = "move", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/piston/PistonHandler;getBrokenBlocks()Ljava/util/List;"))
    private void nowasDepths$carryGemBlockPurity(World world, BlockPos pos, Direction dir, boolean retract,
                                                 CallbackInfoReturnable<Boolean> info, @Local PistonHandler handler) {
        // le sens du mouvement, tel que le piston le calcule lui-même quelques lignes plus bas
        GemBlockPurity.shift(world, handler.getMovedBlocks(), retract ? dir : dir.getOpposite());
    }
}
