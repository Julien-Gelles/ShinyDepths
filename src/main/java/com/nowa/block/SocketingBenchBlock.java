package com.nowa.block;

import com.mojang.serialization.MapCodec;
import com.nowa.block.entity.SocketingBenchBlockEntity;
import com.nowa.screen.SocketingBenchScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * L'établi de sertisseur : c'est là qu'on sertit une bague ou une lampe frontale, et qu'on en reprend
 * la pierre. Il garde ce qu'on y laisse : on peut fermer l'écran et y revenir plus tard.
 *
 * <p>C'est un établi de quatre blocs (voir {@link BenchBlock}) : un plan de travail sur toute la
 * largeur, la machine à sertir à gauche, à droite un présentoir où se pose l'objet en cours de
 * travail. Sa base tient le contenu ; cassé, l'établi le lâche.
 */
public class SocketingBenchBlock extends BenchBlock {
    public static final MapCodec<SocketingBenchBlock> CODEC = createCodec(SocketingBenchBlock::new);
    private static final Text TITLE = Text.translatable("container.shiny-depths.socketing_bench");
    /**
     * La forme de l'établi : le meuble plein jusqu'au plan de travail, sur les deux blocs ; à gauche,
     * de la machine à sertir jusqu'au fond ; à droite, le présentoir à bague, puis le panneau du fond
     * jusqu'à la corniche. Boîtes dans le repère de l'établi entier.
     */
    private static final Map<Part, Map<Direction, VoxelShape>> SHAPES = shapes(new double[][]{
            {0, 0, 0, 32, 16, 16},                      // le meuble, jusqu'au plan de travail
            {16, 16, 4.5, 32, 28, 16},                  // à gauche : de la machine jusqu'au fond
            {0, 16, 5, 16, 21.5, 16},                   // à droite : une marche basse dès le présentoir…
            {0, 16, 13.5, 16, 28, 16},                  // … puis une haute au panneau du fond
    });

    public SocketingBenchBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(PART, Part.BASE));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shape(SHAPES, state);
    }

    /** L'établi cassé lâche ce qu'on y avait laissé. */
    @Override
    protected void onBaseRemoved(World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof SocketingBenchBlockEntity bench) {
            ItemScatterer.spawn(world, pos, bench.dropped());
        }
    }

    /** Seule la base tient le contenu. */
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.get(PART) == Part.BASE ? new SocketingBenchBlockEntity(pos, state) : null;
    }

    /** Toutes les parties ouvrent le même écran, celui de la base. */
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            player.openHandledScreen(createScreenHandlerFactory(state, world, pos));
        }
        return ActionResult.success(world.isClient);
    }

    @Override
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockPos base = basePos(pos, state);
        if (!(world.getBlockEntity(base) instanceof SocketingBenchBlockEntity bench)) {
            return null;
        }
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) ->
                new SocketingBenchScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, base), bench), TITLE);
    }
}
