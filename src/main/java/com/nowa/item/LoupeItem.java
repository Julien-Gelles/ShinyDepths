package com.nowa.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Loupe de lapidaire : elle estime les pierres polies qui n'ont pas de pureté — celles de la table
 * qu'on a sorties du sachet sans les regarder, et surtout celles qu'on trouve dans les coffres, les
 * diamants et émeraudes du jeu compris. Elle tire comme le sachet ({@link Appraisal}), avec le
 * Coup d'œil et la Facette de celui qui la tient.
 *
 * <p>Deux façons de s'en servir :
 * <ul>
 *   <li>en main, clic droit : elle estime la pile tenue dans l'autre main ;</li>
 *   <li>dans l'inventaire, loupe tenue par la souris, clic droit sur une pile de pierres.</li>
 * </ul>
 * Chaque pierre estimée lui coûte un point d'usure ; quand il ne lui en reste pas assez, elle en
 * estime ce qu'elle peut et se brise.
 */
public class LoupeItem extends Item {
    public LoupeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack loupe = user.getStackInHand(hand);
        Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack stones = user.getStackInHand(otherHand);
        if (!GemPolishing.lacksPurity(stones)) {
            return TypedActionResult.pass(loupe);
        }
        if (user instanceof ServerPlayerEntity player) {
            List<ItemStack> appraised = appraise(loupe, stones, player);
            user.setStackInHand(otherHand, appraised.removeFirst());
            appraised.forEach(user.getInventory()::offerOrDrop);
        }
        // pas de coup de bras : la loupe et la pierre s'abaissent et remontent quand elles changent,
        // comme une pierre qu'on vient d'estimer
        return TypedActionResult.consume(loupe);
    }

    @Override
    public boolean onStackClicked(ItemStack loupe, Slot slot, ClickType clickType, PlayerEntity player) {
        ItemStack stones = slot.getStack();
        if (clickType != ClickType.RIGHT || !GemPolishing.lacksPurity(stones) || !slot.canTakeItems(player)) {
            return false;
        }
        if (player instanceof ServerPlayerEntity serverPlayer) {
            List<ItemStack> appraised = appraise(loupe, stones, serverPlayer);
            slot.setStack(appraised.removeFirst());
            appraised.forEach(player.getInventory()::offerOrDrop);
        }
        return true;
    }

    /**
     * Estime la pile, dans la limite de l'usure qui reste à la loupe, et l'use d'autant. Renvoie ce
     * que devient la pile : les pierres estimées, puis celles qu'il n'a pas pu regarder.
     */
    private static List<ItemStack> appraise(ItemStack loupe, ItemStack stones, ServerPlayerEntity player) {
        int count = loupe.isDamageable()
                ? Math.min(stones.getCount(), loupe.getMaxDamage() - loupe.getDamage())
                : stones.getCount();
        List<ItemStack> result = new ArrayList<>(Appraisal.appraise(stones.getItem(), count, player));
        if (count < stones.getCount()) {
            result.add(stones.copyWithCount(stones.getCount() - count));
        }
        loupe.damage(count, (ServerWorld) player.getWorld(), player, item -> { });
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_SPYGLASS_USE, SoundCategory.PLAYERS, 1.0f, 1.2f);
        return result;
    }
}
