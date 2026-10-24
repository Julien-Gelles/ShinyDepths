package com.nowa.powers;

import com.nowa.item.GemColors;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Les pouvoirs, montrés comme le jeu montre ses effets : une icône en haut à droite de l'écran, une
 * ligne à côté de l'inventaire, avec le niveau en chiffres romains.
 *
 * <p>Chaque pouvoir a son effet à nous. Il ne <em>fait</em> rien : la mécanique reste dans la classe
 * du pouvoir, qui lit la bague ; l'effet n'est que son témoin, posé et retiré par {@link #sync}. Son
 * icône est celle d'un effet du jeu, reteinte à la couleur de la pierre ({@code textures/mob_effect}).
 */
public final class PowerEffects {
    private static final Map<GemPower, RegistryEntry<StatusEffect>> EFFECTS = new EnumMap<>(GemPower.class);
    private static final Map<RegistryEntry<StatusEffect>, GemPower> OWNERS = new HashMap<>();

    static {
        for (GemPower power : GemPower.values()) {
            RegistryEntry<StatusEffect> effect = Registry.registerReference(Registries.STATUS_EFFECT,
                    Identifier.of(MOD_ID, power.id()),
                    new Witness(StatusEffectCategory.BENEFICIAL, GemColors.of(power.gem()) & 0xFFFFFF));
            EFFECTS.put(power, effect);
            OWNERS.put(effect, power);
        }
    }

    private PowerEffects() {
    }

    /** Un effet sans mécanique propre : le jeu ne laisse pas construire le sien hors d'une sous-classe. */
    private static final class Witness extends StatusEffect {
        Witness(StatusEffectCategory category, int color) {
            super(category, color);
        }
    }

    /**
     * Un pouvoir vient de servir et recharge pendant {@code ticks} tics ; 0 s'il est de nouveau prêt.
     * Le client grise son icône en attendant.
     */
    public record CooldownPayload(Identifier effect, int ticks) implements CustomPayload {
        public static final Id<CooldownPayload> ID = new Id<>(Identifier.of(MOD_ID, "power_cooldown"));
        public static final PacketCodec<ByteBuf, CooldownPayload> CODEC = PacketCodec.tuple(
                Identifier.PACKET_CODEC, CooldownPayload::effect, PacketCodecs.VAR_INT, CooldownPayload::ticks,
                CooldownPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    static void register() {
        PayloadTypeRegistry.playS2C().register(CooldownPayload.ID, CooldownPayload.CODEC);
    }

    /** L'effet témoin de ce pouvoir. */
    public static RegistryEntry<StatusEffect> of(GemPower power) {
        return EFFECTS.get(power);
    }

    /** Le pouvoir dont cet effet témoigne, pour lui donner son contour — ou {@code null} si c'est un effet ordinaire. */
    @Nullable
    public static GemPower owner(RegistryEntry<StatusEffect> effect) {
        return OWNERS.get(effect);
    }

    /**
     * Dit au joueur que son pouvoir vient de servir et recharge pendant {@code ticks} tics : son icône
     * se grise, et le gris se retire à mesure. 0 : il est de nouveau prêt.
     */
    public static void cooldown(LivingEntity player, GemPower power, int ticks) {
        if (player instanceof ServerPlayerEntity server) {
            ServerPlayNetworking.send(server, new CooldownPayload(of(power).getKey().orElseThrow().getValue(), ticks));
        }
    }

    /**
     * Ce qu'un pouvoir montre : son niveau (1 = I) et le temps qu'il lui reste, en tics —
     * {@link StatusEffectInstance#INFINITE} pour ce qui tient tant que la bague est portée.
     */
    public record Shown(int level, int ticks) {
        public static Shown permanent(int level) {
            return new Shown(level, StatusEffectInstance.INFINITE);
        }
    }

    /** L'écart de durée, en tics, au-delà duquel on corrige l'effet plutôt que de le laisser courir. */
    private static final int DRIFT = 20;

    /**
     * Remet les effets d'une entité d'accord avec ce que son pouvoir veut montrer ({@code null} : rien).
     * Nos effets sont sans particules ; tout ce qui n'est pas voulu est retiré — le lait n'y change
     * rien longtemps. Un effet à durée s'écoule de lui-même : on ne le repose que s'il s'est écarté de
     * ce qu'on attend.
     */
    static void sync(LivingEntity entity, @Nullable GemPower power, @Nullable Shown shown) {
        for (Map.Entry<GemPower, RegistryEntry<StatusEffect>> entry : EFFECTS.entrySet()) {
            RegistryEntry<StatusEffect> effect = entry.getValue();
            StatusEffectInstance current = entity.getStatusEffect(effect);
            if (entry.getKey() != power || shown == null) {
                if (current != null) {
                    entity.removeStatusEffect(effect);
                }
            } else if (current == null || current.getAmplifier() != shown.level() - 1 || !sameTime(current, shown.ticks())) {
                entity.removeStatusEffect(effect);
                entity.addStatusEffect(new StatusEffectInstance(effect, shown.ticks(), shown.level() - 1, false, false, true));
            }
        }
    }

    private static boolean sameTime(StatusEffectInstance current, int ticks) {
        if (ticks == StatusEffectInstance.INFINITE || current.isInfinite()) {
            return ticks == StatusEffectInstance.INFINITE && current.isInfinite();
        }
        return Math.abs(current.getDuration() - ticks) <= DRIFT;
    }
}
