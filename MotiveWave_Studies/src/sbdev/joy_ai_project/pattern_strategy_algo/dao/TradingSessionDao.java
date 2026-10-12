package sbdev.joy_ai_project.pattern_strategy_algo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.monitoralgo.core.database.DatabaseAlgoManager;

import sbdev.joy_ai_project.pattern_strategy_algo.model.TradingSessionState;
import sbdev.joy_ai_project.pattern_strategy_algo.util.Helper;

/**
 * DAO pour la table trading_session.
 *
 * Responsabilités : - Créer/récupérer la session du jour en appliquant
 * l’unicité sur : (session_date, max_win_threshold, max_loss_threshold,
 * max_successive_loss_threshold, successive_loss_enabled) - Mettre à jour les
 * compteurs (PnL, nb trades, TP/SL, successive_loss_count, timestamps)
 *
 * Remarque : Les noms de champs Java sont alignés sur la BDD : maxWinThreshold,
 * maxLossThreshold, maxSuccessiveLossThreshold, successiveLossCount, …
 */
public class TradingSessionDao {

    /**
     * UPSERT de la session du jour via la clé fonctionnelle. Si existante : on la
     * réutilise et on rafraîchit updated_at. Si nouvelle : on l’insère avec les
     * compteurs à 0. Puis on remplit l’objet TradingSessionState.
     */
    public void upsertAndLoad(TradingSessionState ss) {
        final String sql = """
                  INSERT INTO trading_session (
                    session_date, pnl_session, trade_count, count_tp, count_sl, successive_loss_count,
                    max_win_threshold, max_loss_threshold, max_successive_loss_threshold, successive_loss_enabled,
                    start_time, end_time, updated_at, created_at
                  )
                  VALUES (?, 0.0, 0, 0, 0, 0, ?, ?, ?, ?, ?, NULL, ?, ?)
                  ON CONFLICT (session_date, max_win_threshold, max_loss_threshold, max_successive_loss_threshold, successive_loss_enabled)
                  DO UPDATE SET updated_at = EXCLUDED.updated_at
                  RETURNING id_session, pnl_session, trade_count, count_tp, count_sl, successive_loss_count, start_time, end_time, created_at, updated_at;
                """;

        try (Connection conn = DatabaseAlgoManager.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            final String now = Helper.isoNow();

            pst.setString(1, ss.sessionDate);
            pst.setInt(2, ss.maxWinThreshold);
            pst.setInt(3, ss.maxLossThreshold);
            pst.setInt(4, ss.maxSuccessiveLossThreshold);
            pst.setBoolean(5, ss.successiveLossEnabled);
            pst.setString(6, ss.startTime);
            pst.setString(7, now);
            pst.setString(8, now);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    ss.id = rs.getLong("id_session");
                    ss.pnlSession = rs.getDouble("pnl_session");
                    ss.tradeCount = rs.getInt("trade_count");
                    ss.countTp = rs.getInt("count_tp");
                    ss.countSl = rs.getInt("count_sl");
                    ss.successiveLossCount = rs.getInt("successive_loss_count");
                    ss.createdAt = rs.getString("created_at");
                    ss.updatedAt = rs.getString("updated_at");
                    // start_time / end_time sont renvoyés mais on garde ss.startTime/ss.endTime
                    // côté modèle si tu préfères.
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("upsertAndLoad(trading_session) failed: " + e.getMessage(), e);
        }
    }

    /**
     * Mise à jour des compteurs de session + timestamps (nullable pour end_time).
     */
    public void update(TradingSessionState ss) {
        if (ss == null || ss.id <= 0)
            return;

        final String sql = """
                  UPDATE trading_session SET
                    pnl_session = ?,
                    trade_count = ?,
                    count_tp = ?,
                    count_sl = ?,
                    successive_loss_count = ?,
                    start_time = COALESCE(start_time, ?),
                    end_time = ?,
                    updated_at = ?
                  WHERE id_session = ?;
                """;

        try (Connection conn = DatabaseAlgoManager.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            final String now = Helper.isoNow();
            pst.setDouble(1, ss.pnlSession);
            pst.setInt(2, ss.tradeCount);
            pst.setInt(3, ss.countTp);
            pst.setInt(4, ss.countSl);
            pst.setInt(5, ss.successiveLossCount);
            pst.setString(6, ss.startTime);
            pst.setString(7, ss.endTime); // peut rester null tant que session ouverte
            pst.setString(8, now);
            pst.setLong(9, ss.id);

            pst.executeUpdate();
            ss.updatedAt = now;
        } catch (Exception e) {
            throw new RuntimeException("update(trading_session) failed: " + e.getMessage(), e);
        }
    }
}
