package com.nowa.item;

import com.nowa.config.ModConfig;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Les trois armures du mod : cobalt, titane, platine. La pyrite n'en a pas. La protection se lit dans l'ordre bottes, jambières, plastron, casque,
 * comme dans le jeu ; la texture portée est la recoloration de celle du fer, sous le nom du métal.
 */
public final class ModArmorMaterials {
    public static final RegistryEntry<ArmorMaterial> COBALT = register("cobalt", 2, 5, 6, 2,
            16, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 1.0f, 0.0f, () -> ModItems.COBALT_INGOT);
    public static final RegistryEntry<ArmorMaterial> TITANIUM = register("titanium", 3, 6, 7, 3,
            12, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 2.0f, 0.0f, () -> ModItems.TITANIUM_INGOT);
    public static final RegistryEntry<ArmorMaterial> PLATINUM = register("platinum", 3, 6, 8, 3,
            20, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, 2.5f, 0.05f, () -> ModItems.PLATINUM_INGOT);

    private ModArmorMaterials() {
    }

    private static RegistryEntry<ArmorMaterial> register(String name, int boots, int leggings, int chestplate,
                                                         int helmet, int enchantability,
                                                         RegistryEntry<SoundEvent> equipSound, float toughness,
                                                         float knockbackResistance, Supplier<Item> repairIngredient) {
        String key = "gear." + name + ".armor.";
        Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, ModConfig.i(key + "boots.protection", boots));
        defense.put(ArmorItem.Type.LEGGINGS, ModConfig.i(key + "leggings.protection", leggings));
        defense.put(ArmorItem.Type.CHESTPLATE, ModConfig.i(key + "chestplate.protection", chestplate));
        defense.put(ArmorItem.Type.HELMET, ModConfig.i(key + "helmet.protection", helmet));
        defense.put(ArmorItem.Type.BODY, ModConfig.i(key + "body.protection", chestplate));   // armure de loup
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(Identifier.of(MOD_ID, name)));
        ArmorMaterial material = new ArmorMaterial(defense,
                ModConfig.i(key + "enchantability", enchantability), equipSound,
                () -> Ingredient.ofItems(repairIngredient.get()), layers,
                ModConfig.f(key + "toughness", toughness),
                ModConfig.f(key + "knockback_resistance", knockbackResistance));
        return Registry.registerReference(Registries.ARMOR_MATERIAL, Identifier.of(MOD_ID, name), material);
    }
}
