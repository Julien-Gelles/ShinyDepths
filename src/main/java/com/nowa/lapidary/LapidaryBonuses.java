package com.nowa.lapidary;

import com.nowa.config.ModConfig;
import com.nowa.lapidary.LapidarySkillTree.Skill;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Ce que valent les rangs de l'arbre. Tout est ici, et nulle part ailleurs : les endroits qui les
 * appliquent se contentent de demander, et docs/lapidaire.md recopie ces mêmes chiffres.
 *
 * <p>Chaque tableau commence par la valeur du rang 0, c'est-à-dire celle d'un joueur qui n'a rien
 * appris — lire le rang 0 doit toujours revenir à ne rien changer.
 */
public final class LapidaryBonuses {
    /** Main sûre : part de la durabilité normalement dépensée par le polissoir. */
    private static double[] polisherWear = {1.0, 0.66, 0.5};
    /** Facette : chance qu'une pierre estimée en donne deux. */
    private static double[] extraGem = {0.0, 0.06, 0.12, 0.20};
    /** Polissage vif : part du temps de polissage à la main qui reste. */
    private static double[] polishTime = {1.0, 0.8, 0.6667};
    /** Gisement : chance qu'un minerai du mod rende une matière brute de plus. */
    private static double[] extraDrop = {0.0, 0.10, 0.20, 0.30};
    /** Nodule : chance qu'une roche à géode rende un nodule de plus. */
    private static double[] extraNodule = {0.0, 0.15, 0.30};
    /** Abattage : multiplicateur de vitesse sur les minerais du mod. */
    private static double[] miningSpeed = {1.0, 1.25, 1.5};
    /** Étude : multiplicateur de l'XP de lapidaire. */
    private static double[] xpGain = {1.0, 1.1, 1.2, 1.3};
    /** Fusion rapide : part du temps de fusion du creuset qui reste. */
    private static double[] fusionTime = {1.0, 0.7, 0.4};
    /**
     * Récupération : chance que le creuset épargne des pierres à une fusion. L'indice 0 est « pas de
     * Récupération », 1 le nœud seul, puis les rangs de Récupération sûre.
     */
    private static double[] spareChance = {0.0, 0.05, 0.15, 0.30};
    /** Récupération : combien de pierres sont épargnées quand elle joue ; même indexation, par Récupération multiple. */
    private static int[] spareCount = {0, 1, 2, 3};
    /** Négoce : remise du villageois lapidaire, sur tout ce qu'il demande. */
    private static double[] tradeDiscount = {0.0, 0.10, 0.20, 0.30};

    static {
        ModConfig.onApply(LapidaryBonuses::applyConfig);
    }

    /** Chaque suite se règle rang par rang, l'indice 0 étant toujours « rien appris ». */
    private static void applyConfig() {
        polisherWear = ModConfig.doubles("lapidary.skills.steady_hand.polisher_wear", new double[]{1.0, 0.66, 0.5});
        extraGem = ModConfig.doubles("lapidary.skills.facet.extra_gem_chance", new double[]{0.0, 0.06, 0.12, 0.20});
        polishTime = ModConfig.doubles("lapidary.skills.quick_polish.time", new double[]{1.0, 0.8, 0.6667});
        extraDrop = ModConfig.doubles("lapidary.skills.deposit.extra_drop_chance", new double[]{0.0, 0.10, 0.20, 0.30});
        extraNodule = ModConfig.doubles("lapidary.skills.nodule.extra_nodule_chance", new double[]{0.0, 0.15, 0.30});
        miningSpeed = ModConfig.doubles("lapidary.skills.quarrying.mining_speed", new double[]{1.0, 1.25, 1.5});
        xpGain = ModConfig.doubles("lapidary.skills.study.xp_gain", new double[]{1.0, 1.1, 1.2, 1.3});
        fusionTime = ModConfig.doubles("lapidary.skills.fast_fusion.fusion_time", new double[]{1.0, 0.7, 0.4});
        spareChance = ModConfig.doubles("lapidary.skills.recovery.spare_chance", new double[]{0.0, 0.05, 0.15, 0.30});
        spareCount = ModConfig.ints("lapidary.skills.recovery.spare_count", new int[]{0, 1, 2, 3});
        tradeDiscount = ModConfig.doubles("lapidary.skills.trade.discount", new double[]{0.0, 0.10, 0.20, 0.30});
    }

    private LapidaryBonuses() {
    }

    /** Durabilité que le polissoir dépense réellement, jamais moins d'un point. */
    public static int polisherWear(PlayerEntity player, int cost) {
        return Math.max(1, (int) Math.round(cost * at(polisherWear, player, Skill.STEADY_HAND)));
    }

    public static double extraGemChance(PlayerEntity player) {
        return at(extraGem, player, Skill.FACET);
    }

    /** Part du temps de polissage à la main qui reste, de 1 (rien appris) à moins. */
    public static double polishTime(PlayerEntity player) {
        return at(polishTime, player, Skill.QUICK_POLISH);
    }

    public static double extraDropChance(PlayerEntity player) {
        return at(extraDrop, player, Skill.DEPOSIT);
    }

    public static double extraNoduleChance(PlayerEntity player) {
        return at(extraNodule, player, Skill.NODULE);
    }

    public static double miningSpeed(PlayerEntity player) {
        return at(miningSpeed, player, Skill.QUARRYING);
    }

    public static int xpGain(PlayerEntity player, int amount) {
        return (int) Math.round(amount * at(xpGain, player, Skill.STUDY));
    }

    /** Part du temps de fusion qui reste, à ce rang de Fusion rapide. */
    public static double fusionTime(int rank) {
        return at(fusionTime, rank);
    }

    /**
     * Chance que le creuset épargne des pierres : rien sans Récupération, puis selon le rang de
     * Récupération sûre.
     */
    public static double spareChance(boolean recovery, int chanceRank) {
        return recovery ? at(spareChance, 1 + chanceRank) : 0.0;
    }

    /** Combien de pierres le creuset épargne quand la Récupération joue, selon la Récupération multiple. */
    public static int spareCount(boolean recovery, int countRank) {
        return recovery ? spareCount[Math.clamp(1 + countRank, 0, spareCount.length - 1)] : 0;
    }

    /** Remise du villageois lapidaire pour ce joueur. */
    public static double tradeDiscount(PlayerEntity player) {
        return at(tradeDiscount, player, Skill.TRADE);
    }

    private static double at(double[] values, int rank) {
        return values[Math.max(0, Math.min(rank, values.length - 1))];
    }

    private static double at(double[] values, PlayerEntity player, Skill skill) {
        return values[Math.min(rank(player, skill), values.length - 1)];
    }

    private static int rank(PlayerEntity player, Skill skill) {
        return LapidarySkillTree.rank(Lapidary.get(player), skill);
    }
}
