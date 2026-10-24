package com.nowa.config;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * {@code /nowasdepths config reload} : relit le fichier de réglages sans quitter la partie.
 *
 * <p>Tout ne se relit pas. Ce qui a été cuit dans un objet à son enregistrement — durabilité,
 * protection, dégâts, emplacements de sertissage — appartient déjà au jeu chargé ; la commande le
 * dit plutôt que de laisser croire à un changement qui n'a pas eu lieu.
 */
public final class ConfigCommand {
    private ConfigCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("nowasdepths")
                .then(CommandManager.literal("config")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("reload")
                                .executes(context -> {
                                    ModConfig.reload();
                                    context.getSource().sendFeedback(() ->
                                            Text.translatable("command.nowas-depths.config.reloaded")
                                                    .formatted(Formatting.GREEN), true);
                                    context.getSource().sendFeedback(() ->
                                            Text.translatable("command.nowas-depths.config.restart_needed")
                                                    .formatted(Formatting.GRAY), false);
                                    return 1;
                                }))));
    }
}
