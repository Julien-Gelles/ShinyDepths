package com.nowa.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.nowa.powers.power.Smelting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * Ce qu'un bloc cassé lâche, tiré de sa table de butin, pour celui qui l'a cassé et avec son outil.
 * Le pouvoir Fonderie y fait fondre ce qui peut l'être.
 */
@Mixin(Block.class)
public class BlockMixin {
    @ModifyReturnValue(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;",
            at = @At("RETURN"))
    private static List<ItemStack> nowasDepths$smelting(List<ItemStack> drops, BlockState state, ServerWorld world, BlockPos pos,
                                                       BlockEntity blockEntity, Entity breaker, ItemStack tool) {
        return Smelting.drops(drops, world, breaker, tool);
    }
}
