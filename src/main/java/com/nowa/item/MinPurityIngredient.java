package com.nowa.item;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Un ingrédient de recette : n'importe quelle pierre polie — du mod, ou le diamant et l'émeraude
 * polis —, pourvu qu'elle soit d'au moins cette pureté. Une recette du jeu ne sait pas lire la
 * pureté ; celle-ci reste une recette ordinaire, visible au livre de recettes, qui y fait défiler
 * les pierres acceptées.
 *
 * <p>Dans un fichier de recette : {@code {"fabric:type": "shiny-depths:min_purity", "min_purity": "clear"}}.
 */
public record MinPurityIngredient(Purity min) implements CustomIngredient {
    public static final Serializer SERIALIZER = new Serializer();

    /** L'ingrédient, tel qu'une recette le prend. */
    public static Ingredient atLeast(Purity min) {
        return new MinPurityIngredient(min).toVanilla();
    }

    public static void register() {
        CustomIngredientSerializer.register(SERIALIZER);
    }

    @Override
    public boolean test(ItemStack stack) {
        Purity purity = stack.get(ModDataComponents.PURITY);
        return GemPolishing.isPolished(stack.getItem()) && purity != null && purity.ordinal() >= min.ordinal();
    }

    /** Ce que le livre de recettes fait défiler : chaque pierre polie, à chaque pureté acceptée. */
    @Override
    public List<ItemStack> getMatchingStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (Item gem : GemPolishing.polishedGems()) {
            for (Purity purity : Purity.values()) {
                if (purity.ordinal() >= min.ordinal()) {
                    stacks.add(GemPolishing.polishedStack(gem, purity));
                }
            }
        }
        return stacks;
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static final class Serializer implements CustomIngredientSerializer<MinPurityIngredient> {
        private static final Identifier ID = Identifier.of(MOD_ID, "min_purity");
        private static final MapCodec<MinPurityIngredient> CODEC =
                Purity.CODEC.fieldOf("min_purity").xmap(MinPurityIngredient::new, MinPurityIngredient::min);
        private static final PacketCodec<RegistryByteBuf, MinPurityIngredient> PACKET_CODEC =
                Purity.PACKET_CODEC.<RegistryByteBuf>cast().xmap(MinPurityIngredient::new, MinPurityIngredient::min);

        @Override
        public Identifier getIdentifier() {
            return ID;
        }

        @Override
        public MapCodec<MinPurityIngredient> getCodec(boolean allowEmpty) {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, MinPurityIngredient> getPacketCodec() {
            return PACKET_CODEC;
        }
    }
}
