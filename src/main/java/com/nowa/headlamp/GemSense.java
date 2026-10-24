package com.nowa.headlamp;

import com.nowa.block.ModOres;
import com.nowa.config.ModConfig;
import com.nowa.item.GemColors;
import com.nowa.item.GemPolishing;
import com.nowa.item.ModDataComponents;
import com.nowa.item.Purity;
import com.nowa.headlamp.HeadlampNetworking.GemSensePayload;
import com.nowa.socket.SocketFamily;
import com.nowa.socket.Socketing;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Le sens de la lampe frontale : chaque pierre sertie reconnaît la sienne dans la roche.
 *
 * <p>Ce n'est pas une vision continue mais un <em>battement</em>. Toutes les cinq secondes, la lampe
 * cherche autour de son porteur les minerais de ses pierres et les fait luire une seconde ou deux.
 * On garde ainsi le plaisir de creuser — on sait seulement où creuser — et le coût tient dans un
 * balayage épisodique plutôt que dans un rendu permanent.
 *
 * <p>La portée dépend de la pureté, et elle grimpe vite : une ébréchée voit à peine le bloc voisin,
 * une philosophale fouille la salle entière. Deux pierres identiques ne s'additionnent pas : c'est
 * la meilleure des deux qui compte.
 */
public final class GemSense {
    /** Un battement toutes les cinq secondes. */
    private static int interval = 100;
    /** Il dure deux secondes. */
    private static int glowTicks = 40;
    /** Portée selon la pureté, de l'ébréchée à la philosophale. */
    private static int[] radiusByPurity = {2, 4, 6, 12, 24};
    /** Garde-fou : au-delà, le paquet grossirait pour rien. */
    private static int maxFound = 256;

    /** De la pierre polie aux minerais qui la portent. Construite au premier battement. */
    private static Map<Item, Set<Block>> ores;

    static {
        ModConfig.onApply(GemSense::applyConfig);
    }

    private GemSense() {
    }

    /** Le rythme du battement, et la durée de sa lueur : les lanternes de cobalt battent au même pas. */
    static int interval() {
        return interval;
    }

    static int glowTicks() {
        return glowTicks;
    }

    private static void applyConfig() {
        interval = Math.max(1, ModConfig.i("headlamp.sense.interval_ticks", 100));
        glowTicks = Math.max(1, ModConfig.i("headlamp.sense.glow_ticks", 40));
        radiusByPurity = ModConfig.ints("headlamp.sense.radius_by_purity", new int[]{2, 4, 6, 12, 24});
        maxFound = Math.max(1, ModConfig.i("headlamp.sense.max_found", 256));
    }

    static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // les battements sont décalés d'un joueur à l'autre : jamais deux balayages le même tic
                if ((server.getTicks() + player.getId()) % interval == 0) {
                    pulse(player);
                }
            }
        });
    }

    private static void pulse(ServerPlayerEntity player) {
        ItemStack lamp = worn(player);
        if (lamp.isEmpty()) {
            return;
        }
        reach(lamp).forEach((gem, radius) -> {
            List<BlockPos> found = scan(player.getServerWorld(), player.getBlockPos(), radius, ores().get(gem));
            if (!found.isEmpty()) {
                ServerPlayNetworking.send(player, new GemSensePayload(GemColors.of(gem), glowTicks, found));
            }
        });
    }

    /** La lampe que ce joueur porte sur la tête, ou rien. */
    public static ItemStack worn(PlayerEntity player) {
        ItemStack head = player.getEquippedStack(EquipmentSlot.HEAD);
        return SocketFamily.of(head) == SocketFamily.LAMP ? head : ItemStack.EMPTY;
    }

    /**
     * Ce que la lampe sent, pierre par pierre : la meilleure portée de chaque sorte. Un deuxième
     * rubis n'allonge rien — seule la plus pure des deux compte.
     */
    public static Map<Item, Integer> reach(ItemStack lamp) {
        Map<Item, Integer> reach = new java.util.LinkedHashMap<>();
        for (ItemStack gem : Socketing.get(lamp).gems()) {
            if (senses(gem.getItem())) {
                reach.merge(gem.getItem(), radiusOf(gem), Math::max);
            }
        }
        return reach;
    }

    /** Cette pierre dort-elle dans une roche ? Seules celles-là ont quelque chose à sentir. */
    public static boolean senses(Item gem) {
        Set<Block> targets = ores().get(gem);
        return targets != null && !targets.isEmpty();
    }

    /** Portée de cette pierre selon sa pureté ; une pierre sans pureté compte comme limpide. */
    public static int radiusOf(ItemStack gem) {
        Purity purity = gem.get(ModDataComponents.PURITY);
        int index = purity == null ? Purity.CLEAR.ordinal() : purity.ordinal();
        return Math.max(1, radiusByPurity[Math.min(index, radiusByPurity.length - 1)]);
    }

    /**
     * Les minerais cherchés dans les chunks déjà chargés autour du joueur. La palette de chaque
     * section écarte d'emblée celles qui ne contiennent rien d'intéressant, si bien qu'un battement
     * ne parcourt en pratique qu'une poignée de sections.
     */
    private static List<BlockPos> scan(ServerWorld world, BlockPos center, int radius, Set<Block> targets) {
        Predicate<BlockState> matches = state -> targets.contains(state.getBlock());
        List<BlockPos> found = new ArrayList<>();
        ChunkPos origin = new ChunkPos(center);
        int span = (radius >> 4) + 1;
        int squared = radius * radius;
        for (int dx = -span; dx <= span && found.size() < maxFound; dx++) {
            for (int dz = -span; dz <= span && found.size() < maxFound; dz++) {
                WorldChunk chunk = world.getChunkManager().getWorldChunk(origin.x + dx, origin.z + dz);
                if (chunk == null) {
                    continue;                                  // chunk non chargé : rien à y chercher
                }
                collect(chunk, center, squared, radius, matches, found);
            }
        }
        return found;
    }

    private static void collect(WorldChunk chunk, BlockPos center, int squared, int radius,
                                Predicate<BlockState> matches, List<BlockPos> found) {
        ChunkSection[] sections = chunk.getSectionArray();
        for (int index = 0; index < sections.length; index++) {
            ChunkSection section = sections[index];
            int baseY = chunk.sectionIndexToCoord(index) * 16;
            if (baseY > center.getY() + radius || baseY + 15 < center.getY() - radius) {
                continue;                                      // section hors de portée en hauteur
            }
            if (section.isEmpty() || !section.hasAny(matches)) {
                continue;
            }
            int baseX = chunk.getPos().getStartX();
            int baseZ = chunk.getPos().getStartZ();
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockPos pos = new BlockPos(baseX + x, baseY + y, baseZ + z);
                        if (pos.getSquaredDistance(center) > squared
                                || !matches.test(section.getBlockState(x, y, z))) {
                            continue;
                        }
                        found.add(pos);
                        if (found.size() >= maxFound) {
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * La table des minerais, déduite du butin de chacun : un minerai qui donne du rubis brut est un
     * minerai de rubis. Rien n'est à tenir à jour, ajouter un gisement suffit.
     */
    private static Map<Item, Set<Block>> ores() {
        if (ores == null) {
            Map<Item, Set<Block>> table = new HashMap<>();
            for (ModOres.Ore ore : ModOres.ores()) {
                Item polished = GemPolishing.polishedFor(new ItemStack(ore.drop()));
                if (polished != null) {
                    table.computeIfAbsent(polished, gem -> new HashSet<>()).add(ore.block());
                }
            }
            // le diamant et l'émeraude du jeu : leurs minerais ne sont pas les nôtres, on les nomme
            table.computeIfAbsent(net.minecraft.item.Items.DIAMOND, gem -> new HashSet<>())
                    .addAll(Set.of(net.minecraft.block.Blocks.DIAMOND_ORE, net.minecraft.block.Blocks.DEEPSLATE_DIAMOND_ORE));
            table.computeIfAbsent(net.minecraft.item.Items.EMERALD, gem -> new HashSet<>())
                    .addAll(Set.of(net.minecraft.block.Blocks.EMERALD_ORE, net.minecraft.block.Blocks.DEEPSLATE_EMERALD_ORE));
            ores = Map.copyOf(table);
        }
        return ores;
    }
}
