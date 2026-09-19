package com.nowa.datagen;

import com.nowa.block.ModBlocks;
import com.nowa.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

import static com.nowa.ShinyDepths.MOD_ID;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        offerReversibleCompactingRecipes(exporter, RecipeCategory.MISC, ModItems.RUBY, RecipeCategory.BUILDING_BLOCKS, ModBlocks.RUBY_BLOCK);

        offerIngotSmelting(exporter, ModItems.RAW_PLATINUM, ModItems.PLATINUM_INGOT, 1.5f);
        offerIngotSmelting(exporter, ModItems.RAW_PYRITE, ModItems.PYRITE_INGOT, 0.7f);
        offerIngotSmelting(exporter, ModItems.RAW_COBALT, ModItems.COBALT_INGOT, 1.0f);
        offerIngotSmelting(exporter, ModItems.RAW_TITANIUM, ModItems.TITANIUM_INGOT, 1.2f);
        offerIngotSmelting(exporter, ModItems.RAW_BISMUTH, ModItems.BISMUTH_INGOT, 0.8f);

        // Polissoir : un silex au-dessus d'un bâton
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.POLISHER)
                .pattern("F")
                .pattern("S")
                .input('F', Items.FLINT)
                .input('S', Items.STICK)
                .criterion(hasItem(Items.FLINT), conditionsFromItem(Items.FLINT))
                .offerTo(exporter);

        // Table de lapidaire en fer : lingots de fer, polissoir et planches
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.IRON_LAPIDARY_TABLE)
                .pattern("III")
                .pattern("#P#")
                .pattern("###")
                .input('I', Items.IRON_INGOT)
                .input('P', ModItems.POLISHER)
                .input('#', ItemTags.PLANKS)
                .criterion(hasItem(ModItems.POLISHER), conditionsFromItem(ModItems.POLISHER))
                .offerTo(exporter);

        // Table en diamant : la table en fer entourée de 4 diamants
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.DIAMOND_LAPIDARY_TABLE)
                .pattern(" D ")
                .pattern("DTD")
                .pattern(" D ")
                .input('D', Items.DIAMOND)
                .input('T', ModBlocks.IRON_LAPIDARY_TABLE)
                .criterion(hasItem(ModBlocks.IRON_LAPIDARY_TABLE), conditionsFromItem(ModBlocks.IRON_LAPIDARY_TABLE))
                .offerTo(exporter);

        // Table en netherite : table de forgeron, comme les outils en netherite
        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.ofItems(ModBlocks.DIAMOND_LAPIDARY_TABLE),
                        Ingredient.ofItems(Items.NETHERITE_INGOT),
                        RecipeCategory.DECORATIONS, ModBlocks.NETHERITE_LAPIDARY_TABLE.asItem())
                .criterion(hasItem(Items.NETHERITE_INGOT), conditionsFromItem(Items.NETHERITE_INGOT))
                .offerTo(exporter, Identifier.of(MOD_ID, "netherite_lapidary_table_smithing"));
    }

    // Brut -> lingot : four (200 ticks) et haut-fourneau (100 ticks), comme le fer vanilla
    private static void offerIngotSmelting(RecipeExporter exporter, Item raw, Item ingot, float experience) {
        String group = getItemPath(ingot);
        offerSmelting(exporter, List.of(raw), RecipeCategory.MISC, ingot, experience, 200, group);
        offerBlasting(exporter, List.of(raw), RecipeCategory.MISC, ingot, experience, 100, group);
    }
}
