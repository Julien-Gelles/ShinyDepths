package com.nowa.mixin;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Les objets que le rendu de la main croit tenir. Il les compare à chaque tic à ceux réellement en
 * main, et abaisse puis remonte la main quand ils diffèrent : en changer un, c'est déclencher ce
 * mouvement sans toucher à l'objet.
 */
@Mixin(HeldItemRenderer.class)
public interface HeldItemRendererAccessor {
    @Accessor("mainHand")
    void shinyDepths$setMainHand(ItemStack stack);

    @Accessor("offHand")
    void shinyDepths$setOffHand(ItemStack stack);
}
