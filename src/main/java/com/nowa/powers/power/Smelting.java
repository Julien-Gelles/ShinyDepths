package com.nowa.powers.power;

import com.nowa.powers.GemPower;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * La fonderie (ambre) : ce que l'on mine a une chance de tomber déjà fondu, comme sorti du four —
 * lingot à la place du brut, verre à la place du sable, pierre à la place de la pierre taillée. Sans
 * l'expérience du four.
 *
 * <p>Seulement à la pioche et à la pelle : à la hache, les bûches tourneraient en charbon de bois.
 * Chaque pile tirée par le bloc a sa propre chance, et fond toute entière.
 */
public final class Smelting {
    /** La chance de chaque pile de tomber fondue. */
    private static final PowerValue CHANCE = PowerValue.of("smelting.chance", 0.20, 0.40, 0.60, 0.80, 1.00);

    private Smelting() {
    }

    /** Ce que lâche un bloc cassé par {@code breaker} avec {@code tool} : ce qui en tombe vraiment. */
    public static List<ItemStack> drops(List<ItemStack> drops, ServerWorld world, @Nullable Entity breaker, ItemStack tool) {
        if (!(breaker instanceof PlayerEntity player) || drops.isEmpty()
                || !(tool.isIn(ItemTags.PICKAXES) || tool.isIn(ItemTags.SHOVELS))) {
            return drops;
        }
        int level = Powers.of(player).level(GemPower.SMELTING);
        if (level <= 0) {
            return drops;
        }
        double chance = CHANCE.at(level);
        List<ItemStack> result = new ArrayList<>(drops.size());
        for (ItemStack stack : drops) {
            result.add(world.getRandom().nextDouble() < chance ? smelted(world, stack) : stack);
        }
        return result;
    }

    /** Cette pile sortie du four, ou elle-même si le four n'en fait rien. */
    private static ItemStack smelted(ServerWorld world, ItemStack stack) {
        return world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, new SingleStackRecipeInput(stack), world)
                .map(entry -> entry.value().getResult(world.getRegistryManager()))
                .filter(result -> !result.isEmpty())
                .map(result -> result.copyWithCount(Math.min(result.getMaxCount(), result.getCount() * stack.getCount())))
                .orElse(stack);
    }
}
