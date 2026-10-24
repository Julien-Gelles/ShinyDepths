package com.nowa.jewelry;

import com.nowa.item.ModJewelry.Kind;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

import java.util.ArrayList;
import java.util.List;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Les bijoux portés : là où ils vivent, et ce qu'il advient d'eux à la mort.
 *
 * <p>Le contenu est gardé à la mort par l'attachment ; c'est
 * {@code PlayerEntityMixin} qui le vide d'abord, au même endroit et à la même condition que le jeu
 * vide l'inventaire — si bien qu'une partie en « keepInventory » garde aussi ses bagues.
 */
public final class Jewelry {
    public static final AttachmentType<JewelryInventory> WORN = AttachmentRegistry.create(
            Identifier.of(MOD_ID, "jewelry"),
            builder -> builder
                    .initializer(JewelryInventory::new)
                    .persistent(JewelryInventory.CODEC)
                    .copyOnDeath());

    private Jewelry() {
    }

    public static void register() {
        LOGGER.info("├─ Registering Jewelry slots for " + MOD_ID);
        // Un joueur qui arrive, qui ressuscite ou qui change de monde est un corps neuf, sans
        // aucun de nos modificateurs : on les repose a chaque fois.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            of(handler.player).takeDisplaced().forEach(handler.player.getInventory()::offerOrDrop);
            JewelryAttributes.refresh(handler.player);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                JewelryAttributes.refresh(newPlayer));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
                JewelryAttributes.refresh(player));
    }

    /** Les bijoux de ce joueur ; l'inventaire est créé à la première demande. */
    public static JewelryInventory of(PlayerEntity player) {
        JewelryInventory worn = player.getAttachedOrCreate(WORN);
        worn.bind(player);
        return worn;
    }

    /** Les bijoux d'une forme que porte ce joueur. */
    public static List<ItemStack> worn(PlayerEntity player, Kind kind) {
        return List.copyOf(of(player).of(kind).values());
    }

    /**
     * À la mort : les bijoux tombent avec le reste, sauf si la règle du monde garde l'inventaire.
     * Appelé depuis {@code PlayerEntity.dropInventory}, donc au bon moment pour que les objets
     * apparaissent sur place.
     */
    public static void dropAll(PlayerEntity player) {
        if (player.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            return;
        }
        JewelryInventory worn = of(player);
        List<ItemStack> dropped = new ArrayList<>();
        for (Worn place : Worn.values()) {
            ItemStack stack = worn.get(place);
            if (!stack.isEmpty()) {
                dropped.add(stack);
                worn.set(place, ItemStack.EMPTY);
            }
        }
        dropped.forEach(stack -> player.dropItem(stack, true, false));
        if (!dropped.isEmpty()) {
            worn.markDirty();
        }
    }
}
