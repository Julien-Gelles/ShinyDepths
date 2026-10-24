package com.nowa.lapidary;

/**
 * Posé sur l'écran du générateur automatique par {@code CrafterScreenHandlerMixin} : de quoi lui
 * faire recalculer son aperçu quand ce n'est pas la grille qui a changé, mais le joueur retenu.
 */
public interface CrafterPreview {
    void shinyDepths$refreshPreview();
}
