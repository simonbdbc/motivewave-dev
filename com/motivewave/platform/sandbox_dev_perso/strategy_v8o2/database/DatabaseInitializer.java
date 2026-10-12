package sbdev.joy_ai_project.strategy_v8o3.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    /** Initialise la structure de la base sur la connexion passée */
    public static void initDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS instrument (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT UNIQUE,
                            instrument_key TEXT
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS interval (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            scale TEXT NOT NULL,
                            interval INTEGER NOT NULL,
                            UNIQUE (scale, interval)
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS strategy (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            instrument_id INTEGER,
                            interval_id INTEGER,
                            name TEXT,
                            takeprofit REAL,
                            stoploss REAL,
                            max_win INTEGER,
                            max_loss INTEGER,
                            max_consecutive_loss INTEGER,
                            UNIQUE (instrument_id, interval_id, name),
                            FOREIGN KEY (instrument_id) REFERENCES instrument(id),
                            FOREIGN KEY (interval_id) REFERENCES interval(id)
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS strategy_hours (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            strategy_id INTEGER,
                            start TEXT,
                            end TEXT,
                            FOREIGN KEY (strategy_id) REFERENCES strategy(id)
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS trade (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            strategy_id INTEGER,
                            timestamp INTEGER,
                            date_time TEXT,
                            direction TEXT,
                            entry_price REAL,
                            tp_price REAL,
                            sl_price REAL,
                            result TEXT,
                            updated_at TEXT,
                            UNIQUE (strategy_id, timestamp),
                            FOREIGN KEY (strategy_id) REFERENCES strategy(id)
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS renko_snapshot_aio (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            instrument_key TEXT NOT NULL,
                            instrument_name TEXT NOT NULL,
                            interval_scale TEXT NOT NULL,
                            interval_value INTEGER NOT NULL,
                            timestamp INTEGER NOT NULL,
                            date_time TEXT NOT NULL,
                            price_res REAL NOT NULL,
                            price_supp REAL NOT NULL,
                            renko_color TEXT NOT NULL,
                            hilo_color_1 TEXT NOT NULL,
                            hilo_color_2 TEXT NOT NULL,
                            hilo_price_1 REAL NOT NULL,
                            hilo_price_2 REAL NOT NULL,
                            updated_at TEXT,
                            UNIQUE (instrument_key, interval_scale, interval_value, timestamp)
                        );
                    """);

            stmt.execute("""
                        CREATE INDEX IF NOT EXISTS idx_renko_snapshot_instrument_interval_time
                        ON renko_snapshot_aio (instrument_key, interval_scale, interval_value, timestamp);
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS hilo_history (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            instrument TEXT,
                            hilo_type TEXT,
                            value REAL,
                            color TEXT,
                            timestamp INTEGER,
                            date TEXT,
                            idx INTEGER,
                            updated_at TEXT,
                            UNIQUE (hilo_type, timestamp)
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS renko_snapshot_aio_live (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            instrument_key TEXT NOT NULL,
                            instrument_name TEXT NOT NULL,
                            interval_scale TEXT NOT NULL,
                            interval_value INTEGER NOT NULL,
                            timestamp INTEGER NOT NULL,
                            date_time TEXT NOT NULL,
                            price_res REAL NOT NULL,
                            price_supp REAL NOT NULL,
                            renko_color TEXT NOT NULL,
                            hilo_color_1 TEXT NOT NULL,
                            hilo_color_2 TEXT NOT NULL,
                            hilo_price_1 REAL NOT NULL,
                            hilo_price_2 REAL NOT NULL,
                            updated_at TEXT,
                            UNIQUE (instrument_key, interval_scale, interval_value, timestamp)
                        );
                    """);

            stmt.execute("""
                        CREATE INDEX IF NOT EXISTS idx_renko_snapshot_live_instrument_interval_time
                        ON renko_snapshot_aio_live (instrument_key, interval_scale, interval_value, timestamp);
                    """);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Erreur création tables SQLite : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Supprime une table et ré-initialise la base */
    public static void deleteTableDatabase(Connection conn, String table) {
        if (!table.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("Nom de table invalide : " + table);
        }

        String dropSql = "DROP TABLE IF EXISTS " + table;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(dropSql);
            initDatabase(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Réinitialise toute la structure (supprime, puis recrée) */
    public static void resetDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS trade");
            stmt.executeUpdate("DROP TABLE IF EXISTS strategy_hours");
            stmt.executeUpdate("DROP TABLE IF EXISTS strategy");
            stmt.executeUpdate("DROP TABLE IF EXISTS renko_snapshot_aio");
            stmt.executeUpdate("DROP TABLE IF EXISTS instrument");
            stmt.executeUpdate("DROP TABLE IF EXISTS interval");
            stmt.executeUpdate("DROP TABLE IF EXISTS hilo_history");
            stmt.executeUpdate("DROP TABLE IF EXISTS renko_snapshot_aio_live");
            initDatabase(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
