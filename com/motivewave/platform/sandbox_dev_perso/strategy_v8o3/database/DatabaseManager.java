package sbdev.joy_ai_project.strategy_v8o3.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import sbdev.joy_ai_project.strategy_v8o3.config.GlobalConfig;

public class DatabaseManager {
    private static volatile DatabaseManager instance;
    private static volatile boolean initialized = false;
    private final ThreadLocal<Connection> connection = ThreadLocal.withInitial(() -> {
        try {
            String dbPath = GlobalConfig.getInstance().get("DB_PATH");
            File file = new File(dbPath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists())
                parent.mkdirs();
            String dbUrl = "jdbc:sqlite:" + dbPath;
            Connection conn = DriverManager.getConnection(dbUrl);

            // Appel unique à l'init au premier thread qui crée une connexion
            synchronized (DatabaseManager.class) {
                if (!initialized) {
                    DatabaseInitializer.initDatabase(conn);
                    initialized = true;
                }
            }

            // Activation du mode WAL
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL;");
            }

            return conn;
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

    public Connection getConnection() {
        return connection.get();
    }

    public Connection createNewConnection() {
        try {
            String dbPath = GlobalConfig.getInstance().get("DB_PATH");
            File file = new File(dbPath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists())
                parent.mkdirs();
            String dbUrl = "jdbc:sqlite:" + dbPath;
            Connection conn = DriverManager.getConnection(dbUrl);

            // Si jamais tu veux garantir l'init même ici
            synchronized (DatabaseManager.class) {
                if (!initialized) {
                    DatabaseInitializer.initDatabase(conn);
                    initialized = true;
                }
            }
            return conn;
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'ouvrir la connexion à la base SQLite", e);
        }
    }
}
