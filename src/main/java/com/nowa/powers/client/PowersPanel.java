package com.nowa.powers.client;

import com.google.common.collect.Ordering;
import com.nowa.item.GemColors;
import com.nowa.powers.GemPower;
import com.nowa.powers.PowerEffects;
import com.nowa.powers.Powers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;

import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Le pouvoir du joueur, dans la fenêtre d'inventaire : la pierre de sa bague, dans le coin, et à son
 * survol le nom du pouvoir et son niveau.
 *
 * <p>Ce que le pouvoir <em>fait</em> paraît ailleurs, parmi les effets du joueur (voir
 * {@link PowerEffects}) : cette classe ne fait qu'y repeindre le contour de son effet témoin, à la
 * couleur de la pierre, et le griser quand il recharge (voir {@link Cooldowns}). L'anneau à teinter
 * ({@code power_frames.png}) est tiré des fonds d'effets du jeu.
 */
public final class PowersPanel {
    private static final Identifier FRAMES = Identifier.of(MOD_ID, "textures/gui/power_frames.png");
    private static final int FRAMES_W = 128, FRAMES_H = 64;
    /** La profondeur des icônes d'effet à l'écran de jeu (la cinquième couche, à 800), plus de quoi passer devant. */
    private static final float HUD_DEPTH = 900.0f;
    /** Le côté de la pierre de l'inventaire. */
    private static final int CORNER = 16;

    private PowersPanel() {
    }

    /**
     * Dans l'inventaire : la pierre de la bague, seule, dans le coin de la fenêtre — ni cadre ni fond.
     * Rien n'est dessiné sans pouvoir.
     *
     * @param x le coin haut gauche de la pierre
     */
    public static void drawCorner(DrawContext context, int x, int y) {
        GemPower power = PowersClient.active().power();
        if (power != null) {
            context.drawItem(new ItemStack(power.gem()), x, y);
        }
    }

    /** Au survol de la pierre : le nom du pouvoir, à la couleur de la pierre, et son niveau. */
    public static void drawCornerTooltip(DrawContext context, TextRenderer text, int x, int y, int mouseX, int mouseY) {
        Powers.Active active = PowersClient.active();
        if (active.power() == null || mouseX < x || mouseX >= x + CORNER || mouseY < y || mouseY >= y + CORNER) {
            return;
        }
        int rgb = GemColors.of(active.power().gem()) & 0xFFFFFF;
        context.drawTooltip(text, List.of(
                active.power().displayName().copy().styled(style -> style.withColor(rgb)),
                Text.translatable("tooltip.shiny-depths.power.level", Text.translatable("enchantment.level." + active.level()))
                        .formatted(Formatting.GRAY)), mouseX, mouseY);
    }

    // ---------- le contour de l'effet témoin ----------

    /** Le cadre, teinté : {@code u}, {@code v} désignent l'anneau voulu dans {@code power_frames.png}. */
    private static void frame(DrawContext context, int x, int y, int u, int v, int width, int height, GemPower power) {
        int color = GemColors.of(power.gem());
        context.setShaderColor((color >> 16 & 0xFF) / 255.0f, (color >> 8 & 0xFF) / 255.0f, (color & 0xFF) / 255.0f, 1.0f);
        context.drawTexture(FRAMES, x, y, u, v, width, height, FRAMES_W, FRAMES_H);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * Dans l'inventaire : le jeu vient de dessiner les effets du joueur à droite de la fenêtre ; on
     * repasse sur celui de son pouvoir pour repeindre son contour à la couleur de la pierre. Le
     * placement est recalculé comme le fait le jeu ({@code AbstractInventoryScreen.drawStatusEffects}).
     *
     * @param left le bord gauche des effets : deux pixels après la fenêtre
     * @param top  le haut de la fenêtre
     */
    public static void drawInventoryFrames(DrawContext context, int left, int top, int screenWidth) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        int room = screenWidth - left;
        if (player == null || player.getStatusEffects().isEmpty() || room < 32) {
            return;
        }
        boolean wide = room >= 120;
        Collection<StatusEffectInstance> effects = player.getStatusEffects();
        Cooldowns.prune(effects);
        int step = effects.size() > 5 ? 132 / (effects.size() - 1) : 33;
        int y = top;
        for (StatusEffectInstance effect : Ordering.natural().sortedCopy(effects)) {
            GemPower power = PowerEffects.owner(effect.getEffectType());
            if (power != null) {
                frame(context, left, y, 0, wide ? 0 : 32, wide ? 120 : 32, 32, power);
            }
            // dans l'inventaire c'est tout l'intérieur de l'encadré qui se grise, nom et durée compris —
            // mais pas le cadre lui-même, qui garde la couleur de la pierre : trois pixels de bord, coins arrondis d'un
            Cooldowns.draw(context, left + 3, y + 3, (wide ? 120 : 32) - 6, 32 - 6, 1, effect, power != null);
            y += step;
        }
    }

    /**
     * En jeu : la même retouche sur les icônes du haut de l'écran, placées comme le jeu les place
     * ({@code InGameHud.renderStatusEffectOverlay}) — les bons effets sur la première rangée, les
     * mauvais sur la seconde, de droite à gauche.
     */
    public static void drawHudFrames(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.options.hudHidden || player.getStatusEffects().isEmpty()
                || client.currentScreen instanceof AbstractInventoryScreen<?> screen && screen.hideStatusEffectHud()) {
            return;
        }
        // le jeu empile son affichage par couches, chacune 200 plus près de l'œil que la précédente, et
        // les effets sont sur la cinquième : sans cette avance, tout ce qu'on dessine ici passerait dessous
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, HUD_DEPTH);
        Cooldowns.prune(player.getStatusEffects());
        int good = 0, bad = 0;
        for (StatusEffectInstance effect : Ordering.natural().reverse().sortedCopy(player.getStatusEffects())) {
            if (!effect.shouldShowIcon()) {
                continue;
            }
            int x = context.getScaledWindowWidth();
            int y = client.isDemo() ? 16 : 1;
            if (effect.getEffectType().value().isBeneficial()) {
                x -= 25 * ++good;
            } else {
                x -= 25 * ++bad;
                y += 26;
            }
            GemPower power = PowerEffects.owner(effect.getEffectType());
            if (power != null) {
                frame(context, x, y, 32, 32, 24, 24, power);
            }
            Cooldowns.draw(context, x + 3, y + 3, effect, power != null);
        }
        context.getMatrices().pop();
    }
}
