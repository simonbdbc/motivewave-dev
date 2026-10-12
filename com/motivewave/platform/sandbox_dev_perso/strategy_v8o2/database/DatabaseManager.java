package sbdev.joy_ai_project.strategy_v8o3.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import sbdev.joy_ai_project.strategy_v8o3.config.GlobalConfig;

public class DatabaseManager {
    private static volatile DatabaseManager instance;
    private final ThreadLocal<Connection> connection = ThreadLocal.withInitial(() -> {
        try {
            String dbPath = GlobalConfig.getInstance().get("DB_PATH");
            File file = new File(dbPath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists())
                parent.mkdirs();
            String dbUrl = "jdbc:sqlite:" + dbPath;
            return DriverManager.getConnection(dbUrl);
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'ouvrir la connexion à la base SQLite", e);
        }
    });

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    // Pour usages classiques (dans studies, etc.)
    public Connection getConnection() {
        return connection.get();
    }

    // Pour usage dans un thread dédié, ex : writer asynchrone
    public Connection createNewConnection() {
        try {
            String dbPath = GlobalConfig.getInstance().get("DB_PATH");
            File file = new File(dbPath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists())
                parent.mkdirs();
            String dbUrl = "jdbc:sqlite:" + dbPath;
            return DriverManager.getConnection(dbUrl);
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'ouvrir la connexion à la base SQLite", e);
        }
    }
}
