package com.nowa.screen;

import com.nowa.block.entity.LapidaryTableBlockEntity;
import com.nowa.item.StonePouchItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Interface de la table (disposition du four) : pierre brute en haut à gauche, sachet vide en bas à gauche,
 * sachet rempli à droite. Retirer le sachet donne l'XP stockée et révèle la pureté des pierres
 * (Coup d'œil du joueur qui le retire).
 */
public class LapidaryTableScreenHandler extends ScreenHandler {
    private static final int TABLE_SLOTS = 3;
    private final Inventory inventory;
    private final PropertyDelegate properties;

    // côté client : la position de la table arrive avec l'ouverture de l'écran
    public LapidaryTableScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, inventoryAt(playerInventory, pos), new ArrayPropertyDelegate(2));
    }

    public LapidaryTableScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate properties) {
        super(ModScreenHandlers.LAPIDARY_TABLE, syncId);
        checkSize(inventory, TABLE_SLOTS);
        checkDataCount(properties, 2);
        this.inventory = inventory;
        this.properties = properties;
        inventory.onOpen(playerInventory.player);

        addInputSlot(inventory, LapidaryTableBlockEntity.INPUT, 68, 25);
        addInputSlot(inventory, LapidaryTableBlockEntity.POUCH, 68, 61);
        addOutputSlot(inventory, LapidaryTableBlockEntity.OUTPUT, 135, 43);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 100 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 158));
        }
        addProperties(properties);
    }

    private void addInputSlot(Inventory inventory, int index, int x, int y) {
        addSlot(new Slot(inventory, index, x, y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return inventory.isValid(index, stack);
            }
        });
    }

    private void addOutputSlot(Inventory inventory, int index, int x, int y) {
        addSlot(new Slot(inventory, index, x, y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            // comme la sortie du four : retirer le sac donne l'XP stockée ; ses pierres reçoivent leur pureté
            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    StonePouchItem.appraise(stack, serverPlayer);
                    if (inventory instanceof LapidaryTableBlockEntity table) {
                        table.grantStoredXp(serverPlayer);
                    }
                }
                super.onTakeItem(player, stack);
            }
        });
    }

    private static Inventory inventoryAt(PlayerInventory playerInventory, BlockPos pos) {
        return playerInventory.player.getWorld().getBlockEntity(pos) instanceof Inventory inventory ? inventory : new SimpleInventory(TABLE_SLOTS);
    }

    /** Le niveau de la table ouverte : il décide des pierres qu'elle sait tailler. */
    public int getLevel() {
        return inventory instanceof LapidaryTableBlockEntity table ? table.getLevel() : 1;
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
        if (slotIndex < TABLE_SLOTS) {                         // table -> inventaire du joueur
            if (!insertItem(stack, TABLE_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (inventory.isValid(LapidaryTableBlockEntity.INPUT, stack)) {   // inventaire -> pierre brute
            if (!insertItem(stack, LapidaryTableBlockEntity.INPUT, LapidaryTableBlockEntity.INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (inventory.isValid(LapidaryTableBlockEntity.POUCH, stack)) {   // inventaire -> sachet vide
            if (!insertItem(stack, LapidaryTableBlockEntity.POUCH, LapidaryTableBlockEntity.POUCH + 1, false)) {
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
