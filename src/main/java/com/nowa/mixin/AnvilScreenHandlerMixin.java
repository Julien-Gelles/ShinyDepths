package com.nowa.mixin;

import com.nowa.item.PolisherItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.StringHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Enclume et polissoir : un seul silex le répare entièrement (en vanilla, un matériau répare 25 %),
 * pour le prix d'un seul matériau. On laisse l'enclume faire son calcul normal, puis on ajuste uniquement ce cas.
 * Les livres enchantés suivent la règle vanilla (comme le pinceau : seuls Solidité et Raccommodage s'appliquent).
 */
@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    @Shadow
    private int repairItemUsage;
    @Shadow
    private String newItemName;
    @Shadow
    @Final
    private Property levelCost;

    public AnvilScreenHandlerMixin(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(method = "updateResult", at = @At("TAIL"))
    private void nowasDepths$polisherRules(CallbackInfo ci) {
        ItemStack tool = this.input.getStack(0);
        if (!(tool.getItem() instanceof PolisherItem)) {
            return;
        }
        ItemStack material = this.input.getStack(1);
        ItemStack result = this.output.getStack(0);

        if (material.isOf(((PolisherItem) tool.getItem()).repairItem()) && !result.isEmpty()
                && this.repairItemUsage > 0) {
            result.setDamage(0);
            this.repairItemUsage = 1;
            // coût vanilla d'une réparation avec un seul matériau : pénalité de travaux + 1 (+ 1 si renommage)
            int rename = 0;
            if (this.newItemName == null || StringHelper.isBlank(this.newItemName)) {
                if (tool.contains(DataComponentTypes.CUSTOM_NAME)) {
                    rename = 1;
                }
            } else if (!this.newItemName.equals(tool.getName().getString())) {
                rename = 1;
            }
            this.levelCost.set(tool.getOrDefault(DataComponentTypes.REPAIR_COST, 0) + 1 + rename);
        }
    }
}
