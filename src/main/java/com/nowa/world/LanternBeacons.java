package com.nowa.world;

import com.nowa.block.ModBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Le répertoire des lanternes de cobalt posées dans le monde. Ce sont des <em>balises</em> : qui porte
 * une lampe frontale les voit luire à travers la roche (voir {@code com.nowa.headlamp.LanternSight}),
 * et retrouve le chemin qu'il a jalonné en descendant.
 *
 * <p>Chercher les lanternes autour d'un joueur en fouillant les blocs coûterait cher à cette portée.
 * On tient donc leur liste, par monde et par chunk. Elle se remplit au chargement d'un chunk — la
 * palette de chaque section suffit à écarter celles qui n'en contiennent pas — et se tient à jour
 * ensuite à la pose et à la casse. Rien n'est sauvegardé : le monde lui-même fait foi.
 */
public final class LanternBeacons {
    private static final Map<World, Map<Long, List<BlockPos>>> LANTERNS = new ConcurrentHashMap<>();

    private LanternBeacons() {
    }

    public static void register() {
        LOGGER.info("├─ Registering lantern beacons for " + MOD_ID);
        ServerChunkEvents.CHUNK_LOAD.register(LanternBeacons::scan);
        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> chunks(world).remove(chunk.getPos().toLong()));
        ServerWorldEvents.UNLOAD.register((server, world) -> LANTERNS.remove(world));
    }

    /**
     * Les lanternes à moins de {@code radius} blocs de ce point, dans les chunks chargés, les plus
     * proches d'abord, et pas plus de {@code max}.
     */
    public static List<BlockPos> near(World world, BlockPos center, int radius, int max) {
        Map<Long, List<BlockPos>> byChunk = LANTERNS.get(world);
        if (byChunk == null || byChunk.isEmpty() || radius <= 0) {
            return List.of();
        }
        List<BlockPos> found = new ArrayList<>();
        long squared = (long) radius * radius;
        int span = (radius >> 4) + 1;
        int chunkX = center.getX() >> 4, chunkZ = center.getZ() >> 4;
        for (int dx = -span; dx <= span; dx++) {
            for (int dz = -span; dz <= span; dz++) {
                List<BlockPos> lanterns = byChunk.get(ChunkPos.toLong(chunkX + dx, chunkZ + dz));
                if (lanterns == null) {
                    continue;
                }
                for (BlockPos lantern : lanterns) {
                    if (lantern.getSquaredDistance(center) <= squared) {
                        found.add(lantern);
                    }
                }
            }
        }
        if (found.size() > max) {
            found.sort((a, b) -> Double.compare(a.getSquaredDistance(center), b.getSquaredDistance(center)));
            return List.copyOf(found.subList(0, max));
        }
        return found;
    }

    /** Appelé par le bloc lui-même : toute pose passe par là, y compris celle d'un distributeur. */
    public static void add(World world, BlockPos pos) {
        if (world.isClient()) {
            return;
        }
        BlockPos placed = pos.toImmutable();
        chunks(world).compute(ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4), (key, known) -> {
            if (known != null && known.contains(placed)) {
                return known;
            }
            List<BlockPos> next = known == null ? new ArrayList<BlockPos>() : new ArrayList<>(known);
            next.add(placed);
            return List.copyOf(next);
        });
    }

    public static void remove(World world, BlockPos pos) {
        if (world.isClient()) {
            return;
        }
        chunks(world).computeIfPresent(ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4), (key, known) -> {
            List<BlockPos> next = new ArrayList<>(known);
            next.remove(pos);
            return next.isEmpty() ? null : List.copyOf(next);
        });
    }

    /** Au chargement d'un chunk : on y retrouve les lanternes posées lors des sessions passées. */
    private static void scan(ServerWorld world, WorldChunk chunk) {
        List<BlockPos> found = new ArrayList<>();
        ChunkSection[] sections = chunk.getSectionArray();
        for (int index = 0; index < sections.length; index++) {
            ChunkSection section = sections[index];
            if (section.isEmpty() || !section.hasAny(LanternBeacons::isLantern)) {
                continue;                                   // la palette de la section suffit à l'écarter
            }
            int baseX = chunk.getPos().getStartX();
            int baseY = chunk.sectionIndexToCoord(index) * 16;
            int baseZ = chunk.getPos().getStartZ();
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (isLantern(section.getBlockState(x, y, z))) {
                            found.add(new BlockPos(baseX + x, baseY + y, baseZ + z));
                        }
                    }
                }
            }
        }
        long key = chunk.getPos().toLong();
        if (found.isEmpty()) {
            chunks(world).remove(key);
        } else {
            chunks(world).put(key, List.copyOf(found));
        }
    }

    private static boolean isLantern(BlockState state) {
        return state.isOf(ModBlocks.COBALT_LANTERN);
    }

    private static Map<Long, List<BlockPos>> chunks(World world) {
        return LANTERNS.computeIfAbsent(world, key -> new ConcurrentHashMap<>());
    }
}
