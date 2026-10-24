package com.nowa;

import com.nowa.block.ModBlocks;
import com.nowa.item.GemColors;
import com.nowa.item.ModItems;
import com.nowa.item.StonePouchItem;
import com.nowa.lapidary.client.LapidaryClient;
import com.nowa.screen.LapidaryTableScreen;
import com.nowa.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;

public class NowasDepthsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(){
        HandledScreens.register(ModScreenHandlers.LAPIDARY_TABLE, LapidaryTableScreen::new);
        net.minecraft.client.render.block.entity.BlockEntityRendererFactories.register(
                com.nowa.block.entity.ModBlockEntities.LAPIDARY_TABLE,
                com.nowa.block.client.LapidaryTableRenderer::new);
        HandledScreens.register(ModScreenHandlers.SOCKETING_BENCH, com.nowa.screen.SocketingBenchScreen::new);

        net.minecraft.client.render.block.entity.BlockEntityRendererFactories.register(
                com.nowa.block.entity.ModBlockEntities.SOCKETING_BENCH,
                com.nowa.block.client.SocketingBenchRenderer::new);
        HandledScreens.register(ModScreenHandlers.FUSION_CRUCIBLE, com.nowa.screen.FusionCrucibleScreen::new);
        net.minecraft.client.render.block.entity.BlockEntityRendererFactories.register(
                com.nowa.block.entity.ModBlockEntities.FUSION_CRUCIBLE,
                com.nowa.block.client.FusionCrucibleRenderer::new);
        HandledScreens.register(ModScreenHandlers.GEM_SHOWCASE, com.nowa.screen.GemShowcaseScreen::new);
        net.minecraft.client.render.block.entity.BlockEntityRendererFactories.register(
                com.nowa.block.entity.ModBlockEntities.GEM_SHOWCASE,
                com.nowa.block.client.GemShowcaseRenderer::new);
        // la scie a des parties transparentes, comme celle du tailleur de pierre ; la lanterne aussi.
        // Sans cette déclaration, un bloc est peint comme opaque et ses trous virent au noir.
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
                ModBlocks.NETHERITE_LAPIDARY_TABLE,
                ModBlocks.COBALT_LANTERN, ModBlocks.GEM_SHOWCASE);
        // la chaîne est en couche adoucie, comme celle du jeu et comme les barreaux de fer
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.COBALT_CHAIN, RenderLayer.getCutoutMipped());
        registerPouchColors();
        registerJewelryColors();
        com.nowa.message.client.ModMessagesClient.register();
        com.nowa.socket.SocketTooltip.register();
        com.nowa.powers.client.PowersClient.register();
        LapidaryClient.register();
        com.nowa.dynamite.client.DynamiteClient.register();
        com.nowa.headlamp.client.GemSenseRenderer.register();
    }

    /**
     * Un bijou serti montre sa pierre : le modele passe a sa version a deux couches des qu'un
     * logement est occupe, et la deuxieme couche prend la couleur de la premiere pierre posee. Une
     * bague de rubis se reconnait donc d'un coup d'oeil, sans lire l'infobulle.
     */
    private static void registerJewelryColors() {
        List<Item> jewels = com.nowa.item.ModJewelry.all();
        Identifier gem = Identifier.of(NowasDepths.MOD_ID, "gem");
        for (Item jewel : jewels) {
            ModelPredicateProviderRegistry.register(jewel, gem,
                    (stack, world, entity, seed) -> com.nowa.socket.Socketing.get(stack).isEmpty() ? 0.0f : 1.0f);
        }
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (tintIndex != 1) {
                return -1;
            }
            net.minecraft.item.ItemStack stone = com.nowa.socket.Socketing.get(stack).first();
            return stone.isEmpty() ? -1 : GemColors.of(stone.getItem());
        }, jewels.toArray(new ItemConvertible[0]));
    }

    /**
     * Un sachet qui ne contient qu'une sorte de pierre porte une pastille de sa couleur. Le modèle
     * la montre selon la propriété {@code gem}, et la teinte vient du nuancier.
     */
    private static void registerPouchColors() {
        List<Item> pouches = new ArrayList<>();
        pouches.add(ModItems.STONE_POUCH);
        pouches.addAll(ModItems.DYED_STONE_POUCHES.values());
        Identifier gem = Identifier.of(NowasDepths.MOD_ID, "gem");
        for (Item pouch : pouches) {
            ModelPredicateProviderRegistry.register(pouch, gem,
                    (stack, world, entity, seed) -> StonePouchItem.singleGem(stack) == null ? 0.0f : 1.0f);
        }
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            Item stone = tintIndex == 1 ? StonePouchItem.singleGem(stack) : null;
            return stone == null ? -1 : GemColors.of(stone);
        }, pouches.toArray(new ItemConvertible[0]));
    }
}
