package com.nowa.world;

import com.nowa.config.ModConfig;
import com.nowa.item.GemPolishing;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;

import java.util.List;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Les chauves-souris vivent au fond des grottes, et il leur arrive d'y avoir ramassé quelque chose :
 * une fois sur dix, celle qui meurt lâche une pierre déjà polie.
 *
 * <p>Le tirage est plat : toutes les pierres ont la même chance. La pierre tombe <em>sans
 * pureté</em>, comme toute pierre qui ne sort pas de la taille : c'est la loupe qui la révèle.
 *
 * <p>La pierre tombe quelle que soit la cause de la mort, comme un butin ordinaire, et obéit à la
 * règle de monde qui coupe le butin des créatures.
 */
public final class BatGems {
    /** Chance qu'une chauve-souris lâche une pierre, entre 0 et 1. */
    private static double chance = 0.10;

    static {
        ModConfig.onApply(() -> chance = Math.clamp(ModConfig.d("bats.gem_chance", 0.10), 0.0, 1.0));
    }

    private BatGems() {
    }

    public static void register() {
        LOGGER.info("├─ Registering Bat gems for " + MOD_ID);
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof BatEntity bat)
                    || !bat.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
                return;
            }
            Random random = bat.getRandom();
            if (random.nextDouble() >= chance) {
                return;
            }
            List<Item> gems = GemPolishing.polishedGems();
            bat.dropStack(new ItemStack(gems.get(random.nextInt(gems.size()))));
        });
    }
}
