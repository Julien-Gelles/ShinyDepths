package com.nowa.datagen;

import com.nowa.block.ModBlocks;
import com.nowa.item.ModGear;
import com.nowa.dynamite.Dynamite;
import com.nowa.headlamp.Headlamp;
import com.nowa.item.ModJewelry;
import com.nowa.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import com.nowa.block.ModBlocks;
import com.nowa.item.PouchDyeRecipe;
import com.nowa.item.PurityBlockRecipes;
import com.nowa.item.MinPurityIngredient;
import com.nowa.item.Purity;
import net.minecraft.data.server.recipe.ComplexRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.data.server.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

import static com.nowa.NowasDepths.MOD_ID;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        // bruts et lingots : compactage classique dans les deux sens
        ModBlocks.compactBlocks().forEach((item, block) ->
                offerReversibleCompactingRecipes(exporter, RecipeCategory.MISC, item, RecipeCategory.BUILDING_BLOCKS, block));

        // pierres polies : recettes spéciales, qui exigent la même pureté et la reportent sur le bloc
        ComplexRecipeJsonBuilder.create(PurityBlockRecipes.Compacting::new)
                .offerTo(exporter, Identifier.of(MOD_ID, "gem_block_compacting"));
        ComplexRecipeJsonBuilder.create(PurityBlockRecipes.Uncompacting::new)
                .offerTo(exporter, Identifier.of(MOD_ID, "gem_block_uncompacting"));

        // pépites : deux métaux seulement, dans les deux sens comme le fer et l'or. Les identifiants
        // sont donnés à la main, puisque le lingot se fait déjà depuis son bloc
        offerNuggetRecipes(exporter, ModItems.PYRITE_NUGGET, ModItems.PYRITE_INGOT, "pyrite");
        offerNuggetRecipes(exporter, ModItems.COBALT_NUGGET, ModItems.COBALT_INGOT, "cobalt");

        offerIngotSmelting(exporter, ModItems.RAW_PLATINUM, ModItems.PLATINUM_INGOT, 1.5f);
        offerIngotSmelting(exporter, ModItems.RAW_PYRITE, ModItems.PYRITE_INGOT, 0.7f);
        offerIngotSmelting(exporter, ModItems.RAW_COBALT, ModItems.COBALT_INGOT, 1.0f);
        offerIngotSmelting(exporter, ModItems.RAW_TITANIUM, ModItems.TITANIUM_INGOT, 1.2f);

        // Polissoir : un silex et un bâton, dans n'importe quelle disposition (comme le briquet)
        ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.POLISHER)
                .input(Items.FLINT)
                .input(Items.STICK)
                .criterion(hasItem(Items.FLINT), conditionsFromItem(Items.FLINT))
                .offerTo(exporter);

        // Polissoirs en métal : le polissoir de pierre et un lingot, dans n'importe quelle disposition
        offerMetalPolisher(exporter, ModItems.PYRITE_POLISHER, ModItems.PYRITE_INGOT);
        offerMetalPolisher(exporter, ModItems.COBALT_POLISHER, ModItems.COBALT_INGOT);

        // Loupe de lapidaire : une vitre cerclée de cuivre, sur un manche. Aucun nœud de l'arbre :
        // toute pierre trouvée doit pouvoir être estimée.
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.LOUPE)
                .pattern(" C ")
                .pattern("CGC")
                .pattern(" S ")
                .input('C', Items.COPPER_INGOT)
                .input('G', Items.GLASS_PANE)
                .input('S', Items.STICK)
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter);

        // Sachet à pierres : un simple cuir
        ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STONE_POUCH)
                .input(Items.LEATHER)
                .criterion(hasItem(Items.LEATHER), conditionsFromItem(Items.LEATHER))
                .offerTo(exporter);

        // Sachet + colorant : sachet de cette couleur, contenu conservé
        ComplexRecipeJsonBuilder.create(PouchDyeRecipe::new).offerTo(exporter, Identifier.of(MOD_ID, "stone_pouch_dyeing"));


        // La table de lapidaire : lingots de fer, polissoir et planches. Elle a l'allure de netherite,
        // mais c'est la table du début du métier : la seule depuis la refonte.
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.NETHERITE_LAPIDARY_TABLE)
                .pattern("III")
                .pattern("#P#")
                .pattern("###")
                .input('I', Items.IRON_INGOT)
                .input('P', ModItems.POLISHER)
                .input('#', ItemTags.PLANKS)
                .criterion(hasItem(ModItems.POLISHER), conditionsFromItem(ModItems.POLISHER))
                .offerTo(exporter);

        // Établi de sertisseur : une table de forgeron rehaussée de deux pierres polies
        // Vitrine à pierres : trois vitres sur une pierre polie — n'importe laquelle du mod — et un
        // socle de planches. Aucun niveau de métier : c'est un meuble, et le poste du villageois.
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.GEM_SHOWCASE)
                .pattern("GGG")
                .pattern(" J ")
                .pattern("WWW")
                .input('G', Items.GLASS_PANE)
                .input('J', Ingredient.ofItems(com.nowa.item.GemPolishing.polishedGems().toArray(new Item[0])))
                .input('W', ItemTags.PLANKS)
                .criterion(hasItem(Items.GLASS_PANE), conditionsFromItem(Items.GLASS_PANE))
                .offerTo(exporter);

        // Creuset de fusion : un chaudron sur un haut fourneau, entre deux pierres polies et du fer
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.FUSION_CRUCIBLE)
                .pattern("GCG")
                .pattern("IBI")
                .input('G', ConventionalItemTags.GEMS)
                .input('C', Items.CAULDRON)
                .input('I', Items.IRON_INGOT)
                .input('B', Items.BLAST_FURNACE)
                .criterion(hasItem(Items.BLAST_FURNACE), conditionsFromItem(Items.BLAST_FURNACE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.SOCKETING_BENCH)
                .pattern("G G")
                .pattern("ITI")
                .input('G', ConventionalItemTags.GEMS)
                .input('I', Items.IRON_INGOT)
                .input('T', Items.SMITHING_TABLE)
                .criterion(hasItem(ModItems.RUBY), conditionsFromItem(ModItems.RUBY))
                .offerTo(exporter);

        // panoplies : les neuf recettes vanilla d'un lingot, pour chacun des quatre métaux
        ModGear.sets().forEach(set -> offerGear(exporter, set));
        for (ModJewelry.Metal metal : ModJewelry.Metal.values()) {
            offerJewelry(exporter, metal);
        }

        // ce qui est usé se refond : une pièce d'équipement rend une pépite, comme le fer et l'or
        offerIngotSmelting(exporter, gear(ModGear.Metal.PYRITE), ModItems.PYRITE_NUGGET, 0.1f);
        offerIngotSmelting(exporter, gear(ModGear.Metal.COBALT), ModItems.COBALT_NUGGET, 0.1f);

        // Dynamite : deux sables et deux poudres à canon, en diagonales. Le jeu accepte aussi le
        // motif retourné : peu importe laquelle des deux diagonales porte le sable.
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, Dynamite.ITEM)
                .pattern("SG")
                .pattern("GS")
                .input('S', Items.SAND)
                .input('G', Items.GUNPOWDER)
                .criterion(hasItem(Items.GUNPOWDER), conditionsFromItem(Items.GUNPOWDER))
                .offerTo(exporter);

        // Lampe frontale : la vitre entre deux fers, une pierre polie d'au moins limpide entre deux poudres
        // lumineuses (n'importe laquelle : la pureté se lit par MinPurityIngredient), et une
        // sangle de cuivre dessous. Aucun niveau de métier : c'est l'outil de n'importe quel mineur.
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, Headlamp.HEADLAMP)
                .pattern("IGI")
                .pattern("LBL")
                .pattern("CCC")
                .input('I', Items.IRON_INGOT)
                .input('G', Items.GLASS_PANE)
                .input('L', Items.GLOWSTONE_DUST)
                .input('C', Items.COPPER_INGOT)
                .input('B', MinPurityIngredient.atLeast(Purity.CLEAR))
                .criterion("has_gem", conditionsFromTag(ConventionalItemTags.GEMS))
                .offerTo(exporter);

        // Lanterne de cobalt : huit pépites autour d'une torche, comme celle du jeu
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.COBALT_LANTERN)
                .pattern("NNN")
                .pattern("NTN")
                .pattern("NNN")
                .input('N', ModItems.COBALT_NUGGET)
                .input('T', Items.TORCH)
                .criterion(hasItem(ModItems.COBALT_NUGGET), conditionsFromItem(ModItems.COBALT_NUGGET))
                .offerTo(exporter);

        // Chaîne de cobalt : un lingot entre deux pépites
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.COBALT_CHAIN)
                .pattern("N")
                .pattern("I")
                .pattern("N")
                .input('N', ModItems.COBALT_NUGGET)
                .input('I', ModItems.COBALT_INGOT)
                .criterion(hasItem(ModItems.COBALT_INGOT), conditionsFromItem(ModItems.COBALT_INGOT))
                .offerTo(exporter);
    }

    private static void offerNuggetRecipes(RecipeExporter exporter, Item nugget, Item ingot, String metal) {
        offerReversibleCompactingRecipes(exporter, RecipeCategory.MISC, nugget, RecipeCategory.MISC, ingot,
                metal + "_ingot_from_nuggets", metal + "_ingot", metal + "_nugget_from_ingot", metal + "_nugget");
    }

    private static void offerMetalPolisher(RecipeExporter exporter, Item polisher, Item ingot) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, polisher)
                .input(ModItems.POLISHER)
                .input(ingot)
                .criterion(hasItem(ingot), conditionsFromItem(ingot))
                .offerTo(exporter);
    }

    /** Les objets d'un métal, sous la forme attendue par la fonte. */
    private static List<ItemConvertible> gear(ModGear.Metal metal) {
        return List.copyOf(ModGear.of(metal).all());
    }

    /**
     * Les dispositions sont celles du jeu, et elles viennent de la pièce elle-même : c'est le même
     * motif qui dit ce qu'une pioche coûte et ce qu'on en récupère quand elle se brise.
     */
    private static void offerGear(RecipeExporter exporter, ModGear.Set set) {
        Item ingot = set.metal().ingot();
        for (ModGear.Piece piece : ModGear.Piece.values()) {
            if (set.of(piece) == null) {
                continue;                                  // ce métal n'a pas cette pièce
            }
            RecipeCategory category = piece.isArmor() || piece == ModGear.Piece.SWORD
                    ? RecipeCategory.COMBAT : RecipeCategory.TOOLS;
            ShapedRecipeJsonBuilder builder = ShapedRecipeJsonBuilder.create(category, set.of(piece));
            piece.pattern().forEach(builder::pattern);
            builder.input('X', ingot);
            if (String.join("", piece.pattern()).indexOf('#') >= 0) {
                builder.input('#', Items.STICK);
            }
            builder.criterion(hasItem(ingot), conditionsFromItem(ingot)).offerTo(exporter);
        }
    }

    /**
     * Les bijoux d'un metal. Comme pour les panoplies, la disposition vient de la forme elle-meme,
     * si bien qu'il n'y a qu'un endroit a changer pour rendre une bague plus ou moins chere.
     */
    private static void offerJewelry(RecipeExporter exporter, ModJewelry.Metal metal) {
        Item ingot = metal.ingot();
        for (ModJewelry.Kind kind : ModJewelry.Kind.values()) {
            ShapedRecipeJsonBuilder builder =
                    ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModJewelry.of(metal, kind));
            kind.pattern().forEach(builder::pattern);
            builder.input('X', ingot).criterion(hasItem(ingot), conditionsFromItem(ingot)).offerTo(exporter);
        }
    }

    // Brut -> lingot : four (200 ticks) et haut-fourneau (100 ticks), comme le fer vanilla
    private static void offerIngotSmelting(RecipeExporter exporter, Item raw, Item ingot, float experience) {
        offerIngotSmelting(exporter, List.of(raw), ingot, experience);
    }

    private static void offerIngotSmelting(RecipeExporter exporter, List<ItemConvertible> inputs, Item ingot, float experience) {
        String group = getItemPath(ingot);
        offerSmelting(exporter, inputs, RecipeCategory.MISC, ingot, experience, 200, group);
        offerBlasting(exporter, inputs, RecipeCategory.MISC, ingot, experience, 100, group);
    }
}
