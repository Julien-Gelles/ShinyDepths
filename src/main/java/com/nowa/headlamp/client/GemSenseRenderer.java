package com.nowa.headlamp.client;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.nowa.headlamp.GemSense;
import com.nowa.headlamp.HeadlampNetworking.GemSensePayload;
import com.nowa.headlamp.HeadlampNetworking.LanternSightPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import com.nowa.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Le battement de la lampe frontale, vu du joueur : les minerais reconnus s'allument à travers la roche, puis
 * s'éteignent. Les lanternes de cobalt à portée battent avec eux, d'un halo qui suit leur forme.
 *
 * <p>La lueur est celle des flèches spectrales : un halo épais autour de la silhouette, visible à
 * travers la roche — c'est là tout l'intérêt, une pierre enfouie doit se voir.
 */
public final class GemSenseRenderer {
    /** Un battement en cours : sa couleur, ce qu'il a trouvé, et le temps qu'il lui reste. */
    private record Pulse(int color, List<BlockPos> positions, int total, int left) {
    }

    /** Un battement par couleur : deux pierres de même teinte ravivent le même éclat. */
    private static final Map<Integer, Pulse> PULSES = new LinkedHashMap<>();
    /** Le traitement du jeu qui fait briller les entités : bords, puis flou. */
    private static final Identifier OUTLINE_EFFECT = Identifier.ofVanilla("shaders/post/entity_outline.json");

    /** Le battement des lanternes de cobalt : celles qu'il a trouvées, sa durée, et le temps qu'il lui reste. */
    private static List<BlockPos> lanterns = List.of();
    private static int lanternTotal;
    private static int lanternTicks;
    /** La lueur d'une lanterne : le bleu clair du cobalt. */
    private static final int LANTERN_COLOR = 0x6CC8F0;
    /**
     * La forme d'une lanterne, en pixels du bloc, pour que le halo en épouse le contour et non celui
     * du bloc : le corps, le chapeau, puis l'anse si elle est posée, ou la chaîne si elle est
     * suspendue. Ce sont les cotes du modèle de lanterne du jeu.
     */
    private static final float[][] LANTERN_STANDING = {
            {5, 0, 5, 11, 7, 11}, {6, 7, 6, 10, 9, 10}, {6.5f, 9, 7.5f, 9.5f, 11, 8.5f}};
    private static final float[][] LANTERN_HANGING = {
            {5, 1, 5, 11, 8, 11}, {6, 8, 6, 10, 10, 10}, {6.5f, 10, 7.5f, 9.5f, 16, 8.5f}};

    private static PostEffectProcessor processor;
    private static boolean failed;
    private static int processorWidth;
    private static int processorHeight;

    private GemSenseRenderer() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(GemSensePayload.ID, (payload, context) ->
                context.client().execute(() -> PULSES.put(payload.color(),
                        new Pulse(payload.color(), payload.positions(), payload.ticks(), payload.ticks()))));
        ClientPlayNetworking.registerGlobalReceiver(LanternSightPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    lanterns = payload.positions();
                    lanternTotal = payload.ticks();
                    lanternTicks = payload.ticks();
                }));
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> fade());
        WorldRenderEvents.LAST.register(context -> {
            if (!PULSES.isEmpty() || lanternsShown()) {
                draw(context.matrixStack(), context.camera().getPos(), context.tickCounter().getTickDelta(false));
            }
        });
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.of(MOD_ID, "gem_sense_glow");
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        release();
                    }
                });
    }

    /** Chaque tic ronge la lueur ; un battement épuisé disparaît. */
    private static void fade() {
        PULSES.values().removeIf(pulse -> pulse.left() <= 1);
        PULSES.replaceAll((color, pulse) ->
                new Pulse(pulse.color(), pulse.positions(), pulse.total(), pulse.left() - 1));
        if (lanternTicks > 0 && --lanternTicks == 0) {
            lanterns = List.of();                            // le battement est fini
        }
    }

    public static void clear() {
        PULSES.clear();
        lanterns = List.of();
        lanternTicks = 0;
    }

    /** Y a-t-il des lanternes à montrer ? Il faut encore porter la lampe : l'ôter les éteint aussitôt. */
    private static boolean lanternsShown() {
        MinecraftClient client = MinecraftClient.getInstance();
        return !lanterns.isEmpty() && lanternTicks > 0 && client.player != null
                && !GemSense.worn(client.player).isEmpty();
    }

    /**
     * Le halo des flèches spectrales, emprunté tel quel. Le jeu trace les silhouettes des entités
     * brillantes dans un tampon à part, en cherche les bords, les floute, puis pose le résultat sur
     * l'image. On fait de même avec nos blocs, dans <em>notre</em> exemplaire de ce traitement : celui
     * du jeu ne tourne que s'il a lui-même une entité brillante à montrer, et le partager aurait
     * brouillé les siennes.
     *
     * <p>C'est la silhouette qui brille, pas chaque arête : deux minerais voisins se fondent en un seul
     * contour, comme deux entités brillantes qui se chevauchent.
     */
    private static void draw(MatrixStack matrices, Vec3d camera, float tickDelta) {
        if (matrices == null || !ensureProcessor()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        Framebuffer glow = processor.getSecondaryTarget("final");
        glow.clear(MinecraftClient.IS_SYSTEM_MAC);
        glow.beginWrite(false);

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        // la silhouette entière, à travers la roche, et sans mélange : deux blocs qui se recouvrent
        // ne doivent pas briller deux fois plus
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (Pulse pulse : new ArrayList<>(PULSES.values())) {
            int alpha = alphaOf(pulse);
            if (alpha <= 0) {
                continue;
            }
            int color = pulse.color();
            int red = color >> 16 & 0xFF;
            int green = color >> 8 & 0xFF;
            int blue = color & 0xFF;
            for (BlockPos pos : pulse.positions()) {
                cube(buffer, matrix, pos, red, green, blue, alpha);
            }
        }
        if (lanternsShown()) {
            // les lanternes de cobalt : le même battement que les pierres, sur la forme de la lanterne
            int alpha = alphaOf(lanternTotal, lanternTicks);
            for (BlockPos pos : lanterns) {
                BlockState state = client.world == null ? null : client.world.getBlockState(pos);
                if (state == null || !state.isOf(ModBlocks.COBALT_LANTERN)) {
                    continue;                                  // cassée depuis le battement
                }
                for (float[] part : state.get(LanternBlock.HANGING) ? LANTERN_HANGING : LANTERN_STANDING) {
                    box(buffer, matrix, pos.getX() + part[0] / 16, pos.getY() + part[1] / 16, pos.getZ() + part[2] / 16,
                            pos.getX() + part[3] / 16, pos.getY() + part[4] / 16, pos.getZ() + part[5] / 16,
                            LANTERN_COLOR >> 16 & 0xFF, LANTERN_COLOR >> 8 & 0xFF, LANTERN_COLOR & 0xFF, alpha);
                }
            }
        }
        BuiltBuffer built = buffer.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
        matrices.pop();

        processor.render(tickDelta);
        client.getFramebuffer().beginWrite(false);
        // même mélange que le jeu pour ses propres contours
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        glow.draw(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight(), false);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    /** Charge le traitement à la première lueur, et le recale si la fenêtre a changé de taille. */
    private static boolean ensureProcessor() {
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        if (processor == null) {
            if (failed) {
                return false;
            }
            try {
                processor = new PostEffectProcessor(client.getTextureManager(), client.getResourceManager(),
                        client.getFramebuffer(), OUTLINE_EFFECT);
            } catch (IOException | JsonSyntaxException exception) {
                // sans le traitement, pas de lueur : on ne réessaie pas à chaque image
                LOGGER.warn("Impossible de charger {} pour le sens de la lampe frontale", OUTLINE_EFFECT, exception);
                failed = true;
                return false;
            }
            processor.setupDimensions(width, height);
            processorWidth = width;
            processorHeight = height;
        } else if (width != processorWidth || height != processorHeight) {
            processor.setupDimensions(width, height);
            processorWidth = width;
            processorHeight = height;
        }
        return true;
    }

    /** Les ressources rechargées, le traitement est rebâti à la prochaine lueur. */
    private static void release() {
        if (processor != null) {
            processor.close();
            processor = null;
        }
        failed = false;
    }

    /**
     * La lueur monte vite et retombe doucement : le premier dixième du battement suffit à
     * l'allumer, le reste l'éteint, ce qui donne une pulsation plutôt qu'un clignotement.
     */
    private static int alphaOf(Pulse pulse) {
        return alphaOf(pulse.total(), pulse.left());
    }

    private static int alphaOf(int total, int left) {
        float progress = 1.0f - (float) left / Math.max(1, total);
        float strength = progress < 0.1f ? progress / 0.1f : 1.0f - (progress - 0.1f) / 0.9f;
        return MathHelper.clamp((int) (strength * 255), 0, 255);
    }

    private static void cube(BufferBuilder buffer, Matrix4f matrix, BlockPos pos,
                             int red, int green, int blue, int alpha) {
        box(buffer, matrix, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0f, pos.getY() + 1.0f,
                pos.getZ() + 1.0f, red, green, blue, alpha);
    }

    private static void box(BufferBuilder buffer, Matrix4f matrix, float x0, float y0, float z0,
                            float x1, float y1, float z1, int red, int green, int blue, int alpha) {
        face(buffer, matrix, red, green, blue, alpha, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        face(buffer, matrix, red, green, blue, alpha, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        face(buffer, matrix, red, green, blue, alpha, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        face(buffer, matrix, red, green, blue, alpha, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        face(buffer, matrix, red, green, blue, alpha, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        face(buffer, matrix, red, green, blue, alpha, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
    }

    private static void face(BufferBuilder buffer, Matrix4f matrix, int red, int green, int blue, int alpha,
                             float... corners) {
        for (int i = 0; i < corners.length; i += 3) {
            buffer.vertex(matrix, corners[i], corners[i + 1], corners[i + 2]).color(red, green, blue, alpha);
        }
    }
}
