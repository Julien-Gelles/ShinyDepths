package com.nowa.item;

import com.mojang.serialization.Codec;
import com.nowa.config.ModConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.random.Random;

/**
 * Classe de pureté d'un joyau poli, tirée au polissage. Les chances dépendent du rang
 * de « Coup d'œil » du dernier lapidaire à avoir eu la pierre brute (0 à 5).
 */
public enum Purity implements StringIdentifiable {
    CHIPPED("chipped", Formatting.GRAY),
    DULL("dull", Formatting.WHITE),
    CLEAR("clear", Formatting.AQUA),
    PURE("pure", Formatting.LIGHT_PURPLE),
    PHILOSOPHAL("philosophal", Formatting.GOLD);

    public static final Codec<Purity> CODEC = StringIdentifiable.createCodec(Purity::values);
    public static final PacketCodec<ByteBuf, Purity> PACKET_CODEC =
            PacketCodecs.indexed(index -> values()[index], Purity::ordinal);

    /**
     * Chances en % (Ébréché, Terne, Limpide, Pur, Philosophal) ; ligne = rang de Coup d'œil.
     * La philosophale est devenue la classe rare du mod : inaccessible sans Coup d'œil, et
     * six fois sur cent au rang maximum, là où l'ancienne barre montait à quinze.
     */
    private static final int[][] DEFAULT_CHANCES = {
            {42, 38, 17, 3, 0},
            {34, 38, 22, 5, 1},
            {27, 37, 26, 8, 2},
            {20, 35, 30, 12, 3},
            {14, 31, 35, 16, 4},
            {8, 26, 40, 20, 6},
    };
    public static final int MAX_RANK = DEFAULT_CHANCES.length - 1;
    /** Les chances effectives, relues dans le fichier de réglages. */
    private static int[][] chances = DEFAULT_CHANCES;

    static {
        ModConfig.onApply(Purity::applyConfig);
    }

    /** Une clé par classe et par rang : lapidary.purity.rank_3.pure, par exemple. */
    private static void applyConfig() {
        int[][] values = new int[DEFAULT_CHANCES.length][];
        for (int rank = 0; rank < DEFAULT_CHANCES.length; rank++) {
            values[rank] = new int[DEFAULT_CHANCES[rank].length];
            for (Purity purity : values()) {
                values[rank][purity.ordinal()] = ModConfig.i(
                        "lapidary.purity.rank_" + rank + "." + purity.name, DEFAULT_CHANCES[rank][purity.ordinal()]);
            }
        }
        chances = values;
    }

    private final String name;
    private final Formatting color;

    Purity(String name, Formatting color) {
        this.name = name;
        this.color = color;
    }

    public static Purity roll(int rank, Random random) {
        int[] rolled = chances[Math.clamp(rank, 0, MAX_RANK)];
        int value = random.nextInt(100);
        for (Purity purity : values()) {
            value -= rolled[purity.ordinal()];
            if (value < 0) {
                return purity;
            }
        }
        return CHIPPED;
    }

    /** La chance, en %, de tirer cette classe au rang de Coup d'œil donné (0 = rien appris). */
    public int chance(int rank) {
        return chances[Math.clamp(rank, 0, MAX_RANK)][ordinal()];
    }

    public Text displayName() {
        return Text.translatable("purity.nowas-depths." + name).formatted(color);
    }

    @Override
    public String asString() {
        return name;
    }
}
