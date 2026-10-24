package com.nowa.screen;

import com.mojang.datafixers.util.Pair;
import com.nowa.block.entity.SocketingBenchBlockEntity;
import com.nowa.socket.SocketFamily;
import com.nowa.socket.Socketing;
import com.nowa.socket.Sockets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import static com.nowa.ShinyDepths.MOD_ID;

/**
 * L'établi de sertisseur, depuis la refonte du 2026-10-14 : un seul écran, deux entrées, deux sorties.
 *
 * <ul>
 *   <li><b>Une bague vide et une pierre</b> : la bague sertie sort.</li>
 *   <li><b>Une bague sertie, sans pierre</b> : la bague vide sort, et sa pierre à côté.</li>
 *   <li><b>Une bague sertie et une pierre</b> : la bague sertie de la nouvelle pierre sort, et
 *       l'ancienne à côté.</li>
 * </ul>
 * La lampe frontale suit les mêmes règles avec ses emplacements : une pierre va dans le premier
 * libre ; sans pierre on reprend la dernière posée ; lampe pleine, la nouvelle remplace la dernière.
 * Rien n'est tiré au sort : la pose et le retrait sont sûrs.
 *
 * <p>Tant qu'on n'a rien pris, les sorties ne sont qu'un aperçu. Prendre l'une d'elles fait le
 * travail : les entrées sont consommées, et l'autre sortie devient réelle — elle attend qu'on la
 * reprenne, et l'établi ne propose rien d'autre d'ici là. Tout ce qui est posé reste dans l'établi
 * quand on ferme l'écran (voir {@link SocketingBenchBlockEntity}).
 */
public class SocketingBenchScreenHandler extends ScreenHandler {
    /** Les cases d'entrée : l'objet à sertir, et la pierre. */
    public static final int ITEM = 0, GEM = 1, INPUT_SLOTS = 2;
    /** Les cases de sortie : l'objet, et la pierre qu'il rend. */
    public static final int OUT_ITEM = 0, OUT_GEM = 1, OUTPUT_SLOTS = 2;
    /**
     * Où sont les cases à l'écran : l'objet à gauche, les deux sorties à droite. La case de la pierre
     * se pose sur l'objet dessiné en grand, là où se tient sa pierre : l'écran la déplace selon
     * l'objet (voir {@link SocketingBenchScreen}) ; ceci est sa place sur une bague.
     */
    public static final int ITEM_X = 17, ITEM_Y = 50, GEM_X = 72, GEM_Y = 22;
    public static final int OUTPUT_X = 146, TOP_Y = 39, BOTTOM_Y = 61;
    /** De combien la fenêtre est plus haute que celle de l'établi du jeu. */
    public static final int GROW = 24;
    /** Les contours d'attente des cases vides : une bague, et le seul contour d'une pierre. */
    private static final Identifier GHOST_ITEM = Identifier.of(MOD_ID, "item/empty_slot_ring");
    private static final Identifier GHOST_GEM = Identifier.of(MOD_ID, "item/empty_slot_gem");
    private static final int BENCH_SLOTS = INPUT_SLOTS + OUTPUT_SLOTS;
    /** Propriété synchronisée : les sorties sont-elles réelles, en attente qu'on les reprenne ? */
    private static final int PROP_PENDING = 0;

    private final ScreenHandlerContext context;
    private final PropertyDelegate properties = new ArrayPropertyDelegate(1);
    private final @Nullable SocketingBenchBlockEntity bench;
    private final SimpleInventory input;
    private final SimpleInventory output;
    private final InventoryChangedListener onChange = this::onContentChanged;
    /** Pendant qu'on écrit l'aperçu dans les sorties, on ne se réécoute pas. */
    private boolean previewing;

    public SocketingBenchScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY, null);
    }

    public SocketingBenchScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context,
                                       @Nullable SocketingBenchBlockEntity bench) {
        super(ModScreenHandlers.SOCKETING_BENCH, syncId);
        this.context = context;
        this.bench = bench;
        this.input = bench != null ? bench.input() : new SimpleInventory(INPUT_SLOTS);
        this.output = bench != null ? bench.output() : new SimpleInventory(OUTPUT_SLOTS);
        // seules les entrées refont l'aperçu : une sortie qu'on prend ne doit pas le faire renaître
        input.addListener(onChange);

        addSlot(new Slot(input, ITEM, ITEM_X, ITEM_Y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return Socketing.capacity(stack) > 0;
            }

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, GHOST_ITEM);
            }

            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });
        // la case de la pierre n'existe que sur un objet posé (ou tant qu'elle tient encore une pierre)
        addSlot(new Slot(input, GEM, GEM_X, GEM_Y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return Socketing.isSettable(stack) && !input.getStack(ITEM).isEmpty();
            }

            @Override
            public boolean isEnabled() {
                return !input.getStack(ITEM).isEmpty() || hasStack();
            }

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, GHOST_GEM);
            }
        });
        addOutputSlot(OUT_ITEM, TOP_Y);
        addOutputSlot(OUT_GEM, BOTTOM_Y);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + GROW + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142 + GROW));
        }
        addProperties(properties);
        if (bench != null) {
            properties.set(PROP_PENDING, bench.pending() ? 1 : 0);
            onContentChanged(input);
        }
    }

    private void addOutputSlot(int index, int y) {
        addSlot(new Slot(output, index, OUTPUT_X, y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity taker, ItemStack stack) {
                taken(index);
                super.onTakeItem(taker, stack);
            }
        });
    }

    /** L'objet posé à gauche : l'écran le dessine en grand sous la case de la pierre. */
    public ItemStack item() {
        return input.getStack(ITEM);
    }

    /** Les sorties sont-elles réelles, en attente qu'on les reprenne ? Sinon ce n'est qu'un aperçu. */
    public boolean pending() {
        return properties.get(PROP_PENDING) != 0;
    }

    private void setPending(boolean pending) {
        properties.set(PROP_PENDING, pending ? 1 : 0);
        if (bench != null) {
            bench.setPending(pending);
        }
    }

    // ---------- l'aperçu ----------

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (previewing) {
            return;
        }
        if (pending()) {
            if (output.isEmpty()) {
                setPending(false);                            // tout est repris : l'établi propose de nouveau
            } else {
                sendContentUpdates();
                return;
            }
        }
        ItemStack[] preview = preview(input.getStack(ITEM), input.getStack(GEM));
        previewing = true;
        output.setStack(OUT_ITEM, preview[OUT_ITEM]);
        output.setStack(OUT_GEM, preview[OUT_GEM]);
        previewing = false;
        sendContentUpdates();
    }

    /** Ce que l'établi rendrait de cet objet et de cette pierre : l'objet, et la pierre qu'il lâche. Deux vides s'il n'y a rien à faire. */
    private static ItemStack[] preview(ItemStack item, ItemStack gem) {
        ItemStack[] none = {ItemStack.EMPTY, ItemStack.EMPTY};
        if (item.isEmpty() || Socketing.capacity(item) <= 0) {
            return none;
        }
        Sockets sockets = Socketing.get(item);
        ItemStack result = item.copyWithCount(1);
        if (gem.isEmpty()) {
            int last = lastOccupied(sockets);
            if (last < 0) {
                return none;                                  // rien à reprendre
            }
            return new ItemStack[]{result, removeInto(result, last)};
        }
        if (!Socketing.fits(item, gem)) {
            return none;
        }
        if (Socketing.freeSlots(item) > 0) {
            Socketing.set(result, sockets.firstFree(), gem, null);
            return new ItemStack[]{result, ItemStack.EMPTY};
        }
        int last = lastOccupied(sockets);
        if (ItemStack.areItemsAndComponentsEqual(sockets.get(last), gem.copyWithCount(1))) {
            return none;                                      // la même pierre : il n'y a rien à changer
        }
        ItemStack old = removeInto(result, last);
        Socketing.set(result, last, gem, null);
        return new ItemStack[]{result, old};
    }

    private static int lastOccupied(Sockets sockets) {
        for (int cell = sockets.size() - 1; cell >= 0; cell--) {
            if (!sockets.isFree(cell)) {
                return cell;
            }
        }
        return -1;
    }

    private static ItemStack removeInto(ItemStack result, int cell) {
        return Socketing.remove(result, cell);
    }

    // ---------- le travail ----------

    /**
     * Une sortie vient d'être prise. Si c'était l'aperçu, le travail se fait : l'objet et une pierre
     * sont consommés, et l'autre sortie reste, réelle, jusqu'à ce qu'on la reprenne.
     */
    private void taken(int index) {
        if (pending()) {
            if (output.isEmpty()) {
                setPending(false);
                onContentChanged(input);
            }
            return;
        }
        boolean usedGem = !input.getStack(GEM).isEmpty();
        previewing = true;
        input.setStack(ITEM, ItemStack.EMPTY);
        if (usedGem) {
            input.getStack(GEM).decrement(1);
            input.markDirty();
        }
        previewing = false;
        setPending(!output.getStack(1 - index).isEmpty());
        context.run((world, pos) -> world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.8f,
                world.getRandom().nextFloat() * 0.1f + 0.9f));
        onContentChanged(input);
    }

    // ---------- plomberie ----------

    @Override
    public boolean canUse(PlayerEntity user) {
        return canUse(context, user, com.nowa.block.ModBlocks.SOCKETING_BENCH);
    }

    @Override
    public void onClosed(PlayerEntity closing) {
        super.onClosed(closing);
        input.removeListener(onChange);
    }

    @Override
    public ItemStack quickMove(PlayerEntity mover, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack moved = stack.copy();
        if (index < BENCH_SLOTS) {
            if (!insertItem(stack, BENCH_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int target = SocketFamily.of(stack) != null ? ITEM : Socketing.isSettable(stack) ? GEM : -1;
            if (target < 0 || !insertItem(stack, target, target + 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        slot.onTakeItem(mover, moved);
        return moved;
    }
}
