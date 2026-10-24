package com.nowa.item;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.nowa.ShinyDepths.LOGGER;
import static com.nowa.ShinyDepths.MOD_ID;

public class ModDataComponents {
    /** Classe de pureté d'un joyau poli. */
    public static final ComponentType<Purity> PURITY = register("purity",
            ComponentType.<Purity>builder().codec(Purity.CODEC).packetCodec(Purity.PACKET_CODEC).build());

    /**
     * Marque une pierre polie du jeu — diamant, émeraude — qui sort de la table et n'a pas encore été
     * estimée. Une pierre du mod sans pureté sort forcément de la taille ; un diamant sans pureté,
     * lui, peut aussi venir d'un coffre ou d'un villageois, et celui-là n'est pas estimé par le
     * sachet : la marque fait la différence.
     */
    public static final ComponentType<net.minecraft.util.Unit> UNAPPRAISED = register("unappraised",
            ComponentType.<net.minecraft.util.Unit>builder().codec(net.minecraft.util.Unit.CODEC)
                    .packetCodec(net.minecraft.network.codec.PacketCodec.unit(net.minecraft.util.Unit.INSTANCE)).build());

    /** Contenu d'un sachet à pierres (4 cases). */
    public static final ComponentType<PouchContents> POUCH_CONTENTS = register("pouch_contents",
            ComponentType.<PouchContents>builder().codec(PouchContents.CODEC).packetCodec(PouchContents.PACKET_CODEC).build());

    /** Les pierres serties sur une arme, un outil ou une armure. */
    public static final ComponentType<com.nowa.socket.Sockets> SOCKETS = register("sockets",
            ComponentType.<com.nowa.socket.Sockets>builder().codec(com.nowa.socket.Sockets.CODEC)
                    .packetCodec(com.nowa.socket.Sockets.PACKET_CODEC).build());

    /**
     * L'accord que donnait la Résonance, une compétence retirée. Plus rien ne le lit ni ne l'écrit ;
     * le type reste enregistré pour que les pierres qui le portent encore se chargent sans erreur.
     */
    public static final ComponentType<Float> RESONANCE = register("resonance",
            ComponentType.<Float>builder().codec(com.mojang.serialization.Codec.FLOAT)
                    .packetCodec(net.minecraft.network.codec.PacketCodecs.FLOAT).build());


    private static <T> ComponentType<T> register(String name, ComponentType<T> type) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, name), type);
    }

    public static void registerDataComponents() {
        LOGGER.info("├─ Registering Mod Data Components for " + MOD_ID);
    }
}
