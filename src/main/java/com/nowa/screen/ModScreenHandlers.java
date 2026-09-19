package com.nowa.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

public class ModScreenHandlers {
    public static final ScreenHandlerType<LapidaryTableScreenHandler> LAPIDARY_TABLE = Registry.register(Registries.SCREEN_HANDLER,
            Identifier.of(MOD_ID, "lapidary_table"), new ExtendedScreenHandlerType<>(LapidaryTableScreenHandler::new, BlockPos.PACKET_CODEC));

    public static void registerScreenHandlers() {
        LOGGER.info("├─ Registering Mod Screen Handlers for " + MOD_ID);
    }
}
