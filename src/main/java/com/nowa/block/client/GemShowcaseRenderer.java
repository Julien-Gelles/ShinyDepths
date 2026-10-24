package com.nowa.block.client;

import com.nowa.block.GemShowcaseBlock;
import com.nowa.block.entity.GemShowcaseBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

/**
 * Les pierres exposées dans la vitrine : celles que son propriétaire y a rangées, chacune à la place
 * de sa case, posées à plat sur le feutre et inclinées avec le plateau.
 *
 * <p>Les cotes sont celles du modèle (voir le script qui l'écrit) : le plateau tourne de 22,5° autour
 * du centre du bloc, son feutre est à 14 pixels de haut avant inclinaison, et les pierres se
 * répartissent entre les montants de la vitrine.
 */
public class GemShowcaseRenderer implements BlockEntityRenderer<GemShowcaseBlockEntity> {
    private static final float TILT = -22.5f;
    private static final float FELT = 14.0f / 16.0f;
    /** L'espace entre les montants, avant inclinaison, en pixels : de gauche à droite, d'avant en arrière. */
    private static final float X0 = 1.5f;
    private static final float X1 = 14.5f;
    private static final float Z0 = 4.5f;
    private static final float Z1 = 14.5f;
    public static final float SCALE = 0.15f;

    private final ItemRenderer itemRenderer;

    public GemShowcaseRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(GemShowcaseBlockEntity showcase, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = showcase.getCachedState();
        if (showcase.getWorld() == null || !(state.getBlock() instanceof GemShowcaseBlock)) {
            return;
        }
        matrices.push();
        // le modèle est tourné par l'état du bloc autour de son centre, puis son dessus est incliné
        // autour de ce même centre : on fait les deux, dans cet ordre
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - state.get(GemShowcaseBlock.FACING).asRotation()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(TILT));
        matrices.translate(-0.5, -0.5, -0.5);

        // dans une vitrine double, les pierres vont jusqu'au bord du bloc du côté de la jonction : les
        // dix colonnes sont alors régulièrement espacées d'un bout à l'autre du meuble
        GemShowcaseBlock.Half half = state.get(GemShowcaseBlock.HALF);
        float x0 = half == GemShowcaseBlock.Half.LEFT ? 0.0f : X0;
        float x1 = half == GemShowcaseBlock.Half.RIGHT ? 16.0f : X1;
        float stepX = (x1 - x0) / GemShowcaseBlockEntity.COLUMNS;
        float stepZ = (Z1 - Z0) / GemShowcaseBlockEntity.ROWS;
        int seed = (int) showcase.getPos().asLong();
        for (int slot = 0; slot < GemShowcaseBlockEntity.SLOTS; slot++) {
            ItemStack gem = showcase.gem(slot);
            if (gem.isEmpty()) {
                continue;
            }
            int col = slot % GemShowcaseBlockEntity.COLUMNS;
            int row = slot / GemShowcaseBlockEntity.COLUMNS;
            // vu de face, la première colonne est à gauche : c'est le côté des x élevés du modèle ; et
            // la première rangée, en haut de l'écran, est au fond de la vitrine
            float x = x1 - (col + 0.5f) * stepX;
            float z = Z1 - (row + 0.5f) * stepZ;
            matrices.push();
            matrices.translate(x / 16.0f, FELT + 0.01f, z / 16.0f);
            // à plat, face vers le haut, et le haut de l'objet vers le fond de la vitrine : c'est le
            // sens dans lequel le lit celui qui se tient devant
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
            matrices.scale(SCALE, SCALE, SCALE);
            itemRenderer.renderItem(gem, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV,
                    matrices, vertexConsumers, showcase.getWorld(), seed + slot);
            matrices.pop();
        }
        matrices.pop();
    }
}
