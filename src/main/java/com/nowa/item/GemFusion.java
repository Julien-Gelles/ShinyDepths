package com.nowa.item;

import net.minecraft.item.ItemStack;

/**
 * La fusion des puretés : quatre pierres polies identiques — même pierre, même pureté — donnent une
 * pierre de la pureté au-dessus. C'est ce qui donne un sens au surplus : une pierre de trop finit par
 * en faire une meilleure. La philosophale ne fusionne plus : c'est la plus haute.
 *
 * <p>Elle se fait au creuset de fusion ({@link com.nowa.block.FusionCrucibleBlock}).
 */
public final class GemFusion {
    /** Le nombre de pierres qu'une fusion consomme : une par case d'entrée du creuset. */
    public static final int COUNT = 4;

    private GemFusion() {
    }

    /** Cette pierre peut-elle entrer dans une fusion ? Polie, de pureté connue, et pas encore philosophale. */
    public static boolean fusible(ItemStack gem) {
        Purity purity = gem.get(ModDataComponents.PURITY);
        return !gem.isEmpty() && GemPolishing.isPolished(gem.getItem()) && purity != null && purity != Purity.PHILOSOPHAL;
    }

    /** Ces deux pierres sont-elles les mêmes — même pierre, même pureté ? */
    public static boolean same(ItemStack one, ItemStack other) {
        return ItemStack.areItemsAndComponentsEqual(one, other);
    }

    /** La pierre que donne la fusion de pierres comme celle-ci : la même, une pureté au-dessus. Vide si elle ne fusionne pas. */
    public static ItemStack result(ItemStack gem) {
        if (!fusible(gem)) {
            return ItemStack.EMPTY;
        }
        Purity purity = gem.get(ModDataComponents.PURITY);
        return GemPolishing.polishedStack(gem.getItem(), Purity.values()[purity.ordinal() + 1]);
    }
}
