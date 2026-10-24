package com.nowa.powers.power;

import com.nowa.powers.GemPower;
import com.nowa.powers.PowerCooldowns;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

/**
 * La poche de l'End (onyx) : un clic droit qui ne fait rien d'autre ouvre le coffre de l'End du
 * joueur, où qu'il soit — le même contenu que tous ses coffres de l'End. Puis une recharge.
 */
public final class EnderPocket {
    /** La recharge, en secondes. */
    private static final PowerValue COOLDOWN = PowerValue.of("ender_pocket.cooldown", 60, 45, 30, 20, 10);
    private static final Text TITLE = Text.translatable("container.enderchest");

    private EnderPocket() {
    }

    /** Le joueur a fait un clic droit qui n'a rien fait d'autre. */
    public static void use(ServerPlayerEntity player) {
        int level = Powers.of(player).level(GemPower.ENDER_POCKET);
        if (level <= 0 || player.isSpectator() || player.currentScreenHandler != player.playerScreenHandler
                || !PowerCooldowns.trigger(player, GemPower.ENDER_POCKET, (int) Math.round(COOLDOWN.at(level) * 20.0))) {
            return;
        }
        EnderChestInventory chest = player.getEnderChestInventory();
        chest.setActiveBlockEntity(null);                  // aucun coffre posé : rien à ouvrir ni à refermer au monde
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, inventory, opener) -> GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, chest), TITLE));
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_ENDER_CHEST_OPEN,
                SoundCategory.PLAYERS, 0.5f, 1.0f);
    }
}
