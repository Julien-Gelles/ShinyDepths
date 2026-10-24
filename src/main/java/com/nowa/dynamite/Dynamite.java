package com.nowa.dynamite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nowa.config.ModConfig;
import com.nowa.item.ModItemTags;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

/**
 * La dynamite : un bâton qu'on allume au briquet et qu'on lance. Allumée, elle explose au bout de sa
 * mèche — dans la main si on la garde — ou au premier contact si on l'a lancée. Éteinte, elle se
 * lance aussi, mais n'explose jamais : elle retombe et se ramasse.
 *
 * <p>La mèche n'est pas un compte à rebours qu'on décrémente : la dynamite allumée porte l'instant
 * où elle explosera. Rien ne change donc sur l'objet de tic en tic — il ne tressaute pas dans la
 * main — et la même donnée sert au bâton tenu, au bâton lancé et à l'animation de la mèche.
 */
public final class Dynamite {
    /** Le nombre de petits cubes de la mèche, donc d'images de son animation (voir les modèles). */
    public static final int FUSE_STAGES = 7;

    /**
     * Une mèche allumée, posée sur la pile entière : elle dit qu'<em>un</em> bâton brûle.
     *
     * @param explodesAt l'instant du monde où elle arrive au bout
     * @param duration   ce qu'elle durait entière, pour savoir où en est la flamme
     * @param counted    le bâton qui brûle fait-il partie du nombre affiché ? Non en général : on
     *                   l'a retiré de la pile en l'allumant, et la pile reste en main derrière lui.
     *                   Oui quand c'était le dernier, puisqu'une pile ne peut pas afficher zéro.
     */
    public record Fuse(long explodesAt, int duration, boolean counted) {
        public static final Codec<Fuse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("explodes_at").forGetter(Fuse::explodesAt),
                Codec.INT.fieldOf("duration").forGetter(Fuse::duration),
                Codec.BOOL.fieldOf("counted").forGetter(Fuse::counted)
        ).apply(instance, Fuse::new));
        public static final PacketCodec<ByteBuf, Fuse> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_LONG, Fuse::explodesAt, PacketCodecs.VAR_INT, Fuse::duration,
                PacketCodecs.BOOL, Fuse::counted, Fuse::new);

        /** L'image de la mèche à cet instant : 0 au bout, {@code FUSE_STAGES - 1} contre le bâton. */
        public int stage(long now) {
            double burnt = 1.0 - (double) (explodesAt - now) / Math.max(1, duration);
            return Math.clamp((int) (burnt * FUSE_STAGES), 0, FUSE_STAGES - 1);
        }
    }

    public static final ComponentType<Fuse> FUSE = Registry.register(Registries.DATA_COMPONENT_TYPE,
            Identifier.of(MOD_ID, "fuse"),
            ComponentType.<Fuse>builder().codec(Fuse.CODEC).packetCodec(Fuse.PACKET_CODEC).build());

    public static final Item ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "dynamite"),
            new DynamiteItem(new Item.Settings().maxCount(ModConfig.i("dynamite.stack_size", 16))));

    public static final EntityType<DynamiteEntity> ENTITY = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "dynamite"),
            EntityType.Builder.<DynamiteEntity>create(DynamiteEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25f, 0.25f).maxTrackingRange(4).trackingTickInterval(10).build());

    /** Durée de la mèche : quatre secondes, comme le bloc de TNT. */
    private static int fuseTicks = 80;
    /** Puissance de l'explosion : la moitié de celle du bloc de TNT (4). */
    private static float power = 2.0f;
    /** Élan du lancer ; une boule de neige part à 1,5. */
    private static float throwSpeed = 1.1f;
    /** Poids en vol ; une boule de neige tombe à 0,03. */
    private static double gravity = 0.05;

    static {
        ModConfig.onApply(() -> {
            fuseTicks = Math.max(1, ModConfig.i("dynamite.fuse_ticks", 80));
            power = (float) Math.max(0.0, ModConfig.d("dynamite.power", 2.0));
            throwSpeed = (float) Math.max(0.1, ModConfig.d("dynamite.throw_speed", 1.1));
            gravity = Math.max(0.0, ModConfig.d("dynamite.gravity", 0.05));
        });
    }

    private Dynamite() {
    }

    public static void register() {
        LOGGER.info("├─ Registering Dynamite for " + MOD_ID);
    }

    static float throwSpeed() {
        return throwSpeed;
    }

    static double gravity() {
        return gravity;
    }

    public static boolean isLit(ItemStack stack) {
        return stack.isOf(ITEM) && stack.contains(FUSE);
    }

    /** La mèche est-elle arrivée au bout ? */
    public static boolean isDue(ItemStack stack, World world) {
        Fuse fuse = stack.get(FUSE);
        return stack.isOf(ITEM) && fuse != null && world.getTime() >= fuse.explodesAt();
    }

    /** De quoi allumer une mèche : le briquet du jeu, ou celui d'un autre mod. */
    public static boolean isLighter(ItemStack stack) {
        return stack.isOf(Items.FLINT_AND_STEEL) || stack.isIn(ConventionalItemTags.IGNITER_TOOLS);
    }

    /**
     * Allume un bâton de la pile tenue dans cette main avec le briquet de l'autre. La pile ne bouge
     * pas : elle perd un bâton — celui qui brûle — et porte sa mèche, si bien qu'on voit en main la
     * dynamite allumée, et qu'une fois celle-ci lancée la pile est toujours là, prête pour la
     * suivante. Mêmes bruits que le bloc de TNT qu'on amorce au briquet.
     */
    public static void light(World world, PlayerEntity player, Hand dynamiteHand) {
        if (world.isClient()) {
            return;
        }
        Hand lighterHand = dynamiteHand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack stack = player.getStackInHand(dynamiteHand);
        // le dernier bâton reste compté : une pile ne peut pas tomber à zéro et rester en main
        boolean taken = stack.getCount() > 1 && !player.isInCreativeMode();
        boolean counted = stack.getCount() == 1 && !player.isInCreativeMode();
        if (taken) {
            stack.decrement(1);
        }
        stack.set(FUSE, new Fuse(world.getTime() + fuseTicks, fuseTicks, counted));
        player.getStackInHand(lighterHand).damage(1, player, LivingEntity.getSlotForHand(lighterHand));
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1.0f,
                world.getRandom().nextFloat() * 0.4f + 0.8f);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    /**
     * Retire de la pile le bâton qui brûlait — lancé, ou explosé : la mèche s'en va, et le bâton
     * aussi s'il était encore compté dans la pile.
     */
    public static void spend(ItemStack stack) {
        Fuse fuse = stack.get(FUSE);
        if (fuse == null) {
            return;
        }
        stack.remove(FUSE);
        if (fuse.counted()) {
            stack.decrement(1);
        }
    }

    /** Le bâton allumé seul, tel qu'il part en vol : la pile reste derrière. */
    public static ItemStack burningStick(ItemStack stack) {
        ItemStack stick = stack.copyWithCount(1);
        Fuse fuse = stack.get(FUSE);
        if (fuse != null) {
            stick.set(FUSE, new Fuse(fuse.explodesAt(), fuse.duration(), true));
        }
        return stick;
    }

    /**
     * L'explosion, deux fois moins forte que celle du bloc de TNT — rayon, dégâts et blocs brisés en
     * découlent — avec le même bruit et les mêmes règles de monde.
     *
     * @param source   le bâton lancé, ou rien si elle explose dans une main : le jeu épargne l'entité
     *                 source, et celui qui la tenait ne doit pas l'être
     * @param culprit  celui à qui l'on impute les dégâts
     */
    public static void explode(World world, @Nullable Entity source, @Nullable LivingEntity culprit, Vec3d pos) {
        if (world.isClient()) {
            return;
        }
        DamageSource damage = source != null
                ? Explosion.createDamageSource(world, source)
                : world.getDamageSources().explosion(null, culprit);
        // le jeu abîme les entités pendant cet appel : le temps qu'il dure, les pierres au sol sont épargnées
        blasting = true;
        try {
            world.createExplosion(source, damage, null, pos.x, pos.y, pos.z, power, false,
                    World.ExplosionSourceType.TNT);
        } finally {
            blasting = false;
        }
    }

    /** Une dynamite est-elle en train d'exploser ? Vrai le seul temps de son explosion. */
    private static boolean blasting;

    /**
     * Cet objet, tombé au sol, survit-il à la dynamite qui explose en ce moment ? Les pierres et les
     * minerais, oui : on fait sauter un filon à plusieurs bâtons, et le second ne doit pas détruire
     * ce que le premier a fait tomber. Toute autre explosion — TNT, creeper — les détruit comme avant.
     */
    public static boolean spares(ItemStack stack) {
        return blasting && stack.isIn(ModItemTags.DYNAMITE_PROOF);
    }
}
