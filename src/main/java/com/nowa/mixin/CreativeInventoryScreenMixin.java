package com.nowa.mixin;

import com.nowa.jewelry.JewelryPanel;
import com.nowa.jewelry.JewelrySlot;
import com.nowa.jewelry.Worn;
import com.nowa.jewelry.client.MovableSlot;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Les bijoux dans l'onglet inventaire du créatif.
 *
 * <p>Cet onglet recopie toutes les cases de l'inventaire du joueur et les place lui-même, d'après
 * leur numéro. Il ne connaît pas les nôtres : il les rangeait donc avec la barre d'objets, où l'on
 * voyait les bijoux par-dessus les objets tenus. On les replace après lui, à droite du portrait, et
 * on dessine leurs cadres — la texture de l'onglet n'en a pas là.
 */
@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin
        extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {
    private CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler handler,
                                         PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "setSelectedTab", at = @At("TAIL"))
    private void shinyDepths$placeJewelry(ItemGroup group, CallbackInfo info) {
        if (!((CreativeInventoryScreen) (Object) this).isInventoryTabSelected()) {
            return;
        }
        for (Slot slot : this.handler.slots) {
            if (slot instanceof CreativeSlotAccessor copy && copy.shinyDepths$slot() instanceof JewelrySlot jewel) {
                ((MovableSlot) slot).shinyDepths$moveTo(
                        JewelryPanel.creativeX(jewel.place()), JewelryPanel.creativeY(jewel.place()));
            }
        }
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void shinyDepths$jewelryFrames(DrawContext context, float delta, int mouseX, int mouseY,
                                           CallbackInfo info) {
        if (!((CreativeInventoryScreen) (Object) this).isInventoryTabSelected()) {
            return;
        }
        for (Worn place : Worn.values()) {
            context.drawTexture(JewelryPanel.TEXTURE,
                    this.x + JewelryPanel.creativeX(place) - 1, this.y + JewelryPanel.creativeY(place) - 1,
                    0, JewelryPanel.frameV(place), JewelryPanel.FRAME, JewelryPanel.FRAME,
                    JewelryPanel.TEXTURE_WIDTH, JewelryPanel.TEXTURE_HEIGHT);
        }
    }
}
