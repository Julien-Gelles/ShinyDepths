package com.nowa.item;

import com.nowa.config.ModConfig;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Les bagues, en fer, en or, en titane et en platine — les métaux de joaillier, pas tous ceux qui
 * ont un lingot. Elles ne s'usent pas et ne frappent rien : une bague porte une pierre, dont le set
 * donne ses pouvoirs à qui la porte, dans l'emplacement qu'il a en plus de son armure (voir
 * {@link com.nowa.jewelry.Jewelry}). Le métal ne donne qu'un peu d'armure.
 *
 * <p>Il y eut aussi des colliers et des bracelets ; ils ont disparu avec la refonte du 2026-10-14.
 */
public final class ModJewelry {
    /**
     * Les trois formes, ce que chacune porte et sa disposition a l'etabli. Comme pour les
     * panoplies, le nombre de lingots ne se declare pas : il se compte dans le motif.
     */
    public enum Kind {
        RING("ring", ModItemTags.RINGS, " X ", "X X", " X ");

        private final String name;
        private final TagKey<Item> tag;
        private final List<String> pattern;

        Kind(String name, TagKey<Item> tag, String... pattern) {
            this.name = name;
            this.tag = tag;
            this.pattern = List.of(pattern);
        }

        public String kindName() {
            return name;
        }

        /** Le tag des bijoux de cette forme : c'est lui que la case a bijou interroge. */
        public TagKey<Item> tag() {
            return tag;
        }

        /** La disposition a l'etabli : X un lingot. */
        public List<String> pattern() {
            return pattern;
        }

        /** Ce que le bijou a coute en lingots. */
        public int ingots() {
            return (int) pattern.stream().flatMapToInt(String::chars).filter(c -> c == 'X').count();
        }
    }

    /**
     * Les métaux d'un bijou : deux du jeu, deux du mod. Le nom sert d'identifiant et de nom de
     * texture.
     */
    public enum Metal {
        IRON("iron", Items.IRON_INGOT, 0.5),
        GOLD("gold", Items.GOLD_INGOT, 0.25),
        TITANIUM(ModGear.Metal.TITANIUM, 1.0),
        PLATINUM(ModGear.Metal.PLATINUM, 1.0);

        private final String name;
        private final Item ingot;
        private final double defaultArmor;
        /** Ce qu'un bijou de ce métal donne d'armure une fois porté, serti ou non. */
        private double armor;

        Metal(String name, Item ingot, double armor) {
            this.name = name;
            this.ingot = ingot;
            this.defaultArmor = armor;
            this.armor = armor;
        }

        Metal(ModGear.Metal gear, double armor) {
            this(gear.materialName(), gear.ingot(), armor);
        }

        public double armor() {
            return armor;
        }

        public String materialName() {
            return name;
        }

        /** Le lingot de la recette. */
        public Item ingot() {
            return ingot;
        }
    }

    private static final Map<Metal, Map<Kind, Item>> PIECES = forge();
    private static final Map<Item, Metal> METAL_OF = new HashMap<>();
    private static final Map<Item, Kind> KIND_OF = new HashMap<>();

    static {
        PIECES.forEach((metal, kinds) -> kinds.forEach((kind, item) -> {
            METAL_OF.put(item, metal);
            KIND_OF.put(item, kind);
        }));
    }

    static {
        ModConfig.onApply(() -> {
            for (Metal metal : Metal.values()) {
                metal.armor = Math.max(0.0, ModConfig.d("jewelry.armor." + metal.name, metal.defaultArmor));
            }
        });
    }

    private ModJewelry() {
    }

    public static Item of(Metal metal, Kind kind) {
        return PIECES.get(metal).get(kind);
    }

    /** Le métal d'un bijou, ou {@code null} si l'objet n'en est pas un. */
    public static Metal metalOf(Item item) {
        return METAL_OF.get(item);
    }

    /** La forme d'un bijou, ou {@code null} si l'objet n'en est pas un. */
    public static Kind kindOf(Item item) {
        return KIND_OF.get(item);
    }

    /** Tous les bijoux d'une forme, dans l'ordre des métaux. */
    public static List<Item> allOf(Kind kind) {
        return PIECES.values().stream().map(kinds -> kinds.get(kind)).toList();
    }

    /** Tous les bijoux, métal par métal. */
    public static List<Item> all() {
        List<Item> items = new ArrayList<>();
        PIECES.values().forEach(kinds -> items.addAll(kinds.values()));
        return items;
    }

    public static void registerJewelry() {
        LOGGER.info("├─ Registering Mod Jewelry for " + MOD_ID);
    }

    private static Map<Metal, Map<Kind, Item>> forge() {
        Map<Metal, Map<Kind, Item>> pieces = new EnumMap<>(Metal.class);
        for (Metal metal : Metal.values()) {
            Map<Kind, Item> kinds = new EnumMap<>(Kind.class);
            for (Kind kind : Kind.values()) {
                kinds.put(kind, register(metal, kind));
            }
            pieces.put(metal, Collections.unmodifiableMap(kinds));
        }
        return Collections.unmodifiableMap(pieces);
    }

    /** Un bijou ne s'empile pas : chacun porte ses propres pierres. */
    private static Item register(Metal metal, Kind kind) {
        return Registry.register(Registries.ITEM,
                Identifier.of(MOD_ID, metal.materialName() + "_" + kind.kindName()),
                new JewelryItem(kind, new Item.Settings().maxCount(1)));
    }
}
