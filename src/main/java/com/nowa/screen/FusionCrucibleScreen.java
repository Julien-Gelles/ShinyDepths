package com.nowa.screen;

import com.nowa.item.GemFusion;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static com.nowa.ShinyDepths.MOD_ID;

/**
 * L'écran du creuset de fusion, en peigne. Le fond reprend celui du four du jeu, plus haut : les
 * quatre coupelles en rangée en haut, sous chacune une rigole creusée comme une case ; elles se
 * rejoignent en un collecteur, et une seule tombe dans la grande case du creuset. Pendant la fusion
 * la lave les remplit à la place de la flèche du four : elle descend sous les coupelles, court le
 * long du collecteur vers le milieu, puis tombe dans le creuset.
 */
public class FusionCrucibleScreen extends HandledScreen<FusionCrucibleScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/gui/fusion_crucible.png");
    /** Les rigoles pleines de lave sont peintes hors du panneau, à la même place décalée de tant vers le bas. */
    private static final int LAVA_DY = 180;
    /**
     * Le fond des rigoles (en pixels du panneau) : chaque descente sous une coupelle, le collecteur,
     * la chute vers le creuset (son milieu est entre les colonnes 87 et 88).
     */
    private static final int DROP_TOP = 38, DROP_HEIGHT = 12, RUN_Y = 50, FALL_X = 87, FALL_TOP = 52, FALL_HEIGHT = 10;
    /** Où en est la fusion quand la lave atteint le collecteur, puis la chute. */
    private static final float RUN_AT = 0.3f, FALL_AT = 0.75f;

    public FusionCrucibleScreen(FusionCrucibleScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundHeight += FusionCrucibleScreenHandler.GROW;
        playerInventoryTitleY = backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        float progress = handler.progress();
        if (progress <= 0.0f) {
            return;
        }
        float drop = Math.min(1.0f, progress / RUN_AT);
        float run = clamp((progress - RUN_AT) / (FALL_AT - RUN_AT));
        float fall = clamp((progress - FALL_AT) / (1.0f - FALL_AT));
        for (int cup = 0; cup < GemFusion.COUNT; cup++) {
            int center = FusionCrucibleScreenHandler.CUPS_X + FusionCrucibleScreenHandler.CUP_STEP * cup + 8;
            lava(context, center - 1, DROP_TOP, 2, Math.round(DROP_HEIGHT * drop));
            // le long du collecteur jusqu'au-dessus de la chute, croisement compris
            if (center < FALL_X) {                                  // vers la droite
                lava(context, center - 1, RUN_Y, Math.round((FALL_X + 2 - (center - 1)) * run), 2);
            } else {                                                // vers la gauche
                int width = Math.round((center + 1 - FALL_X) * run);
                lava(context, center + 1 - width, RUN_Y, width, 2);
            }
        }
        lava(context, FALL_X, FALL_TOP, 2, Math.round(FALL_HEIGHT * fall));
    }

    /** Découvre la lave sur ce morceau de rigole (en pixels du panneau). */
    private void lava(DrawContext context, int u, int v, int width, int height) {
        if (width > 0 && height > 0) {
            context.drawTexture(TEXTURE, x + u, y + v, u, v + LAVA_DY, width, height);
        }
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
