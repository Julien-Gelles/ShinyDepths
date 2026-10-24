# Lapidaire — Nowa's Depths

Le métier du mod : on polit des pierres, on gagne de l'expérience de lapidaire, et chaque niveau
donne un point à dépenser dans l'arbre de compétences (touche par défaut, écran dédié).

---

## Niveaux

Passer du niveau *n* au niveau *n+1* coûte **40 + 20n** points d'expérience : 40 pour le premier,
640 pour le trente-et-unième, soit **9 900 en tout pour le niveau 30**.

Il n'y a **pas de niveau maximum**. Tout l'arbre coûte **75 points** et se termine donc au niveau
75, mais on continue de monter ensuite ; les points s'accumulent alors sans emploi, en attendant
qu'une version ultérieure leur en trouve un.

| Source d'XP | Gain |
|---|---|
| Polir une pierre de dureté 1 / 2 / 3 / 4 | 4 / 8 / 16 / 32 |
| Ouvrir une géode | 32 |

L'XP est versée à qui **récupère** les pierres, pas à qui les a posées dans la table : c'est le
même principe que la pureté, tirée au moment où un joueur prend le sachet.

## Ce que le métier réserve

Un lapidaire ne forge son propre outillage qu'après en avoir **débloqué la recette dans l'arbre**
(depuis le 2026-10-18 ; c'était avant un niveau). C'est le **seul verrou** du métier : une fois
l'objet fait, il sert sans condition, y compris entre les mains de quelqu'un qui n'a jamais taillé
une pierre. Ce qui sépare alors le maître du novice, ce sont les chances que l'arbre lui donne.

| Recette | Nœud de l'arbre | Coût |
|---|---|---|
| Polissoir, sachet à pierres | aucun : le métier commence avec eux | — |
| Table de lapidaire | Taille, après Coup d'œil I | 2 |
| Loupe de lapidaire | Taille, après Coup d'œil II (depuis le 2026-10-19) | 1 |
| Polissoir en pyrite | Taille, après Main sûre I | 2 |
| Polissoir en cobalt | Taille, après le polissoir en pyrite | 3 |
| Établi de sertisseur | Atelier, dès la racine | 2 |
| Creuset de fusion | Atelier, après l'établi | 3 |

Il n'y a plus qu'**une table de lapidaire** (depuis le 2026-10-14), à l'allure de netherite, à la
recette de l'ancienne table en fer (lingots de fer, polissoir, planches). Elle polit toutes les
pierres, à la plus grande vitesse.

L'établi reste vide tant que la recette manque, et un message dit quel nœud débloquer — plutôt
qu'une recette qui ne donne rien sans raison.

**Le générateur automatique** tient son métier d'**un seul joueur à la fois** : le dernier à y avoir
eu les mains. C'est d'abord celui qui le pose, puis chacun de ceux qui **referment** son écran, l'un
chassant l'autre. Le bloc retient son nom et ses recettes, et ne fabrique l'outillage du lapidaire que
si ce joueur-là sait le faire.

C'est voulu : un lapidaire de haut niveau ne peut pas poser une machine pour un novice et la lui
laisser — dès que le novice y touche et referme, ce sont ses recettes qui comptent. Il reste possible de
l'alimenter par des entonnoirs sans jamais l'ouvrir ; c'est moins simple, et c'est le but.

Pourquoi la fermeture plutôt que l'ouverture : plusieurs joueurs peuvent ouvrir le même générateur,
et l'on retient ainsi le dernier à en être sorti. Qui regarde l'écran entre-temps ne compte pas : on
ne fabrique pas depuis l'écran, il faut en sortir pour actionner la machine.

Les recettes du joueur retenu se relisent tant qu'il est connecté — débloquer une recette remet
donc la machine en marche sans avoir à la reposer ni la rouvrir — et à défaut, les dernières notées
font foi (une machine posée avant l'arbre du 2026-10-18 ne sait rien tant que son joueur n'est pas
revenu). Casser le bloc efface cette mémoire, si bien que les générateurs récupérés s'empilent comme
n'importe quel autre. Un générateur que personne n'a posé ni ouvert — dispensé, généré, placé par
commande — ne fabrique rien du métier.

Ce que la machine refuserait de fabriquer **ne s'affiche pas** dans sa case de sortie : l'aperçu
passe par la même vérification que la fabrication, donc par le joueur retenu. Il est recalculé pour
ceux qui regardent encore quand un autre joueur referme l'écran.

## L'arbre

Refait de zéro le 2026-10-18. **Chaque nœud coûte des points** — un point par niveau gagné — et ne
demande **que son parent**, jamais un niveau : on peut foncer dans une branche dès le début. Trois
branches partent de la racine ; à l'écran, l'arbre se lit de gauche à droite, la Taille au-dessus de
la racine, la Prospection et l'Atelier en dessous. La molette le fait défiler de haut en bas, Maj +
molette de gauche à droite. À gauche de la fenêtre, des **onglets verticaux** — ceux de l'écran des progrès —, un par métier,
chacun avec l'arbre de son métier. Il n'y en a qu'un pour l'instant, le lapidaire (icône : le
polissoir). Les coûts se règlent sous `lapidary.tree.<nœud>.cost`.

**Les coûts montent avec l'avancée** (2026-10-19, sa demande) : 1, 2 ou 3 points, le premier
rang d'une branche le moins cher, sans que chaque fin de branche coûte 3 ; 75 points en tout. **La forme du cadre dit le coût** — les trois formes des
progrès du jeu : carré pour 1 point, arrondi pour 2, à pointes pour 3. Obtenu et disponible
reprennent les cadres du jeu ; indisponible, les mêmes formes repeintes en sombre
(`textures/gui/sprites/lapidary/*_frame_locked.png`, tirés des cadres du jeu par script). La
racine garde son cadre à pointes doré.

### Livres — le guide du mod (gratuits)

Disposition (2026-10-19, sa demande) : la **racine seule dans la première colonne** ; une barre
part d'elle vers la droite jusqu'au **tronc**, la ligne verticale d'où partent toutes les branches.
Les livres occupent la **deuxième colonne**, entre la racine et le tronc : le premier pend de cette
barre, les suivants à la verticale en dessous. Les branches commencent à la troisième colonne
(`BOOK_X`, `TRUNK_X`, `FIRST_COLUMN` dans `LapidarySkillTree`). Trois nœuds de
livre, **gratuits** (coût 0, réglable comme les autres), chacun après le précédent. Ils donneront
les **tomes I, II et III du guide du mod**, des livres à écrire : pour l'instant les débloquer ne
donne rien, et leur infobulle le dit (« à venir »). Un nœud gratuit affiche « Clic : débloquer
(gratuit) » et prend le cadre carré.

| Nœud | Prérequis | Coût | Effet |
|---|---|---|---|
| **Livre du lapidaire I** | racine | 0 | Tome I du guide (à venir) |
| **Livre du lapidaire II** | Livre I | 0 | Tome II du guide (à venir) |
| **Livre du lapidaire III** | Livre II | 0 | Tome III du guide (à venir) |

### Taille — la pierre, et de quoi la tailler (30 points)

| Nœud | Prérequis | Coût par rang | Effet par rang |
|---|---|---|---|
| **Coup d'œil** I → V | racine | 1, 1, 2, 2, 3 | Déplace les chances de pureté au polissage (voir minerais.md) |
| **Table de lapidaire** | Coup d'œil I | 2 | Recette |
| **Loupe de lapidaire** | Coup d'œil II | 1 | Recette |
| **Main sûre** I → II | Coup d'œil I | 1, 2 | Le polissoir ne dépense plus que deux tiers, puis la moitié, de sa durabilité |
| **Polissage vif** I → II | Main sûre I | 1, 2 | +25 %, puis +50 % de vitesse d'utilisation du polissoir (durée × 0,8, puis × 0,667 ; réglage `lapidary.skills.quick_polish.time`) |
| **Polissoir en pyrite** | Main sûre I | 2 | Recette |
| **Polissoir en cobalt** | Polissoir en pyrite | 3 | Recette |
| **Facette** I → III | Coup d'œil II | 2, 2, 3 | 6 %, 12 %, 20 % de chances qu'une pierre estimée en donne deux |

### Prospection — ce qu'on rapporte de la mine (13 points)

| Nœud | Prérequis | Coût par rang | Effet par rang |
|---|---|---|---|
| **Gisement** I → III | racine | 1, 2, 3 | 10 %, 20 %, 30 % de chances qu'un minerai du mod rende une matière brute de plus |
| **Abattage** I → II | Gisement I | 1, 2 | Mine les minerais du mod 25 %, puis 50 % plus vite |
| **Nodule** I → II | Gisement II | 2, 2 | 15 %, 30 % de chances qu'une roche à géode rende un nodule de plus |

Le supplément de Gisement et de Nodule se pose **à côté** du butin normal : Fortune ne le multiplie
pas, et le Toucher de soie l'annule — la roche part entière, il n'y a rien à y ajouter. (Gisement
s'appelait Filon ; renommé pour ne pas se confondre avec le pouvoir du diamant.)

### Atelier — les machines, l'étude et le commerce (32 points)

| Nœud | Prérequis | Coût par rang | Effet par rang |
|---|---|---|---|
| **Étude** I → III | racine | 1, 2, 3 | +10 %, +20 %, +30 % d'XP de lapidaire, quelle qu'en soit la source |
| **Établi de sertisseur** | racine | 2 | Recette |
| **Creuset de fusion** | Établi | 3 | Recette |
| **Fusion rapide** I → II | Creuset | 2, 2 | La fusion dure 70 %, puis 40 % du temps (5 s → 3,5 s → 2 s) |
| **Récupération** | Creuset | 2 | 5 % de chances qu'une fusion épargne une des quatre pierres, tirée au hasard, qui reste dans sa coupelle |
| **Récupération multiple** I → II | Récupération | 2, 2 | Quand la Récupération joue, elle épargne 2, puis 3 pierres |
| **Récupération sûre** I → II | Récupération | 2, 3 | La Récupération joue 15 %, puis 30 % du temps |
| **Négoce** I → III | racine | 1, 2, 3 | Le villageois lapidaire demande 10 %, 20 %, 30 % de moins : moins d'émeraudes quand il vend, moins de pierres quand il achète |

**Le creuset applique le savoir de son lapidaire** : celui qui l'a posé, puis le dernier à l'avoir
ouvert. Il retient ses rangs de Fusion rapide et des trois Récupérations, et s'en sert même nourri par des
entonnoirs. La remise du Négoce passe par le même chemin que celle du Héros du village : posée quand
le villageois montre ses offres, effacée à la fin de l'échange, arrondie au plus proche.

Retirés le 2026-10-18 : **Extraction** (le retrait d'une pierre est sans risque depuis le
2026-10-14) et **Doigté** (il rabattait des niveaux de fabrication, qui n'existent plus). Les
points qu'on y avait mis sont rendus, comme ceux de l'ancien Filon : un nœud qui n'existe plus est
oublié à la connexion.

Textes des infobulles (2026-10-19, sa demande) : la racine affiche les chances de pureté comme
Coup d'œil ; les bonus se disent en « ±X % de … en utilisant … » (Main sûre, Polissage vif,
Abattage, Fusion rapide) ; les nombres s'écrivent en chiffres (« 1 des 4 pierres »). Infobulle
large de 170 px au plus.

Refaite le 2026-10-19 : la **Récupération** n'a plus qu'un rang (5 %), d'où partent deux branches
(sa demande) — Récupération multiple sur la ligne du nœud, Récupération sûre sur la ligne en
dessous, à côté du Négoce. Les deux se combinent : avec tout, 30 % de chances que trois pierres
sur quatre soient épargnées. L'ancien rang II est rendu. Réglages :
`lapidary.skills.recovery.spare_chance` (0 ; 0,05 ; 0,15 ; 0,30) et
`lapidary.skills.recovery.spare_count` (0, 1, 2, 3) — indice 0 sans Récupération, puis la
Récupération seule, puis les rangs de la branche.

## Où tout cela vit

- `LapidarySkillTree` : les nœuds, leurs coordonnées, leurs prérequis et leurs coûts. Une compétence
  est une chaîne de rangs (`facet_1`, `facet_2`…) ; le reste du mod ne demande jamais qu'un rang.
  Une recette est un nœud seul (`recipe_lapidary_table`…).
- `LapidaryCrafting` : les recettes réservées, et la mémoire des générateurs automatiques.
- `LapidaryBonuses` : **tous les chiffres ci-dessus**, et eux seuls. Les endroits qui les
  appliquent — polissoir, sachet, minage, creuset, villageois — se contentent d'interroger cette classe, si bien
  qu'un équilibrage se fait dans un seul fichier.
- `LapidaryMining` : les suppléments au minage, accrochés à la casse d'un bloc.
- `LapidaryLevels` : la courbe, sans plafond.

Les descriptions affichées dans l'arbre recopient ces valeurs dans les fichiers de langue : elles
sont à reprendre en même temps qu'un chiffre change. Seules les chances de pureté (racine et Coup
d'œil) sont lues dans le code et affichées en liste, une classe par ligne à sa couleur : elles
suivent toujours la configuration.

## Les onglets du créatif

Depuis le 2026-10-22 (sa demande), le mod a **deux onglets** au lieu d'un (`ModItemGroups`) :

- **Nowa's Depths : Lapidaire**, le polissoir pour emblème : polissoirs, loupe, sachets, table,
  établi, creuset, vitrine, pierres brutes et géode, pierres polies, blocs de pierres, bagues ;
- **Nowa's Depths : Mineur**, la lampe frontale pour emblème — ce qui touche au futur métier de
  mineur : lampe, dynamite, lanterne et chaîne de cobalt, métaux bruts, lingots, pépites et leurs
  blocs, minerais dans la roche (pierres et géodes comprises), panoplies.
