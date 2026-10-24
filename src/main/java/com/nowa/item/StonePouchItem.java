package com.nowa.item;

import com.nowa.block.entity.LapidaryTableBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.Text;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ClickType;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Sachet à pierres (1 cuir, teignable) : 4 cases, qui n'acceptent que les pierres et minerais
 * (tag {@code nowas-depths:pouch_storable}). Il fonctionne comme le bundle :
 * <ul>
 *   <li>dans l'inventaire, clic droit pour y ranger un objet ou en sortir le dernier rangé ;</li>
 *   <li>clic droit en main : il jette tout son contenu devant soi.</li>
 * </ul>
 * Vides, les sachets s'empilent par 16 ; ranger un objet sur une pile de sachets vides le remplace
 * par un sachet qui le contient. Un sachet non vide est limité à 1 par case.
 * <p>
 * Vide, il se place dans la table de lapidaire, qui le remplit de pierres polies sans pureté. Dès qu'un joueur
 * récupère ou manipule le sachet, ces pierres reçoivent leur pureté selon son Coup d'œil.
 */
public class StonePouchItem extends Item {
    private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4f, 0.4f, 1.0f);

    @Nullable
    private final DyeColor color;

    public StonePouchItem(@Nullable DyeColor color, Settings settings) {
        super(settings);
        this.color = color;
    }

    @Nullable
    public DyeColor getColor() {
        return color;
    }

    // ---------- contenu ----------

    public static PouchContents contents(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.POUCH_CONTENTS, PouchContents.EMPTY);
    }

    /** Vide : sans donnée, empilable par 16 ; non vide : 1 par case (sinon le contenu serait partagé par la pile). */
    public static void setContents(ItemStack pouch, List<ItemStack> stacks) {
        PouchContents contents = new PouchContents(stacks);
        if (contents.isEmpty()) {
            pouch.remove(ModDataComponents.POUCH_CONTENTS);
            restoreStackSize(pouch);
        } else {
            pouch.set(ModDataComponents.POUCH_CONTENTS, contents);
            pouch.set(DataComponentTypes.MAX_STACK_SIZE, 1);
        }
    }

    /**
     * Rend au sachet la taille de pile de son objet. On la <em>remet</em>, on ne la retire pas :
     * retirer une donnée que l'objet porte d'origine laisse une marque « retirée » sur la pile, qui
     * la limite à 1 et l'empêche de s'empiler avec un sachet neuf.
     */
    private static void restoreStackSize(ItemStack pouch) {
        pouch.set(DataComponentTypes.MAX_STACK_SIZE, pouch.getDefaultComponents().get(DataComponentTypes.MAX_STACK_SIZE));
    }

    public static boolean isEmptyPouch(ItemStack stack) {
        return stack.getItem() instanceof StonePouchItem && contents(stack).isEmpty();
    }

    /** Pierres, minerais, lingots… (jamais un sachet). */
    public static boolean canStore(ItemStack stack) {
        return !stack.isEmpty() && stack.isIn(ModItemTags.POUCH_STORABLE) && !(stack.getItem() instanceof StonePouchItem);
    }

    /** Combien d'exemplaires de {@code stack} les 4 cases peuvent encore accueillir. */
    private static int roomFor(List<ItemStack> stacks, ItemStack stack) {
        int room = (PouchContents.SLOTS - stacks.size()) * stack.getMaxCount();
        for (ItemStack existing : stacks) {
            if (ItemStack.areItemsAndComponentsEqual(existing, stack)) {
                room += existing.getMaxCount() - existing.getCount();
            }
        }
        return room;
    }

    /** Range une copie de {@code stack} (complète les piles identiques, puis une case libre) ; renvoie le nombre rangé. */
    private static int insert(List<ItemStack> stacks, ItemStack stack) {
        int remaining = Math.min(stack.getCount(), roomFor(stacks, stack));
        int inserted = remaining;
        for (ItemStack existing : stacks) {
            if (remaining > 0 && ItemStack.areItemsAndComponentsEqual(existing, stack)) {
                int moved = Math.min(remaining, existing.getMaxCount() - existing.getCount());
                existing.increment(moved);
                remaining -= moved;
            }
        }
        if (remaining > 0) {
            stacks.add(0, stack.copyWithCount(remaining));
        }
        return inserted;
    }

    /** Un sachet vide détaché de la pile, contenant {@code stack}. */
    private static ItemStack pouchWith(ItemStack emptyPouches, ItemStack stack) {
        ItemStack pouch = emptyPouches.copyWithCount(1);
        setContents(pouch, List.of(stack.copy()));
        return pouch;
    }

    // ---------- table de lapidaire ----------

    /** Sachet rempli par la table à partir d'un sachet vide (la couleur est conservée). */
    public static ItemStack fill(ItemStack emptyPouch, Item gem, int count) {
        return pouchWith(emptyPouch, GemPolishing.unappraised(gem, count));
    }

    /** Sachet rempli d'une pierre déjà expertisée : sa pureté est conservée. */
    public static ItemStack fillWith(ItemStack emptyPouch, ItemStack stone) {
        return pouchWith(emptyPouch, stone.copyWithCount(1));
    }

    /** Ajoute une pierre déjà expertisée : même pile si elle est identique, nouvelle case sinon. */
    public static boolean addStack(ItemStack pouch, ItemStack stone) {
        List<ItemStack> stacks = new ArrayList<>(contents(pouch).stacks());
        for (int slot = 0; slot < stacks.size(); slot++) {
            if (ItemStack.areItemsAndComponentsEqual(stacks.get(slot), stone)) {
                ItemStack merged = stacks.get(slot).copy();
                merged.increment(1);
                stacks.set(slot, merged);
                setContents(pouch, stacks);
                return true;
            }
        }
        if (stacks.size() >= PouchContents.SLOTS) {
            return false;
        }
        stacks.add(stone.copyWithCount(1));
        setContents(pouch, stacks);
        return true;
    }

    /**
     * Nombre de pierres polies sans pureté du type {@code gem}, si le sachet ne contient que ça ; -1 sinon.
     * La table n'ajoute des pierres qu'à un sachet dans cet état.
     */
    public static int unappraisedCount(ItemStack pouch, Item gem) {
        PouchContents contents = contents(pouch);
        if (contents.size() != 1) {
            return -1;
        }
        ItemStack stones = contents.get(0);
        return stones.isOf(gem) && needsAppraisal(stones) ? stones.getCount() : -1;
    }

    /**
     * La pierre d'un sachet qui n'en contient qu'une sorte, toutes puretés confondues ; {@code null}
     * s'il est vide ou s'il en mêle plusieurs. C'est ce qui lui donne son nom et sa pastille.
     */
    public static Item singleGem(ItemStack pouch) {
        PouchContents contents = contents(pouch);
        if (contents.size() == 0) {
            return null;
        }
        Item gem = contents.get(0).getItem();
        return contents.stream().allMatch(stones -> stones.isOf(gem)) ? gem : null;
    }

    /**
     * Les pierres d'un sachet qui n'en contient qu'un type, et pas encore estimées ; vide sinon.
     * L'écran de la table s'en sert pour montrer la pierre plutôt que le sachet qui l'emballe.
     */
    public static ItemStack singleUnappraised(ItemStack pouch) {
        PouchContents contents = contents(pouch);
        if (contents.size() != 1) {
            return ItemStack.EMPTY;
        }
        ItemStack stones = contents.get(0);
        return needsAppraisal(stones) ? stones.copy() : ItemStack.EMPTY;
    }

    public static void addUnappraised(ItemStack pouch, Item gem, int count) {
        int current = unappraisedCount(pouch, gem);
        if (current >= 0) {
            setContents(pouch, List.of(GemPolishing.unappraised(gem, current + count)));
        }
    }

    /**
     * Ajoute une pierre non expertisée en ouvrant une case si son type est nouveau, là où
     * {@link #addUnappraised} n'accepte que d'épaissir la pile déjà présente. C'est ce qu'il faut
     * pour une géode, dont les quatre pierres sont rarement du même type.
     *
     * @return faux si les quatre cases sont déjà prises par d'autres pierres
     */
    public static boolean addMixedUnappraised(ItemStack pouch, Item gem, int count) {
        List<ItemStack> stacks = new ArrayList<>(contents(pouch).stacks());
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack stones = stacks.get(slot);
            if (stones.isOf(gem) && needsAppraisal(stones)) {
                stacks.set(slot, GemPolishing.unappraised(gem, stones.getCount() + count));
                setContents(pouch, stacks);
                return true;
            }
        }
        if (stacks.size() >= PouchContents.SLOTS) {
            return false;
        }
        stacks.add(GemPolishing.unappraised(gem, count));
        setContents(pouch, stacks);
        return true;
    }

    /** Une pierre polie dont personne n'a encore révélé la pureté. */
    public static boolean needsAppraisal(ItemStack stack) {
        return GemPolishing.needsAppraisal(stack);
    }

    /**
     * Révèle la pureté des pierres polies du sachet avec le Coup d'œil de ce joueur (une seule fois par pierre).
     * Une pile se répartit en jusqu'à 4 classes ; ce qui ne tient plus dans les 4 cases va au joueur.
     */
    public static void appraise(ItemStack pouch, PlayerEntity player) {
        PouchContents contents = contents(pouch);
        if (player.getWorld().isClient || contents.stream().noneMatch(StonePouchItem::needsAppraisal)) {
            return;
        }
        List<ItemStack> appraised = new ArrayList<>();
        for (ItemStack stack : contents.stacks()) {
            if (needsAppraisal(stack)) {
                // une case ne tient qu'une pile : le surplus ira au joueur avec le reste
                appraised.addAll(Appraisal.appraise(stack.getItem(), stack.getCount(), player));
            } else {
                appraised.add(stack);
            }
        }
        while (appraised.size() > PouchContents.SLOTS) {
            player.getInventory().offerOrDrop(appraised.remove(appraised.size() - 1));
        }
        setContents(pouch, appraised);
    }

    // ---------- comportement du bundle ----------

    // sachet tenu par la souris, clic droit sur une case : range l'objet de la case (ou y sort le dernier rangé)
    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.RIGHT || slot.inventory instanceof LapidaryTableBlockEntity) {
            return false;
        }
        ItemStack target = slot.getStack();
        if (stack.getCount() > 1) {
            // pile de sachets vides : l'objet de la case devient un sachet qui le contient
            if (!isEmptyPouch(stack) || !canStore(target) || !slot.canTakeItems(player)) {
                return false;
            }
            ItemStack pouch = pouchWith(stack, target);
            if (!slot.canInsert(pouch)) {
                return false;
            }
            slot.setStack(pouch);
            stack.decrement(1);
            playSound(player, SoundEvents.ITEM_BUNDLE_INSERT);
            return true;
        }
        appraise(stack, player);
        List<ItemStack> stacks = contents(stack).stacks();
        if (target.isEmpty()) {
            if (stacks.isEmpty()) {
                return false;
            }
            ItemStack rest = slot.insertStack(stacks.remove(0));
            if (!rest.isEmpty()) {
                stacks.add(0, rest);
            } else {
                playSound(player, SoundEvents.ITEM_BUNDLE_REMOVE_ONE);
            }
        } else if (canStore(target)) {
            int room = roomFor(stacks, target);
            if (room > 0) {
                insert(stacks, slot.takeStackRange(target.getCount(), room, player));
                playSound(player, SoundEvents.ITEM_BUNDLE_INSERT);
            }
        } else {
            return false;
        }
        setContents(stack, stacks);
        return true;
    }

    // objet tenu par la souris, clic droit sur le sachet : range l'objet (ou sort le dernier rangé si main vide)
    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType,
                             PlayerEntity player, StackReference cursorStackReference) {
        if (clickType != ClickType.RIGHT || !slot.canTakePartial(player) || slot.inventory instanceof LapidaryTableBlockEntity) {
            return false;
        }
        if (stack.getCount() > 1) {
            // pile de sachets vides : l'objet tenu devient un sachet qui le contient
            if (!isEmptyPouch(stack) || !canStore(otherStack)) {
                return false;
            }
            cursorStackReference.set(pouchWith(stack, otherStack));
            stack.decrement(1);
            playSound(player, SoundEvents.ITEM_BUNDLE_INSERT);
            return true;
        }
        appraise(stack, player);
        List<ItemStack> stacks = contents(stack).stacks();
        if (otherStack.isEmpty()) {
            if (stacks.isEmpty()) {
                return false;
            }
            cursorStackReference.set(stacks.remove(0));
            playSound(player, SoundEvents.ITEM_BUNDLE_REMOVE_ONE);
        } else if (canStore(otherStack)) {
            int inserted = insert(stacks, otherStack);
            if (inserted > 0) {
                otherStack.decrement(inserted);
                playSound(player, SoundEvents.ITEM_BUNDLE_INSERT);
            }
        } else {
            return false;
        }
        setContents(stack, stacks);
        return true;
    }

    // clic droit en main : jette tout le contenu devant soi
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (stack.getCount() > 1 || contents(stack).isEmpty()) {
            return TypedActionResult.fail(stack);
        }
        if (!world.isClient) {
            appraise(stack, user);
            for (ItemStack content : contents(stack).stacks()) {
                user.dropItem(content, true);
            }
            setContents(stack, List.of());
        }
        playSound(user, SoundEvents.ITEM_BUNDLE_DROP_CONTENTS);
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof ServerPlayerEntity player) {
            appraise(stack, player);
            // les sachets vidés avant la correction portent encore la marque qui les limite à 1
            if (contents(stack).isEmpty() && stack.getMaxCount() != getMaxCount()) {
                restoreStackSize(stack);
            }
        }
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        ItemUsage.spawnItemContents(entity, contents(entity.getStack()).stacks());
    }

    /** « Sachet de rubis » dès qu'il ne tient qu'une sorte de pierre ; son nom d'origine sinon. */
    @Override
    public Text getName(ItemStack stack) {
        Item gem = singleGem(stack);
        return gem == null ? super.getName(stack)
                : Text.translatable("item.nowas-depths.stone_pouch.of", new ItemStack(gem).getName());
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return Optional.of(new StonePouchTooltipData(contents(stack)));
    }

    // barre de remplissage : nombre de cases occupées sur 4
    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return !contents(stack).isEmpty();
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.min(1 + 12 * contents(stack).size() / PouchContents.SLOTS, 13);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return ITEM_BAR_COLOR;
    }

    private static void playSound(Entity entity, SoundEvent sound) {
        entity.playSound(sound, 0.8f, 0.8f + entity.getWorld().getRandom().nextFloat() * 0.4f);
    }

    /** Données de l'infobulle (4 cases, comme le bundle), dessinée côté client. */
    public record StonePouchTooltipData(PouchContents contents) implements TooltipData {
    }
}
