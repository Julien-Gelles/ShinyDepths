package com.nowa.lapidary;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Paquets du métier. Les données du joueur sont synchronisées par l'attachment et les annonces
 * passent par com.nowa.message : il ne reste que la demande de déblocage, du client vers le serveur.
 */
public final class LapidaryNetworking {
    private LapidaryNetworking() {
    }

    /** Le joueur clique sur un nœud de l'arbre. */
    public record UnlockSkillPayload(String nodeId) implements CustomPayload {
        public static final Id<UnlockSkillPayload> ID = new Id<>(Identifier.of(MOD_ID, "unlock_skill"));
        public static final PacketCodec<ByteBuf, UnlockSkillPayload> CODEC =
                PacketCodecs.STRING.xmap(UnlockSkillPayload::new, UnlockSkillPayload::nodeId);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    static void register() {
        PayloadTypeRegistry.playC2S().register(UnlockSkillPayload.ID, UnlockSkillPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UnlockSkillPayload.ID,
                (payload, context) -> Lapidary.tryUnlock(context.player(), payload.nodeId()));
    }
}
