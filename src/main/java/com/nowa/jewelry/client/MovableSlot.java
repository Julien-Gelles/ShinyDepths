package com.nowa.jewelry.client;

/**
 * Les coordonnées d'une case sont déclarées définitives par le jeu, qui ne les change jamais.
 * L'onglet inventaire du créatif place pourtant lui-même les copies des cases du joueur, et range
 * les bijoux au mauvais endroit : ce contretype, posé sur toutes les cases par {@code SlotMixin},
 * sert à les replacer, côté client uniquement.
 */
public interface MovableSlot {
    void nowasDepths$moveTo(int x, int y);
}
