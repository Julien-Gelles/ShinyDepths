package com.nowa.lapidary.client;

import com.nowa.item.PouchContents;
import com.nowa.item.StonePouchItem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/** Infobulle du sachet à pierres, dans le style du bundle vanilla : toujours 4 cases. */
public class StonePouchTooltipComponent implements TooltipComponent {
    private static final Identifier BACKGROUND = Identifier.ofVanilla("container/bundle/background");
    private static final Identifier SLOT = Identifier.ofVanilla("container/bundle/slot");
    private static final int SLOT_WIDTH = 18;
    private static final int SLOT_HEIGHT = 20;

    private final PouchContents contents;

    public StonePouchTooltipComponent(PouchContents contents) {
        this.contents = contents;
    }

    @Override
    public int getHeight() {
        return SLOT_HEIGHT + 2 + 4;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return PouchContents.SLOTS * SLOT_WIDTH + 2;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        // une seule pierre sans pureté suffit : le sachet entier reste fermé, et ses cinq cases muettes
        boolean sealed = contents.stream().anyMatch(StonePouchItem::needsAppraisal);
        context.drawGuiTexture(BACKGROUND, x, y, getWidth(textRenderer), SLOT_HEIGHT + 2);
        for (int i = 0; i < PouchContents.SLOTS; i++) {
            int slotX = x + 1 + i * SLOT_WIDTH;
            int slotY = y + 1;
            context.drawGuiTexture(SLOT, slotX, slotY, SLOT_WIDTH, SLOT_HEIGHT);
            if (sealed) {
                drawUnknown(textRenderer, context, slotX + 1, slotY + 1);
            } else if (i < contents.size()) {
                ItemStack stack = contents.get(i);
                context.drawItem(stack, slotX + 1, slotY + 1);
                context.drawItemInSlot(textRenderer, stack, slotX + 1, slotY + 1);
            }
        }
    }

    /**
     * Tant que personne n'a récupéré le sachet, ses pierres n'ont pas de pureté — et tant qu'elles
     * n'en ont pas, elles n'ont rien à montrer : la case ne porte qu'un point d'interrogation.
     */
    private static void drawUnknown(TextRenderer textRenderer, DrawContext context, int x, int y) {
        String mark = "?";
        context.drawText(textRenderer, mark, x + (16 - textRenderer.getWidth(mark)) / 2, y + 4, 0xFFAAAAAA, true);
    }
}
