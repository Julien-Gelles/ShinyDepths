package com.nowa.item;

import com.nowa.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Compactage des pierres polies : 9 pierres d'une même classe de pureté donnent un bloc de cette classe,
 * et le bloc redonne ses 9 pierres. Des recettes spéciales, car une recette classique ne saurait pas
 * exiger la même pureté partout ni la reporter sur le bloc.
 */
public final class PurityBlockRecipes {
    public static final RecipeSerializer<Compacting> COMPACTING = Registry.register(Registries.RECIPE_SERIALIZER,
            Identifier.of(MOD_ID, "gem_block_compacting"), new SpecialRecipeSerializer<>(Compacting::new));
    public static final RecipeSerializer<Uncompacting> UNCOMPACTING = Registry.register(Registries.RECIPE_SERIALIZER,
            Identifier.of(MOD_ID, "gem_block_uncompacting"), new SpecialRecipeSerializer<>(Uncompacting::new));

    /**
     * Le diamant et l'émeraude du jeu suivent la même règle, avec les blocs du jeu. Leurs deux
     * recettes d'origine sont remplacées par celles-ci (data/minecraft/recipe) ; la pureté d'un
     * bloc posé est tenue par {@code GemBlockPurity}.
     */
    private static final Map<Item, Block> VANILLA_BLOCKS = Map.of(
            Items.DIAMOND, Blocks.DIAMOND_BLOCK, Items.EMERALD, Blocks.EMERALD_BLOCK);

    private static Map<Item, Item> blockToGem;

    private PurityBlockRecipes() {
    }

    /** Les sérialiseurs sont enregistrés au chargement de la classe ; cet appel le déclenche. */
    public static void register() {
    }

    /** Objet du bloc de pierres → pierre polie correspondante. */
    @Nullable
    private static Item gemOf(Item blockItem) {
        if (blockToGem == null) {
            blockToGem = new HashMap<>();
            ModBlocks.gemBlocks().forEach((gem, block) -> blockToGem.put(block.asItem(), gem));
            VANILLA_BLOCKS.forEach((gem, block) -> blockToGem.put(block.asItem(), gem));
        }
        return blockToGem.get(blockItem);
    }

    @Nullable
    private static Block blockOf(Item gem) {
        Block block = ModBlocks.gemBlocks().get(gem);
        return block != null ? block : VANILLA_BLOCKS.get(gem);
    }

    /** Les piles non vides de la grille. */
    private static List<ItemStack> stacksOf(CraftingRecipeInput input) {
        return input.getStacks().stream().filter(stack -> !stack.isEmpty()).toList();
    }

    /** 9 pierres identiques (même pierre, même pureté) → 1 bloc de cette pureté. */
    public static class Compacting extends SpecialCraftingRecipe {
        public Compacting(CraftingRecipeCategory category) {
            super(category);
        }

        @Override
        public boolean matches(CraftingRecipeInput input, World world) {
            List<ItemStack> stacks = stacksOf(input);
            if (stacks.size() != 9 || blockOf(stacks.getFirst().getItem()) == null) {
                return false;
            }
            return stacks.stream().allMatch(stack -> ItemStack.areItemsAndComponentsEqual(stack, stacks.getFirst()));
        }

        @Override
        public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
            List<ItemStack> stacks = stacksOf(input);
            Block block = blockOf(stacks.getFirst().getItem());
            if (block == null) {
                return ItemStack.EMPTY;
            }
            ItemStack result = new ItemStack(block);
            Purity purity = stacks.getFirst().get(ModDataComponents.PURITY);
            if (purity != null) {
                result.set(ModDataComponents.PURITY, purity);
                GemPolishing.applyGlint(result);
            }
            return result;
        }

        @Override
        public boolean fits(int width, int height) {
            return width >= 3 && height >= 3;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return COMPACTING;
        }
    }

    /** 1 bloc de pierres → 9 pierres de sa pureté. */
    public static class Uncompacting extends SpecialCraftingRecipe {
        public Uncompacting(CraftingRecipeCategory category) {
            super(category);
        }

        @Override
        public boolean matches(CraftingRecipeInput input, World world) {
            List<ItemStack> stacks = stacksOf(input);
            return stacks.size() == 1 && stacks.getFirst().getItem() instanceof BlockItem blockItem
                    && gemOf(blockItem) != null;
        }

        @Override
        public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
            ItemStack block = stacksOf(input).getFirst();
            Item gem = gemOf(block.getItem());
            if (gem == null) {
                return ItemStack.EMPTY;
            }
            Purity purity = block.get(ModDataComponents.PURITY);
            ItemStack result = purity == null ? new ItemStack(gem) : GemPolishing.polishedStack(gem, purity);
            result.setCount(9);
            return result;
        }

        @Override
        public boolean fits(int width, int height) {
            return width * height >= 1;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return UNCOMPACTING;
        }
    }
}
