package com.nowa.socket;

import com.nowa.item.ModItemTags;
import net.minecraft.item.ItemStack;

/**
 * Ce qui se sertit. Depuis la refonte du 2026-10-14, deux objets seulement : la <b>bague</b>, dont la
 * pierre donne un set, et la <b>lampe frontale</b>, dont les pierres font luire leur minerai. Les
 * armes, les outils et les armures ne prennent plus de pierres.
 *
 * <p>L'appartenance se lit dans des tags, pour que les objets d'un autre mod puissent s'y inscrire.
 */
public enum SocketFamily {
    /** Les bagues : une pierre, celle du set. */
    JEWELRY,
    /**
     * La lampe frontale. Ses pierres ne donnent aucun pouvoir : chacune fait luire son propre
     * minerai (voir {@link com.nowa.headlamp.GemSense}). Seules les pierres qui dorment dans une
     * roche s'y posent donc.
     */
    LAMP;

    /** La famille de cet objet, ou {@code null} s'il ne se sertit pas. */
    public static SocketFamily of(ItemStack stack) {
        if (stack.isIn(ModItemTags.SOCKETABLE_HEADLAMP)) {
            return LAMP;
        }
        return stack.isIn(ModItemTags.SOCKETABLE_JEWELRY) ? JEWELRY : null;
    }
}
