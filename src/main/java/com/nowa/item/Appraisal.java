package com.nowa.item;

import com.nowa.lapidary.Lapidary;
import com.nowa.lapidary.LapidaryBonuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * L'estimation d'une pierre polie : on lui révèle sa pureté, une fois pour toutes. Le sachet le
 * fait pour les pierres qui sortent de la table, la loupe pour celles qu'on trouve sans pureté.
 * Les deux tirent pareil : chances du Coup d'œil du joueur, et la Facette qui dédouble une pierre
 * de temps en temps.
 */
public final class Appraisal {
    private Appraisal() {
    }

    /**
     * Estime {@code count} pierres de ce type pour ce joueur. Les pierres se répartissent selon leur
     * pureté, une pile par classe (plusieurs si une classe dépasse une pile).
     */
    public static List<ItemStack> appraise(Item gem, int count, PlayerEntity player) {
        int rank = Lapidary.purityRank(player);
        double extra = LapidaryBonuses.extraGemChance(player);
        int[] counts = new int[Purity.values().length];
        for (int i = 0; i < count; i++) {
            int purity = Purity.roll(rank, player.getRandom()).ordinal();
            counts[purity] += extra > 0 && player.getRandom().nextDouble() < extra ? 2 : 1;
        }
        List<ItemStack> appraised = new ArrayList<>();
        for (Purity purity : Purity.values()) {
            int left = counts[purity.ordinal()];
            while (left > 0) {
                ItemStack gems = GemPolishing.polishedStack(gem, purity);
                gems.setCount(Math.min(left, gems.getMaxCount()));
                left -= gems.getCount();
                appraised.add(gems);
            }
        }
        return appraised;
    }
}
