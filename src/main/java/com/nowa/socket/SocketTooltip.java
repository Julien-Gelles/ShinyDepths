package com.nowa.socket;

import com.nowa.item.GemColors;
import com.nowa.item.ModDataComponents;
import com.nowa.item.ModJewelry;
import com.nowa.powers.Powers;
import com.nowa.item.Purity;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Ce que l'infobulle dit du sertissage : le nombre d'emplacements occupés, puis les pierres, une
 * par ligne — la principale dans sa propre couleur. Le set qu'elle donne n'est pas répété ici : il est dans le
 * nom de l'objet (voir {@code ItemStackMixin}). S'y ajoute ce qu'un bijou donne par son seul métal.
 */
public final class SocketTooltip {
    private SocketTooltip() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            ModJewelry.Metal metal = ModJewelry.metalOf(stack.getItem());
            if (metal != null && metal.armor() > 0) {
                lines.add(Text.translatable("tooltip.nowas-depths.jewelry.armor", number(metal.armor()))
                        .formatted(Formatting.BLUE));
            }
            int capacity = Socketing.capacity(stack);
            if (capacity <= 0) {
                return;
            }
            Sockets sockets = Socketing.get(stack);
            lines.add(Text.translatable("tooltip.nowas-depths.sockets", sockets.size(), capacity)
                    .formatted(Formatting.GRAY));
            boolean hasMain = Socketing.hasMain(stack);
            // la bague dit ce qu'elle donne : son pouvoir, au niveau de la pureté de sa pierre
            Powers.Active ring = Powers.ofRing(stack);
            if (ring.power() != null) {
                int rgb = GemColors.of(ring.power().gem()) & 0xFFFFFF;
                lines.add(Text.translatable("tooltip.nowas-depths.ring.power", ring.power().displayName(),
                        Text.translatable("enchantment.level." + ring.level())).styled(style -> style.withColor(rgb)));
            }
            for (int cell = 0; cell < capacity; cell++) {
                ItemStack gem = sockets.get(cell);
                if (gem.isEmpty()) {
                    continue;
                }
                MutableText line = Text.literal(" • ").append(named(gem));
                if (hasMain && cell == Socketing.MAIN_CELL) {
                    int rgb = GemColors.of(gem.getItem()) & 0xFFFFFF;
                    lines.add(line.styled(style -> style.withColor(rgb)));
                } else {
                    lines.add(line.formatted(Formatting.DARK_GRAY));
                }
            }
        });
    }

    /** « Rubis (pure) », ou le seul nom d'une pierre sans pureté. */
    private static Text named(ItemStack gem) {
        Purity purity = gem.get(ModDataComponents.PURITY);
        return purity == null ? gem.getName()
                : Text.translatable("tooltip.nowas-depths.socket_gem", gem.getName(), purity.displayName());
    }

    /** 0,25 s'écrit 0.25, et 1 s'écrit 1. */
    private static String number(double value) {
        return value == Math.rint(value) ? Integer.toString((int) value) : Double.toString(value);
    }
}
