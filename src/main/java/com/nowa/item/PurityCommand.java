package com.nowa.item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;

/**
 * {@code /nowasdepths purity <classe>} : donne cette pureté à la pile de pierres polies tenue en
 * main. Un outil d'essai, réservé aux opérateurs — une pierre prise dans l'onglet créatif n'a pas de
 * pureté, et sans pureté elle ne se sertit pas.
 */
public final class PurityCommand {
    private PurityCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("nowasdepths")
                .then(CommandManager.literal("purity")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("class", StringArgumentType.word())
                                .suggests((context, builder) -> CommandSource.suggestMatching(
                                        Arrays.stream(Purity.values()).map(Purity::asString), builder))
                                .executes(context -> set(context.getSource(),
                                        StringArgumentType.getString(context, "class"))))));
    }

    private static int set(ServerCommandSource source, String name) {
        ServerPlayerEntity player = source.getPlayer();
        Purity purity = Arrays.stream(Purity.values()).filter(value -> value.asString().equals(name))
                .findFirst().orElse(null);
        ItemStack held = player == null ? ItemStack.EMPTY : player.getMainHandStack();
        if (purity == null || !GemPolishing.isPolished(held.getItem())) {
            source.sendError(Text.translatable("command.nowas-depths.purity.not_a_gem"));
            return 0;
        }
        held.set(ModDataComponents.PURITY, purity);
        held.remove(ModDataComponents.UNAPPRAISED);
        GemPolishing.applyGlint(held);
        source.sendFeedback(() -> Text.translatable("command.nowas-depths.purity.set", purity.displayName())
                .formatted(Formatting.GREEN), false);
        return 1;
    }
}
