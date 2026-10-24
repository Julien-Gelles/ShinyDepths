package com.nowa.socket;

import com.mojang.serialization.Codec;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.ArrayList;
import java.util.List;

/**
 * Les pierres serties sur un objet, <em>chacune à son emplacement</em> : une pierre posée dans la
 * troisième case y reste, même si les deux premières sont vides. L'établi peut ainsi montrer, quand
 * on y repose l'objet, quelles cases sont prises et lesquelles restent libres.
 *
 * <p>Chaque pile est une pierre polie avec sa classe de pureté ; c'est elle qui décide de la
 * puissance de l'effet. Immuable : on la remplace à chaque changement, comme le contenu d'un sachet.
 *
 * <p>La sauvegarde est la liste des emplacements, les vides compris. Les objets sertis avant que les
 * emplacements ne comptent se lisent sans conversion : leurs pierres occupent les premières cases.
 */
public final class Sockets {
    public static final Sockets EMPTY = new Sockets(List.of());

    public static final Codec<Sockets> CODEC = ItemStack.OPTIONAL_CODEC.listOf().xmap(Sockets::new, Sockets::cells);
    public static final PacketCodec<RegistryByteBuf, Sockets> PACKET_CODEC =
            ItemStack.OPTIONAL_LIST_PACKET_CODEC.xmap(Sockets::new, Sockets::cells);

    /** Un élément par emplacement, vide s'il est libre ; jamais d'emplacement vide en fin de liste. */
    private final List<ItemStack> cells;
    /** Les pierres seules, dans l'ordre des emplacements : c'est ce que lisent les effets. */
    private final List<ItemStack> gems;

    public Sockets(List<ItemStack> cells) {
        List<ItemStack> kept = new ArrayList<>();
        for (ItemStack stack : cells) {
            kept.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        }
        while (!kept.isEmpty() && kept.getLast().isEmpty()) {
            kept.removeLast();
        }
        this.cells = List.copyOf(kept);
        this.gems = kept.stream().filter(stack -> !stack.isEmpty()).toList();
    }

    /** Les emplacements, vides compris, jusqu'au dernier occupé. */
    public List<ItemStack> cells() {
        return cells;
    }

    /** Les pierres serties, sans les emplacements vides. */
    public List<ItemStack> gems() {
        return gems;
    }

    /** Le nombre de pierres serties. */
    public int size() {
        return gems.size();
    }

    public boolean isEmpty() {
        return gems.isEmpty();
    }

    /** La pierre de cet emplacement, ou une pile vide s'il est libre. */
    public ItemStack get(int cell) {
        return cell >= 0 && cell < cells.size() ? cells.get(cell) : ItemStack.EMPTY;
    }

    public boolean isFree(int cell) {
        return get(cell).isEmpty();
    }

    /** La première pierre sertie, celle qui donne sa couleur à un bijou. */
    public ItemStack first() {
        return gems.isEmpty() ? ItemStack.EMPTY : gems.getFirst();
    }

    /** Le premier emplacement libre : sert à sertir sans établi, là où la case n'importe pas. */
    public int firstFree() {
        for (int cell = 0; cell < cells.size(); cell++) {
            if (cells.get(cell).isEmpty()) {
                return cell;
            }
        }
        return cells.size();
    }

    /** La même monture, avec cette pierre à cet emplacement. */
    public Sockets with(int cell, ItemStack gem) {
        List<ItemStack> copy = new ArrayList<>(cells);
        while (copy.size() <= cell) {
            copy.add(ItemStack.EMPTY);
        }
        copy.set(cell, gem.copyWithCount(1));
        return new Sockets(copy);
    }

    /** La même monture, cet emplacement libéré ; les autres pierres ne bougent pas. */
    public Sockets without(int cell) {
        if (cell < 0 || cell >= cells.size()) {
            return this;
        }
        List<ItemStack> copy = new ArrayList<>(cells);
        copy.set(cell, ItemStack.EMPTY);
        return new Sockets(copy);
    }

    /** Combien de fois cette pierre est déjà sertie : sert au rendement décroissant. */
    public int countOf(ItemStack gem) {
        return (int) gems.stream().filter(stone -> stone.isOf(gem.getItem())).count();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Sockets sockets && ItemStack.stacksEqual(cells, sockets.cells);
    }

    @Override
    public int hashCode() {
        return ItemStack.listHashCode(cells);
    }
}
