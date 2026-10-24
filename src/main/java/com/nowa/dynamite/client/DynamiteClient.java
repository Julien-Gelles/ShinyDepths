package com.nowa.dynamite.client;

import com.nowa.dynamite.Dynamite;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Le dessin de la dynamite : un modèle en volume, en main comme en vol, dont la mèche se consume
 * d'après l'instant où elle doit exploser.
 *
 * <p>La propriété {@code shiny-depths:fuse} vaut 0 pour un bâton éteint, puis monte d'un cran par
 * petit cube de mèche brûlé ; le modèle de l'objet passe d'une forme à la suivante sur ces seuils.
 */
public final class DynamiteClient {
    private DynamiteClient() {
    }

    public static void register() {
        EntityRendererRegistry.register(Dynamite.ENTITY, DynamiteEntityRenderer::new);
        ModelPredicateProviderRegistry.register(Dynamite.ITEM, Identifier.of(MOD_ID, "fuse"),
                (stack, world, entity, seed) -> {
                    Dynamite.Fuse fuse = stack.get(Dynamite.FUSE);
                    // dans un inventaire le jeu ne donne pas de monde : on prend celui du joueur
                    World clock = world != null ? world : MinecraftClient.getInstance().world;
                    if (fuse == null || clock == null) {
                        return 0.0f;
                    }
                    return (fuse.stage(clock.getTime()) + 1) / (float) Dynamite.FUSE_STAGES;
                });
    }
}
