package com.nowa.block;

import com.nowa.world.LanternBeacons;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


/**
 * La lanterne de cobalt : une lanterne ordinaire — même lumière, mêmes créatures tenues à distance par
 * cette lumière — qui sert en plus de balise. Qui porte une lampe frontale la voit luire à travers
 * la roche, de loin.
 *
 * <p>Le bloc ne fait que tenir le répertoire des lanternes à jour ({@link LanternBeacons}) ; c'est
 * {@link com.nowa.headlamp.LanternSight} qui les montre. Pose et casse passent toutes deux par ici, d'où que vienne
 * le geste.
 */
public class CobaltLanternBlock extends LanternBlock {
    public CobaltLanternBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        LanternBeacons.add(world, pos);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (!newState.isOf(this)) {
            LanternBeacons.remove(world, pos);
        }
    }
}
