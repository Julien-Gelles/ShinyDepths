package com.nowa.dynamite;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Le bâton de dynamite. Un clic droit fait, dans l'ordre : en allumer un si l'autre main tient un
 * briquet et qu'aucun ne brûle encore, sinon le lancer — allumé ou non.
 *
 * <p>Allumé et gardé en main, il explose au bout de sa mèche, là où se tient celui qui le porte.
 */
public class DynamiteItem extends Item {
    public DynamiteItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        Hand other = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (!Dynamite.isLit(stack) && Dynamite.isLighter(user.getStackInHand(other))) {
            Dynamite.light(world, user, hand);
            return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW,
                SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
        if (!world.isClient()) {
            DynamiteEntity thrown = new DynamiteEntity(world, user);
            thrown.setItem(Dynamite.burningStick(stack));
            thrown.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, Dynamite.throwSpeed(), 1.0f);
            world.spawnEntity(thrown);
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (Dynamite.isLit(stack)) {
            Dynamite.spend(stack);              // le bâton a déjà quitté la pile en s'allumant
        } else {
            stack.decrementUnlessCreative(1, user);
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    /**
     * La mèche brûle aussi dans l'inventaire : arrivée au bout, la dynamite explose sur son porteur.
     * Seul le bâton allumé part ; le reste de la pile n'explose pas.
     *
     * <p>L'explosion part des pieds, pas de la main : à hauteur de poitrine elle serait trop loin du
     * sol pour l'entamer, alors qu'un bâton lancé explose toujours contre ce qu'il touche.
     */
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient() || !Dynamite.isDue(stack, world)) {
            return;
        }
        Dynamite.spend(stack);
        Vec3d pos = new Vec3d(entity.getX(), entity.getY() + 0.1, entity.getZ());
        Dynamite.explode(world, null, entity instanceof LivingEntity living ? living : null, pos);
    }
}
