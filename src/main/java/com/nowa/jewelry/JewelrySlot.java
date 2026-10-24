package com.nowa.jewelry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * Une case à bijou : elle n'accepte que sa forme, et jamais plus d'un objet.
 *
 * <p>Elle ne retient pas l'inventaire, seulement le joueur, et le redemande à chaque accès. C'est
 * nécessaire : le jeu construit l'écran d'inventaire d'un joueur <em>dans son constructeur</em>,
 * donc avant d'avoir lu sa sauvegarde, et l'inventaire qui existait alors n'est pas celui que la
 * sauvegarde rend ensuite. Une case qui l'aurait gardé montrerait des doigts vides après chaque
 * reconnexion.
 */
public class JewelrySlot extends Slot {
    private final PlayerEntity owner;
    private final Worn place;

    public JewelrySlot(PlayerEntity owner, Worn place, int x, int y) {
        super(Jewelry.of(owner), place.ordinal(), x, y);
        this.owner = owner;
        this.place = place;
    }

    public Worn place() {
        return place;
    }

    private JewelryInventory worn() {
        return Jewelry.of(owner);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return place.accepts(stack);
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public ItemStack getStack() {
        return worn().get(place);
    }

    @Override
    public void setStack(ItemStack stack) {
        setStack(stack, ItemStack.EMPTY);
    }

    @Override
    public void setStack(ItemStack stack, ItemStack previous) {
        worn().set(place, stack);
    }

    @Override
    public void setStackNoCallbacks(ItemStack stack) {
        setStack(stack);
    }

    @Override
    public ItemStack takeStack(int amount) {
        return worn().removeStack(place.ordinal(), amount);
    }

    @Override
    public void markDirty() {
        worn().markDirty();
    }
}
