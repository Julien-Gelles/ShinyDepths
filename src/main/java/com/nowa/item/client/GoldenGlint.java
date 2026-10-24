package com.nowa.item.client;

import com.nowa.item.ModDataComponents;
import com.nowa.item.Purity;
import com.nowa.mixin.ImmediateAccessor;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.SequencedMap;

import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Le reflet des pierres philosophales : le miroitement de l'enchantement, mais doré.
 *
 * <p>Le jeu n'expose pas la couleur du reflet, seulement sa texture, et celle-ci est globale.
 * On recopie donc la couche de rendu du reflet vanilla en changeant la seule texture, et un mixin
 * la substitue à l'originale pendant le rendu d'une pierre philosophale — le reste du jeu garde
 * son reflet violet.
 */
public final class GoldenGlint {
    private static final Identifier ITEM_TEXTURE = Identifier.of(MOD_ID, "textures/misc/golden_glint_item.png");
    private static final Identifier ENTITY_TEXTURE = Identifier.of(MOD_ID, "textures/misc/golden_glint_entity.png");

    /** Copie de {@code RenderLayer.getGlint()} : le reflet des objets, en inventaire comme en main. */
    public static final RenderLayer ITEM = RenderLayer.of("shiny_depths_golden_glint",
            VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS, 1536,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.GLINT_PROGRAM)
                    .texture(new RenderPhase.Texture(ITEM_TEXTURE, true, false))
                    .writeMaskState(RenderPhase.COLOR_MASK)
                    .cull(RenderPhase.DISABLE_CULLING)
                    .depthTest(RenderPhase.EQUAL_DEPTH_TEST)
                    .transparency(RenderPhase.GLINT_TRANSPARENCY)
                    .texturing(RenderPhase.GLINT_TEXTURING)
                    .build(false));

    /** Copie de {@code RenderLayer.getEntityGlint()} : l'objet posé dans le monde. */
    public static final RenderLayer ENTITY = RenderLayer.of("shiny_depths_golden_glint_entity",
            VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS, 1536,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.ENTITY_GLINT_PROGRAM)
                    .texture(new RenderPhase.Texture(ENTITY_TEXTURE, true, false))
                    .writeMaskState(RenderPhase.COLOR_MASK)
                    .cull(RenderPhase.DISABLE_CULLING)
                    .depthTest(RenderPhase.EQUAL_DEPTH_TEST)
                    .transparency(RenderPhase.GLINT_TRANSPARENCY)
                    .texturing(RenderPhase.ENTITY_GLINT_TEXTURING)
                    .target(RenderPhase.ITEM_ENTITY_TARGET)
                    .build(false));

    /** Copie de {@code RenderLayer.getDirectEntityGlint()}. */
    public static final RenderLayer DIRECT_ENTITY = RenderLayer.of("shiny_depths_golden_glint_direct",
            VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS, 1536,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.DIRECT_ENTITY_GLINT_PROGRAM)
                    .texture(new RenderPhase.Texture(ENTITY_TEXTURE, true, false))
                    .writeMaskState(RenderPhase.COLOR_MASK)
                    .cull(RenderPhase.DISABLE_CULLING)
                    .depthTest(RenderPhase.EQUAL_DEPTH_TEST)
                    .transparency(RenderPhase.GLINT_TRANSPARENCY)
                    .texturing(RenderPhase.ENTITY_GLINT_TEXTURING)
                    .build(false));

    /**
     * Le dessinateur immédiat vide son tampon de secours avant ceux de ses couches connues : une
     * couche inconnue serait donc peinte avant l'objet, et son test de profondeur « égal » ne
     * trouverait rien à recouvrir. On lui donne donc un tampon, comme le jeu le fait pour ses cinq
     * couches de reflet. C'est fait à la première pierre rencontrée, le dessinateur n'existant pas
     * encore au chargement du client.
     */
    public static void ensureBuffers(VertexConsumerProvider provider) {
        if (!(provider instanceof VertexConsumerProvider.Immediate immediate)) {
            return;
        }
        SequencedMap<RenderLayer, BufferAllocator> buffers = ((ImmediateAccessor) immediate).shinyDepths$layerBuffers();
        if (buffers.containsKey(ITEM)) {
            return;
        }
        for (RenderLayer layer : List.of(ITEM, ENTITY, DIRECT_ENTITY)) {
            buffers.put(layer, new BufferAllocator(layer.getExpectedBufferSize()));
        }
    }

    /**
     * La pierre en cours de rendu, posée par le mixin le temps d'un objet. Le rendu d'objets n'a
     * lieu que sur le fil de rendu, mais on reste prudent : un champ statique simple suffirait,
     * un ThreadLocal évite d'avoir à le prouver.
     */
    private static final ThreadLocal<Boolean> RENDERING_PHILOSOPHAL = ThreadLocal.withInitial(() -> false);

    private GoldenGlint() {
    }

    public static void begin(ItemStack stack) {
        RENDERING_PHILOSOPHAL.set(stack.get(ModDataComponents.PURITY) == Purity.PHILOSOPHAL);
    }

    public static void end() {
        RENDERING_PHILOSOPHAL.set(false);
    }

    public static boolean isActive() {
        return RENDERING_PHILOSOPHAL.get();
    }
}
