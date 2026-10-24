# Sertissage — Shiny Depths

Ce que deviennent les pierres polies : on en sertit une sur une **bague**, dont elle fait le pouvoir
(voir [bijoux.md](bijoux.md)), ou une sur la **lampe frontale**, dont elle fait luire le
minerai (voir [lampe-frontale.md](lampe-frontale.md)). Tout se fait à l'établi de sertisseur.

---

## Décisions (refonte du 2026-10-14)

| Question | Choix |
|---|---|
| Supports | La **bague** et la **lampe frontale**, et rien d'autre. Les armes, les outils et les armures ne prennent plus de pierres ; les colliers et les bracelets n'existent plus. |
| Emplacements | **Un**, sur la bague comme sur la lampe |
| Ce qui va sur une bague | Une pierre qui donne un pouvoir : toutes les pierres polies, émeraude comprise |
| Ce qui va sur la lampe | Une pierre qui dort dans la roche, comme avant |
| Rôle de la pureté | Elle fait le **niveau** du pouvoir, de 1 (ébréchée) à 5 (philosophale). Une pierre **sans pureté ne se sertit pas** |
| Niveau de lapidaire, risque | **Aucun** : poser et retirer une pierre est toujours sûr. Le risque n'apportait que de la frustration |
| Le surplus de pierres | La **fusion des puretés**, au creuset de fusion : quatre pierres identiques en font une de la pureté au-dessus |

Les objets sertis avant la refonte gardent leurs pierres dans leurs données, mais elles ne font plus
rien et ne se reprennent pas : elles sont perdues.

## L'établi de sertisseur

Un bloc à part, distinct de la table de lapidaire, avec **un seul écran**.

### Le meuble

Comme la table de lapidaire, c'est un établi de **deux blocs de large, deux de haut, un de
profondeur**, dans le même bois rouge et sur les mêmes pieds, aux couleurs de la table de lapidaire
**en netherite** : chants, poignées, machine et outils sont de ce métal. On en pose un bloc, les trois autres
viennent avec ; il faut la place des quatre. On en casse un, tout part, et l'objet tombe une fois.
Cliquer n'importe laquelle des quatre parties ouvre le même écran.

Vu de face :

- **à gauche**, un corps à deux portes sous un plan de travail, qui porte la **machine à sertir** —
  un socle, une colonne, un bras et son poinçon au-dessus d'une pierre, un levier, un volant — et un
  plateau à pierres, long et étroit, où sont posées à plat, en désordre, trois vraies pierres du mod
  (un péridot, une pierre de lune et un grenat : un décor, toujours là, qu'on ne peut pas prendre ; leur
  disposition est calculée pour qu'aucune n'en touche une autre) ; au mur, un tournevis et une clé à
  molette ;
- **à droite**, un petit **présentoir à bague** : un socle de bois sombre, un coussin bas de laine
  brune et trois petits pics de métal — un court devant, deux plus hauts derrière, à gauche et à
  droite — où la bague se pose, inclinée.
  Derrière lui, au mur, une barre où pendent trois clés plates, un marteau de bijoutier et une
  paire de brucelles.

Un seul meuble, pas deux blocs côte à côte : depuis la refonte du 2026-10-14 (plus d'armes ni
d'armures, donc plus de mannequin), le **plan de travail** et le **panneau de fond** avec sa
corniche courent **sur les deux blocs**, au-dessus de quatre portes — comme la table de lapidaire et
le creuset de fusion. Le panneau est à la hauteur de celui de la table de lapidaire (28 pixels).

**L'établi garde ce qu'on y laisse.** L'objet et la pierre posés, et ce qu'un travail a rendu sans
qu'on l'ait repris, restent dans le bloc quand on ferme l'écran : on peut y revenir plus tard. Le contenu est celui du bloc, pas celui d'un joueur : un
autre qui ouvre l'établi y trouve la même chose. Casser l'établi fait tout tomber. Un entonnoir ne
peut rien y mettre ni y prendre — ce contenu n'est pas un inventaire au sens du jeu.

Deux choses s'en voient sur le bloc, pour tout le monde :

- **l'objet posé dans l'établi** — la bague, ou la lampe — se tient sur le présentoir, incliné vers
  l'arrière sur ses trois pics, face à celui qui regarde, tant qu'il y reste ; on ne peut pas l'y prendre, il
  faut passer par l'écran ;
- **la pierre sous le poinçon** de la machine est **la pierre de l'établi** : celle de la case de
  la pierre (la deuxième entrée), et elle seule. C'est la pierre elle-même, posé à plat
  sur l'enclumette. Sans pierre, l'enclumette est nue.

**Sa forme est un escalier d'avant en arrière**, comme celle de la table de lapidaire : le meuble
plein jusqu'au plan de travail ; au-dessus, à gauche, une marche haute de la machine à sertir jusqu'au
fond ; à droite, une marche basse dès le présentoir, puis une haute au panneau du fond. Les
boîtes sont écrites une fois, dans le repère de l'établi entier, dans `SocketingBenchBlock.SHAPES` ;
`BenchBlock.shapes` les découpe sur les quatre blocs et les tourne selon l'orientation.

Les modèles (`models/block/socketing_bench_*.json`) sont écrits par un script, sans texture
nouvelle : le bois et le plan de la table de lapidaire (flancs compris : des planches lisses), la laine brune du jeu pour le
coussin, et des blocs du jeu. Le code commun aux trois établis est `com.nowa.block.BenchBlock` ; le
contenu vit dans `SocketingBenchBlockEntity` ; l'objet présenté et les pierres sont dessinés par
`SocketingBenchRenderer`.

### L'écran

À gauche, la case de **l'objet** (une bague ou une lampe) ; vide, elle montre le **contour d'une
bague**. Au milieu, sans objet, les **quatre bagues** du mod en petit, comme la table de lapidaire
montre ses pierres (survolées, leur infobulle). Dès qu'un objet est posé, il est dessiné **en grand**
(×4) et la case de **la pierre** apparaît dessus, à la place de sa pierre — le chaton en haut de la
bague, la lentille de la lampe : un voile gris à demi transparent où l'objet se devine, avec, vide,
le **seul contour d'une pierre** (celui de l'émeraude d'attente du jeu, sans son dessin intérieur).
Sans objet, la case de la pierre n'existe pas — sauf si une pierre y est restée, pour la reprendre.
Puis une flèche, et deux sorties à droite — **l'objet** et **la pierre qu'il rend**. Le fond reprend
celui de l'établi du jeu, plus haut de 24 pixels.

| On pose | Il sort |
|---|---|
| Une bague vide et une pierre | La bague sertie |
| Une bague sertie, sans pierre | La bague vide, et sa pierre |
| Une bague sertie et une pierre | La bague sertie de la nouvelle pierre, et l'ancienne |

La lampe frontale suit exactement les mêmes règles.

Tant qu'on n'a rien pris, les sorties ne sont qu'un **aperçu**. Prendre l'une d'elles fait le
travail : l'objet et une pierre sont consommés — un coup d'enclume —, et l'autre sortie, s'il y en a
une, devient réelle : elle attend qu'on la reprenne, et l'établi ne propose rien d'autre d'ici là.
Casser l'établi fait tomber les entrées et ce qui attend, jamais l'aperçu. Le clic-majuscule range
une bague ou une lampe dans la case de l'objet, une pierre dans celle de la pierre.

Ce qu'une bague donnera se lit dans l'**infobulle** de la bague qui sort : une ligne à la couleur
de la pierre dit « Vol de vie — niveau IV ».

## La fusion des puretés : le creuset de fusion

**Quatre pierres polies identiques** — même pierre, même pureté — donnent **une pierre de la pureté
au-dessus** : ébréchée → terne → limpide → pure → philosophale. La philosophale ne fusionne plus.

Cela se fait au **creuset de fusion**, le troisième établi du métier : le même meuble de quatre
blocs que la table de lapidaire et l'établi de sertisseur — même bois rouge, mêmes pieds, même plan
de travail et même panneau de fond, sur toute la largeur. À gauche, deux tiroirs sous le plan et,
dessus, **quatre coupelles** en carré ; à droite, la **gueule allumée d'un four** (celle du haut
fourneau du jeu) sous le plan et, dessus, **le creuset** (le chaudron du jeu) avec son **bain de
lave**, sous une hotte. Au mur, une longue scie à métaux, et un serre-joint dessous. Aucune texture nouvelle.

- **L'écran**, en peigne, reprend le fond du four du jeu, plus haut de 24 pixels : les quatre cases
  des coupelles en rangée en haut ; sous chacune une rigole, creusée comme une case, qui descend ;
  elles se rejoignent en un collecteur, et une seule chute tombe dans la grande case du creuset, en
  bas au milieu. Pendant la fusion, la **lave** (celle du jeu) remplit les rigoles à la place de la
  flèche du four : elle descend sous les coupelles, court vers le milieu, puis tombe dans le creuset.
- **Il travaille seul**, comme un four : quand ses quatre coupelles tiennent la même pierre, la
  fusion prend **5 secondes**, consomme une pierre de chaque coupelle et pose le résultat dans le
  creuset. Quatre piles de huit font huit fusions d'affilée. Il s'arrête si le creuset est plein.
- **Le clic-majuscule** répartit une pile sur les quatre coupelles, une pierre à la fois dans la
  moins remplie.
- **Un entonnoir** le nourrit par le dessus et les côtés, en répartissant de même, et le vide par le
  dessous. Un comparateur lit son contenu.
- **Tout se voit sur le bloc** : chaque pierre posée dans sa coupelle, et la pierre fondue sur le
  bain du creuset.
- **Sa forme** est en escalier, comme les autres établis : à gauche une marche basse sur les
  coupelles, puis le panneau du fond ; à droite, le creuset et sa hotte.
- **Fabrication** : `GCG` / `IBI` — deux pierres polies, un chaudron, deux lingots de fer, un haut
  fourneau. Se débloque dans l'arbre du lapidaire (Atelier, après l'établi, 3 points).

La durée se règle : `gems.fusion_seconds` (5).

## Où tout cela vit

- `Socketing` : ce qui se sertit, où, poser et retirer. `SocketFamily` : la bague et la lampe.
  `Sockets` : le composant des pierres serties.
- `SocketingBenchScreenHandler` / `SocketingBenchScreen` : l'écran ; `SocketingBenchBlockEntity` : ce
  que garde le bloc ; `SocketingBenchRenderer` : l'objet sur le présentoir et la pierre sous le poinçon.
- `GemFusion` : la règle de la fusion. `FusionCrucibleBlock`, `FusionCrucibleBlockEntity` (le
  travail, l'entonnoir), `FusionCrucibleScreenHandler` / `FusionCrucibleScreen`,
  `FusionCrucibleRenderer` (les pierres sur le bloc). Le modèle est écrit par un script, dans le
  repère de l'établi entier, comme ceux des deux autres établis.
- `SocketTooltip` : l'infobulle d'un objet serti.
