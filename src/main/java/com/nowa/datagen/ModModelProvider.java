package com.nowa.datagen;

import com.nowa.block.ModBlocks;
import com.nowa.block.ModOres;
import com.nowa.headlamp.Headlamp;
import com.nowa.item.ModGear;
import com.nowa.item.ModJewelry;
import com.nowa.item.ModItems;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureMap;
import net.minecraft.util.Identifier;

import java.util.Optional;

import static com.nowa.ShinyDepths.MOD_ID;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        // tous les blocs de rangement : un simple cube
        ModBlocks.gemBlocks().values().forEach(blockStateModelGenerator::registerSimpleCubeAll);
        ModBlocks.compactBlocks().values().forEach(blockStateModelGenerator::registerSimpleCubeAll);
        ModOres.ores().forEach(ore -> blockStateModelGenerator.registerSimpleCubeAll(ore.block()));

        // la lumière de la lampe frontale ne se dessine pas : le modèle vide du jeu, celui de l'air
        blockStateModelGenerator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(
                Headlamp.LIGHT, Identifier.ofVanilla("block/air")));

        // ferronnerie de cobalt : la lanterne suit le gabarit du jeu, la chaîne a son modèle écrit à la main
        blockStateModelGenerator.registerLantern(ModBlocks.COBALT_LANTERN);
        blockStateModelGenerator.registerAxisRotated(ModBlocks.COBALT_CHAIN,
                ModelIds.getBlockModelId(ModBlocks.COBALT_CHAIN));
    }

    /** La pastille de couleur que porte un sachet d'une seule sorte de pierre. */
    private static final Identifier POUCH_GEM = Identifier.of(MOD_ID, "item/stone_pouch_gem");

    /**
     * Deux modèles par sachet : le sachet nu, et le même avec sa pastille. Une propriété du modèle
     * bascule de l'un à l'autre, {@code shiny-depths:gem}, que le client met à 1 quand le sachet ne
     * contient qu'une sorte de pierre.
     */
    private static void registerPouch(ItemModelGenerator generator, Item pouch) {
        Identifier withGem = Models.GENERATED_TWO_LAYERS.upload(
                ModelIds.getItemModelId(pouch).withSuffixedPath("_gem"),
                TextureMap.layered(TextureMap.getId(pouch), POUCH_GEM), generator.writer);
        Models.GENERATED.upload(ModelIds.getItemModelId(pouch), TextureMap.layer0(pouch), generator.writer,
                (id, textures) -> {
                    JsonObject json = Models.GENERATED.createJson(id, textures);
                    JsonObject predicate = new JsonObject();
                    predicate.addProperty(MOD_ID + ":gem", 1);
                    JsonObject override = new JsonObject();
                    override.add("predicate", predicate);
                    override.addProperty("model", withGem.toString());
                    JsonArray overrides = new JsonArray();
                    overrides.add(override);
                    json.add("overrides", overrides);
                    return json;
                });
    }

    /**
     * Deux modeles par bijou : le bijou nu, et le meme avec sa pierre. La propriete
     * {@code shiny-depths:gem} bascule de l'un a l'autre des qu'un logement est occupe, et la
     * couleur de la pierre vient teinter la deuxieme couche — c'est exactement le tour du sachet.
     */
    private static void registerJewel(ItemModelGenerator generator, Item jewel, ModJewelry.Kind kind) {
        Identifier stone = Identifier.of(MOD_ID, "item/" + kind.kindName() + "_stone");
        Identifier withStone = Models.GENERATED_TWO_LAYERS.upload(
                ModelIds.getItemModelId(jewel).withSuffixedPath("_gem"),
                TextureMap.layered(TextureMap.getId(jewel), stone), generator.writer);
        Models.GENERATED.upload(ModelIds.getItemModelId(jewel), TextureMap.layer0(jewel), generator.writer,
                (id, textures) -> {
                    JsonObject json = Models.GENERATED.createJson(id, textures);
                    JsonObject predicate = new JsonObject();
                    predicate.addProperty(MOD_ID + ":gem", 1);
                    JsonObject override = new JsonObject();
                    override.add("predicate", predicate);
                    override.addProperty("model", withStone.toString());
                    JsonArray overrides = new JsonArray();
                    overrides.add(override);
                    json.add("overrides", overrides);
                    return json;
                });
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItems.RUBY, Models.GENERATED);
        itemModelGenerator.register(Headlamp.HEADLAMP, Models.GENERATED);
        // la dynamite est un modèle en volume, écrit à la main dans resources
        itemModelGenerator.register(ModItems.GEODE, Models.GENERATED);
        itemModelGenerator.register(ModItems.LOUPE, Models.GENERATED);
        // polisher.json est écrit à la main : il reprend les transformations du pinceau vanilla
        registerPouch(itemModelGenerator, ModItems.STONE_POUCH);
        ModItems.DYED_STONE_POUCHES.values().forEach(pouch -> registerPouch(itemModelGenerator, pouch));

        itemModelGenerator.register(ModItems.RAW_PLATINUM, Models.GENERATED);
        itemModelGenerator.register(ModItems.PLATINUM_INGOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_PYRITE, Models.GENERATED);
        itemModelGenerator.register(ModItems.PYRITE_INGOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_COBALT, Models.GENERATED);
        itemModelGenerator.register(ModItems.COBALT_INGOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_TITANIUM, Models.GENERATED);
        itemModelGenerator.register(ModItems.TITANIUM_INGOT, Models.GENERATED);

        itemModelGenerator.register(ModItems.MOONSTONE, Models.GENERATED);
        itemModelGenerator.register(ModItems.ONYX, Models.GENERATED);
        itemModelGenerator.register(ModItems.GARNET, Models.GENERATED);
        itemModelGenerator.register(ModItems.AMBER, Models.GENERATED);
        itemModelGenerator.register(ModItems.PERIDOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.JADE, Models.GENERATED);
        itemModelGenerator.register(ModItems.BLUE_TOPAZ, Models.GENERATED);
        itemModelGenerator.register(ModItems.TANZANITE, Models.GENERATED);
        itemModelGenerator.register(ModItems.ROSE_QUARTZ, Models.GENERATED);
        itemModelGenerator.register(ModItems.SUGILITE, Models.GENERATED);
        itemModelGenerator.register(ModItems.TOURMALINE, Models.GENERATED);
        itemModelGenerator.register(ModItems.SAPPHIRE, Models.GENERATED);

        itemModelGenerator.register(ModItems.RAW_RUBY, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_MOONSTONE, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_DIAMOND, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_EMERALD, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_ONYX, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_GARNET, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_AMBER, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_PERIDOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_JADE, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_BLUE_TOPAZ, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_TANZANITE, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_ROSE_QUARTZ, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_SUGILITE, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_TOURMALINE, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_SAPPHIRE, Models.GENERATED);
        itemModelGenerator.register(ModItems.PYRITE_NUGGET, Models.GENERATED);
        itemModelGenerator.register(ModItems.COBALT_NUGGET, Models.GENERATED);
        // la chaîne se tient à plat en inventaire, avec sa propre texture d'objet
        itemModelGenerator.register(ModBlocks.COBALT_CHAIN.asItem(), Models.GENERATED);

        // panoplies : outils tenus en main, armure à plat avec ses variantes de garniture
        ModGear.sets().forEach(set -> {
            set.tools().forEach(tool -> itemModelGenerator.register(tool, Models.HANDHELD));
            set.armor().forEach(piece -> itemModelGenerator.registerArmor((ArmorItem) piece));
        });
        for (ModJewelry.Kind kind : ModJewelry.Kind.values()) {
            ModJewelry.allOf(kind).forEach(jewel -> registerJewel(itemModelGenerator, jewel, kind));
        }
    }
}
