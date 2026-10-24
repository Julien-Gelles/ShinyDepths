package com.nowa.powers;

import com.nowa.powers.power.DoubleJump;
import com.nowa.powers.power.EnderPocket;
import com.nowa.powers.power.Recall;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Ce que le client dit au serveur : les deux gestes que seul le client voit — la touche de saut
 * pressée en l'air, et un clic droit qui n'a rien fait d'autre.
 */
public final class PowerNetworking {
    private PowerNetworking() {
    }

    /** Le joueur vient de sauter une seconde fois en l'air (péridot). */
    public record DoubleJumpPayload() implements CustomPayload {
        public static final Id<DoubleJumpPayload> ID = new Id<>(Identifier.of(MOD_ID, "power_double_jump"));
        public static final PacketCodec<ByteBuf, DoubleJumpPayload> CODEC = PacketCodec.unit(new DoubleJumpPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /**
     * Le joueur a fait un clic droit qui n'a rien fait d'autre — ni ouvrir, ni poser, ni se servir de
     * ce qu'il tient : c'est le geste du rappel (pierre de lune) et de la poche de l'End (onyx).
     */
    public record UsePayload() implements CustomPayload {
        public static final Id<UsePayload> ID = new Id<>(Identifier.of(MOD_ID, "power_use"));
        public static final PacketCodec<ByteBuf, UsePayload> CODEC = PacketCodec.unit(new UsePayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    static void register() {
        PayloadTypeRegistry.playC2S().register(DoubleJumpPayload.ID, DoubleJumpPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UsePayload.ID, UsePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DoubleJumpPayload.ID,
                (payload, context) -> context.server().execute(() -> DoubleJump.jumped(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(UsePayload.ID, (payload, context) -> context.server().execute(() -> {
            Recall.use(context.player());
            EnderPocket.use(context.player());
        }));
    }
}
