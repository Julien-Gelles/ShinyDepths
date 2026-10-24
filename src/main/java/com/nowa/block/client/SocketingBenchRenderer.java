package com.nowa.block.client;

import com.nowa.block.SocketingBenchBlock;
import com.nowa.block.entity.SocketingBenchBlockEntity;
import com.nowa.item.ModItems;
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

import java.util.List;
import java.util.stream.Stream;

/**
 * Ce que l'établi de sertisseur montre de son contenu. Sur le présentoir de droite, la bague — ou la
 * lampe frontale — qu'on est en train de sertir, inclinée sur ses trois petits pics, face à celui
 * qui regarde. Sous le poinçon de la machine, la pierre de l'établi. Et sur le plateau, trois pierres
 * de décor, toujours là.
 *
 * <p>Les places sont données dans le repère de l'établi entier (voir le script des modèles) : x de 0
 * à 32 vu de face, la base occupant 16 à 32, et z de 0 (la façade) à 16.
 */
public class SocketingBenchRenderer implements BlockEntityRenderer<SocketingBenchBlockEntity> {
    /**
     * L'objet présenté : posé sur le pic du bas du présentoir, adossé aux deux du haut, incliné vers
     * l'arrière de cet angle.
     */
    private static final float SHOWN_X = 8.0f, SHOWN_Y = 19.4f, SHOWN_Z = 7.6f, SHOWN_TILT = 40.0f;
    /** La taille de l'objet présenté : une bague à la mesure du doigt. */
    private static final float SHOWN_SCALE = 0.26f;

    /** Le milieu du dessus de l'enclumette de la machine : la pierre y est posée à plat. */
    private static final float GEM_X = 25.5f, GEM_Y = 18.5f, GEM_Z = 7.5f;
    /** La taille des pierres de la vitrine à pierres : partout, une pierre posée a cette taille. */
    private static final float GEM_SCALE = GemShowcaseRenderer.SCALE;

    /**
     * Les pierres du plateau, jetées là en désordre : où est chacune, et de quel angle elle est
     * tournée. La disposition a été calculée pour qu'aucune pierre n'en touche une autre ni le bord
     * (le dessin de chaque pierre, pixel par pixel) : on ne la retouche pas à l'œil.
     */
    private static final List<ItemStack> TRAY_GEMS = Stream.of(ModItems.PERIDOT, ModItems.MOONSTONE, ModItems.GARNET)
            .map(ItemStack::new).toList();
    private static final float[][] TRAY_SPOTS = {{18.28f, 4.26f, -14.0f}, {18.29f, 6.74f, 23.0f}, {18.61f, 9.17f, -40.0f}};
    /** Le fond du plateau, juste au-dessus du plan de travail. */
    private static final float TRAY_FLOOR = 16.3f;

    private final ItemRenderer items;

    public SocketingBenchRenderer(BlockEntityRendererFactory.Context context) {
        this.items = context.getItemRenderer();
    }

    @Override
    public void render(SocketingBenchBlockEntity bench, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = bench.getCachedState();
        World world = bench.getWorld();
        if (world == null || !(state.getBlock() instanceof SocketingBenchBlock)
                || state.get(SocketingBenchBlock.PART) != SocketingBenchBlock.Part.BASE) {
            return;
        }
        int above = WorldRenderer.getLightmapCoordinates(world, bench.getPos().up());
        int aside = WorldRenderer.getLightmapCoordinates(world, bench.getPos().offset(SocketingBenchBlock.aside(state)).up());

        matrices.push();
        // le modèle est tourné par l'état du bloc autour de son centre : on fait de même
        matrices.translate(0.5, 0.0, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(
                180.0f - state.get(SocketingBenchBlock.FACING).asRotation()));
        matrices.translate(-0.5, 0.0, -0.5);
        show(bench.shownGear(), world, aside, matrices, vertexConsumers);
        lay(bench.shownGem(), GEM_X, GEM_Y + 0.1f, GEM_Z, 0.0f, world, above, matrices, vertexConsumers);
        // le plateau à pierres : trois vraies pierres, toujours là — c'est un décor, pas un contenu
        for (int index = 0; index < TRAY_GEMS.size(); index++) {
            float[] spot = TRAY_SPOTS[index];
            lay(TRAY_GEMS.get(index), spot[0], TRAY_FLOOR, spot[1], spot[2], world, above, matrices, vertexConsumers);
        }
        matrices.pop();
    }

    /** L'objet en cours de travail, debout sur le présentoir, tourné vers la façade. */
    private void show(ItemStack shown, World world, int light, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        if (shown.isEmpty()) {
            return;
        }
        matrices.push();
        matrices.translate((SHOWN_X - 16.0f) / 16.0f, SHOWN_Y / 16.0f, SHOWN_Z / 16.0f);
        // tournée vers la façade, puis le haut renversé vers le fond
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(SHOWN_TILT));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        matrices.scale(SHOWN_SCALE, SHOWN_SCALE, SHOWN_SCALE);
        items.renderItem(shown, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, 0);
        matrices.pop();
    }

    /** Pose une pierre à plat, à cet endroit de l'établi (en pixels), tournée de cet angle. */
    private void lay(ItemStack gem, float x, float y, float z, float turn, World world, int light,
                     MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        if (gem.isEmpty()) {
            return;
        }
        matrices.push();
        matrices.translate((x - 16.0f) / 16.0f, y / 16.0f, z / 16.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(turn));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        matrices.scale(GEM_SCALE, GEM_SCALE, GEM_SCALE);
        items.renderItem(gem, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices,
                vertexConsumers, world, 0);
        matrices.pop();
    }

    /** Le présentoir est hors du bloc de la base, à côté : on ne s'y limite pas. */
    @Override
    public boolean rendersOutsideBoundingBox(SocketingBenchBlockEntity bench) {
        return true;
    }
}
