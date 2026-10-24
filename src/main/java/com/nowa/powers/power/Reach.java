package com.nowa.powers.power;

import com.nowa.powers.GemPower;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import static com.nowa.NowasDepths.MOD_ID;

/**
 * La portée (jade) : on atteint les blocs et les créatures de plus loin — pour casser, poser, ouvrir
 * et frapper. Ce sont les deux portées du jeu, allongées d'autant ; le client les reçoit du serveur.
 */
public final class Reach {
    /** Les blocs gagnés. */
    private static final PowerValue EXTRA = PowerValue.of("reach.blocks", 0.5, 1.0, 1.5, 2.0, 2.5);
    private static final Identifier MODIFIER = Identifier.of(MOD_ID, "power_reach");

    private Reach() {
    }

    /** Remet les portées du joueur d'accord avec sa bague. Côté serveur. */
    public static void update(PlayerEntity player) {
        double extra = EXTRA.at(Powers.of(player).level(GemPower.REACH));
        set(player, EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE, extra);
        set(player, EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, extra);
    }

    private static void set(PlayerEntity player, RegistryEntry<EntityAttribute> attribute, double value) {
        EntityAttributeInstance instance = player.getAttributeInstance(attribute);
        if (instance == null) {
            return;
        }
        EntityAttributeModifier current = instance.getModifier(MODIFIER);
        if (current == null ? value == 0.0 : current.value() == value) {
            return;
        }
        instance.removeModifier(MODIFIER);
        if (value != 0.0) {
            instance.addTemporaryModifier(new EntityAttributeModifier(MODIFIER, value, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }
}
