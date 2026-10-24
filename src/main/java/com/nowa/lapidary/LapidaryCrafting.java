package com.nowa.lapidary;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nowa.item.ModItems;
import com.nowa.message.ModMessages;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CrafterScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.nowa.ShinyDepths.MOD_ID;

/**
 * Ce qu'un lapidaire doit savoir avant de forger ses propres outils. Depuis le 2026-10-18, chaque
 * outil du métier — table, polissoirs de pyrite et de cobalt, établi de sertisseur, creuset de
 * fusion — se débloque par sa recette dans l'arbre (voir {@link LapidarySkillTree.Recipe}), et non
 * plus à un niveau. Le polissoir de pierre et le sachet restent libres : le métier commence avec eux.
 *
 * <p>La progression se joue à la <em>fabrication</em>, pas à l'usage : une fois l'outil en main, il
 * sert sans condition, même entre les mains de quelqu'un qui n'a jamais taillé une pierre.
 */
public final class LapidaryCrafting {
    /**
     * Le joueur dont un générateur automatique tient son métier, et les recettes qu'on lui connaît.
     * Une sauvegarde d'avant l'arbre du 2026-10-18 n'a pas la liste : la machine ne sait alors rien
     * fabriquer du métier tant que son joueur n'est pas revenu.
     */
    public record Owner(UUID id, List<String> recipes) {
        public static final Codec<Owner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Uuids.CODEC.fieldOf("id").forGetter(Owner::id),
                Codec.STRING.listOf().optionalFieldOf("recipes", List.of()).forGetter(Owner::recipes)
        ).apply(instance, Owner::new));
    }

    /**
     * Le métier attaché à un générateur automatique : un seul joueur à la fois, le dernier à y avoir
     * eu les mains. Il vit dans le bloc : le casser l'oublie, et les blocs récupérés s'empilent comme
     * les autres.
     */
    public static final AttachmentType<Owner> CRAFTER_OWNER = AttachmentRegistry.create(
            Identifier.of(MOD_ID, "crafter_owner"), builder -> builder.persistent(Owner.CODEC));

    /**
     * Le générateur dont on cherche la recette en ce moment. Le jeu la cherche dans une méthode
     * statique, qui ne sait rien du bloc qui l'appelle : on note donc sa position le temps de la
     * recherche, que ce soit pour fabriquer ou pour montrer l'aperçu.
     */
    private static final ThreadLocal<BlockPos> CURRENT_CRAFTER = new ThreadLocal<>();

    private LapidaryCrafting() {
    }

    public static void enterCrafter(BlockPos pos) {
        CURRENT_CRAFTER.set(pos);
    }

    public static void leaveCrafter() {
        CURRENT_CRAFTER.remove();
    }

    public static @Nullable BlockPos currentCrafter() {
        return CURRENT_CRAFTER.get();
    }

    /**
     * Le générateur prend le métier du dernier joueur à y avoir eu les mains : celui qui le pose
     * d'abord, puis chacun de ceux qui <em>referment</em> son écran, l'un chassant l'autre. Un
     * maître ne peut donc pas poser une machine pour un novice et la lui laisser : dès que le novice
     * y touche, ce sont ses recettes qui comptent. (Il peut encore l'alimenter par des entonnoirs
     * sans jamais l'ouvrir — c'est moins simple, et c'est le but.)
     *
     * <p>La fermeture plutôt que l'ouverture, parce que plusieurs joueurs peuvent ouvrir le même
     * générateur : on retient ainsi le dernier à en être sorti. Qui regarde l'écran pendant ce temps
     * n'entre pas en compte — on ne fabrique pas depuis l'écran, il faut en sortir pour actionner la
     * machine.
     */
    public static void remember(World world, BlockPos pos, @Nullable LivingEntity user) {
        if (world.isClient() || !(user instanceof PlayerEntity player) || player.isSpectator()) {
            return;
        }
        BlockEntity block = world.getBlockEntity(pos);
        if (block != null) {
            block.setAttached(CRAFTER_OWNER, new Owner(player.getUuid(), known(player)));
        }
    }

    /**
     * Un joueur referme l'écran : c'est lui que le générateur retient désormais. Ceux qui regardent
     * encore la grille voient leur aperçu recalculé, puisque la machine ne répond plus au même métier.
     */
    public static void closed(ServerPlayerEntity player, CrafterBlockEntity block) {
        if (player.isSpectator()) {
            return;
        }
        remember(player.getWorld(), block.getPos(), player);
        for (ServerPlayerEntity other : player.getServer().getPlayerManager().getPlayerList()) {
            if (other != player && other.currentScreenHandler instanceof CrafterScreenHandler screen
                    && screen.getInputInventory() == block && screen instanceof CrafterPreview preview) {
                preview.shinyDepths$refreshPreview();
            }
        }
    }

    /**
     * Un générateur ne fabrique l'outillage du lapidaire que si le joueur qu'il a retenu sait le
     * faire. Ses recettes sont relues tant que ce joueur est connecté — débloquer la recette suffit
     * donc à remettre la machine en marche, sans la reposer ni la rouvrir — et sinon on s'en tient à
     * celles notées. Un générateur que personne n'a posé ni ouvert n'a aucun métier derrière lui.
     */
    public static boolean crafterCanCraft(ServerWorld world, BlockPos pos, ItemStack result) {
        LapidarySkillTree.Recipe recipe = LapidarySkillTree.recipeFor(result.getItem());
        if (recipe == null) {
            return true;
        }
        BlockEntity block = world.getBlockEntity(pos);
        Owner owner = block == null ? null : block.getAttached(CRAFTER_OWNER);
        if (owner == null) {
            return false;
        }
        PlayerEntity player = world.getServer().getPlayerManager().getPlayer(owner.id());
        if (player == null) {
            return owner.recipes().contains(recipe.nodeId());
        }
        List<String> known = known(player);
        if (!known.equals(owner.recipes())) {
            block.setAttached(CRAFTER_OWNER, new Owner(owner.id(), known));
        }
        return known.contains(recipe.nodeId());
    }

    /** Les recettes du métier que ce joueur a débloquées dans l'arbre. */
    private static List<String> known(PlayerEntity player) {
        LapidaryData data = Lapidary.get(player);
        return Arrays.stream(LapidarySkillTree.Recipe.values())
                .map(LapidarySkillTree.Recipe::nodeId).filter(data::isUnlocked).toList();
    }

    /** Le joueur peut-il forger cet objet ? S'il est réservé au métier, il faut en avoir débloqué la recette. */
    public static boolean canCraft(PlayerEntity player, ItemStack result) {
        LapidarySkillTree.Recipe recipe = LapidarySkillTree.recipeFor(result.getItem());
        return recipe == null || Lapidary.get(player).isUnlocked(recipe.nodeId());
    }

    /** Dit pourquoi l'établi reste vide — une fois, l'identifiant du message empêchant l'empilement. */
    public static void explain(ServerPlayerEntity player, ItemStack result) {
        LapidarySkillTree.Recipe recipe = LapidarySkillTree.recipeFor(result.getItem());
        LapidarySkillTree.Node node = recipe == null ? null : LapidarySkillTree.get(recipe.nodeId());
        if (node == null) {
            return;
        }
        ModMessages.send(player, new ModMessages.Message("craft_level",
                Text.translatable("lapidary.shiny-depths.craft.locked", result.getName()),
                Text.translatable("lapidary.shiny-depths.craft.locked.detail", Text.translatable(node.translationKey())),
                new ItemStack(ModItems.POLISHER)));
    }
}
