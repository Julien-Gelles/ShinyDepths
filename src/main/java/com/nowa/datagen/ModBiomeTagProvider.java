package com.nowa.datagen;

import com.nowa.world.ModBiomeTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.concurrent.CompletableFuture;

/**
 * Remplit les tags « ce biome est riche en … ». On passe par les tags conventionnels {@code c:}
 * chaque fois qu'il en existe un : un mod de biomes qui les respecte héritera de nos minerais
 * sans une ligne de code. Les quatre cas sans équivalent conventionnel citent un biome vanilla.
 */
public class ModBiomeTagProvider extends FabricTagProvider<Biome> {
    public ModBiomeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.BIOME, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        // l'or des fous là où l'on cherche l'or, le grenat sur les terres rouges
        getOrCreateTagBuilder(ModBiomeTags.HAS_PYRITE).addOptionalTag(ConventionalBiomeTags.IS_BADLANDS);
        getOrCreateTagBuilder(ModBiomeTags.HAS_GARNET).addOptionalTag(ConventionalBiomeTags.IS_BADLANDS);

        // métaux d'intrusion : les massifs, et les sommets pour le plus rare
        getOrCreateTagBuilder(ModBiomeTags.HAS_TITANIUM).addOptionalTag(ConventionalBiomeTags.IS_MOUNTAIN);
        getOrCreateTagBuilder(ModBiomeTags.HAS_PLATINUM).addOptionalTag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK);

        getOrCreateTagBuilder(ModBiomeTags.HAS_RUBY).addOptionalTag(ConventionalBiomeTags.IS_HOT_OVERWORLD);
        getOrCreateTagBuilder(ModBiomeTags.HAS_AMBER)
                .addOptionalTag(ConventionalBiomeTags.IS_TAIGA)
                .addOptionalTag(ConventionalBiomeTags.IS_OLD_GROWTH);
        getOrCreateTagBuilder(ModBiomeTags.HAS_JADE).addOptionalTag(ConventionalBiomeTags.IS_JUNGLE);
        getOrCreateTagBuilder(ModBiomeTags.HAS_BLUE_TOPAZ).addOptionalTag(ConventionalBiomeTags.IS_OCEAN);
        getOrCreateTagBuilder(ModBiomeTags.HAS_MOONSTONE)
                .addOptionalTag(ConventionalBiomeTags.IS_SNOWY)
                .addOptionalTag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK);

        // sans tag conventionnel : on cite le biome vanilla, en optionnel pour rester valide s'il disparaît
        getOrCreateTagBuilder(ModBiomeTags.HAS_TOURMALINE).addOptional(BiomeKeys.LUSH_CAVES.getValue());
        getOrCreateTagBuilder(ModBiomeTags.HAS_PERIDOT).addOptional(BiomeKeys.BASALT_DELTAS.getValue());
        getOrCreateTagBuilder(ModBiomeTags.HAS_COBALT).addOptional(BiomeKeys.SOUL_SAND_VALLEY.getValue());
        getOrCreateTagBuilder(ModBiomeTags.HAS_ROSE_QUARTZ).addOptional(BiomeKeys.CRIMSON_FOREST.getValue());
        getOrCreateTagBuilder(ModBiomeTags.HAS_SUGILITE)
                .addOptional(BiomeKeys.END_MIDLANDS.getValue())
                .addOptional(BiomeKeys.END_BARRENS.getValue());
        getOrCreateTagBuilder(ModBiomeTags.HAS_TANZANITE).addOptional(BiomeKeys.END_HIGHLANDS.getValue());
    }
}
