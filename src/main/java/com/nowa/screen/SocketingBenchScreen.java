package com.nowa.screen;

import com.nowa.item.ModJewelry;
import com.nowa.jewelry.client.MovableSlot;
import com.nowa.socket.SocketFamily;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static com.nowa.NowasDepths.MOD_ID;

/**
 * L'écran de l'établi de sertisseur : la case de l'objet à gauche ; au milieu, l'objet posé dessiné
 * en grand, la case de la pierre posée sur sa pierre ; ce qui en sort à droite. Sans objet, le
 * milieu montre les bagues qui se sertissent, en petit, comme la table de lapidaire montre ses
 * pierres. Le fond reprend celui de l'établi du jeu, plus haut (voir {@link SocketingBenchScreenHandler}
 * pour ce que font les cases). Ce qu'une bague donnera — son set, son niveau — se lit dans
 * l'infobulle de la bague qui sort.
 */
public class SocketingBenchScreen extends HandledScreen<SocketingBenchScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/gui/socketing_bench.png");
    /** Une case entière, hors du panneau dans la texture : on en reprend le cadre. */
    private static final int FRAME_U = 176, FRAME_V = 0;
    /** L'objet en grand : le coin de son dessin, et son grossissement (16 pixels × 4). */
    private static final int ART_X = 50, ART_Y = 22, ART_SCALE = 4;
    /**
     * Où se tient la pierre sur le dessin de l'objet, en pixels de sa texture : le chaton en haut
     * de la bague, la lentille de la lampe frontale.
     */
    private static final float[] RING_STONE = {7.5f, 2.0f}, LAMP_STONE = {8.0f, 10.0f};
    /** Le fond de la case de la pierre posée sur l'objet : le gris des cases du jeu, à demi transparent. */
    private static final int VEIL = 0x808B8B8B;
    /** Sans objet posé : les bagues, en carré au milieu du panneau (coin haut-gauche de chacune). */
    private static final int[][] EXAMPLE_SPOTS = {{62, 40}, {82, 40}, {62, 60}, {82, 60}};

    private final List<ItemStack> examples = ModJewelry.allOf(ModJewelry.Kind.RING).stream()
            .limit(EXAMPLE_SPOTS.length).map(Item::getDefaultStack).toList();

    public SocketingBenchScreen(SocketingBenchScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundHeight += SocketingBenchScreenHandler.GROW;
        playerInventoryTitleY = backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        placeGemSlot();
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        placeGemSlot();
    }

    /** La case de la pierre suit l'objet posé : sur le chaton de la bague, sur la lentille de la lampe. */
    private void placeGemSlot() {
        float[] stone = SocketFamily.of(handler.item()) == SocketFamily.LAMP ? LAMP_STONE : RING_STONE;
        ((MovableSlot) handler.getSlot(SocketingBenchScreenHandler.GEM)).nowasDepths$moveTo(
                Math.round(ART_X + stone[0] * ART_SCALE) - 8, Math.round(ART_Y + stone[1] * ART_SCALE) - 8);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        placeGemSlot();
        ItemStack item = handler.item();
        int cellX = x + handler.getSlot(SocketingBenchScreenHandler.GEM).x - 1;
        int cellY = y + handler.getSlot(SocketingBenchScreenHandler.GEM).y - 1;
        if (item.isEmpty()) {
            for (int index = 0; index < examples.size(); index++) {
                context.drawItem(examples.get(index), x + EXAMPLE_SPOTS[index][0], y + EXAMPLE_SPOTS[index][1]);
            }
            if (handler.getSlot(SocketingBenchScreenHandler.GEM).hasStack()) {
                context.drawTexture(TEXTURE, cellX, cellY, FRAME_U, FRAME_V, 18, 18);  // une pierre restée là
            }
            return;
        }
        // l'objet en grand, en retrait : la case de la pierre et ce qu'elle tient passent devant
        context.getMatrices().push();
        context.getMatrices().translate(x + ART_X, y + ART_Y, -100.0f);
        context.getMatrices().scale(ART_SCALE, ART_SCALE, 1.0f);
        context.drawItem(item, 0, 0);
        context.getMatrices().pop();
        // la case de la pierre : le seul cadre, et un voile où l'objet se devine
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 90.0f);
        context.drawTexture(TEXTURE, cellX, cellY, FRAME_U, FRAME_V, 18, 1);
        context.drawTexture(TEXTURE, cellX, cellY + 17, FRAME_U, FRAME_V + 17, 18, 1);
        context.drawTexture(TEXTURE, cellX, cellY + 1, FRAME_U, FRAME_V + 1, 1, 16);
        context.drawTexture(TEXTURE, cellX + 17, cellY + 1, FRAME_U + 17, FRAME_V + 1, 1, 16);
        context.fill(cellX + 1, cellY + 1, cellX + 17, cellY + 17, VEIL);
        context.getMatrices().pop();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
        if (focusedSlot != null || !handler.item().isEmpty()) {
            return;                                     // une case survolée a déjà son infobulle
        }
        for (int index = 0; index < examples.size(); index++) {
            int ex = x + EXAMPLE_SPOTS[index][0], ey = y + EXAMPLE_SPOTS[index][1];
            if (mouseX >= ex && mouseX < ex + 16 && mouseY >= ey && mouseY < ey + 16) {
                context.drawItemTooltip(textRenderer, examples.get(index), mouseX, mouseY);
                return;
            }
        }
    }
}
