package com.nowa.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.nowa.NowasDepths.LOGGER;
import static com.nowa.NowasDepths.MOD_ID;

/**
 * Le fichier de réglages du mod : {@code config/nowas-depths.json}.
 *
 * <p>Tout nombre qui décide de quelque chose y figure, sous une clé en points
 * ({@code gear.platinum.armor.helmet.protection}) écrite dans le fichier comme une arborescence.
 * Le fichier n'est pas une liste à tenir à jour à la main : il est <em>réécrit</em> au démarrage à
 * partir de ce que le mod a réellement demandé, si bien qu'il contient toujours exactement les
 * réglages de la version installée, avec les valeurs du joueur conservées et les nouveautés
 * ajoutées à leur valeur d'origine.
 *
 * <p>Chaque système lit ses valeurs dans un {@link #onApply(Runnable) rappel} plutôt qu'au fil de
 * l'eau : c'est ce qui permet de tout relire d'un coup avec {@code /nowas-depths config reload}.
 * Les réglages cuits dans un objet au moment de son enregistrement — durabilité, protection,
 * dégâts — font exception : ceux-là demandent un redémarrage.
 */
public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String HEADER = "_lisez_moi";
    private static final String HEADER_TEXT =
            "Réglages de Nowa's Depths. Chaque valeur peut être modifiée ; supprimer une clé la "
                    + "remet à sa valeur d'origine au prochain démarrage. Les réglages d'équipement "
                    + "(durabilité, protection, dégâts) sont appliqués à l'enregistrement des objets "
                    + "et demandent un redémarrage ; les autres se rechargent avec "
                    + "/nowas-depths config reload.";

    /** Ce que le fichier contenait au dernier chargement. */
    private static final Map<String, JsonPrimitive> stored = new TreeMap<>();
    /** Ce que le mod a demandé, avec la valeur retenue : c'est ce qui sera réécrit. */
    private static final Map<String, JsonPrimitive> used = new TreeMap<>();
    /** Les systèmes qui gardent leurs réglages en mémoire et savent les relire. */
    private static final List<Runnable> sections = new ArrayList<>();

    private static Path path;

    private ModConfig() {
    }

    /** À appeler tout au début de l'initialisation, avant que quoi que ce soit ne lise un réglage. */
    public static void load() {
        path = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
        stored.clear();
        if (!Files.exists(path)) {
            LOGGER.info("├─ No config file yet, writing defaults to {}", path);
            return;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonElement root = GSON.fromJson(reader, JsonElement.class);
            if (root != null && root.isJsonObject()) {
                flatten("", root.getAsJsonObject(), stored);
            }
        } catch (IOException | RuntimeException failure) {
            LOGGER.error("├─ Unreadable config file, falling back to defaults: {}", failure.toString());
            stored.clear();
        }
    }

    /**
     * Déclare un système qui garde ses réglages en mémoire. Le rappel est exécuté aussitôt — ce qui
     * inscrit ses clés dans le fichier — puis à chaque rechargement.
     */
    public static void onApply(Runnable section) {
        sections.add(section);
        section.run();
    }

    /** Relit le fichier et redonne la main à tous les systèmes. Renvoie faux si le fichier est illisible. */
    public static boolean reload() {
        load();
        used.clear();
        sections.forEach(Runnable::run);
        save();
        return true;
    }

    /** Réécrit le fichier avec tout ce que le mod utilise, classé par section. */
    public static void save() {
        if (path == null) {
            return;
        }
        JsonObject root = new JsonObject();
        root.addProperty(HEADER, HEADER_TEXT);
        used.forEach((key, value) -> put(root, key, value));
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException failure) {
            LOGGER.error("├─ Could not write the config file: {}", failure.toString());
        }
        stored.keySet().stream()
                .filter(key -> !used.containsKey(key))
                .forEach(key -> LOGGER.warn("├─ Unknown config key ignored: {}", key));
    }

    // ---------- lecture ----------

    public static int i(String key, int fallback) {
        JsonPrimitive value = read(key, new JsonPrimitive(fallback));
        return value.isNumber() ? value.getAsInt() : repair(key, fallback);
    }

    public static double d(String key, double fallback) {
        JsonPrimitive value = read(key, new JsonPrimitive(fallback));
        return value.isNumber() ? value.getAsDouble() : repair(key, fallback);
    }

    /**
     * Un flottant s'élargit mal en double : 2,4f donnerait 2,4000000953674316 dans le fichier.
     * On passe donc par son écriture décimale, qui est celle que le code a écrite.
     */
    public static float f(String key, float fallback) {
        return (float) d(key, Double.parseDouble(Float.toString(fallback)));
    }

    public static boolean flag(String key, boolean fallback) {
        JsonPrimitive value = read(key, new JsonPrimitive(fallback));
        return value.isBoolean() ? value.getAsBoolean() : repair(key, fallback);
    }

    public static String text(String key, String fallback) {
        JsonPrimitive value = read(key, new JsonPrimitive(fallback));
        return value.isString() ? value.getAsString() : repair(key, fallback);
    }

    /** Valeur du mauvais type : on garde celle d'origine et on remet le fichier d'aplomb. */
    private static <T> T repair(String key, T fallback) {
        LOGGER.warn("├─ Config value of the wrong type at {}, using the default", key);
        used.put(key, fallback instanceof Boolean flag ? new JsonPrimitive(flag)
                : fallback instanceof Number number ? new JsonPrimitive(number)
                : new JsonPrimitive(String.valueOf(fallback)));
        return fallback;
    }

    /** Une suite de nombres, par exemple les chances d'une classe de pureté rang par rang. */
    public static int[] ints(String key, int[] fallback) {
        int[] values = new int[fallback.length];
        for (int index = 0; index < fallback.length; index++) {
            values[index] = i(key + "." + index, fallback[index]);
        }
        return values;
    }

    public static double[] doubles(String key, double[] fallback) {
        double[] values = new double[fallback.length];
        for (int index = 0; index < fallback.length; index++) {
            values[index] = d(key + "." + index, fallback[index]);
        }
        return values;
    }

    private static JsonPrimitive read(String key, JsonPrimitive fallback) {
        JsonPrimitive value = stored.getOrDefault(key, fallback);
        used.put(key, value);
        return value;
    }

    // ---------- arborescence ----------

    /** Des objets imbriqués du fichier vers les clés en points du code. */
    private static void flatten(String prefix, JsonObject object, Map<String, JsonPrimitive> target) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue().isJsonObject()) {
                flatten(key, entry.getValue().getAsJsonObject(), target);
            } else if (entry.getValue().isJsonPrimitive() && !key.equals(HEADER)) {
                target.put(key, entry.getValue().getAsJsonPrimitive());
            }
        }
    }

    /** Et l'inverse, pour que le fichier se lise par sections plutôt qu'en une longue liste. */
    private static void put(JsonObject root, String key, JsonPrimitive value) {
        String[] parts = key.split("\\.");
        JsonObject node = root;
        for (int index = 0; index < parts.length - 1; index++) {
            JsonElement child = node.get(parts[index]);
            if (child == null || !child.isJsonObject()) {
                JsonObject created = new JsonObject();
                node.add(parts[index], created);
                node = created;
            } else {
                node = child.getAsJsonObject();
            }
        }
        node.add(parts[parts.length - 1], value);
    }
}
