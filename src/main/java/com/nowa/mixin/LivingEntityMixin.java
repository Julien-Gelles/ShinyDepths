package com.nowa.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.nowa.item.GearSalvage;
import com.nowa.powers.PowerHooks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Deux greffes sur toute entité vivante : la pièce d'équipement qui cède, et les dégâts reçus, qui
 * passent par les pouvoirs.
 *
 * <p>Le point de passage de toute pièce d'équipement qui cède : le jeu prévient l'entité pour qu'elle
 * joue le bruit et les particules. On en profite pour rendre ce qui peut l'être — la pile, elle,
 * est déjà vide à ce moment, d'où le seul objet en paramètre.
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    /** Les dégâts qu'une entité va recevoir passent par le pouvoir de celui qui frappe. */
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float shinyDepths$powerDamage(float amount, @Local(argsOnly = true) DamageSource source) {
        return PowerHooks.damage((LivingEntity) (Object) this, source, amount);
    }

    @Inject(method = "sendEquipmentBreakStatus", at = @At("HEAD"))
    private void shinyDepths$salvageBrokenGear(Item item, EquipmentSlot slot, CallbackInfo info) {
        GearSalvage.onBroken((LivingEntity) (Object) this, item);
    }
}
