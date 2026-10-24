package com.nowa.item;

import com.nowa.config.ModConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;

/**
 * Ce qu'il reste d'une pièce de panoplie qui vient de céder.
 *
 * <p>La pyrite ne tient pas longtemps, mais elle ne se perd pas : une pioche brisée rend ses trois
 * lingots, dont un seul est resté entier — les deux autres reviennent en miettes. C'est le
 * caractère du métal : il s'effrite sans cesse et reste là quand même, si bien qu'on ne manque
 * jamais de pyrite, seulement d'outils en état.
 *
 * <p>Les autres métaux ne s'effritent pas : ils durent assez pour qu'on les répare à l'enclume, et
 * la pyrite comme le cobalt se refondent de toute façon au four quand on démonte une pièce encore
 * en état.
 */
public final class GearSalvage {
    /** Les métaux qui reviennent de leurs cendres, et la pépite dans laquelle ils s'émiettent. */
    private static final Map<ModGear.Metal, Item> CRUMBS = Map.of(ModGear.Metal.PYRITE, ModItems.PYRITE_NUGGET);
    private static int nuggetsPerIngot = 9;
    /** Ce qu'on retrouve, en centièmes de ce que la pièce a coûté. Cent : rien ne se perd. */
    private static int recovery = 100;
    /** Un lingot sur trois a tenu le choc ; le reste part en pépites. */
    private static int wholeShare = 3;

    static {
        ModConfig.onApply(GearSalvage::applyConfig);
    }

    private static void applyConfig() {
        nuggetsPerIngot = ModConfig.i("salvage.nuggets_per_ingot", 9);
        recovery = ModConfig.i("salvage.recovery_percent", 100);
        wholeShare = Math.max(1, ModConfig.i("salvage.one_ingot_in", 3));
    }

    private GearSalvage() {
    }

    /** Appelé par le mixin au moment où une pièce d'équipement cède, quelle qu'en soit la cause. */
    public static void onBroken(LivingEntity entity, Item item) {
        if (entity.getWorld().isClient()) {
            return;
        }
        ModGear.Metal metal = ModGear.metalOf(item);
        ModGear.Piece piece = ModGear.pieceOf(item);
        // le polissoir de pyrite n'est pas une panoplie, mais il s'effrite de la même façon
        boolean polisher = item == ModItems.PYRITE_POLISHER;
        if (polisher) {
            metal = ModGear.Metal.PYRITE;
        }
        Item crumb = metal == null ? null : CRUMBS.get(metal);
        if (crumb == null || (piece == null && !polisher)) {
            return;
        }
        int ingots = polisher ? 1 : piece.ingots();
        int value = ingots * nuggetsPerIngot * recovery / 100;
        int whole = Math.min(ingots / wholeShare, value / nuggetsPerIngot);
        give(entity, new ItemStack(metal.ingot(), whole));
        give(entity, new ItemStack(crumb, value - whole * nuggetsPerIngot));
    }

    /** Toujours au sol, jamais dans l'inventaire : ce qui se brise tombe, et se ramasse. */
    private static void give(LivingEntity entity, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        entity.dropStack(stack, entity.getHeight() / 2);   // à hauteur de main, pas sous les pieds
    }
}
