package com.nowa.mixin;

import com.nowa.powers.power.Merchant;
import com.nowa.villager.LapidaryVillager;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Le villageois prépare ses offres pour le joueur qui vient lui parler : réputation, puis Héros du
 * village. La remise du pouvoir Marchand s'y ajoute, et chez le villageois lapidaire celle du Négoce
 * de l'arbre ; le jeu les efface avec les autres quand l'échange se ferme.
 */
@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {
    @Inject(method = "prepareOffersFor", at = @At("TAIL"))
    private void shinyDepths$merchantDiscount(PlayerEntity player, CallbackInfo info) {
        VillagerEntity villager = (VillagerEntity) (Object) this;
        Merchant.discount(player, villager.getOffers());
        if (villager.getVillagerData().getProfession() == LapidaryVillager.PROFESSION) {
            LapidaryVillager.tradeDiscount(player, villager.getOffers());
        }
    }
}
