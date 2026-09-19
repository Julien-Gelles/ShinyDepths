package com.nowa;

import com.nowa.block.ModBlocks;
import com.nowa.screen.LapidaryTableScreen;
import com.nowa.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;

public class ShinyDepthsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(){
        HandledScreens.register(ModScreenHandlers.LAPIDARY_TABLE, LapidaryTableScreen::new);
        // la scie a des parties transparentes, comme celle du tailleur de pierre
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
                ModBlocks.IRON_LAPIDARY_TABLE, ModBlocks.DIAMOND_LAPIDARY_TABLE, ModBlocks.NETHERITE_LAPIDARY_TABLE);
    }
}
