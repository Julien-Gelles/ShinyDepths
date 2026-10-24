package com.nowa.item;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.List;

/**
 * La rareté d'une pierre polie, telle que la donne docs/minerais.md. Le villageois lapidaire en
 * tire ses prix, les coffres des structures la fréquence de chaque pierre.
 */
public enum GemRarity {
    UNCOMMON("uncommon",
            ModItems.ONYX, ModItems.GARNET, ModItems.AMBER, ModItems.JADE, ModItems.ROSE_QUARTZ),
    RARE("rare",
            ModItems.MOONSTONE, ModItems.PERIDOT, ModItems.BLUE_TOPAZ, ModItems.SUGILITE, ModItems.TOURMALINE,
            ModItems.RUBY),
    VERY_RARE("very_rare", ModItems.TANZANITE, ModItems.SAPPHIRE, Items.DIAMOND);

    private final String key;
    private final List<Item> gems;

    GemRarity(String key, Item... gems) {
        this.key = key;
        this.gems = List.of(gems);
    }

    /** Le nom dans le fichier de configuration. */
    public String key() {
        return key;
    }

    /**
     * Les pierres de cette rareté. Le diamant est très rare, comme au minage (depuis le 2026-10-24) ;
     * l'émeraude n'y est pas, c'est la monnaie du villageois.
     */
    public List<Item> gems() {
        return gems;
    }
}
