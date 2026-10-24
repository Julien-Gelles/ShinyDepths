package com.nowa.item;

import com.nowa.config.ModConfig;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.nowa.NowasDepths.LOGGER;
import static com.nowa.NowasDepths.MOD_ID;

/**
 * Les panoplies des quatre métaux : épée, pioche, hache, pelle, houe, et pour ceux qui en ont une,
 * les quatre pièces d'armure. La pyrite n'a que les outils.
 *
 * <p>Les modificateurs d'attaque reprennent exactement ceux du jeu — c'est le matériau qui fait la
 * différence, pas la forme de l'objet. Seules la hache et la houe varient selon le métal, comme en
 * vanilla où la hache en diamant frappe moins fort que celle en fer mais plus vite.
 */
public final class ModGear {
    /** Ce qu'un métal apporte : son matériau d'outil et sa panoplie. */
    public enum Metal {
        // la pyrite ne se porte pas : trop tendre
        PYRITE("pyrite", ModItems.PYRITE_INGOT, ModToolMaterials.PYRITE, null, 0),
        COBALT("cobalt", ModItems.COBALT_INGOT, ModToolMaterials.COBALT, ModArmorMaterials.COBALT, 24),
        TITANIUM("titanium", ModItems.TITANIUM_INGOT, ModToolMaterials.TITANIUM, ModArmorMaterials.TITANIUM, 32),
        PLATINUM("platinum", ModItems.PLATINUM_INGOT, ModToolMaterials.PLATINUM, ModArmorMaterials.PLATINUM, 40);

        private final String name;
        private final Item ingot;
        private final ToolMaterial tool;
        private final RegistryEntry<ArmorMaterial> armor;
        /** Multiplicateur de durabilité de l'armure : 15 pour le fer, 33 pour le diamant, 37 pour la netherite. */
        private final int armorDurability;

        Metal(String name, Item ingot, ToolMaterial tool, RegistryEntry<ArmorMaterial> armor,
              int armorDurability) {
            this.name = name;
            this.ingot = ingot;
            this.tool = tool;
            this.armor = armor;
            this.armorDurability = armor == null ? 0
                    : ModConfig.i("gear." + name + ".armor.durability_multiplier", armorDurability);
        }

        public String materialName() {
            return name;
        }

        /** Le lingot dont tout part : recette, réparation, enclume. */
        public Item ingot() {
            return ingot;
        }

        /** Ce métal a-t-il une armure ? Tous ont des outils, pas tous une panoplie à porter. */
        public boolean hasArmor() {
            return armor != null;
        }

        public ToolMaterial toolMaterial() {
            return tool;
        }

    }

    /**
     * Les neuf formes, avec leur disposition à l'établi. Le nombre de lingots ne se déclare pas :
     * il se compte dans le motif, ce qui garantit que la recette et ce qu'on récupère d'une pièce
     * brisée parlent toujours du même coût.
     */
    public enum Piece {
        SWORD("sword", null, "X", "X", "#"),
        PICKAXE("pickaxe", null, "XXX", " # ", " # "),
        AXE("axe", null, "XX", "X#", " #"),
        SHOVEL("shovel", null, "X", "#", "#"),
        HOE("hoe", null, "XX", " #", " #"),
        HELMET("helmet", ArmorItem.Type.HELMET, "XXX", "X X"),
        CHESTPLATE("chestplate", ArmorItem.Type.CHESTPLATE, "X X", "XXX", "XXX"),
        LEGGINGS("leggings", ArmorItem.Type.LEGGINGS, "XXX", "X X", "X X"),
        BOOTS("boots", ArmorItem.Type.BOOTS, "X X", "X X");

        private final String name;
        private final ArmorItem.Type armorType;
        private final List<String> pattern;

        Piece(String name, ArmorItem.Type armorType, String... pattern) {
            this.name = name;
            this.armorType = armorType;
            this.pattern = List.of(pattern);
        }

        public String pieceName() {
            return name;
        }

        public ArmorItem.Type armorType() {
            return armorType;
        }

        public boolean isArmor() {
            return armorType != null;
        }

        /** La disposition à l'établi : X un lingot, # un bâton. */
        public List<String> pattern() {
            return pattern;
        }

        /** Ce que la pièce a coûté en lingots. */
        public int ingots() {
            return (int) pattern.stream().flatMapToInt(String::chars).filter(c -> c == 'X').count();
        }
    }

    /** Les objets d'un métal : cinq outils, et quatre pièces d'armure s'il en a une. */
    public record Set(Metal metal, Map<Piece, Item> pieces) {
        public Item of(Piece piece) {
            return pieces.get(piece);
        }

        public Item sword() {
            return of(Piece.SWORD);
        }

        public Item pickaxe() {
            return of(Piece.PICKAXE);
        }

        public Item axe() {
            return of(Piece.AXE);
        }

        public Item shovel() {
            return of(Piece.SHOVEL);
        }

        public Item hoe() {
            return of(Piece.HOE);
        }

        public Item helmet() {
            return of(Piece.HELMET);
        }

        public Item chestplate() {
            return of(Piece.CHESTPLATE);
        }

        public Item leggings() {
            return of(Piece.LEGGINGS);
        }

        public Item boots() {
            return of(Piece.BOOTS);
        }

        public List<Item> tools() {
            return List.of(sword(), pickaxe(), axe(), shovel(), hoe());
        }

        /** Les pièces d'armure du métal ; aucune s'il n'en a pas. */
        public List<Item> armor() {
            return Stream.of(helmet(), chestplate(), leggings(), boots()).filter(Objects::nonNull).toList();
        }

        public List<Item> all() {
            List<Item> items = new ArrayList<>(tools());
            items.addAll(armor());
            return items;
        }
    }

    private static final Map<Metal, Set> SETS = forge();
    /** Retour en arrière : de l'objet à ce qu'il est, pour qui ne tient qu'un ItemStack. */
    private static final Map<Item, Metal> METAL_OF = new HashMap<>();
    private static final Map<Item, Piece> PIECE_OF = new HashMap<>();

    static {
        SETS.forEach((metal, set) -> set.pieces().forEach((piece, item) -> {
            METAL_OF.put(item, metal);
            PIECE_OF.put(item, piece);
        }));
    }

    private ModGear() {
    }

    public static Collection<Set> sets() {
        return SETS.values();
    }

    public static Set of(Metal metal) {
        return SETS.get(metal);
    }

    /** Le métal d'un objet de panoplie, ou {@code null} si l'objet n'en est pas un. */
    public static Metal metalOf(Item item) {
        return METAL_OF.get(item);
    }

    /** La forme d'un objet de panoplie, ou {@code null} si l'objet n'en est pas un. */
    public static Piece pieceOf(Item item) {
        return PIECE_OF.get(item);
    }

    /** Tous les objets des cinq panoplies, dans l'ordre des métaux. */
    public static List<Item> all() {
        List<Item> items = new ArrayList<>();
        SETS.values().forEach(set -> items.addAll(set.all()));
        return items;
    }

    public static void registerGear() {
        LOGGER.info("├─ Registering Mod Gear for " + MOD_ID);
    }

    private static Map<Metal, Set> forge() {
        Map<Metal, Set> sets = new EnumMap<>(Metal.class);
        for (Metal metal : Metal.values()) {
            Map<Piece, Item> pieces = new EnumMap<>(Piece.class);
            for (Piece piece : Piece.values()) {
                if (piece.isArmor() && !metal.hasArmor()) {
                    continue;
                }
                pieces.put(piece, register(metal, piece));
            }
            sets.put(metal, new Set(metal, Collections.unmodifiableMap(pieces)));
        }
        return Collections.unmodifiableMap(sets);
    }

    private static Item register(Metal metal, Piece piece) {
        ToolMaterial tool = metal.tool;
        String key = "gear." + metal.name + "." + piece.name + ".";
        // la hache troque des dégâts contre de la vitesse au fur et à mesure des paliers,
        // la houe fait l'inverse : elle rend ses dégâts pour frapper plus vite
        boolean high = tool.getAttackDamage() >= 3.0f;
        Item item = switch (piece) {
            case SWORD -> new SwordItem(tool, settings(SwordItem.createAttributeModifiers(tool,
                    ModConfig.i(key + "attack_damage", 3), ModConfig.f(key + "attack_speed", -2.4f))));
            case PICKAXE -> new PickaxeItem(tool, settings(PickaxeItem.createAttributeModifiers(tool,
                    ModConfig.f(key + "attack_damage", 1.0f), ModConfig.f(key + "attack_speed", -2.8f))));
            case AXE -> new AxeItem(tool, settings(AxeItem.createAttributeModifiers(tool,
                    ModConfig.f(key + "attack_damage", high ? 5.0f : 6.0f),
                    ModConfig.f(key + "attack_speed", high ? -3.0f : -3.1f))));
            case SHOVEL -> new ShovelItem(tool, settings(ShovelItem.createAttributeModifiers(tool,
                    ModConfig.f(key + "attack_damage", 1.5f), ModConfig.f(key + "attack_speed", -3.0f))));
            case HOE -> new HoeItem(tool, settings(MiningToolItem.createAttributeModifiers(tool,
                    ModConfig.f(key + "attack_damage", -tool.getAttackDamage()),
                    ModConfig.f(key + "attack_speed", high ? 0.0f : -1.0f))));
            default -> new ArmorItem(metal.armor, piece.armorType(),
                    new Item.Settings().maxDamage(ModConfig.i(key + "durability",
                            piece.armorType().getMaxDamage(metal.armorDurability))));
        };
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, metal.name + "_" + piece.name), item);
    }

    private static Item.Settings settings(net.minecraft.component.type.AttributeModifiersComponent modifiers) {
        return new Item.Settings().attributeModifiers(modifiers);
    }
}
