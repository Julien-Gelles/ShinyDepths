package com.nowa.block;

import com.mojang.serialization.MapCodec;
import com.nowa.block.entity.GemShowcaseBlockEntity;
import com.nowa.block.entity.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

/**
 * La vitrine à pierres : un pupitre surmonté d'une vitrine de verre où l'on expose ses pierres.
 * C'est aussi le poste de travail du villageois lapidaire (voir {@code LapidaryVillager}).
 *
 * <p>Elle retient qui l'a posée : lui seul y range et y reprend des pierres. N'importe qui peut
 * l'ouvrir pour regarder. Les pierres exposées se voient sur le bloc lui-même. Vide, elle peut
 * devenir l'étal d'un villageois, qui la garnit d'un décor de pierres qu'on ne peut pas prendre.
 *
 * <p>Elle se tourne vers celui qui la pose, comme le pupitre du jeu : le plateau penche vers lui.
 */
public class GemShowcaseBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public static final MapCodec<GemShowcaseBlock> CODEC = createCodec(GemShowcaseBlock::new);
    /**
     * La vitrine porte-t-elle les pierres d'un joueur ? Une vitrine garnie n'est pas un poste de
     * travail : un villageois ne peut pas s'y installer et recouvrir ces pierres de son décor.
     */
    public static final BooleanProperty STOCKED = BooleanProperty.of("stocked");

    /**
     * Seule, ou moitié d'une vitrine double. Deux vitrines posées côte à côte, tournées du même côté,
     * n'en font qu'une, comme deux coffres : une seule grande vitrine de verre sur deux pieds, et un
     * écran de trente cases. La gauche et la droite sont celles de qui regarde la vitrine de face.
     */
    public enum Half implements StringIdentifiable {
        SINGLE("single"), LEFT("left"), RIGHT("right");

        private final String name;

        Half(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }

        private Half other() {
            return this == LEFT ? RIGHT : this == RIGHT ? LEFT : SINGLE;
        }
    }

    public static final EnumProperty<Half> HALF = EnumProperty.of("half", Half.class);

    /**
     * Les cotes du pupitre du jeu : un socle sur tout le bloc, un pied, et le volume du dessus
     * incliné avec sa vitrine. L'arrière de ce dessus dépasse un peu du bloc, pas sa forme.
     */
    private static final VoxelShape SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            Block.createCuboidShape(2.0, 2.0, 2.0, 14.0, 3.0, 14.0),
            Block.createCuboidShape(4.0, 3.0, 4.0, 12.0, 14.0, 12.0),
            Block.createCuboidShape(0.0, 9.0, 0.0, 16.0, 16.0, 16.0));

    public GemShowcaseBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(STOCKED, false)
                .with(HALF, Half.SINGLE));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, STOCKED, HALF);
    }

    // ---------- vitrine double ----------

    /** La droite de celui qui regarde une vitrine tournée de ce côté. */
    private static Direction toRight(Direction facing) {
        return facing.rotateYCounterclockwise();
    }

    /** De quel côté se trouve l'autre moitié de cette vitrine, ou {@code null} si elle est seule. */
    @Nullable
    private static Direction toPartner(BlockState state) {
        return switch (state.get(HALF)) {
            case LEFT -> toRight(state.get(FACING));
            case RIGHT -> toRight(state.get(FACING)).getOpposite();
            case SINGLE -> null;
        };
    }

    /** L'autre moitié de cette vitrine, si elle est double. */
    @Nullable
    public static GemShowcaseBlockEntity partner(World world, BlockPos pos, BlockState state) {
        Direction side = toPartner(state);
        return side != null && world.getBlockEntity(pos.offset(side)) instanceof GemShowcaseBlockEntity other
                ? other : null;
    }

    /**
     * Cette vitrine voisine peut-elle s'accoler à celle qu'on pose ? Il faut qu'elle soit seule,
     * tournée du même côté, à celui qui pose (ou à personne), et qu'aucun villageois n'en ait fait
     * son étal.
     */
    private boolean canJoin(ItemPlacementContext ctx, BlockPos neighbor, Direction facing) {
        BlockState state = ctx.getWorld().getBlockState(neighbor);
        return state.isOf(this) && state.get(HALF) == Half.SINGLE && state.get(FACING) == facing
                && ctx.getWorld().getBlockEntity(neighbor) instanceof GemShowcaseBlockEntity other
                && !other.isTended() && ctx.getPlayer() != null && other.belongsTo(ctx.getPlayer());
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
        BlockState state = getDefaultState().with(FACING, facing);
        if (ctx.shouldCancelInteraction()) {
            return state;                                   // accroupi : on la veut seule, comme un coffre
        }
        Direction right = toRight(facing);
        if (canJoin(ctx, ctx.getBlockPos().offset(right), facing)) {
            return state.with(HALF, Half.LEFT);
        }
        if (canJoin(ctx, ctx.getBlockPos().offset(right.getOpposite()), facing)) {
            return state.with(HALF, Half.RIGHT);
        }
        return state;
    }

    /**
     * Les deux moitiés se suivent : une vitrine seule épouse celle qu'on vient d'accoler, et une
     * moitié dont l'autre disparaît redevient une vitrine seule.
     */
    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                   WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        boolean sameWay = neighborState.isOf(this) && neighborState.get(FACING) == state.get(FACING);
        if (state.get(HALF) == Half.SINGLE) {
            if (sameWay && toPartner(neighborState) == direction.getOpposite()) {
                return state.with(HALF, neighborState.get(HALF).other());
            }
        } else if (toPartner(state) == direction
                && !(sameWay && neighborState.get(HALF) == state.get(HALF).other())) {
            return state.with(HALF, Half.SINGLE);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    // ---------- contenu ----------

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GemShowcaseBlockEntity(pos, state);
    }

    /** La vitrine regarde, une fois par seconde, si un villageois en a fait son étal. */
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient || type != ModBlockEntities.GEM_SHOWCASE) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<GemShowcaseBlockEntity>) GemShowcaseBlockEntity::tick;
    }

    /** La vitrine appartient à celui qui la pose. */
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        if (!world.isClient && placer instanceof PlayerEntity player
                && world.getBlockEntity(pos) instanceof GemShowcaseBlockEntity showcase) {
            showcase.setOwner(player);
        }
    }

    /** Tout le monde peut ouvrir l'écran ; c'est lui qui dit à qui il est permis de toucher. */
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && world.getBlockEntity(pos) instanceof GemShowcaseBlockEntity showcase) {
            player.openHandledScreen(showcase);
        }
        return ActionResult.success(world.isClient);
    }

    /** Une vitrine cassée rend ses pierres. */
    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof GemShowcaseBlockEntity showcase) {
            ItemScatterer.spawn(world, pos, showcase.gems());
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
