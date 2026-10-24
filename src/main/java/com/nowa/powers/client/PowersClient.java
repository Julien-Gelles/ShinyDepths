package com.nowa.powers.client;

import com.nowa.powers.GemPower;
import com.nowa.powers.PowerEffects;
import com.nowa.powers.PowerNetworking;
import com.nowa.powers.Powers;
import com.nowa.powers.power.DoubleJump;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;

/**
 * Les pouvoirs vus du joueur : ce que le serveur lui dit de sa bague. L'affichage, dans l'inventaire
 * et parmi les effets, est dans {@link PowersPanel}.
 *
 * <p>S'y ajoutent les deux gestes que seul le client voit : la touche de saut pressée en l'air (double
 * saut), et un clic droit qui n'a rien fait d'autre (rappel, poche de l'End).
 */
public final class PowersClient {
    /** Ce que le serveur a calculé pour ce joueur la dernière fois. */
    private static Powers.Active active = Powers.Active.NONE;
    /** Le double saut : le joueur l'a-t-il encore (il le reprend au sol), et quand sa recharge finit. */
    private static boolean jumpCharged;
    private static long jumpReadyAt;
    private static boolean wasPressed;
    private static boolean wasAirborne;

    private PowersClient() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(Powers.ActivePayload.ID, (payload, context) ->
                context.client().execute(() -> active = payload.active()));
        ClientPlayNetworking.registerGlobalReceiver(PowerEffects.CooldownPayload.ID, (payload, context) ->
                context.client().execute(() -> Cooldowns.start(payload.effect(), payload.ticks())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            active = Powers.Active.NONE;
            jumpReadyAt = 0;
            Cooldowns.clear();
        });
        Powers.clientActive = PowersClient::active;
        HudRenderCallback.EVENT.register((context, tickCounter) -> PowersPanel.drawHudFrames(context));
        ClientTickEvents.END_CLIENT_TICK.register(PowersClient::tickJump);
    }

    public static Powers.Active active() {
        return active;
    }

    // ---------- le double saut ----------

    private static void tickJump(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return;
        }
        boolean pressed = client.options.jumpKey.isPressed();
        boolean airborne = !player.isOnGround();
        if (!airborne) {
            jumpCharged = true;
        }
        int cooldown = DoubleJump.cooldownTicks(active.level(GemPower.DOUBLE_JUMP));
        long now = client.world.getTime();
        // une pression franche, en l'air depuis au moins un tic : ni le saut du sol, ni la touche tenue
        if (cooldown > 0 && pressed && !wasPressed && airborne && wasAirborne && jumpCharged && now >= jumpReadyAt
                && client.currentScreen == null && canJumpAgain(player)) {
            jumpCharged = false;
            jumpReadyAt = now + cooldown;
            player.jump();
            player.fallDistance = 0.0f;
            ClientPlayNetworking.send(new PowerNetworking.DoubleJumpPayload());
        }
        wasPressed = pressed;
        wasAirborne = airborne;
    }

    /** Pas dans l'eau, pas sur une échelle, pas en vol : là, la touche de saut a déjà un autre sens. */
    private static boolean canJumpAgain(ClientPlayerEntity player) {
        return !player.isTouchingWater() && !player.isInLava() && !player.isClimbing() && !player.isFallFlying()
                && !player.hasVehicle() && !player.getAbilities().allowFlying
                && !player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
    }

    // ---------- le clic droit qui ne fait rien d'autre ----------

    /**
     * Le clic droit n'a rien fait : ni créature, ni bloc, ni objet tenu ne l'a pris. Si la bague a un
     * pouvoir qui s'en sert, on le dit au serveur, qui juge de la recharge.
     */
    public static void idleUse(MinecraftClient client) {
        GemPower power = active.power();
        if (client.player == null || client.interactionManager == null || client.interactionManager.isBreakingBlock()
                || power != GemPower.RECALL && power != GemPower.ENDER_POCKET) {
            return;
        }
        ClientPlayNetworking.send(new PowerNetworking.UsePayload());
    }
}
