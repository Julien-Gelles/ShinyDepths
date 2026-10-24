package com.nowa.block;

import com.nowa.item.ModItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChainBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

public class ModBlocks {
    /** Pierre polie → son bloc (9 pierres d'une même classe de pureté). */
    private static final Map<Item, Block> GEM_BLOCKS = new LinkedHashMap<>();
    /** Pierre brute ou lingot → son bloc de rangement (recette réversible classique). */
    private static final Map<Item, Block> COMPACT_BLOCKS = new LinkedHashMap<>();

    // ---------- blocs de pierres polies ----------
    public static final Block RUBY_BLOCK = registerGemBlock("ruby_block", ModItems.RUBY);
    public static final Block MOONSTONE_BLOCK = registerGemBlock("moonstone_block", ModItems.MOONSTONE);
    public static final Block ONYX_BLOCK = registerGemBlock("onyx_block", ModItems.ONYX);
    public static final Block GARNET_BLOCK = registerGemBlock("garnet_block", ModItems.GARNET);
    public static final Block AMBER_BLOCK = registerGemBlock("amber_block", ModItems.AMBER);
    public static final Block PERIDOT_BLOCK = registerGemBlock("peridot_block", ModItems.PERIDOT);
    public static final Block JADE_BLOCK = registerGemBlock("jade_block", ModItems.JADE);
    public static final Block BLUE_TOPAZ_BLOCK = registerGemBlock("blue_topaz_block", ModItems.BLUE_TOPAZ);
    public static final Block TANZANITE_BLOCK = registerGemBlock("tanzanite_block", ModItems.TANZANITE);
    public static final Block ROSE_QUARTZ_BLOCK = registerGemBlock("rose_quartz_block", ModItems.ROSE_QUARTZ);
    public static final Block SUGILITE_BLOCK = registerGemBlock("sugilite_block", ModItems.SUGILITE);
    public static final Block TOURMALINE_BLOCK = registerGemBlock("tourmaline_block", ModItems.TOURMALINE);
    public static final Block SAPPHIRE_BLOCK = registerGemBlock("sapphire_block", ModItems.SAPPHIRE);

    // ---------- blocs de lingots ----------
    public static final Block PLATINUM_BLOCK = registerCompactBlock("platinum_block", ModItems.PLATINUM_INGOT, Blocks.IRON_BLOCK);
    public static final Block PYRITE_BLOCK = registerCompactBlock("pyrite_block", ModItems.PYRITE_INGOT, Blocks.IRON_BLOCK);
    public static final Block COBALT_BLOCK = registerCompactBlock("cobalt_block", ModItems.COBALT_INGOT, Blocks.IRON_BLOCK);
    public static final Block TITANIUM_BLOCK = registerCompactBlock("titanium_block", ModItems.TITANIUM_INGOT, Blocks.IRON_BLOCK);

    // ---------- blocs de matières brutes (métaux seulement : les pierres se polissent) ----------
    public static final Block RAW_PLATINUM_BLOCK = registerRawBlock("raw_platinum_block", ModItems.RAW_PLATINUM);
    public static final Block RAW_PYRITE_BLOCK = registerRawBlock("raw_pyrite_block", ModItems.RAW_PYRITE);
    public static final Block RAW_COBALT_BLOCK = registerRawBlock("raw_cobalt_block", ModItems.RAW_COBALT);
    public static final Block RAW_TITANIUM_BLOCK = registerRawBlock("raw_titanium_block", ModItems.RAW_TITANIUM);

    /** Ferronnerie de cobalt : lumière froide, et une veille qui tient les créatures à distance. */
    public static final Block COBALT_LANTERN = registerBlock("cobalt_lantern",
            new CobaltLanternBlock(AbstractBlock.Settings.copy(Blocks.LANTERN)));
    public static final Block COBALT_CHAIN = registerBlock("cobalt_chain",
            new ChainBlock(AbstractBlock.Settings.copy(Blocks.CHAIN)));

    /** Établi de sertisseur : pose et retire les pierres sur l'équipement. */
    public static final Block SOCKETING_BENCH = registerBlock("socketing_bench",
            new SocketingBenchBlock(benchSettings()));

    /** Creuset de fusion : quatre pierres identiques en font une de la pureté au-dessus. */
    public static final Block FUSION_CRUCIBLE = registerBlock("fusion_crucible",
            new FusionCrucibleBlock(benchSettings()));

    /** Vitrine à pierres : le poste de travail du villageois lapidaire. */
    public static final Block GEM_SHOWCASE = registerBlock("gem_showcase",
            new GemShowcaseBlock(AbstractBlock.Settings.copy(Blocks.LECTERN).nonOpaque()));

    /**
     * La table de lapidaire : une seule depuis la refonte du 2026-10-14, à l'allure de netherite. Elle a
     * le plus haut niveau des anciennes tables (4) : elle polit toutes les pierres, à la plus grande
     * vitesse. Les tables en fer et en diamant ont disparu.
     */
    public static final Block NETHERITE_LAPIDARY_TABLE = registerBlock("netherite_lapidary_table", new LapidaryTableBlock(4, benchSettings()));

    /** Pierre polie → bloc correspondant (pour les recettes et le datagen). */
    public static Map<Item, Block> gemBlocks() {
        return Collections.unmodifiableMap(GEM_BLOCKS);
    }

    /** Brut ou lingot → bloc correspondant. */
    public static Map<Item, Block> compactBlocks() {
        return Collections.unmodifiableMap(COMPACT_BLOCKS);
    }

    private static Block registerGemBlock(String name, Item gem) {
        Block block = registerBlock(name, new PurityBlock(AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK)));
        GEM_BLOCKS.put(gem, block);
        return block;
    }

    private static Block registerRawBlock(String name, Item raw) {
        return registerCompactBlock(name, raw, Blocks.RAW_IRON_BLOCK);
    }

    /** Bloc de rangement simple, avec les réglages d'un bloc vanilla. */
    private static Block registerCompactBlock(String name, Item item, Block model) {
        return registerCompact(name, item, new Block(AbstractBlock.Settings.copy(model)));
    }

    private static Block registerCompact(String name, Item item, Block block) {
        Block registered = registerBlock(name, block);
        COMPACT_BLOCKS.put(item, registered);
        return registered;
    }

    /** Ouvert au paquet pour que ModOres enregistre ses minerais de la même façon. */
    /**
     * Les établis sont faits de quatre blocs ajourés : ils ne cachent pas leurs voisins, et un
     * piston ne doit pas pouvoir en déplacer un seul.
     */
    private static AbstractBlock.Settings benchSettings() {
        return AbstractBlock.Settings.copy(Blocks.SMITHING_TABLE).nonOpaque()
                .pistonBehavior(net.minecraft.block.piston.PistonBehavior.BLOCK);
    }

    static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block){
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name),
               new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks(){
        ModOres.register();
        LOGGER.info("└─ Registering Mod Blocks for " + MOD_ID);
    }
}
