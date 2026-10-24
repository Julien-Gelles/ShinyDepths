package com.nowa.world;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import static com.nowa.NowasDepths.MOD_ID;

/**
 * Les biomes où un minerai est plus dense. Jamais une condition d'existence : chaque minerai garde
 * une génération de fond sur toute sa dimension, ces tags ne font qu'ajouter des veines en plus.
 * Leur contenu est rempli au datagen à partir des tags conventionnels {@code c:}, pour qu'un mod
 * de biomes ajouté plus tard en hérite sans qu'on touche au code.
 */
public final class ModBiomeTags {
    public static final TagKey<Biome> HAS_PYRITE = of("pyrite");
    public static final TagKey<Biome> HAS_TITANIUM = of("titanium");
    public static final TagKey<Biome> HAS_PLATINUM = of("platinum");
    public static final TagKey<Biome> HAS_RUBY = of("ruby");
    public static final TagKey<Biome> HAS_GARNET = of("garnet");
    public static final TagKey<Biome> HAS_AMBER = of("amber");
    public static final TagKey<Biome> HAS_JADE = of("jade");
    public static final TagKey<Biome> HAS_BLUE_TOPAZ = of("blue_topaz");
    public static final TagKey<Biome> HAS_TOURMALINE = of("tourmaline");
    public static final TagKey<Biome> HAS_MOONSTONE = of("moonstone");
    public static final TagKey<Biome> HAS_PERIDOT = of("peridot");
    public static final TagKey<Biome> HAS_COBALT = of("cobalt");
    public static final TagKey<Biome> HAS_ROSE_QUARTZ = of("rose_quartz");
    public static final TagKey<Biome> HAS_SUGILITE = of("sugilite");
    public static final TagKey<Biome> HAS_TANZANITE = of("tanzanite");

    private ModBiomeTags() {
    }

    private static TagKey<Biome> of(String material) {
        return TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "has_ore/" + material));
    }
}
