package com.nowa.block.entity;

import com.nowa.block.LapidaryTableBlock;
import com.nowa.item.GemPolishing;
import com.nowa.screen.LapidaryTableScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Contenu et progression d'une table de lapidaire.
 * Temps : 10 s pour une pierre du niveau de la table, divisé par 2 par niveau d'avance.
 * Entonnoirs : entrée par le haut et les côtés, sortie par le dessous (comme le four).
 */
public class LapidaryTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos>, SidedInventory {
    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    private static final int BASE_TICKS = 200;
    private static final int[] TOP_AND_SIDES = {INPUT};
    private static final int[] BOTTOM = {OUTPUT};

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private int progress;
    private int maxProgress;

    private final PropertyDelegate properties = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return index == 0 ? progress : maxProgress;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = value;
            } else {
                maxProgress = value;
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public LapidaryTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAPIDARY_TABLE, pos, state);
    }

    public int getLevel() {
        return getCachedState().getBlock() instanceof LapidaryTableBlock table ? table.getLevel() : 2;
    }

    public static void tick(World world, BlockPos pos, BlockState state, LapidaryTableBlockEntity table) {
        ItemStack input = table.items.get(INPUT);
        Item polished = GemPolishing.polishedFor(input, table.getLevel());
        if (polished == null || !table.canOutput(polished)) {
            if (table.progress != 0) {
                table.progress = 0;
                markDirty(world, pos, state);
            }
            return;
        }
        table.maxProgress = BASE_TICKS >> (table.getLevel() - GemPolishing.tierOf(input));
        table.progress++;
        if (table.progress % 40 == 1) {
            world.playSound(null, pos, SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.25f, 1.3f);
        }
        if (table.progress >= table.maxProgress) {
            input.decrement(1);
            ItemStack output = table.items.get(OUTPUT);
            if (output.isEmpty()) {
                table.items.set(OUTPUT, new ItemStack(polished));
            } else {
                output.increment(1);
            }
            table.progress = 0;
            world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 0.6f, 1.2f);
        }
        markDirty(world, pos, state);
    }

    private boolean canOutput(Item polished) {
        ItemStack output = items.get(OUTPUT);
        return output.isEmpty() || (output.isOf(polished) && output.getCount() < output.getMaxCount());
    }

    // ---------- interface ----------
    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return pos;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new LapidaryTableScreenHandler(syncId, playerInventory, this, properties);
    }

    // ---------- sauvegarde ----------
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        Inventories.writeNbt(nbt, items, registries);
        nbt.putInt("progress", progress);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        Inventories.readNbt(nbt, items, registries);
        progress = nbt.getInt("progress");
    }

    // ---------- inventaire ----------
    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack removed = Inventories.splitStack(items, slot, amount);
        if (!removed.isEmpty()) {
            markDirty();
        }
        return removed;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(items, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ItemStack previous = items.get(slot);
        boolean sameItem = !stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(previous, stack);
        items.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        if (slot == INPUT && !sameItem) {
            progress = 0;                                      // nouvelle pierre : on repart de zéro
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return slot == INPUT && GemPolishing.canPolish(stack, getLevel());
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return side == Direction.DOWN ? BOTTOM : TOP_AND_SIDES;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return isValid(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == OUTPUT;
    }
}
