package com.monitoralgo.core.dao;

import com.monitoralgo.algo.model.TradeDaoBatchReport;
import com.monitoralgo.core.model.*;
import com.monitoralgo.core.util.logger.ProcessLogger;
import com.monitoralgo.core.database.DatabaseManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table trade_strategy_history (trades simulés)
 *
 * Schéma (rappel) :
 * CREATE TABLE IF NOT EXISTS trade_strategy_history (
 * id_trade BIGSERIAL PRIMARY KEY,
 * pattern_strategy_id BIGINT NOT NULL REFERENCES
 * pattern_strategy(id_pattern_strategy),
 * timestamp_entry BIGINT NOT NULL,
 * date_time_entry TEXT NOT NULL,
 * entry_price DOUBLE PRECISION NOT NULL,
 * timestamp_exit BIGINT,
 * date_time_exit TEXT,
 * exit_price DOUBLE PRECISION,
 * tp_price DOUBLE PRECISION,
 * sl_price DOUBLE PRECISION,
 * count_bar INTEGER,
 * result TEXT,
 * created_at TEXT,
 * UNIQUE(pattern_strategy_id, timestamp_entry, entry_price)
 * );
 */
public class TradeStrategyHistoryDao {

    // =========================================================
    // INSERT SIMPLE
    // =========================================================

    /**
     * Insère un trade simulé directement (modèle simple).
     * Points d'attention :
     * - entry_price est NOT NULL (on l'exige côté Java).
     * - timestamp_exit, date_time_exit, exit_price, count_bar sont NULLables →
     * setObject(...).
     * - created_at est géré côté SQL via NOW().
     * - clé d’unicité : (pattern_strategy_id, timestamp_entry, entry_price).
     */
    public void insert(TradeStrategyHistory trade) throws SQLException {
        final String sql = """
                INSERT INTO trade_strategy_history (
                    pattern_strategy_id, timestamp_entry, date_time_entry, entry_price,
                    timestamp_exit, date_time_exit, exit_price, tp_price, sl_price,
                    count_bar, result, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS'))
                ON CONFLICT (pattern_strategy_id, timestamp_entry, entry_price) DO NOTHING
                """;

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // --- NOT NULL ---
            ps.setLong(1, trade.getPatternStrategyId());
            ps.setLong(2, trade.getTimestampEntry());
            ps.setString(3, trade.getDateTimeEntry());
            if (trade.getEntryPrice() == null) {
                throw new IllegalArgumentException("entry_price must not be null");
            }
            ps.setDouble(4, trade.getEntryPrice());

            // --- NULLABLES ---
            ps.setObject(5, trade.getTimestampExit(), Types.BIGINT);
            ps.setObject(6, emptyToNull(trade.getDateTimeExit()), Types.VARCHAR);
            ps.setObject(7, trade.getExitPrice(), Types.DOUBLE);
            ps.setObject(8, trade.getTpPrice(), Types.DOUBLE);
            ps.setObject(9, trade.getSlPrice(), Types.DOUBLE);
            ps.setObject(10, trade.getCountBar(), Types.INTEGER);

            // result (texte libre : "TP", "SL", "NoExit", ...)
            ps.setString(11, trade.getResult());

            ps.executeUpdate();

            // Récupère l'id généré si insertion
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    trade.setIdTrade(keys.getLong(1));
                }
            }
        }
    }

    // =========================================================
    // MAPPING (SIMPLE / FULL)
    // =========================================================

    /** Mapping ResultSet → modèle simple TradeStrategyHistory */
    private TradeStrategyHistory mapRow(ResultSet rs) throws SQLException {
        TradeStrategyHistory t = new TradeStrategyHistory();
        t.setIdTrade(rs.getLong("id_trade"));
        t.setPatternStrategyId(rs.getLong("pattern_strategy_id"));
        t.setTimestampEntry(rs.getLong("timestamp_entry"));
        t.setDateTimeEntry(rs.getString("date_time_entry"));
        t.setEntryPrice(rs.getDouble("entry_price")); // NOT NULL
        t.setTimestampExit(getNullableLong(rs, "timestamp_exit"));
        t.setDateTimeExit(rs.getString("date_time_exit"));
        t.setExitPrice(getNullableDouble(rs, "exit_price"));
        t.setTpPrice(getNullableDouble(rs, "tp_price"));
        t.setSlPrice(getNullableDouble(rs, "sl_price"));
        t.setCountBar(getNullableInt(rs, "count_bar"));
        t.setResult(rs.getString("result"));
        t.setCreatedAt(rs.getString("created_at"));
        return t;
    }

    /** Mapping ResultSet → TradeStrategyHistoryFull avec tous les modèles liés */
    private TradeStrategyHistoryFull mapRowFull(ResultSet rs) throws SQLException {
        Instrument instrument = new Instrument(
                rs.getInt("id_instrument"),
                rs.getString("instrument_label"),
                rs.getString("instrument_key"),
                rs.getDouble("tick_size"));
        Timeframe timeframe = new Timeframe(
                rs.getInt("id_timeframe"),
                rs.getString("timeframe_scale"),
                rs.getInt("timeframe_value"));
        RenkoSize renkoSize = new RenkoSize(
                rs.getInt("id_renko_size"),
                rs.getDouble("renko_value_point"),
                rs.getInt("renko_value_tick"));
        Direction direction = new Direction(
                rs.getInt("id_direction"),
                rs.getString("direction_label"));
        TakeProfit takeprofit = new TakeProfit(
                rs.getInt("id_takeprofit"),
                rs.getDouble("takeprofit_value_point"),
                rs.getInt("takeprofit_value_tick"));
        StopLoss stoploss = new StopLoss(
                rs.getInt("id_stoploss"),
                rs.getDouble("stoploss_value_point"),
                rs.getInt("stoploss_value_tick"));
        Suite suiteRenko = new Suite(rs.getInt("renko_suite_id"), rs.getString("renko_pattern"));
        Suite suiteHilo1 = new Suite(rs.getInt("hilo1_suite_id"), rs.getString("hilo1_pattern"));
        Suite suiteHilo2 = new Suite(rs.getInt("hilo2_suite_id"), rs.getString("hilo2_pattern"));
        Pattern pattern = new Pattern(rs.getInt("id_pattern"), suiteRenko, suiteHilo1, suiteHilo2);

        // Le constructeur Full doit accepter countBar avant createdAt (cf. ton modèle à
        // jour)
        return new TradeStrategyHistoryFull(
                rs.getLong("id_trade"),
                instrument,
                timeframe,
                renkoSize,
                pattern,
                direction,
                takeprofit,
                stoploss,
                rs.getLong("timestamp_entry"),
                rs.getString("date_time_entry"),
                getNullableDouble(rs, "entry_price"),
                rs.getLong("timestamp_exit"),
                rs.getString("date_time_exit"),
                getNullableDouble(rs, "exit_price"),
                getNullableDouble(rs, "tp_price"),
                getNullableDouble(rs, "sl_price"),
                getNullableInt(rs, "count_bar"),
                rs.getString("result"),
                rs.getString("created_at"));
    }

    // =========================================================
    // SELECTS
    // =========================================================

    /** Récupère tous les trades (simple) */
    public List<TradeStrategyHistory> findAll() throws SQLException {
        final String sql = "SELECT * FROM trade_strategy_history ORDER BY id_trade ASC";
        List<TradeStrategyHistory> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Récupère un trade par id (simple) */
    public TradeStrategyHistory findById(long id) throws SQLException {
        final String sql = "SELECT * FROM trade_strategy_history WHERE id_trade = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        }
        return null;
    }

    /** Récupère tous les trades (FULL) avec jointures */
    public List<TradeStrategyHistoryFull> findAllFull() throws SQLException {
        final String sql = """
                SELECT t.id_trade, t.pattern_strategy_id, t.timestamp_entry, t.date_time_entry, t.entry_price,
                       t.timestamp_exit, t.date_time_exit, t.exit_price, t.tp_price, t.sl_price,
                       t.count_bar, t.result, t.created_at,
                       i.id_instrument, i.label AS instrument_label, i.key AS instrument_key, i.tick_size,
                       tf.id_timeframe, tf.scale AS timeframe_scale, tf.value AS timeframe_value,
                       rs.id_renko_size, rs.value_point AS renko_value_point, rs.value_tick AS renko_value_tick,
                       d.id_direction, d.label AS direction_label,
                       tp.id_takeprofit, tp.value_point AS takeprofit_value_point, tp.value_tick AS takeprofit_value_tick,
                       sl.id_stoploss, sl.value_point AS stoploss_value_point, sl.value_tick AS stoploss_value_tick,
                       p.id_pattern,
                       sr.id_suite AS renko_suite_id, sr.pattern AS renko_pattern,
                       sh1.id_suite AS hilo1_suite_id, sh1.pattern AS hilo1_pattern,
                       sh2.id_suite AS hilo2_suite_id, sh2.pattern AS hilo2_pattern
                FROM trade_strategy_history t
                INNER JOIN pattern_strategy ps ON t.pattern_strategy_id = ps.id_pattern_strategy
                INNER JOIN instrument i ON ps.instrument_id = i.id_instrument
                INNER JOIN timeframe tf ON ps.timeframe_id = tf.id_timeframe
                INNER JOIN renko_size rs ON ps.renko_size_id = rs.id_renko_size
                INNER JOIN direction d ON ps.direction_id = d.id_direction
                INNER JOIN takeprofit tp ON ps.takeprofit_id = tp.id_takeprofit
                INNER JOIN stoploss sl ON ps.stoploss_id = sl.id_stoploss
                INNER JOIN pattern p ON ps.pattern_id = p.id_pattern
                INNER JOIN suite sr ON p.suite_renko_id = sr.id_suite
                INNER JOIN suite sh1 ON p.suite_hilo1_id = sh1.id_suite
                INNER JOIN suite sh2 ON p.suite_hilo2_id = sh2.id_suite
                ORDER BY t.id_trade ASC
                """;
        List<TradeStrategyHistoryFull> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowFull(rs));
            }
        }
        return list;
    }

    // =========================================================
    // INSERT BATCH (FULL)
    // =========================================================

    /**
     * Nouvelle API : insert batch FULL + rapport détaillé.
     * Retourne un TradeDaoBatchReport (rowsAffected, batchCount, autoCommit).
     *
     * Notes :
     * - Conflits résolus par DO NOTHING → rowsAffected ~ « nouveaux » inserts.
     * - Si le driver renvoie SUCCESS_NO_INFO dans executeBatch(),
     * on bascule sur une estimation fiable via countAll() BEFORE/AFTER.
     * - Optimisations : transaction + SET LOCAL synchronous_commit=OFF.
     */
    public TradeDaoBatchReport insertFullBatchWithReport(List<TradeStrategyHistoryFull> batch, int BATCH_SQL_SIZE,
            ProcessLogger LOGGER, String STUDY_ID, String INSTANCE_NAME)
            throws SQLException {
        if (batch == null || batch.isEmpty()) {
            return new TradeDaoBatchReport(0, 0, true);
        }

        final String cleLogique = buildCleLogique(batch.get(0));
        final String instanceId = String.valueOf(System.identityHashCode(this));
        LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "[DAO] Trade Strategy History Full",
                "Début Insert Full Batch");

        final String sql = """
                INSERT INTO trade_strategy_history (
                    pattern_strategy_id, timestamp_entry, date_time_entry, entry_price,
                    timestamp_exit, date_time_exit, exit_price, tp_price, sl_price,
                    count_bar, result, created_at
                )
                SELECT
                    ps.id_pattern_strategy, ?,?,?,?,?,?,?,?,?,?,TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS')
                FROM pattern_strategy ps
                WHERE ps.instrument_id = ? AND ps.timeframe_id = ? AND ps.renko_size_id = ?
                  AND ps.pattern_id = ? AND ps.direction_id = ?
                  AND ps.takeprofit_id = ? AND ps.stoploss_id = ?
                ON CONFLICT (pattern_strategy_id, timestamp_entry, entry_price) DO NOTHING
                """;

        final int CHUNK_SIZE = BATCH_SQL_SIZE;

        // Comptages pour logs/rapport
        long before = countAllSafe();
        LOGGER.log(String.format("%s  ↳ Taille batch : %,d (chunk: %,d)", cleLogique, batch.size(), CHUNK_SIZE));
        LOGGER.log(String.format("%s  ↳ Count BEFORE : %,d (trade_strategy_history)", cleLogique, before));

        boolean usedAutoCommit = true;
        int batchCount = 0;
        long sumFromExecuteBatch = 0;
        boolean sawSuccessNoInfo = false;

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            boolean oldAuto = conn.getAutoCommit();
            usedAutoCommit = oldAuto == Boolean.TRUE;
            conn.setAutoCommit(false);

            // Optimisation I/O pour la txn
            try (Statement st = conn.createStatement()) {
                st.execute("SET LOCAL synchronous_commit = OFF");
            }

            int filled = 0;
            for (TradeStrategyHistoryFull t : batch) {
                // Champs NOT NULL
                ps.setLong(1, t.getTimestampEntry());
                ps.setString(2, t.getDateTimeEntry());
                if (t.getEntryPrice() == null) {
                    throw new IllegalArgumentException("entry_price must not be null in batch");
                }
                ps.setDouble(3, t.getEntryPrice());

                // Champs NULLables
                ps.setObject(4, t.getTimestampExit(), Types.BIGINT);
                ps.setObject(5, emptyToNull(t.getDateTimeExit()), Types.VARCHAR);
                ps.setObject(6, t.getExitPrice(), Types.DOUBLE);
                ps.setObject(7, t.getTpPrice(), Types.DOUBLE);
                ps.setObject(8, t.getSlPrice(), Types.DOUBLE);
                ps.setObject(9, t.getCountBar(), Types.INTEGER);

                // result
                ps.setString(10, t.getResult());

                // WHERE (clé pattern_strategy)
                ps.setInt(11, t.getInstrument().getId());
                ps.setInt(12, t.getTimeframe().getId());
                ps.setInt(13, t.getRenkoSize().getId());
                ps.setInt(14, t.getPattern().getId());
                ps.setInt(15, t.getDirection().getId());
                ps.setInt(16, t.getTakeprofit().getId());
                ps.setInt(17, t.getStoploss().getId());

                ps.addBatch();
                filled++;

                if (filled % CHUNK_SIZE == 0) {
                    int[] ret = ps.executeBatch();
                    conn.commit();
                    batchCount++;
                    long added = sumCounts(ret);
                    if (added < 0)
                        sawSuccessNoInfo = true; // SUCCESS_NO_INFO rencontré
                    sumFromExecuteBatch += Math.max(0, added);
                    LOGGER.log(String.format("%s  ✔ Flushed %,d/%d trades", cleLogique, filled, batch.size()));
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

            conn.setAutoCommit(oldAuto);

        } catch (SQLException e) {
            LOGGER.log("[ERROR] Batch insert échoué: " + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            LOGGER.log(sw.toString());
            throw e;
        } finally {
            LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "[DAO] Trade Strategy History Full",
                    "Fin Insert Full Batch");
        }

        long after = countAllSafe();
        long insertedByDelta = Math.max(0, after - before);
        long totalRowsAffected = sawSuccessNoInfo ? insertedByDelta : sumFromExecuteBatch;

        // Logs finaux explicites
        LOGGER.log(String.format("%s  ↳ Count  AFTER : %,d (trade_strategy_history)", cleLogique, after));
        LOGGER.log(String.format("%s  ↳ Delta (new) : +%,d ; RowsAffected (driver) : %,d ; Batches : %d",
                cleLogique, insertedByDelta, sumFromExecuteBatch, batchCount));

        return new TradeDaoBatchReport(totalRowsAffected, batchCount, usedAutoCommit);
    }

    /**
     * API historique conservée : renvoie un int.
     * Appelle la nouvelle méthode et retourne totalRowsAffected (≈ nb d’inserts).
     */
    public int insertFullBatch(List<TradeStrategyHistoryFull> batch, int BATCH_SQL_SIZE,
            ProcessLogger LOGGER, String STUDY_ID, String INSTANCE_NAME)
            throws SQLException {
        TradeDaoBatchReport rep = insertFullBatchWithReport(batch, BATCH_SQL_SIZE, LOGGER, STUDY_ID, INSTANCE_NAME);
        return (int) rep.totalRowsAffected;
    }

    // =========================================================
    // DIVERS
    // =========================================================

    /** Construit une clé lisible pour logs (pas utilisée par la BDD) */
    private String buildCleLogique(TradeStrategyHistoryFull t) {
        return String.format("[%s|%s#%s|%s|%s|%s|%s|%s|%s|%s]",
                t.getInstrument().getKey(),
                t.getTimeframe().getScale(),
                t.getTimeframe().getValue(),
                t.getRenkoSize().getValuePoint(),
                t.getPattern().getSuiteRenko().getPattern(),
                t.getPattern().getSuiteHilo1().getPattern(),
                t.getPattern().getSuiteHilo2().getPattern(),
                t.getDirection().getLabel(),
                t.getTakeprofit().getValuePoint(),
                t.getStoploss().getValuePoint());
    }

    /** Compte toutes les lignes de trade_strategy_history */
    public long countAll() throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM trade_strategy_history");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    // Version "safe" pour les logs : ne lève pas
    private long countAllSafe() {
        try {
            return countAll();
        } catch (Exception e) {
            return -1L;
        }
    }

    // Additionne proprement les retours de executeBatch().
    // Retourne -1 si au moins une valeur == Statement.SUCCESS_NO_INFO.
    private long sumCounts(int[] counts) {
        if (counts == null)
            return 0L;
        long sum = 0L;
        for (int c : counts) {
            if (c == Statement.SUCCESS_NO_INFO) {
                return -1L;
            }
            if (c == Statement.EXECUTE_FAILED) {
                // déjà géré par rollback au-dessus; on ignore cette valeur parcellaire
                continue;
            }
            if (c > 0)
                sum += c;
        }
        return sum;
    }

    // =========================================================
    // HELPERS NULLABLES
    // =========================================================

    private static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    private static Double getNullableDouble(ResultSet rs, String col) throws SQLException {
        double v = rs.getDouble(col);
        return rs.wasNull() ? null : v;
    }

    private static Integer getNullableInt(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    private static Long getNullableLong(ResultSet rs, String col) throws SQLException {
        long v = rs.getLong(col);
        return rs.wasNull() ? null : v;
    }
}
