package com.nowa.block;

import com.nowa.block.entity.LapidaryTableBlockEntity;
import com.nowa.block.entity.ModBlockEntities;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Table de lapidaire : polit les pierres brutes de son niveau et des niveaux inférieurs,
 * sans combustible, comme un four (brut à gauche, poli à droite).
 *
 * <p>C'est un établi de quatre blocs (voir {@link BenchBlock}) : un meuble sur pieds en bas, le plan
 * de travail et ce qu'il porte au-dessus. Le contenu, la progression et l'XP vivent dans sa base ; les
 * trois autres parties ne font que renvoyer vers elle, pour l'écran comme pour les entonnoirs.
 */
public class LapidaryTableBlock extends BenchBlock implements InventoryProvider {
    /** La meule tourne-t-elle ? Ne sert qu'à la partie qui la porte, pour l'animer. */
    public static final BooleanProperty WORKING = BooleanProperty.of("working");

    public static final MapCodec<LapidaryTableBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(LapidaryTableBlock::getLevel),
            createSettingsCodec()
    ).apply(instance, LapidaryTableBlock::new));

    /**
     * La forme de l'établi : un escalier d'avant en arrière, sur toute la largeur de chaque bloc. Le
     * meuble est plein jusqu'au plan de travail ; au-dessus, à gauche, une marche basse commence
     * juste avant le bras de la meule, puis une haute au panneau du fond ; à droite, une seule,
     * juste avant le meuble à tiroirs. Boîtes dans le repère de l'établi entier.
     */
    private static final Map<Part, Map<Direction, VoxelShape>> SHAPES = shapes(new double[][]{
            {0, 0, 0, 32, 16, 16},                      // le meuble, jusqu'au plan de travail
            {16, 16, 4.5, 32, 19.5, 16},                // à gauche : du bras de la meule jusqu'au fond
            {16, 16, 13.5, 32, 28, 16},                 // … puis le panneau du fond
            {0, 16, 11, 16, 28, 16},                    // à droite : le meuble à tiroirs
    });

    private final int level;

    public LapidaryTableBlock(int level, Settings settings) {
        super(settings);
        this.level = level;
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH).with(PART, Part.BASE).with(WORKING, false));
    }

    public int getLevel() {
        return level;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, WORKING);
    }

    /** La table cassée lâche son contenu. */
    @Override
    protected void onBaseRemoved(World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof LapidaryTableBlockEntity table) {
            ItemScatterer.spawn(world, pos, table);
            world.updateComparators(pos, this);
        }
    }

    /**
     * Casser la table donne l'XP qu'elle contenait encore, comme le four — quelle que soit la partie
     * cassée.
     */
    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos base = basePos(pos, state);
        if (player instanceof ServerPlayerEntity serverPlayer
                && world.getBlockEntity(base) instanceof LapidaryTableBlockEntity table) {
            table.grantStoredXp(serverPlayer);
        }
        return super.onBreak(world, pos, state, player);
    }

    // ---------- forme et dessin ----------

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shape(SHAPES, state);
    }

    // ---------- contenu ----------

    /** Seule la base porte le contenu. */
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.get(PART) == Part.BASE ? new LapidaryTableBlockEntity(pos, state) : null;
    }

    /** Toutes les parties ouvrent le même écran, celui de la base. */
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && world.getBlockEntity(basePos(pos, state)) instanceof LapidaryTableBlockEntity table) {
            player.openHandledScreen(table);
        }
        return ActionResult.success(world.isClient);
    }

    /**
     * Les quatre parties donnent accès au même contenu, celui de la base : un entonnoir se branche
     * donc où l'on veut — par-dessus pour apporter les pierres brutes, par-dessous pour reprendre
     * les sachets — sans avoir à viser le bon bloc.
     */
    @Nullable
    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        return world.getBlockEntity(basePos(pos, state)) instanceof LapidaryTableBlockEntity table ? table : null;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(basePos(pos, state)));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : validateTicker(type, ModBlockEntities.LAPIDARY_TABLE, LapidaryTableBlockEntity::tick);
    }
}
