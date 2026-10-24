package com.nowa.mixin;

import com.nowa.item.ModJewelry;
import com.nowa.jewelry.JewelryPanel;
import com.nowa.jewelry.JewelrySlot;
import com.nowa.jewelry.Worn;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Les trois cases à bijoux s'ajoutent à l'inventaire du joueur. Ce sont de vraies cases du jeu, si
 * bien que la synchronisation, le glisser-déposer et le clic droit viennent d'eux-mêmes ; il ne
 * reste à écrire que le panneau qui les entoure, côté client.
 *
 * <p>Le jeu range ses propres cases jusqu'à la 45<sup>e</sup>. Les nôtres viennent après, et son
 * clic-maj les traite déjà correctement dans un sens : son dernier recours renvoie vers l'inventaire
 * principal, ce qui est exactement ce qu'on veut en sortant un bijou. Reste l'autre sens, qu'il
 * ignore, et qu'on ajoute ici.
 */
@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends ScreenHandler {
    protected PlayerScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nowasDepths$jewelrySlots(PlayerInventory inventory, boolean onServer, PlayerEntity owner,
                                          CallbackInfo info) {
        for (Worn place : Worn.values()) {
            addSlot(new JewelrySlot(owner, place, JewelryPanel.survivalX(place), JewelryPanel.survivalY(place)));
        }
    }

    /**
     * Un bijou qu'on prend dans son sac au clic-maj se met au doigt, comme une armure se met sur le
     * dos. Sans cela, il partirait dans la barre d'objets : le jeu ne connaît pas nos cases, et son
     * dernier recours est l'inventaire.
     */
    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void nowasDepths$wear(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> info) {
        if (index < PlayerScreenHandler.INVENTORY_START) {
            return;                                        // fabrication et armure : on n'y touche pas
        }
        Slot from = slots.get(index);
        if (from instanceof JewelrySlot || !from.hasStack()) {
            return;
        }
        ItemStack moved = from.getStack();
        if (ModJewelry.kindOf(moved.getItem()) == null) {
            return;                                        // ce n'est pas un bijou : au jeu de jouer
        }
        int first = nowasDepths$firstJewelrySlot();
        ItemStack before = moved.copy();
        if (first < 0 || !insertItem(moved, first, first + Worn.values().length, false)) {
            return;                                        // aucune place libre : le jeu s'en charge
        }
        if (moved.isEmpty()) {
            from.setStack(ItemStack.EMPTY);
        } else {
            from.markDirty();
        }
        from.onTakeItem(player, moved);
        info.setReturnValue(before);
    }

    /** Où commence notre colonne : cherchée plutôt que supposée, un autre mod pouvant ajouter des cases. */
    private int nowasDepths$firstJewelrySlot() {
        for (int index = 0; index < slots.size(); index++) {
            if (slots.get(index) instanceof JewelrySlot) {
                return index;
            }
        }
        return -1;
    }
}
