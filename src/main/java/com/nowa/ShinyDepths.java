package com.nowa;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.nowa.block.ModBlocks;
import com.nowa.config.ConfigCommand;
import com.nowa.config.ModConfig;
import com.nowa.message.ModMessages;
import com.nowa.command.LocateOreCommand;
import com.nowa.world.LanternBeacons;
import com.nowa.world.ModWorldGen;
import com.nowa.block.entity.ModBlockEntities;
import com.nowa.screen.ModScreenHandlers;
import com.nowa.item.ModItemGroups;
import com.nowa.item.ModDataComponents;
import com.nowa.item.ModGear;
import com.nowa.item.ModItems;
import com.nowa.item.ModJewelry;
import com.nowa.jewelry.Jewelry;
import com.nowa.item.PouchDyeRecipe;
import com.nowa.item.PurityBlockRecipes;
import com.nowa.lapidary.Lapidary;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShinyDepths implements ModInitializer {
	public static final String MOD_ID = "shiny-depths";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
		ModConfig.load();                       // avant tout le reste : les objets s'enregistrent avec ces valeurs
		ModDataComponents.registerDataComponents();
		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		com.nowa.item.MinPurityIngredient.register();
		ModGear.registerGear();
		ModJewelry.registerJewelry();
		PouchDyeRecipe.register();
		PurityBlockRecipes.register();
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();
		ModScreenHandlers.registerScreenHandlers();
		Lapidary.register();
		Jewelry.register();
		com.nowa.headlamp.Headlamp.register();
		com.nowa.dynamite.Dynamite.register();
		com.nowa.villager.LapidaryVillager.register();
		ModMessages.register();
		ModWorldGen.register();
		LanternBeacons.register();
		com.nowa.socket.Socketing.register();
		com.nowa.powers.Powers.register();
		com.nowa.powers.PowerHooks.register();
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> com.nowa.item.PurityCommand.register(dispatcher));
		com.nowa.world.GemBlockPurity.register();
		com.nowa.world.BatGems.register();
		com.nowa.world.ChestGems.register();
		CommandRegistrationCallback.EVENT.register(LocateOreCommand::register);
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> ConfigCommand.register(dispatcher));
		warmUpConfigHolders();
		ModConfig.save();                       // le fichier reflète alors exactement ce que le mod utilise
	}

	/**
	 * Charge les classes qui portent des réglages mais que rien n'a encore touchées. Leurs valeurs
	 * s'inscrivent au chargement de la classe ; sans ce passage, le fichier ne les contiendrait
	 * qu'après la première partie où elles auraient servi.
	 */
	private static void warmUpConfigHolders() {
		for (Class<?> holder : java.util.List.of(
				com.nowa.item.Purity.class,
				com.nowa.item.GemColors.class,
				com.nowa.item.PolisherItem.class,
				com.nowa.item.GeodeContents.class,
				com.nowa.item.GearSalvage.class,
				com.nowa.socket.Socketing.class,
				com.nowa.lapidary.LapidaryLevels.class,
				com.nowa.lapidary.LapidaryBonuses.class,
				com.nowa.lapidary.LapidaryCrafting.class,
				com.nowa.lapidary.LapidarySkillTree.class,
				com.nowa.headlamp.LanternSight.class,
				com.nowa.powers.Powers.class,
				com.nowa.powers.PowerEffects.class,
				com.nowa.powers.power.Lifesteal.class,
				com.nowa.powers.power.Fury.class,
				com.nowa.powers.power.Scholar.class,
				com.nowa.powers.power.Sobriety.class,
				com.nowa.powers.power.Reach.class,
				com.nowa.powers.power.Merchant.class,
				com.nowa.powers.power.Riposte.class,
				com.nowa.powers.power.Reflect.class,
				com.nowa.powers.power.Piercing.class,
				com.nowa.powers.power.Smelting.class,
				com.nowa.powers.power.DoubleJump.class,
				com.nowa.powers.power.Recall.class,
				com.nowa.powers.power.EnderPocket.class,
				com.nowa.powers.power.Vein.class,
				com.nowa.powers.power.Fishing.class,
				com.nowa.world.BatGems.class,
				com.nowa.dynamite.Dynamite.class,
				com.nowa.villager.LapidaryVillager.class,
				com.nowa.headlamp.GemSense.class,
				com.nowa.headlamp.HeadlampLight.class,
				com.nowa.block.entity.LapidaryTableBlockEntity.class)) {
			try {
				Class.forName(holder.getName(), true, ShinyDepths.class.getClassLoader());
			} catch (ClassNotFoundException impossible) {
				LOGGER.error("Could not load config holder {}", holder.getName());
			}
		}
	}
}