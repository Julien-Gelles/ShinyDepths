package com.nowa.block.entity;

import com.nowa.block.GemShowcaseBlock;
import com.nowa.item.GemPolishing;
import com.nowa.item.Purity;
import com.nowa.villager.LapidaryVillager;
import com.nowa.screen.GemShowcaseScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Ce qu'une vitrine à pierres expose, et à qui elle appartient.
 *
 * <p>Les pierres ne sont <em>pas</em> un inventaire au sens du jeu : la vitrine garde les siennes
 * dans un rangement privé, que seul son écran sait ouvrir. Un entonnoir posé dessous ne peut donc
 * rien y prendre — ce serait la façon la plus simple de vider la vitrine d'un autre.
 *
 * <p>Elle retient qui l'a posée. Lui seul range et reprend les pierres ; les autres ouvrent le même
 * écran pour regarder. Une vitrine sans propriétaire — posée par une commande, ou avant que cette
 * mémoire n'existe — est ouverte à tous.
 *
 * <p>Quand un villageois lapidaire en fait son poste de travail, elle devient son <em>étal</em> : elle
 * se garnit de pierres qui ne sont qu'un décor. Elles n'existent nulle part comme objets : la vitrine
 * ne garde qu'un nombre, tiré à l'arrivée du villageois, dont le décor se déduit. On ne peut donc ni
 * les prendre, ni les faire tomber en cassant le bloc. Le villageois
 * parti, la vitrine redevient celle de son propriétaire. Seule une vitrine vide peut servir d'étal
 * (voir {@link GemShowcaseBlock#STOCKED}) : un villageois ne s'installe pas sur les pierres d'un joueur.
 */
public class GemShowcaseBlockEntity extends BlockEntity
        implements ExtendedScreenHandlerFactory<GemShowcaseScreenHandler.Opening> {
    public static final int COLUMNS = 5;
    public static final int ROWS = 3;
    public static final int SLOTS = COLUMNS * ROWS;

    /** Une pierre par case : on expose, on n'entrepose pas. */
    private final SimpleInventory gems = new SimpleInventory(SLOTS) {
        @Override
        public int getMaxCountPerStack() {
            return 1;
        }

        @Override
        public void markDirty() {
            super.markDirty();
            changed();
        }
    };
    private @Nullable UUID owner;
    private String ownerName = "";
    /** Un villageois lapidaire en a fait son étal. */
    private boolean tended;
    /** Ce dont le décor de l'étal est tiré : un nouveau nombre à chaque villageois qui s'installe. */
    private long decorationSeed;
    /** Le décor de l'étal, calculé une fois par tirage. */
    private @Nullable List<ItemStack> decoration;

    public GemShowcaseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GEM_SHOWCASE, pos, state);
    }

    /** Ce qui a sa place sous la vitrine : les pierres polies du mod, et les gemmes de n'importe qui. */
    public static boolean accepts(ItemStack stack) {
        return GemPolishing.isPolished(stack.getItem()) || stack.isIn(ConventionalItemTags.GEMS);
    }

    public SimpleInventory gems() {
        return gems;
    }

    /** Ce qu'on voit dans cette case : la pierre du propriétaire, ou le décor de l'étal. */
    public ItemStack gem(int slot) {
        return tended ? decoration().get(slot) : gems.getStack(slot);
    }

    public boolean isTended() {
        return tended;
    }

    /**
     * Les pierres de décor d'un étal. Elles se déduisent d'un seul nombre, tiré quand le villageois
     * s'installe : le serveur et le client en tirent le même décor, et il ne change pas tant que ce
     * villageois reste. Deux cases sur trois sont garnies, de pierres ternes, limpides ou pures.
     */
    private List<ItemStack> decoration() {
        if (decoration == null) {
            List<Item> kinds = GemPolishing.polishedGems();
            Purity[] purities = {Purity.DULL, Purity.CLEAR, Purity.PURE};
            java.util.Random random = new java.util.Random(decorationSeed);
            List<ItemStack> made = new ArrayList<>();
            for (int slot = 0; slot < SLOTS; slot++) {
                Item kind = kinds.get(random.nextInt(kinds.size()));
                Purity purity = purities[random.nextInt(purities.length)];
                made.add(random.nextInt(3) == 0 ? ItemStack.EMPTY : GemPolishing.polishedStack(kind, purity));
            }
            decoration = List.copyOf(made);
        }
        return decoration;
    }

    /** Jusqu'où chercher le villageois qui a pris ce poste : il ne s'éloigne guère de son village. */
    private static final double KEEPER_RANGE = 64.0;

    /**
     * Une fois par seconde : un villageois a-t-il pris ce poste, ou l'a-t-il quitté ? Le jeu ne
     * prévient pas le bloc ; on regarde donc si son poste de travail est occupé.
     *
     * <p>Le jeu donne un métier en deux temps : le villageois <em>réserve</em> le poste, puis marche
     * jusqu'à lui et ne devient lapidaire qu'à moins de deux blocs. La réservation prend déjà la
     * place sur le poste ; l'étal n'apparaît donc que lorsqu'un villageois lapidaire a vraiment ce
     * poste pour poste de travail, et disparaît quand la place se libère. Un villageois parti trop
     * loin ou dans un tronçon déchargé ne défait pas l'étal : seule la place libérée le fait.
     */
    public static void tick(World world, BlockPos pos, BlockState state, GemShowcaseBlockEntity showcase) {
        if (world.getTime() % 20 != 0 || !(world instanceof ServerWorld server)) {
            return;
        }
        PointOfInterestStorage posts = server.getPointOfInterestStorage();
        boolean taken = posts.hasTypeAt(LapidaryVillager.JOB_SITE_KEY, pos) && posts.getFreeTickets(pos) == 0;
        boolean occupied = taken && (showcase.tended || hasKeeper(server, pos));
        if (occupied != showcase.tended) {
            showcase.tended = occupied;
            if (occupied) {
                // un nouvel étal, un nouveau décor : reposer le bloc au même endroit n'y change rien
                showcase.decorationSeed = world.getRandom().nextLong();
                showcase.decoration = null;
            }
            showcase.changed();
        }
    }

    /** Un villageois lapidaire a-t-il ce bloc pour poste de travail — pas seulement réservé ? */
    private static boolean hasKeeper(ServerWorld world, BlockPos pos) {
        GlobalPos site = GlobalPos.create(world.getRegistryKey(), pos);
        return !world.getEntitiesByClass(VillagerEntity.class, new Box(pos).expand(KEEPER_RANGE),
                villager -> villager.getVillagerData().getProfession() == LapidaryVillager.PROFESSION
                        && villager.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE)
                        .filter(site::equals).isPresent()).isEmpty();
    }

    public void setOwner(PlayerEntity player) {
        owner = player.getUuid();
        ownerName = player.getGameProfile().getName();
        markDirty();
    }

    /** Cette vitrine est-elle à ce joueur ? Une vitrine sans propriétaire est à tout le monde. */
    public boolean belongsTo(PlayerEntity player) {
        return owner == null || owner.equals(player.getUuid());
    }

    /** Personne ne touche à l'étal d'un villageois ; sinon, la vitrine est à celui qui l'a posée. */
    public boolean canEdit(PlayerEntity player) {
        return !tended && belongsTo(player);
    }

    /** L'autre moitié de cette vitrine, si elle est double. */
    @Nullable
    public GemShowcaseBlockEntity partner() {
        return world == null ? null : GemShowcaseBlock.partner(world, pos, getCachedState());
    }

    /** Le contenu a changé : on le sauvegarde, et on le montre à ceux qui regardent le bloc. */
    private void changed() {
        markDirty();
        if (world == null || world.isClient) {
            return;
        }
        // l'état du bloc dit si la vitrine porte les pierres d'un joueur : c'est ce qui la retire,
        // ou la rend, aux villageois en quête d'un poste
        BlockState state = getCachedState();
        boolean stocked = !gems.isEmpty();
        if (state.contains(GemShowcaseBlock.STOCKED) && state.get(GemShowcaseBlock.STOCKED) != stocked) {
            world.setBlockState(pos, state.with(GemShowcaseBlock.STOCKED, stocked), Block.NOTIFY_ALL);
        }
        world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
    }

    // ---------- écran ----------

    @Override
    public GemShowcaseScreenHandler.Opening getScreenOpeningData(ServerPlayerEntity player) {
        GemShowcaseBlockEntity other = partner();
        return new GemShowcaseScreenHandler.Opening(canEdit(player) && (other == null || other.canEdit(player)),
                shownOwner(other), tended, other != null);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (tended) {
            // l'étal : l'écran montre le décor, dans un rangement à part que rien ne relie à la vitrine
            SimpleInventory shown = new SimpleInventory(SLOTS);
            for (int slot = 0; slot < SLOTS; slot++) {
                shown.setStack(slot, decoration().get(slot).copy());
            }
            return new GemShowcaseScreenHandler(syncId, playerInventory, shown, null, false, ownerName, true, this, null);
        }
        GemShowcaseBlockEntity other = partner();
        if (other == null) {
            return new GemShowcaseScreenHandler(syncId, playerInventory, gems, null, canEdit(player), ownerName,
                    false, this, null);
        }
        // une vitrine double : les pierres des deux moitiés, la gauche d'abord, telles qu'on les voit de face
        boolean left = getCachedState().get(GemShowcaseBlock.HALF) == GemShowcaseBlock.Half.LEFT;
        return new GemShowcaseScreenHandler(syncId, playerInventory, left ? gems : other.gems,
                left ? other.gems : gems, canEdit(player) && other.canEdit(player), shownOwner(other), false,
                this, other);
    }

    /** Le nom affiché : le sien, ou celui de l'autre moitié si cette vitrine-ci n'est à personne. */
    private String shownOwner(@Nullable GemShowcaseBlockEntity other) {
        return ownerName.isEmpty() && other != null ? other.ownerName : ownerName;
    }

    // ---------- sauvegarde, et envoi au client pour le dessin ----------

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        Inventories.writeNbt(nbt, gems.getHeldStacks(), registries);
        if (owner != null) {
            nbt.putUuid("owner", owner);
            nbt.putString("owner_name", ownerName);
        }
        nbt.putBoolean("tended", tended);
        nbt.putLong("decoration", decorationSeed);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        gems.getHeldStacks().clear();
        Inventories.readNbt(nbt, gems.getHeldStacks(), registries);
        owner = nbt.containsUuid("owner") ? nbt.getUuid("owner") : null;
        ownerName = nbt.getString("owner_name");
        tended = nbt.getBoolean("tended");
        if (decorationSeed != nbt.getLong("decoration")) {
            decorationSeed = nbt.getLong("decoration");
            decoration = null;
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
}
