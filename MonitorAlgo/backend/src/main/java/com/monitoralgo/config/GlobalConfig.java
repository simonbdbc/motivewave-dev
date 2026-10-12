package com.monitoralgo.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GlobalConfig
 * - Lit UNIQUEMENT un fichier .env :
 * 1) ./.env (local, à la racine du projet)
 * 2) /app/.env (en Docker, monté par docker-compose)
 * - Aucune lecture de System.getenv(), aucune option -D, pas de fallback
 * silencieux.
 * - Lève une IllegalStateException si le .env est introuvable ou si une clé
 * requise manque.
 */
public final class GlobalConfig {

    private static final Map<String, String> PROPS = new ConcurrentHashMap<>();
    private static volatile boolean loaded = false;
    private static volatile Path loadedFrom;
    private static final Object LOCK = new Object();

    private GlobalConfig() {
    }

    /* ===================== API publique ===================== */

    /** Retourne la valeur ou null si absente. */
    public static String get(String key) {
        ensureLoaded();
        return PROPS.get(key);
    }

    /** Retourne la valeur ou lève une IllegalStateException si absente/vide. */
    public static String getRequired(String key) {
        ensureLoaded();
        String v = PROPS.get(key);
        if (v == null || v.isBlank()) {
            String where = loadedFrom != null ? loadedFrom.toString() : "(non trouvé)";
            String msg = "[GlobalConfig] ERREUR: clé \"" + key + "\" absente dans " + where;
            System.err.println(msg);
            throw new IllegalStateException(msg);
        }
        return v;
    }

    public static int getRequiredInt(String key) {
        String v = getRequired(key);
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            String msg = "[GlobalConfig] ERREUR: \"" + key + "\"=\"" + v + "\" (entier attendu)";
            System.err.println(msg);
            throw new IllegalStateException(msg, e);
        }
    }

    public static boolean getRequiredBool(String key) {
        String v = getRequired(key);
        if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(v);
        }
        String msg = "[GlobalConfig] ERREUR: \"" + key + "\"=\"" + v + "\" (boolean attendu: true/false)";
        System.err.println(msg);
        throw new IllegalStateException(msg);
    }

    /** Debug : vue immuable. */
    public static Map<String, String> asMap() {
        ensureLoaded();
        return Collections.unmodifiableMap(PROPS);
    }

    /** Rechargement manuel (utile en dev). */
    public static void reload() {
        synchronized (LOCK) {
            PROPS.clear();
            loadedFrom = null;
            loaded = false;
            loadOnce();
        }
    }

    /* ================== Chargement du .env ================== */

    private static void ensureLoaded() {
        if (!loaded) {
            synchronized (LOCK) {
                if (!loaded)
                    loadOnce();
            }
        }
    }

    private static void loadOnce() {
        Path env = resolveFixedLocations();
        if (env == null) {
            String msg = "[GlobalConfig] ERREUR: .env introuvable. Cherché dans : ./.env puis /app/.env";
            System.err.println(msg);
            // on marque chargé pour éviter boucles; getRequired lèvera ensuite
            loaded = true;
            return;
        }
        try (BufferedReader br = Files.newBufferedReader(env, StandardCharsets.UTF_8)) {
            parseDotEnv(br);
            loadedFrom = env.toAbsolutePath().normalize();
            loaded = true;
            System.out.println("[GlobalConfig] .env chargé depuis: " + loadedFrom);
        } catch (IOException e) {
            String msg = "[GlobalConfig] ERREUR de lecture du .env: " + env + " (" + e.getMessage() + ")";
            System.err.println(msg);
            throw new IllegalStateException(msg, e);
        }
    }

    /** Emplacements FIXES : ./.env (local) puis /app/.env (docker). */
    private static Path resolveFixedLocations() {
        Path local = Paths.get("..").toAbsolutePath().normalize().resolve(".env");
        if (Files.isRegularFile(local))
            return local;
        Path docker = Paths.get("/app/.env");
        if (Files.isRegularFile(docker))
            return docker;
        return null;
    }

    /**
     * Parser simple .env (lignes vides, #commentaires, export KEY=, guillemets).
     */
    private static void parseDotEnv(Reader reader) throws IOException {
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            int n = 0;
            while ((line = br.readLine()) != null) {
                n++;
                String raw = line.trim();
                if (raw.isEmpty() || raw.startsWith("#"))
                    continue;
                if (raw.startsWith("export "))
                    raw = raw.substring("export ".length()).trim();

                int eq = raw.indexOf('=');
                if (eq <= 0) {
                    System.err.println("[GlobalConfig] Ligne ignorée (pas de '=') #" + n + " : " + line);
                    continue;
                }
                String key = raw.substring(0, eq).trim();
                String val = raw.substring(eq + 1).trim();

                if ((val.startsWith("\"") && val.endsWith("\"")) ||
                        (val.startsWith("'") && val.endsWith("'"))) {
                    val = val.substring(1, val.length() - 1);
                }
                if (!key.isEmpty()) {
                    PROPS.put(key, val);
                }
            }
        }
    }
}
