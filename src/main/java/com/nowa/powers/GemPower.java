package com.nowa.powers;

import com.nowa.item.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Les quinze pouvoirs, et la pierre qui donne chacun. Une bague sertie d'une de ces pierres donne
 * son pouvoir, au niveau de sa pureté (voir {@link Powers}).
 *
 * <p>Ce que fait chaque pouvoir, niveau par niveau, est décrit dans docs/bijoux.md.
 */
public enum GemPower {
    LIFESTEAL("lifesteal", ModItems.ROSE_QUARTZ),
    FURY("fury", ModItems.RUBY),
    RIPOSTE("riposte", ModItems.GARNET),
    PIERCING("piercing", ModItems.SUGILITE),
    REFLECT("reflect", ModItems.BLUE_TOPAZ),
    DOUBLE_JUMP("double_jump", ModItems.PERIDOT),
    RECALL("recall", ModItems.MOONSTONE),
    REACH("reach", ModItems.JADE),
    ENDER_POCKET("ender_pocket", ModItems.ONYX),
    VEIN("vein", Items.DIAMOND),
    SMELTING("smelting", ModItems.AMBER),
    FISHING("fishing", ModItems.TOURMALINE),
    SCHOLAR("scholar", ModItems.SAPPHIRE),
    MERCHANT("merchant", Items.EMERALD),
    SOBRIETY("sobriety", ModItems.TANZANITE);

    private static final Map<Item, GemPower> BY_GEM = new HashMap<>();

    static {
        for (GemPower power : values()) {
            BY_GEM.put(power.gem, power);
        }
    }

    private final String id;
    private final Item gem;

    GemPower(String id, Item gem) {
        this.id = id;
        this.gem = gem;
    }

    public String id() {
        return id;
    }

    /** La pierre qui donne ce pouvoir. */
    public Item gem() {
        return gem;
    }

    public Text displayName() {
        return Text.translatable("power.nowas-depths." + id);
    }

    /** Le pouvoir que donne cette pierre, ou {@code null} si elle n'en donne pas. */
    @Nullable
    public static GemPower of(Item gem) {
        return BY_GEM.get(gem);
    }
}
