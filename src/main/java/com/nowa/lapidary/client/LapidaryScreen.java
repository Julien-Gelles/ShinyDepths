package com.nowa.lapidary.client;

import com.nowa.NowasDepths;
import com.nowa.item.Purity;
import com.nowa.lapidary.Lapidary;
import com.nowa.lapidary.LapidaryData;
import com.nowa.lapidary.LapidaryLevels;
import com.nowa.lapidary.LapidaryNetworking;
import com.nowa.lapidary.LapidarySkillTree;
import com.nowa.lapidary.LapidarySkillTree.Node;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Arbre du lapidaire, dans le style de l'écran des progrès vanilla, mais redimensionnable :
 * la fenêtre s'adapte à l'écran, l'en-tête (une ligne) contient le niveau et la barre d'XP,
 * et le contenu se fait glisser à la souris quand il dépasse.
 * Clic (sans glisser) sur un nœud disponible : dépense un point.
 *
 * <p>Sur la gauche de la fenêtre, des onglets verticaux — ceux de l'écran des progrès — un par
 * métier, chacun montrant l'arbre de son métier. Il n'y en a qu'un pour l'instant, le lapidaire :
 * le jour où un autre métier aura son arbre, il prendra le sien sous celui-ci.
 */
public class LapidaryScreen extends Screen {
    private static final Identifier BACKGROUND = Identifier.ofVanilla("textures/block/deepslate_tiles.png");
    private static final Identifier FRAME_ROOT = Identifier.ofVanilla("advancements/challenge_frame_obtained");
    private static final Identifier XP_BAR_BACKGROUND = Identifier.ofVanilla("hud/experience_bar_background");
    private static final Identifier XP_BAR_PROGRESS = Identifier.ofVanilla("hud/experience_bar_progress");
    /** Les onglets verticaux de l'écran des progrès : le premier, ceux du milieu, le dernier ; et leur version ouverte. */
    private static final Identifier TAB_TOP = Identifier.ofVanilla("advancements/tab_left_top");
    private static final Identifier TAB_MIDDLE = Identifier.ofVanilla("advancements/tab_left_middle");
    private static final Identifier TAB_BOTTOM = Identifier.ofVanilla("advancements/tab_left_bottom");
    private static final Identifier TAB_TOP_SELECTED = Identifier.ofVanilla("advancements/tab_left_top_selected");
    private static final Identifier TAB_MIDDLE_SELECTED = Identifier.ofVanilla("advancements/tab_left_middle_selected");
    private static final Identifier TAB_BOTTOM_SELECTED = Identifier.ofVanilla("advancements/tab_left_bottom_selected");
    /** Un onglet : sa taille, et ce qui en dépasse à gauche de la fenêtre (le reste passe sous le cadre). */
    private static final int TAB_WIDTH = 32, TAB_HEIGHT = 28, TAB_OUT = 28;

    /** Un onglet de métier : son icône et son nom. */
    private record Tab(ItemStack icon, Text title) {
    }

    /** Les métiers qui ont un arbre, dans l'ordre de leurs onglets. */
    private static final List<Tab> TABS = List.of(
            new Tab(new ItemStack(com.nowa.item.ModItems.POLISHER), Text.translatable("lapidary.nowas-depths.screen.title")));

    // couleurs du cadre de la fenêtre des progrès (textures/gui/advancements/window.png)
    private static final int BLACK = 0xFF000000;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int GRAY = 0xFFC6C6C6;
    private static final int SHADOW = 0xFF555555;
    private static final int PAGE_BORDER_DARK = 0xFF202020;
    private static final int PAGE_BORDER_LIGHT = 0xFF939393;
    private static final int TEXT_COLOR = 0x404040;
    private static final float LOCKED_ICON_BRIGHTNESS = 0.4f;    // luminosité de leur icône (1 = normale)

    private static final int MIN_WIDTH = 300;
    private static final int MAX_WIDTH = 440;
    private static final int MIN_HEIGHT = 170;
    private static final int MAX_HEIGHT = 300;
    private static final int MARGIN = 20;           // espace autour de la fenêtre
    private static final int HEADER_HEIGHT = 18;    // une ligne : titre, niveau + barre d'XP, points
    private static final int BORDER = 9;
    private static final int CONTENT_PADDING = 12;  // marge autour de l'arbre dans la page
    private static final int NODE_SIZE = 26;
    private static final int XP_BAR_WIDTH = 182;
    private static final int DRAG_THRESHOLD = 3;
    /** Ce que fait défiler un cran de molette, en pixels : une case et un peu plus. */
    private static final int SCROLL_STEP = 20;
    /** La largeur, en pixels, au-delà de laquelle une ligne d'infobulle revient à la ligne. */
    private static final int TOOLTIP_WIDTH = 170;

    private int left;
    private int top;
    private int windowWidth;
    private int windowHeight;
    private int pageLeft;
    private int pageTop;
    private int pageWidth;
    private int pageHeight;

    // limites de l'arbre (coordonnées des nœuds) et décalage actuel du contenu dans la page
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;
    private double offsetX;
    private double offsetY;

    private boolean pressedInPage;
    private double dragDistance;
    /** L'onglet ouvert. */
    private int selectedTab;

    public LapidaryScreen() {
        super(Text.translatable("lapidary.nowas-depths.screen.title"));
    }

    @Override
    protected void init() {
        // les onglets dépassent à gauche : la fenêtre et eux sont centrés ensemble
        windowWidth = MathHelper.clamp(width - 2 * MARGIN - TAB_OUT, MIN_WIDTH, MAX_WIDTH);
        windowHeight = MathHelper.clamp(height - 2 * MARGIN, MIN_HEIGHT, MAX_HEIGHT);
        left = (width - windowWidth + TAB_OUT) / 2;
        top = (height - windowHeight) / 2;
        pageLeft = left + BORDER;
        pageTop = top + HEADER_HEIGHT;
        pageWidth = windowWidth - 2 * BORDER;
        pageHeight = windowHeight - HEADER_HEIGHT - BORDER;

        minX = Integer.MAX_VALUE;
        minY = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        maxY = Integer.MIN_VALUE;
        for (Node node : LapidarySkillTree.nodes()) {
            minX = Math.min(minX, node.x() - CONTENT_PADDING);
            minY = Math.min(minY, node.y() - CONTENT_PADDING);
            maxX = Math.max(maxX, node.x() + NODE_SIZE + CONTENT_PADDING);
            maxY = Math.max(maxY, node.y() + NODE_SIZE + CONTENT_PADDING);
        }
        // au départ : arbre centré verticalement, racine à gauche
        offsetX = -minX;
        offsetY = (pageHeight - minY - maxY) / 2.0;
        clampOffset();
    }

    /** Empêche de faire sortir l'arbre de la page ; s'il tient entièrement, il reste centré. */
    private void clampOffset() {
        offsetX = clampAxis(offsetX, minX, maxX, pageWidth);
        offsetY = clampAxis(offsetY, minY, maxY, pageHeight);
    }

    private static double clampAxis(double offset, int min, int max, int pageSize) {
        if (max - min <= pageSize) {
            return (pageSize - min - max) / 2.0;
        }
        return MathHelper.clamp(offset, pageSize - max, -min);
    }

    private LapidaryData data() {
        return client != null && client.player != null ? Lapidary.get(client.player) : LapidaryData.EMPTY;
    }

    private int nodeX(Node node) {
        return pageLeft + (int) Math.round(offsetX) + node.x();
    }

    private int nodeY(Node node) {
        return pageTop + (int) Math.round(offsetY) + node.y();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        LapidaryData data = data();

        drawTabs(context, false);                 // les onglets fermés passent sous le cadre…
        drawWindow(context);
        drawTabs(context, true);                  // …l'onglet ouvert s'y fond
        context.enableScissor(pageLeft, pageTop, pageLeft + pageWidth, pageTop + pageHeight);
        drawPageBackground(context);
        drawLinks(context, data, true);
        drawLinks(context, data, false);
        for (Node node : LapidarySkillTree.nodes()) {
            drawNode(context, data, node, nodeX(node), nodeY(node));
        }
        context.disableScissor();
        drawHeader(context, data);

        Node hovered = pressedInPage && dragDistance >= DRAG_THRESHOLD ? null : nodeAt(mouseX, mouseY);
        if (hovered != null) {
            // les descriptions sont longues : chaque ligne revient à la ligne passé une largeur fixe
            List<OrderedText> lines = new ArrayList<>();
            for (Text line : tooltip(data, hovered)) {
                lines.addAll(textRenderer.wrapLines(line, TOOLTIP_WIDTH));
            }
            context.drawOrderedTooltip(textRenderer, lines, mouseX, mouseY);
        }
        int tab = tabAt(mouseX, mouseY);
        if (tab >= 0) {
            context.drawTooltip(textRenderer, TABS.get(tab).title(), mouseX, mouseY);
        }
    }

    // ---------- les onglets des métiers ----------

    private int tabX() {
        return left - TAB_OUT;
    }

    private int tabY(int index) {
        return top + index * TAB_HEIGHT;
    }

    /** Dessine les onglets fermés, ou seulement celui qui est ouvert, avec leur icône. */
    private void drawTabs(DrawContext context, boolean selected) {
        for (int index = 0; index < TABS.size(); index++) {
            if ((index == selectedTab) != selected) {
                continue;
            }
            boolean first = index == 0, last = index == TABS.size() - 1 && !first;
            Identifier sprite = selected
                    ? (first ? TAB_TOP_SELECTED : last ? TAB_BOTTOM_SELECTED : TAB_MIDDLE_SELECTED)
                    : (first ? TAB_TOP : last ? TAB_BOTTOM : TAB_MIDDLE);
            context.drawGuiTexture(sprite, tabX(), tabY(index), TAB_WIDTH, TAB_HEIGHT);
            context.drawItem(TABS.get(index).icon(), tabX() + 10, tabY(index) + 6);
        }
    }

    /** L'onglet sous la souris, ou -1. */
    private int tabAt(double mouseX, double mouseY) {
        for (int index = 0; index < TABS.size(); index++) {
            if (mouseX >= tabX() && mouseX < left && mouseY >= tabY(index) && mouseY < tabY(index) + TAB_HEIGHT) {
                return index;
            }
        }
        return -1;
    }

    /** Cadre gris biseauté de la fenêtre des progrès, à n'importe quelle taille. */
    private void drawWindow(DrawContext context) {
        int x = left;
        int y = top;
        int r = left + windowWidth;
        int b = top + windowHeight;
        context.fill(x + 3, y + 3, r - 3, b - 3, GRAY);
        // haut et coin supérieur gauche (lumière)
        context.fill(x + 2, y, r - 3, y + 1, BLACK);
        context.fill(x + 1, y + 1, x + 2, y + 2, BLACK);
        context.fill(x + 2, y + 1, r - 3, y + 2, LIGHT);
        context.fill(r - 3, y + 1, r - 2, y + 2, BLACK);
        context.fill(x, y + 2, x + 1, y + 3, BLACK);
        context.fill(x + 1, y + 2, r - 3, y + 3, LIGHT);
        context.fill(r - 3, y + 2, r - 2, y + 3, GRAY);
        context.fill(r - 2, y + 2, r - 1, y + 3, BLACK);
        context.fill(x + 3, y + 3, x + 4, y + 4, LIGHT);
        // côtés : lumière à gauche, ombre à droite
        context.fill(x, y + 3, x + 1, b - 3, BLACK);
        context.fill(x + 1, y + 3, x + 3, b - 3, LIGHT);
        context.fill(r - 3, y + 3, r - 1, b - 3, SHADOW);
        context.fill(r - 1, y + 3, r, b - 3, BLACK);
        context.fill(r - 4, b - 4, r - 3, b - 3, SHADOW);
        // bas (ombre)
        context.fill(x + 1, b - 3, x + 2, b - 2, BLACK);
        context.fill(x + 2, b - 3, x + 3, b - 2, GRAY);
        context.fill(x + 3, b - 3, r - 1, b - 2, SHADOW);
        context.fill(r - 1, b - 3, r, b - 2, BLACK);
        context.fill(x + 2, b - 2, x + 3, b - 1, BLACK);
        context.fill(x + 3, b - 2, r - 2, b - 1, SHADOW);
        context.fill(r - 2, b - 2, r - 1, b - 1, BLACK);
        context.fill(x + 3, b - 1, r - 3, b, BLACK);
        // bordure creusée de la page
        int pageRight = pageLeft + pageWidth;
        int pageBottom = pageTop + pageHeight;
        context.fill(pageLeft - 1, pageTop - 1, pageRight, pageTop, PAGE_BORDER_DARK);
        context.fill(pageLeft - 1, pageTop, pageLeft, pageBottom, PAGE_BORDER_DARK);
        context.fill(pageLeft, pageBottom, pageRight + 1, pageBottom + 1, PAGE_BORDER_LIGHT);
        context.fill(pageRight, pageTop, pageRight + 1, pageBottom, PAGE_BORDER_LIGHT);
    }

    /** Fond en tuiles qui suit le déplacement du contenu. */
    private void drawPageBackground(DrawContext context) {
        int startX = pageLeft + Math.floorMod((int) Math.round(offsetX), 16) - 16;
        int startY = pageTop + Math.floorMod((int) Math.round(offsetY), 16) - 16;
        for (int x = startX; x < pageLeft + pageWidth; x += 16) {
            for (int y = startY; y < pageTop + pageHeight; y += 16) {
                context.drawTexture(BACKGROUND, x, y, 0f, 0f, 16, 16, 16, 16);
            }
        }
    }

    /**
     * Une seule ligne : titre à gauche, points à droite, et entre les deux le niveau, la barre d'XP
     * et la progression, centrés. La barre raccourcit si la fenêtre est étroite.
     */
    private void drawHeader(DrawContext context, LapidaryData data) {
        int textY = top + 6;
        context.drawText(textRenderer, title, left + 8, textY, TEXT_COLOR, false);
        Text points = Text.translatable("lapidary.nowas-depths.screen.points",
                Lapidary.count("lapidary.nowas-depths.points", data.availablePoints()));
        int pointsX = left + windowWidth - 8 - textRenderer.getWidth(points);
        context.drawText(textRenderer, points, pointsX, textY, TEXT_COLOR, false);

        int level = data.level();
        String levelText = String.valueOf(level);
        Text progress = Text.translatable("lapidary.nowas-depths.screen.xp",
                LapidaryLevels.progressInLevel(data.xp()), LapidaryLevels.xpToNext(level));
        int levelWidth = textRenderer.getWidth(levelText);
        int progressWidth = textRenderer.getWidth(progress);

        int regionStart = left + 8 + textRenderer.getWidth(title) + 10;
        int regionEnd = pointsX - 10;
        int barWidth = MathHelper.clamp(regionEnd - regionStart - levelWidth - progressWidth - 10, 20, XP_BAR_WIDTH);
        int groupWidth = levelWidth + 5 + barWidth + 5 + progressWidth;
        int x = regionStart + (regionEnd - regionStart - groupWidth) / 2;

        // niveau en vert avec contour noir, comme au-dessus de la barre d'XP vanilla
        context.drawText(textRenderer, levelText, x + 1, textY, 0x000000, false);
        context.drawText(textRenderer, levelText, x - 1, textY, 0x000000, false);
        context.drawText(textRenderer, levelText, x, textY + 1, 0x000000, false);
        context.drawText(textRenderer, levelText, x, textY - 1, 0x000000, false);
        context.drawText(textRenderer, levelText, x, textY, 0x80FF20, false);

        int barX = x + levelWidth + 5;
        int barY = textY + 2;
        int filled = LapidaryLevels.progressInLevel(data.xp()) * (barWidth + 1) / LapidaryLevels.xpToNext(level);
        drawBarPart(context, XP_BAR_BACKGROUND, barX, barY, barWidth, barWidth);
        drawBarPart(context, XP_BAR_PROGRESS, barX, barY, barWidth, filled);
        context.drawText(textRenderer, progress, barX + barWidth + 5, textY, TEXT_COLOR, false);
    }

    /**
     * Dessine les {@code visible} premiers pixels d'une barre raccourcie à {@code width} :
     * le début de la texture, puis son bout arrondi (2 derniers pixels) pour garder une fin propre.
     */
    private static void drawBarPart(DrawContext context, Identifier sprite, int x, int y, int width, int visible) {
        int body = Math.min(visible, width - 2);
        if (body > 0) {
            context.drawGuiTexture(sprite, XP_BAR_WIDTH, 5, 0, 0, x, y, body, 5);
        }
        int end = visible - (width - 2);
        if (end > 0) {
            context.drawGuiTexture(sprite, XP_BAR_WIDTH, 5, XP_BAR_WIDTH - 2, 0, x + width - 2, y, end, 5);
        }
    }

    /** Liens en coude parent → enfant, comme les progrès : un contour noir puis la ligne. */
    private void drawLinks(DrawContext context, LapidaryData data, boolean outline) {
        for (Node node : LapidarySkillTree.nodes()) {
            Node parent = node.parent() == null ? null : LapidarySkillTree.get(node.parent());
            if (parent == null) {
                continue;
            }
            int x1 = nodeX(parent) + NODE_SIZE / 2;
            int y1 = nodeY(parent) + NODE_SIZE / 2;
            int x2 = nodeX(node) + NODE_SIZE / 2;
            int y2 = nodeY(node) + NODE_SIZE / 2;
            // la racine passe par le tronc ; un livre pend droit de la barre qui y mène
            int midX = LapidarySkillTree.bookVolume(node) > 0 ? x2
                    : parent.isRoot() ? nodeX(parent) + LapidarySkillTree.TRUNK_X
                    : x2 - (node.x() - parent.x()) / 2;
            if (outline) {
                for (int offset = -1; offset <= 1; offset++) {
                    context.drawHorizontalLine(x1, midX, y1 + offset, BLACK);
                    context.drawVerticalLine(midX + offset, y1, y2, BLACK);
                    context.drawHorizontalLine(midX, x2, y2 + offset, BLACK);
                }
            } else {
                int color = LapidarySkillTree.isUnlocked(data, node) ? 0xFFFFFFFF : 0xFF7F7F7F;
                context.drawHorizontalLine(x1, midX, y1, color);
                context.drawVerticalLine(midX, y1, y2, color);
                context.drawHorizontalLine(midX, x2, y2, color);
            }
        }
    }

    private void drawNode(DrawContext context, LapidaryData data, Node node, int x, int y) {
        boolean unlocked = LapidarySkillTree.isUnlocked(data, node);
        if (!unlocked && !LapidarySkillTree.canUnlock(data, node)) {
            context.drawGuiTexture(frame(node, "locked"), x, y, NODE_SIZE, NODE_SIZE);
            // teinte appliquée uniquement aux pixels de l'objet : le fond de la case reste sombre
            context.setShaderColor(LOCKED_ICON_BRIGHTNESS, LOCKED_ICON_BRIGHTNESS, LOCKED_ICON_BRIGHTNESS, 1.0f);
            context.drawItem(new ItemStack(node.icon()), x + 5, y + 5);
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            return;
        }
        Identifier frame = node.isRoot() ? FRAME_ROOT : frame(node, unlocked ? "obtained" : "unobtained");
        context.drawGuiTexture(frame, x, y, NODE_SIZE, NODE_SIZE);
        context.drawItem(new ItemStack(node.icon()), x + 5, y + 5);
    }

    /**
     * Le cadre d'un nœud dit ce qu'il coûte, par les trois formes des progrès du jeu : carré pour
     * 1 point, arrondi pour 2, à pointes pour 3 et plus. Obtenu et disponible sont les cadres du
     * jeu ; indisponible, les mêmes formes repeintes en sombre ({@code textures/gui/sprites/lapidary}).
     */
    private static Identifier frame(Node node, String state) {
        String shape = node.cost() <= 1 ? "task" : node.cost() == 2 ? "goal" : "challenge";
        return state.equals("locked")
                ? Identifier.of(NowasDepths.MOD_ID, "lapidary/" + shape + "_frame_locked")
                : Identifier.ofVanilla("advancements/" + shape + "_frame_" + state);
    }

    private List<Text> tooltip(LapidaryData data, Node node) {
        List<Text> lines = new ArrayList<>();
        boolean unlocked = LapidarySkillTree.isUnlocked(data, node);
        lines.add(Text.translatable(node.translationKey()).formatted(unlocked ? Formatting.GREEN : Formatting.WHITE));
        lines.add(Text.translatable(node.translationKey() + ".desc").formatted(Formatting.GRAY));
        // la racine et Coup d'œil disent leurs chances de pureté : une ligne par classe, lues dans le code
        int purityRank = purityRankOf(node);
        if (purityRank >= 0) {
            for (Purity purity : Purity.values()) {
                if (purity.chance(purityRank) > 0) {
                    lines.add(Text.translatable("lapidary.nowas-depths.screen.purity_line", purity.displayName(),
                            purity.chance(purityRank)).formatted(Formatting.GRAY));
                }
            }
        }
        if (node.isRoot()) {
            return lines;
        }
        if (unlocked) {
            lines.add(Text.translatable("lapidary.nowas-depths.screen.unlocked").formatted(Formatting.GOLD));
        } else if (!LapidarySkillTree.parentUnlocked(data, node)) {
            Node parent = LapidarySkillTree.get(node.parent());
            lines.add(Text.translatable("lapidary.nowas-depths.screen.requires",
                    Text.translatable(parent.translationKey())).formatted(Formatting.RED));
        } else if (data.availablePoints() < node.cost()) {
            lines.add(Text.translatable("lapidary.nowas-depths.screen.not_enough_points",
                    Lapidary.count("lapidary.nowas-depths.points", node.cost())).formatted(Formatting.RED));
        } else {
            Text price = node.cost() == 0 ? Text.translatable("lapidary.nowas-depths.screen.free")
                    : Lapidary.count("lapidary.nowas-depths.points", node.cost());
            lines.add(Text.translatable("lapidary.nowas-depths.screen.click_to_unlock", price)
                    .formatted(Formatting.GREEN));
        }
        return lines;
    }

    /** Le rang de Coup d'œil dont ce nœud montre les chances de pureté : 0 pour la racine, -1 s'il n'en montre pas. */
    private static int purityRankOf(Node node) {
        if (node.isRoot()) {
            return 0;
        }
        for (int rank = 1; rank <= LapidarySkillTree.Skill.KEEN_EYE.ranks(); rank++) {
            if (node.id().equals(LapidarySkillTree.Skill.KEEN_EYE.nodeId(rank))) {
                return rank;
            }
        }
        return -1;
    }

    private boolean isInPage(double mouseX, double mouseY) {
        return mouseX >= pageLeft && mouseY >= pageTop && mouseX < pageLeft + pageWidth && mouseY < pageTop + pageHeight;
    }

    @Nullable
    private Node nodeAt(double mouseX, double mouseY) {
        if (!isInPage(mouseX, mouseY)) {
            return null;
        }
        for (Node node : LapidarySkillTree.nodes()) {
            int x = nodeX(node);
            int y = nodeY(node);
            if (mouseX >= x && mouseY >= y && mouseX < x + NODE_SIZE && mouseY < y + NODE_SIZE) {
                return node;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int tab = tabAt(mouseX, mouseY);
        if (button == 0 && tab >= 0) {
            if (tab != selectedTab) {
                selectedTab = tab;
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            }
            return true;
        }
        if (button == 0 && isInPage(mouseX, mouseY)) {
            pressedInPage = true;
            dragDistance = 0;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && pressedInPage) {
            dragDistance += Math.abs(deltaX) + Math.abs(deltaY);
            offsetX += deltaX;
            offsetY += deltaY;
            clampOffset();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    /**
     * La molette fait défiler l'arbre de haut en bas ; avec Maj, de gauche à droite. Le défilement
     * horizontal d'un pavé tactile passe aussi. Le contenu s'arrête à ses bords, comme au glisser :
     * s'il tient en entier dans la page sur un axe, il ne bouge pas sur cet axe.
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isInPage(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (hasShiftDown()) {
            offsetX += (verticalAmount + horizontalAmount) * SCROLL_STEP;
        } else {
            offsetY += verticalAmount * SCROLL_STEP;
            offsetX += horizontalAmount * SCROLL_STEP;
        }
        clampOffset();
        return true;
    }

    /** Le déblocage se fait au relâchement, et seulement si on n'a pas fait glisser le contenu. */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && pressedInPage) {
            pressedInPage = false;
            Node node = nodeAt(mouseX, mouseY);
            if (dragDistance < DRAG_THRESHOLD && node != null && LapidarySkillTree.canUnlock(data(), node)) {
                ClientPlayNetworking.send(new LapidaryNetworking.UnlockSkillPayload(node.id()));
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // la touche qui ouvre l'écran le referme aussi, comme L pour les progrès
        if (LapidaryClient.OPEN_SCREEN.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
