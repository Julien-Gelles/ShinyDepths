package com.nowa.jewelry;

import net.minecraft.util.Identifier;

import static com.nowa.NowasDepths.MOD_ID;

/**
 * Où se tient la case de la bague, dans les deux écrans d'inventaire du joueur.
 *
 * <p>En survie, juste au-dessus de la seconde main, dans l'espace libre entre le portrait et la
 * grille de fabrication. En créatif, la seconde main est à gauche du portrait ; la bague lui fait
 * pendant à droite, à sa hauteur.
 *
 * <p>Les deux fenêtres du jeu n'ont pas de cadre à cet endroit : on le dessine depuis
 * {@link #TEXTURE} — un cadre de case vanilla marqué de la silhouette d'une bague. La texture ne
 * garde que ce cadre-là.
 */
public final class JewelryPanel {
    public static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/gui/jewelry_slots.png");
    public static final int TEXTURE_WIDTH = 18;
    public static final int TEXTURE_HEIGHT = 18;
    /** Un cadre de case : l'objet, plus un pixel de bordure de chaque côté. */
    public static final int FRAME = 18;

    private JewelryPanel() {
    }

    /** Position de l'objet dans l'inventaire de survie, relative à la fenêtre. */
    public static int survivalX(Worn place) {
        return 77;
    }

    public static int survivalY(Worn place) {
        return 44;
    }

    /** Position de l'objet dans l'onglet inventaire du créatif, relative à la fenêtre. */
    public static int creativeX(Worn place) {
        return 127;
    }

    public static int creativeY(Worn place) {
        return 20;
    }

    /** Hauteur du cadre de cette place dans la texture : il n'y en a qu'un, celui de la bague. */
    public static int frameV(Worn place) {
        return 0;
    }
}
