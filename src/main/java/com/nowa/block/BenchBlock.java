package com.nowa.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Un établi de deux blocs de large et deux de haut : celui du lapidaire, celui du sertisseur.
 *
 * <p>Quatre blocs, d'un seul tenant — on en pose un, les trois autres viennent avec ; on en casse un,
 * tout part. Ce que l'établi retient vit dans une seule de ces parties, {@link Part#BASE}, celle où
 * l'on a cliqué pour poser ; les trois autres ne font que renvoyer vers elle.
 */
public abstract class BenchBlock extends BlockWithEntity {
    /**
     * Les quatre parties de l'établi. Vu de face, la base est en bas à gauche et l'établi s'étend
     * vers la droite et vers le haut.
     */
    public enum Part implements StringIdentifiable {
        /** En bas à gauche : c'est elle qui tient ce que l'établi retient. */
        BASE("base", 0, 0),
        /** En bas à droite. */
        BASE_SIDE("base_side", 1, 0),
        /** Au-dessus de la base. */
        TOP("top", 0, 1),
        /** Au-dessus de la partie de droite. */
        TOP_SIDE("top_side", 1, 1);

        private final String name;
        private final int aside;
        private final int above;

        Part(String name, int aside, int above) {
            this.name = name;
            this.aside = aside;
            this.above = above;
        }

        @Override
        public String asString() {
            return name;
        }
    }

    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.of("part", Part.class);

    protected BenchBlock(Settings settings) {
        super(settings);
    }

    // ---------- les quatre parties ----------

    /** Vers où l'établi s'étend depuis sa base : la droite de celui qui lui fait face. */
    public static Direction aside(BlockState state) {
        return state.get(FACING).rotateYCounterclockwise();
    }

    /** Où se trouve cette partie d'un établi dont la base est là. */
    protected static BlockPos partPos(BlockPos base, BlockState state, Part part) {
        return base.offset(aside(state), part.aside).up(part.above);
    }

    /** La base de l'établi dont ce bloc fait partie. */
    public static BlockPos basePos(BlockPos pos, BlockState state) {
        Part part = state.get(PART);
        return pos.offset(aside(state), -part.aside).down(part.above);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
        World world = ctx.getWorld();
        for (Part part : Part.values()) {
            BlockPos pos = partPos(ctx.getBlockPos(), state, part);
            if (part != Part.BASE && (!world.isInBuildLimit(pos) || !world.getBlockState(pos).canReplace(ctx))) {
                return null;                    // pas la place des quatre blocs : on ne pose rien
            }
        }
        return state;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        if (world.isClient) {
            return;
        }
        for (Part part : Part.values()) {
            if (part != Part.BASE) {
                world.setBlockState(partPos(pos, state, part), state.with(PART, part), Block.NOTIFY_ALL);
            }
        }
    }

    /** La base disparaît : c'est le moment de lâcher ce que l'établi contenait. */
    protected void onBaseRemoved(World world, BlockPos pos) {
    }

    /**
     * Une partie qui disparaît emporte les autres. La base lâche le contenu et retire les trois
     * autres sans rien faire tomber ; une autre partie fait céder la base, qui donne l'objet — une
     * seule fois, quel que soit le bloc qu'on a cassé.
     */
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (state.get(PART) == Part.BASE) {
                onBaseRemoved(world, pos);
                for (Part part : Part.values()) {
                    BlockPos other = partPos(pos, state, part);
                    if (part != Part.BASE && world.getBlockState(other).isOf(this)) {
                        world.setBlockState(other, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
                    }
                }
            } else {
                BlockPos base = basePos(pos, state);
                BlockState baseState = world.getBlockState(base);
                if (baseState.isOf(this) && baseState.get(PART) == Part.BASE) {
                    world.breakBlock(base, true);
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    /**
     * En créatif, rien ne tombe : on retire la base sans butin avant que le jeu ne retire le bloc
     * visé.
     */
    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos base = basePos(pos, state);
        if (!world.isClient && player.isCreative() && !base.equals(pos) && world.getBlockState(base).isOf(this)) {
            world.setBlockState(base, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
        }
        return super.onBreak(world, pos, state, player);
    }

    // ---------- forme ----------

    /**
     * La forme de chaque partie, pour chaque orientation, à partir de boîtes décrites une seule fois
     * dans le repère de l'établi entier vu de face — celui des scripts de modèles : x de 0 à 32 (la
     * base occupe 16 à 32), y de 0 à 32, z de 0 (la façade) à 16. Une boîte vaut
     * {@code {x0, y0, z0, x1, y1, z1}} ; elle est découpée sur les quatre blocs, puis tournée.
     */
    protected static Map<Part, Map<Direction, VoxelShape>> shapes(double[][] boxes) {
        Map<Part, Map<Direction, VoxelShape>> shapes = new EnumMap<>(Part.class);
        for (Part part : Part.values()) {
            double cellX = part.aside == 0 ? 16.0 : 0.0, cellY = 16.0 * part.above;
            Map<Direction, VoxelShape> turned = new EnumMap<>(Direction.class);
            for (Direction facing : Direction.Type.HORIZONTAL) {
                VoxelShape shape = VoxelShapes.empty();
                for (double[] box : boxes) {
                    // la part de cette boîte qui tombe dans ce bloc, dans le repère du bloc
                    double x0 = Math.max(box[0], cellX) - cellX, x1 = Math.min(box[3], cellX + 16.0) - cellX;
                    double y0 = Math.max(box[1], cellY) - cellY, y1 = Math.min(box[4], cellY + 16.0) - cellY;
                    double z0 = box[2], z1 = box[5];
                    if (x0 >= x1 || y0 >= y1 || z0 >= z1) {
                        continue;
                    }
                    shape = VoxelShapes.union(shape, switch (facing) {
                        case EAST -> Block.createCuboidShape(16.0 - z1, y0, x0, 16.0 - z0, y1, x1);
                        case SOUTH -> Block.createCuboidShape(16.0 - x1, y0, 16.0 - z1, 16.0 - x0, y1, 16.0 - z0);
                        case WEST -> Block.createCuboidShape(z0, y0, 16.0 - x1, z1, y1, 16.0 - x0);
                        default -> Block.createCuboidShape(x0, y0, z0, x1, y1, z1);
                    });
                }
                turned.put(facing, shape.simplify());
            }
            shapes.put(part, turned);
        }
        return shapes;
    }

    /** La forme de ce bloc-ci, parmi celles que {@link #shapes} a préparées. */
    protected static VoxelShape shape(Map<Part, Map<Direction, VoxelShape>> shapes, BlockState state) {
        return shapes.get(state.get(PART)).get(state.get(FACING));
    }

    // ---------- dessin ----------

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
