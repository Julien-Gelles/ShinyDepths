package com.nowa.jewelry;

import com.nowa.item.ModJewelry;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static com.nowa.NowasDepths.MOD_ID;

/**
 * Ce que les bijoux portés valent au joueur par leur seul métal : un peu d'armure, sertis ou non.
 *
 * <p>Un bijou n'est dans aucun emplacement d'équipement du jeu : rien ne s'applique de soi-même, et
 * il faut poser ce modificateur à la main. On repart chaque fois de zéro — on retire les nôtres,
 * puis on repose ceux des bijoux actuels — de sorte que rien ne puisse s'empiler ni rester accroché
 * à un doigt vide. Ce que valent les <em>pierres</em> d'un bijou se calcule ailleurs.
 */
public final class JewelryAttributes {
    private JewelryAttributes() {
    }

    /** Recalcule tout ce que les bijoux donnent à ce joueur. */
    public static void refresh(ServerPlayerEntity player) {
        EntityAttributeInstance armor = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
        if (armor == null) {
            return;
        }
        JewelryInventory worn = Jewelry.of(player);
        for (Worn place : Worn.values()) {
            armor.removeModifier(id(place));
            ModJewelry.Metal metal = ModJewelry.metalOf(worn.get(place).getItem());
            if (metal != null && metal.armor() > 0) {
                armor.addTemporaryModifier(new EntityAttributeModifier(id(place), metal.armor(),
                        EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    private static Identifier id(Worn place) {
        return Identifier.of(MOD_ID, "jewelry/" + place.asString() + "/armor");
    }
}
