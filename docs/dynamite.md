# Dynamite — Nowa's Depths

Un bâton qu'on allume au briquet et qu'on lance. Deux fois moins fort que le bloc de TNT, mais il
tient dans la main et part où l'on vise.

---

## Fabrication

Deux **sables** et deux **poudres à canon**, en diagonales, dans un carré de deux cases de côté :

| | |
|---|---|
| Sable | Poudre à canon |
| Poudre à canon | Sable |

Peu importe laquelle des deux diagonales porte le sable, et le carré tient dans la grille de
l'inventaire. Aucun niveau de métier. Les bâtons s'empilent par 16.

## S'en servir

Tout passe par le clic droit, dynamite en main :

| Situation | Ce que fait le clic |
|---|---|
| Un **briquet dans l'autre main**, dynamite éteinte | Il **allume** la mèche |
| Dynamite **allumée** | Il la **lance** |
| Dynamite éteinte, pas de briquet | Il la **lance** éteinte |

Peu importe quelle main tient quoi. Briquet en main principale et bloc visé, le jeu servirait
d'abord le briquet et mettrait le feu au sol : avec une dynamite dans l'autre main, il allume la
dynamite à la place.

Un seul bâton s'allume à la fois, et **la pile ne bouge pas** :

1. on tient une pile de dynamite ;
2. on allume : la pile perd un bâton — celui qui brûle — et c'est lui qu'on voit en main, mèche
   animée ;
3. on lance : le nombre ne change plus, le bâton était déjà décompté, et la pile est toujours en
   main, prête pour le suivant.

Le dernier bâton d'une pile reste affiché tant qu'il brûle (une pile ne peut pas montrer zéro) et
disparaît quand on le lance. Le briquet s'use d'un point, comme pour amorcer un bloc de TNT, et le
bruit est le même.

## La mèche

Elle dure **quatre secondes**, comme celle du bloc de TNT, et on la voit se consumer, en main comme
en vol. La mèche est faite de sept petits cubes ; à chaque étape, celui qui vient de brûler
**disparaît** et le suivant devient l'**étincelle** :

| Cube | Noir | Étincelle | Disparu |
|---|---|---|---|
| Le premier, au bout | avant l'allumage | jamais | dès l'allumage |
| Les suivants | tant que la flamme n'y est pas | une étape | ensuite |

La mèche raccourcit donc vers le bâton, l'étincelle toujours à son bout ; à la dernière étape il n'en
reste rien.

Une fois allumée, la dynamite explose quoi qu'on en fasse :

- **lancée**, au premier contact — bloc, joueur, créature — ou en plein vol si la mèche arrive au
  bout avant ;
- **gardée en main** (ou n'importe où dans l'inventaire), sur celui qui la porte — seul le bâton
  allumé explose, le reste de la pile est épargné ;
- **lâchée au sol**, là où elle gît.

**Éteinte, elle n'explose jamais**, même en heurtant quelque chose : lancée, elle retombe en objet
et se ramasse.

Une dynamite allumée rangée dans un coffre ne s'y consume pas : elle explosera dans la main de celui
qui la reprendra, si sa mèche est passée entre-temps.

## L'explosion

**Deux fois moins forte que le bloc de TNT** : puissance 2 contre 4. Le rayon, les blocs brisés et
les dégâts en découlent — environ moitié moins de dégâts à bout portant. Même bruit, mêmes
particules, et les mêmes règles de monde que le TNT (`tntExplosionDropDecay`).

Elle blesse celui qui la tient si elle explose en main, et les dégâts d'un bâton lancé sont imputés
à celui qui l'a lancé.

En main, l'explosion part **des pieds** du porteur et non de sa poitrine : à cette puissance, un
mètre de vide suffit à ce que la pierre résiste, et rien n'aurait été détruit autour de lui. Depuis
le sol, elle entame ce qui l'entoure comme le fait un bâton lancé, qui explose toujours contre ce
qu'il touche.

### Ce qu'elle épargne

Une dynamite fait moins de dégâts que la TNT : on en use plusieurs au même endroit, et le second
bâton exploserait sur ce que le premier vient de faire tomber. Son explosion **ne détruit donc pas
les pierres et les minerais tombés au sol** : tout ce qu'un sachet à pierres accepte (pierres brutes
et polies, minerais, lingots, pépites, poudres…), plus les géodes et les sachets eux-mêmes. C'est le
tag `#nowas-depths:dynamite_proof`, qu'un datapack peut étendre.

Seule la dynamite les épargne : la TNT, un creeper ou toute autre explosion les détruisent comme
avant. Et les autres objets au sol — outils, blocs, nourriture — restent détruits par la dynamite.

## Le lancer

Comme une boule de neige, mais plus lourd : elle part moins vite (1,1 contre 1,5) et retombe plus tôt
(poids 0,05 contre 0,03). Elle va donc nettement moins loin.

## Réglages

Tout est sous `dynamite` dans `config/nowas-depths.json`.

| Clé | Défaut | Ce qu'elle fait |
|---|---|---|
| `dynamite.fuse_ticks` | 80 | Durée de la mèche, en tics (20 par seconde) |
| `dynamite.power` | 2.0 | Puissance de l'explosion ; le bloc de TNT vaut 4 |
| `dynamite.throw_speed` | 1.1 | Élan du lancer ; une boule de neige vaut 1,5 |
| `dynamite.gravity` | 0.05 | Poids en vol ; une boule de neige vaut 0,03 |
| `dynamite.stack_size` | 16 | Taille de la pile *(redémarrage)* |

## Modèle

La dynamite est un **modèle en volume**, d'après la capture fournie par l'auteur du mod : un bâton
rouge barré d'une bande blanche, et une mèche de petits cubes noirs qui monte puis se recourbe. On
le voit ainsi en main, dans l'inventaire et en vol, où il tournoie sur lui-même.

Il n'existe pas de dynamite dans le jeu : le modèle est dessiné, avec les couleurs du côté du bloc de
TNT. Tout se trouve dans les ressources du mod, écrit à la main (rien ne vient du datagen) :

- `textures/item/dynamite_model.png` — la petite planche de couleurs que le modèle découpe ;
- `models/item/dynamite_display.json` — les positions en main, au sol, dans l'inventaire ;
- `models/item/dynamite.json` — éteinte, avec les seuils qui mènent aux modèles allumés ;
- `models/item/dynamite_lit_0.json` à `dynamite_lit_6.json` — une forme par étape de la mèche.

Si la mèche change de longueur, `Dynamite.FUSE_STAGES` doit suivre.

## Côté technique

- La mèche n'est pas un compte à rebours : la pile porte **l'instant où le bâton allumé explosera**
  (composant `nowas-depths:fuse`), et retient si ce bâton est encore compté dans son nombre. Rien ne change sur l'objet de tic en tic, il ne tressaute donc pas
  dans la main, et la même donnée sert au bâton tenu, au bâton lancé et à l'animation.
- `DynamiteEntity` porte l'objet lancé, mèche comprise : c'est lui que `DynamiteEntityRenderer`
  dessine en vol, en le faisant tournoyer.
- En main, l'explosion n'a pas d'entité source : le jeu épargne toujours l'entité source d'une
  explosion, et celui qui tenait le bâton ne doit pas l'être.

## Où tout cela vit

`com.nowa.dynamite` : `Dynamite` (objet, mèche, explosion, réglages), `DynamiteItem` (allumer, lancer,
brûler en main), `DynamiteEntity` (le vol), `client/DynamiteClient` (animation de la mèche),
`client/DynamiteEntityRenderer` (le bâton en vol).
`FlintAndSteelItemMixin` pour le briquet en main principale, `ItemEntityMixin` pour le bâton lâché au
sol.
