package com.nowa.mixin;

import com.nowa.item.client.GoldenGlint;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Donne aux pierres philosophales un reflet doré. Le choix de la couche de reflet se fait dans une
 * méthode statique qui ne connaît pas l'objet rendu : on note donc la pierre au début du rendu,
 * et on substitue la couche juste après. Hors de ce cas, tout suit le chemin d'origine.
 */
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At("HEAD"))
    private void nowasDepths$markPhilosophal(ItemStack stack, ModelTransformationMode mode, boolean leftHanded,
                                             MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                             int light, int overlay, BakedModel model, CallbackInfo info) {
        GoldenGlint.begin(stack);
    }

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At("RETURN"))
    private void nowasDepths$clearPhilosophal(ItemStack stack, ModelTransformationMode mode, boolean leftHanded,
                                              MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                              int light, int overlay, BakedModel model, CallbackInfo info) {
        GoldenGlint.end();
    }

    /** Chemin du rendu en trois dimensions : objet en main, posé au sol, dans un cadre. */
    @Inject(method = "getItemGlintConsumer", at = @At("HEAD"), cancellable = true)
    private static void nowasDepths$goldenGlint(VertexConsumerProvider provider, RenderLayer layer, boolean solid,
                                                boolean glint, CallbackInfoReturnable<VertexConsumer> info) {
        if (glint && GoldenGlint.isActive()) {
            GoldenGlint.ensureBuffers(provider);
            RenderLayer golden = solid ? GoldenGlint.ITEM : GoldenGlint.ENTITY;
            info.setReturnValue(VertexConsumers.union(provider.getBuffer(golden), provider.getBuffer(layer)));
        }
    }

    /** Chemin du rendu à plat : c'est celui de l'inventaire, et il passe par une autre méthode. */
    @Inject(method = "getDirectItemGlintConsumer", at = @At("HEAD"), cancellable = true)
    private static void nowasDepths$goldenDirectGlint(VertexConsumerProvider provider, RenderLayer layer, boolean solid,
                                                      boolean glint, CallbackInfoReturnable<VertexConsumer> info) {
        if (glint && GoldenGlint.isActive()) {
            GoldenGlint.ensureBuffers(provider);
            RenderLayer golden = solid ? GoldenGlint.ITEM : GoldenGlint.DIRECT_ENTITY;
            info.setReturnValue(VertexConsumers.union(provider.getBuffer(golden), provider.getBuffer(layer)));
        }
    }
}
