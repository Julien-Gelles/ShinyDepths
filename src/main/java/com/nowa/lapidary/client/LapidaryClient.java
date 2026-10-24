package com.nowa.lapidary.client;

import com.nowa.item.ModDataComponents;
import com.nowa.item.ModItems;
import com.nowa.item.Purity;
import com.nowa.item.StonePouchItem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

/** Côté client du métier : touche K, bouton dans le menu Échap, notification de niveau, infobulles de pureté. */
public final class LapidaryClient {
    public static final KeyBinding OPEN_SCREEN = new KeyBinding(
            "key.nowas-depths.lapidary", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.nowas-depths");

    private LapidaryClient() {
    }

    public static void register() {
        KeyBindingHelper.registerKeyBinding(OPEN_SCREEN);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_SCREEN.wasPressed()) {
                if (client.currentScreen == null && client.player != null) {
                    client.setScreen(new LapidaryScreen());
                }
            }
        });

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            Purity purity = stack.get(ModDataComponents.PURITY);
            if (purity != null) {
                lines.add(1, Text.translatable("purity.nowas-depths.tooltip", purity.displayName()).formatted(Formatting.GRAY));
            }
        });

        TooltipComponentCallback.EVENT.register(data -> data instanceof StonePouchItem.StonePouchTooltipData pouch
                ? new StonePouchTooltipComponent(pouch.contents()) : null);

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof GameMenuScreen) {
                addMenuButton(client, screen);
            }
        });
    }

    /** Petit bouton à icône à gauche du bouton « Progrès » du menu Échap. */
    private static void addMenuButton(MinecraftClient client, Screen screen) {
        for (ClickableWidget widget : Screens.getButtons(screen)) {
            if (widget.getMessage().getContent() instanceof TranslatableTextContent content
                    && content.getKey().equals("gui.advancements")) {
                ItemIconButton button = new ItemIconButton(widget.getX() - 24, widget.getY(),
                        Text.translatable("lapidary.nowas-depths.screen.title"), new ItemStack(ModItems.POLISHER),
                        b -> client.setScreen(new LapidaryScreen()));
                Screens.getButtons(screen).add(button);
                return;
            }
        }
    }

    /** Bouton carré 20 × 20 qui affiche un objet au lieu d'un texte. */
    private static class ItemIconButton extends ButtonWidget {
        private final ItemStack icon;

        ItemIconButton(int x, int y, Text message, ItemStack icon, PressAction onPress) {
            super(x, y, 20, 20, message, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.icon = icon;
            setTooltip(Tooltip.of(message));
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            context.drawItem(icon, getX() + 2, getY() + 2);
        }

        @Override
        public void drawMessage(DrawContext context, net.minecraft.client.font.TextRenderer textRenderer, int color) {
            // pas de texte : seulement l'icône
        }
    }
}
