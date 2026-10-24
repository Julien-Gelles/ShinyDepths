package com.nowa.lapidary;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Progression de lapidaire d'un joueur. Immuable : chaque changement crée une nouvelle instance,
 * ce qui déclenche la sauvegarde et la synchronisation de l'attachment.
 *
 * @param xp       XP totale gagnée depuis le début ; le niveau en est déduit (voir {@link LapidaryLevels})
 * @param unlocked identifiants des nœuds de l'arbre débloqués (chacun au coût qu'il a dans l'arbre)
 */
public record LapidaryData(int xp, List<String> unlocked) {
    public static final LapidaryData EMPTY = new LapidaryData(0, List.of());

    public static final Codec<LapidaryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("xp", 0).forGetter(LapidaryData::xp),
            Codec.STRING.listOf().optionalFieldOf("unlocked", List.of()).forGetter(LapidaryData::unlocked)
    ).apply(instance, LapidaryData::new));

    public static final PacketCodec<ByteBuf, LapidaryData> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, LapidaryData::xp,
            PacketCodecs.STRING.collect(PacketCodecs.toList()), LapidaryData::unlocked,
            LapidaryData::new);

    public LapidaryData {
        unlocked = List.copyOf(unlocked);
    }

    public int level() {
        return LapidaryLevels.levelFor(xp);
    }

    /** Points gagnés (1 par niveau) moins points dépensés, au coût de chaque nœud. */
    public int availablePoints() {
        return level() - LapidarySkillTree.spentPoints(unlocked);
    }

    public boolean isUnlocked(String nodeId) {
        return unlocked.contains(nodeId);
    }

    public LapidaryData withXp(int newXp) {
        return new LapidaryData(Math.max(0, Math.min(newXp, LapidaryLevels.MAX_XP)), unlocked);   // plafond technique seulement
    }

    public LapidaryData withUnlocked(String nodeId) {
        List<String> nodes = new ArrayList<>(unlocked);
        nodes.add(nodeId);
        return new LapidaryData(xp, nodes);
    }

    /** Ne garde que les nœuds qui existent encore (l'arbre évolue d'une version à l'autre). */
    public LapidaryData retainUnlocked(Predicate<String> exists) {
        List<String> kept = unlocked.stream().filter(exists).toList();
        return kept.size() == unlocked.size() ? this : new LapidaryData(xp, kept);
    }

    public LapidaryData withoutUnlocks() {
        return new LapidaryData(xp, List.of());
    }
}
