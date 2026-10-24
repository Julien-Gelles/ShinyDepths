package com.nowa.socket;

import com.nowa.headlamp.GemSense;
import com.nowa.item.GemPolishing;
import com.nowa.item.ModDataComponents;
import com.nowa.item.ModItemTags;
import com.nowa.powers.GemPower;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static com.nowa.NowasDepths.MOD_ID;

/**
 * Le sertissage : poser une pierre polie sur une bague ou une lampe frontale, et la retirer. Depuis
 * la refonte du 2026-10-14, ni niveau exigé ni risque : la pose et le retrait sont sûrs.
 *
 * <p>La bague n'a qu'un emplacement : sa pierre choisit le pouvoir, et sa pureté en fait le niveau
 * (voir {@link com.nowa.powers.Powers}). La lampe en a plusieurs ; chacune de ses pierres fait luire son
 * propre minerai.
 */
public final class Socketing {
    /** L'emplacement de la pierre principale : le premier de l'objet. */
    public static final int MAIN_CELL = 0;

    private Socketing() {
    }

    // ---------- lecture ----------

    public static Sockets get(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SOCKETS, Sockets.EMPTY);
    }

    /** Nombre d'emplacements de l'objet, lu dans les tags : 0 s'il ne se sertit pas. */
    public static int capacity(ItemStack stack) {
        if (SocketFamily.of(stack) == null) {
            return 0;
        }
        for (int count = ModItemTags.MAX_SOCKETS; count >= 1; count--) {
            if (stack.isIn(ModItemTags.SOCKETS[count])) {
                return count;
            }
        }
        return 0;
    }

    public static int freeSlots(ItemStack stack) {
        return Math.max(0, capacity(stack) - get(stack).size());
    }

    /**
     * Une pierre qu'on peut sertir : une pierre polie — du mod, ou le diamant et l'émeraude du jeu —
     * dont la <b>pureté est connue</b>. Une pierre trouvée dans la nature n'en a pas tant qu'un
     * lapidaire ne l'a pas examinée, et ne se sertit donc pas.
     */
    public static boolean isSettable(ItemStack gem) {
        return GemPolishing.isPolished(gem.getItem()) && gem.contains(ModDataComponents.PURITY);
    }

    /**
     * Cette pierre va-t-elle sur cet objet, emplacements libres mis à part ? Sur une bague, une pierre
     * qui donne un pouvoir ; sur la lampe, une pierre qui dort dans la roche.
     */
    public static boolean fits(ItemStack gear, ItemStack gem) {
        SocketFamily family = SocketFamily.of(gear);
        if (family == null || !isSettable(gem)) {
            return false;
        }
        return family == SocketFamily.JEWELRY ? GemPower.of(gem.getItem()) != null : GemSense.senses(gem.getItem());
    }

    /** Peut-on poser cette pierre sur cet objet, dans un emplacement libre ? */
    public static boolean canSet(ItemStack gear, ItemStack gem) {
        return freeSlots(gear) > 0 && fits(gear, gem);
    }

    /** Cet objet a-t-il une pierre principale, qui choisit un pouvoir ? La bague, oui ; la lampe, non. */
    public static boolean hasMain(ItemStack gear) {
        return SocketFamily.of(gear) == SocketFamily.JEWELRY;
    }

    /** La pierre principale de l'objet : celle qui choisit son pouvoir. Vide s'il n'en a pas. */
    public static ItemStack main(ItemStack gear) {
        return hasMain(gear) ? get(gear).get(MAIN_CELL) : ItemStack.EMPTY;
    }

    // ---------- modification ----------

    /** Pose la pierre dans le premier emplacement libre et recalcule les attributs. */
    public static void set(ItemStack gear, ItemStack gem) {
        set(gear, get(gear).firstFree(), gem, null);
    }

    /** Pose la pierre dans cet emplacement. L'objet est modifié sur place. */
    public static void set(ItemStack gear, int cell, ItemStack gem, @Nullable PlayerEntity player) {
        ItemStack tuned = gem.copyWithCount(1);
        tuned.remove(ModDataComponents.RESONANCE);         // l'accord d'une compétence qui n'existe plus
        gear.set(ModDataComponents.SOCKETS, get(gear).with(cell, tuned));
        refresh(gear);
    }

    /**
     * Retire la pierre de cet emplacement et la rend telle qu'elle était sertie. Les autres pierres
     * gardent leur place.
     */
    public static ItemStack remove(ItemStack gear, int cell) {
        Sockets sockets = get(gear);
        ItemStack gem = sockets.get(cell).copy();
        Sockets left = sockets.without(cell);
        if (left.isEmpty()) {
            gear.remove(ModDataComponents.SOCKETS);
        } else {
            gear.set(ModDataComponents.SOCKETS, left);
        }
        refresh(gear);
        return gem;
    }

    /**
     * Remet les attributs de l'objet à ceux de sa fabrication. Les pierres n'y écrivent plus rien ;
     * mais l'ancien système y posait un modificateur par pierre, et un objet serti avant le
     * changement les porte encore. On les retire, et on répare au passage un objet à qui l'ancien
     * dessertissage aurait ôté jusqu'à ses propres attributs.
     */
    public static void refresh(ItemStack gear) {
        AttributeModifiersComponent now = gear.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        AttributeModifiersComponent made = gear.getDefaultComponents().get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        boolean stale = now == null ? made != null : now.modifiers().stream().anyMatch(entry ->
                entry.modifier().id().getNamespace().equals(MOD_ID) && entry.modifier().id().getPath().startsWith("socket_"));
        if (stale) {
            // remettre la valeur de fabrication efface la modification, là où « retirer » la marquerait
            gear.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, made);
        }
    }

    /** Les objets sertis avant ce changement sont nettoyés dès qu'ils passent entre les mains d'un joueur. */
    public static void register() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (net.minecraft.server.network.ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if ((server.getTicks() + player.getId()) % 40 != 0) {
                    continue;
                }
                for (int slot = 0; slot < player.getInventory().size(); slot++) {
                    ItemStack stack = player.getInventory().getStack(slot);
                    if (!stack.isEmpty() && capacity(stack) > 0) {
                        refresh(stack);
                    }
                }
            }
        });
    }
}
