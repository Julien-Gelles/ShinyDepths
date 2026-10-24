package com.nowa.mixin;

import com.nowa.jewelry.client.MovableSlot;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Rend les coordonnées d'une case modifiables. Rien n'est injecté : le mixin n'ajoute qu'un
 * accesseur, dont seul l'onglet inventaire du créatif se sert pour replacer les cases à bijoux.
 */
@Mixin(Slot.class)
public class SlotMixin implements MovableSlot {
    @Mutable
    @Shadow
    @Final
    public int x;
    @Mutable
    @Shadow
    @Final
    public int y;

    @Override
    public void nowasDepths$moveTo(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }
}
