package com.nowa.lapidary;

import com.nowa.block.ModOres;
import com.nowa.item.ModItems;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Ce que la branche Prospection ajoute au fond de la mine : une matière brute de plus de temps en
 * temps (Filon), un nodule de plus dans les roches à géode (Nodule).
 *
 * <p>Le supplément se pose à côté du butin normal, sans passer par la table de butin : il ne
 * bénéficie donc pas de Fortune, et le Toucher de soie l'annule — la roche est alors emportée
 * entière, il n'y a rien à ajouter.
 */
public final class LapidaryMining {
    /** Minerai du mod → ce qu'il lâche, construit une fois pour toutes. */
    private static final Map<Block, Item> ORE_DROPS = new HashMap<>();

    private LapidaryMining() {
    }

    public static void register() {
        ModOres.ores().forEach(ore -> ORE_DROPS.put(ore.block(), ore.drop()));
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> onBroken(world, player, pos, state));
    }

    private static void onBroken(World world, PlayerEntity player, BlockPos pos, BlockState state) {
        if (world.isClient() || player.isCreative()) {
            return;
        }
        Item drop = ORE_DROPS.get(state.getBlock());
        if (drop == null || hasSilkTouch(world, player.getMainHandStack())) {
            return;
        }
        double chance = drop == ModItems.GEODE
                ? LapidaryBonuses.extraNoduleChance(player)
                : LapidaryBonuses.extraDropChance(player);
        if (chance > 0 && player.getRandom().nextDouble() < chance) {
            Block.dropStack(world, pos, new ItemStack(drop));
        }
    }

    private static boolean hasSilkTouch(World world, ItemStack tool) {
        return world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.SILK_TOUCH)
                .map(silkTouch -> EnchantmentHelper.getLevel(silkTouch, tool) > 0)
                .orElse(false);
    }
}
