// monitoralgo/algo/dao/PatternStrategyStatsDao.java
package com.monitoralgo.core.dao;

import com.monitoralgo.core.database.DatabaseManager;
// import com.monitoralgo.core.util.CoreLogger;
import com.monitoralgo.core.util.logger.ProcessLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;

/**
 * Met à jour en masse les statistiques de la table pattern_strategy
 * à partir des données de trade_strategy_history.
 *
 * Hypothèses:
 * - La fonction Postgres compute_score_brut(...) existe (vérifiée au
 * lancement).
 * - Schéma pattern_strategy conforme à la DDL fournie (stats_updated_at
 * présent).
 * - trade_strategy_history contient tp_price, sl_price, count_bar, result,
 * timestamp_entry, ...
 */
public class PatternStrategyStatsDao {

    /**
     * Vérifie que la fonction compute_score_brut est présente (sinon on stoppe).
     */
    public static void checkComputeScoreBrutPresent(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM pg_proc WHERE proname = 'compute_score_brut'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new IllegalStateException("La fonction compute_score_brut n'est pas présente en base !");
                }
            }
        }
    }

    /**
     * Met à jour les stats de pattern_strategy depuis trade_strategy_history via un
     * UPDATE SQL massif.
     * - Met à jour : count, last_timestamp_entry, last_date_time_entry,
     * average_bar, count_tp, count_sl,
     * delta, winrate, profit_factor, score, stats_updated_at.
     * - Ne touche PAS à created_at.
     *
     * @param STUDY_ID      identifiant d'étude pour le logger
     * @param INSTANCE_NAME nom d'instance pour le logger
     * @throws SQLException en cas d'erreur SQL
     */
    public void updatePatternStrategyStatsFull(ProcessLogger LOGGER, String STUDY_ID, String INSTANCE_NAME)
            throws SQLException {
        // CoreLogger LOGGER = CoreLogger.getInstance(STUDY_ID, INSTANCE_NAME);
        final String instanceId = String.valueOf(System.identityHashCode(this));
        LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "[DAO] Pattern Strategy Stats",
                "Début Update PatternStrategy Stats");

        int affectedRows = 0;

        // IMPORTANT :
        // - stats_updated_at remplace updated_at.
        // - timestamp de référence côté history = timestamp_entry.
        // - average_bar = moyenne arrondie des count_bar non NULL.
        final String updateSql = """
                    UPDATE pattern_strategy p
                    SET
                        count                = stats.count_total,
                        last_timestamp_entry = stats.last_timestamp_entry,
                        last_date_time_entry = stats.last_date_time_entry,
                        average_bar          = stats.average_bar,
                        count_tp             = stats.count_tp,
                        count_sl             = stats.count_sl,
                        delta                = stats.count_tp - stats.count_sl,
                        winrate              = stats.winrate,
                        profit_factor        = stats.profit_factor,
                        score                = stats.score,
                        stats_updated_at     = TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS')
                    FROM (
                        SELECT
                            ts.pattern_strategy_id,
                            COUNT(*) AS count_total,

                            -- dernier timestamp d'entrée (en ms)
                            MAX(ts.timestamp_entry) AS last_timestamp_entry,

                            -- formatage humain du dernier timestamp d'entrée
                            TO_CHAR(TO_TIMESTAMP(MAX(ts.timestamp_entry) / 1000), 'DD-MM-YYYY HH24:MI:SS') AS last_date_time_entry,

                            -- moyenne des count_bar NON NULL → arrondie ; NULL si aucune valeur
                            ROUND(AVG(ts.count_bar) FILTER (WHERE ts.count_bar IS NOT NULL), 0)::INTEGER AS average_bar,

                            -- compte TP / SL
                            SUM(CASE WHEN ts.result = 'TP' THEN 1 ELSE 0 END) AS count_tp,
                            SUM(CASE WHEN ts.result = 'SL' THEN 1 ELSE 0 END) AS count_sl,

                            -- winrate en texte (ex: "54.17%") sur trades clos (TP/SL)
                            CASE
                                WHEN SUM(CASE WHEN ts.result IN ('TP','SL') THEN 1 ELSE 0 END) > 0
                                     THEN ROUND(
                                             (100.0 * SUM(CASE WHEN ts.result = 'TP' THEN 1 ELSE 0 END))
                                             / NULLIF(SUM(CASE WHEN ts.result IN ('TP','SL') THEN 1 ELSE 0 END), 0)::numeric
                                          , 2
                                          )::TEXT || '%%'
                                ELSE 'N/A'
                            END AS winrate,

                            -- profit_factor borné à 5.0 (TP sum / |SL sum|)
                            CASE
                                WHEN SUM(CASE WHEN ts.result = 'SL' THEN 1 ELSE 0 END) > 0
                                     THEN ROUND(
                                             LEAST(5.0,
                                                 (SUM(CASE WHEN ts.result = 'TP' THEN ts.tp_price ELSE 0 END))
                                                 / NULLIF(ABS(SUM(CASE WHEN ts.result = 'SL' THEN ts.sl_price ELSE 0 END)), 0)
                                             )::numeric
                                          , 2)
                                WHEN SUM(CASE WHEN ts.result = 'TP' THEN 1 ELSE 0 END) > 0
                                     THEN 5.0
                                ELSE 0.0
                            END AS profit_factor,

                            -- score via fonction PostgreSQL custom
                            compute_score_brut(
                                COUNT(*)::integer,
                                SUM(CASE WHEN ts.result = 'TP' THEN 1 ELSE 0 END)::integer,
                                SUM(CASE WHEN ts.result = 'SL' THEN 1 ELSE 0 END)::integer,
                                CASE
                                    WHEN SUM(CASE WHEN ts.result = 'SL' THEN 1 ELSE 0 END) > 0
                                         THEN LEAST(5.0,
                                             (SUM(CASE WHEN ts.result = 'TP' THEN ts.tp_price ELSE 0 END))
                                             / NULLIF(ABS(SUM(CASE WHEN ts.result = 'SL' THEN ts.sl_price ELSE 0 END)), 0)
                                         )
                                    WHEN SUM(CASE WHEN ts.result = 'TP' THEN 1 ELSE 0 END) > 0
                                         THEN 5.0
                                    ELSE 0.0
                                END
                            ) AS score
                        FROM trade_strategy_history ts
                        GROUP BY ts.pattern_strategy_id
                    ) stats
                    WHERE p.id_pattern_strategy = stats.pattern_strategy_id;
                """;

        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement()) {

            // 1) Sanity check : fonction compute_score_brut présente
            checkComputeScoreBrutPresent(conn);

            // 2) Exécution de l'UPDATE massif
            LOGGER.log("[RUN] Mise à jour des statistiques pattern_strategy...");
            affectedRows = stmt.executeUpdate(updateSql);
            LOGGER.log("✔ Nb de stratégies mises à jour : " + affectedRows);

        } catch (SQLException e) {
            // Log complet + propagation
            LOGGER.log("[ERROR] Update stats échoué: " + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            LOGGER.log(sw.toString());
            throw e;
        } finally {
            LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "[DAO] Pattern Strategy Stats",
                    "Fin Update PatternStrategy Stats");
        }
    }
}
