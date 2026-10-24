package com.nowa.lapidary;

import com.nowa.block.ModBlocks;
import com.nowa.block.ModOres;
import com.nowa.config.ModConfig;
import com.nowa.item.ModGear;
import com.nowa.item.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Arbre de compétences du lapidaire, refait le 2026-10-18 : trois branches — Taille, Prospection,
 * Atelier — et des nœuds qui coûtent des points. Le joueur gagne un point par niveau ; un nœud ne
 * demande que son parent, jamais un niveau, si bien qu'on peut foncer dans une branche dès le début.
 * Tout l'arbre coûte 75 points ; les niveaux suivants continuent de monter sans rien à dépenser.
 *
 * <p>Deux sortes de nœuds : les rangs d'une compétence ({@code keen_eye_1}, {@code keen_eye_2}…),
 * que le joueur prend dans l'ordre et dont le reste du mod demande le rang atteint (valeurs dans
 * {@link LapidaryBonuses}) ; et les recettes, un nœud chacune, qui permettent de fabriquer un outil
 * du métier (voir {@link LapidaryCrafting}).
 *
 * <p>Coordonnées en pixels, racine en (0, 0) ; l'arbre se lit de gauche à droite, chaque branche sur
 * ses lignes : la Taille au-dessus de la racine, la Prospection et l'Atelier en dessous, sans ligne
 * vide entre deux branches. L'écran centre l'arbre et le fait glisser.
 */
public final class LapidarySkillTree {
    public static final String ROOT = "root";

    /** Une compétence et le nombre de rangs qu'elle compte. */
    public enum Skill {
        /** Chances de pureté au polissage et à l'estimation. */
        KEEN_EYE("keen_eye", 5),
        /** Usure du polissoir. */
        STEADY_HAND("steady_hand", 2),
        /** Vitesse du polissage à la main. */
        QUICK_POLISH("quick_polish", 2),
        /** Pierres dédoublées à l'estimation. */
        FACET("facet", 3),
        /** Butin supplémentaire dans les minerais du mod. */
        DEPOSIT("deposit", 3),
        /** Vitesse de minage des minerais du mod. */
        QUARRYING("quarrying", 2),
        /** Nodule supplémentaire dans les roches à géode. */
        NODULE("nodule", 2),
        /** XP de lapidaire gagnée. */
        STUDY("study", 3),
        /** Vitesse du creuset de fusion. */
        FAST_FUSION("fast_fusion", 2),
        /** Le creuset épargne parfois une pierre : le nœud de départ. */
        RECOVERY("recovery", 1),
        /** Nombre de pierres que le creuset épargne d'un coup. */
        RECOVERY_COUNT("recovery_count", 2),
        /** Chance que le creuset épargne des pierres. */
        RECOVERY_CHANCE("recovery_chance", 2),
        /** Remise du villageois lapidaire. */
        TRADE("trade", 3);

        private final String id;
        private final int ranks;

        Skill(String id, int ranks) {
            this.id = id;
            this.ranks = ranks;
        }

        public String id() {
            return id;
        }

        public int ranks() {
            return ranks;
        }

        public String nodeId(int rank) {
            return id + "_" + rank;
        }
    }

    /** Les recettes du métier, un nœud chacune : l'objet qu'elles permettent de fabriquer. */
    public enum Recipe {
        LAPIDARY_TABLE("lapidary_table", ModBlocks.NETHERITE_LAPIDARY_TABLE),
        LOUPE("loupe", ModItems.LOUPE),
        PYRITE_POLISHER("pyrite_polisher", ModItems.PYRITE_POLISHER),
        COBALT_POLISHER("cobalt_polisher", ModItems.COBALT_POLISHER),
        SOCKETING_BENCH("socketing_bench", ModBlocks.SOCKETING_BENCH),
        FUSION_CRUCIBLE("fusion_crucible", ModBlocks.FUSION_CRUCIBLE);

        private final String id;
        private final ItemConvertible item;

        Recipe(String id, ItemConvertible item) {
            this.id = id;
            this.item = item;
        }

        public String nodeId() {
            return "recipe_" + id;
        }

        public Item item() {
            return item.asItem();
        }
    }

    /** @param cost les points qu'il coûte ; 0 pour la racine, acquise d'office */
    public record Node(String id, ItemConvertible icon, int x, int y, @Nullable String parent, int cost) {
        public boolean isRoot() {
            return parent == null;
        }

        public String translationKey() {
            return "lapidary.shiny-depths.node." + id;
        }
    }

    private static final int COL_SPACING = 48;
    /**
     * Trois colonnes avant les branches : la racine seule, puis les livres, puis le tronc — la
     * barre verticale d'où partent toutes les branches —, puis les branches elles-mêmes.
     */
    public static final int BOOK_X = 56;
    private static final int FIRST_COLUMN = 112;
    /** Abscisse du tronc, à mi-chemin entre le bord droit des livres et la première colonne (cases de 26). */
    public static final int TRUNK_X = (BOOK_X + 26 + FIRST_COLUMN) / 2;
    private static final int ROW_SPACING = 40;

    private static final Map<String, Node> NODES = new LinkedHashMap<>();

    static {
        add(new Node(ROOT, ModItems.POLISHER, 0, 0, null, 0));

        // ---------- Livres : sous la racine, à la verticale, gratuits ----------
        book(1, ROOT, Items.BOOK);
        book(2, bookId(1), Items.WRITABLE_BOOK);
        book(3, bookId(2), Items.ENCHANTED_BOOK);

        // ---------- Taille : ce qu'on tire de la pierre, et de quoi la tailler ----------
        chain(Skill.KEEN_EYE, ROOT, -4, 0, new int[]{1, 1, 2, 2, 3},
                ModItems.AMBER, ModItems.PERIDOT, ModItems.GARNET, ModItems.SAPPHIRE, ModItems.RUBY);
        recipe(Recipe.LAPIDARY_TABLE, Skill.KEEN_EYE.nodeId(1), -5, 0, 2);
        recipe(Recipe.LOUPE, Skill.KEEN_EYE.nodeId(2), -5, 1, 1);
        chain(Skill.FACET, Skill.KEEN_EYE.nodeId(2), -5, 2, new int[]{2, 2, 3},
                ModItems.MOONSTONE, ModItems.BLUE_TOPAZ, ModItems.TANZANITE);
        chain(Skill.STEADY_HAND, Skill.KEEN_EYE.nodeId(1), -3, 1, new int[]{1, 2},
                Items.FLINT, Items.GRINDSTONE);
        chain(Skill.QUICK_POLISH, Skill.STEADY_HAND.nodeId(1), -2, 2, new int[]{1, 2},
                Items.FEATHER, Items.SUGAR);
        recipe(Recipe.PYRITE_POLISHER, Skill.STEADY_HAND.nodeId(1), -1, 2, 2);
        recipe(Recipe.COBALT_POLISHER, Recipe.PYRITE_POLISHER.nodeId(), -1, 3, 3);

        // ---------- Prospection : ce qu'on rapporte de la mine ----------
        chain(Skill.DEPOSIT, ROOT, 1, 0, new int[]{1, 2, 3},
                ModItems.RAW_RUBY, ModItems.RAW_SAPPHIRE, ModItems.RAW_PLATINUM);
        chain(Skill.QUARRYING, Skill.DEPOSIT.nodeId(1), 2, 1, new int[]{1, 2},
                ModGear.of(ModGear.Metal.PYRITE).pickaxe(), ModGear.of(ModGear.Metal.COBALT).pickaxe());
        chain(Skill.NODULE, Skill.DEPOSIT.nodeId(2), 0, 2, new int[]{2, 2},
                ModItems.GEODE, ModOres.CALCITE_GEODE_ORE);

        // ---------- Atelier : les machines du métier, l'étude et le commerce ----------
        chain(Skill.STUDY, ROOT, 3, 0, new int[]{1, 2, 3},
                Items.BOOK, Items.WRITABLE_BOOK, Items.EXPERIENCE_BOTTLE);
        recipe(Recipe.SOCKETING_BENCH, ROOT, 4, 0, 2);
        recipe(Recipe.FUSION_CRUCIBLE, Recipe.SOCKETING_BENCH.nodeId(), 4, 1, 3);
        chain(Skill.FAST_FUSION, Recipe.FUSION_CRUCIBLE.nodeId(), 4, 2, new int[]{2, 2},
                Items.BLAZE_POWDER, Items.LAVA_BUCKET);
        chain(Skill.RECOVERY, Recipe.FUSION_CRUCIBLE.nodeId(), 5, 2, new int[]{2},
                Items.HOPPER);
        chain(Skill.RECOVERY_COUNT, Skill.RECOVERY.nodeId(1), 5, 3, new int[]{2, 2},
                Items.CAULDRON, Items.CHEST);
        chain(Skill.RECOVERY_CHANCE, Skill.RECOVERY.nodeId(1), 6, 3, new int[]{2, 3},
                Items.RABBIT_FOOT, Items.NETHER_STAR);
        chain(Skill.TRADE, ROOT, 6, 0, new int[]{1, 2, 3},
                Items.EMERALD, ModBlocks.GEM_SHOWCASE, Items.EMERALD_BLOCK);
    }

    private LapidarySkillTree() {
    }

    /**
     * Pose une compétence : ses rangs se suivent de gauche à droite sur une ligne, le premier
     * accroché au nœud donné. Le coût de chaque rang se règle dans le fichier de configuration.
     */
    private static void chain(Skill skill, String parent, int row, int startColumn,
                              int[] costs, ItemConvertible... icons) {
        String previous = parent;
        for (int rank = 1; rank <= skill.ranks(); rank++) {
            String id = skill.nodeId(rank);
            add(new Node(id, icons[rank - 1], x(startColumn + rank - 1), row * ROW_SPACING, previous,
                    cost(id, costs[rank - 1])));
            previous = id;
        }
    }

    /** Les livres du guide du mod : nombre de tomes, et ce qui les nomme. */
    public static final int BOOKS = 3;

    public static String bookId(int volume) {
        return "book_" + volume;
    }

    /** Le tome que donne ce nœud, ou 0 si ce n'est pas un livre. */
    public static int bookVolume(Node node) {
        for (int volume = 1; volume <= BOOKS; volume++) {
            if (node.id().equals(bookId(volume))) {
                return volume;
            }
        }
        return 0;
    }

    /**
     * Pose un livre : dans sa colonne, entre la racine et le tronc, un rang plus bas que le
     * précédent ; le premier pend de la barre qui va de la racine au tronc. Gratuit par défaut ; le
     * coût se règle comme les autres.
     */
    private static void book(int volume, String parent, ItemConvertible icon) {
        String id = bookId(volume);
        add(new Node(id, icon, BOOK_X, volume * ROW_SPACING, parent, cost(id, 0)));
    }

    /** Pose une recette : un nœud seul, dont l'icône est l'objet qu'elle permet de fabriquer. */
    private static void recipe(Recipe recipe, String parent, int row, int column, int cost) {
        String id = recipe.nodeId();
        add(new Node(id, recipe.item(), x(column), row * ROW_SPACING, parent, cost(id, cost)));
    }

    private static int x(int column) {
        return FIRST_COLUMN + COL_SPACING * column;
    }

    private static int cost(String id, int fallback) {
        return Math.max(0, ModConfig.i("lapidary.tree." + id + ".cost", fallback));
    }

    /** Rang atteint dans une compétence : le nombre de ses nœuds débloqués, dans l'ordre. */
    public static int rank(LapidaryData data, Skill skill) {
        int rank = 0;
        while (rank < skill.ranks() && data.isUnlocked(skill.nodeId(rank + 1))) {
            rank++;
        }
        return rank;
    }

    /** Rang de Coup d'œil (0 à 5), qui fixe les chances de pureté. */
    public static int purityRank(LapidaryData data) {
        return rank(data, Skill.KEEN_EYE);
    }

    /** La recette qui permet de fabriquer cet objet, ou {@code null} s'il n'est pas réservé au métier. */
    @Nullable
    public static Recipe recipeFor(Item item) {
        for (Recipe recipe : Recipe.values()) {
            if (recipe.item() == item) {
                return recipe;
            }
        }
        return null;
    }

    /** Les points dépensés pour ces nœuds : la somme de leurs coûts. Un nœud qui n'existe plus ne coûte rien. */
    public static int spentPoints(Collection<String> unlocked) {
        int spent = 0;
        for (String id : unlocked) {
            Node node = get(id);
            spent += node == null ? 0 : node.cost();
        }
        return spent;
    }

    private static void add(Node node) {
        NODES.put(node.id(), node);
    }

    public static List<Node> nodes() {
        return List.copyOf(NODES.values());
    }

    @Nullable
    public static Node get(String id) {
        return NODES.get(id);
    }

    /** La racine est toujours acquise. */
    public static boolean isUnlocked(LapidaryData data, Node node) {
        return node.isRoot() || data.isUnlocked(node.id());
    }

    public static boolean parentUnlocked(LapidaryData data, Node node) {
        Node parent = node.parent() == null ? null : get(node.parent());
        return parent == null || isUnlocked(data, parent);
    }

    /** Le joueur peut-il dépenser ses points sur ce nœud maintenant ? Il lui faut son parent, et de quoi le payer. */
    public static boolean canUnlock(LapidaryData data, Node node) {
        return !isUnlocked(data, node)
                && parentUnlocked(data, node)
                && data.availablePoints() >= node.cost();
    }
}
