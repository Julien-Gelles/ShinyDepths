package com.nowa.message.client;

import com.nowa.message.ModMessages;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/** Côté client : affiche les messages, qu'ils viennent du serveur ou d'une interface du mod. */
public final class ModMessagesClient {
    private ModMessagesClient() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ModMessages.MessagePayload.ID, (payload, context) ->
                show(new ModMessages.Message(payload.id(), payload.title(), payload.detail(), payload.icon(), payload.style())));
    }

    /** Raccourci pour les messages purement locaux : une interface qui refuse un geste, par exemple. */
    public static void show(String name, Text title, Text detail, ItemStack icon) {
        show(new ModMessages.Message(name, title, detail, icon));
    }

    public static void show(ModMessages.Message message) {
        ToastManager manager = MinecraftClient.getInstance().getToastManager();
        MessageToast shown = manager.getToast(MessageToast.class, message.id());
        if (shown == null) {
            manager.add(new MessageToast(message));
        } else {
            shown.refresh(message);
        }
    }
}
