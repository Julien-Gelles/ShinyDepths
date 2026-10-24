package com.nowa.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.RegistryEntryPredicateArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.nowa.block.ModBlockTags.ORES;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * /locate ore &lt;minerai ou #tag&gt; [rayon] : trouve le minerai le plus proche.
 *
 * <p>Vient se greffer sur le /locate vanilla, à côté de structure, biome et poi, et ne connaît que
 * les blocs de {@code #c:ores} — ceux du mod comme ceux du jeu. Contrairement aux structures, un
 * minerai n'existe pas avant que son chunk soit généré : la recherche se limite donc aux chunks
 * déjà chargés autour du joueur.
 */
public final class LocateOreCommand {
    private static final int DEFAULT_RADIUS = 8;
    private static final int MAX_RADIUS = 32;
    private static final SimpleCommandExceptionType NOT_FOUND =
            new SimpleCommandExceptionType(Text.translatable("commands.shiny-depths.locate_ore.not_found"));
    private static final SimpleCommandExceptionType NOT_AN_ORE =
            new SimpleCommandExceptionType(Text.translatable("commands.shiny-depths.locate_ore.not_an_ore"));

    private LocateOreCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("locate")
                .then(literal("ore").requires(source -> source.hasPermissionLevel(2))
                        .then(argument("ore", RegistryEntryPredicateArgumentType.registryEntryPredicate(registryAccess, RegistryKeys.BLOCK))
                                .suggests((context, builder) -> CommandSource.suggestMatching(suggestions(), builder))
                                .executes(context -> locate(context, DEFAULT_RADIUS))
                                .then(argument("radius", IntegerArgumentType.integer(1, MAX_RADIUS))
                                        .executes(context -> locate(context, IntegerArgumentType.getInteger(context, "radius")))))));
    }

    /** On ne propose que des minerais, plus les deux tags qui les regroupent. */
    private static Stream<String> suggestions() {
        Stream<String> tags = Stream.of("#" + ORES.id(), "#" + ConventionalBlockTags.ORES.id());
        return Stream.concat(tags, ores().stream()
                .map(entry -> entry.getKey().map(key -> key.getValue().toString()).orElse(""))
                .filter(name -> !name.isEmpty()));
    }

    private static List<RegistryEntry<Block>> ores() {
        return Registries.BLOCK.getEntryList(ConventionalBlockTags.ORES)
                .map(list -> list.stream().toList())
                .orElseGet(() -> Registries.BLOCK.getEntryList(ORES)
                        .map(RegistryEntryList.Named::stream)
                        .map(Stream::toList)
                        .orElse(List.of()));
    }

    private static int locate(CommandContext<ServerCommandSource> context, int radius) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        var wanted = RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "ore", RegistryKeys.BLOCK);

        // la commande ne sait chercher que des minerais : on refuse tout de suite le reste
        List<RegistryEntry<Block>> targets = new ArrayList<>(ores().stream().filter(wanted).toList());
        if (targets.isEmpty()) {
            throw NOT_AN_ORE.create();
        }
        Predicate<BlockState> matches = state -> targets.contains(state.getBlock().getRegistryEntry());

        BlockPos origin = BlockPos.ofFloored(source.getPosition());
        ChunkPos center = new ChunkPos(origin);
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        int found = 0;
        int scanned = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                WorldChunk chunk = world.getChunkManager().getWorldChunk(center.x + dx, center.z + dz);
                if (chunk == null) {
                    continue;                                   // chunk non chargé : rien à y chercher
                }
                scanned++;
                ChunkSection[] sections = chunk.getSectionArray();
                for (int index = 0; index < sections.length; index++) {
                    ChunkSection section = sections[index];
                    if (section.isEmpty() || !section.hasAny(matches)) {
                        continue;                               // la palette de la section suffit à l'écarter
                    }
                    int baseX = chunk.getPos().getStartX();
                    int baseY = chunk.sectionIndexToCoord(index) * 16;
                    int baseZ = chunk.getPos().getStartZ();
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                if (!matches.test(section.getBlockState(x, y, z))) {
                                    continue;
                                }
                                found++;
                                BlockPos pos = new BlockPos(baseX + x, baseY + y, baseZ + z);
                                double distance = pos.getSquaredDistance(origin);
                                if (distance < nearestDistance) {
                                    nearestDistance = distance;
                                    nearest = pos;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (nearest == null) {
            throw NOT_FOUND.create();
        }
        BlockPos target = nearest;
        int total = found;
        int chunks = scanned;
        source.sendFeedback(() -> Text.translatable("commands.shiny-depths.locate_ore.success",
                wanted.asString(), coordinates(target), (int) Math.sqrt(target.getSquaredDistance(origin)), total, chunks), false);
        return (int) Math.sqrt(nearestDistance);
    }

    /** Coordonnées cliquables, comme celles du /locate vanilla. */
    private static MutableText coordinates(BlockPos pos) {
        String teleport = "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
        return Texts.bracketed(Text.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()))
                .styled(style -> style.withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, teleport))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip"))));
    }
}
