package com.monitoralgo.core.dao;

import com.monitoralgo.core.database.DatabaseManager;
import com.monitoralgo.core.model.TradingSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table trading_session (gestion des sessions journalières et
 * paramètres de risque).
 *
 * DDL de référence :
 * trading_session(
 * id_session BIGSERIAL PRIMARY KEY,
 * session_date TEXT NOT NULL,
 * pnl_session DOUBLE PRECISION NULL,
 * trade_count INT NULL,
 * count_tp INT NULL,
 * count_sl INT NULL,
 * successive_loss_count INT NULL,
 * max_win_threshold INT NOT NULL,
 * max_loss_threshold INT NOT NULL,
 * max_successive_loss_threshold INT NOT NULL,
 * successive_loss_enabled BOOLEAN NOT NULL,
 * start_time TEXT NULL,
 * end_time TEXT NULL,
 * updated_at TEXT NULL,
 * created_at TEXT NULL,
 * UNIQUE(session_date, max_win_threshold, max_loss_threshold,
 * max_successive_loss_threshold, successive_loss_enabled)
 * )
 */
public class TradingSessionDao {

    // =========================================================
    // INSERT (UPSERT)
    // =========================================================

    /**
     * Insère ou met à jour une session de trading.
     *
     * Conflit géré sur l'unicité :
     * (session_date, max_win_threshold, max_loss_threshold,
     * max_successive_loss_threshold, successive_loss_enabled)
     *
     * Règles :
     * - En INSERT : on remplit created_at et updated_at à NOW().
     * - En UPDATE (ON CONFLICT) : on met à jour les champs "stats" + end_time
     * éventuellement,
     * et on rafraîchit updated_at. On ne touche PAS à created_at.
     * - start_time : souvent défini à la première ouverture. Pour éviter d'écraser
     * une valeur
     * existante, on applique COALESCE(t.start_time, EXCLUDED.start_time) → n’écrit
     * que si start_time
     * n’est pas déjà présent.
     */
    public void insert(TradingSession s) throws SQLException {
        final String sql = """
                INSERT INTO trading_session (
                    session_date, pnl_session, trade_count, count_tp, count_sl, successive_loss_count,
                    max_win_threshold, max_loss_threshold, max_successive_loss_threshold, successive_loss_enabled,
                    start_time, end_time, updated_at, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?,
                        ?, ?, ?, ?,
                        ?, ?,
                        TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS'),
                        TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS'))
                ON CONFLICT (session_date, max_win_threshold, max_loss_threshold, max_successive_loss_threshold, successive_loss_enabled)
                DO UPDATE SET
                    pnl_session           = EXCLUDED.pnl_session,
                    trade_count           = EXCLUDED.trade_count,
                    count_tp              = EXCLUDED.count_tp,
                    count_sl              = EXCLUDED.count_sl,
                    successive_loss_count = EXCLUDED.successive_loss_count,
                    -- ne remplace start_time que s'il est absent en base
                    start_time            = COALESCE(trading_session.start_time, EXCLUDED.start_time),
                    end_time              = EXCLUDED.end_time,
                    updated_at            = TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS')
                """;

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // NOT NULL (clé logique + contraintes)
            requireNonEmpty(s.getSessionDate(), "session_date");
            ps.setString(1, s.getSessionDate());

            // NULLABLES (stats)
            ps.setObject(2, s.getPnlSession(), Types.DOUBLE);
            ps.setObject(3, s.getTradeCount(), Types.INTEGER);
            ps.setObject(4, s.getCountTp(), Types.INTEGER);
            ps.setObject(5, s.getCountSl(), Types.INTEGER);
            ps.setObject(6, s.getSuccessiveLossCount(), Types.INTEGER);

            // NOT NULL (paramètres de risque)
            ps.setInt(7, s.getMaxWinThreshold());
            ps.setInt(8, s.getMaxLossThreshold());
            ps.setInt(9, s.getMaxSuccessiveLossThreshold());
            ps.setBoolean(10, s.isSuccessiveLossEnabled());

            // NULLABLES (horodatages / timings applicatifs)
            ps.setObject(11, emptyToNull(s.getStartTime()), Types.VARCHAR);
            ps.setObject(12, emptyToNull(s.getEndTime()), Types.VARCHAR);

            ps.executeUpdate();

            // Si insertion (pas update), récupère la PK
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setIdSession(keys.getLong(1));
                }
            }
        }
    }

    // =========================================================
    // SELECTS (SIMPLE)
    // =========================================================

    /** Mapping ResultSet → modèle TradingSession */
    private TradingSession mapRow(ResultSet rs) throws SQLException {
        TradingSession t = new TradingSession();
        t.setIdSession(rs.getLong("id_session"));

        t.setSessionDate(rs.getString("session_date"));
        t.setPnlSession(getNullableDouble(rs, "pnl_session"));
        t.setTradeCount(getNullableInt(rs, "trade_count"));
        t.setCountTp(getNullableInt(rs, "count_tp"));
        t.setCountSl(getNullableInt(rs, "count_sl"));
        t.setSuccessiveLossCount(getNullableInt(rs, "successive_loss_count"));

        t.setMaxWinThreshold(rs.getInt("max_win_threshold"));
        t.setMaxLossThreshold(rs.getInt("max_loss_threshold"));
        t.setMaxSuccessiveLossThreshold(rs.getInt("max_successive_loss_threshold"));
        t.setSuccessiveLossEnabled(rs.getBoolean("successive_loss_enabled"));

        t.setStartTime(rs.getString("start_time"));
        t.setEndTime(rs.getString("end_time"));
        t.setUpdatedAt(rs.getString("updated_at"));
        t.setCreatedAt(rs.getString("created_at"));
        return t;
    }

    /** Récupère toutes les sessions (ordre chronologique par id) */
    public List<TradingSession> findAll() throws SQLException {
        final String sql = "SELECT * FROM trading_session ORDER BY id_session ASC";
        List<TradingSession> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapRow(rs));
        }
        return list;
    }

    /** Récupère une session par PK */
    public TradingSession findById(long id) throws SQLException {
        final String sql = "SELECT * FROM trading_session WHERE id_session = ?";
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

    /**
     * Récupère une session par clé logique UNIQUE :
     * (session_date, max_win_threshold, max_loss_threshold,
     * max_successive_loss_threshold, successive_loss_enabled)
     */
    public TradingSession findByUniqueKey(String sessionDate,
            int maxWinThreshold,
            int maxLossThreshold,
            int maxSuccessiveLossThreshold,
            boolean successiveLossEnabled) throws SQLException {
        final String sql = """
                SELECT * FROM trading_session
                WHERE session_date = ?
                  AND max_win_threshold = ?
                  AND max_loss_threshold = ?
                  AND max_successive_loss_threshold = ?
                  AND successive_loss_enabled = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sessionDate);
            ps.setInt(2, maxWinThreshold);
            ps.setInt(3, maxLossThreshold);
            ps.setInt(4, maxSuccessiveLossThreshold);
            ps.setBoolean(5, successiveLossEnabled);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Récupère les 3 dernières sessions, triées par date de création décroissante.
     */
    public List<TradingSession> findLastThree() throws SQLException {
        final String sql = """
                SELECT *
                FROM trading_session
                ORDER BY created_at DESC
                LIMIT 3
                """;

        List<TradingSession> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // =========================================================
    // Mises à jour ciblées (optionnel pratique)
    // =========================================================

    /** Met fin à une session (set end_time = NOW) en la ciblant par id_session. */
    public int closeSession(long idSession) throws SQLException {
        final String sql = """
                UPDATE trading_session
                SET end_time = TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS'),
                    updated_at = TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS')
                WHERE id_session = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idSession);
            return ps.executeUpdate();
        }
    }

    /** (Optionnel) Initialise le start_time si absent (idempotent). */
    public int ensureStartTime(long idSession, String startTime) throws SQLException {
        final String sql = """
                UPDATE trading_session
                SET start_time = COALESCE(start_time, ?),
                    updated_at = TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS')
                WHERE id_session = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startTime);
            ps.setLong(2, idSession);
            return ps.executeUpdate();
        }
    }

    // =========================================================
    // DIVERS
    // =========================================================

    public long countAll() throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM trading_session");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
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

    private static Integer getNullableInt(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    private static void requireNonEmpty(String v, String field) {
        if (v == null || v.isEmpty()) {
            throw new IllegalArgumentException("Field '" + field + "' must not be empty");
        }
    }
}
