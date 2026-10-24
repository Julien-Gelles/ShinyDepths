package com.nowa.mixin;

import com.nowa.item.GemPolishing;
import com.nowa.item.ModDataComponents;
import com.nowa.item.Purity;
import com.nowa.world.GemBlockPurity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Un bloc de pierres polies rend sa pureté à l'objet qu'il lâche (voir
 * {@link GemBlockPurity}) : cassé à la main, à l'explosion, peu importe — tout butin de bloc passe
 * par ici. Les autres blocs ne sont pas concernés : on sort à la première ligne.
 */
@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "getDroppedStacks", at = @At("RETURN"))
    private void nowasDepths$dropGemBlockPurity(BlockState state, LootContextParameterSet.Builder builder,
                                                CallbackInfoReturnable<List<ItemStack>> info) {
        if (!GemBlockPurity.isGemBlock(state)) {
            return;
        }
        Vec3d origin = builder.getOptional(LootContextParameters.ORIGIN);
        if (origin == null) {
            return;
        }
        Purity purity = GemBlockPurity.take(builder.getWorld(), BlockPos.ofFloored(origin));
        if (purity == null) {
            return;
        }
        for (ItemStack stack : info.getReturnValue()) {
            if (stack.isOf(state.getBlock().asItem())) {
                stack.set(ModDataComponents.PURITY, purity);
                GemPolishing.applyGlint(stack);
            }
        }
    }
}
