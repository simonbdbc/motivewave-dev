package com.monitoralgo.core.dao;

import com.monitoralgo.core.database.DatabaseManager;
import com.monitoralgo.core.model.*;
import com.monitoralgo.core.util.CoreLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table trade_strategy_live (exécution "live" des trades).
 *
 * Schéma attendu :
 * trade_strategy_live(
 * id_trade BIGSERIAL PK,
 * pattern_strategy_id BIGINT NOT NULL FK pattern_strategy(id_pattern_strategy),
 * timestamp_entry BIGINT NOT NULL,
 * date_time_entry TEXT NOT NULL,
 * entry_price DOUBLE PRECISION NOT NULL,
 * timestamp_exit BIGINT NULL,
 * date_time_exit TEXT NULL,
 * exit_price DOUBLE PRECISION NULL,
 * tp_price DOUBLE PRECISION NULL,
 * sl_price DOUBLE PRECISION NULL,
 * result TEXT NULL,
 * updated_at TEXT NULL,
 * UNIQUE(pattern_strategy_id, timestamp_entry, entry_price)
 * )
 */
public class TradeStrategyLiveDao {

    // =========================================================
    // INSERT (UPSERT)
    // =========================================================

    /**
     * Insère un enregistrement "live". Si (pattern_strategy_id, timestamp_entry,
     * entry_price)
     * existe déjà, met à jour les champs de sortie + updated_at.
     *
     * Champs NOT NULL : pattern_strategy_id, timestamp_entry, date_time_entry,
     * entry_price.
     * Champs NULLABLE : timestamp_exit, date_time_exit, exit_price, tp_price,
     * sl_price, result.
     */
    public void insert(TradeStrategyLive trade) throws SQLException {
        final String sql = """
                INSERT INTO trade_strategy_live (
                    pattern_strategy_id, timestamp_entry, date_time_entry, entry_price,
                    timestamp_exit, date_time_exit, exit_price, tp_price, sl_price, result, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS'))
                ON CONFLICT (pattern_strategy_id, timestamp_entry, entry_price) DO UPDATE SET
                    timestamp_exit = EXCLUDED.timestamp_exit,
                    date_time_exit = EXCLUDED.date_time_exit,
                    exit_price = EXCLUDED.exit_price,
                    tp_price = EXCLUDED.tp_price,
                    sl_price = EXCLUDED.sl_price,
                    result = EXCLUDED.result,
                    updated_at = TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS')
                """;

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // NOT NULL
            requireNonNull(trade.getPatternStrategyId(), "pattern_strategy_id");
            requireNonNull(trade.getTimestampEntry(), "timestamp_entry");
            requireNonEmpty(trade.getDateTimeEntry(), "date_time_entry");
            requireNonNull(trade.getEntryPrice(), "entry_price");

            ps.setLong(1, trade.getPatternStrategyId());
            ps.setLong(2, trade.getTimestampEntry());
            ps.setString(3, trade.getDateTimeEntry());
            ps.setDouble(4, trade.getEntryPrice());

            // NULLABLES (setObject gère correctement les NULL)
            ps.setObject(5, trade.getTimestampExit(), Types.BIGINT);
            ps.setObject(6, emptyToNull(trade.getDateTimeExit()), Types.VARCHAR);
            ps.setObject(7, trade.getExitPrice(), Types.DOUBLE);
            ps.setObject(8, trade.getTpPrice(), Types.DOUBLE);
            ps.setObject(9, trade.getSlPrice(), Types.DOUBLE);
            ps.setObject(10, emptyToNull(trade.getResult()), Types.VARCHAR);

            ps.executeUpdate();

            // Récupère l'id si insertion (pas en cas de pur UPDATE via upsert)
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    trade.setIdTrade(keys.getLong(1));
                }
            }
        }
    }

    // =========================================================
    // SELECTS (SIMPLE)
    // =========================================================

    /** Mapping ResultSet → modèle simple TradeStrategyLive */
    private TradeStrategyLive mapRow(ResultSet rs) throws SQLException {
        TradeStrategyLive t = new TradeStrategyLive();
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
        t.setResult(rs.getString("result"));
        t.setUpdatedAt(rs.getString("updated_at"));
        return t;
    }

    /** Récupère tous les trades live (simple) */
    public List<TradeStrategyLive> findAll() throws SQLException {
        final String sql = "SELECT * FROM trade_strategy_live ORDER BY id_trade ASC";
        List<TradeStrategyLive> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapRow(rs));
        }
        return list;
    }
    
    /** Récupère tous les trades live (simple) */
    public List<TradeStrategyLive> findAllByDate() throws SQLException {
        final String sql = "SELECT * FROM trade_strategy_live ORDER BY updated_at DESC";
        List<TradeStrategyLive> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapRow(rs));
        }
        return list;
    }

    /** Récupère un trade live par id (simple) */
    public TradeStrategyLive findById(long id) throws SQLException {
        final String sql = "SELECT * FROM trade_strategy_live WHERE id_trade = ?";
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

    // =========================================================
    // SELECT FULL (jointures)
    // =========================================================

    /**
     * Mapping ResultSet → TradeStrategyLiveFull (avec tous les objets liés).
     * Hypothèse : ton modèle TradeStrategyLiveFull possède un constructeur
     * avec (idTrade, instrument, timeframe, renkoSize, pattern, direction,
     * takeprofit, stoploss,
     * timestampEntry, dateTimeEntry, entryPrice, timestampExit, dateTimeExit,
     * exitPrice, tpPrice, slPrice, result, updatedAt)
     */
    private TradeStrategyLiveFull mapRowFull(ResultSet rs) throws SQLException {
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

        return new TradeStrategyLiveFull(
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
                rs.getDouble("entry_price"),
                getNullableLong(rs, "timestamp_exit"),
                rs.getString("date_time_exit"),
                getNullableDouble(rs, "exit_price"),
                getNullableDouble(rs, "tp_price"),
                getNullableDouble(rs, "sl_price"),
                rs.getString("result"),
                rs.getString("updated_at"));
    }

    /** Récupère tous les trades live (FULL) avec jointures */
    public List<TradeStrategyLiveFull> findAllFull() throws SQLException {
        final String sql = """
                SELECT t.id_trade, t.pattern_strategy_id, t.timestamp_entry, t.date_time_entry, t.entry_price,
                       t.timestamp_exit, t.date_time_exit, t.exit_price, t.tp_price, t.sl_price, t.result, t.updated_at,
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
                FROM trade_strategy_live t
                INNER JOIN pattern_strategy ps ON t.pattern_strategy_id = ps.id_pattern_strategy
                INNER JOIN instrument i   ON ps.instrument_id = i.id_instrument
                INNER JOIN timeframe tf   ON ps.timeframe_id = tf.id_timeframe
                INNER JOIN renko_size rs  ON ps.renko_size_id = rs.id_renko_size
                INNER JOIN direction d    ON ps.direction_id = d.id_direction
                INNER JOIN takeprofit tp  ON ps.takeprofit_id = tp.id_takeprofit
                INNER JOIN stoploss sl    ON ps.stoploss_id = sl.id_stoploss
                INNER JOIN pattern p      ON ps.pattern_id = p.id_pattern
                INNER JOIN suite sr  ON p.suite_renko_id = sr.id_suite
                INNER JOIN suite sh1 ON p.suite_hilo1_id = sh1.id_suite
                INNER JOIN suite sh2 ON p.suite_hilo2_id = sh2.id_suite
                ORDER BY t.id_trade ASC
                """;

        List<TradeStrategyLiveFull> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapRowFull(rs));
        }
        return list;
    }

    // =========================================================
    // INSERT BATCH (UPSERT)
    // =========================================================

    /**
     * Insert en batch des enregistrements FULL.
     * - Résout pattern_strategy_id via un INSERT…SELECT sur pattern_strategy.
     * - ON CONFLICT fait un UPDATE des champs de sortie + updated_at.
     */
    public int insertFullBatch(List<TradeStrategyLiveFull> batch, String STUDY_ID, String INSTANCE_NAME)
            throws SQLException {

        if (batch == null || batch.isEmpty())
            return 0;
        final String cleLogique = buildCleLogique(batch.get(0));

        final String instanceId = String.valueOf(System.identityHashCode(this));
        CoreLogger LOGGER = CoreLogger.getInstance(STUDY_ID, INSTANCE_NAME);
        LOGGER.setContext(instanceId, STUDY_ID, "[DAO] Trade Strategy Live Full", "Début Insert Full Batch");

        final String sql = """
                INSERT INTO trade_strategy_live (
                    pattern_strategy_id, timestamp_entry, date_time_entry, entry_price,
                    timestamp_exit, date_time_exit, exit_price, tp_price, sl_price, result, updated_at
                )
                SELECT
                    ps.id_pattern_strategy, ?, ?, ?, ?, ?, ?, ?, ?, ?, TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS')
                FROM pattern_strategy ps
                WHERE ps.instrument_id = ? AND ps.timeframe_id = ? AND ps.renko_size_id = ?
                  AND ps.pattern_id = ? AND ps.direction_id = ?
                  AND ps.takeprofit_id = ? AND ps.stoploss_id = ?
                ON CONFLICT (pattern_strategy_id, timestamp_entry, entry_price) DO UPDATE SET
                    timestamp_exit = EXCLUDED.timestamp_exit,
                    date_time_exit = EXCLUDED.date_time_exit,
                    exit_price     = EXCLUDED.exit_price,
                    tp_price       = EXCLUDED.tp_price,
                    sl_price       = EXCLUDED.sl_price,
                    result         = EXCLUDED.result,
                    updated_at     = TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS')
                """;

        final int CHUNK_SIZE = 20_000;
        int count = 0;

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // Petites optimisations de latence disque pour la transaction
            try (Statement st = conn.createStatement()) {
                st.execute("SET LOCAL synchronous_commit = OFF");
            }

            for (TradeStrategyLiveFull t : batch) {
                // NOT NULL
                requireNonNull(t.getTimestampEntry(), "timestamp_entry (batch)");
                requireNonEmpty(t.getDateTimeEntry(), "date_time_entry (batch)");
                requireNonNull(t.getEntryPrice(), "entry_price (batch)");

                ps.setLong(1, t.getTimestampEntry());
                ps.setString(2, t.getDateTimeEntry());
                ps.setDouble(3, t.getEntryPrice());

                // NULLABLE
                ps.setObject(4, t.getTimestampExit(), Types.BIGINT);
                ps.setObject(5, emptyToNull(t.getDateTimeExit()), Types.VARCHAR);
                ps.setObject(6, t.getExitPrice(), Types.DOUBLE);
                ps.setObject(7, t.getTpPrice(), Types.DOUBLE);
                ps.setObject(8, t.getSlPrice(), Types.DOUBLE);
                ps.setObject(9, emptyToNull(t.getResult()), Types.VARCHAR);

                // WHERE (pattern_strategy)
                ps.setInt(10, t.getInstrument().getId());
                ps.setInt(11, t.getTimeframe().getId());
                ps.setInt(12, t.getRenkoSize().getId());
                ps.setInt(13, t.getPattern().getId());
                ps.setInt(14, t.getDirection().getId());
                ps.setInt(15, t.getTakeprofit().getId());
                ps.setInt(16, t.getStoploss().getId());

                ps.addBatch();
                count++;

                if (count % CHUNK_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    LOGGER.log(String.format("%s  ✔ Insertion de %d/%d trades", cleLogique, count, batch.size()));
                }
            }

            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(oldAuto);
            LOGGER.log(String.format("%s  ✔ Insertion finale du Batch → total trades insérés : %d", cleLogique, count));

        } catch (SQLException e) {
            LOGGER.log("[ERROR] Batch insert partial échoué: " + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            LOGGER.log(sw.toString());
            throw e;
        } finally {
            LOGGER.setContext(instanceId, STUDY_ID, "[DAO] Trade Strategy History Full", "Fin Insert Full Batch");
        }

        return count;
    }

    // =========================================================
    // DIVERS
    // =========================================================

    /** Compte toutes les lignes de trade_strategy_live */
    public long countAll() throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM trade_strategy_live");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    /** Clé lisible pour les logs (optionnelle) */
    private String buildCleLogique(TradeStrategyLiveFull t) {
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

    // =========================================================
    // HELPERS
    // =========================================================

    private static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    private static Double getNullableDouble(ResultSet rs, String col) throws SQLException {
        double v = rs.getDouble(col);
        return rs.wasNull() ? null : v;
    }

    private static Long getNullableLong(ResultSet rs, String col) throws SQLException {
        long v = rs.getLong(col);
        return rs.wasNull() ? null : v;
    }

    private static void requireNonNull(Object v, String field) {
        if (v == null)
            throw new IllegalArgumentException("Field '" + field + "' must not be null");
    }

    private static void requireNonEmpty(String v, String field) {
        if (v == null || v.isEmpty()) {
            throw new IllegalArgumentException("Field '" + field + "' must not be empty");
        }
    }
}
