package com.monitoralgo.core.dao;

import com.monitoralgo.algo.model.PatternDaoBatchReport;
import com.monitoralgo.core.database.DatabaseManager;
import com.monitoralgo.core.model.*;
import com.monitoralgo.core.util.logger.ProcessLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.*;

/**
 * DAO pour la table pattern_strategy (patterns détectés en backtest)
 *
 * Schéma (extrait) :
 * pattern_strategy(
 * id_pattern_strategy BIGSERIAL PK,
 * instrument_id INT NOT NULL,
 * timeframe_id INT NOT NULL,
 * renko_size_id INT NOT NULL,
 * pattern_id INT NOT NULL,
 * direction_id INT NOT NULL,
 * takeprofit_id INT NOT NULL,
 * stoploss_id INT NOT NULL,
 * last_timestamp_entry BIGINT,
 * last_date_time_entry TEXT,
 * average_bar INTEGER,
 * count INTEGER,
 * count_tp INTEGER,
 * count_sl INTEGER,
 * delta INTEGER,
 * winrate TEXT,
 * profit_factor DOUBLE PRECISION,
 * score DOUBLE PRECISION,
 * stats_updated_at TEXT, -- remplace updated_at
 * created_at TEXT,
 * UNIQUE (instrument_id, timeframe_id, renko_size_id, pattern_id, direction_id,
 * takeprofit_id, stoploss_id)
 * )
 */
public class PatternStrategyDao {

    // ---------------------------------------------------------
    // MAPPING ID-ONLY (mappe les colonnes primitives de ps.*)
    // ---------------------------------------------------------
    public static PatternStrategy mapRowIdOnly(ResultSet rs) throws SQLException {
        PatternStrategy p = new PatternStrategy();
        p.setIdPatternStrategy(rs.getLong("id_pattern_strategy"));
        p.setInstrumentId(rs.getInt("instrument_id"));
        p.setTimeframeId(rs.getInt("timeframe_id"));
        p.setRenkoSizeId(rs.getInt("renko_size_id"));
        p.setPatternId(rs.getInt("pattern_id"));
        p.setDirectionId(rs.getInt("direction_id"));
        p.setTakeprofitId(rs.getInt("takeprofit_id"));
        p.setStoplossId(rs.getInt("stoploss_id"));

        p.setLastTimestampEntry(rs.getObject("last_timestamp_entry", Long.class));
        p.setLastDateTimeEntry(rs.getString("last_date_time_entry"));
        p.setAverageBar(rs.getObject("average_bar", Integer.class));
        p.setCount(rs.getObject("count", Integer.class));
        p.setCountTp(rs.getObject("count_tp", Integer.class));
        p.setCountSl(rs.getObject("count_sl", Integer.class));
        p.setDelta(rs.getObject("delta", Integer.class));
        p.setWinrate(rs.getString("winrate"));
        p.setProfitFactor(rs.getObject("profit_factor", Double.class));
        p.setScore(rs.getObject("score", Double.class));

        p.setStatsUpdatedAt(rs.getString("stats_updated_at"));
        p.setCreatedAt(rs.getString("created_at"));
        return p;
    }

    // ----------------------------------------------------------------
    // MAPPING FULL OBJETS (joint les tables référentielles et mappe ps.*)
    // ----------------------------------------------------------------
    public static PatternStrategyFull mapRowWithJoins(ResultSet rs) throws SQLException {
        PatternStrategyFull p = new PatternStrategyFull();
        p.setId(rs.getLong("id_pattern_strategy"));

        // Objets référentiels
        p.setInstrument(InstrumentDao.mapRowFromJoin(rs));
        p.setTimeframe(TimeframeDao.mapRowFromJoin(rs));
        p.setRenkoSize(RenkoSizeDao.mapRowFromJoin(rs));
        p.setPattern(PatternDao.mapRowWithSuites(rs));
        p.setDirection(DirectionDao.mapRowFromJoin(rs));
        p.setTakeprofit(TakeProfitDao.mapRowFromJoin(rs));
        p.setStoploss(StopLossDao.mapRowFromJoin(rs));

        // Champs statistiques
        p.setLastTimestampEntry(rs.getObject("last_timestamp_entry", Long.class));
        p.setLastDateTimeEntry(rs.getString("last_date_time_entry"));
        p.setAverageBar(rs.getObject("average_bar", Integer.class));
        p.setCount(rs.getObject("count", Integer.class));
        p.setCountTp(rs.getObject("count_tp", Integer.class));
        p.setCountSl(rs.getObject("count_sl", Integer.class));
        p.setDelta(rs.getObject("delta", Integer.class));
        p.setWinrate(rs.getString("winrate"));
        p.setProfitFactor(rs.getObject("profit_factor", Double.class));
        p.setScore(rs.getObject("score", Double.class));

        p.setStatsUpdatedAt(rs.getString("stats_updated_at"));
        p.setCreatedAt(rs.getString("created_at"));
        return p;
    }

    // --------------------------
    // FIND ALL (ID only)
    // --------------------------
    public List<PatternStrategy> findAllIdOnly() throws SQLException {
        String sql = "SELECT * FROM pattern_strategy";
        List<PatternStrategy> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowIdOnly(rs));
            }
        }
        return list;
    }

    // --------------------------
    // FIND ALL (FULL objets)
    // --------------------------
    public List<PatternStrategyFull> findAllWithJoins() throws SQLException {
        String sql = """
                SELECT ps.*,
                       i.id_instrument, i.label AS instrument_label, i.key AS instrument_key, i.tick_size,
                       tf.id_timeframe, tf.scale AS tf_scale, tf.value AS tf_value,
                       r.id_renko_size, r.value_point AS renko_point, r.value_tick AS renko_tick,
                       p.id_pattern,
                       s1.id_suite AS renko_suite_id, s1.pattern AS renko_suite_pattern,
                       s2.id_suite AS hilo1_suite_id, s2.pattern AS hilo1_suite_pattern,
                       s3.id_suite AS hilo2_suite_id, s3.pattern AS hilo2_suite_pattern,
                       d.id_direction, d.label AS direction_label,
                       tp.id_takeprofit, tp.value_point AS tp_point, tp.value_tick AS tp_tick,
                       sl.id_stoploss, sl.value_point AS sl_point, sl.value_tick AS sl_tick
                FROM pattern_strategy ps
                JOIN instrument  i  ON ps.instrument_id  = i.id_instrument
                JOIN timeframe   tf ON ps.timeframe_id   = tf.id_timeframe
                JOIN renko_size  r  ON ps.renko_size_id  = r.id_renko_size
                JOIN pattern     p  ON ps.pattern_id     = p.id_pattern
                JOIN suite       s1 ON p.suite_renko_id  = s1.id_suite
                JOIN suite       s2 ON p.suite_hilo1_id  = s2.id_suite
                JOIN suite       s3 ON p.suite_hilo2_id  = s3.id_suite
                JOIN direction   d  ON ps.direction_id   = d.id_direction
                JOIN takeprofit  tp ON ps.takeprofit_id  = tp.id_takeprofit
                JOIN stoploss    sl ON ps.stoploss_id    = sl.id_stoploss
                """;
        List<PatternStrategyFull> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowWithJoins(rs));
            }
        }
        return list;
    }

    // -------------------------------------------------------------------
    // INSERT BATCH (clé logique + UPSERT) — gère average_bar + stats dates
    // -------------------------------------------------------------------
    public void insertAllBatch(List<PatternStrategy> batch) throws SQLException {
        insertAllBatch(batch, null, null, null);
    }

    /** Surcharge avec logger + contexte pour logs de comptage avant/après. */
    public void insertAllBatch(List<PatternStrategy> batch,
            ProcessLogger LOGGER, String STUDY_ID, String INSTANCE_NAME) throws SQLException {
        if (batch == null || batch.isEmpty())
            return;

        String sql = """
                INSERT INTO pattern_strategy
                    (instrument_id, timeframe_id, renko_size_id, pattern_id, direction_id, takeprofit_id, stoploss_id,
                     last_timestamp_entry, last_date_time_entry, average_bar,
                     count, count_tp, count_sl, delta, winrate, profit_factor, score,
                     stats_updated_at, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                        TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS'),
                        TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS'))
                ON CONFLICT (instrument_id, timeframe_id, renko_size_id, pattern_id, direction_id, takeprofit_id, stoploss_id)
                DO UPDATE SET
                    last_timestamp_entry = EXCLUDED.last_timestamp_entry,
                    last_date_time_entry = EXCLUDED.last_date_time_entry,
                    average_bar         = EXCLUDED.average_bar,
                    count               = EXCLUDED.count,
                    count_tp            = EXCLUDED.count_tp,
                    count_sl            = EXCLUDED.count_sl,
                    delta               = EXCLUDED.delta,
                    winrate             = EXCLUDED.winrate,
                    profit_factor       = EXCLUDED.profit_factor,
                    score               = EXCLUDED.score,
                    stats_updated_at    = TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS')
                """;

        long before = 0, after = 0;
        if (LOGGER != null) {
            before = countAllSafe();
            final String instanceId = String.valueOf(System.identityHashCode(this));
            LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "[DAO] Pattern Strategy All",
                    "Début Insert All Batch");
            LOGGER.log(String.format("  ↳ Taille batch : %,d", batch.size()));
            LOGGER.log(String.format("  ↳ Count BEFORE : %,d (pattern_strategy)", before));
        }

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (PatternStrategy p : batch) {
                // Clés (1..7)
                ps.setInt(1, p.getInstrumentId());
                ps.setInt(2, p.getTimeframeId());
                ps.setInt(3, p.getRenkoSizeId());
                ps.setInt(4, p.getPatternId());
                ps.setInt(5, p.getDirectionId());
                ps.setInt(6, p.getTakeprofitId());
                ps.setInt(7, p.getStoplossId());

                // Stats
                ps.setObject(8, p.getLastTimestampEntry(), Types.BIGINT);
                ps.setString(9, p.getLastDateTimeEntry());
                ps.setObject(10, p.getAverageBar(), Types.INTEGER);
                ps.setObject(11, p.getCount(), Types.INTEGER);
                ps.setObject(12, p.getCountTp(), Types.INTEGER);
                ps.setObject(13, p.getCountSl(), Types.INTEGER);
                ps.setObject(14, p.getDelta(), Types.INTEGER);
                ps.setString(15, p.getWinrate());
                ps.setObject(16, p.getProfitFactor(), Types.DOUBLE);
                ps.setObject(17, p.getScore(), Types.DOUBLE);

                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        }

        if (LOGGER != null) {
            after = countAllSafe();
            long insertedNew = Math.max(0, after - before);
            long attempted = batch.size();
            long updatedEst = Math.max(0, attempted - insertedNew);
            LOGGER.log(String.format("  ↳ Count  AFTER : %,d (pattern_strategy)", after));
            LOGGER.log(String.format("  ↳ Delta (new) : +%,d ; Updates (est.) : %,d", insertedNew, updatedEst));
            LOGGER.setContext(null, STUDY_ID, INSTANCE_NAME, "[DAO] Pattern Strategy All", "Fin Insert All Batch");
        }
    }

    // ------------------------------------------------------
    // Recherche par clé logique (UNIQUE) de pattern_strategy
    // ------------------------------------------------------
    public PatternStrategy findByLogicalKey(int instrumentId, int timeframeId, int renkoSizeId, int patternId,
            int directionId, int takeprofitId, int stoplossId) throws SQLException {
        String sql = """
                SELECT * FROM pattern_strategy
                WHERE instrument_id = ? AND timeframe_id = ? AND renko_size_id = ? AND pattern_id = ?
                  AND direction_id = ? AND takeprofit_id = ? AND stoploss_id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instrumentId);
            ps.setInt(2, timeframeId);
            ps.setInt(3, renkoSizeId);
            ps.setInt(4, patternId);
            ps.setInt(5, directionId);
            ps.setInt(6, takeprofitId);
            ps.setInt(7, stoplossId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowIdOnly(rs);
                }
            }
        }
        return null;
    }

    // ---------------------------------------------------------------------------------
    // INSERT FULL BATCH (avec joins) — gère average_bar +
    // stats_updated_at/created_at
    // ---------------------------------------------------------------------------------
    public void insertFullBatch(List<PatternStrategyFull> batch) throws SQLException {
        insertFullBatch(batch, 5000, null, null, null);
    }

    public void insertFullBatch(List<PatternStrategyFull> batch,
            int CHUNK_SIZE,
            ProcessLogger LOGGER, String STUDY_ID, String INSTANCE_NAME) throws SQLException {
        if (batch == null || batch.isEmpty())
            return;

        String sql = """
                INSERT INTO pattern_strategy (
                    instrument_id, timeframe_id, renko_size_id, pattern_id, direction_id, takeprofit_id, stoploss_id,
                    last_timestamp_entry, last_date_time_entry, average_bar,
                    count, count_tp, count_sl, delta, winrate, profit_factor, score,
                    stats_updated_at, created_at
                )
                SELECT
                    i.id_instrument, tf.id_timeframe, r.id_renko_size, p.id_pattern, d.id_direction, tp.id_takeprofit, sl.id_stoploss,
                    ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS'),
                    TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS')
                FROM instrument i
                JOIN timeframe   tf ON tf.id_timeframe = ?
                JOIN renko_size  r  ON r.id_renko_size = ?
                JOIN direction   d  ON d.id_direction = ?
                JOIN takeprofit  tp ON tp.id_takeprofit = ?
                JOIN stoploss    sl ON sl.id_stoploss = ?
                JOIN pattern     p  ON p.id_pattern = ?
                WHERE i.id_instrument = ? AND tf.id_timeframe = ? AND r.id_renko_size = ? AND d.id_direction = ?
                  AND tp.id_takeprofit = ? AND sl.id_stoploss = ? AND p.id_pattern = ?
                ON CONFLICT (
                    instrument_id, timeframe_id, renko_size_id, pattern_id, direction_id, takeprofit_id, stoploss_id
                )
                DO UPDATE SET
                    last_timestamp_entry = EXCLUDED.last_timestamp_entry,
                    last_date_time_entry = EXCLUDED.last_date_time_entry,
                    average_bar         = EXCLUDED.average_bar,
                    count               = EXCLUDED.count,
                    count_tp            = EXCLUDED.count_tp,
                    count_sl            = EXCLUDED.count_sl,
                    delta               = EXCLUDED.delta,
                    winrate             = EXCLUDED.winrate,
                    profit_factor       = EXCLUDED.profit_factor,
                    score               = EXCLUDED.score,
                    stats_updated_at    = TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS')
                """;

        long before = 0, after = 0;
        if (LOGGER != null) {
            before = countAllSafe();
            final String instanceId = String.valueOf(System.identityHashCode(this));
            LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "[DAO] Pattern Strategy Full",
                    "Début Insert Full Batch");
            LOGGER.log(String.format("  ↳ Taille batch : %,d (chunk: %,d)", batch.size(), CHUNK_SIZE));
            LOGGER.log(String.format("  ↳ Count BEFORE : %,d (pattern_strategy)", before));
        }

        int count = 0;
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (PatternStrategyFull p : batch) {
                    // Stats placeholders (1..10)
                    ps.setObject(1, p.getLastTimestampEntry(), Types.BIGINT);
                    ps.setString(2, p.getLastDateTimeEntry());
                    ps.setObject(3, p.getAverageBar(), Types.INTEGER);
                    ps.setObject(4, p.getCount(), Types.INTEGER);
                    ps.setObject(5, p.getCountTp(), Types.INTEGER);
                    ps.setObject(6, p.getCountSl(), Types.INTEGER);
                    ps.setObject(7, p.getDelta(), Types.INTEGER);
                    ps.setString(8, p.getWinrate());
                    ps.setObject(9, p.getProfitFactor(), Types.DOUBLE);
                    ps.setObject(10, p.getScore(), Types.DOUBLE);

                    // JOIN ids (11..16)
                    int idTimeframe = p.getTimeframe().getId();
                    int idRenko = p.getRenkoSize().getId();
                    int idDir = p.getDirection().getId();
                    int idTP = p.getTakeprofit().getId();
                    int idSL = p.getStoploss().getId();
                    int idPattern = p.getPattern().getId();

                    ps.setInt(11, idTimeframe);
                    ps.setInt(12, idRenko);
                    ps.setInt(13, idDir);
                    ps.setInt(14, idTP);
                    ps.setInt(15, idSL);
                    ps.setInt(16, idPattern);

                    // WHERE ids (17..23)
                    int idInstrument = p.getInstrument().getId();
                    ps.setInt(17, idInstrument);
                    ps.setInt(18, idTimeframe);
                    ps.setInt(19, idRenko);
                    ps.setInt(20, idDir);
                    ps.setInt(21, idTP);
                    ps.setInt(22, idSL);
                    ps.setInt(23, idPattern);

                    ps.addBatch();
                    count++;

                    if (count % CHUNK_SIZE == 0) {
                        ps.executeBatch();
                        conn.commit();
                        if (LOGGER != null) {
                            LOGGER.log(String.format("  ✔ Flushed %,d/%d rows", count, batch.size()));
                        }
                    }
                }
                ps.executeBatch();
                conn.commit();
            }
        } catch (SQLException e) {
            if (LOGGER != null) {
                LOGGER.log("[ERROR] Batch insert échoué: " + e.getMessage());
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                LOGGER.log(sw.toString());
            }
            throw e;
        }

        if (LOGGER != null) {
            after = countAllSafe();
            long insertedNew = Math.max(0, after - before);
            long attempted = batch.size();
            long updatedEst = Math.max(0, attempted - insertedNew);
            LOGGER.log(String.format("  ↳ Count  AFTER : %,d (pattern_strategy)", after));
            LOGGER.log(String.format("  ↳ Delta (new) : +%,d ; Updates (est.) : %,d", insertedNew, updatedEst));
            LOGGER.setContext(null, STUDY_ID, INSTANCE_NAME, "[DAO] Pattern Strategy Full", "Fin Insert Full Batch");
        }
    }

    /**
     * Insert en batch partiel les identifiants des PatternStrategyFull SANS stats.
     * Conflit => DO NOTHING (pas de reset des stats ici).
     *
     * Retourne le nombre d'éléments « tentés » (rows ajoutées au batch),
     * et logge countAll() BEFORE/AFTER pour estimer new vs update.
     */
    public int insertPartialBatch(List<PatternStrategyFull> batch, int BATCH_SQL_SIZE, ProcessLogger LOGGER,
            String STUDY_ID, String INSTANCE_NAME) throws SQLException {
        PatternDaoBatchReport rep = insertPartialBatchWithReport(batch, BATCH_SQL_SIZE, LOGGER, STUDY_ID,
                INSTANCE_NAME);
        // pour compatibilité ascendante, on renvoie totalRowsAffected ou attempted ?
        // Historiquement, tu additionnais ce retour à "totalPatternsInserted".
        // Comme DO NOTHING => rowsAffected ≈ new inserts, on renvoie ce nombre.
        return (int) rep.totalRowsAffected;
    }

    /**
     * Nouvelle méthode demandée : exécute l'insert partial en batch et
     * retourne un PatternDaoBatchReport (rowsAffected, batchCount, autoCommit).
     *
     * rowsAffected :
     * - On essaye de sommer les retours de executeBatch().
     * - Si le driver renvoie SUCCESS_NO_INFO (-2), on bascule sur
     * une estimation fiable via countAll() BEFORE/AFTER.
     */
    public PatternDaoBatchReport insertPartialBatchWithReport(List<PatternStrategyFull> batch,
            int BATCH_SQL_SIZE,
            ProcessLogger LOGGER,
            String STUDY_ID,
            String INSTANCE_NAME) throws SQLException {
        if (batch == null || batch.isEmpty()) {
            return new PatternDaoBatchReport(0, 0, true);
        }

        final String instanceId = String.valueOf(System.identityHashCode(this));
        LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "[DAO] Pattern Strategy Partial",
                "Début Insert Partial Batch");

        final String cleLogique = buildCleLogique(batch.get(0)); // préfixe lisible dans les logs

        String sql = """
                INSERT INTO pattern_strategy (
                    instrument_id, timeframe_id, renko_size_id, pattern_id, direction_id, takeprofit_id, stoploss_id,
                    stats_updated_at, created_at
                )
                SELECT
                    i.id_instrument, tf.id_timeframe, r.id_renko_size, p.id_pattern, d.id_direction, tp.id_takeprofit, sl.id_stoploss,
                    TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS'),
                    TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS')
                FROM instrument i
                JOIN timeframe tf ON tf.id_timeframe = ?
                JOIN renko_size r ON r.id_renko_size = ?
                JOIN direction d ON d.id_direction = ?
                JOIN takeprofit tp ON tp.id_takeprofit = ?
                JOIN stoploss sl ON sl.id_stoploss = ?
                JOIN pattern p ON p.id_pattern = ?
                WHERE i.id_instrument = ? AND tf.id_timeframe = ? AND r.id_renko_size = ? AND d.id_direction = ?
                  AND tp.id_takeprofit = ? AND sl.id_stoploss = ? AND p.id_pattern = ?
                ON CONFLICT (
                    instrument_id, timeframe_id, renko_size_id, pattern_id, direction_id, takeprofit_id, stoploss_id
                )
                DO NOTHING
                """;

        final int CHUNK_SIZE = BATCH_SQL_SIZE;
        int batchCount = 0;
        long sumFromExecuteBatch = 0;
        boolean sawSuccessNoInfo = false;

        long before = countAllSafe();
        LOGGER.log(String.format("%s  ↳ Taille batch : %,d (chunk: %,d)", cleLogique, batch.size(), CHUNK_SIZE));
        LOGGER.log(String.format("%s  ↳ Count BEFORE : %,d (pattern_strategy)", cleLogique, before));

        boolean usedAutoCommit = true;

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            boolean oldAutoCommit = conn.getAutoCommit();
            usedAutoCommit = oldAutoCommit == Boolean.TRUE;
            try {
                conn.setAutoCommit(false);

                // optimisation latence disque pendant la txn
                try (Statement st = conn.createStatement()) {
                    st.execute("SET LOCAL synchronous_commit = OFF");
                }

                int filled = 0;
                for (PatternStrategyFull p : batch) {
                    int idTimeframe = p.getTimeframe().getId();
                    int idRenko = p.getRenkoSize().getId();
                    int idDir = p.getDirection().getId();
                    int idTP = p.getTakeprofit().getId();
                    int idSL = p.getStoploss().getId();
                    int idPattern = p.getPattern().getId();
                    int idInstrument = p.getInstrument().getId();

                    ps.setInt(1, idTimeframe);
                    ps.setInt(2, idRenko);
                    ps.setInt(3, idDir);
                    ps.setInt(4, idTP);
                    ps.setInt(5, idSL);
                    ps.setInt(6, idPattern);
                    ps.setInt(7, idInstrument);
                    ps.setInt(8, idTimeframe);
                    ps.setInt(9, idRenko);
                    ps.setInt(10, idDir);
                    ps.setInt(11, idTP);
                    ps.setInt(12, idSL);
                    ps.setInt(13, idPattern);

                    ps.addBatch();
                    filled++;

                    if (filled % CHUNK_SIZE == 0) {
                        int[] ret = ps.executeBatch();
                        conn.commit();
                        batchCount++;
                        long added = sumCounts(ret);
                        if (added < 0)
                            sawSuccessNoInfo = true; // -2 rencontré
                        sumFromExecuteBatch += Math.max(0, added);
                        LOGGER.log(
                                String.format("%s  ✔ Flushed %,d/%d rows (partial)", cleLogique, filled, batch.size()));
                    }
                }
                // reliquat
                int[] ret = ps.executeBatch();
                conn.commit();
                batchCount++;
                long added = sumCounts(ret);
                if (added < 0)
                    sawSuccessNoInfo = true;
                sumFromExecuteBatch += Math.max(0, added);

            } catch (SQLException e) {
                conn.rollback();
                LOGGER.log("[ERROR] Batch insert partial échoué: " + e.getMessage());
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                LOGGER.log(sw.toString());
                throw e;
            } finally {
                if (!conn.isClosed()) {
                    conn.setAutoCommit(oldAutoCommit);
                }
            }
        }

        long after = countAllSafe();
        long insertedByDelta = Math.max(0, after - before);
        long totalRowsAffected = sawSuccessNoInfo ? insertedByDelta : sumFromExecuteBatch;

        // Logs finaux
        LOGGER.log(String.format("%s  ↳ Count  AFTER : %,d (pattern_strategy)", cleLogique, after));
        LOGGER.log(String.format("%s  ↳ Delta (new) : +%,d ; RowsAffected (driver) : %,d ; Batches : %d",
                cleLogique, insertedByDelta, sumFromExecuteBatch, batchCount));
        LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "[DAO] Pattern Strategy Partial", "Fin Insert Partial Batch");

        return new PatternDaoBatchReport(totalRowsAffected, batchCount, usedAutoCommit);
    }

    // -----------------------------------------------------------------
    // Util: clé logique lisible pour PatternStrategyFull (pour les logs)
    // -----------------------------------------------------------------
    private String buildCleLogique(PatternStrategyFull p) {
        return String.format("[%s|%s#%s|%s|%s|%s|%s|%s|%s|%s]",
                p.getInstrument().getKey(),
                p.getTimeframe().getScale(),
                p.getTimeframe().getValue(),
                p.getRenkoSize().getValuePoint(),
                p.getPattern().getSuiteRenko().getPattern(),
                p.getPattern().getSuiteHilo1().getPattern(),
                p.getPattern().getSuiteHilo2().getPattern(),
                p.getDirection().getLabel(),
                p.getTakeprofit().getValuePoint(),
                p.getStoploss().getValuePoint());
    }

    // -----------------------------------------
    // Compte brut des lignes de pattern_strategy
    // -----------------------------------------
    public long countAll() throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM pattern_strategy");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    /** Version "safe" de countAll() pour usage dans les logs (gère exceptions). */
    private long countAllSafe() {
        try {
            return countAll();
        } catch (Exception ignore) {
            return -1L;
        }
    }

    /**
     * Additionne proprement les retours de executeBatch().
     * Retourne -1 si au moins une valeur == Statement.SUCCESS_NO_INFO (-2),
     * ce qui signifie que le driver ne sait pas donner la granularité.
     */
    private long sumCounts(int[] counts) {
        if (counts == null)
            return 0L;
        long sum = 0L;
        for (int c : counts) {
            if (c == Statement.SUCCESS_NO_INFO) {
                return -1L; // on signalera à l'appelant d'utiliser le delta table
            }
            if (c == Statement.EXECUTE_FAILED) {
                // on ne devrait pas en voir car on catch + rollback au-dessus, mais au cas où
                continue;
            }
            if (c > 0)
                sum += c;
        }
        return sum;
    }
}
