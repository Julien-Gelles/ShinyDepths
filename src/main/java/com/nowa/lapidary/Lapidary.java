package com.nowa.lapidary;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import com.nowa.item.ModItems;
import com.nowa.message.ModMessages;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import static com.nowa.NowasDepths.LOGGER;
import static com.nowa.NowasDepths.MOD_ID;

/**
 * Point d'entrée du métier de lapidaire : données par joueur (sauvegardées, gardées à la mort,
 * synchronisées avec son client), gain d'XP et déblocage des nœuds de l'arbre.
 */
public final class Lapidary {
    public static final AttachmentType<LapidaryData> DATA = AttachmentRegistry.create(
            Identifier.of(MOD_ID, "lapidary"),
            builder -> builder
                    .initializer(() -> LapidaryData.EMPTY)
                    .persistent(LapidaryData.CODEC)
                    .copyOnDeath()
                    .syncWith(LapidaryData.PACKET_CODEC, AttachmentSyncPredicate.targetOnly()));

    private Lapidary() {
    }

    public static void register() {
        LOGGER.info("├─ Registering Lapidary profession for " + MOD_ID);
        LapidaryNetworking.register();
        LapidaryMining.register();
        CommandRegistrationCallback.EVENT.register(LapidaryCommand::register);
        // nœuds retirés de l'arbre : on rend les points dépensés dessus
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                set(handler.player, get(handler.player).retainUnlocked(id -> LapidarySkillTree.get(id) != null)));
    }

    public static LapidaryData get(PlayerEntity player) {
        return player.getAttachedOrElse(DATA, LapidaryData.EMPTY);
    }

    /** Rang d'une compétence pour ce joueur. */
    public static int rank(PlayerEntity player, LapidarySkillTree.Skill skill) {
        return LapidarySkillTree.rank(get(player), skill);
    }

    /** Rang de Coup d'œil du joueur (0 à 5), qui fixe ses chances de pureté. */
    public static int purityRank(PlayerEntity player) {
        return LapidarySkillTree.purityRank(get(player));
    }

    public static void set(ServerPlayerEntity player, LapidaryData data) {
        player.setAttached(DATA, data);
    }

    /** Ajoute de l'XP de lapidaire ; annonce chaque niveau gagné. */
    public static void addXp(ServerPlayerEntity player, int amount) {
        amount = LapidaryBonuses.xpGain(player, amount);       // l'Étude majore tout ce qui rentre
        if (amount <= 0) {
            return;
        }
        LapidaryData before = get(player);
        LapidaryData after = before.withXp(before.xp() + amount);
        set(player, after);
        player.sendMessage(Text.translatable("lapidary.nowas-depths.xp_gained", amount).formatted(Formatting.AQUA), true);
        if (after.level() > before.level()) {
            onLevelUp(player, after.level());
        }
    }

    private static void onLevelUp(ServerPlayerEntity player, int level) {
        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1.4f);
        ModMessages.send(player, new ModMessages.Message("lapidary_level",
                Text.translatable("lapidary.nowas-depths.toast.title", level),
                Text.translatable("lapidary.nowas-depths.toast.description"),
                new ItemStack(ModItems.POLISHER), ModMessages.Style.ACHIEVEMENT));
    }

    /**
     * Un nombre accordé : « 1 point », « 3 points ». La clé reçoit {@code .zero}, {@code .one} ou
     * {@code .many} selon le nombre, et chaque langue y met sa forme (0 est au singulier en
     * français, au pluriel en anglais).
     */
    public static Text count(String key, int amount) {
        return Text.translatable(key + (amount == 0 ? ".zero" : amount == 1 ? ".one" : ".many"), amount);
    }

    /** Demande du client : dépenser un point sur un nœud. Tout est revérifié côté serveur. */
    static void tryUnlock(ServerPlayerEntity player, String nodeId) {
        LapidarySkillTree.Node node = LapidarySkillTree.get(nodeId);
        LapidaryData data = get(player);
        if (node == null || !LapidarySkillTree.canUnlock(data, node)) {
            return;
        }
        set(player, data.withUnlocked(nodeId));
        // les nœuds de livre donneront leur tome du guide : les livres ne sont pas encore écrits
        // (LapidarySkillTree.bookVolume), il n'y a donc rien à remettre pour l'instant
        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS, 1.0f, 1.2f);
    }
}
