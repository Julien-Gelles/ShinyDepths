package com.nowa.headlamp;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

/**
 * La lumière de la lampe frontale : un bloc invisible, qu'on traverse et qu'on remplace comme de
 * l'air, et qui n'existe que pour éclairer. Le jeu n'a pas de lumière qui se déplace ; on en pose
 * une à la hauteur de la tête du joueur, et on la déplace avec lui.
 *
 * <p>Elle ne doit jamais survivre à sa lampe. Toutes les secondes, elle vérifie qu'une lampe la tient
 * encore et disparaît sinon — y compris après un arrêt brutal du serveur : le rappel est sauvegardé
 * avec le chunk, et au rechargement plus aucune lampe ne la réclame.
 */
public class HeadlampLightBlock extends Block {
    /** Le délai entre deux vérifications, en tics. */
    public static final int CHECK_TICKS = 20;

    public HeadlampLightBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (HeadlampLight.isHeld(world, pos)) {
            world.scheduleBlockTick(pos, this, CHECK_TICKS);
        } else {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), HeadlampLight.FLAGS);
        }
    }
}
