package com.nowa.jewelry;

import com.nowa.item.ModJewelry.Kind;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

/**
 * Ce qu'un joueur porte en plus de son armure : une bague, au-dessus de la seconde main.
 *
 * <p>Le nom sert de clé dans la sauvegarde. Les colliers et bracelets d'avant la refonte n'ont plus
 * de place : à la lecture, ils reviennent au joueur — s'ils existent encore.
 */
public enum Worn implements StringIdentifiable {
    RING("ring", Kind.RING);

    private final String name;
    private final Kind kind;

    Worn(String name, Kind kind) {
        this.name = name;
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    /** Cet emplacement accepte-t-il ce bijou ? Le tag laisse la porte ouverte aux autres mods. */
    public boolean accepts(ItemStack stack) {
        return stack.isIn(kind.tag());
    }

    /** La place réservée à une forme : il n'y en a qu'une par forme. */
    public static Worn of(Kind kind) {
        for (Worn place : values()) {
            if (place.kind == kind) {
                return place;
            }
        }
        throw new IllegalArgumentException("Aucune place pour " + kind);
    }

    /**
     * La place portant ce nom dans une sauvegarde. Les premières versions avaient deux bagues :
     * la première retrouve la place unique, la seconde n'en a plus et revient au joueur.
     */
    public static @Nullable Worn byName(String name) {
        if (name.equals("first_ring")) {
            return RING;
        }
        for (Worn place : values()) {
            if (place.name.equals(name)) {
                return place;
            }
        }
        return null;
    }

    @Override
    public String asString() {
        return name;
    }
}
