package com.nowa.lapidary;

/**
 * Courbe de progression. On part du niveau 0 ; passer du niveau n au niveau n+1 coûte 40 + 20n XP
 * (40 pour le niveau 1, 620 pour le niveau 30), soit 9 900 XP pour le niveau 30 — de quoi remplir
 * l'arbre, qui compte exactement trente nœuds.
 *
 * <p>Il n'y a pas de niveau maximum : on continue de monter une fois l'arbre terminé, les points
 * s'accumulant alors sans emploi. Seule une limite technique subsiste, assez loin pour qu'aucune
 * partie ne l'atteigne, afin que les calculs tiennent dans un entier.
 */
public final class LapidaryLevels {
    /** Coût du premier niveau, puis supplément par niveau suivant. */
    private static int xpBase = 40;
    private static int xpStep = 20;
    private static int geodeXp = 32;
    private static int[] polishXp = {0, 4, 8, 16, 32};

    static {
        com.nowa.config.ModConfig.onApply(LapidaryLevels::applyConfig);
    }

    private static void applyConfig() {
        xpBase = com.nowa.config.ModConfig.i("lapidary.levels.xp_base", 40);
        xpStep = com.nowa.config.ModConfig.i("lapidary.levels.xp_step", 20);
        geodeXp = com.nowa.config.ModConfig.i("lapidary.xp.geode", 32);
        polishXp = com.nowa.config.ModConfig.ints("lapidary.xp.polish_by_hardness", new int[]{0, 4, 8, 16, 32});
    }
    /** Plafond technique de l'XP : le niveau correspondant est à cinq chiffres. */
    public static final int MAX_XP = 1_000_000_000;

    private LapidaryLevels() {
    }

    /** XP à gagner pendant le niveau {@code level} pour atteindre le suivant. */
    public static int xpToNext(int level) {
        return xpBase + xpStep * level;
    }

    /** XP d'une géode ouverte : autant qu'une pierre de dureté 4, pour ses quatre pierres d'un coup. */
    public static int geodeXp() {
        return geodeXp;
    }

    /** XP totale nécessaire pour atteindre {@code level} : la somme des paliers précédents. */
    public static int totalXpFor(int level) {
        return (int) Math.min(MAX_XP, exactXpFor(level));
    }

    /**
     * La même somme sans plafond, forme close de Σ(base + step·k) pour k de 0 à n−1 :
     * n·base + step·n(n−1)/2. Les comparaisons de niveau ont besoin d'une suite strictement croissante.
     */
    private static long exactXpFor(int level) {
        return (long) level * xpBase + (long) xpStep * level * (level - 1) / 2;
    }

    /**
     * Le niveau atteint avec cette XP. La somme se renverse : n = (√(900 + 40x) − 30) / 20.
     * On corrige d'un cran au besoin, l'arrondi des flottants n'étant pas sûr sur les bornes.
     */
    public static int levelFor(int xp) {
        // renversement de la somme : n = (√((base − step/2)² + 2·step·x) − (base − step/2)) / step
        double offset = xpBase - xpStep / 2.0;
        int level = xpStep == 0
                ? (xpBase == 0 ? 0 : Math.max(0, xp) / xpBase)
                : (int) ((Math.sqrt(offset * offset + 2.0 * xpStep * Math.max(0, xp)) - offset) / xpStep);
        while (level > 0 && exactXpFor(level) > xp) {
            level--;
        }
        while (exactXpFor(level + 1) <= xp) {
            level++;
        }
        return level;
    }

    /** XP déjà gagnée dans le niveau actuel (pour la barre de progression). */
    public static int progressInLevel(int xp) {
        return xp - totalXpFor(levelFor(xp));
    }

    public static int polishXp(int stoneTier) {
        return stoneTier > 0 && stoneTier < polishXp.length ? polishXp[stoneTier] : 0;
    }
}
