package backtest.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    /** Initialise la structure de la base sur la connexion passée */
    public static void initDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {

            // --- TABLES DE DIMENSIONS ---

            stmt.execute("""
                    CREATE TABLE instrument (
                        id_instrument SERIAL PRIMARY KEY,
                        label TEXT NOT NULL,        -- ex: "NQM5"
                        key TEXT NOT NULL UNIQUE,   -- code technique
                        tick_size FLOAT NOT NULL
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE timeframe (
                        id_timeframe SERIAL PRIMARY KEY,
                        scale TEXT NOT NULL,    -- ex: "min"
                        value INT NOT NULL      -- ex: 1, 5, 15...
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE direction (
                        id_direction SERIAL PRIMARY KEY,
                        label TEXT NOT NULL CHECK(label IN ('LONG', 'SHORT'))
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE suite (
                        id_suite SERIAL PRIMARY KEY,
                        pattern TEXT NOT NULL UNIQUE
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE renko_size (
                        id_renko_size SERIAL PRIMARY KEY,
                        value_point FLOAT NOT NULL,
                        value_tick INT NOT NULL
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE takeprofit (
                        id_takeprofit SERIAL PRIMARY KEY,
                        value_point FLOAT NOT NULL,
                        value_tick INT NOT NULL
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE stoploss (
                        id_stoploss SERIAL PRIMARY KEY,
                        value_point FLOAT NOT NULL,
                        value_tick INT NOT NULL
                    );
                    """);

            // --- PATTERN : COMBINAISON DE 3 SUITES ---

            stmt.execute("""
                    CREATE TABLE pattern (
                        id_pattern SERIAL PRIMARY KEY,
                        suite_renko_id INT NOT NULL REFERENCES suite(id_suite),
                        suite_hilo1_id INT NOT NULL REFERENCES suite(id_suite),
                        suite_hilo2_id INT NOT NULL REFERENCES suite(id_suite),
                        UNIQUE(suite_renko_id, suite_hilo1_id, suite_hilo2_id)
                    );
                    """);

            // --- STRATÉGIE (COMBINAISON UNIQUE DE TOUS LES PARAMÈTRES) ---

            stmt.execute("""
                    CREATE TABLE pattern_strategy (
                        id_pattern_strategy BIGSERIAL PRIMARY KEY,
                        instrument_id INT NOT NULL REFERENCES instrument(id_instrument),
                        timeframe_id INT NOT NULL REFERENCES timeframe(id_timeframe),
                        renko_size_id INT NOT NULL REFERENCES renko_size(id_renko_size),
                        pattern_id INT NOT NULL REFERENCES pattern(id_pattern),
                        direction_id INT NOT NULL REFERENCES direction(id_direction),
                        takeprofit_id INT NOT NULL REFERENCES takeprofit(id_takeprofit),
                        stoploss_id INT NOT NULL REFERENCES stoploss(id_stoploss),
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
                            instrument_id,
                            timeframe_id,
                            renko_size_id,
                            pattern_id,
                            direction_id,
                            takeprofit_id,
                            stoploss_id
                        )
                    );
                    """);

            // --- INDEXES RECOMMANDÉS POUR pattern_strategy ---

            stmt.execute("""
                    CREATE INDEX idx_pattern_strategy_instrument_timeframe
                    ON pattern_strategy (instrument_id, timeframe_id);
                    """);

            stmt.execute("""
                    CREATE INDEX idx_pattern_strategy_pattern
                    ON pattern_strategy (pattern_id);
                    """);

            stmt.execute("""
                    CREATE INDEX idx_pattern_strategy_direction
                    ON pattern_strategy (direction_id);
                    """);

            // --- TRADES INDIVIDUELS ---

            stmt.execute("""
                    CREATE TABLE trade_strategy (
                        id_trade BIGSERIAL PRIMARY KEY,
                        pattern_strategy_id BIGINT NOT NULL REFERENCES pattern_strategy(id_pattern_strategy),
                        timestamp BIGINT NOT NULL,
                        date_time_entry TEXT NOT NULL,
                        date_time_exit TEXT,
                        entry_price DOUBLE PRECISION,
                        exit_price DOUBLE PRECISION,
                        tp_price DOUBLE PRECISION,
                        sl_price DOUBLE PRECISION,
                        result TEXT,
                        created_at TEXT,
                        UNIQUE(pattern_strategy_id, date_time_entry, entry_price)
                    );
                    """);

            // --- INDEXES RECOMMANDÉS POUR trade_strategy ---

            stmt.execute("""
                    CREATE INDEX idx_trade_strategy_pattern ON trade_strategy(pattern_strategy_id);
                    """);

            stmt.execute("""
                    CREATE INDEX idx_trade_strategy_timestamp ON trade_strategy(timestamp);
                    """);

            stmt.execute("""
                    CREATE INDEX idx_trade_strategy_pattern_time ON trade_strategy(pattern_strategy_id, timestamp);
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
            stmt.executeUpdate("DROP TABLE IF EXISTS trade_strategy CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS pattern_strategy CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS pattern CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS stoploss CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS takeprofit CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS renko_size CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS suite CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS direction CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS instrument CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS timeframe CASCADE");
            initDatabase(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
