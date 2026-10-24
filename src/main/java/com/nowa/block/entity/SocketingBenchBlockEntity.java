package com.nowa.block.entity;

import com.nowa.screen.SocketingBenchScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

/**
 * Ce que l'établi de sertisseur garde : l'objet et la pierre qu'on y a posés, et ce qu'un travail a
 * rendu sans qu'on l'ait encore repris. On peut fermer l'écran et y revenir plus tard, tout est resté
 * en place — et tout tombe si on casse l'établi. L'aperçu, lui, ne tombe pas : ce n'est qu'un
 * aperçu.
 *
 * <p>Ce contenu n'est <em>pas</em> un inventaire au sens du jeu : seul l'écran sait l'ouvrir, un
 * entonnoir ne peut rien y mettre ni y prendre.
 *
 * <p>Deux choses se voient sur le bloc, et sont donc envoyées à ceux qui le regardent : l'objet, que
 * porte le mannequin, et la première pierre, posée sous le poinçon de la machine.
 */
public class SocketingBenchBlockEntity extends BlockEntity {
    private static final String INPUT = "input", OUTPUT = "output", PENDING = "pending";
    private static final String SHOWN = "shown", SHOWN_GEAR = "shown_gear", SHOWN_GEM = "shown_gem";

    /** Une case de rangement qui prévient l'établi dès qu'on y touche. */
    private final class Stored extends SimpleInventory {
        private Stored(int size) {
            super(size);
        }

        @Override
        public void markDirty() {
            super.markDirty();
            changed();
        }
    }

    /** L'objet et la pierre : les cases d'entrée de l'écran. */
    private final SimpleInventory input = new Stored(SocketingBenchScreenHandler.INPUT_SLOTS);
    /** Les cases de sortie : un aperçu, ou ce qu'un travail a rendu et qu'on n'a pas repris. */
    private final SimpleInventory output = new Stored(SocketingBenchScreenHandler.OUTPUT_SLOTS);
    /** Les sorties sont-elles réelles ? Sinon ce n'est qu'un aperçu, qui ne tombe pas si l'on casse l'établi. */
    private boolean pending;

    private ItemStack shownGear = ItemStack.EMPTY;
    private ItemStack shownGem = ItemStack.EMPTY;

    public SocketingBenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOCKETING_BENCH, pos, state);
    }

    public SimpleInventory input() {
        return input;
    }

    public SimpleInventory output() {
        return output;
    }

    public boolean pending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
        markDirty();
    }

    /** Ce qui tombe si l'on casse l'établi : les entrées, et les sorties si elles sont réelles. */
    public SimpleInventory dropped() {
        SimpleInventory all = new SimpleInventory(input.size() + output.size());
        for (int slot = 0; slot < input.size(); slot++) {
            all.setStack(slot, input.getStack(slot));
        }
        if (pending) {
            for (int slot = 0; slot < output.size(); slot++) {
                all.setStack(input.size() + slot, output.getStack(slot));
            }
        }
        return all;
    }

    /** L'objet que porte le mannequin. */
    public ItemStack shownGear() {
        return shownGear;
    }

    /** La pierre posée sous le poinçon de la machine. */
    public ItemStack shownGem() {
        return shownGem;
    }

    /** Le contenu a changé : on le sauvegarde, et on montre ce qui se voit à ceux qui regardent. */
    private void changed() {
        markDirty();
        if (world != null && !world.isClient && refreshShown()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    /**
     * Ce qui se voit sur le bloc : l'objet sur le présentoir, et sur la machine la pierre de la case
     * de la pierre — rien si elle est vide. Dit si quelque chose a changé.
     */
    private boolean refreshShown() {
        ItemStack gear = input.getStack(SocketingBenchScreenHandler.ITEM);
        ItemStack gem = input.getStack(SocketingBenchScreenHandler.GEM);
        if (ItemStack.areEqual(gear, shownGear) && ItemStack.areItemsAndComponentsEqual(gem, shownGem)) {
            return false;
        }
        shownGear = gear.copy();
        shownGem = gem.copyWithCount(1);
        return true;
    }

    // ---------- sauvegarde ----------

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.put(INPUT, Inventories.writeNbt(new NbtCompound(), input.getHeldStacks(), registries));
        nbt.put(OUTPUT, Inventories.writeNbt(new NbtCompound(), output.getHeldStacks(), registries));
        nbt.putBoolean(PENDING, pending);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains(INPUT)) {
            // la sauvegarde : tout le contenu
            input.getHeldStacks().clear();
            output.getHeldStacks().clear();
            Inventories.readNbt(nbt.getCompound(INPUT), input.getHeldStacks(), registries);
            Inventories.readNbt(nbt.getCompound(OUTPUT), output.getHeldStacks(), registries);
            pending = nbt.getBoolean(PENDING);
            refreshShown();
        } else {
            // l'envoi au client : seulement ce qui se voit
            shownGear = ItemStack.fromNbtOrEmpty(registries, nbt.getCompound(SHOWN_GEAR));
            shownGem = ItemStack.fromNbtOrEmpty(registries, nbt.getCompound(SHOWN_GEM));
        }
    }

    // ---------- envoi au client pour le dessin ----------

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = new NbtCompound();
        // jamais vide : le client ignore une mise à jour sans contenu, et garderait l'objet d'avant
        nbt.putBoolean(SHOWN, true);
        if (!shownGear.isEmpty()) {
            nbt.put(SHOWN_GEAR, shownGear.encode(registries));
        }
        if (!shownGem.isEmpty()) {
            nbt.put(SHOWN_GEM, shownGem.encode(registries));
        }
        return nbt;
    }
}
