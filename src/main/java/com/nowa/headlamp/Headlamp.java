package com.nowa.headlamp;

import com.nowa.config.ModConfig;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

/**
 * La lampe frontale : l'outil du mineur plutôt que du lapidaire. Elle éclaire autour de la tête et,
 * sertie, fait luire à travers la roche le minerai de ses pierres. N'importe quel joueur peut la
 * fabriquer, sans niveau de métier.
 *
 * <p>Elle a son propre paquet : le jour où un métier de mineur existera, elle en sera la première
 * pièce.
 */
public final class Headlamp {
    /** Logements de la lampe : un seul depuis la refonte du 2026-10-14 — une pierre, un minerai senti. */
    public static final int SOCKETS = 1;

    /** Une armure qui ne protège de rien : la lampe se porte en casque, sans en être un. */
    public static final RegistryEntry<ArmorMaterial> MATERIAL = registerMaterial();

    public static final Item HEADLAMP = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "headlamp"),
            new HeadlampItem(MATERIAL, new Item.Settings().maxCount(1)));

    /**
     * La lumière posée à la hauteur de la tête. Sa force est lue au démarrage : le jeu fige la lumière d'un
     * bloc à son enregistrement, la changer demande de relancer.
     */
    public static final Block LIGHT = Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "headlamp_light"),
            new HeadlampLightBlock(AbstractBlock.Settings.create()
                    .replaceable()
                    .noCollision()
                    .dropsNothing()
                    .nonOpaque()
                    .noBlockBreakParticles()
                    .strength(-1.0f, 3600000.8f)
                    .pistonBehavior(PistonBehavior.DESTROY)
                    .luminance(state -> Math.clamp(ModConfig.i("headlamp.light.level", 15), 0, 15))));

    private Headlamp() {
    }

    public static void register() {
        LOGGER.info("├─ Registering Headlamp for " + MOD_ID);
        HeadlampNetworking.register();
        GemSense.register();
        LanternSight.register();
        HeadlampLight.register();
    }

    private static RegistryEntry<ArmorMaterial> registerMaterial() {
        Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            defense.put(type, 0);
        }
        ArmorMaterial material = new ArmorMaterial(defense, 0, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN,
                () -> Ingredient.ofItems(Items.COPPER_INGOT),
                List.of(new ArmorMaterial.Layer(Identifier.of(MOD_ID, "headlamp"))), 0.0f, 0.0f);
        return Registry.registerReference(Registries.ARMOR_MATERIAL, Identifier.of(MOD_ID, "headlamp"), material);
    }
}
