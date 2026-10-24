# Réglages — Nowa's Depths

Tout ce que le mod chiffre se règle dans **`config/nowas-depths.json`**, créé au premier démarrage.
Quatre cents valeurs, de la durabilité d'un casque en particulier aux chances de pureté rang par
rang.

---

## Comment le fichier se tient à jour

Il n'est pas une liste à maintenir à la main : au démarrage, le mod **le réécrit** à partir de ce
qu'il utilise réellement.

- une valeur modifiée est conservée ;
- une clé supprimée réapparaît à sa valeur d'origine — c'est la façon de revenir en arrière ;
- une clé ajoutée par une nouvelle version du mod s'ajoute toute seule ;
- une clé inconnue est retirée, avec un avertissement dans le journal ;
- une valeur du mauvais type (du texte là où un nombre est attendu) est remplacée par celle
  d'origine, avec un avertissement.

Pour repartir de zéro : supprimer le fichier.

## Ce qui se recharge, et ce qui ne se recharge pas

`/nowasdepths config reload` (opérateur) relit le fichier en cours de partie.

Se rechargent aussitôt : les chances de pureté, les bonus de l'arbre, les niveaux exigés, les
effets des pierres serties, le dessertissage, les géodes, le polissoir, les tables, la récupération
de la pyrite, la portée à laquelle la lampe frontale voit les lanternes, l'XP.

Demandent un **redémarrage** : tout ce qui est cuit dans un objet au moment où le jeu
l'enregistre — durabilité, protection, ténacité, dégâts, vitesse d'attaque, enchantabilité, palier
de minage, XP lâchée par un minerai. La commande le rappelle.

## Les sections

| Section | Ce qu'on y trouve |
|---|---|
| `gear` | Les quatre métaux : outils (durabilité, vitesse, dégâts, enchantabilité, palier de minage), armure (protection pièce par pièce, ténacité, recul, multiplicateur de durabilité), durabilité de chaque pièce, modificateurs d'attaque de chaque forme |
| `lapidary` | La courbe de niveaux, l'XP gagnée, les chances de pureté rang par rang, le coût en points de chaque nœud de l’arbre, les valeurs de chaque rang de compétence, la durée des tables |
| `gems` | `gems.fusion_seconds` : la durée d'une fusion au creuset de fusion (5) |
| `ores` | L'XP lâchée par chaque minerai, minimum et maximum |
| `geode` | Le nombre de pierres d'une géode et le poids de chacune |
| `polisher` | Durabilité, niveau, durée du polissage, nombre d'usages par dureté |
| `salvage` | Ce qu'une pièce en pyrite brisée rend : taux de récupération et part revenant en lingots |
| `jewelry` | L'armure que donne une bague portée, par métal (voir bijoux.md) |
| `powers` | Les pouvoirs des bagues : chaque valeur, en cinq nombres, un par niveau — le niveau est la pureté de la pierre de la bague (voir bijoux.md) |
| `headlamp` | La lampe frontale : logements, force de la lumière, battement du sens des pierres et portée selon la pureté, portée à laquelle elle voit les lanternes de cobalt (voir lampe-frontale.md) |
| `dynamite` | La dynamite : durée de la mèche, puissance de l'explosion, élan et poids du lancer, taille de la pile (voir dynamite.md) |
| `villager` | Le villageois lapidaire : la valeur de chaque rareté et de chaque pureté (sans pureté comprise), et le prix en émeraudes d'un point de valeur (voir villageois.md) |
| `bats` | La chance qu'une chauve-souris lâche une pierre polie en mourant |
| `loot` | `loot.chest_gems` : la chance qu'un coffre de structure cache des pierres polies sans pureté, leur nombre, et le poids de chaque rareté (relu au `/reload`, voir minerais.md) |
| `loupe` | `loupe.durability` : les pierres que la loupe estime avant de se briser (128, au redémarrage) |
| `worldgen` | Un interrupteur par gisement |

## Le cas de la génération du monde

`worldgen.<gisement>.enabled` éteint un gisement : il n'est plus ajouté à aucun biome, et cela
prend effet au chargement du monde suivant.

**Les autres nombres de la génération** — taille des veines, plage de hauteur, densité, annulation
à l'air — ne sont pas encore réglables. Ils vivent dans les fichiers de données du mod
(`data/nowas-depths/worldgen/`), écrits une fois pour toutes à la compilation : les lire dans le
fichier de réglages suppose de construire les features au chargement du monde plutôt que de les
livrer toutes faites. C'est un changement de structure à part entière, et il est à faire.

En attendant, un datapack peut redéfinir ces fichiers : c'est le mécanisme prévu par le jeu, et il
fonctionne dès aujourd'hui.

## Exemples

Un casque en platine plus solide, sans toucher au reste de la panoplie :

```json
"gear": { "platinum": { "helmet": { "durability": 880 } } }
```

Une pureté philosophale deux fois plus fréquente au rang maximum :

```json
"lapidary": { "purity": { "rank_5": { "philosophal": 12, "pure": 14 } } }
```

Les chances d'une même ligne devraient totaliser cent : ce qui manque revient à la classe ébréchée.

Une fusion deux fois plus rapide au creuset :

```json
"gems": { "fusion_seconds": 2.5 }
```

## Où c'est implémenté

`com.nowa.config.ModConfig` : lecture, écriture, arborescence, rechargement. Chaque système
enregistre un rappel avec `ModConfig.onApply(...)`, exécuté au démarrage puis à chaque
rechargement, ce qui garantit à la fois que ses clés figurent dans le fichier et qu'elles se
relisent. Les valeurs cuites à l'enregistrement, elles, se lisent directement dans le constructeur
de l'objet concerné.
