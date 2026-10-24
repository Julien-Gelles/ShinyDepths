package com.nowa.mixin;

import com.nowa.jewelry.JewelryPanel;
import com.nowa.jewelry.Worn;
import com.nowa.powers.client.PowersPanel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Les cadres des cases à bijoux, dans la fenêtre d'inventaire au-dessus de la seconde main. La
 * texture du jeu n'en a pas à cet endroit : on les dessine par-dessus, une fois le fond posé.
 *
 * <p>S'y ajoute la pierre du pouvoir de la bague, dans le coin haut droit de la fenêtre (voir
 * {@link PowersPanel}).
 *
 * <p>Les cases font partie de la fenêtre : elles suivent le livre de recettes quand il la pousse,
 * et un clic sur elles reste un clic dans la fenêtre — pas un objet jeté par terre.
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    private InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void nowasDepths$jewelryFrames(DrawContext context, float delta, int mouseX, int mouseY,
                                           CallbackInfo info) {
        for (Worn place : Worn.values()) {
            context.drawTexture(JewelryPanel.TEXTURE,
                    this.x + JewelryPanel.survivalX(place) - 1, this.y + JewelryPanel.survivalY(place) - 1,
                    0, JewelryPanel.frameV(place), JewelryPanel.FRAME, JewelryPanel.FRAME,
                    JewelryPanel.TEXTURE_WIDTH, JewelryPanel.TEXTURE_HEIGHT);
        }
        PowersPanel.drawCorner(context, this.x + POWER_X, this.y + POWER_Y);
    }

    /**
     * Les effets venus d'un set prennent un contour à sa couleur, à droite de la fenêtre ; et
     * survoler la pierre du coin donne le set porté et ses niveaux.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void nowasDepths$setsTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo info) {
        PowersPanel.drawInventoryFrames(context, this.x + this.backgroundWidth + 2, this.y, this.width);
        PowersPanel.drawCornerTooltip(context, this.textRenderer, this.x + POWER_X, this.y + POWER_Y, mouseX, mouseY);
    }

    /** La pierre du pouvoir : dans le coin haut droit de la fenêtre, à la même distance du bord en haut et à droite. */
    @Unique
    private static final int POWER_GAP = 5, POWER_X = 176 - POWER_GAP - 16, POWER_Y = POWER_GAP;
}
