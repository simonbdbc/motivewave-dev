package com.monitoralgo.stats.dao;

import com.monitoralgo.stats.model.SnapshotAioByRenko;
import com.monitoralgo.algo.util.BacktestUtil;
import com.monitoralgo.core.model.Instrument;
import com.monitoralgo.core.model.RenkoSize;
import com.monitoralgo.core.model.Timeframe;
import com.monitoralgo.stats.database.DatabaseStatsManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO d'accès à la table snapshot_aio_by_renko.
 *
 * Objectifs clés pour le backtest :
 * - Récupération groupée des snapshots par (instrument_key, tf_scale,
 * tf_interval, renko_size),
 * avec fenêtre temporelle optionnelle.
 * - Compteurs précis pour les logs/diagnostics (total lignes, total lignes
 * filtrées,
 * nb de groupes distincts trouvés vs nb de groupes possibles).
 * - Insert simple et insert batch (transactionnel) pour alimenter/mettre à jour
 * les snapshots.
 *
 * Remarques :
 * - On ne force pas autoCommit=true ici : les connexions récupérées via
 * DatabaseStatsManager
 * restent dans leur mode par défaut (souvent true). Seul insertBatch() ouvre
 * explicitement
 * une transaction pour le batch.
 */
public class SnapshotAioByRenkoDao {

    /*
     * =============================================================================
     * ============
     * QUERIES - LECTURE
     * =============================================================================
     * ============
     */

    /**
     * Récupère tous les snapshots groupés par
     * (instrument_key|tf_scale|tf_interval|renko_size).
     * Sans filtration temporelle.
     */
    public static Map<String, List<SnapshotAioByRenko>> getGroupedSnapshotsAioByRenko(
            List<Instrument> instruments,
            List<Timeframe> timeframes,
            List<RenkoSize> renkoSizes) throws SQLException {

        if (instruments == null || instruments.isEmpty()
                || timeframes == null || timeframes.isEmpty()
                || renkoSizes == null || renkoSizes.isEmpty()) {
            return Collections.emptyMap();
        }

        String inInstrument = String.join(",", Collections.nCopies(instruments.size(), "?"));
        String inInterval = String.join(",", Collections.nCopies(timeframes.size(), "?"));
        String inRenko = String.join(",", Collections.nCopies(renkoSizes.size(), "?"));

        String sql = "SELECT id, instrument_key, instrument_name, tf_scale, tf_interval, renko_size, " +
                "       timestamp, date_time, price_res, price_sup, " +
                "       renko_color, hilo_color_1, hilo_color_2, " +
                "       hilo_price_1, hilo_price_2, updated_at " +
                "FROM snapshot_aio_by_renko " +
                "WHERE instrument_key IN (" + inInstrument + ") " +
                "  AND tf_interval   IN (" + inInterval + ") " +
                "  AND renko_size    IN (" + inRenko + ") " +
                "ORDER BY instrument_key, tf_scale, tf_interval, renko_size, timestamp";

        try (Connection conn = DatabaseStatsManager.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            int idx = 1;
            // Instruments
            for (Instrument inst : instruments) {
                pst.setString(idx++, inst.getKey());
            }
            // Timeframes (valeur entière)
            for (Timeframe tf : timeframes) {
                pst.setInt(idx++, tf.getValue());
            }
            // Renko sizes (valeur en points)
            for (RenkoSize r : renkoSizes) {
                pst.setDouble(idx++, r.getValuePoint());
            }

            try (ResultSet rs = pst.executeQuery()) {
                Map<String, List<SnapshotAioByRenko>> groupedRecords = new LinkedHashMap<>();
                while (rs.next()) {
                    SnapshotAioByRenko snap = mapRow(rs);
                    String groupKey = snap.getInstrumentKey() + "|" + snap.getTfScale() + "|"
                            + snap.getTfInterval() + "|" + snap.getRenkoSize();
                    groupedRecords.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(snap);
                }
                return groupedRecords;
            }
        }
    }

    /**
     * Version filtrée par fenêtre temporelle optionnelle [from, to].
     * Si from==null, pas de borne inférieure. Si to==null, pas de borne supérieure.
     */
    public static Map<String, List<SnapshotAioByRenko>> getGroupedSnapshotsAioByRenko(
            List<Instrument> instruments,
            List<Timeframe> timeframes,
            List<RenkoSize> renkoSizes,
            LocalDateTime from,
            LocalDateTime to) throws SQLException {

        if (instruments == null || instruments.isEmpty()
                || timeframes == null || timeframes.isEmpty()
                || renkoSizes == null || renkoSizes.isEmpty()) {
            return Collections.emptyMap();
        }

        String inInstrument = String.join(",", Collections.nCopies(instruments.size(), "?"));
        String inInterval = String.join(",", Collections.nCopies(timeframes.size(), "?"));
        String inRenko = String.join(",", Collections.nCopies(renkoSizes.size(), "?"));

        StringBuilder sql = new StringBuilder(
                "SELECT id, instrument_key, instrument_name, tf_scale, tf_interval, renko_size, " +
                        "       timestamp, date_time, price_res, price_sup, " +
                        "       renko_color, hilo_color_1, hilo_color_2, " +
                        "       hilo_price_1, hilo_price_2, updated_at " +
                        "FROM snapshot_aio_by_renko " +
                        "WHERE instrument_key IN (" + inInstrument + ") " +
                        "  AND tf_interval   IN (" + inInterval + ") " +
                        "  AND renko_size    IN (" + inRenko + ") ");

        // Fenêtre temporelle optionnelle sur la colonne "timestamp" (epoch millis)
        if (from != null) {
            sql.append("  AND timestamp >= ? ");
        }
        if (to != null) {
            sql.append("  AND timestamp <= ? ");
        }

        sql.append("ORDER BY instrument_key, tf_scale, tf_interval, renko_size, timestamp");

        try (Connection conn = DatabaseStatsManager.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            // Instruments
            for (Instrument inst : instruments) {
                pst.setString(idx++, inst.getKey());
            }
            // Timeframes
            for (Timeframe tf : timeframes) {
                pst.setInt(idx++, tf.getValue());
            }
            // RenkoSizes
            for (RenkoSize r : renkoSizes) {
                pst.setDouble(idx++, r.getValuePoint());
            }
            // Timestamps
            if (from != null) {
                pst.setLong(idx++, BacktestUtil.toEpochMilli(from));
            }
            if (to != null) {
                pst.setLong(idx++, BacktestUtil.toEpochMilli(to));
            }

            try (ResultSet rs = pst.executeQuery()) {
                Map<String, List<SnapshotAioByRenko>> groupedRecords = new LinkedHashMap<>();
                while (rs.next()) {
                    SnapshotAioByRenko snap = mapRow(rs);
                    String groupKey = snap.getInstrumentKey() + "|" + snap.getTfScale() + "|"
                            + snap.getTfInterval() + "|" + snap.getRenkoSize();
                    groupedRecords.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(snap);
                }
                return groupedRecords;
            }
        }
    }

    /** Mappe une ligne de ResultSet en objet SnapshotAioByRenko. */
    private static SnapshotAioByRenko mapRow(ResultSet rs) throws SQLException {
        SnapshotAioByRenko snap = new SnapshotAioByRenko();
        snap.setId(rs.getInt("id"));
        snap.setInstrumentKey(rs.getString("instrument_key"));
        snap.setInstrumentName(rs.getString("instrument_name"));
        snap.setTfScale(rs.getString("tf_scale"));
        snap.setTfInterval(rs.getInt("tf_interval"));
        snap.setRenkoSize(rs.getDouble("renko_size"));
        snap.setTimestamp(rs.getLong("timestamp"));
        snap.setDateTime(rs.getString("date_time"));
        snap.setPriceRes(rs.getDouble("price_res"));
        snap.setPriceSup(rs.getDouble("price_sup"));
        snap.setRenkoColor(rs.getString("renko_color"));
        snap.setHiloColor1(rs.getString("hilo_color_1"));
        snap.setHiloColor2(rs.getString("hilo_color_2"));
        snap.setHiloPrice1(rs.getDouble("hilo_price_1"));
        snap.setHiloPrice2(rs.getDouble("hilo_price_2"));
        snap.setUpdatedAt(rs.getString("updated_at"));
        return snap;
    }

    /**
     * Récupère tous les enregistrements (utilitaire générique).
     * Évite pour les gros volumes : préférer les méthodes groupées/filtrées.
     */
    public List<SnapshotAioByRenko> findAll() throws SQLException {
        String sql = "SELECT * FROM snapshot_aio_by_renko";
        List<SnapshotAioByRenko> list = new ArrayList<>();
        try (Connection conn = DatabaseStatsManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /*
     * =============================================================================
     * ============
     * QUERIES - INSERT
     * =============================================================================
     * ============
     */

    /**
     * Insert/Upsert d'un snapshot.
     * ON CONFLICT sur (instrument_key, tf_scale, tf_interval, renko_size,
     * timestamp).
     */
    public void insert(SnapshotAioByRenko s) throws SQLException {
        String sql = "INSERT INTO snapshot_aio_by_renko (" +
                "  instrument_key, instrument_name, tf_scale, tf_interval, renko_size, timestamp, date_time," +
                "  price_res, price_sup, renko_color, hilo_color_1, hilo_color_2, hilo_price_1, hilo_price_2, updated_at"
                +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (instrument_key, tf_scale, tf_interval, renko_size, timestamp) DO UPDATE SET " +
                "  instrument_name = EXCLUDED.instrument_name, " +
                "  date_time       = EXCLUDED.date_time, " +
                "  price_res       = EXCLUDED.price_res, " +
                "  price_sup       = EXCLUDED.price_sup, " +
                "  renko_color     = EXCLUDED.renko_color, " +
                "  hilo_color_1    = EXCLUDED.hilo_color_1, " +
                "  hilo_color_2    = EXCLUDED.hilo_color_2, " +
                "  hilo_price_1    = EXCLUDED.hilo_price_1, " +
                "  hilo_price_2    = EXCLUDED.hilo_price_2, " +
                "  updated_at      = EXCLUDED.updated_at";

        try (Connection conn = DatabaseStatsManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getInstrumentKey());
            ps.setString(2, s.getInstrumentName());
            ps.setString(3, s.getTfScale());
            ps.setInt(4, s.getTfInterval());
            ps.setDouble(5, s.getRenkoSize());
            ps.setLong(6, s.getTimestamp());
            ps.setString(7, s.getDateTime());
            ps.setDouble(8, s.getPriceRes());
            ps.setDouble(9, s.getPriceSup());
            ps.setString(10, s.getRenkoColor());
            ps.setString(11, s.getHiloColor1());
            ps.setString(12, s.getHiloColor2());
            ps.setDouble(13, s.getHiloPrice1());
            ps.setDouble(14, s.getHiloPrice2());
            ps.setString(15, s.getUpdatedAt());

            ps.executeUpdate();
        }
    }

    /**
     * Insert/Upsert en batch d'une liste de snapshots.
     * - Ouvre explicitement une transaction (autoCommit=false) pour améliorer le
     * débit.
     * - Laisse la connexion revenir à son autoCommit par défaut grâce au
     * try-with-resources.
     */
    public void insertBatch(List<SnapshotAioByRenko> batch) throws SQLException {
        if (batch == null || batch.isEmpty())
            return;

        String sql = "INSERT INTO snapshot_aio_by_renko (" +
                "  instrument_key, instrument_name, tf_scale, tf_interval, renko_size, timestamp, date_time," +
                "  price_res, price_sup, renko_color, hilo_color_1, hilo_color_2, hilo_price_1, hilo_price_2, updated_at"
                +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (instrument_key, tf_scale, tf_interval, renko_size, timestamp) DO UPDATE SET " +
                "  instrument_name = EXCLUDED.instrument_name, " +
                "  date_time       = EXCLUDED.date_time, " +
                "  price_res       = EXCLUDED.price_res, " +
                "  price_sup       = EXCLUDED.price_sup, " +
                "  renko_color     = EXCLUDED.renko_color, " +
                "  hilo_color_1    = EXCLUDED.hilo_color_1, " +
                "  hilo_color_2    = EXCLUDED.hilo_color_2, " +
                "  hilo_price_1    = EXCLUDED.hilo_price_1, " +
                "  hilo_price_2    = EXCLUDED.hilo_price_2, " +
                "  updated_at      = EXCLUDED.updated_at";

        try (Connection conn = DatabaseStatsManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // transaction explicite pour le batch

            for (SnapshotAioByRenko s : batch) {
                ps.setString(1, s.getInstrumentKey());
                ps.setString(2, s.getInstrumentName());
                ps.setString(3, s.getTfScale());
                ps.setInt(4, s.getTfInterval());
                ps.setDouble(5, s.getRenkoSize());
                ps.setLong(6, s.getTimestamp());
                ps.setString(7, s.getDateTime());
                ps.setDouble(8, s.getPriceRes());
                ps.setDouble(9, s.getPriceSup());
                ps.setString(10, s.getRenkoColor());
                ps.setString(11, s.getHiloColor1());
                ps.setString(12, s.getHiloColor2());
                ps.setDouble(13, s.getHiloPrice1());
                ps.setDouble(14, s.getHiloPrice2());
                ps.setString(15, s.getUpdatedAt());
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
        }
    }

    /*
     * =============================================================================
     * ============
     * QUERIES - UTILS
     * =============================================================================
     * ============
     */

    /**
     * Retourne tous les snapshots pour un instrument/intervalle/renkoSize dans un
     * range de timestamps.
     */
    public List<SnapshotAioByRenko> findByInstrumentKeyAndDateRange(
            String instrumentKey, Integer tfInterval, Double renkoSize,
            long timestampFrom, long timestampTo) throws SQLException {

        String sql = "SELECT * FROM snapshot_aio_by_renko " +
                "WHERE instrument_key = ? " +
                "  AND tf_interval    = ? " +
                "  AND renko_size     = ? " +
                "  AND timestamp BETWEEN ? AND ? " +
                "ORDER BY timestamp ASC";

        List<SnapshotAioByRenko> list = new ArrayList<>();
        try (Connection conn = DatabaseStatsManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, instrumentKey);
            ps.setInt(2, tfInterval);
            ps.setDouble(3, renkoSize);
            ps.setLong(4, timestampFrom);
            ps.setLong(5, timestampTo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /*
     * =============================================================================
     * ============
     * COMPTEURS POUR LOGS/DIAGNOSTICS
     * =============================================================================
     * ============
     */

    /**
     * Nombre total de lignes dans snapshot_aio_by_renko (toutes conditions
     * confondues).
     */
    public long countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM snapshot_aio_by_renko";
        try (Connection conn = DatabaseStatsManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    /**
     * Nombre de lignes correspondant aux filtres (instruments, timeframes,
     * renkoSizes)
     * et à une éventuelle fenêtre temporelle [from, to].
     */
    public long countByFilters(
            List<Instrument> instruments,
            List<Timeframe> timeframes,
            List<RenkoSize> renkoSizes,
            LocalDateTime from,
            LocalDateTime to) throws SQLException {

        if (instruments == null || instruments.isEmpty()
                || timeframes == null || timeframes.isEmpty()
                || renkoSizes == null || renkoSizes.isEmpty()) {
            return 0L;
        }

        String inInstrument = String.join(",", Collections.nCopies(instruments.size(), "?"));
        String inInterval = String.join(",", Collections.nCopies(timeframes.size(), "?"));
        String inRenko = String.join(",", Collections.nCopies(renkoSizes.size(), "?"));

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) " +
                        "FROM snapshot_aio_by_renko " +
                        "WHERE instrument_key IN (" + inInstrument + ") " +
                        "  AND tf_interval   IN (" + inInterval + ") " +
                        "  AND renko_size    IN (" + inRenko + ") ");

        if (from != null) {
            sql.append("  AND timestamp >= ? ");
        }
        if (to != null) {
            sql.append("  AND timestamp <= ? ");
        }

        try (Connection conn = DatabaseStatsManager.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Instrument inst : instruments) {
                pst.setString(idx++, inst.getKey());
            }
            for (Timeframe tf : timeframes) {
                pst.setInt(idx++, tf.getValue());
            }
            for (RenkoSize r : renkoSizes) {
                pst.setDouble(idx++, r.getValuePoint());
            }
            if (from != null) {
                pst.setLong(idx++, BacktestUtil.toEpochMilli(from));
            }
            if (to != null) {
                pst.setLong(idx++, BacktestUtil.toEpochMilli(to));
            }

            try (ResultSet rs = pst.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    /**
     * Nombre de groupes DISTINCTs (instrument_key|tf_scale|tf_interval|renko_size)
     * présents dans la base pour les filtres fournis (et fenêtre temporelle
     * optionnelle).
     *
     * NOTE : utilisation de CONCAT_WS pour éviter les cast explicites.
     */
    public long countDistinctGroupsByFilters(
            List<Instrument> instruments,
            List<Timeframe> timeframes,
            List<RenkoSize> renkoSizes,
            LocalDateTime from,
            LocalDateTime to) throws SQLException {

        if (instruments == null || instruments.isEmpty()
                || timeframes == null || timeframes.isEmpty()
                || renkoSizes == null || renkoSizes.isEmpty()) {
            return 0L;
        }

        String inInstrument = String.join(",", Collections.nCopies(instruments.size(), "?"));
        String inInterval = String.join(",", Collections.nCopies(timeframes.size(), "?"));
        String inRenko = String.join(",", Collections.nCopies(renkoSizes.size(), "?"));

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT CONCAT_WS('|', instrument_key, tf_scale, tf_interval, renko_size)) " +
                        "FROM snapshot_aio_by_renko " +
                        "WHERE instrument_key IN (" + inInstrument + ") " +
                        "  AND tf_interval   IN (" + inInterval + ") " +
                        "  AND renko_size    IN (" + inRenko + ") ");

        if (from != null) {
            sql.append("  AND timestamp >= ? ");
        }
        if (to != null) {
            sql.append("  AND timestamp <= ? ");
        }

        try (Connection conn = DatabaseStatsManager.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Instrument inst : instruments) {
                pst.setString(idx++, inst.getKey());
            }
            for (Timeframe tf : timeframes) {
                pst.setInt(idx++, tf.getValue());
            }
            for (RenkoSize r : renkoSizes) {
                pst.setDouble(idx++, r.getValuePoint());
            }
            if (from != null) {
                pst.setLong(idx++, BacktestUtil.toEpochMilli(from));
            }
            if (to != null) {
                pst.setLong(idx++, BacktestUtil.toEpochMilli(to));
            }

            try (ResultSet rs = pst.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    /**
     * Nombre théorique de groupes possibles (produit cartésien).
     * Utile pour comparer "groupes trouvés" vs "groupes possibles".
     */
    public long countGroupsPossible(
            List<Instrument> instruments,
            List<Timeframe> timeframes,
            List<RenkoSize> renkoSizes) {

        long ni = (instruments == null) ? 0 : instruments.size();
        long nt = (timeframes == null) ? 0 : timeframes.size();
        long nr = (renkoSizes == null) ? 0 : renkoSizes.size();
        return ni * nt * nr;
    }
}
