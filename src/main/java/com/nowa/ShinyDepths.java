package com.nowa;

import com.nowa.block.ModBlocks;
import com.nowa.block.entity.ModBlockEntities;
import com.nowa.screen.ModScreenHandlers;
import com.nowa.item.ModItemGroups;
import com.nowa.item.ModItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShinyDepths implements ModInitializer {
	public static final String MOD_ID = "shiny-depths";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();
		ModScreenHandlers.registerScreenHandlers();
	}
}