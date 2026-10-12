package backtest.database;

import backtest.config.DbConfig;
import backtest.model.RenkoRecord;
import backtest.util.BacktestUtil;
import backtest.model.PatternResult;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;

public class BacktestDbManager {
    private static HikariDataSource ds;

    // Initialisation du pool et de la table pattern_renko, une seule fois
    static {
        try {
            DbConfig cfg = DbConfig.getInstance();
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(cfg.get("db.url"));
            config.setUsername(cfg.get("db.user"));
            config.setPassword(cfg.get("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(cfg.get("db.pool.max")));
            config.setMinimumIdle(Integer.parseInt(cfg.get("db.pool.min")));
            config.setIdleTimeout(Long.parseLong(cfg.get("db.pool.idleTimeout")));
            config.setConnectionTimeout(Long.parseLong(cfg.get("db.pool.connectionTimeout")));
            ds = new HikariDataSource(config);

            // Création de la table au lancement (idempotent)
            try (Connection conn = ds.getConnection()) {
                createPatternRenkoTableIfNeeded(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Impossible d'initialiser HikariCP DataSource pour BacktestDbManager", e);
        }
    }

    // --------- Obtenir une connexion du pool -----------
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    // --------- Récupère tous les RenkoRecord filtrés par intervalValues et
    // renkoSizes -----------
    public static Map<String, List<RenkoRecord>> getGroupedRenkoRecords(List<Integer> intervalValues,
            List<Double> renkoSizes) throws SQLException {
        if (intervalValues == null || intervalValues.isEmpty() || renkoSizes == null || renkoSizes.isEmpty())
            return Collections.emptyMap();

        String inInterval = String.join(",", Collections.nCopies(intervalValues.size(), "?"));
        String inRenko = String.join(",", Collections.nCopies(renkoSizes.size(), "?"));

        String sql = "SELECT id, instrument_key, tf_scale, tf_interval, renko_size, timestamp, date_time, renko_color, hilo_color_1, hilo_color_2, price_res "
                + "FROM snapshot_aio_by_renko " + "WHERE tf_interval IN (" + inInterval + ") AND renko_size IN ("
                + inRenko + ") " + "ORDER BY instrument_key, tf_scale, tf_interval, renko_size, timestamp";

        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            int idx = 1;
            for (Integer v : intervalValues)
                pst.setInt(idx++, v);
            for (Double v : renkoSizes)
                pst.setDouble(idx++, v);

            try (ResultSet rs = pst.executeQuery()) {
                Map<String, List<RenkoRecord>> groupedRecords = new LinkedHashMap<>();
                while (rs.next()) {
                    RenkoRecord rec = new RenkoRecord(rs.getLong("id"), rs.getString("instrument_key"),
                            rs.getString("tf_scale"), rs.getInt("tf_interval"), rs.getDouble("renko_size"),
                            rs.getLong("timestamp"), rs.getString("date_time"), rs.getString("renko_color"),
                            rs.getString("hilo_color_1"), rs.getString("hilo_color_2"), rs.getDouble("price_res"));
                    String groupKey = rec.instrumentKey + "|" + rec.intervalScale + "|" + rec.intervalValue + "|"
                            + rec.renkoSize;
                    groupedRecords.computeIfAbsent(groupKey, _ -> new ArrayList<>()).add(rec);
                }
                return groupedRecords;
            }
        }
    }

    // --------- Récupère la liste des RenkoRecord pour un pattern donné ----------
    public static List<RenkoRecord> getRenkoRecordsForPattern(String instrumentKey, String intervalScale,
            int intervalValue, double renkoSize) throws SQLException {
        String sql = "SELECT id, instrument_key, tf_scale, tf_interval, renko_size, timestamp, date_time, renko_color, hilo_color_1, hilo_color_2, price_res "
                + "FROM snapshot_aio_by_renko WHERE instrument_key = ? AND tf_scale = ? AND tf_interval = ? AND renko_size = ? ORDER BY timestamp";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, instrumentKey);
            pst.setString(2, intervalScale);
            pst.setInt(3, intervalValue);
            pst.setDouble(4, renkoSize);
            try (ResultSet rs = pst.executeQuery()) {
                List<RenkoRecord> renkos = new ArrayList<>();
                while (rs.next()) {
                    renkos.add(new RenkoRecord(rs.getLong("id"), rs.getString("instrument_key"),
                            rs.getString("tf_scale"), rs.getInt("tf_interval"), rs.getDouble("renko_size"),
                            rs.getLong("timestamp"), rs.getString("date_time"), rs.getString("renko_color"),
                            rs.getString("hilo_color_1"), rs.getString("hilo_color_2"), rs.getDouble("price_res")));
                }
                return renkos;
            }
        }
    }

    // --------- Création de la table pattern_renko si non existante ----------
    public static void createPatternRenkoTableIfNeeded(Connection conn) throws SQLException {
        String createTableSql = """
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
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
        }
    }

    // --------- Insert/Upsert PatternResult en base (PostgreSQL) ----------
    public static void insertPatternResults(List<PatternResult> results, String nowIso) throws SQLException {
        String insertSql = """
                    INSERT INTO pattern_renko (
                        instrument_key, tf_scale, tf_interval, renko_size, tp, sl, pattern_renko,
                        pattern_hilo1, pattern_hilo2, direction, count, last_date_time_entry,
                        count_tp, count_sl, delta, winrate, profit_factor, score, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (instrument_key, tf_scale, tf_interval, tp, sl, renko_size,
                                 pattern_renko, pattern_hilo1, pattern_hilo2, direction)
                    DO UPDATE SET
                        count=EXCLUDED.count,
                        last_date_time_entry=EXCLUDED.last_date_time_entry,
                        count_tp=EXCLUDED.count_tp,
                        count_sl=EXCLUDED.count_sl,
                        delta=EXCLUDED.delta,
                        winrate=EXCLUDED.winrate,
                        profit_factor=EXCLUDED.profit_factor,
                        score=EXCLUDED.score,
                        updated_at=EXCLUDED.updated_at
                """;
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(insertSql)) {
            conn.setAutoCommit(false);
            for (PatternResult r : results) {
                pst.setString(1, r.instrumentKey);
                pst.setString(2, r.intervalScale);
                pst.setInt(3, r.intervalValue);
                pst.setDouble(4, r.renkoSize);
                pst.setInt(5, r.tp);
                pst.setInt(6, r.sl);
                pst.setString(7, BacktestUtil.normalizePattern(r.patternRenko));
                pst.setString(8, BacktestUtil.normalizePattern(r.patternHilo1));
                pst.setString(9, BacktestUtil.normalizePattern(r.patternHilo2));
                pst.setString(10, r.direction);
                pst.setInt(11, r.count);
                pst.setString(12, r.lastDateTimeEntry);
                pst.setInt(13, r.countTp);
                pst.setInt(14, r.countSl);
                pst.setInt(15, r.delta);
                pst.setString(16, r.winrate);
                pst.setDouble(17, r.profitFactor);
                pst.setDouble(18, r.score);
                pst.setString(19, nowIso);
                pst.addBatch();
            }
            pst.executeBatch();
            conn.commit();
        }
    }

    // --------- Nettoyage du pool ----------
    public static void closePool() {
        if (ds != null)
            ds.close();
    }
}
