package sbdev.joy_ai_project.strategy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import sbdev.joy_ai_project.strategy.StrategyByIntervalV4;
import sbdev.joy_ai_project.strategy.model.Bracket;
import sbdev.joy_ai_project.strategy.model.PatternRenkoMatched;
import sbdev.joy_ai_project.strategy.util.Helper;
import sbdev.joy_ai_project.strategy.util.StudyLogger;

public class HistoryBufferPatternDbManagerV4 {

    private final BlockingQueue<SaveEvent> queue = new LinkedBlockingQueue<>(10000);
    private final ConcurrentHashMap<String, String> lastSavedPeriod = new ConcurrentHashMap<>();

    private static volatile long lastPurgeDay = -1;

    private final StrategyByIntervalV4 strategy;
    private final StudyLogger logger;

    public HistoryBufferPatternDbManagerV4(StrategyByIntervalV4 strategy, StudyLogger logger) {
        this.strategy = strategy;
        this.logger = logger;
        Thread writer = new Thread(() -> {
            Connection conn = null;
            try {
                try {
                    conn = DatabaseManager.getConnection();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        SaveEvent event = queue.take();
                        PatternRenkoMatched match = saveToDbAndFindMatchPattern(event, conn);
                        if (match != null) {
                            // System.out.println("✅ Match trouvé : " + match);
                            this.logger.log("✅ Match trouvé : " + match);
                            // 🔁 Appelle ici le traitement métier (ex :
                            this.strategy.handlePatternMatched(match);
                        } else {
                            this.logger.log("⚠️ Aucun match trouvé pour " + event.pattern_renko + " - "
                                    + event.pattern_hilo1 + " - " + event.pattern_hilo2);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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

    public void saveSnapshotAndFindMatchPattern(String instrumentName, String scale, int interval, double renkoSize,
            String patternRenko, String patternHiLo1, String patternHiLo2, long timestamp) {
        String instrumentKey = Helper.extractInstrumentKeyGroup(instrumentName);
        String key = instrumentKey + "|" + scale + "|" + interval + "|" + renkoSize + "|" + patternRenko + "|"
                + patternHiLo1 + "|" + patternHiLo2 + "|" + timestamp;
        if (lastSavedPeriod.putIfAbsent(key, patternRenko + "|" + patternHiLo1 + "|" + patternHiLo2) != null) {
            return;
        }
        String nowIso = Helper.isoNow();
        String date = Helper.isoDate(timestamp);
        if (!queue.offer(new SaveEvent(instrumentKey, scale, interval, renkoSize, patternRenko, patternHiLo1,
                patternHiLo2, timestamp, date, nowIso))) {
            // System.err.println("HistoryBufferPatternDbManager queue overflow! Sauvegarde
            // ignorée.");
            this.logger.log("HistoryBufferPatternDbManager queue overflow! Sauvegarde ignorée.");
        }
    }

    private PatternRenkoMatched saveToDbAndFindMatchPattern(SaveEvent event, Connection conn) {
        // Si l'upsert n'a rien retourné (déjà existant),
        // on re-sélectionne la clé avec COALESCE()
        String sql = """
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
                SELECT *
                FROM pattern_renko pr
                JOIN (
                    SELECT
                        COALESCE(u.instrument_key, ?) AS instrument_key,
                        COALESCE(u.tf_scale, ?) AS tf_scale,
                        COALESCE(u.tf_interval, ?) AS tf_interval,
                        COALESCE(u.renko_size, ?) AS renko_size,
                        COALESCE(u.pattern_renko, ?) AS pattern_renko,
                        COALESCE(u.pattern_hilo1, ?) AS pattern_hilo1,
                        COALESCE(u.pattern_hilo2, ?) AS pattern_hilo2
                    FROM upserted u
                    RIGHT JOIN (SELECT 1) ON TRUE
                    LIMIT 1
                ) i
                  ON pr.instrument_key = i.instrument_key
                 AND pr.tf_scale = i.tf_scale
                 AND pr.tf_interval = i.tf_interval
                 AND pr.renko_size = i.renko_size
                 AND pr.pattern_renko = i.pattern_renko
                 AND pr.pattern_hilo1 = i.pattern_hilo1
                 AND pr.pattern_hilo2 = i.pattern_hilo2
                ORDER BY pr.score DESC
                LIMIT 1;
                """;

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
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
            pst.setString(11, event.instrument_key);
            pst.setString(12, event.tf_scale);
            pst.setInt(13, event.tf_interval);
            pst.setDouble(14, event.renko_size);
            pst.setString(15, event.pattern_renko);
            pst.setString(16, event.pattern_hilo1);
            pst.setString(17, event.pattern_hilo2);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    PatternRenkoMatched pattern = new PatternRenkoMatched();
                    pattern.instrumentKey = rs.getString("instrument_key");
                    pattern.intervalScale = rs.getString("tf_scale");
                    pattern.intervalValue = rs.getInt("tf_interval");
                    pattern.renkoSize = rs.getDouble("renko_size");
                    pattern.bracket = new Bracket(rs.getInt("tp"), rs.getInt("sl"));
                    pattern.patternRenko = rs.getString("pattern_renko");
                    pattern.patternHilo1 = rs.getString("pattern_hilo1");
                    pattern.patternHilo2 = rs.getString("pattern_hilo2");
                    pattern.timestamp = event.timestamp;
                    pattern.date = event.date;
                    pattern.updated_at = event.updated_at;
                    pattern.direction = rs.getString("direction");
                    pattern.count = rs.getInt("count");
                    pattern.countTp = rs.getInt("count_tp");
                    pattern.countSl = rs.getInt("count_sl");
                    pattern.delta = rs.getInt("delta");
                    pattern.winrate = rs.getString("winrate");
                    pattern.profitFactor = rs.getDouble("profit_factor");
                    pattern.score = rs.getDouble("score");
                    pattern.lastDateTimeEntry = rs.getString("last_date_time_entry");
                    return pattern;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        purgeIfNeeded(conn);
        return null;
    }

    private void purgeIfNeeded(Connection conn) {
        long today = System.currentTimeMillis() / (24L * 60 * 60 * 1000);
        if (lastPurgeDay != today) {
            int ligneNumber = purgeOldEntries(conn);
            if (ligneNumber > 0) {
                this.logger.log("HistoryBufferPatternDbManager : Purge de " + ligneNumber
                        + " valeurs history_buffer_pattern (plus de 2 jours)");
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
            // if (n > 0) {
            // // System.out.println("HistoryBufferPatternDbManager : Purge de " + n
            // // + " valeurs history_buffer_pattern (plus de 2 jours)");
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void resetHistoryTable() {
        try (Connection conn = DatabaseManager.getConnection(); Statement st = conn.createStatement()) {
            st.execute("TRUNCATE TABLE history_buffer_pattern RESTART IDENTITY");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SaveEvent {
        final String instrument_key, tf_scale, pattern_renko, pattern_hilo1, pattern_hilo2, date, updated_at;
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
