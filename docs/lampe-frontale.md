# Lampe frontale — Nowa's Depths

L'outil du mineur, et non du lapidaire : n'importe quel joueur peut la fabriquer et s'en servir,
sans niveau de métier. Elle se porte **à la place du casque**, éclaire autour de la tête, et,
sertie d'une pierre, fait luire à travers la roche le minerai de cette pierre.

Le jour où un **métier de mineur** existera, elle en sera la première pièce : elle a déjà son
propre paquet (`com.nowa.headlamp`) pour cette raison.

---

## Fabrication

| | | |
|---|---|---|
| Lingot de fer | Vitre | Lingot de fer |
| Poudre lumineuse | Pierre polie, au moins limpide | Poudre lumineuse |
| Lingot de cuivre | Lingot de cuivre | Lingot de cuivre |

La vitre entre deux fers, une pierre polie entre deux poudres lumineuses, une sangle de cuivre
dessous. **N'importe quelle pierre polie** convient — celles du mod, le diamant et l'émeraude polis —
pourvu qu'elle soit **au moins limpide** (limpide, pure ou philosophale) ; une pierre sans pureté ne
passe pas. Le livre de recettes fait défiler les pierres acceptées. (Depuis le 2026-10-18 : c'était
une pierre de bismuth, avant la suppression du bismuth.) Aucun niveau de lapidaire n'est demandé.

C'est un ingrédient à nous, `nowas-depths:min_purity` (`MinPurityIngredient`), qu'une autre recette
peut reprendre : `{"fabric:type": "nowas-depths:min_purity", "min_purity": "clear"}`.

## Se porter

C'est une pièce d'armure pour le jeu : elle va dans la **case du casque**, se met d'un **clic
droit**, se dessine sur la tête — un bandeau de cuivre et une lampe au milieu du front. Mais elle
ne protège de rien, et c'est son prix : on troque la protection contre la lumière et le sens des
pierres.

## La lumière

La lampe éclaire **tout autour de la tête**, comme une torche qu'on porterait au front, et la
lumière suit le joueur quand il se déplace.

Le jeu ne sait pas éclairer depuis une entité. La lampe pose donc un **vrai bloc de lumière**,
invisible, dans la case de la tête, et le déplace dix fois par seconde avec le joueur.

- Si la tête est dans autre chose que de l'air (de l'eau, une herbe haute), la lumière se pose à
  hauteur des pieds ; s'il n'y a d'air nulle part, rien ne s'allume. **Sous l'eau, la lampe
  n'éclaire donc pas** pour l'instant.
- C'est une vraie lumière : **tout le monde la voit**, et elle compte pour le jeu — aucune créature
  hostile n'apparaît dans la zone éclairée.
- Elle ne gêne rien : on la traverse, on pose un bloc à sa place, l'eau la remplace, un piston la
  détruit. Sa pose et son retrait ne préviennent pas les voisins : un observateur ne la voit pas
  passer.
- Elle ne survit pas à sa lampe. Toutes les secondes, elle vérifie qu'une lampe la tient encore et
  disparaît sinon — y compris après un arrêt brutal du serveur, puisque ce rappel est sauvegardé
  avec le chunk.

## Le sens des pierres

**Un seul logement** depuis la refonte du 2026-10-14 (il y en avait trois). La pierre sertie
**reconnaît la sienne dans la roche** : un rubis fait luire le minerai de rubis, un saphir celui de
saphir. Elle ne donne aucun pouvoir ; seules les pierres qui dorment dans une roche peuvent donc s'y
poser.

Ce n'est pas une vision continue mais un **battement** : toutes les cinq secondes, la lampe cherche
autour de son porteur, et ce qu'elle trouve s'allume deux secondes avant de s'éteindre, avec le halo
des flèches spectrales.

La **portée dépend de la pureté**, et elle grimpe vite :

| Pureté | Portée |
|---|---|
| Ébréchée | 2 |
| Terne | 4 |
| Limpide | 6 |
| Pure | 12 |
| Philosophale | 24 |

Le sertissage passe par l'établi de sertisseur, comme pour une bague : la lampe et une pierre pour
la sertir ; la lampe seule pour reprendre sa pierre ; la lampe sertie et une pierre pour changer de
pierre et récupérer l'ancienne. Rien n'est risqué (voir sertissage.md).

### Les lanternes de cobalt

La lampe fait aussi voir les **lanternes de cobalt** à travers la roche, jusqu'à **64 blocs** : un
halo bleu clair qui épouse la forme de la lanterne — corps, chapeau, anse ou chaîne — et non celle
du bloc. C'est un **battement**, le même que celui des pierres et au même instant : toutes les cinq
secondes, les lanternes s'allument puis s'éteignent doucement. Il ne demande aucune pierre : porter
la lampe suffit, l'ôter les éteint aussitôt. On jalonne sa
descente de lanternes et on les suit pour remonter.

Depuis le 2026-10-19 (sa demande), ni la lampe ni la lanterne n'ont de description dans leur
infobulle : la lampe ne montre que ses pierres serties (« Pierres serties : x/y », par l'infobulle du
sertissage), la lanterne rien de plus que son nom.

### Côté technique

- `GemSense` (serveur) : un battement par joueur, décalé d'un joueur à l'autre. Le balayage ne
  regarde que les chunks déjà chargés, et la palette de chaque section écarte d'emblée celles qui ne
  contiennent rien. Au-delà de 256 blocs trouvés, on s'arrête. La table des minerais est déduite du
  butin de chaque gisement : ajouter un gisement suffit à le rendre sensible.
- `GemSenseRenderer` (client) : les blocs sont tracés pleins, profondeur désactivée, dans un tampon
  à part ; le traitement du jeu (`entity_outline`) en cherche les bords et les floute, puis le
  résultat est posé sur l'image. On en garde **notre propre exemplaire** : celui du jeu ne tourne
  que s'il a une entité brillante à montrer. C'est la silhouette qui brille, pas chaque arête : un
  filon se dessine d'un seul contour.

## Réglages

Tout est sous `headlamp` dans `config/nowas-depths.json`.

| Clé | Défaut | Ce qu'elle fait |
|---|---|---|
| `headlamp.light.level` | 15 | Force de la lumière *(redémarrage)* |
| `headlamp.light.interval_ticks` | 2 | Tous les combien de tics la lumière suit le joueur |
| `headlamp.sense.radius_by_purity` | 2, 4, 6, 12, 24 | Portée du sens, de l'ébréchée à la philosophale |
| `headlamp.sense.interval_ticks` | 100 | Temps entre deux battements |
| `headlamp.sense.glow_ticks` | 40 | Durée de la lueur |
| `headlamp.sense.max_found` | 256 | Plafond de blocs allumés d'un coup |
| `headlamp.lanterns.radius` | 64 | Jusqu'où la lampe voit une lanterne de cobalt |
| `headlamp.lanterns.max_shown` | 256 | Plafond de lanternes montrées, les plus proches d'abord |

## Textures

Il n'existe pas de lampe frontale dans le jeu : les deux textures sont **dessinées**, avec les
couleurs des rampes du jeu — lingot de cuivre pour la sangle, lingot de fer pour le boîtier,
pierre lumineuse pour le verre. La forme de l'icône est dessinée par l'auteur du mod — les trois lanières qui se rejoignent derrière la lampe ; seules leurs teintes ont été posées ensuite, sur la rampe du lingot de cuivre.
Remplacer `item/headlamp.png` ou `models/armor/headlamp_layer_1.png` suffit : rien d'autre n'en
dépend.

## Où tout cela vit

- `Headlamp` : l'objet, son matériau d'armure sans protection, le bloc de lumière, l'enregistrement.
- `HeadlampItem` : la pièce de casque qui ne protège de rien.
- `HeadlampLight`, `HeadlampLightBlock` : la lumière.
- `GemSense`, `HeadlampNetworking`, `client/GemSenseRenderer` : le battement.
- `LanternSight`, et `com.nowa.world.LanternBeacons` : les lanternes de cobalt vues à travers la roche.
- `SocketFamily.LAMP` et le tag `#nowas-depths:socketable/headlamp` : la famille de sertissage.
