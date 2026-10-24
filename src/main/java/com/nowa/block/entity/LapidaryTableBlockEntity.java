package com.nowa.block.entity;

import com.nowa.block.LapidaryTableBlock;
import com.nowa.item.GemPolishing;
import com.nowa.item.GeodeContents;
import com.nowa.item.ModItems;
import com.nowa.item.StonePouchItem;
import com.nowa.lapidary.Lapidary;
import com.nowa.lapidary.LapidaryLevels;
import com.nowa.screen.LapidaryTableScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
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
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
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
 * Entonnoirs (comme le four) : pierres brutes par le haut, sachets vides par les côtés, sortie par le dessous.
 * Sortie : un sachet à pierres qui se remplit (64 pierres d'un même type au plus). Il faut un sachet vide pour
 * en commencer un ; un sachet n'est consommé que pour un nouveau sachet de sortie, pas à chaque pierre.
 * Les pierres n'y ont pas encore de pureté : elle est tirée quand un joueur récupère le sachet (voir StonePouchItem).
 * Un entonnoir ne prend le sachet que s'il est plein ou si la table ne peut plus le compléter.
 * XP de lapidaire : stockée dans la table et donnée au joueur qui retire les pierres polies (comme l'XP du four).
 */
public class LapidaryTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos>, SidedInventory {
    public static final int INPUT = 0;
    public static final int POUCH = 1;
    public static final int OUTPUT = 2;
    private static int baseTicks = 200;

    static {
        com.nowa.config.ModConfig.onApply(() ->
                baseTicks = com.nowa.config.ModConfig.i("lapidary.table.base_ticks", 200));
    }
    private static final int GEODE_TIER = GemPolishing.GEODE_TIER;   // une géode ne s'ouvre qu'à la table en diamant
    private static final int POUCH_CAPACITY = 64;                // un sachet contient 64 pierres, comme un bundle
    private static final int[] TOP = {INPUT};
    private static final int[] SIDES = {POUCH};
    private static final int[] BOTTOM = {OUTPUT};

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(3, ItemStack.EMPTY);
    private int progress;
    private int maxProgress;
    private int storedXp;
    /** L'empreinte de ce qui était posé sur le plan la dernière fois qu'on l'a envoyé au client. */
    private int lastShown;

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

    /**
     * La meule est sur la partie posée au-dessus de la base : c'est son état qu'on change pour
     * qu'elle tourne pendant le polissage et s'arrête ensuite.
     */
    private static void setWheelSpinning(World world, BlockPos pos, BlockState state, boolean spinning) {
        BlockPos top = pos.up();
        BlockState wheel = world.getBlockState(top);
        if (wheel.isOf(state.getBlock()) && wheel.get(LapidaryTableBlock.WORKING) != spinning) {
            world.setBlockState(top, wheel.with(LapidaryTableBlock.WORKING, spinning), Block.NOTIFY_LISTENERS);
        }
    }

    /**
     * Ce qu'on voit posé sur le plan de travail vient du contenu de la table, que le client ne
     * connaît pas tant que l'écran est fermé. On le lui envoie quand ce qui se voit a changé — la
     * pierre à polir, la présence d'un sachet, ce que le sachet de sortie contient — et pas à chaque
     * tic de polissage.
     */
    private void syncDisplay() {
        ItemStack input = items.get(INPUT);
        int shown = java.util.Objects.hash(input.isEmpty() ? null : input.getItem(),
                items.get(POUCH).isEmpty(), ItemStack.hashCode(items.get(OUTPUT)));
        if (shown != lastShown && world != null) {
            lastShown = shown;
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    public static void tick(World world, BlockPos pos, BlockState state, LapidaryTableBlockEntity table) {
        table.syncDisplay();
        ItemStack input = table.items.get(INPUT);
        boolean geode = table.canOpenGeode(input);
        Item polished = GemPolishing.polishedFor(input, table.getLevel());
        if (!geode && (polished == null || !table.canOutput(polished))) {
            setWheelSpinning(world, pos, state, false);
            if (table.progress != 0) {
                table.progress = 0;
                markDirty(world, pos, state);
            }
            return;
        }
        setWheelSpinning(world, pos, state, true);
        int tier = geode ? GEODE_TIER : GemPolishing.tierOf(input);
        table.maxProgress = baseTicks >> (table.getLevel() - tier);
        table.progress++;
        if (table.progress % 40 == 1) {
            world.playSound(null, pos, SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.25f, 1.3f);
        }
        if (table.progress >= table.maxProgress) {
            table.storedXp += geode ? LapidaryLevels.geodeXp() : LapidaryLevels.polishXp(tier);
            input.decrement(1);
            if (geode) {
                table.openGeode(world);
            } else {
                table.store(polished);
            }
            table.progress = 0;
            world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 0.6f, 1.2f);
        }
        markDirty(world, pos, state);
    }

    /** Range une pierre polie dans le sachet de sortie, en en entamant un s'il le faut. */
    private void store(Item polished) {
        ItemStack output = items.get(OUTPUT);
        if (output.isEmpty()) {
            items.set(OUTPUT, StonePouchItem.fill(items.get(POUCH), polished, 1));
            items.get(POUCH).decrement(1);
        } else {
            StonePouchItem.addUnappraised(output, polished, 1);
        }
    }

    /** Une géode donne d'un coup un sachet entier : quatre pierres tirées au hasard, doublons compris. */
    private void openGeode(World world) {
        ItemStack pouch = StonePouchItem.fill(items.get(POUCH), GeodeContents.draw(world.getRandom()), 1);
        for (int draw = 1; draw < GeodeContents.draws(); draw++) {
            StonePouchItem.addMixedUnappraised(pouch, GeodeContents.draw(world.getRandom()), 1);
        }
        items.set(OUTPUT, pouch);
        items.get(POUCH).decrement(1);
    }

    /**
     * Une géode s'ouvre entièrement d'un coup : il lui faut donc une sortie libre et un sachet vide,
     * là où une pierre polie peut compléter un sachet déjà entamé.
     */
    private boolean canOpenGeode(ItemStack input) {
        return input.isOf(ModItems.GEODE) && getLevel() >= GEODE_TIER
                && items.get(OUTPUT).isEmpty() && StonePouchItem.isEmptyPouch(items.get(POUCH));
    }

    /** Donne au joueur l'XP accumulée par la table (retrait des pierres polies, ou table cassée). */
    public void grantStoredXp(ServerPlayerEntity player) {
        if (storedXp > 0) {
            Lapidary.addXp(player, storedXp);
            storedXp = 0;
            markDirty();
        }
    }

    /**
     * Peut-on ranger cette pierre ? Soit dans le sachet de sortie (même pierre, non expertisé, pas plein),
     * soit dans un nouveau sachet s'il y a un sachet vide en attente.
     */
    private boolean canOutput(Item polished) {
        ItemStack output = items.get(OUTPUT);
        if (output.isEmpty()) {
            return StonePouchItem.isEmptyPouch(items.get(POUCH));
        }
        int count = polished == null ? -1 : StonePouchItem.unappraisedCount(output, polished);
        return count >= 0 && count < POUCH_CAPACITY;
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
        nbt.putInt("stored_xp", storedXp);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        // la lecture ne remplit que les cases écrites : on vide d'abord, sans quoi le client garderait
        // à l'image ce qu'on vient de retirer
        items.clear();
        Inventories.readNbt(nbt, items, registries);
        progress = nbt.getInt("progress");
        storedXp = nbt.getInt("stored_xp");
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
        return switch (slot) {
            case INPUT -> GemPolishing.canPolish(stack, getLevel())
                    || (stack.isOf(ModItems.GEODE) && getLevel() >= GEODE_TIER);
            case POUCH -> StonePouchItem.isEmptyPouch(stack);
            default -> false;
        };
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return switch (side) {
            case DOWN -> BOTTOM;
            case UP -> TOP;
            default -> SIDES;
        };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return isValid(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (slot != OUTPUT) {
            return false;
        }
        // sinon l'entonnoir emporterait un sac par pierre polie
        boolean full = StonePouchItem.contents(stack).stream().mapToInt(ItemStack::getCount).sum() >= POUCH_CAPACITY;
        return full || !canOutput(GemPolishing.polishedFor(items.get(INPUT), getLevel()));
    }
}
