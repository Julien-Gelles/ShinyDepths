package com.nowa.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
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
 * Le coût en durabilité dépend du niveau de la pierre : un polissoir neuf fait 16 pierres de niveau 1
 * ou 4 pierres de niveau 2.
 */
public class PolisherItem extends Item {
    private static final int POLISH_TICKS = 40;
    private static final int[] USES_PER_TIER = {0, 16, 4};   // index = niveau de la pierre
    private final int level;

    public PolisherItem(int level, Settings settings) {
        super(settings);
        this.level = level;
    }

    /** Réparable à l'enclume avec du silex ; un silex suffit à le remettre à neuf (voir AnvilScreenHandlerMixin). */
    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.isOf(Items.FLINT);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BRUSH;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return POLISH_TICKS;
    }

    /** Durabilité consommée pour polir cette pierre. */
    private int costFor(ItemStack tool, ItemStack gem) {
        return tool.getMaxDamage() / USES_PER_TIER[GemPolishing.tierOf(gem)];
    }

    /**
     * Seul le niveau de la pierre compte. Comme en vanilla (épée qui casse un bloc), on ne vérifie pas
     * la durabilité restante : le polissage se fait toujours, et l'outil se brise s'il tombe à zéro ou moins.
     */
    private boolean canPolishNow(ItemStack tool, ItemStack gem) {
        return GemPolishing.canPolish(gem, level);
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
        if (remainingUseTicks % 8 == 0) {
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
        Item polished = GemPolishing.polishedFor(gem, level);
        int cost = costFor(stack, gem);
        gem.decrement(1);
        ItemStack result = new ItemStack(polished);
        if (!player.getInventory().insertStack(result)) {
            player.dropItem(result, false);
        }
        stack.damage(cost, player, LivingEntity.getSlotForHand(toolHand));
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.8f, 1.2f);
        return stack;
    }

    private static Hand otherHand(Hand hand) {
        return hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }
}
