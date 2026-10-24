package com.nowa.item;

import com.nowa.config.ModConfig;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.random.Random;

import java.util.List;

/**
 * Ce qu'on trouve dans une géode : quatre pierres polies tirées au hasard, chacune indépendamment,
 * donc les doublons se cumulent. Le poids d'une pierre suit sa rareté dans le monde (voir docs/minerais.md),
 * de sorte qu'une géode reste surtout une source de pierres communes, avec un saphir de temps en temps.
 * La pureté, elle, n'est pas tirée ici : comme pour toute pierre sortie de la table, elle l'est quand
 * un joueur récupère le sachet, selon son rang Coup d'œil.
 */
public final class GeodeContents {
    /** Nombre de pierres dans une géode. */
    private static int draws = 4;

    public static int draws() {
        return draws;
    }

    private record Entry(Item stone, int weight) {
    }

    // peu commune : 10 · rare : 5 · très rare : 2
    private static final List<Entry> TABLE = List.of(
            new Entry(ModItems.ONYX, 10),
            new Entry(ModItems.GARNET, 10),
            new Entry(ModItems.AMBER, 10),
            new Entry(ModItems.JADE, 10),
            new Entry(ModItems.ROSE_QUARTZ, 10),
            new Entry(net.minecraft.item.Items.DIAMOND, 10),
            new Entry(net.minecraft.item.Items.EMERALD, 10),
            new Entry(ModItems.MOONSTONE, 5),
            new Entry(ModItems.PERIDOT, 5),
            new Entry(ModItems.BLUE_TOPAZ, 5),
            new Entry(ModItems.SUGILITE, 5),
            new Entry(ModItems.TOURMALINE, 5),
            new Entry(ModItems.RUBY, 5),
            new Entry(ModItems.TANZANITE, 2),
            new Entry(ModItems.SAPPHIRE, 2));

    /** Les poids effectifs, relus dans le fichier de réglages, et leur somme. */
    private static List<Entry> weighted = TABLE;
    private static int total = TABLE.stream().mapToInt(Entry::weight).sum();

    static {
        ModConfig.onApply(GeodeContents::applyConfig);
    }

    private static void applyConfig() {
        draws = ModConfig.i("geode.draws", 4);
        weighted = TABLE.stream()
                .map(entry -> new Entry(entry.stone(), ModConfig.i(
                        "geode.weights." + Registries.ITEM.getId(entry.stone()).getPath(), entry.weight())))
                .toList();
        total = Math.max(1, weighted.stream().mapToInt(Entry::weight).sum());
    }

    private GeodeContents() {
    }

    /** Une pierre au hasard, pondérée par sa rareté. */
    public static Item draw(Random random) {
        int roll = random.nextInt(total);
        for (Entry entry : weighted) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry.stone();
            }
        }
        return weighted.getFirst().stone();
    }

    /** Les pierres qu'une géode peut contenir, pour la documentation et les tests. */
    public static List<Item> stones() {
        return weighted.stream().map(Entry::stone).toList();
    }

    public static int weightOf(Item stone) {
        return weighted.stream().filter(entry -> entry.stone() == stone).mapToInt(Entry::weight).findFirst().orElse(0);
    }

    public static int totalWeight() {
        return total;
    }
}
