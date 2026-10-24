# Passation — Nowa's Depths (ex-Shiny Depths) et Nowa's Horizons

Document de reprise pour une nouvelle conversation, écrit le 2026-10-24. Il dit **d'où on part, ce
qui est fait, ce qui reste, et comment l'utilisateur aime travailler**. Rien n'y est figé :
l'utilisateur revient volontiers sur ses décisions, et c'est attendu. Les documents de `docs/`
décrivent l'état actuel du mod ; ce fichier-ci donne la vue d'ensemble et la suite.

> **À lire en premier par le nouveau chat** : les sections 1 (objectifs), 2 (règles absolues),
> 3 (préférences) et 9 (prochaines étapes). Le reste se consulte au besoin.

---

## 1. Les objectifs

L'utilisateur (francophone, pseudo **Nowa**) monte un **modpack Minecraft** : des mods qu'il aime,
plus les siens. Le fil rouge est un jeu de **métiers** façon RPG. Il a deux mods :

| Mod | Dossier | État |
|---|---|---|
| **Nowa's Depths** — s'appelait *Shiny Depths* jusqu'ici | `C:\Users\Nowa\Documents\ShinyDepths` | Bien avancé : minerais, pierres, polissage, pureté, bagues à pouvoirs, métier de lapidaire, villageois, lampe frontale, dynamite |
| **Nowa's Horizons** — nouveau mod de **biomes** | à créer, dossier frère : `C:\Users\Nowa\Documents\<dossier Horizons>` | Rien n'existe encore : **le squelette est à faire** |

**Pourquoi un mod de biomes à lui** : il voulait placer les pierres de Depths par biome. Il aime
certains biomes et en déteste d'autres dans chaque mod de biomes existant, donc aucun ne lui convient
en entier. Deux options lui ont été présentées :

- **A**, créer ses propres biomes de zéro ;
- **B**, un mod « chef d'orchestre » qui trie les biomes des autres mods.

**Il a choisi A.** Ce sera un **deuxième mod, séparé** de Depths. Il veut pouvoir y mettre des
**tags pour tous les biomes, de manière sûre**, et choisir ce qu'il y met.

Points déjà expliqués et acceptés :
- on **ne peut pas copier** les biomes d'autres mods sans l'accord de leurs auteurs ; on peut s'en
  inspirer ;
- créer un biome demande :
  - son climat ;
  - ses couleurs (herbe, feuillage, eau, ciel) ;
  - sa végétation et ses structures (*features*) ;
  - ses créatures ;
  - ses règles de surface (quels blocs au sol) ;
  - sa **place dans la génération** du monde (TerraBlender, ou réécriture du bruit de génération
    vanilla : choix technique à faire).

  Avec les blocs et arbres du jeu, ça reste raisonnable et largement faisable par datagen.

---

## 2. Règles absolues

1. **Ne jamais commiter.** L'utilisateur commite toujours lui-même. On laisse tout dans l'arbre de
   travail, même quand c'est fini et vérifié. Les commandes git en lecture (status, diff, log) vont
   bien ; proposer un message de commit est permis s'il le demande.
2. **Répondre en français.** Le code, les commentaires, les documents et les réponses sont en
   français.
3. **Tenir `docs/` et la mémoire à jour à chaque changement** : quand un chiffre change, le document
   change.
4. **Tout ce qui est chiffré passe par la configuration** (`ModConfig`, voir § 7).
5. **Pas d'interaction qui ne fait rien** : un refus se voit toujours (message, infobulle), jamais un
   silence.
6. **L'utilisateur décide du design.** Toute valeur ou tout choix qu'il n'a pas donné est « à moi » et
   doit lui être **signalé comme tel** dans la réponse.

---

## 3. Préférences de l'utilisateur (tirées de la mémoire de travail)

La mémoire de Claude est rangée **par dossier de travail**. Pour l'instant, elle vit sous
`C:\Users\Nowa\.claude\projects\C--Users-Nowa-Documents-ShinyDepths\memory\`. Un chat ouvert sur un
autre dossier **ne la verra pas** ; d'où ce résumé. Voir aussi § 10.

- **Profil** : sous Windows, IntelliJ, Claude Code dans l'application de bureau. Débutant à
  intermédiaire en git : un fichier créé hors d'IntelliJ reste « Unversioned » tant qu'on ne fait pas
  `git add`, ce qui l'a déjà surpris. Il conçoit visuellement (palettes Figma, croquis, photos de
  référence) et itère beaucoup.
- **Pixel art** :
  - **Rejeté** : les textures générées de toutes pièces (dégradés, facettes de Voronoi…).
  - **Accepté** :
    - les **formes du jeu recoloriées** (lingots : la forme du lingot de fer sur la rampe de
      8 nuances du métal) ;
    - **ses propres dessins** que l'on colorie.
  - **Méthode** : toujours montrer un aperçu avant/après, garder chaque candidat dans le scratchpad,
    demander un croquis après deux essais qui ne convergent pas, et dire d'où vient chaque forme.
  - **Fichiers retouchés à la main** : les icônes d'effets `textures/mob_effect/*.png` sont
    **définitives**. Ne jamais les régénérer par script ; ne toucher que l'icône demandée.
- **Infobulles** :
  - **Nœuds de l'arbre** : courtes et chiffrées, au format « ±X % de <grandeur> en utilisant … ».
    Les nombres s'écrivent en chiffres (« 1 des 4 pierres »). Largeur maximale de 170 px.
  - **Objets** : pas de description ni de mode d'emploi. L'explication va dans `docs/`, pas dans le
    jeu.
- **Niveaux d'effet** : un effet affiché ne dépasse jamais le niveau **V**.
- **Pas de matrice complète** : on ne décline pas chaque objet dans chaque métal. Proposer *lesquels*
  plutôt que tous.
- **Choix visuels** : montrer un rendu et le laisser désigner. Une capture de lui vaut mieux qu'une
  description.
- **Disposition commune des 3 établis** (table de lapidaire, établi de sertisseur, creuset de fusion) :
  meubles 2×2×1 qui partagent la même structure et doivent se lire comme **un seul meuble**.
  - Bois rouge sombre de la table de forgeron.
  - Jamais plus de 2 tiroirs sur une façade.
  - Rien ne dépasse du cadre de sélection.
- **Architecture du modpack** (décidée le 2026-09-26) :
  - **Ne jamais enregistrer de façon conditionnelle.** La configuration coupe du *contenu* (recettes,
    génération, butin, onglet créatif), jamais des entrées de registre : sinon les mondes perdent des
    blocs et client et serveur se désynchronisent.
  - **Où vont les choses** : les **tags disent « où »** (biomes, compatibilité entre mods), la
    **configuration dit « combien »**.
  - **Séparation par métier** : le code de chaque métier vit dans son propre paquet, pour pouvoir le
    sortir dans un jar à part plus tard.
  - **Biomes** : la mémoire disait « pas de mod de biomes à nous ». **C'est dépassé**, il a décidé le
    contraire le 2026-10-24 (§ 1).

---

## 4. Le cadre technique (Depths)

| | |
|---|---|
| Dossier | `C:\Users\Nowa\Documents\ShinyDepths` |
| Minecraft | 1.21.1 · Yarn `1.21.1+build.3` · Fabric Loader 0.16.14 · Fabric API `0.116.17+1.21.1` · Loom `1.10-SNAPSHOT` |
| Java | 21 — `export JAVA_HOME="C:/Program Files/Eclipse Adoptium/jdk-21.0.7.6-hotspot"` |
| Paquet | `com.nowa` · `MOD_ID = "shiny-depths"` · `archives_base_name=shiny-depths` |
| Branche | `feat/jewelry` (base `master`). **~1 270 fichiers modifiés non commités** à ce jour |

```bash
./gradlew build -x test -q   # build complet (le plus utilisé)
./gradlew runDatagen         # après tout changement de recette, butin, modèle, tag, worldgen
./gradlew runClient          # le jeu, que seul l'utilisateur teste
```

- `runDatagen` est le meilleur test automatique : il charge le mod, ses mixins et sa configuration.
- Les cibles des mixins se vérifient dans `build/classes/java/main/shiny-depths-refmap.json`.
- Le jeu n'est **jamais** lancé par Claude : l'utilisateur teste et rapporte.

---

## 5. Ce qui est fait dans Depths (état au 2026-10-24)

Détail complet dans `docs/`. Résumé par système :

### Matériaux — `docs/minerais.md`
- **4 métaux** : platine, titane, pyrite, cobalt. Chacun a minerai, brut, lingot et blocs ; pépites
  pour la pyrite et le cobalt.
- Le **bismuth a été entièrement supprimé** le 2026-10-18. Ne pas le reproposer.
- **12 pierres du mod** et **2 du jeu** (diamant, émeraude). Les minerais du diamant et de l'émeraude
  donnent une pierre brute à polir.
- **Géodes** : un nodule à ouvrir à la table, qui rend 4 pierres. Trois roches à géode : pierre,
  deepslate, calcite.
- **Génération** : `world/OreDeposits` (18 gisements), `ModWorldGen`, `ModBiomeTags`.
  - Chaque pierre a une veine de fond dans toute sa dimension, plus un **bonus de densité** dans des
    biomes favoris, désignés par des tags `shiny-depths:has_ore/<pierre>` remplis avec les tags
    conventionnels `c:`.
  - Le biome n'est **jamais une condition**, seulement un bonus.
  - Seul `worldgen.<gisement>.enabled` est réglable ; tailles, hauteurs et densités sont en dur dans
    le JSON généré.

### Polissage, pureté, estimation
- **Polissoirs** : de base, pyrite, cobalt. Une pierre brute dans l'autre main ; la pureté est tirée
  au polissage.
- **5 puretés** : ébréchée, terne, limpide, pure, philosophale. Les chances dépendent du rang de
  **Coup d'œil**.
- **Une seule table de lapidaire** (meuble 2×2×1, allure netherite). Elle polit par lots dans un
  **sachet** ; les pierres restent **sans pureté** jusqu'à ce qu'un joueur prenne le sachet.
- **Loupe de lapidaire** (2026-10-19) : estime toute pierre polie sans pureté, en main ou par clic
  droit dans l'inventaire.
  - Texture : longue-vue du jeu gris foncé, plus un second cylindre.
  - Animation : descente douce, simulée en créatif.
  - Validée en jeu.
- **Règle** : toute pierre qui ne sort pas de la taille est **sans pureté**. Cela couvre les coffres,
  les chauves-souris, les villageois et les pierres trouvées.
- **Pierres dans les coffres** : tous les coffres `minecraft:chests/*`, 25 % de chances, 1 à
  2 pierres, pondérées par rareté.
- **Creuset de fusion** : 4 pierres identiques donnent 1 pierre d'une pureté au-dessus.

### Bagues et pouvoirs — `docs/bijoux.md`
- **Une seule case de bague**, au-dessus de la seconde main. **4 bagues** : fer, or, titane, platine.
  Le métal ne donne que de l'armure.
- **Une pierre par bague donne un pouvoir simple**, de niveau I à V selon la pureté. Les 15 sont codés
  et **validés en jeu** :

| Pierre | Pouvoir | | Pierre | Pouvoir |
|---|---|---|---|---|
| Quartz rose | Vol de vie | | Onyx | Poche de l'End |
| Rubis | Fureur | | Diamant | Filon |
| Grenat | Riposte | | Ambre | Fonderie |
| Sugilite | Perforation | | Tourmaline | Pêcheur |
| Topaze bleue | Reflet | | Saphir | Érudition |
| Péridot | Double saut | | Émeraude | Marchand |
| Pierre de lune | Rappel | | Tanzanite | Sobriété |
| Jade | Portée | | | |

  À chaque lot de pouvoirs livré, il veut le **tableau complet des 15** avec les valeurs.

### Sertissage — `docs/sertissage.md`
L'établi de sertisseur sertit la bague (1 pierre) et la lampe frontale (3). Les armes, outils et
armures ne se sertissent plus depuis le 2026-10-14. Retirer une pierre est sans risque.

### Métier de lapidaire — `docs/lapidaire.md`
- **Progression** : XP de lapidaire, 1 point par niveau, pas de niveau maximal.
- **Arbre**, refait le 2026-10-18 et validé :
  - un nœud coûte 1, 2 ou 3 points et ne demande que son parent ; **75 points en tout** ;
  - la forme du cadre dit le coût (carré, arrondi, à pointes) ;
  - disposition : racine seule dans la 1re colonne, **3 nœuds de livre gratuits** dans la 2e,
    tronc, puis trois branches (Taille, Prospection, Atelier) ;
  - les **livres ne sont pas encore écrits** : les débloquer ne donne rien pour l'instant.
- **Seul verrou du métier** : la **fabrication** de son outillage, qui passe par des nœuds-recettes
  (table, loupe, polissoirs en métal, établi, creuset).
- **Récupération** au creuset : un nœud de base, puis deux branches, Récupération multiple et
  Récupération sûre.
- **Onglets** : l'écran a des onglets verticaux par métier ; un seul aujourd'hui.

### Villageois lapidaire — `docs/villageois.md`
- **Poste** : la vitrine à pierres, aussi présentoir du joueur, simple ou double. Elle ne se garnit
  qu'une fois le métier vraiment pris : corrigé le 2026-10-23, validé.
- **Ventes** : des pierres contre des émeraudes, de l'ébréchée à la pure, sans pureté comprise. Prix =
  rareté × pureté × 2 ; très rare pure = 45.
- **Rachats** : il rachète les pierres à la pureté exacte, à moitié prix, pour que farmer les pierres
  rapporte des émeraudes.
- **2 échanges par niveau**, comme en vanilla. Les 4 par niveau ont été essayés puis refusés.
- **Raretés** : le **diamant est très rare** depuis le 2026-10-24.

### Mineur (graine d'un futur métier) — `docs/lampe-frontale.md`, `docs/dynamite.md`
- **Lampe frontale** : se porte en casque, 3 pierres, et fait sentir les minerais de ses pierres par
  battements.
- **Dynamite.**
- **Lanterne et chaîne de cobalt** : avec la lampe, les lanternes luisent à travers la roche.
- Le **métier de mineur** viendra **plus tard**, à son initiative.

### Divers
- **Deux onglets créatifs** : *Lapidaire* (icône polissoir) et *Mineur* (icône lampe frontale).
- **Supprimés le 2026-10-22, à ne pas recréer** : le mannequin d'entraînement et l'encadré de la
  créature survolée. Ils ne servaient qu'aux tests.
- **Configuration** — `docs/config.md` : un seul `config/shiny-depths.json` d'environ 400 clés, qui se
  réécrit à chaque démarrage.

---

## 6. Où on en était au moment de la passation

1. **Raretés des pierres**, données à l'utilisateur pour qu'il décide quelles pierres vont dans
   l'Overworld, le Nether ou l'End. Il dessinera ensuite les blocs de minerai.

   | Rareté | Pierres (dimension actuelle) |
   |---|---|
   | Peu commune | onyx (End), grenat, ambre, jade (Overworld), quartz rose (Nether) |
   | Rare | pierre de lune, topaze bleue, tourmaline, rubis (Overworld), péridot (Nether), sugilite (End) |
   | Très rare | saphir (Overworld, deepslate seule), tanzanite (End), diamant (jeu) |

   Le détail (biome favori, roche, hauteurs) est dans `docs/minerais.md` § « Génération dans le
   monde ». **Il n'a pas encore rendu sa répartition.**

2. **La génération par biome** l'a amené à vouloir **son propre mod de biomes** (§ 1).

3. **Il ouvre une nouvelle conversation** qui travaillera **dans les deux dossiers**. Il a annoncé :
   - le **renommage** de Shiny Depths en **Nowa's Depths** ;
   - la **création du squelette** de Nowa's Horizons.

---

## 7. Conventions de travail

### Vérifier l'API plutôt que la deviner
Les noms Yarn 1.21.1 surprennent souvent.

- **Jar nommé** :
  `~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.21.1-net.fabricmc.yarn.1_21_1.1.21.1+build.3-v2/minecraft-merged-1.21.1-net.fabricmc.yarn.1_21_1.1.21.1+build.3-v2.jar`
  - pour voir les membres d'une classe : `javap -p -cp <jar> <classe>` ;
  - pour décompiler : Vineflower,
    `~/.gradle/caches/modules-2/files-2.1/org.vineflower/vineflower/1.11.1/*/vineflower-1.11.1.jar`.
- **Assets du jeu** (textures à recolorier) : `~/.gradle/caches/fabric-loom/1.21.1/minecraft-client.jar`.

### Configuration
- **Ajouter un réglage** : un nouveau chiffre se lit par `ModConfig.i/d/f/flag/text/ints/doubles`,
  dans un bloc `static { ModConfig.onApply(...) }`. C'est ce qui l'inscrit dans le fichier.
- **Le piège** : le fichier **garde les valeurs déjà écrites**. Changer un défaut dans le code ne
  change rien à `run/config/shiny-depths.json`. Pour un rééquilibrage, il faut retirer les clés
  concernées de ce fichier (déjà fait pour `lapidary.tree` et `villager.lapidary.purity_worth.pure`).

### Édition de fichiers (Windows)
- **Contenu complexe** : beaucoup de fichiers sont en **CRLF**. Pour les remplacements longs ou
  accentués, écrire un script Python (3.11, Pillow dispo, pas `python3`) avec l'outil d'écriture
  dans le scratchpad. Le script lit le fichier, détecte le saut de ligne, remplace avec une assertion
  « une seule occurrence », puis réécrit.
- **Heredocs bash** : ils cassent sur les accents et les antislashs.
- **Chemins pour Python** : utiliser `C:/…`, pas `/c/…`.

### Textures
Formes du jeu recoloriées, ou ses dessins. Toujours un aperçu agrandi (`resize NEAREST`) montré à
l'utilisateur avant de conclure.

---

## 8. Le code de Depths

```
src/main/java/com/nowa/
  ShinyDepths.java / ShinyDepthsClient.java / ShinyDepthsDataGenerator.java
  block/      blocs, minerais (ModOres), établis, vitrine, lanterne
  command/    /locate ore, /shinydepths config
  config/     ModConfig
  datagen/    tags, butin, modèles, recettes, biomes, worldgen
  dynamite/   dynamite
  headlamp/   lampe frontale, sens des minerais, lanternes vues à travers la roche
  item/       objets, panoplies (ModGear), bagues (ModJewelry), polissage, pureté, sachets,
              loupe, rareté (GemRarity), estimation (Appraisal)
  jewelry/    case de bague, sauvegarde
  lapidary/   métier : niveaux, arbre (LapidarySkillTree), bonus (LapidaryBonuses), écran
  message/    bandeaux et toasts
  mixin/      22 mixins communs + 8 clients (liste dans shiny-depths.mixins.json)
  powers/     les 15 pouvoirs de bague
  screen/     écrans des établis et de la vitrine
  socket/     sertissage
  villager/   villageois lapidaire
  world/      génération, chauves-souris, pierres des coffres (ChestGems), balises de lanterne
```

**Principes** :
- un seul endroit par chiffre (`LapidaryBonuses`, `OreDeposits`, `GemRarity`…) ;
- les tags plutôt que les listes ;
- des commentaires qui disent le *pourquoi*, en prose française sobre.

---

## 9. Prochaines étapes, dans l'ordre conseillé

### 9.1 Avant tout : commit par l'utilisateur
~1 270 fichiers non commités sur `feat/jewelry`. **Lui suggérer de commiter** avant le renommage, qui
touchera beaucoup de fichiers. Ne pas le faire à sa place.

### 9.2 Renommer Shiny Depths en Nowa's Depths — **à cadrer avec lui d'abord**
Il faut lui faire préciser jusqu'où va le renommage :

| Niveau | Ce qui change | Conséquence |
|---|---|---|
| **Nom affiché** seulement | `fabric.mod.json` (`name`), onglets créatifs, titres, docs | Sans risque |
| **Identifiant de mod** `shiny-depths` → par ex. `nowas-depths` | `MOD_ID`, le dossier `assets/shiny-depths` et `data/shiny-depths`, tous les identifiants (objets, blocs, tags `shiny-depths:*`), clés de traduction, nom du fichier de config, `archives_base_name`, nom du fichier des mixins | **Casse les mondes de test existants** : tous les objets et blocs du mod y disparaissent. Gros changement mécanique, à faire en une fois, par script, puis `runDatagen` |
| Paquet Java `com.nowa` | rien d'obligatoire | Déjà neutre, peut rester |
| Dossier `ShinyDepths` | facultatif | Déplace la mémoire de Claude (§ 10) |

Un identifiant ne peut contenir que `a-z 0-9 - _` : **pas d'apostrophe**. « Nowa's Depths » reste
possible comme nom affiché.

### 9.3 Créer le squelette de Nowa's Horizons
1. **Noms** :
   - **Dossier sans apostrophe** (par ex. `NowasHorizons`) : l'apostrophe casse des scripts et
     certains outils.
   - **Nom affiché** : « Nowa's Horizons ».
   - **Identifiant** : par ex. `nowas-horizons`.
   - **Paquet** : par ex. `com.nowa.horizons`, à confirmer.
2. **Même base technique que Depths** :
   - Gradle, Loom, Fabric API et Yarn aux **mêmes versions** (§ 4) ;
   - Java 21 ;
   - le wrapper Gradle copié depuis Depths ;
   - `fabric.mod.json`, points d'entrée commun, client et **datagen** ;
   - fichier de mixins vide ;
   - FR et EN.
3. **Dépôt git** à lui (`git init`). Rappeler à l'utilisateur qu'il commite lui-même.
4. **Lancer le jeu avec les deux mods** : régler Gradle pour qu'un `runClient` charge Depths et
   Horizons ensemble. Par exemple, l'un dépend de l'autre en `modLocalRuntime`, via une build
   composite ou le jar construit. À choisir, puis vérifier.
5. **Indépendance** : **aucun des deux mods ne doit exiger l'autre**. Ils communiquent par **tags de
   biomes** :
   - Horizons range ses biomes dans les tags conventionnels `c:` (`c:is_hot`, `c:is_jungle`…) et
     dans ses propres tags ;
   - Depths lit `shiny-depths:has_ore/<pierre>`. On peut y ajouter des biomes d'Horizons en entrées
     facultatives (`required: false`), ignorées si Horizons est absent.
6. **Mécanisme de placement des biomes** (TerraBlender ou réécriture du bruit vanilla) : le présenter
   à l'utilisateur avec ses compromis **avant** de coder.

### 9.4 Ensuite, côté génération des pierres (Depths)
- **Sa répartition** : attendre sa répartition des pierres par dimension (§ 6), puis ses **visuels de
  blocs** de minerai (une variante par roche hôte : pierre, deepslate, netherrack, end stone).
- **Familles de biomes** : rattacher chaque pierre à une **grande famille de biomes** (chaud et sec,
  humide, froid, océan, montagne, grottes…) plutôt qu'à des biomes précis, pour que ça tienne avec
  beaucoup de biomes.
- **Commande de vérification** proposée : `/shinydepths biomes`, qui liste les biomes chargés où chaque
  pierre a son bonus.
- **Réglages de génération vivants** : rendre la taille des veines, les hauteurs et la densité
  réglables. Il faut enregistrer les features au chargement au lieu du JSON généré : un changement
  de structure, voir `docs/config.md`.

### 9.5 Liste de fond (à son initiative, ne pas lancer seul)
- **Écrire les 3 livres du guide** du mod, et les donner au déblocage des nœuds de livre, y compris
  aux joueurs qui les ont déjà débloqués (point d'accroche : `Lapidary.tryUnlock`).
- **Textures** : bagues (redessiner la bague en platine régénère les quatre), blocs de pierres et
  minerais (provisoires).
- **Métier de mineur** (lampe, dynamite, lanternes…).
- **Avancements** : aucun n'existe.
- **Grand nettoyage** annoncé par lui.
- **Docs périmées** dans `docs/minerais.md` : le récapitulatif dit que le rubis n'a pas de génération,
  et le tableau des blocs dit « génération à brancher », alors que les gisements existent. À remettre
  d'aplomb lors du nettoyage.

---

## 10. La mémoire de Claude et le nouveau dossier

- **Où vit la mémoire** : `C:\Users\Nowa\.claude\projects\C--Users-Nowa-Documents-ShinyDepths\memory\`,
  un fichier par sujet plus un index `MEMORY.md`. Elle n'est chargée que si la conversation est
  ouverte sur **ce** dossier.
- **Organisation conseillée** :
  - ouvrir le nouveau chat sur `ShinyDepths` (dossier principal) ;
  - **ajouter le dossier d'Horizons à la session** (ajout de dossier dans l'application, ou demande
    d'accès par Claude) : les deux mods sont alors lisibles et modifiables, et la mémoire reste
    chargée.
- **Si le chat est ouvert ailleurs** (par exemple sur `Documents`), copier le dossier `memory` vers
  le dossier de mémoire du nouveau chemin, ou se fier à ce document.
- **Si le dossier ShinyDepths est renommé**, la mémoire ne suit pas : la copier vers le nouveau
  chemin.

---

## 11. Où lire quoi

| Document | Contenu |
|---|---|
| `docs/minerais.md` | Matériaux, raretés, génération (dimensions, biomes, hauteurs), pureté, polissage, loupe, coffres, géodes, chauves-souris, blocs |
| `docs/lapidaire.md` | Le métier : niveaux, recettes réservées, arbre (coûts, disposition, livres), onglets créatifs |
| `docs/villageois.md` | Vitrine, villageois lapidaire, échanges et prix |
| `docs/bijoux.md` | Case de bague, bagues, les 15 pouvoirs et leurs valeurs |
| `docs/sertissage.md` | Établi de sertisseur |
| `docs/lampe-frontale.md`, `docs/dynamite.md` | Les objets du futur mineur |
| `docs/panoplies.md` | Outils et armures des métaux |
| `docs/config.md` | Le fichier de configuration et ses sections |

Attention : `docs/panoplies.md` et `docs/sertissage.md` peuvent être en retard sur les refontes récentes ; les vérifier contre le code.
