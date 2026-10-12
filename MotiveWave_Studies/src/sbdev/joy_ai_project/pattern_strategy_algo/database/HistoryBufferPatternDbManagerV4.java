package sbdev.joy_ai_project.pattern_strategy_algo.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import sbdev.joy_ai_project.pattern_strategy_algo.PatternStrategyAlgo;
import sbdev.joy_ai_project.pattern_strategy_algo.model.Bracket;
import sbdev.joy_ai_project.pattern_strategy_algo.model.PatternRenkoMatched;
import sbdev.joy_ai_project.pattern_strategy_algo.util.Helper;
import sbdev.joy_ai_project.pattern_strategy_algo.util.StudyLogger;

import com.monitoralgo.core.database.DatabaseAlgoManager; // Utilisation du gestionnaire de BDD static

public class HistoryBufferPatternDbManagerV4 {

    // File d'événements à sauvegarder asynchrone
    private final java.util.concurrent.BlockingQueue<SaveEvent> queue = new java.util.concurrent.LinkedBlockingQueue<>(
            10000);
    private final java.util.concurrent.ConcurrentHashMap<String, String> lastSavedPeriod = new java.util.concurrent.ConcurrentHashMap<>();
    private static volatile long lastPurgeDay = -1;

    private final PatternStrategyAlgo strategy;
    private final StudyLogger logger;

    public HistoryBufferPatternDbManagerV4(PatternStrategyAlgo strategy, StudyLogger logger) {
        this.strategy = strategy;
        this.logger = logger;
        // Thread daemon pour écrire en base de données en arrière-plan
        Thread writer = new Thread(() -> {
            Connection conn = null;
            try {
                conn = DatabaseAlgoManager.getConnection(); // Récupère une connexion du pool
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        SaveEvent event = queue.take();
                        PatternRenkoMatched match = saveToDbAndFindMatchPattern(event, conn);
                        if (match != null) {
                            // Log du pattern trouvé
                            this.logger.log("✅ Match trouvé : " + match);
                            // Délègue le traitement du pattern trouvé à la stratégie (génération de signal
                            // d'entrée)
                            this.strategy.onPatternMatched(match);
                        } else {
                            this.logger.log("⚠️ Aucun match trouvé pour " + event.pattern_renko + " - "
                                    + event.pattern_hilo1 + " - " + event.pattern_hilo2);
                        }
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        });
        writer.setName("HistoryBufferPatternDbManager-Writer");
        writer.setDaemon(true);
        writer.start();
    }

    /**
     * Enfile un nouvel événement de pattern Renko/HiLo à sauvegarder. Si un pattern
     * avec exactement les mêmes paramètres a déjà été enregistré au même timestamp,
     * on l'ignore (pas de doublon de période).
     */
    public void saveSnapshotAndFindMatchPattern(String instrumentName, String scale, int interval, double renkoSize,
            String patternRenko, String patternHiLo1, String patternHiLo2, long timestamp) {
        String instrumentKey = Helper.extractInstrumentKeyGroup(instrumentName);
        // Clé unique pour éviter doublon : combine instrument, timeframe, renkoSize,
        // patterns et timestamp
        String key = instrumentKey + "|" + scale + "|" + interval + "|" + renkoSize + "|" + patternRenko + "|"
                + patternHiLo1 + "|" + patternHiLo2 + "|" + timestamp;
        // Si déjà présent, on n'ajoute pas
        if (lastSavedPeriod.putIfAbsent(key, patternRenko + "|" + patternHiLo1 + "|" + patternHiLo2) != null) {
            return;
        }
        String nowIso = Helper.isoNow(); // date/heure actuelle format ISO
        String date = Helper.isoDate(timestamp); // date jour du timestamp format ISO (YYYY-MM-DD)
        // Enfile l'événement de sauvegarde non bloquant
        if (!queue.offer(new SaveEvent(instrumentKey, scale, interval, renkoSize, patternRenko, patternHiLo1,
                patternHiLo2, timestamp, date, nowIso))) {
            this.logger.log("HistoryBufferPatternDbManager queue overflow! Sauvegarde ignorée.");
        }
    }

    /**
     * 1) UPSERT l'empreinte de pattern live (history_buffer_pattern) pour horodater
     * l'occurrence 2) Résout les IDs via les dimensions (instrument, timeframe,
     * renko_size, suite→pattern) 3) Joint la stratégie agrégée pattern_strategy (et
     * ses dimensions) pour récupérer: - id_pattern_strategy - direction (label) -
     * takeprofit/stoploss (value_point) => pour construire le Bracket(tp, sl) - les
     * métriques (count, count_tp, count_sl, delta, winrate, profit_factor, score,
     * last_date_time_entry) 4) Applique le filtre "ps.score > 0" et l'ordre "ORDER
     * BY ps.score DESC" 5) Insère l'occurrence live dans
     * pattern_strategy_finding_live et retourne un PatternRenkoMatched enrichi.
     */
    private PatternRenkoMatched saveToDbAndFindMatchPattern(SaveEvent event, Connection conn) {
        // --------------- SQL ---------------
        // - upserted : on enregistre (ou met à jour) l’occurrence live pour le
        // timestamp donné
        // - i : jeu de clés "naturelles" consolidées (COALESCE) issues de la ligne
        // insérée
        // - Résolution des IDs par jointures sur les dimensions, puis jointure vers
        // pattern_strategy
        // - Filtre ps.score > 0 et tri desc
        final String sql = """
                WITH upserted AS (
                  INSERT INTO history_buffer_pattern (
                    instrument_key, tf_scale, tf_interval, renko_size,
                    pattern_renko, pattern_hilo1, pattern_hilo2,
                    timestamp, date, updated_at
                  )
                  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                  ON CONFLICT (instrument_key, tf_scale, tf_interval, renko_size, pattern_renko, pattern_hilo1, pattern_hilo2, timestamp)
                  DO UPDATE SET updated_at = EXCLUDED.updated_at
                  RETURNING instrument_key, tf_scale, tf_interval, renko_size,
                            pattern_renko, pattern_hilo1, pattern_hilo2
                )
                SELECT
                  -- clés "naturelles" utiles au retour applicatif
                  i.instrument_key,
                  i.tf_scale,
                  i.tf_interval,
                  i.renko_size,
                  i.pattern_renko,
                  i.pattern_hilo1,
                  i.pattern_hilo2,

                  -- données issues de la stratégie consolidée
                  ps.id_pattern_strategy,              -- ✅ on a directement l'ID de la stratégie
                  d.label              AS direction,   -- LONG / SHORT
                  tp.value_point       AS tp,          -- valeur TP en points
                  sl.value_point       AS sl,          -- valeur SL en points

                  -- métriques de la stratégie
                  ps.count,
                  ps.count_tp,
                  ps.count_sl,
                  ps.delta,
                  ps.winrate,
                  ps.profit_factor,
                  ps.score,
                  ps.last_date_time_entry

                FROM (
                  SELECT
                    COALESCE(u.instrument_key, ?) AS instrument_key,
                    COALESCE(u.tf_scale, ?)      AS tf_scale,
                    COALESCE(u.tf_interval, ?)   AS tf_interval,
                    COALESCE(u.renko_size, ?)    AS renko_size,
                    COALESCE(u.pattern_renko, ?) AS pattern_renko,
                    COALESCE(u.pattern_hilo1, ?) AS pattern_hilo1,
                    COALESCE(u.pattern_hilo2, ?) AS pattern_hilo2
                  FROM upserted u
                  RIGHT JOIN (SELECT 1) ON TRUE
                  LIMIT 1
                ) i
                  -- Résolution des IDs par jointures sur les dimensions
                  JOIN instrument    ins ON ins.key   = i.instrument_key
                  JOIN timeframe     tf  ON tf.scale  = i.tf_scale
                                         AND tf.value = i.tf_interval
                  JOIN renko_size    rs  ON rs.value_point = i.renko_size
                  JOIN suite         s0  ON s0.pattern = i.pattern_renko
                  JOIN suite         s1  ON s1.pattern = i.pattern_hilo1
                  JOIN suite         s2  ON s2.pattern = i.pattern_hilo2
                  JOIN pattern       p   ON p.suite_renko_id = s0.id_suite
                                         AND p.suite_hilo1_id = s1.id_suite
                                         AND p.suite_hilo2_id = s2.id_suite
                  -- Jointure vers la stratégie consolidée
                  JOIN pattern_strategy ps
                       ON ps.instrument_id  = ins.id_instrument
                      AND ps.timeframe_id   = tf.id_timeframe
                      AND ps.renko_size_id  = rs.id_renko_size
                      AND ps.pattern_id     = p.id_pattern
                  JOIN direction      d   ON d.id_direction    = ps.direction_id
                  JOIN takeprofit     tp  ON tp.id_takeprofit  = ps.takeprofit_id
                  JOIN stoploss       sl  ON sl.id_stoploss    = ps.stoploss_id
                WHERE ps.score > 0                     -- ✅ filtre demandé
                ORDER BY ps.score DESC                 -- ✅ tri demandé
                LIMIT 1;
                """;

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            // --- Paramètres INSERT (upserted) ---
            pst.setString(1, event.instrument_key);
            pst.setString(2, event.tf_scale);
            pst.setInt(3, event.tf_interval);
            pst.setDouble(4, event.renko_size);
            pst.setString(5, event.pattern_renko);
            pst.setString(6, event.pattern_hilo1);
            pst.setString(7, event.pattern_hilo2);
            pst.setLong(8, event.timestamp);
            pst.setString(9, event.date);
            pst.setString(10, event.updated_at);

            // --- Paramètres COALESCE (bloc i) : mêmes valeurs en secours ---
            pst.setString(11, event.instrument_key);
            pst.setString(12, event.tf_scale);
            pst.setInt(13, event.tf_interval);
            pst.setDouble(14, event.renko_size);
            pst.setString(15, event.pattern_renko);
            pst.setString(16, event.pattern_hilo1);
            pst.setString(17, event.pattern_hilo2);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // ----- Construction de l'objet de sortie -----
                    PatternRenkoMatched pattern = new PatternRenkoMatched();

                    // Clés "naturelles" (retournées telles quelles)
                    pattern.instrumentKey = rs.getString("instrument_key");
                    pattern.intervalScale = rs.getString("tf_scale");
                    pattern.intervalValue = rs.getInt("tf_interval");
                    pattern.renkoSize = rs.getDouble("renko_size");
                    pattern.patternRenko = rs.getString("pattern_renko");
                    pattern.patternHilo1 = rs.getString("pattern_hilo1");
                    pattern.patternHilo2 = rs.getString("pattern_hilo2");

                    // Métier
                    pattern.patternStrategyId = rs.getLong("id_pattern_strategy"); // ✅ déjà résolu
                    pattern.direction = rs.getString("direction"); // LONG / SHORT
                    pattern.bracket = new Bracket(rs.getInt("tp"), // tp (value_point)
                            rs.getInt("sl") // sl (value_point)
                    );

                    // Métriques de la stratégie
                    pattern.count = rs.getInt("count");
                    pattern.countTp = rs.getInt("count_tp");
                    pattern.countSl = rs.getInt("count_sl");
                    pattern.delta = rs.getInt("delta");
                    pattern.winrate = rs.getString("winrate");
                    pattern.profitFactor = rs.getDouble("profit_factor");
                    pattern.score = rs.getDouble("score");
                    pattern.lastDateTimeEntry = rs.getString("last_date_time_entry");

                    // Contexte temporel de l’événement live
                    pattern.timestamp = event.timestamp;
                    pattern.date = event.date;
                    pattern.updated_at = event.updated_at;

                    // ----- On journalise dans pattern_strategy_finding_live -----
                    final String insertFindSql = """
                            INSERT INTO pattern_strategy_finding_live (
                              pattern_strategy_id,
                              instrument_key, timeframe_scale, timeframe_value, renko_size_value_point,
                              pattern_renko, pattern_hilo1, pattern_hilo2,
                              direction_label, takeprofit_value_point, stoploss_value_point,
                              timestamp_entry, date_time_entry,
                              average_bar, count, count_tp, count_sl, delta, winrate, profit_factor, score,
                              stats_updated_at, created_at
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            RETURNING id_pattern_strategy_finding_live;
                            """;
                    try (PreparedStatement pst3 = conn.prepareStatement(insertFindSql)) {
                        pst3.setLong(1, pattern.patternStrategyId);
                        pst3.setString(2, event.instrument_key);
                        pst3.setString(3, event.tf_scale);
                        pst3.setInt(4, event.tf_interval);
                        pst3.setDouble(5, event.renko_size);
                        pst3.setString(6, event.pattern_renko);
                        pst3.setString(7, event.pattern_hilo1);
                        pst3.setString(8, event.pattern_hilo2);
                        pst3.setString(9, pattern.direction);
                        pst3.setDouble(10, pattern.bracket.tp);
                        pst3.setDouble(11, pattern.bracket.sl);
                        pst3.setLong(12, event.timestamp);
                        pst3.setString(13, pattern.date);
                        pst3.setObject(14, null); // average_bar non disponible ici
                        pst3.setInt(15, pattern.count);
                        pst3.setInt(16, pattern.countTp);
                        pst3.setInt(17, pattern.countSl);
                        pst3.setInt(18, pattern.delta);
                        pst3.setString(19, pattern.winrate);
                        pst3.setDouble(20, pattern.profitFactor);
                        pst3.setDouble(21, pattern.score);
                        pst3.setString(22, pattern.lastDateTimeEntry);
                        pst3.setString(23, pattern.updated_at);

                        try (ResultSet rs3 = pst3.executeQuery()) {
                            if (rs3.next()) {
                                pattern.patternFindingId = rs3.getLong("id_pattern_strategy_finding_live");
                            }
                        }
                    }

                    return pattern;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Purge de sécurité
        purgeIfNeeded(conn);
        return null;
    }

    /**
     * Purge la table history_buffer_pattern des entrées plus anciennes que 2 jours
     * pour éviter l'inflation de la table.
     */
    private void purgeIfNeeded(Connection conn) {
        long today = System.currentTimeMillis() / (24L * 60 * 60 * 1000);
        if (lastPurgeDay != today) {
            int ligneNumber = purgeOldEntries(conn);
            if (ligneNumber > 0) {
                this.logger.log("HistoryBufferPatternDbManager : Purge de " + ligneNumber
                        + " entrées history_buffer_pattern (plus de 2 jours)");
            }
            lastPurgeDay = today;
        }
    }

    private static int purgeOldEntries(Connection conn) {
        long cutoff = System.currentTimeMillis() - 2L * 24 * 60 * 60 * 1000;
        String deleteOldSql = "DELETE FROM history_buffer_pattern WHERE timestamp < ?";
        try (PreparedStatement delOld = conn.prepareStatement(deleteOldSql)) {
            delOld.setLong(1, cutoff);
            int n = delOld.executeUpdate();
            return n;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Réinitialise complètement la table history_buffer_pattern (pour du debug ou
     * un nouveau backtest).
     */
    public static void resetHistoryTable() {
        try (Connection conn = DatabaseAlgoManager.getConnection(); Statement st = conn.createStatement()) {
            st.execute("TRUNCATE TABLE history_buffer_pattern RESTART IDENTITY");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Événement interne représentant un pattern identifié à un instant donné, en
     * attente de sauvegarde.
     */
    private static class SaveEvent {
        final String instrument_key, tf_scale;
        final String pattern_renko, pattern_hilo1, pattern_hilo2;
        final String date, updated_at;
        final double renko_size;
        final int tf_interval;
        final long timestamp;

        SaveEvent(String instrumentKey, String tf_scale, int tf_interval, double renko_size, String pattern_renko,
                String pattern_hilo1, String pattern_hilo2, long timestamp, String date, String updated_at) {
            this.instrument_key = instrumentKey;
            this.tf_scale = tf_scale;
            this.tf_interval = tf_interval;
            this.renko_size = renko_size;
            this.pattern_renko = pattern_renko;
            this.pattern_hilo1 = pattern_hilo1;
            this.pattern_hilo2 = pattern_hilo2;
            this.timestamp = timestamp;
            this.date = date;
            this.updated_at = updated_at;
        }
    }
}
