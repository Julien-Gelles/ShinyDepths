package com.nowa.item;

import com.nowa.lapidary.Lapidary;
import com.nowa.lapidary.LapidaryBonuses;
import com.nowa.lapidary.LapidaryLevels;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Polissoir : pierre brute dans l'autre main, clic droit maintenu comme le pinceau d'archéologie.
 *
 * <p>Il polit <em>toutes</em> les pierres, du plus tendre au saphir : la dureté ne ferme rien, elle
 * coûte. Un rubis demande huit fois la durabilité et cinq fois le temps d'un ambre, si bien qu'on
 * finit par vouloir une table, sans qu'un refus muet ait jamais eu à l'expliquer.
 *
 * <p>Les versions en métal ne changent que la vivacité et la durabilité — et la pyrite, qui
 * s'effrite au lieu de disparaître.
 */
public class PolisherItem extends Item {
    /** Durabilité dépensée selon la dureté de la pierre (index = dureté). */
    private static int[] costs = {0, 4, 8, 16, 32};
    /** Durée du polissage en ticks, avant division par la vivacité du polissoir. */
    private static int[] ticks = {0, 40, 80, 120, 200};

    static {
        com.nowa.config.ModConfig.onApply(PolisherItem::applyConfig);
    }

    private static void applyConfig() {
        costs = com.nowa.config.ModConfig.ints("polisher.cost_by_hardness", new int[]{0, 4, 8, 16, 32});
        ticks = com.nowa.config.ModConfig.ints("polisher.ticks_by_hardness", new int[]{0, 40, 80, 120, 200});
    }

    /** Ce qui le répare, et de combien il divise le temps de polissage. */
    private final Item repairItem;
    private final int speed;

    public PolisherItem(Item repairItem, int speed, Settings settings) {
        super(settings);
        this.repairItem = repairItem;
        this.speed = Math.max(1, speed);
    }

    /** Le matériau qui le remet à neuf : silex pour celui de pierre, son lingot pour les autres. */
    public Item repairItem() {
        return repairItem;
    }

    /** Réparable à l'enclume ; un seul matériau suffit à le remettre à neuf (voir AnvilScreenHandlerMixin). */
    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.isOf(repairItem);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BRUSH;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        int ticks = polishTicks(user.getStackInHand(otherHand(user.getActiveHand())));
        // le Polissage vif du lapidaire raccourcit le geste ; le client lit le même arbre, synchronisé
        return user instanceof PlayerEntity player
                ? Math.max(1, (int) Math.round(ticks * LapidaryBonuses.polishTime(player)))
                : ticks;
    }

    /** Durée du polissage : celle de la dureté, divisée par la vivacité du polissoir. */
    private int polishTicks(ItemStack gem) {
        return Math.max(1, at(ticks, gem) / speed);
    }

    /** Durabilité consommée pour polir cette pierre. */
    private int costFor(ItemStack gem) {
        return at(costs, gem);
    }

    private static int at(int[] values, ItemStack gem) {
        int tier = GemPolishing.tierOf(gem);
        return tier > 0 && tier < values.length ? values[tier] : values[values.length - 1];
    }

    /**
     * Toute pierre brute se polit à la main. Comme en vanilla (épée qui casse un bloc), on ne vérifie
     * pas la durabilité restante : le polissage se fait toujours, et l'outil se brise s'il tombe à
     * zéro ou moins.
     */
    private boolean canPolishNow(ItemStack tool, ItemStack gem) {
        return GemPolishing.tierOf(gem) > 0;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack tool = user.getStackInHand(hand);
        if (canPolishNow(tool, user.getStackInHand(otherHand(hand)))) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(tool);
        }
        return TypedActionResult.pass(tool);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }
        ItemStack gem = user.getStackInHand(otherHand(user.getActiveHand()));
        if (!canPolishNow(stack, gem)) {
            user.stopUsingItem();
            return;
        }
        if ((getMaxUseTime(stack, user) - remainingUseTicks) % 10 == 0) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.PLAYERS, 0.35f, 1.4f);
            Vec3d look = user.getRotationVec(1.0f);
            serverWorld.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, gem),
                    user.getX() + look.x * 0.6, user.getEyeY() - 0.4 + look.y * 0.6, user.getZ() + look.z * 0.6,
                    3, 0.1, 0.1, 0.1, 0.02);
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (world.isClient || !(user instanceof PlayerEntity player)) {
            return stack;
        }
        Hand toolHand = player.getActiveHand();
        ItemStack gem = player.getStackInHand(otherHand(toolHand));
        if (!canPolishNow(stack, gem)) {
            return stack;
        }
        Item polished = GemPolishing.polishedFor(gem);
        int cost = LapidaryBonuses.polisherWear(player, costFor(gem));
        int xp = LapidaryLevels.polishXp(GemPolishing.tierOf(gem));
        gem.decrement(1);
        ItemStack result = GemPolishing.polishedStack(polished, Purity.roll(Lapidary.purityRank(player), world.random));
        if (!player.getInventory().insertStack(result)) {
            player.dropItem(result, false);
        }
        stack.damage(cost, player, LivingEntity.getSlotForHand(toolHand));
        if (player instanceof ServerPlayerEntity serverPlayer) {
            Lapidary.addXp(serverPlayer, xp);
        }
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.8f, 1.2f);
        return stack;
    }

    private static Hand otherHand(Hand hand) {
        return hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }
}
