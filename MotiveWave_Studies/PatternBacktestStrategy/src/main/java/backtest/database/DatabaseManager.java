package backtest.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import backtest.config.DbConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static HikariDataSource ds;

    static {
        try {
            DbConfig cfg = DbConfig.getInstance();
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(cfg.get("db.url"));
            config.setUsername(cfg.get("db.user"));
            config.setPassword(cfg.get("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(cfg.get("db.pool.max")));
            config.setMinimumIdle(Integer.parseInt(cfg.get("db.pool.min")));
            config.setIdleTimeout(Long.parseLong(cfg.get("db.pool.idleTimeout")));
            config.setConnectionTimeout(Long.parseLong(cfg.get("db.pool.connectionTimeout")));

            ds = new HikariDataSource(config);

            // ===============================
            // Initialisation structure BDD :
            // ===============================
            try (Connection conn = ds.getConnection()) {
                DatabaseInitializer.initDatabase(conn);
                DataSeeder.seedAll(conn);
                PatternSeeder.seedAllPatterns(conn); // Attention, très long !
            }
            // ===============================

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Impossible d'initialiser HikariCP DataSource", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void closePool() {
        if (ds != null)
            ds.close();
    }
}
