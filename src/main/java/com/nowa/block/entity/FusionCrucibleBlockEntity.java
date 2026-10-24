package com.nowa.block.entity;

import com.nowa.config.ModConfig;
import com.nowa.item.GemFusion;
import com.nowa.lapidary.Lapidary;
import com.nowa.lapidary.LapidaryBonuses;
import com.nowa.lapidary.LapidarySkillTree;
import com.nowa.screen.FusionCrucibleScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Le creuset de fusion : quatre pierres identiques dans ses quatre coupelles, et au bout de quelques
 * secondes, une pierre de la pureté au-dessus dans le creuset (voir {@link GemFusion}). Il travaille
 * seul, comme un four, tant qu'il a de quoi : quatre piles de la même pierre font autant de fusions.
 *
 * <p>Un entonnoir peut le nourrir par le dessus et les côtés — il répartit les pierres sur les
 * quatre coupelles — et le vider par le dessous. Tout ce qu'il contient se voit sur le bloc : les
 * pierres dans leurs coupelles, et la pierre fondue dans le creuset.
 */
public class FusionCrucibleBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory {
    public static final int FIRST_INPUT = 0, OUTPUT = GemFusion.COUNT, SIZE = GemFusion.COUNT + 1;
    private static final int[] INPUTS = {0, 1, 2, 3};
    private static final int[] OUTPUTS = {OUTPUT};
    private static final String ITEMS = "items", PROGRESS = "progress", FAST = "fast_fusion", RECOVERY = "recovery",
            RECOVERY_COUNT = "recovery_count", RECOVERY_CHANCE = "recovery_chance";
    private static final Text TITLE = Text.translatable("container.nowas-depths.fusion_crucible");

    private static int fusionTicks = 100;

    static {
        ModConfig.onApply(() -> fusionTicks = Math.max(1, (int) Math.round(ModConfig.d("gems.fusion_seconds", 5.0) * 20.0)));
    }

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
    private int progress;
    /**
     * Les rangs de Fusion rapide et de Récupération du dernier lapidaire à avoir posé ou ouvert le
     * creuset : c'est son savoir que la machine applique, même quand des entonnoirs la nourrissent.
     */
    private int fastRank, recoveryRank, recoveryCountRank, recoveryChanceRank;

    /** Ce que l'écran suit : l'avancée de la fusion en cours, et sa durée. */
    private final PropertyDelegate properties = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return index == 0 ? progress : fusionTicks();
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = value;
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public FusionCrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_CRUCIBLE, pos, state);
    }

    // ---------- le travail ----------

    /** La pierre que la fusion en cours donnera, ou vide si les coupelles ne permettent pas d'en faire une. */
    private ItemStack planned() {
        ItemStack first = items.get(FIRST_INPUT);
        if (!GemFusion.fusible(first)) {
            return ItemStack.EMPTY;
        }
        for (int slot = FIRST_INPUT + 1; slot < OUTPUT; slot++) {
            if (!GemFusion.same(items.get(slot), first)) {
                return ItemStack.EMPTY;
            }
        }
        ItemStack result = GemFusion.result(first);
        ItemStack out = items.get(OUTPUT);
        boolean room = out.isEmpty() || GemFusion.same(out, result) && out.getCount() < out.getMaxCount();
        return room ? result : ItemStack.EMPTY;
    }

    public static void tick(World world, BlockPos pos, BlockState state, FusionCrucibleBlockEntity crucible) {
        ItemStack result = crucible.planned();
        if (result.isEmpty()) {
            if (crucible.progress != 0) {
                crucible.progress = 0;
                crucible.markDirty();
            }
            return;
        }
        if (++crucible.progress < crucible.fusionTicks()) {
            return;
        }
        crucible.progress = 0;
        // la Récupération épargne parfois des pierres, tirées au hasard : elles restent dans leur coupelle
        boolean recovery = crucible.recoveryRank > 0;
        boolean[] spared = new boolean[GemFusion.COUNT];
        if (world.getRandom().nextDouble() < LapidaryBonuses.spareChance(recovery, crucible.recoveryChanceRank)) {
            List<Integer> order = new ArrayList<>(List.of(0, 1, 2, 3));
            net.minecraft.util.Util.shuffle(order, world.getRandom());
            int count = Math.min(LapidaryBonuses.spareCount(recovery, crucible.recoveryCountRank), GemFusion.COUNT);
            for (int i = 0; i < count; i++) {
                spared[order.get(i)] = true;
            }
        }
        for (int slot = FIRST_INPUT; slot < OUTPUT; slot++) {
            if (!spared[slot - FIRST_INPUT]) {
                crucible.items.get(slot).decrement(1);
            }
        }
        ItemStack out = crucible.items.get(OUTPUT);
        if (out.isEmpty()) {
            crucible.items.set(OUTPUT, result);
        } else {
            out.increment(1);
        }
        world.playSound(null, pos, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 0.8f, 1.2f);
        world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.0f, 1.0f);
        crucible.markDirty();
    }

    /** La durée d'une fusion ici : celle du réglage, raccourcie par la Fusion rapide du lapidaire retenu. */
    private int fusionTicks() {
        return Math.max(1, (int) Math.round(fusionTicks * LapidaryBonuses.fusionTime(fastRank)));
    }

    /** Ce lapidaire vient de poser ou d'ouvrir le creuset : c'est désormais son savoir qui compte. */
    public void remember(PlayerEntity player) {
        if (world == null || world.isClient || player.isSpectator()) {
            return;
        }
        fastRank = Lapidary.rank(player, LapidarySkillTree.Skill.FAST_FUSION);
        recoveryRank = Lapidary.rank(player, LapidarySkillTree.Skill.RECOVERY);
        recoveryCountRank = Lapidary.rank(player, LapidarySkillTree.Skill.RECOVERY_COUNT);
        recoveryChanceRank = Lapidary.rank(player, LapidarySkillTree.Skill.RECOVERY_CHANCE);
        markDirty();
    }

    /** Ce qui change se sauvegarde, et se montre à ceux qui regardent le bloc. */
    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    // ---------- l'inventaire ----------

    @Override
    public int size() {
        return SIZE;
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
        ItemStack taken = Inventories.splitStack(items, slot, amount);
        if (!taken.isEmpty()) {
            markDirty();
        }
        return taken;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack taken = Inventories.removeStack(items, slot);
        markDirty();
        return taken;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.capCount(getMaxCount(stack));
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return world != null && world.getBlockEntity(pos) == this
                && player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clear() {
        items.clear();
        markDirty();
    }

    /** Dans une coupelle, une pierre qui peut fusionner ; dans le creuset, rien qu'on y pose. */
    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return slot != OUTPUT && GemFusion.fusible(stack);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return side == Direction.DOWN ? OUTPUTS : INPUTS;
    }

    /**
     * Un entonnoir remplit les coupelles une à une : une pierre ne va dans une coupelle que si elle
     * n'est pas plus pleine que les autres, et toutes doivent tenir la même pierre. Sans cela, il
     * verserait tout dans la première.
     */
    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        if (slot == OUTPUT || !GemFusion.fusible(stack)) {
            return false;
        }
        int count = items.get(slot).getCount();
        for (int other = FIRST_INPUT; other < OUTPUT; other++) {
            ItemStack there = items.get(other);
            if (!there.isEmpty() && !GemFusion.same(there, stack)) {
                return false;
            }
            if (there.getCount() < count) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return slot == OUTPUT && side == Direction.DOWN;
    }

    // ---------- l'écran ----------

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
        remember(player);
        return new FusionCrucibleScreenHandler(syncId, inventory, this, properties,
                world == null ? ScreenHandlerContext.EMPTY : ScreenHandlerContext.create(world, pos));
    }

    // ---------- sauvegarde, et envoi au client pour le dessin ----------

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.put(ITEMS, Inventories.writeNbt(new NbtCompound(), items, registries));
        nbt.putInt(PROGRESS, progress);
        nbt.putInt(FAST, fastRank);
        nbt.putInt(RECOVERY, recoveryRank);
        nbt.putInt(RECOVERY_COUNT, recoveryCountRank);
        nbt.putInt(RECOVERY_CHANCE, recoveryChanceRank);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        items.clear();
        Inventories.readNbt(nbt.getCompound(ITEMS), items, registries);
        progress = nbt.getInt(PROGRESS);
        fastRank = nbt.getInt(FAST);
        recoveryRank = nbt.getInt(RECOVERY);
        recoveryCountRank = nbt.getInt(RECOVERY_COUNT);
        recoveryChanceRank = nbt.getInt(RECOVERY_CHANCE);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = new NbtCompound();
        nbt.put(ITEMS, Inventories.writeNbt(new NbtCompound(), items, true, registries));
        return nbt;
    }
}
