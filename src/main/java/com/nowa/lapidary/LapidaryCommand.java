package com.nowa.lapidary;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.function.UnaryOperator;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * /lapidary                              : son niveau, son XP et ses points
 * /lapidary xp add &lt;joueurs&gt; &lt;xp&gt;         : donner de l'XP (op)
 * /lapidary level set &lt;joueurs&gt; &lt;niveau&gt;  : fixer le niveau (op)
 * /lapidary reset &lt;joueurs&gt;               : tout remettre à zéro (op)
 */
final class LapidaryCommand {
    private LapidaryCommand() {
    }

    static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                         CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("lapidary")
                .executes(LapidaryCommand::info)
                .then(literal("xp").requires(source -> source.hasPermissionLevel(2))
                        .then(literal("add")
                                .then(argument("targets", EntityArgumentType.players())
                                        .then(argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> {
                                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                                    for (ServerPlayerEntity player : targets(context)) {
                                                        Lapidary.addXp(player, amount);
                                                    }
                                                    return feedback(context, "xp_added", amount);
                                                })))))
                .then(literal("level").requires(source -> source.hasPermissionLevel(2))
                        .then(literal("set")
                                .then(argument("targets", EntityArgumentType.players())
                                        .then(argument("level", IntegerArgumentType.integer(0, 9_000))
                                                .executes(context -> {
                                                    int level = IntegerArgumentType.getInteger(context, "level");
                                                    return modify(context, "level_set", level, data -> {
                                                        LapidaryData updated = data.withXp(LapidaryLevels.totalXpFor(level));
                                                        // moins de niveaux que de points dépensés : on rend les points
                                                        return updated.availablePoints() < 0 ? updated.withoutUnlocks() : updated;
                                                    });
                                                })))))
                .then(literal("reset").requires(source -> source.hasPermissionLevel(2))
                        .then(argument("targets", EntityArgumentType.players())
                                .executes(context -> modify(context, "reset", 0, data -> LapidaryData.EMPTY)))));
    }

    private static int info(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        LapidaryData data = Lapidary.get(context.getSource().getPlayerOrThrow());
        int level = data.level();
        Text progress = Text.translatable("lapidary.shiny-depths.screen.xp",
                LapidaryLevels.progressInLevel(data.xp()), LapidaryLevels.xpToNext(level));
        context.getSource().sendFeedback(() -> Text.translatable("lapidary.shiny-depths.command.info",
                level, progress, Lapidary.count("lapidary.shiny-depths.points", data.availablePoints())), false);
        return level;
    }

    private static Collection<ServerPlayerEntity> targets(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return EntityArgumentType.getPlayers(context, "targets");
    }

    private static int modify(CommandContext<ServerCommandSource> context, String key, int value,
                              UnaryOperator<LapidaryData> change) throws CommandSyntaxException {
        for (ServerPlayerEntity player : targets(context)) {
            Lapidary.set(player, change.apply(Lapidary.get(player)));
        }
        return feedback(context, key, value);
    }

    private static int feedback(CommandContext<ServerCommandSource> context, String key, int value) throws CommandSyntaxException {
        int count = targets(context).size();
        context.getSource().sendFeedback(() -> Text.translatable("lapidary.shiny-depths.command." + key, value,
                Lapidary.count("lapidary.shiny-depths.players", count)), true);
        return count;
    }
}
