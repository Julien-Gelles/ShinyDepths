package com.nowa.headlamp;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Ce que la lampe frontale fait voir — le battement des pierres, les lanternes de cobalt — du serveur
 * vers le seul joueur qui la porte. Rien ne remonte en
 * sens inverse : la lampe se met et se sertit par les cases du jeu.
 */
public final class HeadlampNetworking {
    private HeadlampNetworking() {
    }

    /**
     * Ce que la lampe a senti : la couleur de sa pierre, la durée de la lueur, et les blocs trouvés.
     *
     * @param color     couleur de la pierre, en 0xAARRGGBB
     * @param ticks     combien de temps la lueur doit tenir
     * @param positions les minerais à faire briller
     */
    public record GemSensePayload(int color, int ticks, List<BlockPos> positions) implements CustomPayload {
        public static final Id<GemSensePayload> ID = new Id<>(Identifier.of(MOD_ID, "gem_sense"));
        public static final PacketCodec<ByteBuf, GemSensePayload> CODEC = PacketCodec.tuple(
                PacketCodecs.INTEGER, GemSensePayload::color,
                PacketCodecs.VAR_INT, GemSensePayload::ticks,
                BlockPos.PACKET_CODEC.collect(PacketCodecs.toList()), GemSensePayload::positions,
                GemSensePayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /**
     * Les lanternes de cobalt que la lampe fait luire, le temps d'un battement.
     *
     * @param ticks     combien de temps la lueur doit tenir
     * @param positions les lanternes à portée
     */
    public record LanternSightPayload(int ticks, List<BlockPos> positions) implements CustomPayload {
        public static final Id<LanternSightPayload> ID = new Id<>(Identifier.of(MOD_ID, "lantern_sight"));
        public static final PacketCodec<ByteBuf, LanternSightPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, LanternSightPayload::ticks,
                BlockPos.PACKET_CODEC.collect(PacketCodecs.toList()), LanternSightPayload::positions,
                LanternSightPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    static void register() {
        PayloadTypeRegistry.playS2C().register(GemSensePayload.ID, GemSensePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LanternSightPayload.ID, LanternSightPayload.CODEC);
    }
}
