package com.monitoralgo.core.dao;

import com.monitoralgo.algo.model.TradeDaoBatchReport;
import com.monitoralgo.core.database.DatabaseManager;
import com.monitoralgo.core.model.*;
import com.monitoralgo.core.util.CoreLogger;
import com.monitoralgo.core.util.logger.ProcessLogger;

import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.zip.CRC32;

/**
 * Pipeline bulk robuste pour trade_strategy_history :
 *
 * - TEMP staging par session : trade_strategy_history_stage
 * - COPY CSV -> staging
 * - MERGE (INSERT ... ON CONFLICT DO UPDATE) -> table finale
 * - Advisory lock global autour du MERGE pour éviter les deadlocks entre
 * sessions
 * - Retry exponentiel en cas de deadlock (SQLSTATE 40P01)
 * - (Bonus) Sémaphore JVM optionnel pour lisser le parallélisme d'appels
 *
 * Contrat des colonnes staging (ordre identique au COPY) :
 * instrument_id, timeframe_id, renko_size_id, pattern_id, direction_id,
 * takeprofit_id, stoploss_id,
 * timestamp_entry, date_time_entry, entry_price,
 * timestamp_exit, date_time_exit, exit_price,
 * tp_price, sl_price, count_bar, result, created_at
 */
public class TradeStrategyHistoryPipelineDao {

    // ========= Configuration =========

    /** Nombre maximal de tentatives (deadlock retry) */
    private static final int MAX_RETRIES = 4;

    /** Backoff initial (ms) pour le retry en cas de deadlock */
    private static final long BASE_BACKOFF_MS = 150L;

    /** Clé d’advisory lock PostgreSQL (bigint), stable via CRC32 */
    private static final long ADVISORY_LOCK_KEY = crc32ToLong("trade_strategy_history_pipeline");

    /**
     * Sémaphore JVM optionnelle pour lisser le parallélisme (désactivée par défaut)
     */
    private static volatile Semaphore OPTIONAL_APP_SEMAPHORE = null;

    /**
     * Permet d'activer le sémaphore JVM (bonus).
     * Ex: configureSemaphore(2) limite à 2 pipelines concurrents.
     */
    public static void configureSemaphore(int permits) {
        OPTIONAL_APP_SEMAPHORE = (permits > 0 && permits < Integer.MAX_VALUE) ? new Semaphore(permits, true) : null;
    }

    // ========= Nouvelles APIs (avec rapport) =========

    /**
     * Nouvelle API « avec rapport » :
     * Charge un lot complet (CREATE TEMP STAGING si besoin → COPY → MERGE) dans une
     * transaction,
     * loggue un comptage BEFORE/AFTER et renvoie un TradeDaoBatchReport.
     *
     * @return TradeDaoBatchReport(totalRowsAffected, batchCount=1, autoCommit=<état
     *         initial>)
     */
    public TradeDaoBatchReport loadBatchWithReport(
            List<TradeStrategyHistoryFull> batch,
            ProcessLogger LOGGER,
            String STUDY_ID,
            String INSTANCE_NAME) throws Exception {

        if (batch == null || batch.isEmpty()) {
            return new TradeDaoBatchReport(0L, 0, true);
        }

        final String instanceId = String.valueOf(System.identityHashCode(this));
        LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME,
                "[PIPELINE-DAO] Trade Strategy History Pipeline", "Début Load Batch");

        // Bonus : lisser le parallélisme côté JVM
        if (OPTIONAL_APP_SEMAPHORE != null) {
            OPTIONAL_APP_SEMAPHORE.acquire();
        }

        int attempt = 0;
        Boolean initialAutoCommit = null; // état de l'autoCommit AVANT beginBulkSession
        long merged = 0L; // lignes affectées par le MERGE
        int batchCount = 1; // pipeline = 1 vague (COPY+MERGE)

        try {
            while (true) {
                attempt++;
                try (Connection conn = DatabaseManager.getConnection()) {
                    // capture de l'état d'auto-commit initial pour le rapport
                    initialAutoCommit = conn.getAutoCommit();

                    // mesure "BEFORE" pour un delta robuste (utile si driver retourne NO_INFO
                    // ailleurs)
                    long before = countHistory(conn);

                    beginBulkSession(conn);
                    createTempStagingIfNeeded(conn); // TEMP par session

                    long copied = copyIntoStaging(conn, batch, STUDY_ID, INSTANCE_NAME);
                    merged = mergeFromStagingWithLock(conn, STUDY_ID, INSTANCE_NAME);

                    endBulkSessionCommit(conn);

                    // mesure "AFTER"
                    long after = countHistory(conn);
                    long delta = Math.max(0L, after - before);

                    // Logs explicites
                    LOGGER.log(String.format("[HISTORY/PIPELINE] copied=%,d, merged=%,d, batches=%d", copied, merged,
                            batchCount));
                    LOGGER.log(String.format("[HISTORY/PIPELINE] COUNT before=%,d → after=%,d (delta=+%,d)", before,
                            after, delta));

                    return new TradeDaoBatchReport(merged, batchCount, Boolean.TRUE.equals(initialAutoCommit));
                } catch (SQLException | IOException e) {
                    // deadlock → SQLSTATE 40P01 → backoff + retry
                    if (isDeadlock(e) && attempt < MAX_RETRIES) {
                        long sleep = backoff(attempt);
                        LOGGER.log("[WARN][HISTORY/PIPELINE] Deadlock détecté, retry " + attempt + "/" + MAX_RETRIES
                                + " après " + sleep + "ms : " + e.getMessage());
                        Thread.sleep(sleep);
                        continue; // retry
                    }
                    LOGGER.log("[ERROR][HISTORY/PIPELINE] " + e.getMessage());
                    throw e;
                }
            }
        } finally {
            if (OPTIONAL_APP_SEMAPHORE != null) {
                OPTIONAL_APP_SEMAPHORE.release();
            }
            LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME,
                    "[PIPELINE-DAO] Trade Strategy History Pipeline", "Fin Load Batch");
        }
    }

    /**
     * API historique conservée pour compatibilité :
     * Délègue à loadBatchWithReport(...) et retourne uniquement totalRowsAffected.
     */
    public long loadBatch(
            List<TradeStrategyHistoryFull> batch,
            ProcessLogger LOGGER,
            String STUDY_ID,
            String INSTANCE_NAME) throws Exception {
        TradeDaoBatchReport rep = loadBatchWithReport(batch, LOGGER, STUDY_ID, INSTANCE_NAME);
        return rep.totalRowsAffected;
    }

    // ========= Session / staging =========

    /** Démarre la transaction + durabilité relâchée (session) */
    public void beginBulkSession(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try (Statement st = conn.createStatement()) {
            st.execute("SET LOCAL synchronous_commit = OFF");
        }
    }

    /** Commit + restaure l’autoCommit précédent */
    public void endBulkSessionCommit(Connection conn) throws SQLException {
        boolean prev = true;
        try {
            prev = conn.getAutoCommit();
            conn.commit();
        } finally {
            conn.setAutoCommit(prev);
        }
    }

    /** TEMP staging par session (IF NOT EXISTS) */
    public void createTempStagingIfNeeded(Connection conn) throws SQLException {
        final String ddl = """
                CREATE TEMP TABLE IF NOT EXISTS trade_strategy_history_stage (
                    instrument_id    INT NOT NULL,
                    timeframe_id     INT NOT NULL,
                    renko_size_id    INT NOT NULL,
                    pattern_id       INT NOT NULL,
                    direction_id     INT NOT NULL,
                    takeprofit_id    INT NOT NULL,
                    stoploss_id      INT NOT NULL,
                    timestamp_entry  BIGINT NOT NULL,
                    date_time_entry  TEXT NOT NULL,
                    entry_price      DOUBLE PRECISION NOT NULL,
                    timestamp_exit   BIGINT,
                    date_time_exit   TEXT,
                    exit_price       DOUBLE PRECISION,
                    tp_price         DOUBLE PRECISION,
                    sl_price         DOUBLE PRECISION,
                    count_bar        INTEGER,
                    result           TEXT,
                    created_at       TEXT
                ) ON COMMIT PRESERVE ROWS
                """;
        try (Statement st = conn.createStatement()) {
            st.execute(ddl);
        }
    }

    /**
     * Vide la staging (utile si on réutilise la même session pour plusieurs vagues)
     */
    public void truncateStaging(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("TRUNCATE trade_strategy_history_stage");
        }
    }

    // ========= COPY =========

    /**
     * COPY CSV en staging.
     * 
     * @return nombre de lignes copiées
     */
    public long copyIntoStaging(Connection conn,
            List<TradeStrategyHistoryFull> batch,
            String STUDY_ID,
            String INSTANCE_NAME) throws SQLException, IOException {

        CoreLogger LOGGER = CoreLogger.getInstance(STUDY_ID, INSTANCE_NAME);

        final String copySql = "COPY trade_strategy_history_stage (" +
                "instrument_id,timeframe_id,renko_size_id,pattern_id,direction_id,takeprofit_id,stoploss_id," +
                "timestamp_entry,date_time_entry,entry_price," +
                "timestamp_exit,date_time_exit,exit_price," +
                "tp_price,sl_price,count_bar,result,created_at" +
                ") FROM STDIN (FORMAT csv)";

        // Build CSV (en mémoire – simple et rapide ; passer à un stream si besoin)
        StringBuilder sb = new StringBuilder(Math.max(1, batch.size()) * 192);

        for (TradeStrategyHistoryFull t : batch) {
            Integer instrumentId = getIdOrNull(t.getInstrument());
            Integer timeframeId = getIdOrNull(t.getTimeframe());
            Integer renkoId = getIdOrNull(t.getRenkoSize());
            Integer patternId = getIdOrNull(t.getPattern());
            Integer dirId = getIdOrNull(t.getDirection());
            Integer tpId = getIdOrNull(t.getTakeprofit());
            Integer slId = getIdOrNull(t.getStoploss());

            sb.append(csv(instrumentId)).append(',')
                    .append(csv(timeframeId)).append(',')
                    .append(csv(renkoId)).append(',')
                    .append(csv(patternId)).append(',')
                    .append(csv(dirId)).append(',')
                    .append(csv(tpId)).append(',')
                    .append(csv(slId)).append(',')

                    .append(csv(t.getTimestampEntry())).append(',')
                    .append(csv(t.getDateTimeEntry())).append(',')
                    .append(csv(t.getEntryPrice())).append(',')

                    .append(csv(t.getTimestampExit())).append(',')
                    .append(csv(t.getDateTimeExit())).append(',')
                    .append(csv(t.getExitPrice())).append(',')

                    .append(csv(t.getTpPrice())).append(',')
                    .append(csv(t.getSlPrice())).append(',')
                    .append(csv(t.getCountBar())).append(',')
                    .append(csv(t.getResult())).append(',')
                    .append(csv(t.getCreatedAt()))
                    .append('\n');
        }

        Reader reader = new StringReader(sb.toString());
        PGConnection pg = conn.unwrap(PGConnection.class);
        CopyManager cm = pg.getCopyAPI();
        long rows = cm.copyIn(copySql, reader);

        LOGGER.log("[STAGING/HISTORY] COPY → " + rows + " lignes dans trade_strategy_history_stage");
        return rows;
    }

    // ========= MERGE (avec advisory lock + retry) =========

    /**
     * MERGE staging -> table finale sous advisory lock global.
     * - Résolution du pattern_strategy_id par JOIN sur pattern_strategy
     * - Upsert par UNIQUE(pattern_strategy_id, timestamp_entry, entry_price)
     * - Sur conflit, mise à jour des colonnes "de sortie".
     *
     * @return nombre de lignes affectées (INSERT + UPDATE) côté table finale
     */
    public long mergeFromStagingWithLock(Connection conn,
            String STUDY_ID,
            String INSTANCE_NAME) throws SQLException {
        CoreLogger LOGGER = CoreLogger.getInstance(STUDY_ID, INSTANCE_NAME);

        acquireAdvisoryLock(conn); // verrou global inter-sessions pendant le MERGE
        try {
            final String mergeSql = """
                    INSERT INTO trade_strategy_history (
                        pattern_strategy_id,
                        timestamp_entry, date_time_entry, entry_price,
                        timestamp_exit, date_time_exit, exit_price,
                        tp_price, sl_price, count_bar, result, created_at
                    )
                    SELECT
                        ps.id_pattern_strategy,
                        s.timestamp_entry, s.date_time_entry, s.entry_price,
                        s.timestamp_exit, s.date_time_exit, s.exit_price,
                        s.tp_price, s.sl_price, s.count_bar, s.result,
                        COALESCE(s.created_at, TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS'))
                    FROM trade_strategy_history_stage s
                    JOIN pattern_strategy ps
                      ON ps.instrument_id = s.instrument_id
                     AND ps.timeframe_id  = s.timeframe_id
                     AND ps.renko_size_id = s.renko_size_id
                     AND ps.pattern_id    = s.pattern_id
                     AND ps.direction_id  = s.direction_id
                     AND ps.takeprofit_id = s.takeprofit_id
                     AND ps.stoploss_id   = s.stoploss_id
                    ON CONFLICT (pattern_strategy_id, timestamp_entry, entry_price)
                    DO UPDATE SET
                        timestamp_exit = EXCLUDED.timestamp_exit,
                        date_time_exit = EXCLUDED.date_time_exit,
                        exit_price     = EXCLUDED.exit_price,
                        tp_price       = EXCLUDED.tp_price,
                        sl_price       = EXCLUDED.sl_price,
                        count_bar      = EXCLUDED.count_bar,
                        result         = EXCLUDED.result
                    """;

            long affected;
            try (Statement st = conn.createStatement()) {
                affected = st.executeUpdate(mergeSql);
            }

            // nettoyage staging pour la session (si plusieurs vagues)
            try (Statement st = conn.createStatement()) {
                st.execute("TRUNCATE trade_strategy_history_stage");
            }

            LOGGER.log("[MERGE/HISTORY] staging → trade_strategy_history : " + affected + " lignes affectées");
            return affected;
        } finally {
            releaseAdvisoryLock(conn);
        }
    }

    // ========= Advisory lock helpers =========

    private void acquireAdvisoryLock(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT pg_advisory_lock(?)")) {
            ps.setLong(1, ADVISORY_LOCK_KEY);
            ps.execute();
        }
    }

    private void releaseAdvisoryLock(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT pg_advisory_unlock(?)")) {
            ps.setLong(1, ADVISORY_LOCK_KEY);
            ps.execute();
        }
    }

    // ========= Utils =========

    /** CRC32 stable -> long signé (0..2^32-1 mappé dans un long Java) */
    private static long crc32ToLong(String s) {
        CRC32 crc = new CRC32();
        crc.update(Objects.requireNonNull(s).getBytes());
        // valeur sur 32 bits non signés, portée dans un long Java
        return crc.getValue();
    }

    /**
     * Détermine si l’exception (ou cause) correspond à un deadlock PostgreSQL
     * (SQLSTATE 40P01)
     */
    private static boolean isDeadlock(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof SQLException se) {
                if ("40P01".equals(se.getSQLState()))
                    return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    /** Backoff exponentiel simple avec un peu de jitter */
    private static long backoff(int attempt) {
        long base = (long) (BASE_BACKOFF_MS * Math.pow(2, attempt - 1));
        long jitter = (long) (Math.random() * 100.0);
        return base + jitter;
    }

    /** CSV light (RFC4180-like) pour COPY FROM STDIN */
    private static String csv(Object v) {
        if (v == null)
            return "";
        String s = String.valueOf(v);
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (s.contains("\""))
            s = s.replace("\"", "\"\"");
        return needQuotes ? ("\"" + s + "\"") : s;
    }

    /** Récupère l’ID null-safe depuis les objets de dimension */
    private static Integer getIdOrNull(Object o) {
        if (o == null)
            return null;
        if (o instanceof Instrument i)
            return i.getId();
        if (o instanceof Timeframe tf)
            return tf.getId();
        if (o instanceof RenkoSize r)
            return r.getId();
        if (o instanceof Pattern p)
            return p.getId();
        if (o instanceof Direction d)
            return d.getId();
        if (o instanceof TakeProfit tp)
            return tp.getId();
        if (o instanceof StopLoss sl)
            return sl.getId();
        return null;
    }

    /** Compte des lignes dans trade_strategy_history (outil de contrôle) */
    public long countHistory(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM trade_strategy_history")) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
