# Panoplies — Nowa's Depths

Les quatre métaux du mod se forgent en **outils** : épée, pioche, hache, pelle, houe. Trois d'entre
eux — cobalt, titane, platine — se forgent aussi en **armure**, les quatre pièces. La pyrite n'en a
pas : tous les métaux ne font pas tous les objets, pas plus qu'en vanilla. Soit 32 objets. (Le
bismuth et ses cinq outils ont été retirés du mod le 2026-10-18.)

Les recettes, les dispositions, les modificateurs d'attaque et les tags sont ceux du jeu : seul le
matériau change. Les textures sont les panoplies de fer recoloriées avec la rampe de couleurs du
lingot correspondant ; le manche de bois reste brun, comme sur tous les outils vanilla.

---

## Caractère de chaque métal

| Métal | Idée | Emplacements de l'épée |
|---|---|---|
| **Pyrite** | L'or des fous : tendre et vite usée, mais elle appelle les enchantements | 3 |
| **Cobalt** | Le plus rapide à miner, solide sans être exceptionnel | 5 |
| **Titane** | Entre le fer et la netherite, comme annoncé dans le nuancier | 9 |
| **Platine** | Le plus durable et le plus rapide | 9 |

L'épée, la hache, la pioche, la pelle et la houe se sertissent, pas l'armure. Les outils ont moins
d'emplacements que l'épée : le barème complet est dans
[sertissage.md](sertissage.md).

Aucun métal ne surclasse la netherite : elle garde ses dégâts, sa résistance au feu et son recul.
Le platine la dépasse en durabilité et en vitesse, mais c'est le **sertissage** qui fait la valeur
des panoplies du mod — trois pierres sur chaque pièce de platine contre trois sur la netherite.

## Outils

| Métal | Durabilité | Vitesse de minage | Dégâts du matériau | Palier de minage | Enchantabilité |
|---|---|---|---|---|---|
| Pyrite | 190 | 8,0 | +1,5 | pierre | 22 |
| Cobalt | 850 | 9,0 | +2,5 | fer | 16 |
| Titane | 1600 | 8,0 | +3,0 | diamant | 12 |
| Platine | 2400 | 9,5 | +3,5 | diamant | 20 |
| *Fer* | *250* | *6,0* | *+2,0* | *fer* | *14* |
| *Diamant* | *1561* | *8,0* | *+3,0* | *diamant* | *10* |
| *Netherite* | *2031* | *9,0* | *+4,0* | *netherite* | *15* |

Le **palier de minage** dit ce que l'outil peut récolter : *pierre* suffit au fer et au cuivre,
*fer* ouvre le diamant, l'or et l'émeraude, *diamant* ouvre l'obsidienne et les débris antiques.
La pyrite s'arrête donc au palier de la pierre — elle a l'air précieuse, elle ne l'est pas.

Les modificateurs d'attaque par forme sont ceux du jeu : épée +3 et −2,4 de vitesse, pioche +1 et
−2,8, pelle +1,5 et −3,0, hache +6 et −3,1 (+5 et −3,0 pour le titane et le platine, comme le
diamant), houe rendant ses dégâts pour frapper plus vite.

Une épée donne donc 5,5 dégâts en pyrite, 6,5 en cobalt, 7 en titane et 7,5 en
platine — l'épée en fer en fait 6, celle en diamant 7, celle en netherite 8.

## Armure

Cobalt, titane et platine seulement. Protection lue dans l'ordre bottes / jambières / plastron /
casque.

| Métal | Protection | Total | Ténacité | Recul | Durabilité | Enchantabilité |
|---|---|---|---|---|---|---|
| Cobalt | 2 / 5 / 6 / 2 | 15 | 1 | 0 | ×24 | 16 |
| Titane | 3 / 6 / 7 / 3 | 19 | 2 | 0 | ×32 | 12 |
| Platine | 3 / 6 / 8 / 3 | 20 | 2,5 | 5 % | ×40 | 20 |
| *Fer* | *2 / 5 / 6 / 2* | *15* | *0* | *0* | *×15* | *9* |
| *Diamant* | *3 / 6 / 8 / 3* | *20* | *2* | *0* | *×33* | *10* |
| *Netherite* | *3 / 6 / 8 / 3* | *20* | *3* | *10 %* | *×37* | *15* |

Le son d'équipement suit le rang : celui du fer pour le cobalt, du diamant pour le titane, de la
netherite pour le platine.

## Ce qui marche sans rien de plus

Les objets de chaque métal entrent dans les tags vanilla `#minecraft:swords`, `pickaxes`,
`axes`, `shovels`, `hoes`, `head_armor`, `chest_armor`, `leg_armor` et `foot_armor`. Or tout le
reste du jeu se définit **à partir de ces tags** : les panoplies du mod sont donc enchantables
(table, enclume, livres), réparables à l'enclume avec leur lingot, garnissables à la table de
forgeron, sujettes à Solidité, Raccommodage et Disparition, et sertissables selon leur métal.

Les objets sont aussi déclarés dans les tags conventionnels `#c:tools/melee_weapon` et
`#c:tools/mining_tool`, pour les mods qui trient l'équipement.

Les **garnitures** fonctionnent entièrement : le datagen écrit, comme le fait le jeu pour ses
propres armures, les dix modèles de variantes par pièce, et les motifs portés sur le corps
reprennent les palettes vanilla.

L'équipement en **pyrite et en cobalt se refond** en pépites, au four comme au haut-fourneau, ainsi
que le fait le fer vanilla ; les trois autres métaux n'ont pas de pépite et durent assez pour qu'on
les répare à l'enclume. Voir « Pépites et ferronnerie » dans [minerais.md](minerais.md).

## La pyrite ne se perd pas

Une pièce en pyrite qui **cède rend ce qu'elle a coûté**, mais pas sous la même forme : un lingot
sur trois a tenu le choc, tout le reste revient en pépites. Le métal s'effrite en permanence et ne
disparaît jamais — on ne manque pas de pyrite, on manque d'outils en état.

| Pièce | Coût | Ce qu'on récupère |
|---|---|---|
| Pelle | 1 lingot | 9 pépites |
| Épée, houe | 2 lingots | 18 pépites |
| Pioche, hache | 3 lingots | 1 lingot + 18 pépites |

Tout tombe **au sol**, à hauteur de main de qui portait la pièce, et se ramasse comme n'importe
quel butin — y compris celui d'une créature qui usait un outil de pyrite. Le coût se lit dans
le motif de la recette, jamais dans un tableau à part : changer la
disposition change du même coup ce qu'on récupère.

**Ce que cela implique.** Le retour est entier : un outil de pyrite ne se consomme donc pas
vraiment, il se refait. C'est voulu — c'est le caractère du métal — mais cela veut aussi dire
que le minerai de pyrite cesse d'être une dépense une fois les premiers outils forgés. Si un jour
cela déséquilibre la progression, tout tient dans une constante : `RECOVERY` dans
`GearSalvage.java`, en centièmes. À 75, la pioche ne rendrait plus que 20 pépites.

Les autres métaux ne rendent rien en cassant. Le titane, le cobalt et le platine durent assez pour
qu'on les répare avant.

## Ce qui reste hors panoplie

Un lingot de fer fait aussi un seau, des cisailles, un bouclier, une enclume, des rails… mais ce
sont des objets propres au fer, pas ce que « n'importe quel lingot » sait faire. Des boucliers par
métal ont été essayés puis retirés (2026-10-01), avec l'armure de pyrite : le mod
ne décline pas chaque objet dans chaque métal.
