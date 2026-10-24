package com.nowa.powers;

import com.nowa.powers.power.Fury;
import com.nowa.powers.power.Lifesteal;
import com.nowa.powers.power.Piercing;
import com.nowa.powers.power.Reach;
import com.nowa.powers.power.Riposte;
import com.nowa.powers.power.Scholar;
import com.nowa.powers.power.Sobriety;
import com.nowa.powers.power.Vein;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import static com.nowa.NowasDepths.LOGGER;
import static com.nowa.NowasDepths.MOD_ID;

/**
 * Le carrefour des pouvoirs : les greffons et les événements du jeu arrivent tous ici, et d'ici
 * repartent vers le pouvoir concerné. Chaque pouvoir tient dans sa propre classe ({@code powers.power}) ;
 * celle-ci ne sait que <em>quand</em> les appeler.
 *
 * <p>Codés : vol de vie, fureur, érudition, sobriété, portée (lot 1) ; marchand, riposte, reflet,
 * perforation, fonderie (lot 2) ; double saut, rappel, poche de l'End, filon, pêcheur (lot 3). Le
 * marchand, le reflet, la fonderie et le pêcheur passent par leurs greffons ; le double saut, le rappel
 * et la poche de l'End par les messages du client ({@link PowerNetworking}).
 */
public final class PowerHooks {
    /** Tous les combien de tics on remet attributs et effets témoins d'accord avec la bague. */
    private static final int INTERVAL = 5;

    private PowerHooks() {
    }

    public static void register() {
        LOGGER.info("├─ Registering gem power effects for " + MOD_ID);
        PowerEffects.register();
        PowerNetworking.register();
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            Vein.broken(world, player, pos, state);
        });
        ServerLivingEntityEvents.AFTER_DAMAGE.register((victim, source, base, taken, blocked) -> {
            if (melee(source) instanceof PlayerEntity player && player != victim && !blocked) {
                Lifesteal.struck(player, taken);
            }
            if (victim instanceof PlayerEntity hurt && !blocked) {
                Riposte.hurt(hurt, source, taken);
            }
        });
        // les flèches que le joueur tire, à leur entrée dans le monde
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> Piercing.loaded(entity));
        // le coup qui tue n'a pas d'« après dégâts » : il a ôté toute la vie qui restait avant lui
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (melee(source) instanceof PlayerEntity player && player != victim) {
                Lifesteal.killed(player, victim);
            }
        });
    }

    /** Celui qui porte ce coup de mêlée, ou {@code null} si ce n'en est pas un (flèche, épines, chute…). */
    @Nullable
    private static LivingEntity melee(DamageSource source) {
        return source.isOf(DamageTypes.PLAYER_ATTACK) && source.getAttacker() instanceof LivingEntity attacker
                ? attacker : null;
    }

    // ---------- ce que les greffons appellent ----------

    /** À chaque tic du joueur, des deux côtés. */
    public static void tick(PlayerEntity player) {
        if (player.getWorld().isClient || player.age % INTERVAL != 0) {
            return;
        }
        Reach.update(player);
        Powers.Active active = Powers.of(player);
        PowerEffects.sync(player, active.power(), shown(player, active));
    }

    /** Ce que le pouvoir d'un joueur montre parmi ses effets en ce moment ; {@code null} : rien. */
    @Nullable
    private static PowerEffects.Shown shown(PlayerEntity player, Powers.Active active) {
        if (active.power() == null) {
            return null;
        }
        return switch (active.power()) {
            // la fureur ne paraît que blessé : c'est là qu'elle donne quelque chose
            case FURY -> Fury.shown(player) ? PowerEffects.Shown.permanent(active.level()) : null;
            // la riposte ne paraît qu'armée, le temps qu'il lui reste : son icône se grise à mesure
            case RIPOSTE -> Riposte.remaining(player) > 0 ? new PowerEffects.Shown(active.level(), Riposte.remaining(player)) : null;
            default -> PowerEffects.Shown.permanent(active.level());
        };
    }

    /** Les dégâts qu'une entité va recevoir, avant armure : c'est ici qu'un pouvoir les grossit. */
    public static float damage(LivingEntity victim, DamageSource source, float amount) {
        if (victim.getWorld().isClient || !(melee(source) instanceof PlayerEntity player) || player == victim) {
            return amount;
        }
        Lifesteal.before(player, victim);
        return Riposte.damage(player, Fury.damage(player, amount));
    }

    /** La fatigue que le joueur va subir : ce qui lui creuse l'estomac. */
    public static float exhaustion(PlayerEntity player, float amount) {
        return Sobriety.exhaustion(player, amount);
    }

    /** L'expérience que le joueur va gagner. */
    public static int experience(PlayerEntity player, int amount) {
        return Scholar.experience(player, amount);
    }
}
