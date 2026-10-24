package com.nowa.block.client;

import com.nowa.block.LapidaryTableBlock;
import com.nowa.block.entity.LapidaryTableBlockEntity;
import com.nowa.item.ModDataComponents;
import com.nowa.item.ModItems;
import com.nowa.item.PouchContents;
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
 * Ce qui est posé au milieu du plan de travail : la pierre en cours de polissage près de la scie, le
 * sachet au centre, et à côté de lui les pierres déjà polies. On lit d'un coup d'œil où en est la
 * table, sans ouvrir son écran.
 *
 * <p>Les objets sont dessinés à plat, dans le repère de l'établi entier (voir le script des
 * modèles) : x de 0 à 32 vu de face, la base occupant 16 à 32, et z de 0 (la façade) à 16.
 */
public class LapidaryTableRenderer implements BlockEntityRenderer<LapidaryTableBlockEntity> {
    /** Le plan de travail est au sommet du bloc du bas ; on pose les objets juste dessus. */
    private static final float PLANE = 1.0f + 0.4f / 16.0f;
    /** Une pierre, brute ou polie, a la taille de celles de la vitrine à pierres. */
    private static final float STONE_SCALE = GemShowcaseRenderer.SCALE;
    /** Deux sortes de pierres polies au plus : la place est comptée. */
    private static final float[][] POLISHED_SPOTS = {{11.0f, 4.2f, 25.0f}, {12.4f, 7.6f, -35.0f}};

    /**
     * Les pierres du plateau, jetées là en désordre : où est chacune, et de quel angle elle est
     * tournée. La disposition a été calculée pour qu'aucune pierre n'en touche une autre ni le bord
     * (le dessin de chaque pierre, pixel par pixel) : on ne la retouche pas à l'œil.
     */
    private static final List<ItemStack> TRAY_GEMS = Stream.of(ModItems.RUBY, ModItems.SAPPHIRE, ModItems.SUGILITE,
            ModItems.AMBER, ModItems.TANZANITE).map(ItemStack::new).toList();
    private static final float[][] TRAY_SPOTS = {{4.04f, 4.57f, 48.0f}, {7.21f, 4.38f, -47.0f},
            {5.65f, 6.24f, -15.0f}, {3.88f, 7.42f, 44.0f}, {7.15f, 7.67f, -28.0f}};
    /** Le fond du plateau, juste au-dessus du plan de travail. */
    private static final float TRAY_FLOOR = 1.0f + 0.3f / 16.0f;

    private final ItemRenderer itemRenderer;

    public LapidaryTableRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(LapidaryTableBlockEntity table, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = table.getCachedState();
        World world = table.getWorld();
        if (world == null || !(state.getBlock() instanceof LapidaryTableBlock)
                || state.get(LapidaryTableBlock.PART) != LapidaryTableBlock.Part.BASE) {
            return;
        }
        ItemStack raw = table.getStack(LapidaryTableBlockEntity.INPUT);
        ItemStack output = table.getStack(LapidaryTableBlockEntity.OUTPUT);
        ItemStack pouch = output.isEmpty() ? table.getStack(LapidaryTableBlockEntity.POUCH) : output;
        // la lumière du bloc du dessus : c'est là, à l'air libre, que les objets sont posés
        int lit = WorldRenderer.getLightmapCoordinates(world, table.getPos().up());
        int seed = (int) table.getPos().asLong();

        matrices.push();
        // le modèle est tourné par l'état du bloc autour de son centre : on fait de même
        matrices.translate(0.5, 0.0, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(
                180.0f - state.get(LapidaryTableBlock.FACING).asRotation()));
        matrices.translate(-0.5, 0.0, -0.5);

        lay(raw, 21.2f, 7.4f, 20.0f, STONE_SCALE, matrices, vertexConsumers, lit, world, seed);
        lay(pouch, 16.5f, 6.0f, -12.0f, 0.42f, matrices, vertexConsumers, lit, world, seed + 1);
        List<ItemStack> polished = output.getOrDefault(ModDataComponents.POUCH_CONTENTS, PouchContents.EMPTY).stacks();
        for (int index = 0; index < polished.size() && index < POLISHED_SPOTS.length; index++) {
            float[] spot = POLISHED_SPOTS[index];
            lay(polished.get(index), spot[0], spot[1], spot[2], STONE_SCALE, matrices, vertexConsumers, lit, world,
                    seed + 2 + index);
        }
        // le plateau à pierres : cinq vraies pierres, toujours là — c'est un décor, pas un contenu
        for (int index = 0; index < TRAY_GEMS.size(); index++) {
            float[] spot = TRAY_SPOTS[index];
            lay(TRAY_GEMS.get(index), spot[0], TRAY_FLOOR, spot[1], spot[2], STONE_SCALE, matrices, vertexConsumers,
                    lit, world, seed);
        }
        matrices.pop();
    }

    /** Pose un objet à plat sur le plan, à cet endroit de l'établi, tourné de cet angle. */
    private void lay(ItemStack stack, float x, float z, float turn, float scale, MatrixStack matrices,
                     VertexConsumerProvider vertexConsumers, int light, World world, int seed) {
        lay(stack, x, PLANE, z, turn, scale, matrices, vertexConsumers, light, world, seed);
    }

    /** Le même geste, à cette hauteur (en blocs depuis le pied de l'établi). */
    private void lay(ItemStack stack, float x, float height, float z, float turn, float scale, MatrixStack matrices,
                     VertexConsumerProvider vertexConsumers, int light, World world, int seed) {
        if (stack.isEmpty()) {
            return;
        }
        matrices.push();
        matrices.translate((x - 16.0f) / 16.0f, height, z / 16.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(turn));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        matrices.scale(scale, scale, scale);
        itemRenderer.renderItem(stack, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV,
                matrices, vertexConsumers, world, seed);
        matrices.pop();
    }

    /** Les objets sont posés hors du bloc de la base, au-dessus et à côté : on ne s'y limite pas. */
    @Override
    public boolean rendersOutsideBoundingBox(LapidaryTableBlockEntity table) {
        return true;
    }
}
