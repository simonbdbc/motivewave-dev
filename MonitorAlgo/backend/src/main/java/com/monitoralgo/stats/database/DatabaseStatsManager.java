package com.monitoralgo.stats.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

// On enlève @Component car on ne veut pas l'injecter mais l'utiliser en static
public class DatabaseStatsManager {
    private static HikariDataSource ds;

    // Static block d'initialisation
    static {
        try {
            // Pas de credential embarqué ou de connexion distante implicite.
            String url = requiredEnv("MW_DATASOURCE_URL", "SPRING_DATASOURCE_URL");
            String user = requiredEnv("MW_DATASOURCE_USERNAME", "SPRING_DATASOURCE_USERNAME");
            String password = requiredEnv("MW_DATASOURCE_PASSWORD", "SPRING_DATASOURCE_PASSWORD");

            // Pour les paramètres de pool
            int maxPool = Integer.parseInt(System.getenv().getOrDefault("DB_POOL_MAX", "20"));
            int minIdle = Integer.parseInt(System.getenv().getOrDefault("DB_POOL_MIN", "2"));
            long idleTimeout = Long.parseLong(System.getenv().getOrDefault("DB_POOL_IDLE_TIMEOUT", "60000"));
            long connectionTimeout = Long
                    .parseLong(System.getenv().getOrDefault("DB_POOL_CONNECTION_TIMEOUT", "30000"));

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);
            config.setMaximumPoolSize(maxPool);
            config.setMinimumIdle(minIdle);
            config.setIdleTimeout(idleTimeout);
            config.setConnectionTimeout(connectionTimeout);

            ds = new HikariDataSource(config);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Impossible d'initialiser HikariCP DataSource strategy_stats", e);
        }
    }

    private static String requiredEnv(String name, String legacyName) {
        String value = System.getenv(name);
        if (value == null && !name.equals(legacyName)) value = System.getenv(legacyName);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Configuration DB manquante : " + name);
        }
        return value;
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void closePool() {
        if (ds != null)
            ds.close();
    }
}
