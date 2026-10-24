package com.nowa.item;

import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
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

import static com.nowa.NowasDepths.MOD_ID;

/** Sachet à pierres + colorant = sachet de cette couleur, contenu conservé (comme les bundles colorés). */
public class PouchDyeRecipe extends SpecialCraftingRecipe {
    public static final RecipeSerializer<PouchDyeRecipe> SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER,
            Identifier.of(MOD_ID, "stone_pouch_dyeing"), new SpecialRecipeSerializer<>(PouchDyeRecipe::new));

    public PouchDyeRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    /** Le sachet et le colorant de la grille, ou null si ce n'est pas exactement un de chaque. */
    private static ItemStack[] find(CraftingRecipeInput input) {
        ItemStack pouch = ItemStack.EMPTY;
        ItemStack dye = ItemStack.EMPTY;
        for (ItemStack stack : input.getStacks()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof StonePouchItem && pouch.isEmpty()) {
                pouch = stack;
            } else if (stack.getItem() instanceof DyeItem && dye.isEmpty()) {
                dye = stack;
            } else {
                return null;
            }
        }
        return pouch.isEmpty() || dye.isEmpty() ? null : new ItemStack[]{pouch, dye};
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        ItemStack[] found = find(input);
        return found != null && ((StonePouchItem) found[0].getItem()).getColor() != ((DyeItem) found[1].getItem()).getColor();
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack[] found = find(input);
        if (found == null) {
            return ItemStack.EMPTY;
        }
        return found[0].copyComponentsToNewStack(ModItems.dyedStonePouch(((DyeItem) found[1].getItem()).getColor()), 1);
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static void register() {
        // l'enregistrement du sérialiseur se fait au chargement de la classe
    }
}
