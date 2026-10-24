package com.nowa.mixin;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Le perçage d'une flèche, que le jeu ne pose qu'à son tir à l'arbalète (enchantement Perforation). */
@Mixin(PersistentProjectileEntity.class)
public interface PersistentProjectileAccessor {
    @Invoker("setPierceLevel")
    void nowasDepths$setPierceLevel(byte level);
}
