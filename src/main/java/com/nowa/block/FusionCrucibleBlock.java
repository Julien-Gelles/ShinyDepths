package com.nowa.block;

import com.mojang.serialization.MapCodec;
import com.nowa.block.entity.FusionCrucibleBlockEntity;
import com.nowa.block.entity.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.StateManager;
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
 * Le creuset de fusion : on y fond quatre pierres identiques en une pierre de la pureté au-dessus
 * (voir {@link com.nowa.item.GemFusion}). C'est un établi de quatre blocs, le troisième du métier
 * (voir {@link BenchBlock}) : à gauche le plan de travail et ses quatre coupelles, à droite un four
 * sous le plan et le creuset dessus, sous sa hotte. Sa base tient le contenu ; cassé, il le lâche.
 */
public class FusionCrucibleBlock extends BenchBlock {
    public static final MapCodec<FusionCrucibleBlock> CODEC = createCodec(FusionCrucibleBlock::new);
    /**
     * La forme, en escalier comme les autres établis : le meuble plein jusqu'au plan de travail ; à
     * gauche une marche basse sur les coupelles puis une haute au panneau du fond ; à droite, du
     * creuset jusqu'à la corniche. Boîtes dans le repère de l'établi entier.
     */
    private static final Map<Part, Map<Direction, VoxelShape>> SHAPES = shapes(new double[][]{
            {0, 0, 0, 32, 16, 16},                      // le meuble, jusqu'au plan de travail
            {16, 16, 3, 32, 17, 16},                    // à gauche : une marche basse dès les coupelles…
            {16, 16, 13.5, 32, 28, 16},                 // … puis une haute au panneau du fond
            {0, 16, 3, 16, 28, 16},                     // à droite : le creuset et sa hotte
    });

    public FusionCrucibleBlock(Settings settings) {
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

    /** Le creuset cassé lâche ce qu'il contenait. */
    @Override
    protected void onBaseRemoved(World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof FusionCrucibleBlockEntity crucible) {
            ItemScatterer.spawn(world, pos, crucible);
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.get(PART) == Part.BASE ? new FusionCrucibleBlockEntity(pos, state) : null;
    }

    /** Celui qui pose le creuset en devient le lapidaire : la machine applique son savoir. */
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        if (placer instanceof PlayerEntity player && world.getBlockEntity(pos) instanceof FusionCrucibleBlockEntity crucible) {
            crucible.remember(player);
        }
    }

    /** Toutes les parties ouvrent l'écran de la base. */
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            NamedScreenHandlerFactory factory = createScreenHandlerFactory(state, world, pos);
            if (factory != null) {
                player.openHandledScreen(factory);
            }
        }
        return ActionResult.success(world.isClient);
    }

    @Nullable
    @Override
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return world.getBlockEntity(basePos(pos, state)) instanceof FusionCrucibleBlockEntity crucible ? crucible : null;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(basePos(pos, state)));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : validateTicker(type, ModBlockEntities.FUSION_CRUCIBLE, FusionCrucibleBlockEntity::tick);
    }
}
