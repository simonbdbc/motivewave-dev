package matcher.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DbConfig {
    private static DbConfig instance;
    private final Properties props = new Properties();

    private DbConfig() {
        String configPath = System.getProperty("matcher.dbConfig");
        if (configPath == null) configPath = System.getenv("MATCHER_DB_CONFIG");
        if (configPath == null || configPath.isBlank()) {
            throw new IllegalStateException("Configurer matcher.dbConfig ou MATCHER_DB_CONFIG hors Git");
        }
        try (InputStream input = Files.newInputStream(Path.of(configPath))) {
            props.load(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible de charger la configuration DB externe", ex);
        }
        for (String key : new String[] {"db.url", "db.user", "db.password"}) {
            String value = props.getProperty(key);
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("Configuration DB manquante : " + key);
            }
        }
        props.putIfAbsent("db.pool.max", "20");
        props.putIfAbsent("db.pool.min", "2");
        props.putIfAbsent("db.pool.idleTimeout", "60000");
        props.putIfAbsent("db.pool.connectionTimeout", "30000");
    }

    public static synchronized DbConfig getInstance() {
        if (instance == null) instance = new DbConfig();
        return instance;
    }

    public String get(String key) {
        return props.getProperty(key);
    }
}
