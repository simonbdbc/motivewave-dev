package sbdev.joy_ai_project.strategy.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    /** Initialise la structure de la base sur la connexion passée */
    public static void initDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {

            // Table instrument
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS instrument (
                            id SERIAL PRIMARY KEY,
                            name TEXT UNIQUE,
                            instrument_key TEXT
                        );
                    """);

            // Table timeframe
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS timeframe (
                            id SERIAL PRIMARY KEY,
                            scale TEXT NOT NULL,
                            interval INTEGER NOT NULL,
                            UNIQUE (scale, interval)
                        );
                    """);

            // Table strategy
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS strategy (
                            id SERIAL PRIMARY KEY,
                            instrument_id INTEGER REFERENCES instrument(id),
                            timeframe_id INTEGER REFERENCES timeframe(id),
                            name TEXT,
                            takeprofit DOUBLE PRECISION,
                            stoploss DOUBLE PRECISION,
                            max_win INTEGER,
                            max_loss INTEGER,
                            max_consecutive_loss INTEGER,
                            UNIQUE (instrument_id, timeframe_id, name)
                        );
                    """);

            // Table strategy_hours
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS strategy_hours (
                            id SERIAL PRIMARY KEY,
                            strategy_id INTEGER REFERENCES strategy(id),
                            start TEXT,
                            "end" TEXT
                        );
                    """);

            // Table trade
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS trade (
                            id SERIAL PRIMARY KEY,
                            strategy_id INTEGER REFERENCES strategy(id),
                            timestamp BIGINT,
                            date_time TEXT,
                            direction TEXT,
                            entry_price DOUBLE PRECISION,
                            tp_price DOUBLE PRECISION,
                            sl_price DOUBLE PRECISION,
                            result TEXT,
                            updated_at TEXT,
                            UNIQUE (strategy_id, timestamp)
                        );
                    """);

            // Table snapshot_aio_by_renko
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS snapshot_aio_by_renko (
                            id SERIAL PRIMARY KEY,
                            instrument_key TEXT NOT NULL,
                            instrument_name TEXT NOT NULL,
                            tf_scale TEXT NOT NULL,
                            tf_interval INTEGER NOT NULL,
                            renko_size FLOAT4 NOT NULL,
                            timestamp BIGINT NOT NULL,
                            date_time TEXT NOT NULL,
                            price_res DOUBLE PRECISION NOT NULL,
                            price_sup DOUBLE PRECISION NOT NULL,
                            renko_color TEXT NOT NULL,
                            hilo_color_1 TEXT NOT NULL,
                            hilo_color_2 TEXT NOT NULL,
                            hilo_price_1 DOUBLE PRECISION NOT NULL,
                            hilo_price_2 DOUBLE PRECISION NOT NULL,
                            updated_at TEXT,
                            UNIQUE (instrument_key, tf_scale, tf_interval, renko_size, timestamp)
                        );
                    """);

            // Index sur snapshot_aio_by_renko
            stmt.execute("""
                        CREATE INDEX IF NOT EXISTS idx_snapshot_aio_by_renko_instrument_interval_time
                        ON snapshot_aio_by_renko (instrument_key, tf_scale, tf_interval, renko_size, timestamp);
                    """);

            // Table pattern_renko
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS pattern_renko (
                            instrument_key TEXT NOT NULL,
                            tf_scale TEXT NOT NULL,
                            tf_interval INTEGER NOT NULL,
                            renko_size FLOAT4 NOT NULL,
                            tp INTEGER NOT NULL,
                            sl INTEGER NOT NULL,
                            pattern_renko TEXT NOT NULL,
                            pattern_hilo1 TEXT NOT NULL,
                            pattern_hilo2 TEXT NOT NULL,
                            direction TEXT NOT NULL,
                            count INTEGER,
                            last_date_time_entry TEXT,
                            count_tp INTEGER,
                            count_sl INTEGER,
                            delta INTEGER,
                            winrate TEXT,
                            profit_factor DOUBLE PRECISION,
                            score DOUBLE PRECISION,
                            updated_at TEXT,
                            UNIQUE (
                                instrument_key, tf_scale, tf_interval, renko_size, tp, sl,
                                pattern_renko, pattern_hilo1, pattern_hilo2, direction
                            )
                        );
                    """);

            // Table history_hilo
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS history_hilo (
                            id SERIAL PRIMARY KEY,
                            instrument_key TEXT,
                            hilo_type TEXT,
                            price DOUBLE PRECISION NOT NULL,
                            color TEXT,
                            timestamp BIGINT NOT NULL,
                            date TEXT,
                            updated_at TEXT,
                            UNIQUE (instrument_key, hilo_type, timestamp)
                        );
                    """);

            // Table history_renko
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS history_renko (
                            id SERIAL PRIMARY KEY,
                            instrument_key TEXT,
                            tf_interval INTEGER,
                            renko_size FLOAT4 NOT NULL,
                            price_res DOUBLE PRECISION NOT NULL,
                            price_sup DOUBLE PRECISION NOT NULL,
                            color TEXT,
                            timestamp BIGINT NOT NULL,
                            date TEXT,
                            updated_at TEXT,
                            UNIQUE (instrument_key, tf_interval, renko_size, timestamp)
                        );
                    """);

            // Table pattern_renko
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS history_buffer_pattern (
                            instrument_key TEXT NOT NULL,
                            tf_scale TEXT NOT NULL,
                            tf_interval INTEGER NOT NULL,
                            renko_size FLOAT4 NOT NULL,
                            pattern_renko TEXT NOT NULL,
                            pattern_hilo1 TEXT NOT NULL,
                            pattern_hilo2 TEXT NOT NULL,
                            timestamp BIGINT NOT NULL,
                            date TEXT,
                            updated_at TEXT,
                            UNIQUE (
                                instrument_key, tf_scale, tf_interval, renko_size,
                                pattern_renko, pattern_hilo1, pattern_hilo2, timestamp
                            )
                        );
                    """);

            System.out.println("Database initialized successfully (PostgreSQL, timeframe nomenclature).");

        } catch (SQLException e) {
            System.err.println("Erreur création tables PostgreSQL : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Supprime une table et ré-initialise la base */
    public static void deleteTableDatabase(Connection conn, String table) {
        if (!table.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("Nom de table invalide : " + table);
        }

        String dropSql = "DROP TABLE IF EXISTS " + table + " CASCADE";
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
            stmt.executeUpdate("DROP TABLE IF EXISTS trade CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS strategy_hours CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS strategy CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS snapshot_aio_by_renko CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS instrument CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS timeframe CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS history_hilo CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS history_renko CASCADE");
            initDatabase(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
