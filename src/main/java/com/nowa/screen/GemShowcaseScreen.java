package com.nowa.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static com.nowa.NowasDepths.MOD_ID;

/**
 * Écran de la vitrine à pierres. Son titre dit à qui elle est quand ce n'est pas à soi — à un autre
 * joueur, ou au villageois lapidaire qui en a fait son étal : c'est ce qui explique que les cases
 * ne se laissent ni remplir ni vider.
 */
public class GemShowcaseScreen extends HandledScreen<GemShowcaseScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/gui/gem_showcase.png");
    /** L'écran d'une vitrine double : trente cases, dix par rangée, dans un panneau plus large. */
    private static final Identifier WIDE_TEXTURE = Identifier.of(MOD_ID, "textures/gui/gem_showcase_wide.png");
    private static final int WIDE_WIDTH = 194;

    public GemShowcaseScreen(GemShowcaseScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, readOnlyTitle(handler, title));
        if (handler.wide()) {
            // dix cases de front ne tiennent pas dans la largeur d'un écran du jeu
            backgroundWidth = WIDE_WIDTH;
            playerInventoryTitleX = GemShowcaseScreenHandler.WIDE_INVENTORY_X;
        }
    }

    /** « Vitrine du lapidaire » devant l'étal d'un villageois, « Vitrine de Nowa » devant celle d'un autre. */
    private static Text readOnlyTitle(GemShowcaseScreenHandler handler, Text title) {
        if (handler.tended()) {
            return Text.translatable("container.nowas-depths.gem_showcase.villager");
        }
        return handler.editable() || handler.ownerName().isEmpty()
                ? title
                : Text.translatable("container.nowas-depths.gem_showcase.owned", handler.ownerName());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(handler.wide() ? WIDE_TEXTURE : TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
