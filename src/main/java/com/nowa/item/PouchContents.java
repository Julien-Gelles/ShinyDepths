package com.nowa.item;

import com.mojang.serialization.Codec;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Contenu d'un sachet à pierres : jusqu'à 5 piles (une par case, soit une par classe de pureté), la plus récente en premier comme le bundle.
 * Immuable : on le remplace à chaque changement.
 */
public final class PouchContents {
    public static final int SLOTS = 5;
    public static final PouchContents EMPTY = new PouchContents(List.of());

    public static final Codec<PouchContents> CODEC = ItemStack.CODEC.listOf().xmap(PouchContents::new, PouchContents::stacks);
    public static final PacketCodec<RegistryByteBuf, PouchContents> PACKET_CODEC =
            ItemStack.LIST_PACKET_CODEC.xmap(PouchContents::new, PouchContents::stacks);

    private final List<ItemStack> stacks;

    public PouchContents(List<ItemStack> stacks) {
        this.stacks = stacks.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
    }

    /** Copie modifiable des piles. */
    public List<ItemStack> stacks() {
        return new ArrayList<>(stacks.stream().map(ItemStack::copy).toList());
    }

    public Stream<ItemStack> stream() {
        return stacks.stream();
    }

    public ItemStack get(int index) {
        return stacks.get(index);
    }

    public int size() {
        return stacks.size();
    }

    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PouchContents contents && ItemStack.stacksEqual(stacks, contents.stacks);
    }

    @Override
    public int hashCode() {
        return ItemStack.listHashCode(stacks);
    }
}
