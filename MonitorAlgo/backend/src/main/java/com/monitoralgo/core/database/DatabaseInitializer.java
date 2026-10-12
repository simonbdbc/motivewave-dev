package com.monitoralgo.core.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    /** Initialise la structure de la base sur la connexion passée */
    public static void initDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {

            // --- TABLES DE DIMENSIONS ---

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS instrument (
                        id_instrument SERIAL PRIMARY KEY,
                        label TEXT NOT NULL,        -- ex: "NQM5"
                        key TEXT NOT NULL,          -- code technique
                        tick_size FLOAT NOT NULL,
                        UNIQUE(key)
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS timeframe (
                        id_timeframe SERIAL PRIMARY KEY,
                        scale TEXT NOT NULL,    -- ex: "min"
                        value INT NOT NULL,     -- ex: 1, 5, 15...
                        UNIQUE (scale, value)   -- ✅ Ajout
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS direction (
                        id_direction SERIAL PRIMARY KEY,
                        label TEXT NOT NULL CHECK(label IN ('LONG', 'SHORT')),
                        UNIQUE (label)            -- ✅ Ajout (sinon FK impossible)
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS suite (
                        id_suite SERIAL PRIMARY KEY,
                        pattern TEXT NOT NULL UNIQUE
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS renko_size (
                        id_renko_size SERIAL PRIMARY KEY,
                        value_point FLOAT NOT NULL,
                        value_tick INT NOT NULL,
                        UNIQUE (value_point)                   -- ✅ Ajout
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS takeprofit (
                        id_takeprofit SERIAL PRIMARY KEY,
                        value_point FLOAT NOT NULL,
                        value_tick INT NOT NULL,
                        UNIQUE (value_point)                   -- ✅ Ajout
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS stoploss (
                        id_stoploss SERIAL PRIMARY KEY,
                        value_point FLOAT NOT NULL,
                        value_tick INT NOT NULL,
                        UNIQUE (value_point)                   -- ✅ Ajout
                    );
                    """);

            // --- PATTERN : COMBINAISON DE 3 SUITES ---

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS pattern (
                        id_pattern SERIAL PRIMARY KEY,
                        suite_renko_id INT NOT NULL REFERENCES suite(id_suite),
                        suite_hilo1_id INT NOT NULL REFERENCES suite(id_suite),
                        suite_hilo2_id INT NOT NULL REFERENCES suite(id_suite),
                        UNIQUE(suite_renko_id, suite_hilo1_id, suite_hilo2_id)
                    );
                    """);

            // --- STRATÉGIE (COMBINAISON UNIQUE DE TOUS LES PARAMÈTRES) ---

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS pattern_strategy (
                        id_pattern_strategy BIGSERIAL PRIMARY KEY,
                        instrument_id INT NOT NULL REFERENCES instrument(id_instrument),
                        timeframe_id INT NOT NULL REFERENCES timeframe(id_timeframe),
                        renko_size_id INT NOT NULL REFERENCES renko_size(id_renko_size),
                        pattern_id INT NOT NULL REFERENCES pattern(id_pattern),
                        direction_id INT NOT NULL REFERENCES direction(id_direction),
                        takeprofit_id INT NOT NULL REFERENCES takeprofit(id_takeprofit),
                        stoploss_id INT NOT NULL REFERENCES stoploss(id_stoploss),
                        last_timestamp_entry BIGINT,
                        last_date_time_entry TEXT,
                        average_bar INTEGER,
                        count INTEGER,
                        count_tp INTEGER,
                        count_sl INTEGER,
                        delta INTEGER,
                        winrate TEXT,
                        profit_factor DOUBLE PRECISION,
                        score DOUBLE PRECISION,
                        stats_updated_at TEXT,
                        created_at TEXT,
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
                    CREATE INDEX IF NOT EXISTS idx_pattern_strategy_instrument_timeframe
                    ON pattern_strategy (instrument_id, timeframe_id);
                    """);

            stmt.execute("""
                    CREATE INDEX IF NOT EXISTS idx_pattern_strategy_pattern
                    ON pattern_strategy (pattern_id);
                    """);

            stmt.execute("""
                    CREATE INDEX IF NOT EXISTS idx_pattern_strategy_direction
                    ON pattern_strategy (direction_id);
                    """);

            // --- TRADES INDIVIDUELS HISTORY (nouvelle table demandée) ---

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS trade_strategy_history (
                        id_trade BIGSERIAL PRIMARY KEY,
                        pattern_strategy_id BIGINT NOT NULL REFERENCES pattern_strategy(id_pattern_strategy),
                        timestamp_entry BIGINT NOT NULL,
                        date_time_entry TEXT NOT NULL,
                        entry_price DOUBLE PRECISION NOT NULL,
                        timestamp_exit BIGINT,
                        date_time_exit TEXT,
                        exit_price DOUBLE PRECISION,
                        tp_price DOUBLE PRECISION,
                        sl_price DOUBLE PRECISION,
                        count_bar INTEGER,
                        result TEXT,
                        created_at TEXT,
                        UNIQUE(pattern_strategy_id, timestamp_entry, entry_price)
                    );
                    """);

            // --- STAGING UNLOGGED pour bulk COPY → MERGE vers history ---

            stmt.execute("""
                    CREATE UNLOGGED TABLE IF NOT EXISTS trade_strategy_history_stage (
                        instrument_id      INT NOT NULL,
                        timeframe_id       INT NOT NULL,
                        renko_size_id      INT NOT NULL,
                        pattern_id         INT NOT NULL,
                        direction_id       INT NOT NULL,
                        takeprofit_id      INT NOT NULL,
                        stoploss_id        INT NOT NULL,

                        timestamp_entry    BIGINT NOT NULL,
                        date_time_entry    TEXT NOT NULL,
                        entry_price        DOUBLE PRECISION NOT NULL,

                        timestamp_exit     BIGINT,
                        date_time_exit     TEXT,
                        exit_price         DOUBLE PRECISION,

                        tp_price           DOUBLE PRECISION,
                        sl_price           DOUBLE PRECISION,
                        count_bar          INTEGER,
                        result             TEXT,
                        created_at         TEXT
                    );
                    """);

            // Indexes utiles pour le MERGE (JOIN vers pattern_strategy)
            stmt.execute(
                    """
                            CREATE INDEX IF NOT EXISTS idx_tshs_keys
                            ON trade_strategy_history_stage (instrument_id, timeframe_id, renko_size_id, pattern_id, direction_id, takeprofit_id, stoploss_id);
                            """);

            // --- LOG PATTERN ALERTES ---

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS log_notification (
                        id SERIAL PRIMARY KEY,
                        pattern VARCHAR(255),
                        message VARCHAR(255),
                        active BOOLEAN NOT NULL
                    );
                    """);

            // --- FONCTION SCORE (inchangée) ---

            stmt.execute(
                    """
                            CREATE OR REPLACE FUNCTION compute_score_brut(
                                count_total integer,
                                count_tp integer,
                                count_sl integer,
                                profit_factor double precision
                            )
                            RETURNS double precision AS $$
                            DECLARE
                                winrate double precision;
                                capped_pf double precision;
                                log_volume double precision;
                                winrate_term double precision;
                                log_pf double precision;
                                score double precision;
                            BEGIN
                                IF count_total <= 0 OR count_tp < 0 OR count_sl < 0 OR count_tp > count_total OR count_sl > count_total THEN
                                    RETURN 0.0;
                                END IF;
                                IF profit_factor < 0 THEN
                                    RETURN 0.0;
                                END IF;
                                winrate := count_tp::double precision / count_total;
                                IF (count_total <= 10 AND winrate < 1.0) OR
                                    (count_total <= 100 AND winrate < 0.90) OR
                                    (count_total <= 1000 AND winrate < 0.80) OR
                                    (count_total <= 10000 AND winrate < 0.70) OR
                                    (count_total > 10000 AND winrate < 0.60) THEN
                                    RETURN 0.0;
                                END IF;
                                capped_pf := LEAST(profit_factor, 5.0);
                                log_volume := LN(1.0 + count_total);
                                winrate_term := 2.0 * winrate - 1.0;
                                log_pf := LN(1.0 + capped_pf);
                                score := log_volume * winrate_term * log_pf;
                                IF score > 0 AND score IS NOT NULL THEN
                                    RETURN ROUND(score::numeric, 2);
                                ELSE
                                    RETURN 0.0;
                                END IF;
                            END;
                            $$ LANGUAGE plpgsql IMMUTABLE;
                            """);

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

            // pattern_strategy_finding_live avec FK composite timeframe
            stmt.execute(
                    """
                                CREATE TABLE IF NOT EXISTS pattern_strategy_finding_live (
                                id_pattern_strategy_finding_live BIGSERIAL PRIMARY KEY,
                                pattern_strategy_id BIGINT NOT NULL REFERENCES pattern_strategy(id_pattern_strategy),

                                instrument_key TEXT NOT NULL REFERENCES instrument(key),

                                timeframe_scale TEXT NOT NULL,
                                timeframe_value INT NOT NULL,

                                renko_size_value_point FLOAT NOT NULL REFERENCES renko_size(value_point),

                                pattern_renko TEXT NOT NULL REFERENCES suite(pattern),
                                pattern_hilo1  TEXT NOT NULL REFERENCES suite(pattern),
                                pattern_hilo2  TEXT NOT NULL REFERENCES suite(pattern),

                                direction_label TEXT NOT NULL REFERENCES direction(label),
                                takeprofit_value_point FLOAT NOT NULL REFERENCES takeprofit(value_point),
                                stoploss_value_point  FLOAT NOT NULL REFERENCES stoploss(value_point),

                                timestamp_entry BIGINT,
                                date_time_entry TEXT,
                                average_bar INTEGER,
                                count INTEGER,
                                count_tp INTEGER,
                                count_sl INTEGER,
                                delta INTEGER,
                                winrate TEXT,
                                profit_factor DOUBLE PRECISION,
                                score DOUBLE PRECISION,
                                stats_updated_at TEXT,
                                created_at TEXT,

                                FOREIGN KEY (timeframe_scale, timeframe_value) REFERENCES timeframe(scale, value)  -- ✅ FK composite
                                );
                            """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS trade_strategy_live (
                        id_trade BIGSERIAL PRIMARY KEY,
                        pattern_strategy_id BIGINT NOT NULL REFERENCES pattern_strategy(id_pattern_strategy),
                        timestamp_entry BIGINT NOT NULL,
                        date_time_entry TEXT NOT NULL,
                        entry_price DOUBLE PRECISION NOT NULL,
                        timestamp_exit BIGINT,
                        date_time_exit TEXT,
                        exit_price DOUBLE PRECISION,
                        tp_price DOUBLE PRECISION,
                        sl_price DOUBLE PRECISION,
                        result TEXT,
                        updated_at TEXT,
                        UNIQUE(pattern_strategy_id, timestamp_entry, entry_price)
                    );
                    """);

            // trading_session : 1 ligne par jour et par jeu de paramètres de risque
            stmt.execute("""
                      CREATE TABLE IF NOT EXISTS trading_session (
                        id_session BIGSERIAL PRIMARY KEY,
                        session_date TEXT NOT NULL,                 -- ex: "2025-08-13" (timezone applicative)
                        pnl_session DOUBLE PRECISION,
                        trade_count INT,
                        count_tp INT,
                        count_sl INT,
                        successive_loss_count INT,
                        max_win_threshold INT NOT NULL,
                        max_loss_threshold INT NOT NULL,
                        max_successive_loss_threshold INT NOT NULL,
                        successive_loss_enabled BOOLEAN NOT NULL,
                        start_time TEXT,                            -- set lors de la (première) (re)ouverture
                        end_time TEXT,                              -- set à la fermeture de session/journée
                        updated_at TEXT,
                        created_at TEXT,

                        -- ✅ Unicité d'une session par jour et par paramètres de risque
                        UNIQUE(
                          session_date,
                          max_win_threshold,
                          max_loss_threshold,
                          max_successive_loss_threshold,
                          successive_loss_enabled
                        )
                      );
                    """);

            // (Optionnel) index utile pour requêtes par date
            stmt.execute("""
                      CREATE INDEX IF NOT EXISTS idx_trading_session_date
                      ON trading_session(session_date);
                    """);

            // -- Chaque trade live appartient à UNE seule trading_session
            // -- et est associé à UN pattern_strategy_finding_live.
            // -- La contrainte UNIQUE(id_trade) empêche qu’un même trade soit rattaché à
            // plusieurs sessions.
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS trading_session_trade_link (
                        id_session BIGINT NOT NULL REFERENCES trading_session(id_session) ON DELETE CASCADE,
                        id_trade BIGINT NOT NULL REFERENCES trade_strategy_live(id_trade) ON DELETE CASCADE,
                        id_pattern_strategy_finding_live BIGINT NOT NULL
                          REFERENCES pattern_strategy_finding_live(id_pattern_strategy_finding_live) ON DELETE RESTRICT,

                        created_at TEXT NOT NULL,

                        PRIMARY KEY (id_session, id_trade),
                        UNIQUE (id_trade)
                      );
                        """);

            // -- Index utiles
            stmt.execute("""
                    CREATE INDEX IF NOT EXISTS idx_tstl_session
                    ON trading_session_trade_link(id_session);
                        """);

            stmt.execute("""
                    CREATE INDEX IF NOT EXISTS idx_tstl_pattern
                    ON trading_session_trade_link(id_pattern_strategy_finding_live);
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
            throw new IllegalArgumentException("Nom de table invalide : " + table);
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
            // ordre de drop large pour éviter les FK
            stmt.executeUpdate("DROP TABLE IF EXISTS trade_strategy_history_stage CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS trade_strategy_history CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS trade_strategy_live CASCADE");
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
            stmt.executeUpdate("DROP TABLE IF EXISTS log_pattern CASCADE");
            initDatabase(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
