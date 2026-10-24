package com.nowa.mixin;

import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * La case du joueur que recopie une case de l'onglet inventaire du créatif. La classe n'est pas
 * publique : c'est le seul moyen de savoir qu'une copie montre un bijou.
 */
@Mixin(targets = "net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen$CreativeSlot")
public interface CreativeSlotAccessor {
    @Accessor("slot")
    Slot shinyDepths$slot();
}
