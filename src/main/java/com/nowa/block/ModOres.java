package com.nowa.block;

import com.nowa.config.ModConfig;
import com.nowa.item.ModItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Les minerais dans la roche. Chacun connaît sa roche, sa matière brute et la pioche qu'il exige,
 * ce qui suffit au datagen pour en tirer le modèle, le butin et les tags.
 * Les plages de profondeur et les densités sont décrites dans docs/minerais.md.
 */
public final class ModOres {
    /** La roche hôte décide de l'aspect et de la résistance du bloc. */
    public enum Rock {
        STONE(Blocks.IRON_ORE, new TagMatchRuleTest(BlockTags.STONE_ORE_REPLACEABLES)),
        DEEPSLATE(Blocks.DEEPSLATE_IRON_ORE, new TagMatchRuleTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES)),
        NETHERRACK(Blocks.NETHER_QUARTZ_ORE, new TagMatchRuleTest(BlockTags.BASE_STONE_NETHER)),
        /** Seulement posée par la feature des géodes, jamais en filon. */
        CALCITE(Blocks.CALCITE, new BlockMatchRuleTest(Blocks.CALCITE)),
        END_STONE(Blocks.END_STONE, new BlockMatchRuleTest(Blocks.END_STONE));

        private final Block model;
        /** Ce que la veine a le droit de remplacer à la génération. */
        public final RuleTest replaceable;

        Rock(Block model, RuleTest replaceable) {
            this.model = model;
            this.replaceable = replaceable;
        }
    }

    /** Pioche minimum, selon la dureté de la pierre (voir la colonne « Outil » du doc). */
    public enum Tool {
        STONE(BlockTags.NEEDS_STONE_TOOL),
        IRON(BlockTags.NEEDS_IRON_TOOL),
        DIAMOND(BlockTags.NEEDS_DIAMOND_TOOL);

        public final TagKey<Block> tag;

        Tool(TagKey<Block> tag) {
            this.tag = tag;
        }
    }

    /** Un minerai et ce qu'il faut pour l'exploiter. */
    public record Ore(Block block, Rock rock, Item drop, Tool tool) {
    }

    private static final List<Ore> ORES = new ArrayList<>();

    // ---------- pierre ----------
    public static final Block PYRITE_ORE = register("pyrite_ore", Rock.STONE, ModItems.RAW_PYRITE, Tool.STONE, 0, 0);
    public static final Block TITANIUM_ORE = register("titanium_ore", Rock.STONE, ModItems.RAW_TITANIUM, Tool.IRON, 0, 0);
    public static final Block PLATINUM_ORE = register("platinum_ore", Rock.STONE, ModItems.RAW_PLATINUM, Tool.DIAMOND, 0, 0);
    public static final Block RUBY_ORE = register("ruby_ore", Rock.STONE, ModItems.RAW_RUBY, Tool.IRON, 3, 7);
    public static final Block GARNET_ORE = register("garnet_ore", Rock.STONE, ModItems.RAW_GARNET, Tool.STONE, 2, 5);
    public static final Block AMBER_ORE = register("amber_ore", Rock.STONE, ModItems.RAW_AMBER, Tool.STONE, 2, 5);
    public static final Block JADE_ORE = register("jade_ore", Rock.STONE, ModItems.RAW_JADE, Tool.STONE, 2, 5);
    public static final Block BLUE_TOPAZ_ORE = register("blue_topaz_ore", Rock.STONE, ModItems.RAW_BLUE_TOPAZ, Tool.IRON, 3, 7);
    public static final Block TOURMALINE_ORE = register("tourmaline_ore", Rock.STONE, ModItems.RAW_TOURMALINE, Tool.IRON, 3, 7);
    public static final Block MOONSTONE_ORE = register("moonstone_ore", Rock.STONE, ModItems.RAW_MOONSTONE, Tool.IRON, 3, 7);

    public static final Block GEODE_ORE = register("geode_ore", Rock.STONE, ModItems.GEODE, Tool.IRON, 2, 5);
    /** La paroi des géodes du monde : de la calcite d'où affleure un nodule. */
    public static final Block CALCITE_GEODE_ORE = register("calcite_geode_ore", Rock.CALCITE, ModItems.GEODE, Tool.IRON, 2, 5);

    // ---------- deepslate ----------
    public static final Block DEEPSLATE_TITANIUM_ORE = register("deepslate_titanium_ore", Rock.DEEPSLATE, ModItems.RAW_TITANIUM, Tool.IRON, 0, 0);
    public static final Block DEEPSLATE_PLATINUM_ORE = register("deepslate_platinum_ore", Rock.DEEPSLATE, ModItems.RAW_PLATINUM, Tool.DIAMOND, 0, 0);
    public static final Block DEEPSLATE_SAPPHIRE_ORE = register("deepslate_sapphire_ore", Rock.DEEPSLATE, ModItems.RAW_SAPPHIRE, Tool.DIAMOND, 4, 8);
    public static final Block DEEPSLATE_RUBY_ORE = register("deepslate_ruby_ore", Rock.DEEPSLATE, ModItems.RAW_RUBY, Tool.IRON, 3, 7);
    public static final Block DEEPSLATE_BLUE_TOPAZ_ORE = register("deepslate_blue_topaz_ore", Rock.DEEPSLATE, ModItems.RAW_BLUE_TOPAZ, Tool.IRON, 3, 7);
    public static final Block DEEPSLATE_TOURMALINE_ORE = register("deepslate_tourmaline_ore", Rock.DEEPSLATE, ModItems.RAW_TOURMALINE, Tool.IRON, 3, 7);
    public static final Block DEEPSLATE_GEODE_ORE = register("deepslate_geode_ore", Rock.DEEPSLATE, ModItems.GEODE, Tool.IRON, 2, 5);

    // ---------- netherrack ----------
    public static final Block PERIDOT_ORE = register("peridot_ore", Rock.NETHERRACK, ModItems.RAW_PERIDOT, Tool.IRON, 3, 7);
    public static final Block COBALT_ORE = register("cobalt_ore", Rock.NETHERRACK, ModItems.RAW_COBALT, Tool.IRON, 0, 0);
    public static final Block ROSE_QUARTZ_ORE = register("rose_quartz_ore", Rock.NETHERRACK, ModItems.RAW_ROSE_QUARTZ, Tool.STONE, 2, 5);

    // ---------- end stone ----------
    public static final Block ONYX_ORE = register("onyx_ore", Rock.END_STONE, ModItems.RAW_ONYX, Tool.STONE, 2, 5);
    public static final Block SUGILITE_ORE = register("sugilite_ore", Rock.END_STONE, ModItems.RAW_SUGILITE, Tool.IRON, 3, 7);
    public static final Block TANZANITE_ORE = register("tanzanite_ore", Rock.END_STONE, ModItems.RAW_TANZANITE, Tool.DIAMOND, 4, 8);

    private ModOres() {
    }

    /** La roche d'un minerai, pour le datagen et la génération. */
    public static Rock rockOf(Block ore) {
        return ORES.stream().filter(o -> o.block() == ore).findFirst().map(Ore::rock).orElse(Rock.STONE);
    }

    /** Tous les minerais, dans l'ordre de déclaration. */
    public static List<Ore> ores() {
        return Collections.unmodifiableList(ORES);
    }

    /** Les métaux ne lâchent pas d'expérience, comme le fer et l'or ; les pierres si, comme l'émeraude. */
    private static Block register(String name, Rock rock, Item drop, Tool tool, int xpMin, int xpMax) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.copy(rock.model);
        int min = ModConfig.i("ores." + name + ".xp_min", xpMin);
        int max = Math.max(min, ModConfig.i("ores." + name + ".xp_max", xpMax));
        Block block = max > 0
                ? new ExperienceDroppingBlock(UniformIntProvider.create(min, max), settings)
                : new Block(settings);
        Block registered = ModBlocks.registerBlock(name, block);
        ORES.add(new Ore(registered, rock, drop, tool));
        return registered;
    }

    /** Les minerais s'enregistrent au chargement de la classe ; cet appel le déclenche. */
    static void register() {
    }
}
