package com.nowa.mixin;

import com.nowa.block.ModBlockTags;
import com.nowa.jewelry.Jewelry;
import com.nowa.lapidary.LapidaryBonuses;
import com.nowa.powers.PowerHooks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Les greffes sur le joueur : l'Abattage, les bijoux qu'il laisse tomber en mourant, et les passages
 * par les pouvoirs.
 *
 * <p>L'Abattage : les minerais du mod se cassent plus vite pour qui a appris à les lire. Le calcul de
 * vitesse est le même des deux côtés, et la progression du lapidaire est synchronisée avec son
 * client, si bien que l'animation de minage reste d'accord avec le serveur.
 */
@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void shinyDepths$quarrying(BlockState state, CallbackInfoReturnable<Float> info) {
        if (!state.isIn(ModBlockTags.ORES)) {
            return;
        }
        double factor = LapidaryBonuses.miningSpeed((PlayerEntity) (Object) this);
        if (factor > 1.0) {
            info.setReturnValue((float) (info.getReturnValue() * factor));
        }
    }

    /** Les pouvoirs : leur tic, et ce qu'ils changent à la faim et à l'expérience gagnée. */
    @Inject(method = "tick", at = @At("TAIL"))
    private void shinyDepths$powerTick(CallbackInfo info) {
        PowerHooks.tick((PlayerEntity) (Object) this);
    }

    @ModifyVariable(method = "addExhaustion", at = @At("HEAD"), argsOnly = true)
    private float shinyDepths$powerExhaustion(float exhaustion) {
        return PowerHooks.exhaustion((PlayerEntity) (Object) this, exhaustion);
    }

    @ModifyVariable(method = "addExperience", at = @At("HEAD"), argsOnly = true)
    private int shinyDepths$powerExperience(int experience) {
        return PowerHooks.experience((PlayerEntity) (Object) this, experience);
    }

    /**
     * Les bijoux tombent avec le reste de l'inventaire, et au meme endroit du code : ils obeissent
     * donc a la meme regle de monde, et une partie en « keepInventory » garde ses bagues.
     */
    @Inject(method = "dropInventory", at = @At("TAIL"))
    private void shinyDepths$dropJewelry(CallbackInfo info) {
        Jewelry.dropAll((PlayerEntity) (Object) this);
    }
}
