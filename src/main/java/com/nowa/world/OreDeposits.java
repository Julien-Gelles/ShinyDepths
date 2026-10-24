package com.nowa.world;

import com.nowa.block.ModOres;
import com.nowa.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.nowa.NowasDepths.MOD_ID;

/**
 * La table de génération des minerais, telle que décrite dans docs/minerais.md.
 * Chaque gisement donne une feature de fond, posée sur toute sa dimension, et, quand un biome
 * lui est associé, une seconde feature qui vient s'y ajouter : le biome densifie, il ne conditionne pas.
 */
public final class OreDeposits {
    /**
     * Un gisement éteint n'est plus ajouté à aucun biome. C'est le seul réglage de génération qui
     * agisse en cours de route : la forme des veines et leur densité sont écrites dans les fichiers
     * de données du mod, et il faudrait les construire au chargement du monde pour les régler ici.
     */
    public static boolean enabled(Deposit deposit) {
        return ModConfig.flag("worldgen." + deposit.name() + ".enabled", true);
    }
    /** Uniforme : même chance partout ; trapèze : pic au milieu de la plage, comme le diamant. */
    public enum Shape {UNIFORM, TRAPEZOID}

    /** Où la génération de fond s'applique. */
    public enum Zone {OVERWORLD, NETHER, END_ISLANDS}

    /** Soit un nombre de veines par chunk, soit une veine tous les N chunks. */
    public record Density(int perChunk, int oneInChunks) {
        public static final Density NONE = new Density(0, 0);

        public static Density veines(int perChunk) {
            return new Density(perChunk, 0);
        }

        public static Density chunks(int oneInChunks) {
            return new Density(0, oneInChunks);
        }

        public boolean present() {
            return perChunk > 0 || oneInChunks > 0;
        }

        /** Le modificateur de comptage correspondant, placé en tête de liste comme en vanilla. */
        public PlacementModifier modifier() {
            return perChunk > 0 ? CountPlacementModifier.of(perChunk) : RarityFilterPlacementModifier.of(oneInChunks);
        }
    }

    /**
     * @param ores  les blocs à poser : deux pour un minerai présent à la fois dans la pierre et la deepslate
     * @param bonus densité <em>ajoutée</em> dans les biomes du tag : les deux features se cumulent,
     *              donc base + bonus doit donner le total annoncé dans docs/minerais.md
     */
    public record Deposit(String name, List<Block> ores, Zone zone, int size, float airDiscard,
                          int minY, int maxY, Shape shape, Density base, Density bonus,
                          @Nullable TagKey<Biome> biomes) {

        public RegistryKey<ConfiguredFeature<?, ?>> configured() {
            return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier.of(MOD_ID, "ore_" + name));
        }

        public RegistryKey<PlacedFeature> placedBase() {
            return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, "ore_" + name));
        }

        /** La feature supplémentaire des biomes favorisés. */
        public RegistryKey<PlacedFeature> placedBonus() {
            return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, "ore_" + name + "_biome"));
        }

        /** Les roches que le minerai peut remplacer, déduites des blocs déclarés. */
        public List<OreFeatureConfig.Target> targets() {
            List<OreFeatureConfig.Target> targets = new ArrayList<>();
            for (Block ore : ores) {
                targets.add(OreFeatureConfig.createTarget(ModOres.rockOf(ore).replaceable, ore.getDefaultState()));
            }
            return targets;
        }

        public List<PlacementModifier> modifiers(Density density) {
            return List.of(density.modifier(), SquarePlacementModifier.of(), heightModifier(), BiomePlacementModifier.of());
        }

        private PlacementModifier heightModifier() {
            YOffset min = YOffset.fixed(minY);
            YOffset max = YOffset.fixed(maxY);
            return shape == Shape.TRAPEZOID
                    ? HeightRangePlacementModifier.trapezoid(min, max)
                    : HeightRangePlacementModifier.uniform(min, max);
        }
    }

    public static final List<Deposit> ALL = List.of(
            // ---------- Overworld ----------
            new Deposit("pyrite", List.of(ModOres.PYRITE_ORE), Zone.OVERWORLD, 9, 0.0f,
                    0, 96, Shape.UNIFORM, Density.veines(4), Density.veines(6), ModBiomeTags.HAS_PYRITE),
            new Deposit("titanium", List.of(ModOres.TITANIUM_ORE, ModOres.DEEPSLATE_TITANIUM_ORE), Zone.OVERWORLD, 6, 0.30f,
                    -64, 16, Shape.TRAPEZOID, Density.veines(1), Density.veines(2), ModBiomeTags.HAS_TITANIUM),
            // le plafond monte à 16 comme le titane : en dessous de 0 tout est deepslate, et la
            // variante de pierre n'aurait jamais paru
            new Deposit("platinum", List.of(ModOres.PLATINUM_ORE, ModOres.DEEPSLATE_PLATINUM_ORE), Zone.OVERWORLD, 4, 0.70f,
                    -64, 16, Shape.TRAPEZOID, Density.chunks(8), Density.chunks(2), ModBiomeTags.HAS_PLATINUM),
            new Deposit("ruby", List.of(ModOres.RUBY_ORE, ModOres.DEEPSLATE_RUBY_ORE), Zone.OVERWORLD, 5, 0.40f,
                    -48, 16, Shape.TRAPEZOID, Density.veines(2), Density.veines(2), ModBiomeTags.HAS_RUBY),
            new Deposit("sapphire", List.of(ModOres.DEEPSLATE_SAPPHIRE_ORE), Zone.OVERWORLD, 3, 1.0f,
                    -64, -16, Shape.UNIFORM, Density.chunks(6), Density.NONE, null),
            new Deposit("garnet", List.of(ModOres.GARNET_ORE), Zone.OVERWORLD, 6, 0.20f,
                    0, 80, Shape.UNIFORM, Density.chunks(4), Density.veines(6), ModBiomeTags.HAS_GARNET),
            new Deposit("amber", List.of(ModOres.AMBER_ORE), Zone.OVERWORLD, 7, 0.50f,
                    32, 128, Shape.TRAPEZOID, Density.chunks(4), Density.veines(5), ModBiomeTags.HAS_AMBER),
            new Deposit("jade", List.of(ModOres.JADE_ORE), Zone.OVERWORLD, 6, 0.30f,
                    0, 96, Shape.UNIFORM, Density.chunks(4), Density.veines(5), ModBiomeTags.HAS_JADE),
            new Deposit("blue_topaz", List.of(ModOres.BLUE_TOPAZ_ORE, ModOres.DEEPSLATE_BLUE_TOPAZ_ORE), Zone.OVERWORLD, 4, 0.60f,
                    -48, 32, Shape.UNIFORM, Density.chunks(8), Density.veines(3), ModBiomeTags.HAS_BLUE_TOPAZ),
            new Deposit("tourmaline", List.of(ModOres.TOURMALINE_ORE, ModOres.DEEPSLATE_TOURMALINE_ORE), Zone.OVERWORLD, 4, 0.50f,
                    -48, 32, Shape.UNIFORM, Density.chunks(10), Density.veines(4), ModBiomeTags.HAS_TOURMALINE),
            new Deposit("moonstone", List.of(ModOres.MOONSTONE_ORE), Zone.OVERWORLD, 5, 0.0f,
                    64, 256, Shape.TRAPEZOID, Density.chunks(12), Density.veines(4), ModBiomeTags.HAS_MOONSTONE),

            // le nodule isolé dans la roche : une seule pierre par « veine », et rare
            new Deposit("geode", List.of(ModOres.GEODE_ORE, ModOres.DEEPSLATE_GEODE_ORE), Zone.OVERWORLD, 1, 0.50f,
                    -50, 30, Shape.UNIFORM, Density.chunks(6), Density.NONE, null),

            // ---------- Nether ----------
            new Deposit("peridot", List.of(ModOres.PERIDOT_ORE), Zone.NETHER, 6, 0.0f,
                    10, 117, Shape.UNIFORM, Density.veines(4), Density.veines(6), ModBiomeTags.HAS_PERIDOT),
            new Deposit("cobalt", List.of(ModOres.COBALT_ORE), Zone.NETHER, 5, 0.20f,
                    10, 117, Shape.UNIFORM, Density.veines(3), Density.veines(5), ModBiomeTags.HAS_COBALT),
            new Deposit("rose_quartz", List.of(ModOres.ROSE_QUARTZ_ORE), Zone.NETHER, 7, 0.0f,
                    10, 117, Shape.UNIFORM, Density.chunks(3), Density.veines(8), ModBiomeTags.HAS_ROSE_QUARTZ),

            // ---------- End, îles extérieures seulement ----------
            new Deposit("onyx", List.of(ModOres.ONYX_ORE), Zone.END_ISLANDS, 6, 0.0f,
                    0, 80, Shape.UNIFORM, Density.veines(3), Density.NONE, null),
            new Deposit("sugilite", List.of(ModOres.SUGILITE_ORE), Zone.END_ISLANDS, 4, 0.40f,
                    0, 80, Shape.UNIFORM, Density.chunks(2), Density.veines(2), ModBiomeTags.HAS_SUGILITE),
            new Deposit("tanzanite", List.of(ModOres.TANZANITE_ORE), Zone.END_ISLANDS, 3, 1.0f,
                    20, 80, Shape.TRAPEZOID, Density.NONE, Density.chunks(6), ModBiomeTags.HAS_TANZANITE)
    );

    private OreDeposits() {
    }
}
