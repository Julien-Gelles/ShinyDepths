package com.nowa.screen;

import com.nowa.block.entity.GemShowcaseBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

/**
 * L'écran de la vitrine à pierres : en haut ses cases, en bas l'inventaire du joueur. Une vitrine
 * seule a quinze cases, cinq par rangée ; une vitrine double en a trente, dix par rangée — les cinq
 * de gauche sont celles du bloc de gauche, comme on les voit sur le meuble.
 *
 * <p>Le même écran sert à tout le monde. Celui qui a posé la vitrine y range et y reprend ses
 * pierres ; les autres le voient en lecture seule — ils survolent les pierres pour les lire, mais
 * aucune case ne se laisse remplir ni vider.
 */
public class GemShowcaseScreenHandler extends ScreenHandler {
    /**
     * Ce que le serveur dit à l'écran en l'ouvrant.
     *
     * @param editable  ce joueur peut-il toucher aux pierres ?
     * @param ownerName le nom de celui qui a posé la vitrine, vide si elle n'est à personne
     * @param tended    un villageois lapidaire en a-t-il fait son étal ?
     * @param wide      est-ce une vitrine double ?
     */
    public record Opening(boolean editable, String ownerName, boolean tended, boolean wide) {
        public static final PacketCodec<ByteBuf, Opening> CODEC = PacketCodec.tuple(
                PacketCodecs.BOOL, Opening::editable, PacketCodecs.STRING, Opening::ownerName,
                PacketCodecs.BOOL, Opening::tended, PacketCodecs.BOOL, Opening::wide, Opening::new);
    }

    /** Le coin de la première case : la grille est centrée, celle d'une vitrine double prend toute la largeur. */
    public static final int GRID_X = 44, WIDE_GRID_X = 8;
    public static final int GRID_Y = 18;
    /** L'inventaire du joueur : centré lui aussi, donc décalé dans l'écran plus large de la vitrine double. */
    public static final int INVENTORY_X = 8, WIDE_INVENTORY_X = 17;
    private static final int COLUMNS = GemShowcaseBlockEntity.COLUMNS;
    private static final int ROWS = GemShowcaseBlockEntity.ROWS;

    private final boolean editable;
    private final String ownerName;
    private final boolean tended;
    private final boolean wide;
    /** Le nombre de cases de la vitrine : les premières de l'écran. */
    private final int gemSlots;
    private final @Nullable GemShowcaseBlockEntity showcase;
    private final @Nullable GemShowcaseBlockEntity partner;

    // côté client : le contenu arrive ensuite, case par case
    public GemShowcaseScreenHandler(int syncId, PlayerInventory playerInventory, Opening opening) {
        this(syncId, playerInventory, new SimpleInventory(GemShowcaseBlockEntity.SLOTS),
                opening.wide() ? new SimpleInventory(GemShowcaseBlockEntity.SLOTS) : null,
                opening.editable(), opening.ownerName(), opening.tended(), null, null);
    }

    /**
     * @param left     les pierres de la vitrine — ou de sa moitié gauche, vue de face
     * @param right    celles de la moitié droite, si la vitrine est double
     * @param showcase le bloc ouvert, et {@code partner} son autre moitié : côté serveur seulement
     */
    public GemShowcaseScreenHandler(int syncId, PlayerInventory playerInventory, Inventory left,
                                    @Nullable Inventory right, boolean editable, String ownerName, boolean tended,
                                    @Nullable GemShowcaseBlockEntity showcase, @Nullable GemShowcaseBlockEntity partner) {
        super(ModScreenHandlers.GEM_SHOWCASE, syncId);
        checkSize(left, GemShowcaseBlockEntity.SLOTS);
        this.editable = editable;
        this.ownerName = ownerName;
        this.tended = tended;
        this.wide = right != null;
        this.showcase = showcase;
        this.partner = partner;

        int columns = wide ? COLUMNS * 2 : COLUMNS;
        this.gemSlots = columns * ROWS;
        int gridX = wide ? WIDE_GRID_X : GRID_X;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < columns; col++) {
                // rangée par rangée sur toute la largeur : les cinq premières colonnes sont à gauche
                Inventory gems = col < COLUMNS ? left : right;
                addSlot(new Slot(gems, col % COLUMNS + row * COLUMNS, gridX + col * 18, GRID_Y + row * 18) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return GemShowcaseScreenHandler.this.editable && GemShowcaseBlockEntity.accepts(stack);
                    }

                    @Override
                    public boolean canTakeItems(PlayerEntity player) {
                        return GemShowcaseScreenHandler.this.editable;
                    }

                    @Override
                    public int getMaxItemCount() {
                        return 1;
                    }
                });
            }
        }
        int inventoryX = wide ? WIDE_INVENTORY_X : INVENTORY_X;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, inventoryX + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, inventoryX + col * 18, 142));
        }
    }

    public boolean editable() {
        return editable;
    }

    public String ownerName() {
        return ownerName;
    }

    public boolean tended() {
        return tended;
    }

    public boolean wide() {
        return wide;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (showcase == null) {
            return true;                                    // côté client
        }
        // L'écran se referme dès que ce qu'il montre n'est plus vrai : le villageois s'en va ou
        // s'installe, ou la vitrine change de forme — une moitié cassée, une voisine accolée.
        return !showcase.isRemoved() && showcase.isTended() == tended && showcase.partner() == partner
                && player.squaredDistanceTo(showcase.getPos().toCenterPos()) <= 64.0;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = slots.get(index);
        if (!editable || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack moved = stack.copy();
        if (index < gemSlots) {
            if (!insertItem(stack, gemSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!insertItem(stack, 0, gemSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        return moved;
    }
}
