package com.nowa.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

/** Interface de la table : pierre brute à gauche, pierre polie à droite, inventaire du joueur en dessous. */
public class LapidaryTableScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate properties;

    // côté client : la position de la table arrive avec l'ouverture de l'écran
    public LapidaryTableScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, inventoryAt(playerInventory, pos), new ArrayPropertyDelegate(2));
    }

    public LapidaryTableScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate properties) {
        super(ModScreenHandlers.LAPIDARY_TABLE, syncId);
        checkSize(inventory, 2);
        checkDataCount(properties, 2);
        this.inventory = inventory;
        this.properties = properties;
        inventory.onOpen(playerInventory.player);

        addSlot(new Slot(inventory, 0, 56, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return inventory.isValid(0, stack);
            }
        });
        addSlot(new Slot(inventory, 1, 116, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
        addProperties(properties);
    }

    private static Inventory inventoryAt(PlayerInventory playerInventory, BlockPos pos) {
        return playerInventory.player.getWorld().getBlockEntity(pos) instanceof Inventory inventory ? inventory : new SimpleInventory(2);
    }

    /** Longueur de la flèche de progression, de 0 à 24 pixels. */
    public int getScaledProgress() {
        int progress = properties.get(0);
        int max = properties.get(1);
        return max != 0 && progress != 0 ? progress * 24 / max : 0;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (slotIndex < 2) {                                   // table -> inventaire du joueur
            if (!insertItem(stack, 2, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (inventory.isValid(0, stack)) {             // inventaire -> emplacement de la pierre brute
            if (!insertItem(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTakeItem(player, stack);
        return original;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }
}
