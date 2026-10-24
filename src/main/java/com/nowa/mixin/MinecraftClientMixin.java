package com.nowa.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nowa.item.LoupeItem;
import com.nowa.powers.client.PowersClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Le clic droit du joueur. Le jeu essaie, main après main, la créature visée, le bloc visé, puis
 * l'objet tenu, et s'arrête au premier qui fait quelque chose ; s'il arrive au bout, rien n'a rien
 * fait — c'est le geste du rappel et de la poche de l'End. Une main vide n'envoie rien au serveur :
 * c'est donc ici, côté client, qu'on le voit.
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "doItemUse", at = @At("TAIL"))
    private void shinyDepths$idleUse(CallbackInfo info) {
        PowersClient.idleUse((MinecraftClient) (Object) this);
    }

    /**
     * Après un clic droit réussi, le jeu remet d'un coup l'objet utilisé tout en bas. Pas pour la
     * loupe : elle s'abaisse et remonte en douceur quand son usure change, au même rythme que la
     * pierre qu'elle vient d'estimer dans l'autre main. En créatif, où elle ne s'use pas, on lui
     * fait faire le même mouvement.
     */
    @WrapOperation(method = "doItemUse", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;resetEquipProgress(Lnet/minecraft/util/Hand;)V"))
    private void shinyDepths$smoothLoupe(HeldItemRenderer renderer, Hand hand, Operation<Void> original) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        ItemStack held = client.player == null ? ItemStack.EMPTY : client.player.getStackInHand(hand);
        if (held.getItem() instanceof LoupeItem) {
            if (client.player.isInCreativeMode()) {
                // en créatif la loupe ne s'use pas : rien ne change sur elle, et le jeu ne la
                // bougerait pas. On montre au rendu une copie marquée, qu'il croit différente.
                ItemStack shown = held.copy();
                shown.set(DataComponentTypes.REPAIR_COST, held.getOrDefault(DataComponentTypes.REPAIR_COST, 0) + 1);
                HeldItemRendererAccessor accessor = (HeldItemRendererAccessor) renderer;
                if (hand == Hand.MAIN_HAND) {
                    accessor.shinyDepths$setMainHand(shown);
                } else {
                    accessor.shinyDepths$setOffHand(shown);
                }
            }
            return;
        }
        original.call(renderer, hand);
    }
}
