package com.nowa.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.SequencedMap;

/**
 * Donne accès aux tampons par couche du dessinateur immédiat. Une couche qui n'y figure pas passe
 * par le tampon de secours, lequel est vidé <em>avant</em> les autres : un reflet y serait peint
 * avant l'objet, et son test de profondeur ne trouverait rien.
 */
@Mixin(VertexConsumerProvider.Immediate.class)
public interface ImmediateAccessor {
    @Accessor("layerBuffers")
    SequencedMap<RenderLayer, BufferAllocator> shinyDepths$layerBuffers();
}
