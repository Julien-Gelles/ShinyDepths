package com.nowa.screen;

import com.nowa.block.entity.LapidaryTableBlockEntity;
import com.nowa.item.GemPolishing;
import com.nowa.item.StonePouchItem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static com.nowa.NowasDepths.MOD_ID;

/**
 * Écran de la table de lapidaire : le fond du four, sans combustible ni flamme.
 *
 * <p>À gauche, les pierres que cette table-là sait tailler, posées en grappe. Rien n'y est
 * cliquable : c'est la réponse à « pourquoi mon rubis ne passe pas ici », donnée avant la question.
 */
public class LapidaryTableScreen extends HandledScreen<LapidaryTableScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/gui/lapidary_table.png");
    /** La grappe occupe la marge gauche, entre le titre et l'étiquette de l'inventaire. */
    private static final int CLUSTER_LEFT = 9;
    private static final int CLUSTER_WIDTH = 57;        // la case d'entrée commence à 67
    private static final int CLUSTER_TOP = 20;
    private static final int CLUSTER_HEIGHT = 60;
    private static final int STEP_X = 17;
    private static final int STEP_Y = 18;
    private static final int PER_ROW = 3;

    private int shownLevel = -1;
    private List<Item> gems = List.of();

    public LapidaryTableScreen(LapidaryTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundHeight = 182;                         // seize pixels de plus que le gabarit du four
        playerInventoryTitleY = backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    /** La grappe ne change qu'avec la table ouverte : on la recalcule seulement à ce moment-là. */
    private void updateGems() {
        if (shownLevel != handler.getLevel()) {
            shownLevel = handler.getLevel();
            gems = GemPolishing.tableGems(shownLevel);
        }
    }

    /**
     * Les dispositions particulières : trois pierres se posent aux sommets d'un triangle, six en
     * pentagone avec la dernière au centre. Coordonnées du coin haut-gauche de chaque icône,
     * relatives à la grappe, dans le sens des aiguilles d'une montre en partant du haut.
     */
    private static final int[][] TRIANGLE = {{20, 12}, {31, 32}, {10, 32}};
    private static final int[][] PENTAGON = {{18, 3}, {35, 16}, {30, 37}, {7, 37}, {2, 16}, {18, 22}};

    /** Coin haut-gauche de la pierre numéro {@code index}, relatif au coin de l'écran. */
    private int[] gemSlot(int index) {
        int[][] shape = gems.size() == TRIANGLE.length ? TRIANGLE
                : gems.size() == PENTAGON.length ? PENTAGON : null;
        if (shape != null) {
            return new int[]{CLUSTER_LEFT + shape[index][0], CLUSTER_TOP + shape[index][1]};
        }
        int rows = Math.max(1, (gems.size() + PER_ROW - 1) / PER_ROW);
        int row = 0;
        int seen = 0;
        int inRow = 0;
        for (; row < rows; row++) {
            inRow = gems.size() / rows + (row < gems.size() % rows ? 1 : 0);
            if (index < seen + inRow) {
                break;
            }
            seen += inRow;
        }
        int rowWidth = (inRow - 1) * STEP_X + 16;
        int top = CLUSTER_TOP + (CLUSTER_HEIGHT - (rows - 1) * STEP_Y - 16) / 2;
        return new int[]{CLUSTER_LEFT + (CLUSTER_WIDTH - rowWidth) / 2 + (index - seen) * STEP_X,
                top + row * STEP_Y};
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        updateGems();
        for (int index = 0; index < gems.size(); index++) {
            int[] at = gemSlot(index);
            context.drawItem(new ItemStack(gems.get(index)), x + at[0], y + at[1]);
        }
        int progress = handler.getScaledProgress();
        if (progress > 0) {
            context.drawTexture(TEXTURE, x + 91, y + 43, 176, 14, progress + 1, 16);   // flèche, comme le four
        }
    }

    /**
     * Ce que la case de sortie contient vraiment est un sachet — c'est lui qu'on prend, lui qu'un
     * entonnoir emporte. Mais tant qu'il est là, on montre les pierres qu'il tient : elles disent
     * mieux ce que la table vient de faire.
     */
    private ItemStack outputPreview() {
        return StonePouchItem.singleUnappraised(handler.getSlot(LapidaryTableBlockEntity.OUTPUT).getStack());
    }

    /** On dessine la pierre au lieu du sachet : il n'est donc jamais peint, pas même dessous. */
    @Override
    protected void drawSlot(DrawContext context, Slot slot) {
        ItemStack stones = slot.id == LapidaryTableBlockEntity.OUTPUT ? outputPreview() : ItemStack.EMPTY;
        if (stones.isEmpty()) {
            super.drawSlot(context, slot);
            return;
        }
        context.drawItem(stones, slot.x, slot.y);
        context.drawItemInSlot(textRenderer, stones, slot.x, slot.y);   // le nombre, comme dans une case
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        ItemStack stones = outputPreview();
        if (!stones.isEmpty() && focusedSlot == handler.getSlot(LapidaryTableBlockEntity.OUTPUT)) {
            context.drawItemTooltip(textRenderer, stones, mouseX, mouseY);
            return;                                              // l'infobulle parle de la pierre, pas du sachet
        }
        drawMouseoverTooltip(context, mouseX, mouseY);
        if (focusedSlot != null) {
            return;                                     // une case survolée a déjà son infobulle
        }
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        for (int index = 0; index < gems.size(); index++) {
            int[] at = gemSlot(index);
            if (mouseX >= x + at[0] && mouseX < x + at[0] + 16
                    && mouseY >= y + at[1] && mouseY < y + at[1] + 16) {
                context.drawItemTooltip(textRenderer, new ItemStack(gems.get(index)), mouseX, mouseY);
                return;
            }
        }
    }
}
