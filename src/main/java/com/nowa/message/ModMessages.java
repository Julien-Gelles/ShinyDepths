package com.nowa.message;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

import static com.nowa.NowasDepths.LOGGER;
import static com.nowa.NowasDepths.MOD_ID;

/**
 * Les messages du mod : de courtes explications affichées en haut à droite, comme un progrès obtenu.
 * C'est la forme que le jeu réserve à ce genre d'information, et la seule qui reste visible quand
 * une interface est ouverte — la barre d'action et le tchat sont cachés derrière.
 *
 * <p>Un message porte un identifiant : deux messages de même identifiant ne s'empilent pas, le
 * second relance simplement le premier. Sans cela, cliquer trois fois sur une pierre trop dure
 * remplirait l'écran.
 */
public final class ModMessages {
    /** Le cadre du message : information passagère, ou réussite à souligner. */
    public enum Style {
        INFO, ACHIEVEMENT;

        public static final PacketCodec<ByteBuf, Style> PACKET_CODEC =
                PacketCodecs.VAR_INT.xmap(index -> values()[index], Style::ordinal);
    }

    /**
     * @param id     sert au regroupement : un même id remplace le message affiché au lieu de s'ajouter
     * @param icon   l'objet montré à gauche ; vide pour n'afficher que le texte
     */
    public record Message(Identifier id, Text title, Text detail, ItemStack icon, Style style) {
        public Message(String name, Text title, Text detail, ItemStack icon) {
            this(Identifier.of(MOD_ID, name), title, detail, icon, Style.INFO);
        }

        public Message(String name, Text title, Text detail, ItemStack icon, Style style) {
            this(Identifier.of(MOD_ID, name), title, detail, icon, style);
        }
    }

    /** Message envoyé par le serveur à un joueur précis. */
    public record MessagePayload(Identifier id, Text title, Text detail, ItemStack icon, Style style) implements CustomPayload {
        public static final Id<MessagePayload> ID = new Id<>(Identifier.of(MOD_ID, "message"));
        public static final PacketCodec<RegistryByteBuf, MessagePayload> CODEC = PacketCodec.tuple(
                Identifier.PACKET_CODEC, MessagePayload::id,
                TextCodecs.REGISTRY_PACKET_CODEC, MessagePayload::title,
                TextCodecs.REGISTRY_PACKET_CODEC, MessagePayload::detail,
                ItemStack.OPTIONAL_PACKET_CODEC, MessagePayload::icon,
                Style.PACKET_CODEC, MessagePayload::style,
                MessagePayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    private ModMessages() {
    }

    /** Affiche un message chez ce joueur. Sans effet s'il s'agit d'un faux joueur. */
    public static void send(ServerPlayerEntity player, Message message) {
        ServerPlayNetworking.send(player, new MessagePayload(
                message.id(), message.title(), message.detail(), message.icon(), message.style()));
    }

    public static void register() {
        LOGGER.info("├─ Registering Mod Messages for " + MOD_ID);
        PayloadTypeRegistry.playS2C().register(MessagePayload.ID, MessagePayload.CODEC);
    }
}
