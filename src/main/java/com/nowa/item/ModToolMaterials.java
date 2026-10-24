package com.nowa.item;

import com.nowa.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Lazy;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Les quatre métaux du mod en tant que matériaux d'outils, du plus modeste au plus rare.
 *
 * <p>Chacun a un caractère plutôt qu'un rang : la pyrite est l'or des fous, tendre mais avide
 * d'enchantements ; le cobalt est rapide ; le titane
 * tient entre le fer et la netherite ; le platine dure plus longtemps que tout le reste.
 *
 * <p>L'ingrédient de réparation est différé : les matériaux se chargent avant les objets.
 */
public enum ModToolMaterials implements ToolMaterial {
    PYRITE("stone", 190, 8.0f, 1.5f, 22, () -> ModItems.PYRITE_INGOT),
    COBALT("iron", 850, 9.0f, 2.5f, 16, () -> ModItems.COBALT_INGOT),
    TITANIUM("diamond", 1600, 8.0f, 3.0f, 12, () -> ModItems.TITANIUM_INGOT),
    PLATINUM("diamond", 2400, 9.5f, 3.5f, 20, () -> ModItems.PLATINUM_INGOT);

    /**
     * Les paliers de minage : le tag dit ce que l'outil ne peut PAS extraire. La table vit dans une
     * classe à part, un constructeur d'enum n'ayant pas le droit de lire un champ statique du sien.
     */
    private static final class MiningLevels {
        private static final Map<String, TagKey<Block>> BY_NAME = Map.of(
                "wood", BlockTags.INCORRECT_FOR_WOODEN_TOOL,
                "gold", BlockTags.INCORRECT_FOR_GOLD_TOOL,
                "stone", BlockTags.INCORRECT_FOR_STONE_TOOL,
                "iron", BlockTags.INCORRECT_FOR_IRON_TOOL,
                "diamond", BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
                "netherite", BlockTags.INCORRECT_FOR_NETHERITE_TOOL);
    }

    private final TagKey<Block> inverseTag;
    private final int durability;
    private final float miningSpeed;
    private final float attackDamage;
    private final int enchantability;
    private final Lazy<Ingredient> repairIngredient;

    ModToolMaterials(String miningLevel, int durability, float miningSpeed, float attackDamage,
                     int enchantability, Supplier<net.minecraft.item.Item> repairIngredient) {
        String key = "gear." + name().toLowerCase(Locale.ROOT) + ".tool.";
        String level = ModConfig.text(key + "mining_level", miningLevel);
        this.inverseTag = MiningLevels.BY_NAME.getOrDefault(level, MiningLevels.BY_NAME.get(miningLevel));
        this.durability = ModConfig.i(key + "durability", durability);
        this.miningSpeed = ModConfig.f(key + "mining_speed", miningSpeed);
        this.attackDamage = ModConfig.f(key + "attack_damage", attackDamage);
        this.enchantability = ModConfig.i(key + "enchantability", enchantability);
        this.repairIngredient = new Lazy<>(() -> Ingredient.ofItems(repairIngredient.get()));
    }

    @Override
    public int getDurability() {
        return durability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return miningSpeed;
    }

    @Override
    public float getAttackDamage() {
        return attackDamage;
    }

    @Override
    public TagKey<Block> getInverseTag() {
        return inverseTag;
    }

    @Override
    public int getEnchantability() {
        return enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }
}
