package com.nowa.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import com.nowa.block.ModOres;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.collection.DataPool;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.GeodeCrackConfig;
import net.minecraft.world.gen.feature.GeodeFeatureConfig;
import net.minecraft.world.gen.feature.GeodeLayerConfig;
import net.minecraft.world.gen.feature.GeodeLayerThicknessConfig;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;

import java.util.List;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;

import java.util.function.Predicate;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Branche les gisements de {@link OreDeposits} sur la génération du monde : les features sont
 * écrites en JSON par le datagen, et c'est ici qu'on dit dans quels biomes elles s'appliquent.
 */
public final class ModWorldGen {
    private ModWorldGen() {
    }

    public static final RegistryKey<ConfiguredFeature<?, ?>> GEODE =
            RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier.of(MOD_ID, "geode"));
    public static final RegistryKey<PlacedFeature> GEODE_PLACED =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, "geode"));

    /**
     * La géode du monde, sur le modèle de celle d'améthyste mais réduite : on peut y entrer, pas
     * s'y déplacer. Attention en relisant ces nombres : la feature compare une somme de 1/distance
     * à un seuil de 1/√couche, donc avec un seul point de distribution le rayon d'une couche vaut
     * la racine carrée de sa valeur — 14,44 donne un rayon de 3,8, pas de 14.
     *
     * <p>Résultat : une poche d'air de 5 blocs de large (57 blocs), une paroi de 24 blocs où une
     * calcite à géode affleure une fois sur six, 66 blocs de calcite et 56 de basalte lisse, soit une
     * boule de 8 blocs de large. La géode d'améthyste, avec ses trois points de distribution, fait
     * 15 blocs pour 10 d'air. La fissure permet d'en repérer une depuis une grotte.
     */
    /**
     * La paroi intérieure : de la calcite, où affleurent quelques nodules. Il n'existe pas de bloc de
     * géode plein — le seul bloc qui en contient une est la roche à géode — et une sur six suffit :
     * sur les 24 blocs de la paroi, cela fait 4 nodules, donc 16 pierres par géode.
     */
    private static BlockStateProvider paroi() {
        return new WeightedBlockStateProvider(DataPool.<BlockState>builder()
                .add(Blocks.CALCITE.getDefaultState(), 5)
                .add(ModOres.CALCITE_GEODE_ORE.getDefaultState(), 1));
    }

    private static GeodeFeatureConfig geodeConfig() {
        return new GeodeFeatureConfig(
                new GeodeLayerConfig(
                        BlockStateProvider.of(Blocks.AIR),
                        paroi(),
                        paroi(),
                        BlockStateProvider.of(Blocks.CALCITE),
                        BlockStateProvider.of(Blocks.SMOOTH_BASALT),
                        // le codec refuse une liste vide ; la chance de pousse étant nulle, elle ne sert jamais
                        List.of(ModOres.CALCITE_GEODE_ORE.getDefaultState()),
                        BlockTags.FEATURES_CANNOT_REPLACE,
                        BlockTags.GEODE_INVALID_BLOCKS),
                new GeodeLayerThicknessConfig(6.25, 8.41, 11.56, 14.44),
                new GeodeCrackConfig(0.4, 1.5, 1),
                0.0,                                                // aucune pousse à placer
                0.0,                                                // pas de bloc « bourgeonnant »
                true,
                UniformIntProvider.create(2, 3),                    // distance au mur extérieur
                ConstantIntProvider.create(1),                      // un seul centre : une vraie sphère
                ConstantIntProvider.create(1),
                -8, 8, 0.05, 1);
    }

    /** Les veines elles-mêmes : un bloc, une taille, une chance d'être annulée à l'air libre. */
    public static void bootstrapConfigured(Registerable<ConfiguredFeature<?, ?>> context) {
        for (OreDeposits.Deposit deposit : OreDeposits.ALL) {
            ConfiguredFeatures.register(context, deposit.configured(), Feature.ORE,
                    new OreFeatureConfig(deposit.targets(), deposit.size(), deposit.airDiscard()));
        }
        ConfiguredFeatures.register(context, GEODE, Feature.GEODE, geodeConfig());
    }

    /** Où et à quelle fréquence ces veines sont tentées : une passe de fond, une passe de biome. */
    public static void bootstrapPlaced(Registerable<PlacedFeature> context) {
        var configured = context.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        for (OreDeposits.Deposit deposit : OreDeposits.ALL) {
            RegistryEntry<ConfiguredFeature<?, ?>> entry = configured.getOrThrow(deposit.configured());
            if (deposit.base().present()) {
                PlacedFeatures.register(context, deposit.placedBase(), entry, deposit.modifiers(deposit.base()));
            }
            if (deposit.bonus().present()) {
                PlacedFeatures.register(context, deposit.placedBonus(), entry, deposit.modifiers(deposit.bonus()));
            }
        }
        PlacedFeatures.register(context, GEODE_PLACED, configured.getOrThrow(GEODE),
                List.of(RarityFilterPlacementModifier.of(32), SquarePlacementModifier.of(),
                        HeightRangePlacementModifier.uniform(YOffset.fixed(-50), YOffset.fixed(20)),
                        BiomePlacementModifier.of()));
    }

    /** Appelé à l'initialisation du mod : ajoute les features aux biomes concernés. */
    public static void register() {
        LOGGER.info("├─ Registering ore generation for " + MOD_ID);
        for (OreDeposits.Deposit deposit : OreDeposits.ALL) {
            if (!OreDeposits.enabled(deposit)) {
                continue;                                  // éteint dans le fichier de réglages
            }
            if (deposit.base().present()) {
                BiomeModifications.addFeature(zoneSelector(deposit.zone()),
                        GenerationStep.Feature.UNDERGROUND_ORES, deposit.placedBase());
            }
            if (deposit.bonus().present() && deposit.biomes() != null) {
                BiomeModifications.addFeature(BiomeSelectors.tag(deposit.biomes()),
                        GenerationStep.Feature.UNDERGROUND_ORES, deposit.placedBonus());
            }
        }
        // comme la géode d'améthyste : creusée avant les minerais, dans tout l'Overworld
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.LOCAL_MODIFICATIONS, GEODE_PLACED);
    }

    /** L'End ne reçoit rien sur l'île centrale : seules les îles extérieures portent des minerais. */
    private static Predicate<BiomeSelectionContext> zoneSelector(OreDeposits.Zone zone) {
        return switch (zone) {
            case OVERWORLD -> BiomeSelectors.foundInOverworld();
            case NETHER -> BiomeSelectors.foundInTheNether();
            case END_ISLANDS -> BiomeSelectors.tag(ConventionalBiomeTags.IS_OUTER_END_ISLAND);
        };
    }
}
