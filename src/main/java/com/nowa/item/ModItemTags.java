package com.nowa.item;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import static com.nowa.NowasDepths.MOD_ID;

public class ModItemTags {
    /** Ce qu'un sachet à pierres accepte : pierres, minerais, lingots, pépites, poudres… (défini par le datagen). */
    public static final TagKey<Item> POUCH_STORABLE = of("pouch_storable");

    /**
     * Ce que l'explosion d'une dynamite ne détruit pas quand c'est tombé au sol : tout ce qu'un
     * sachet accepte, plus les géodes et les sachets eux-mêmes.
     */
    public static final TagKey<Item> DYNAMITE_PROOF = of("dynamite_proof");

    // ---------- sertissage ----------
    /** Ce qui se sertit : la bague, et la lampe frontale. */
    public static final TagKey<Item> SOCKETABLE_JEWELRY = of("socketable/jewelry");
    public static final TagKey<Item> SOCKETABLE_HEADLAMP = of("socketable/headlamp");

    // ---------- bijoux ----------
    /** Ce que l'emplacement de bague accepte. Un autre mod peut y inscrire les siennes. */
    public static final TagKey<Item> RINGS = of("rings");
    /** Nombre maximum d'emplacements qu'un objet peut porter. */
    public static final int MAX_SOCKETS = 10;
    /** Nombre d'emplacements : l'objet prend le plus élevé auquel il appartient (index 1 à 10). */
    public static final TagKey<Item>[] SOCKETS = sockets();

    @SuppressWarnings("unchecked")
    private static TagKey<Item>[] sockets() {
        TagKey<Item>[] tags = new TagKey[MAX_SOCKETS + 1];
        for (int count = 1; count <= MAX_SOCKETS; count++) {
            tags[count] = of("sockets/" + count);
        }
        return tags;
    }

    private static TagKey<Item> of(String name) {
        return TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
    }
}
