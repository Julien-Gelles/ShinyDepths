package com.nowa.screen;

import com.nowa.block.entity.FusionCrucibleBlockEntity;
import com.nowa.item.GemFusion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

/**
 * L'écran du creuset de fusion, en peigne : les quatre coupelles en rangée en haut, leurs rigoles
 * qui se rejoignent et tombent dans la case du creuset, en bas au milieu (voir {@link FusionCrucibleScreen}).
 */
public class FusionCrucibleScreenHandler extends ScreenHandler {
    /** La première coupelle, l'écart d'une coupelle à la suivante, et la case du creuset. */
    public static final int CUPS_X = 41, CUPS_Y = 20, CUP_STEP = 26, OUTPUT_X = 80, OUTPUT_Y = 67;
    /** De combien la fenêtre est plus haute que celle du four du jeu. */
    public static final int GROW = 24;
    private static final int OWN_SLOTS = FusionCrucibleBlockEntity.SIZE;

    private final Inventory crucible;
    private final PropertyDelegate properties;
    private final ScreenHandlerContext context;

    /** Côté client : le contenu suit le serveur. */
    public FusionCrucibleScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(OWN_SLOTS), new ArrayPropertyDelegate(2), ScreenHandlerContext.EMPTY);
    }

    public FusionCrucibleScreenHandler(int syncId, PlayerInventory playerInventory, Inventory crucible,
                                       PropertyDelegate properties, ScreenHandlerContext context) {
        super(ModScreenHandlers.FUSION_CRUCIBLE, syncId);
        this.crucible = crucible;
        this.properties = properties;
        this.context = context;
        for (int slot = 0; slot < GemFusion.COUNT; slot++) {
            addSlot(new Slot(crucible, FusionCrucibleBlockEntity.FIRST_INPUT + slot, CUPS_X + CUP_STEP * slot, CUPS_Y) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return GemFusion.fusible(stack);
                }
            });
        }
        addSlot(new Slot(crucible, FusionCrucibleBlockEntity.OUTPUT, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + GROW + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142 + GROW));
        }
        addProperties(properties);
    }

    /** La part de la fusion en cours déjà faite, de 0 à 1. */
    public float progress() {
        int total = properties.get(1);
        return total <= 0 ? 0.0f : Math.min(1.0f, (float) properties.get(0) / total);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, com.nowa.block.ModBlocks.FUSION_CRUCIBLE);
    }

    /**
     * Le clic-majuscule : du creuset vers l'inventaire ; de l'inventaire vers les coupelles, réparti
     * de sorte qu'elles restent égales — une pile de huit pierres en met deux dans chacune.
     */
    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack moved = stack.copy();
        if (index < OWN_SLOTS) {
            if (!insertItem(stack, OWN_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!GemFusion.fusible(stack) || !spread(stack)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        slot.onTakeItem(player, stack);
        return moved;
    }

    /** Verse cette pile dans les coupelles, une pierre à la fois dans la moins remplie. Vrai si quelque chose est passé. */
    private boolean spread(ItemStack stack) {
        boolean any = false;
        while (!stack.isEmpty()) {
            Slot target = null;
            for (int index = 0; index < GemFusion.COUNT; index++) {
                Slot cup = slots.get(index);
                ItemStack there = cup.getStack();
                if (!there.isEmpty() && (!GemFusion.same(there, stack) || there.getCount() >= there.getMaxCount())) {
                    continue;
                }
                if (target == null || there.getCount() < target.getStack().getCount()) {
                    target = cup;
                }
            }
            if (target == null) {
                break;
            }
            if (target.getStack().isEmpty()) {
                target.setStack(stack.split(1));
            } else {
                target.getStack().increment(1);
                stack.decrement(1);
                target.markDirty();
            }
            any = true;
        }
        return any;
    }
}
