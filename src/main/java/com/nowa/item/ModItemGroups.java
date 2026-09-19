package com.nowa.item;

import com.nowa.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

public class ModItemGroups {
    public static final ItemGroup RUBY_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(MOD_ID, "ruby"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.ruby"))
                    .icon(() -> new ItemStack(ModItems.RUBY)).entries((displayContext, entries) -> {
                        entries.add(ModItems.POLISHER);
                        entries.add(ModBlocks.IRON_LAPIDARY_TABLE);
                        entries.add(ModBlocks.DIAMOND_LAPIDARY_TABLE);
                        entries.add(ModBlocks.NETHERITE_LAPIDARY_TABLE);
                        // Pour chaque matériau : brut -> lingot -> pierre (selon ce qui existe)
                        entries.add(ModItems.RAW_PLATINUM);
                        entries.add(ModItems.PLATINUM_INGOT);
                        entries.add(ModItems.RAW_MOONSTONE);
                        entries.add(ModItems.MOONSTONE);
                        entries.add(ModItems.RAW_TITANIUM);
                        entries.add(ModItems.TITANIUM_INGOT);
                        entries.add(ModItems.RAW_ONYX);
                        entries.add(ModItems.ONYX);
                        entries.add(ModItems.RAW_GARNET);
                        entries.add(ModItems.GARNET);
                        entries.add(ModItems.RAW_AMBER);
                        entries.add(ModItems.AMBER);
                        entries.add(ModItems.RAW_PYRITE);
                        entries.add(ModItems.PYRITE_INGOT);
                        entries.add(ModItems.RAW_PERIDOT);
                        entries.add(ModItems.PERIDOT);
                        entries.add(ModItems.RAW_JADE);
                        entries.add(ModItems.JADE);
                        entries.add(ModItems.RAW_COBALT);
                        entries.add(ModItems.COBALT_INGOT);
                        entries.add(ModItems.RAW_BLUE_TOPAZ);
                        entries.add(ModItems.BLUE_TOPAZ);
                        entries.add(ModItems.RAW_SAPPHIRE);
                        entries.add(ModItems.SAPPHIRE);
                        entries.add(ModItems.RAW_TANZANITE);
                        entries.add(ModItems.TANZANITE);
                        entries.add(ModItems.RAW_SUGILITE);
                        entries.add(ModItems.SUGILITE);
                        entries.add(ModItems.RAW_TOURMALINE);
                        entries.add(ModItems.TOURMALINE);
                        entries.add(ModItems.RAW_ROSE_QUARTZ);
                        entries.add(ModItems.ROSE_QUARTZ);
                        entries.add(ModItems.RAW_RUBY);
                        entries.add(ModItems.RUBY);
                        entries.add(ModItems.RAW_BISMUTH);
                        entries.add(ModItems.BISMUTH_INGOT);
                        entries.add(ModBlocks.RUBY_BLOCK);
                        entries.add(ModBlocks.RUBY_ORE);
                        entries.add(ModBlocks.DEEPSLATE_RUBY_ORE);

                    }).build());


    public static void registerItemGroups() {
        LOGGER.info("├─ Registering Mod Item Groups for " + MOD_ID);
    }
}