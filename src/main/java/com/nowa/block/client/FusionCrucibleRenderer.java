package com.nowa.block.client;

import com.nowa.block.FusionCrucibleBlock;
import com.nowa.block.entity.FusionCrucibleBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

/**
 * Ce que contient le creuset de fusion, à sa place sur le meuble : chaque pierre dans sa coupelle,
 * à plat, et la pierre fondue sur le bain du creuset. Les places sont dans le repère de l'établi
 * entier (voir le script des modèles) : x de 0 à 32 vu de face, la base occupant 16 à 32, et z de 0
 * (la façade) à 16.
 */
public class FusionCrucibleRenderer implements BlockEntityRenderer<FusionCrucibleBlockEntity> {
    /** Le milieu de chaque coupelle, dans l'ordre des cases de l'écran : en haut à gauche, en haut à droite, en bas… */
    private static final float[][] CUPS = {{26.5f, 10.5f}, {20.5f, 10.5f}, {26.5f, 5.0f}, {20.5f, 5.0f}};
    /** Le dessus des coupelles, et le bain du creuset. */
    private static final float CUP_Y = 16.6f;
    private static final float BATH_X = 8.0f, BATH_Y = 22.6f, BATH_Z = 8.0f;
    private static final float GEM_SCALE = GemShowcaseRenderer.SCALE;

    private final ItemRenderer items;

    public FusionCrucibleRenderer(BlockEntityRendererFactory.Context context) {
        this.items = context.getItemRenderer();
    }

    @Override
    public void render(FusionCrucibleBlockEntity crucible, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = crucible.getCachedState();
        World world = crucible.getWorld();
        if (world == null || !(state.getBlock() instanceof FusionCrucibleBlock)
                || state.get(FusionCrucibleBlock.PART) != FusionCrucibleBlock.Part.BASE) {
            return;
        }
        int above = WorldRenderer.getLightmapCoordinates(world, crucible.getPos().up());
        matrices.push();
        // le modèle est tourné par l'état du bloc autour de son centre : on fait de même
        matrices.translate(0.5, 0.0, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - state.get(FusionCrucibleBlock.FACING).asRotation()));
        matrices.translate(-0.5, 0.0, -0.5);
        for (int cup = 0; cup < CUPS.length; cup++) {
            lay(crucible.getStack(FusionCrucibleBlockEntity.FIRST_INPUT + cup), CUPS[cup][0], CUP_Y, CUPS[cup][1],
                    world, above, matrices, vertexConsumers);
        }
        // la pierre fondue luit dans son bain : pleine lumière
        lay(crucible.getStack(FusionCrucibleBlockEntity.OUTPUT), BATH_X, BATH_Y, BATH_Z, world, 0xF000F0, matrices, vertexConsumers);
        matrices.pop();
    }

    /** Pose une pierre à plat, à cet endroit de l'établi (en pixels). */
    private void lay(ItemStack gem, float x, float y, float z, World world, int light, MatrixStack matrices,
                     VertexConsumerProvider vertexConsumers) {
        if (gem.isEmpty()) {
            return;
        }
        matrices.push();
        matrices.translate((x - 16.0f) / 16.0f, y / 16.0f, z / 16.0f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        matrices.scale(GEM_SCALE, GEM_SCALE, GEM_SCALE);
        items.renderItem(gem, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, 0);
        matrices.pop();
    }

    /** Le creuset est hors du bloc de la base, à côté et au-dessus : on ne s'y limite pas. */
    @Override
    public boolean rendersOutsideBoundingBox(FusionCrucibleBlockEntity crucible) {
        return true;
    }
}
