package com.nowa.dynamite.client;

import com.nowa.dynamite.DynamiteEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/**
 * La dynamite en vol : son modèle en volume, qui tournoie sur lui-même comme un bâton lancé. Le jeu
 * dessine ses projectiles lancés à plat, toujours face au joueur — bon pour une boule de neige, pas
 * pour un objet qui a une épaisseur.
 *
 * <p>C'est l'objet porté par l'entité qu'on dessine, mèche comprise : l'animation continue en vol.
 */
public class DynamiteEntityRenderer extends EntityRenderer<DynamiteEntity> {
    /** Degrés de culbute par tic. */
    private static final float SPIN = 28.0f;
    private static final float SCALE = 0.75f;

    private final ItemRenderer itemRenderer;

    public DynamiteEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(DynamiteEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        // les premiers instants, le bâton est encore dans le visage de celui qui le lance
        if (entity.age < 2 && dispatcher.camera.getFocusedEntity().squaredDistanceTo(entity) < 12.25) {
            return;
        }
        matrices.push();
        matrices.translate(0.0, 0.125, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(
                MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((entity.age + tickDelta) * SPIN));
        matrices.scale(SCALE, SCALE, SCALE);
        itemRenderer.renderItem(entity.getStack(), ModelTransformationMode.NONE, light, OverlayTexture.DEFAULT_UV,
                matrices, vertexConsumers, entity.getWorld(), entity.getId());
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(DynamiteEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}
