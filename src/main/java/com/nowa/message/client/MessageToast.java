package com.nowa.message.client;

import com.nowa.message.ModMessages;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.util.Identifier;

/**
 * Un message du mod, dessiné comme les notifications du jeu. L'identifiant du message sert de type :
 * c'est ainsi que le gestionnaire sait qu'un message répété doit remplacer le précédent.
 */
public class MessageToast implements Toast {
    private static final Identifier INFO_TEXTURE = Identifier.ofVanilla("toast/system");
    private static final Identifier ACHIEVEMENT_TEXTURE = Identifier.ofVanilla("toast/advancement");
    private static final long DURATION_MS = 4000L;

    private ModMessages.Message message;
    private long shownAt;
    private boolean restarted = true;

    public MessageToast(ModMessages.Message message) {
        this.message = message;
    }

    /** Le même message revient : on ne l'empile pas, on relance son décompte. */
    public void refresh(ModMessages.Message newMessage) {
        this.message = newMessage;
        this.restarted = true;
    }

    @Override
    public Object getType() {
        return message.id();
    }

    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long time) {
        if (restarted) {
            shownAt = time;
            restarted = false;
        }
        Identifier texture = message.style() == ModMessages.Style.ACHIEVEMENT ? ACHIEVEMENT_TEXTURE : INFO_TEXTURE;
        context.drawGuiTexture(texture, 0, 0, getWidth(), getHeight());
        TextRenderer text = manager.getClient().textRenderer;
        int left = 8;
        if (!message.icon().isEmpty()) {
            context.drawItem(message.icon(), 8, 8);
            left = 30;
        }
        context.drawText(text, message.title(), left, 7, 0xFFFFFF00, false);
        context.drawText(text, message.detail(), left, 18, 0xFFFFFFFF, false);
        return time - shownAt >= DURATION_MS * manager.getNotificationDisplayTimeMultiplier()
                ? Visibility.HIDE : Visibility.SHOW;
    }
}
