package com.nowa.mixin;

import com.nowa.jewelry.JewelrySlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * En créatif, l'inventaire ne passe pas par les clics du serveur : le client range lui-même, puis
 * annonce case par case ce qu'elle contient désormais. Le serveur n'écoute ces annonces que pour
 * les 45 cases du jeu ; sans ce greffon, un bijou posé en créatif ne l'aurait été qu'à l'écran.
 *
 * <p>On n'accepte que ce que la case accepterait d'un clic : un bijou de la bonne forme, ou le vide.
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    // En fin de méthode : le jeu a déjà ramené le paquet sur le fil principal et vérifié le mode.
    @Inject(method = "onCreativeInventoryAction", at = @At("TAIL"))
    private void nowasDepths$creativeJewelry(CreativeInventoryActionC2SPacket packet, CallbackInfo info) {
        if (!player.interactionManager.isCreative()) {
            return;
        }
        int index = packet.slot();
        if (index < 0 || index >= player.playerScreenHandler.slots.size()
                || !(player.playerScreenHandler.getSlot(index) instanceof JewelrySlot jewel)) {
            return;
        }
        ItemStack stack = packet.stack();
        if (stack.isEmpty() || (stack.getCount() == 1 && jewel.canInsert(stack))) {
            jewel.setStack(stack);
            player.playerScreenHandler.sendContentUpdates();
        }
    }
}
