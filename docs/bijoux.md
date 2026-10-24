# Bijoux — Nowa's Depths

**Depuis la refonte du 2026-10-14, il n'y a plus qu'un bijou : la bague.** Elle se porte dans un
emplacement à elle, au-dessus de la seconde main, et porte **une seule pierre**. Depuis le
2026-10-16, cette pierre donne **un pouvoir simple** ; sa **pureté** en fait le niveau, de 1
(ébréchée) à 5 (philosophale). Les armes, les outils et les armures ne se sertissent plus ; les
colliers et les bracelets ont disparu ; les sets d'effets ont été remplacés par les pouvoirs.

---

## L'emplacement

C'est une **vraie case du jeu**, ajoutée à l'écran d'inventaire du joueur : le glisser-déposer, le
clic-maj et la synchronisation avec le serveur viennent du jeu lui-même.

- En **survie**, juste au-dessus de la seconde main, entre le portrait et la grille de fabrication.
- En **créatif**, dans l'onglet inventaire, à droite du portrait, en pendant de la seconde main.
  Deux greffons s'en chargent : `CreativeInventoryScreenMixin` pour la place,
  `ServerPlayNetworkHandlerMixin` pour la sauvegarde.

Pas dans la seconde main, à dessein : elle tient déjà le bouclier, la torche, le totem.

Comme une armure, une bague se met d'un **clic droit** en la tenant : elle prend sa place, et celle
qui l'occupait revient dans la main. Elle **tombe à la mort** avec le reste de l'inventaire, sous la
même règle de monde : une partie en `keepInventory` la garde.

## La bague

Quatre métaux : le fer et l'or du jeu, le titane et le platine du mod. Recette ` X ` / `X X` / ` X `,
quatre lingots. Le **métal ne donne que de l'armure**, portée, sertie ou non : or 0,25 · fer 0,5 ·
titane 1 · platine 1. L'infobulle le dit.

Une bague sertie **montre sa pierre** : le modèle bascule sur une seconde couche, à la couleur du
joyau. Son infobulle dit son pouvoir et son niveau.

Le sertissage passe par l'**établi de sertisseur** : voir [sertissage.md](sertissage.md).


## Les pouvoirs

**Depuis le 2026-10-16, une pierre donne un pouvoir, et un seul.** Les quatorze sets à quatre
sections (physique, action, pouvoir, passif) ont été supprimés, code compris : Vampire et sa barre de
sang, Poison et son venin, Furtif, Givre, etc. Une seule pierre étant facile à réunir, chaque pouvoir
est **simple**, et pas forcément de combat : la moitié servent à miner, pêcher, commercer ou voyager.
Aucun ne double une potion du jeu.

- **Le pouvoir** est celui de la pierre sertie dans la bague.
- **Le niveau** est sa pureté : ébréchée 1, terne 2, limpide 3, pure 4, philosophale 5.
- **N'importe qui** porte une bague et en profite, lapidaire ou non.
- Le **bismuth** a été retiré du mod le 2026-10-18, avec son pouvoir, l'Irisation.

| Pierre | Pouvoir | Ce que ça fait | Niv. 1 → 5 | État |
|---|---|---|---|---|
| Quartz rose | **Vol de vie** | Une part des dégâts de mêlée infligés soigne celui qui frappe | 4 / 8 / 12 / 16 / 20 % | codé |
| Rubis | **Fureur** | Plus on a perdu de vie, plus on frappe fort à la mêlée ; le bonus entier à un cœur | jusqu'à +10 / 20 / 30 / 40 / 50 % | codé |
| Grenat | **Riposte** | Après avoir encaissé un coup, le prochain coup dans les 3 s fait plus mal | +15 / 30 / 45 / 60 / 75 % | codé |
| Sugilite | **Perforation** | Les flèches traversent les créatures ; elles perdent une part de leurs dégâts à chaque corps traversé, rien au niveau V | traverse 1 / 2 / 3 / 4 / 5 · garde 60 / 70 / 80 / 90 / 100 % | codé |
| Topaze bleue | **Reflet** | Chance de renvoyer un projectile vers son tireur | 10 / 20 / 30 / 40 / 50 % | codé |
| Péridot | **Double saut** | Un saut en l'air, avec une recharge | recharge 10 / 8 / 6 / 4 / 2 s | codé |
| Pierre de lune | **Rappel** | Clic droit (s'il ne fait rien d'autre) : téléportation au point de réapparition | recharge 10 / 8 / 6 / 4 / 2 min | codé |
| Jade | **Portée** | On atteint blocs et créatures de plus loin : casser, poser, ouvrir, frapper | +0,5 / 1 / 1,5 / 2 / 2,5 blocs | codé |
| Onyx | **Poche de l'End** | Clic droit (s'il ne fait rien d'autre) : ouvre son coffre de l'End | recharge 60 / 45 / 30 / 20 / 10 s | codé |
| Diamant | **Filon** | Casser un minerai casse les minerais identiques qui le touchent ; accroupi, un seul | 2 / 4 / 6 / 8 / 10 blocs en plus | codé |
| Ambre | **Fonderie** | Chance que ce qu'on mine tombe déjà fondu (sans l'expérience du four) | 20 / 40 / 60 / 80 / 100 % | codé |
| Tourmaline | **Pêcheur** | Le poisson mord plus vite, avec une meilleure chance de trésor | −15 / 30 / 45 / 60 / 75 % d'attente | codé |
| Saphir | **Érudition** | Plus d'expérience gagnée, d'où qu'elle vienne ; les dépenses ne changent pas | +20 / 40 / 60 / 80 / 100 % | codé |
| Émeraude | **Marchand** | Les villageois font une remise | −5 / 10 / 15 / 20 / 25 % | codé |
| Tanzanite | **Sobriété** | La faim descend moins vite | −15 / 30 / 45 / 60 / 75 % | codé |

Les chiffres et l'attribution des pierres sont de Claude, sauf le vol de vie du quartz (exemple de
l'utilisateur) ; la liste des pouvoirs a été arrêtée avec l'utilisateur. « Clic droit s'il ne fait
rien d'autre » : peu importe ce qu'on tient, tant que le clic n'ouvre pas un coffre ou ne sert pas un
objet.

### Ce que font les pouvoirs codés

- **Vol de vie** : à chaque coup de mêlée qui porte (pas paré au bouclier), le joueur regagne la part
  des dégâts infligés, armure comprise. Le coup qui tue compte aussi : il rend la part de toute la
  vie qui restait à la victime (absorption comprise).
- **Fureur** : les dégâts de mêlée sont multipliés par `1 + bonus × rage`, où la rage va de 0 (pleine
  vie) à 1 (un cœur ou moins), à proportion de la vie perdue. Une créature, un joueur :
  tout ce qu'on frappe.
- **Portée** : les deux portées du jeu (blocs, créatures) sont allongées d'autant. Le serveur pose le
  bonus ; le client le reçoit comme tout attribut.
- **Érudition** : chaque gain d'expérience est multiplié ; les fractions de point s'accumulent d'un
  gain à l'autre au lieu de se perdre. Ce qui se dépense (enchantement, enclume) et ce qui retire de
  l'expérience ne change pas.
- **Sobriété** : chaque fatigue (courir, sauter, frapper, miner, se soigner…) est réduite d'autant ;
  la faim se creuse donc moins vite.
- **Riposte** : un coup encaissé (pas paré au bouclier) dont l'auteur est une créature ou un joueur —
  à la mêlée ou de loin — arme la riposte 3 s ; le prochain coup de mêlée la consomme. Elle se
  multiplie à la Fureur s'il y en avait (une seule bague : jamais les deux à la fois).
- **Reflet** : un projectile qui allait toucher le porteur — flèche, trident, boule de feu, boule de
  neige, potion jetée… mais pas le bouchon d'une canne à pêche — a sa chance de repartir vers les yeux
  de son tireur, à la même vitesse, avec un bruit de bouclier. Il change de camp : c'est le porteur
  qui l'a renvoyé. Sans tireur (un distributeur), il repart d'où il venait.
- **Perforation** (le 2026-10-16, à la place du Ricochet) : à son entrée dans le monde, une flèche
  tirée par le joueur — à l'arc ou à l'arbalète — reçoit le perçage du jeu (celui de l'enchantement
  Perforation) : elle traverse autant de créatures que le niveau, et en touche donc une de plus.
  Après chaque créature blessée, ses dégâts sont multipliés par la part gardée : 60 % au niveau I
  (100, 60, 36, 22… % des dégâts d'origine pour la 1re, 2e, 3e, 4e créature), 100 % au niveau V. Une
  arbalète qui perce déjà davantage garde son perçage. Le trident n'est pas une flèche.
- **Fonderie** : à la **pioche et à la pelle** seulement (à la hache, les bûches tourneraient en
  charbon de bois), chaque pile que lâche le bloc a sa chance de sortir du four : lingot à la place
  du brut, verre à la place du sable, pierre à la place de la pierre taillée, briques à la place de
  l'argile… Toute la pile fond, Fortune comprise ; sans l'expérience du four.
- **Double saut** : en l'air depuis au moins un tic, une pression franche sur la touche de saut fait
  sauter une seconde fois, comme du sol ; la chute repart de là. Il se regagne en touchant le sol,
  après la recharge. Pas dans l'eau ni la lave, sur une échelle, en vol, en selle, ni avec des
  élytres sur le dos : la touche de saut y a déjà un autre sens. C'est le client qui saute (il
  déplace le joueur) ; le serveur vérifie la recharge, et fait entendre le saut.
- **Rappel** et **Poche de l'End** : leur geste est un **clic droit qui ne fait rien d'autre**, quoi
  qu'on tienne. Le jeu essaie la créature visée, le bloc visé, puis l'objet tenu ; si aucun ne fait
  rien (un coffre s'ouvre, un bloc se pose, un bouclier se lève, on mange : rien), le pouvoir part.
  Tenu, le clic se répète, mais la recharge l'arrête.
- **Rappel** : téléportation à son point de réapparition — lit, ancre (sans en user une charge) —, même
  depuis une autre dimension ; à défaut, au point d'apparition du monde. Particules et bruit d'une
  téléportation au départ et à l'arrivée ; la chute repart de zéro.
- **Poche de l'End** : ouvre le coffre de l'End du joueur, avec son bruit ; pas par-dessus un autre
  écran.
- **Filon** : un minerai (ceux du jeu et ceux du mod) cassé par un joueur **debout**, avec l'outil qui
  le récolte, fait casser les minerais **du même bloc** qui le touchent par une face, une arête ou un
  coin, et ainsi de suite, les plus proches d'abord, jusqu'à la limite. Chaque bloc est un vrai coup
  de l'outil : il s'use, Fortune et Toucher de soie jouent, la faim se creuse, le butin tombe sur
  place. Si l'outil casse ou change de main, le filon s'arrête. Accroupi : un seul bloc.
- **Pêcheur** : l'attente avant la touche (5 à 30 s dans le jeu) perd sa part, avant que l'Appât
  n'ôte la sienne ; et la chance de la prise monte de 1 / 1 / 2 / 2 / 3 — comme la Chance de la mer,
  à laquelle elle s'ajoute : plus de trésors, moins de déchets.
- **Marchand** : la remise s'ajoute à celles de la réputation et du Héros du village, sur chaque
  offre d'un villageois, et s'efface avec elles à la fin de l'échange. Elle est arrondie au plus
  proche : une offre trop petite pour qu'elle fasse une unité n'est pas touchée. Pas sur le
  marchand ambulant.

### À l'écran

- Chaque pouvoir a son **effet témoin** parmi les effets du joueur : une icône qui dessine ce qu'il
  fait (voir plus bas), son niveau en chiffres romains (jamais au-delà de V : c'est la pureté), et un
  contour à la couleur de la pierre. Il dure tant que la bague est portée.
- La **Fureur** ne paraît que blessé : à pleine vie elle ne donne rien.
- La **Riposte** ne paraît qu'armée, avec le temps qui lui reste : son icône se grise dans le sens
  des aiguilles d'une montre à mesure que ses 3 s s'écoulent.
- Les pouvoirs à recharge (Double saut, Rappel, Poche de l'End) grisent leur icône pendant la
  recharge, le gris se retirant dans le sens des aiguilles d'une montre. La recharge n'est pas
  sauvegardée : une déconnexion la remet à zéro.
- Dans l'inventaire, la pierre de la bague paraît dans le coin haut droit de la fenêtre ; à son
  survol, le nom du pouvoir et son niveau.
- L'infobulle de la bague dit « Vol de vie — niveau IV », à la couleur de la pierre.

Les icônes disent ce que fait le pouvoir, aux couleurs de la chose dessinée — c'est le contour de
l'effet qui porte la couleur de la pierre. Quand le jeu a l'objet qui raconte le pouvoir, sa forme est
reprise telle quelle ; sinon elle est dessinée, avec un contour sombre comme les icônes d'effets du jeu.

Les icônes ont été arrêtées le 2026-10-17 : plusieurs ont été retouchées à la main par l'utilisateur
après génération ; le tableau ci-dessous décrit l'idée de chacune, les PNG font foi. Elles ne se
régénèrent plus par script.

| Pouvoir | Icône |
|---|---|
| Vol de vie | Une bouche ouverte, deux crocs blancs, du sang au bout (dessinée) |
| Fureur | L'épée en fer du jeu, la pointe de la lame ensanglantée |
| Riposte | Deux épées en fer du jeu, croisées |
| Perforation | La flèche du jeu, traversant un anneau de vent (dessiné) |
| Reflet | Un bouclier de bois cerclé de fer, une flèche qui en repart (dessinée) |
| Double saut | L'icône Saut amélioré du jeu, teinte en vert (la seule reteinte, à la demande de l'utilisateur) |
| Rappel | L'œil de l'End du jeu, passé en blanc bleuté, la pupille fendue presque noire |
| Portée | Une longue lance : hampe de bois, ligature, fer en feuille (dessinée) |
| Poche de l'End | La face avant du coffre de l'End du jeu : couvercle et caisson, son œil vert, sans contour ajouté |
| Filon | L'icône Célérité du jeu (celle de la balise), la tête de la pioche passée en diamant |
| Fonderie | La flamme de l'icône Résistance au feu du jeu, telle quelle, sur un disque de fonte sombre |
| Pêcheur | La canne à pêche du jeu, son fil et son hameçon passés en or |
| Érudition | La fiole d'expérience du jeu |
| Marchand | L'icône Héros du village du jeu : l'émeraude, et la banderole passée en bleu roi |
| Sobriété | Un gros gigot (dessiné) |

### Ce que l'ancien système laisse derrière lui

- La marque de sang des armes du Vampire (`nowas-depths:blood`) a été retirée le 2026-10-17, une fois
  tout essayé : une arme qui la porterait encore dans une vieille sauvegarde ne se chargerait plus.
- Les flammes posées par le set Feu, le venin, les cœurs scellés, les effets des anciens sets
  disparaissent au chargement, sans dommage.

## Réglages

Tout est sous `jewelry` dans `config/nowas-depths.json`.

| Clé | Défaut | Ce qu'elle fait |
|---|---|---|
| `jewelry.armor.gold` | 0,25 | Armure d'une bague en or portée |
| `jewelry.armor.iron` | 0,5 | Armure d'une bague en fer portée |
| `jewelry.armor.titanium` | 1 | Armure d'une bague en titane portée |
| `jewelry.armor.platinum` | 1 | Armure d'une bague en platine portée |

Les pouvoirs sont sous `powers`, chacun une liste de cinq nombres, un par pureté :

| Clé | Défaut | Ce qu'elle fait |
|---|---|---|
| `powers.lifesteal.share` | 0,04 / 0,08 / 0,12 / 0,16 / 0,2 | Part des dégâts de mêlée rendue en vie |
| `powers.fury.max_bonus` | 0,1 / 0,2 / 0,3 / 0,4 / 0,5 | Bonus de dégâts de mêlée, entier à un cœur |
| `powers.reach.blocks` | 0,5 / 1 / 1,5 / 2 / 2,5 | Blocs de portée gagnés |
| `powers.scholar.bonus` | 0,2 / 0,4 / 0,6 / 0,8 / 1 | Bonus d'expérience gagnée |
| `powers.sobriety.saved` | 0,15 / 0,3 / 0,45 / 0,6 / 0,75 | Part de fatigue retirée |
| `powers.merchant.discount` | 0,05 / 0,1 / 0,15 / 0,2 / 0,25 | Part du prix remise par les villageois |
| `powers.riposte.bonus` | 0,15 / 0,3 / 0,45 / 0,6 / 0,75 | Bonus du coup qui riposte |
| `powers.riposte.seconds` | 3 / 3 / 3 / 3 / 3 | Temps pendant lequel la riposte reste armée |
| `powers.reflect.chance` | 0,1 / 0,2 / 0,3 / 0,4 / 0,5 | Chance de renvoyer un projectile |
| `powers.piercing.pierced` | 1 / 2 / 3 / 4 / 5 | Créatures qu'une flèche traverse |
| `powers.piercing.kept` | 0,6 / 0,7 / 0,8 / 0,9 / 1 | Part des dégâts gardée après chaque créature |
| `powers.smelting.chance` | 0,2 / 0,4 / 0,6 / 0,8 / 1 | Chance qu'une pile tombe fondue |
| `powers.double_jump.cooldown` | 10 / 8 / 6 / 4 / 2 | Recharge du double saut, en secondes |
| `powers.recall.cooldown_minutes` | 10 / 8 / 6 / 4 / 2 | Recharge du rappel, en minutes |
| `powers.ender_pocket.cooldown` | 60 / 45 / 30 / 20 / 10 | Recharge de la poche de l'End, en secondes |
| `powers.vein.extra_blocks` | 2 / 4 / 6 / 8 / 10 | Blocs cassés en plus du premier |
| `powers.fishing.shorter_wait` | 0,15 / 0,3 / 0,45 / 0,6 / 0,75 | Part de l'attente retirée |
| `powers.fishing.luck` | 1 / 1 / 2 / 2 / 3 | Chance de la prise ajoutée |

## Ce qui reste à décider

- **Les textures** : la bague est une silhouette simple, projetée sur la rampe de huit nuances de
  chaque lingot. La bague en platine sert de gabarit : redessiner celle-ci suffit à régénérer les
  quatre.

## Où tout cela vit

- `ModJewelry` : les quatre bagues, leurs métaux et leur recette.
- `JewelryItem` : le clic droit qui met une bague.
- `Worn` : la place de la bague, et ce qu'elle accepte.
- `JewelryInventory` / `Jewelry` : ce qu'on porte, sa sauvegarde et sa chute à la mort. La
  sauvegarde lit les places par leur nom : un nom inconnu (la seconde bague des premières versions)
  ne fait rien perdre, l'objet revient dans l'inventaire à la connexion.
- `JewelrySlot` : la case. Elle ne retient pas l'inventaire mais le **joueur**, et le redemande à
  chaque accès — le jeu construit l'écran d'inventaire dans le constructeur du joueur, donc avant
  d'avoir lu sa sauvegarde, et une case qui aurait gardé cet inventaire-là montrerait des doigts
  vides après chaque reconnexion.
- `JewelryPanel`, `InventoryScreenMixin` : la place des cases et leurs cadres en survie.
- `CreativeInventoryScreenMixin`, `CreativeSlotAccessor`, `SlotMixin`, `ServerPlayNetworkHandlerMixin` :
  les mêmes cases dans l'onglet inventaire du créatif.
- `powers.GemPower` : les quinze pouvoirs et leur pierre. `powers.Powers` : le pouvoir et le niveau
  de la bague portée, envoyés au client. `powers.PowerValue` : une valeur réglable, niveau par niveau.
- `powers.PowerHooks` : le carrefour où arrivent greffons et événements du jeu ; `powers.power.*` :
  un pouvoir par classe. `powers.PowerEffects` : les effets témoins et leur recharge.
- `powers.client.PowersClient`, `PowersPanel`, `Cooldowns` : la pierre dans l'inventaire, le contour
  coloré des effets témoins, le voile gris des recharges.
- Greffons : `LivingEntityMixin` (dégâts), `PlayerEntityMixin` (tic, fatigue, expérience),
  `VillagerEntityMixin` (Marchand), `ProjectileEntityMixin` (Reflet), `PersistentProjectileEntityMixin`
  et `PersistentProjectileAccessor` (Perforation), `BlockMixin` (Fonderie), `FishingRodItemMixin` et
  `FishingBobberEntityMixin` (Pêcheur), `MinecraftClientMixin` (le clic droit qui ne fait rien d'autre).
- `powers.PowerCooldowns` : les recharges, côté serveur. `powers.PowerNetworking` : le double saut et
  le clic droit, dits par le client.
- Icônes : `textures/mob_effect/<pouvoir>.png`, dessinées ou reprises des objets du jeu, puis retouchées à la main : les fichiers font foi.
