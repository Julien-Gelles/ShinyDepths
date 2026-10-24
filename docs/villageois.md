# Villageois lapidaire — Nowa's Depths

Un nouveau métier de villageois. Le lapidaire vend des pierres polies contre des émeraudes — des
plus médiocres aux plus belles à mesure qu'il monte en niveau — et rachète celles du joueur.

---

## Son poste de travail

**La vitrine à pierres**, un bloc fait pour cela, aux **cotes du pupitre du jeu** : un socle sur tout
le bloc, surmonté d'un second niveau en retrait, un pied de huit pixels, et un dessus incliné vers celui qui le pose, sur toute la largeur du
bloc, dont l'arrière dépasse un peu du haut — comme le pupitre. Ce dessus est un plateau de trois
pixels d'épaisseur, couvert de feutre noir, sous une vitrine de verre : c'est là qu'on expose ses pierres.
Le sommet du pied suit la pente du plateau : il est fait de quatre tranches qui montent en marches
et entrent dans le plateau, si bien qu'on ne voit pas de jour dessous à l'arrière.

Un villageois sans métier qui en trouve une à portée devient lapidaire.

### Exposer ses pierres

La vitrine s'ouvre d'un clic droit : en haut, **quinze cases** (cinq colonnes, trois rangées) ; en
bas, l'inventaire du joueur. Chaque case reçoit **une** pierre — une pierre polie du mod, ou une gemme
de n'importe quel autre mod ou du jeu (diamant, émeraude, améthyste…). Ce qu'on y range **se voit sur
le bloc** : chaque pierre est posée à plat sur le feutre, à la place de sa case, sous la vitre.

**Elle appartient à celui qui l'a posée.** Lui seul y range et y reprend des pierres. N'importe qui
d'autre peut l'ouvrir : il voit les mêmes cases, les survole pour lire le nom et la pureté de chaque
pierre, mais aucune ne se laisse prendre ni poser. L'écran le dit par son titre, qui devient
« Vitrine de *nom* ».

Deux précautions vont avec :

- un **entonnoir** ne peut rien prendre dans une vitrine : ses pierres ne sont pas un inventaire au
  sens du jeu ;
- une vitrine **sans propriétaire** — posée par une commande, ou avant que cette mémoire n'existe —
  est ouverte à tous.

Casser la vitrine fait tomber ses pierres, **quel que soit celui qui la casse** : la protéger du vol
relève des protections de terrain, pas de ce bloc.

### La vitrine double

Deux vitrines posées **côte à côte, tournées du même côté**, n'en font qu'une, comme deux coffres.

- **Le meuble** : une seule grande vitrine de verre d'un bord à l'autre — ni montants ni vitre à la
  jonction — sur ses **deux pieds**, un par bloc.
- **L'écran** : **trente cases**, dix par rangée, dans un panneau un peu plus large que ceux du jeu.
  Les cinq colonnes de gauche sont celles du bloc de gauche : ce qu'on voit dans l'écran est disposé
  comme sur le meuble, où les dix colonnes de pierres sont régulièrement espacées.
- **Pour les accoler**, il faut que la voisine soit seule, tournée du même côté, **à soi** (ou à
  personne), et qu'aucun villageois n'en ait fait son étal. On pose **accroupi** pour garder deux
  vitrines séparées.
- **Casser une moitié** ne fait tomber que ses quinze pierres ; l'autre redevient une vitrine seule
  et garde les siennes.
- **Un villageois ne s'installe pas** devant une vitrine double, même vide : seule une vitrine seule
  est un poste de travail.

Les trois modèles — la vitrine seule et les deux moitiés — sont écrits par le même script, et la
propriété `half` du bloc (`single`, `left`, `right`, vues de face) dit lequel montrer.

### L'étal du villageois

Une vitrine **vide** peut devenir le poste de travail d'un villageois lapidaire. Elle devient alors
son **étal** — quand il a vraiment pris le poste, pas quand il le réserve (corrigé le 2026-10-23 :
le jeu fait d'abord réserver le poste, puis le villageois marche jusqu'à lui et ne prend le métier
qu'à moins de deux blocs ; l'étal apparaissait dès la réservation) : elle se garnit de pierres — deux cases sur trois, ternes, limpides ou pures — qui ne
sont qu'un **décor**.

- On peut ouvrir l'étal et survoler ces pierres ; son écran s'intitule « Vitrine du lapidaire ».
- On **ne peut pas les prendre**, pas plus son propriétaire qu'un autre.
- Elles **ne tombent pas** quand on casse le bloc : ce ne sont pas des objets. La vitrine ne garde
  qu'un nombre, tiré quand le villageois s'installe, dont le décor se déduit.
- Le décor est **tiré à chaque installation** : il ne change pas tant que le villageois reste, mais
  un autre villageois, ou le même après avoir reposé le bloc, en aura un nouveau.
- Le villageois parti (mort, ou métier perdu), la vitrine redevient celle de son propriétaire, vide.

Une vitrine qui porte **les pierres d'un joueur n'est pas un poste de travail** : aucun villageois ne
viendra s'y installer et recouvrir ces pierres de son décor. Il suffit d'y laisser une pierre pour la
garder pour soi ; il faut la vider pour l'offrir à un villageois.

**Recette**, sans niveau de métier :

| | | |
|---|---|---|
| Vitre | Vitre | Vitre |
| | Pierre polie | |
| Planches | Planches | Planches |

N'importe laquelle des quatorze pierres polies du mod convient, de n'importe quelle pureté, et
n'importe quelles planches.

Le modèle est écrit à la main dans les ressources (`models/block/gem_showcase.json`), sans texture
nouvelle : le bois de l'établi du lapidaire, de la laine noire pour le feutre, le verre et le fer du
jeu, et les blocs de pierres du mod.

## Son habit

Composé de deux habits du jeu, sans rien dessiner : le **tablier** est celui du tanneur, la **loupe**
le monocle du cartographe — reporté sur l'**œil droit** du villageois, et son or changé en acier
noir et gris. Sa version zombie est composée de la même façon. Les deux fichiers sont
`textures/entity/villager/profession/lapidary.png` et
`textures/entity/zombie_villager/profession/lapidary.png`.

## Ses échanges

Refaits le 2026-10-19, étoffés le 2026-10-22. Il **vend des pierres contre des émeraudes**, de cinq
sortes :

- **ébréchées** et **ternes**, à bas prix ;
- **sans pureté**, un peu plus cher : un pari, que la [loupe](minerais.md#la-loupe-de-lapidaire)
  tranchera ;
- **limpides**, puis **pures**, de plus en plus chères.

Jamais de philosophale. Surtout, il **rachète les pierres du joueur contre des émeraudes**, à
chaque niveau : polir et revendre ses pierres est un vrai moyen de faire des émeraudes.

- Les **rachats en vrac** acceptent **n'importe quelle pureté**, sans pureté comprise : le bon
  endroit pour écouler ses ébréchées.
- Les **rachats à la pureté** demandent une pureté exacte et paient **la moitié du prix de
  vente**. Une pierre qui vaut moins d'une émeraude se rachète par lots.

Comme tout villageois, il tire **2 échanges par niveau** parmi ceux du tableau (le comportement du
jeu, gardé à sa demande le 2026-10-23). La sorte de pierre est tirée au hasard dans sa rareté : deux
lapidaires ne demandent pas les mêmes.

| Niveau | On donne | On reçoit |
|---|---|---|
| **1 · Novice** | 2 émeraudes | 1 polissoir |
| | 2 pierres peu communes, toute pureté | 1 émeraude |
| | 1 peu commune terne | 1 émeraude |
| | 1 émeraude | 1 peu commune ébréchée |
| | 2 émeraudes | 1 peu commune terne |
| **2 · Apprenti** | 3 pierres peu communes | 1 sachet à pierres |
| | 2 pierres rares, toute pureté | 3 émeraudes |
| | 8 pierres peu communes | 1 géode |
| | 1 peu commune limpide | 2 émeraudes |
| | 1 rare terne | 2 émeraudes |
| | 3 émeraudes | 1 peu commune sans pureté |
| | 2 émeraudes | 1 rare ébréchée |
| | 4 émeraudes | 1 rare terne |
| **3 · Compagnon** | 1 pierre très rare, toute pureté | 4 émeraudes |
| | 1 peu commune pure | 4 émeraudes |
| | 1 rare limpide | 5 émeraudes |
| | 1 très rare terne | 5 émeraudes |
| | 5 émeraudes | 1 peu commune limpide |
| | 6 émeraudes | 1 rare sans pureté |
| | 10 émeraudes | 1 très rare terne |
| **4 · Expert** | 1 rare pure | 9 émeraudes |
| | 1 très rare limpide | 12 émeraudes |
| | 9 émeraudes | 1 peu commune pure |
| | 10 émeraudes | 1 rare limpide |
| | 15 émeraudes | 1 très rare sans pureté |
| **5 · Maître** | 1 très rare pure | 22 émeraudes |
| | 18 émeraudes | 1 rare pure |
| | 25 émeraudes | 1 très rare limpide |
| | 45 émeraudes | 1 très rare pure |

Son stock : à la vente, 12 pour une pierre à 4 émeraudes ou moins, 8 jusqu'à 16, 4 au-delà ; au
rachat, 16 jusqu'à 2 émeraudes, 12 jusqu'à 5, 8 au-delà. Comme tout villageois, il se réapprovisionne
à son poste. Le **Négoce** de l'arbre du lapidaire baisse ce qu'il demande (voir
[lapidaire.md](lapidaire.md)).

### D'où viennent les prix

Ils ne sont pas écrits, ils se calculent. Une pierre vaut **sa rareté × sa pureté**. À la vente,
chaque point de valeur coûte **2 émeraudes**, arrondi au-dessus ; au rachat, il en rapporte **1**,
arrondi au-dessous.

| Rareté | Valeur | Pierres |
|---|---|---|
| Peu commune | 1 | onyx, grenat, ambre, jade, quartz rose |
| Rare | 2 | pierre de lune, péridot, topaze bleue, sugilite, tourmaline, rubis |
| Très rare | 5 | tanzanite, saphir, diamant (peu commun avant le 2026-10-24) |

| Pureté | Valeur |
|---|---|
| Ébréchée | 0,5 |
| Terne | 1 |
| Sans pureté | 1,5 |
| Limpide | 2,5 |
| Pure | 4,5 (6 avant le 2026-10-22 : la très rare pure coûtait 60) |

Prix de vente (en émeraudes) :

| | Ébréchée | Terne | Sans pureté | Limpide | Pure |
|---|---|---|---|---|---|
| Peu commune | 1 | 2 | 3 | 5 | 9 |
| Rare | 2 | 4 | 6 | 10 | 18 |
| Très rare | 5 | 10 | 15 | 25 | 45 |

Prix de rachat à la pureté (en émeraudes) :

| | Terne | Limpide | Pure |
|---|---|---|---|
| Peu commune | 1 | 2 | 4 |
| Rare | 2 | 5 | 9 |
| Très rare | 5 | 12 | 22 |

Les raretés sont celles de [minerais.md](minerais.md) (`GemRarity`, partagées avec les pierres des
coffres).

## Réglages

Sous `villager.lapidary` dans `config/nowas-depths.json`. Les changer rééquilibre tous les échanges
d'un coup ; les villageois déjà formés gardent les leurs.

| Clé | Défaut | Ce qu'elle fait |
|---|---|---|
| `villager.lapidary.worth.uncommon` | 1 | Valeur d'une pierre peu commune |
| `villager.lapidary.worth.rare` | 2 | Valeur d'une pierre rare |
| `villager.lapidary.worth.very_rare` | 5 | Valeur d'une pierre très rare |
| `villager.lapidary.purity_worth.chipped` | 0,5 | Valeur de la pureté ébréchée |
| `villager.lapidary.purity_worth.dull` | 1 | Valeur de la pureté terne |
| `villager.lapidary.purity_worth.unappraised` | 1,5 | Valeur d'une pierre sans pureté |
| `villager.lapidary.purity_worth.clear` | 2,5 | Valeur de la pureté limpide |
| `villager.lapidary.purity_worth.pure` | 4,5 | Valeur de la pureté pure |
| `villager.lapidary.emeralds_per_worth` | 2 | Émeraudes demandées par point de valeur, à la vente |
| `villager.lapidary.buy_emeralds_per_worth` | 1 | Émeraudes données par point de valeur, au rachat |

## Où tout cela vit

`com.nowa.villager.LapidaryVillager` : le poste, le métier, les valeurs et les échanges. Le tag
`data/minecraft/tags/point_of_interest_type/acquirable_job_site.json` est ce qui autorise un
villageois sans métier à chercher ce poste. `VillagerEntityMixin` : les remises (Marchand,
Négoce).
