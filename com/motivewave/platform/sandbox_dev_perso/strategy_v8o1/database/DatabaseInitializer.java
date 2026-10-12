package sbdev.joy_ai_project.strategy_v8o1.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import sbdev.joy_ai_project.strategy_v8o3.config.Config;

public class DatabaseInitializer {
    // private static final String DB_PATH =
    // "/Users/simon/Projets/PlateformeDev/dev-motivewave/MotiveWave_Studies/src/sbdev/joy_ai_project/strategy_v8o3/database/data_sqlite/strategy_stats_v8.db";

    private static final String DB_PATH = Config.getInstance().get("DB_PATH");
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {

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

            // stmt.execute("""
            // CREATE TABLE IF NOT EXISTS renko_snapshot (
            // id INTEGER PRIMARY KEY AUTOINCREMENT,
            // instrument_id INTEGER NOT NULL,
            // interval_id INTEGER NOT NULL,
            // timestamp INTEGER NOT NULL,
            // date_time TEXT NOT NULL,
            // price_res REAL NOT NULL,
            // price_supp REAL NOT NULL,
            // renko_color TEXT NOT NULL,
            // hilo_color_1 TEXT NOT NULL,
            // hilo_color_2 TEXT NOT NULL,
            // hilo_price_1 REAL NOT NULL,
            // hilo_price_2 REAL NOT NULL,
            // FOREIGN KEY (instrument_id) REFERENCES instrument(id),
            // FOREIGN KEY (interval_id) REFERENCES interval(id),
            // UNIQUE (instrument_id, interval_id, timestamp)
            // );
            // CREATE INDEX idx_renko_snapshot_instrument_interval_time
            // ON renko_snapshot (instrument_id, interval_id, timestamp);
            // """);

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
                        CREATE INDEX idx_renko_snapshot_instrument_interval_time
                        ON renko_snapshot_aio (instrument_key, interval_scale, interval_value, timestamp);
                    """);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void resetDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DROP TABLE IF EXISTS trade");
            stmt.executeUpdate("DROP TABLE IF EXISTS strategy_hours");
            stmt.executeUpdate("DROP TABLE IF EXISTS strategy");
            stmt.executeUpdate("DROP TABLE IF EXISTS renko_snapshot_aio");
            stmt.executeUpdate("DROP TABLE IF EXISTS instrument");
            stmt.executeUpdate("DROP TABLE IF EXISTS interval");

            // DROP TABLE IF EXISTS trade;
            // DROP TABLE IF EXISTS strategy_hours;
            // DROP TABLE IF EXISTS strategy;
            // DROP TABLE IF EXISTS renko_snapshot_aio;
            // DROP TABLE IF EXISTS instrument;
            // DROP TABLE IF EXISTS interval;

            initDatabase();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // public static void main(String[] args) {
    // initDatabase();
    // }
}
