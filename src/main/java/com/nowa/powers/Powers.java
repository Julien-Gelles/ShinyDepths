package com.nowa.powers;

import com.nowa.item.ModDataComponents;
import com.nowa.item.Purity;
import com.nowa.jewelry.Jewelry;
import com.nowa.jewelry.Worn;
import com.nowa.socket.Socketing;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Le moteur des pouvoirs : de la bague qu'un joueur porte, il tire son pouvoir et son niveau. Ce que
 * fait chaque pouvoir n'est pas ici ; les pouvoirs viennent le consulter par {@link #of}.
 *
 * <h2>Les règles, depuis la refonte du 2026-10-16</h2>
 * <ul>
 *   <li><b>Le pouvoir</b> est celui de la pierre sertie dans la bague — la seule qui compte.</li>
 *   <li><b>Le niveau</b> est sa pureté : ébréchée 1, terne 2, limpide 3, pure 4, philosophale 5.</li>
 * </ul>
 */
public final class Powers {
    /** Tous les combien de tics on relit la bague d'un joueur. */
    private static final int INTERVAL = 5;

    /** Ce que chaque joueur connecté porte, tel qu'on l'a calculé la dernière fois. */
    private static final Map<UUID, Active> CURRENT = new HashMap<>();

    /** D'où le client tient son propre pouvoir ; branché par {@code PowersClient}. */
    public static Supplier<Active> clientActive = () -> Active.NONE;

    private Powers() {
    }

    /**
     * Le pouvoir qu'une entité porte, et son niveau.
     *
     * @param power le pouvoir, ou {@code null} s'il n'y en a pas
     * @param level de 1 à 5, 0 sans pouvoir
     */
    public record Active(@Nullable GemPower power, int level) {
        public static final Active NONE = new Active(null, 0);

        public static final PacketCodec<ByteBuf, Active> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, active -> active.power == null ? 0 : active.power.ordinal() + 1,
                PacketCodecs.VAR_INT, Active::level,
                (power, level) -> power == 0 ? NONE : new Active(GemPower.values()[power - 1], level));

        /** Le niveau de ce pouvoir, 0 si ce n'est pas celui qu'on porte : ce que consultent les pouvoirs. */
        public int level(GemPower wanted) {
            return power == wanted ? level : 0;
        }
    }

    /** Le pouvoir du joueur, envoyé à son propre client pour l'affichage. */
    public record ActivePayload(Active active) implements CustomPayload {
        public static final Id<ActivePayload> ID = new Id<>(Identifier.of(MOD_ID, "power"));
        public static final PacketCodec<ByteBuf, ActivePayload> CODEC = Active.CODEC.xmap(ActivePayload::new, ActivePayload::active);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void register() {
        LOGGER.info("├─ Registering gem powers for " + MOD_ID);
        PayloadTypeRegistry.playS2C().register(ActivePayload.ID, ActivePayload.CODEC);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if ((server.getTicks() + player.getId()) % INTERVAL != 0) {
                    continue;
                }
                Active now = compute(player);
                if (!now.equals(CURRENT.put(player.getUuid(), now))) {
                    ServerPlayNetworking.send(player, new ActivePayload(now));
                }
            }
        });
        // à la reconnexion le client ne sait plus rien : on oublie pour tout lui redire
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> CURRENT.remove(handler.player.getUuid()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> CURRENT.remove(handler.player.getUuid()));
    }

    /**
     * Le pouvoir de ce joueur en ce moment. Jamais {@code null}. Côté client on ne connaît que le sien,
     * tel que le serveur l'a dit : les autres joueurs y paraissent sans pouvoir.
     */
    public static Active of(PlayerEntity player) {
        if (player.getWorld().isClient) {
            return player.isMainPlayer() ? clientActive.get() : Active.NONE;
        }
        return CURRENT.getOrDefault(player.getUuid(), Active.NONE);
    }

    /** Le même, pour n'importe quelle entité : seul un joueur porte une bague. */
    public static Active of(LivingEntity entity) {
        return entity instanceof PlayerEntity player ? of(player) : Active.NONE;
    }

    /** Le pouvoir et le niveau que donne cette bague, telle qu'elle est sertie ; aucun si elle n'a pas de pierre à pouvoir. */
    public static Active ofRing(ItemStack ring) {
        ItemStack gem = Socketing.main(ring);
        GemPower power = gem.isEmpty() ? null : GemPower.of(gem.getItem());
        Purity purity = gem.get(ModDataComponents.PURITY);
        return power == null || purity == null ? Active.NONE : new Active(power, level(purity));
    }

    /** Le niveau que donne une pierre de cette pureté : 1 à 5. */
    public static int level(Purity purity) {
        return purity.ordinal() + 1;
    }

    /** Lit la bague du joueur. */
    public static Active compute(PlayerEntity player) {
        return ofRing(Jewelry.of(player).get(Worn.RING));
    }
}
