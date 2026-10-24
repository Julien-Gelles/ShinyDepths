package com.nowa.jewelry;

import com.mojang.serialization.Codec;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Les bijoux portés par un joueur, une place par {@link Worn}. C'est un inventaire ordinaire, si
 * bien que les emplacements ajoutés à l'écran d'inventaire se synchronisent, se glissent et se
 * cliquent comme ceux d'une armure, sans un octet de réseau écrit à la main.
 *
 * <p>Le porteur n'est pas sauvegardé : il est retrouvé à chaque lecture, et ne sert qu'à savoir sur
 * qui reposer les effets quand le contenu change.
 */
public class JewelryInventory extends SimpleInventory {
    /**
     * Les clés sont lues comme de simples noms plutôt que comme des {@link Worn} : un nom inconnu
     * ferait sinon échouer toute la lecture, et le joueur perdrait tous ses bijoux pour un seul.
     */
    public static final Codec<JewelryInventory> CODEC =
            Codec.unboundedMap(Codec.STRING, ItemStack.CODEC).xmap(JewelryInventory::new, JewelryInventory::asMap);

    private @Nullable PlayerEntity owner;
    /** Ce que la sauvegarde contenait sans place où le mettre : rendu au joueur à son arrivée. */
    private final List<ItemStack> displaced = new ArrayList<>();

    public JewelryInventory() {
        super(Worn.values().length);
    }

    private JewelryInventory(Map<String, ItemStack> saved) {
        this();
        saved.forEach((name, stack) -> {
            Worn place = Worn.byName(name);
            if (place != null && get(place).isEmpty()) {
                setStack(place.ordinal(), stack);
            } else {
                displaced.add(stack);
            }
        });
    }

    /** Lie l'inventaire à son porteur, pour que les effets suivent ce qu'on y pose. */
    void bind(PlayerEntity player) {
        this.owner = player;
    }

    public ItemStack get(Worn place) {
        return getStack(place.ordinal());
    }

    public void set(Worn place, ItemStack stack) {
        setStack(place.ordinal(), stack);
    }

    /** Ce qu'occupe chaque place, les places vides omises : la sauvegarde reste courte. */
    private Map<String, ItemStack> asMap() {
        Map<String, ItemStack> worn = new LinkedHashMap<>();
        for (Worn place : Worn.values()) {
            ItemStack stack = get(place);
            if (!stack.isEmpty()) {
                worn.put(place.asString(), stack);
            }
        }
        return worn;
    }

    /** Vide et rend ce qui n'a pas trouvé de place à la lecture de la sauvegarde. */
    List<ItemStack> takeDisplaced() {
        List<ItemStack> taken = List.copyOf(displaced);
        displaced.clear();
        return taken;
    }

    /** Les pierres serties dans les bijoux d'une même forme. */
    public Map<Worn, ItemStack> of(com.nowa.item.ModJewelry.Kind kind) {
        Map<Worn, ItemStack> found = new EnumMap<>(Worn.class);
        for (Worn place : Worn.values()) {
            if (place.kind() == kind && !get(place).isEmpty()) {
                found.put(place, get(place));
            }
        }
        return found;
    }

    /**
     * Un bijou qu'on pose ou qu'on retire change aussitôt ce que le joueur vaut : les modificateurs
     * sont reposés ici, plutôt que relus à chaque tic.
     */
    @Override
    public void markDirty() {
        super.markDirty();
        if (owner instanceof ServerPlayerEntity served) {
            JewelryAttributes.refresh(served);
        }
    }
}
