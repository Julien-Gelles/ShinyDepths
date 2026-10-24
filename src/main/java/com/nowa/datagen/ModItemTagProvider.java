package com.nowa.datagen;

import com.nowa.item.ModGear;
import com.nowa.headlamp.Headlamp;
import com.nowa.item.ModJewelry;
import com.nowa.item.ModItemTags;
import com.nowa.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        // Comme le pinceau : livres Solidité et Raccommodage applicables à l'enclume
        getOrCreateTagBuilder(ItemTags.DURABILITY_ENCHANTABLE)
                .add(ModItems.POLISHER);

        // Tags communs « c: » : nos matériaux sont reconnus par les autres mods (et par le sachet)
        getOrCreateTagBuilder(ConventionalItemTags.GEMS)
                .add(ModItems.RUBY, ModItems.MOONSTONE, ModItems.ONYX, ModItems.GARNET, ModItems.AMBER, ModItems.PERIDOT,
                        ModItems.JADE, ModItems.BLUE_TOPAZ, ModItems.TANZANITE, ModItems.ROSE_QUARTZ, ModItems.SUGILITE,
                        ModItems.TOURMALINE, ModItems.SAPPHIRE);
        getOrCreateTagBuilder(ConventionalItemTags.RAW_MATERIALS)
                .add(ModItems.RAW_PLATINUM, ModItems.RAW_PYRITE, ModItems.RAW_COBALT, ModItems.RAW_TITANIUM,
                        ModItems.RAW_RUBY, ModItems.RAW_MOONSTONE, ModItems.RAW_ONYX, ModItems.RAW_GARNET, ModItems.RAW_AMBER,
                        ModItems.RAW_PERIDOT, ModItems.RAW_JADE, ModItems.RAW_BLUE_TOPAZ, ModItems.RAW_TANZANITE,
                        ModItems.RAW_ROSE_QUARTZ, ModItems.RAW_SUGILITE, ModItems.RAW_TOURMALINE, ModItems.RAW_SAPPHIRE,
                        ModItems.RAW_DIAMOND, ModItems.RAW_EMERALD);
        getOrCreateTagBuilder(ConventionalItemTags.NUGGETS)
                .add(ModItems.PYRITE_NUGGET, ModItems.COBALT_NUGGET);
        getOrCreateTagBuilder(ConventionalItemTags.INGOTS)
                .add(ModItems.PLATINUM_INGOT, ModItems.PYRITE_INGOT, ModItems.COBALT_INGOT, ModItems.TITANIUM_INGOT);

        // ---------- panoplies du mod ----------
        // Tout découle de ces neuf tags : enchantements, garnitures, solidité et disparition
        // sont eux-mêmes définis à partir d'eux en vanilla, on n'a donc rien d'autre à déclarer.
        ModGear.sets().forEach(set -> {
            getOrCreateTagBuilder(ItemTags.SWORDS).add(set.sword());
            getOrCreateTagBuilder(ItemTags.PICKAXES).add(set.pickaxe());
            getOrCreateTagBuilder(ItemTags.AXES).add(set.axe());
            getOrCreateTagBuilder(ItemTags.SHOVELS).add(set.shovel());
            getOrCreateTagBuilder(ItemTags.HOES).add(set.hoe());
            if (set.metal().hasArmor()) {
                getOrCreateTagBuilder(ItemTags.HEAD_ARMOR).add(set.helmet());
                getOrCreateTagBuilder(ItemTags.CHEST_ARMOR).add(set.chestplate());
                getOrCreateTagBuilder(ItemTags.LEG_ARMOR).add(set.leggings());
                getOrCreateTagBuilder(ItemTags.FOOT_ARMOR).add(set.boots());
            }
            // tags conventionnels : les autres mods reconnaissent nos outils comme les leurs
            getOrCreateTagBuilder(ConventionalItemTags.MELEE_WEAPON_TOOLS).add(set.sword(), set.axe());
            getOrCreateTagBuilder(ConventionalItemTags.MINING_TOOL_TOOLS)
                    .add(set.pickaxe(), set.axe(), set.shovel(), set.hoe());
        });

        // ---------- lampe frontale ----------
        // sa propre famille : ses pierres ne donnent pas d'attributs, elles sentent leur minerai
        getOrCreateTagBuilder(ModItemTags.SOCKETABLE_HEADLAMP).add(Headlamp.HEADLAMP);
        getOrCreateTagBuilder(ModItemTags.SOCKETS[Headlamp.SOCKETS]).add(Headlamp.HEADLAMP);

        // la bague : sa forme, sa famille, et une seule pierre
        var rings = getOrCreateTagBuilder(ModItemTags.RINGS);
        var socketable = getOrCreateTagBuilder(ModItemTags.SOCKETABLE_JEWELRY);
        var oneSocket = getOrCreateTagBuilder(ModItemTags.SOCKETS[1]);
        ModJewelry.allOf(ModJewelry.Kind.RING).forEach(ring -> {
            rings.add(ring);
            socketable.add(ring);
            oneSocket.add(ring);
        });

        // Sachet à pierres : pierres, minerais, lingots, pépites et poudres (du mod, de Minecraft et des autres mods)
        getOrCreateTagBuilder(ModItemTags.POUCH_STORABLE)
                .addOptionalTag(ConventionalItemTags.GEMS)
                .addOptionalTag(ConventionalItemTags.RAW_MATERIALS)
                .addOptionalTag(ConventionalItemTags.INGOTS)
                .addOptionalTag(ConventionalItemTags.NUGGETS)
                .addOptionalTag(ConventionalItemTags.DUSTS)
                .addOptionalTag(ConventionalItemTags.ORES)
                .add(Items.COAL, Items.CHARCOAL, Items.FLINT, Items.NETHERITE_SCRAP, Items.ECHO_SHARD);

        // ce que la dynamite épargne au sol : le butin d'un filon, et ce qui le contient
        var proof = getOrCreateTagBuilder(ModItemTags.DYNAMITE_PROOF)
                .addTag(ModItemTags.POUCH_STORABLE)
                .add(ModItems.GEODE, ModItems.STONE_POUCH);
        ModItems.DYED_STONE_POUCHES.values().forEach(proof::add);
    }
}
