package sbdev.joy_ai_project.pattern_strategy_algo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.monitoralgo.core.database.DatabaseAlgoManager;

import sbdev.joy_ai_project.pattern_strategy_algo.util.Helper;

/**
 * DAO pour : - trade_strategy_live (insert d’un trade CLOS avec RETURNING
 * id_trade) - trading_session_trade_link (lien id_session ↔ id_trade ↔
 * id_pattern_strategy_finding_live)
 *
 * Convention : on persiste le trade une fois CLOS (TP/SL/Close). L’insert
 * inclut donc les champs d’entrée ET de sortie.
 */
public class TradeStrategyLiveDao {

    /** Insert d’un trade clôturé (entrée+sortie) */
    private static final String INSERT_TRADE_SQL = """
              INSERT INTO trade_strategy_live (
                pattern_strategy_id,
                timestamp_entry, date_time_entry, entry_price,
                timestamp_exit,  date_time_exit,  exit_price,
                tp_price, sl_price, result, updated_at
              )
              VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
              RETURNING id_trade;
            """;

    /**
     * Insert du lien session ↔ trade ↔ pattern_signal_live (idempotent via
     * UNIQUE(id_trade))
     */
    private static final String INSERT_LINK_SQL = """
              INSERT INTO trading_session_trade_link (
                id_session, id_trade, id_pattern_strategy_finding_live, created_at
              )
              VALUES (?, ?, ?, ?)
              ON CONFLICT (id_trade) DO NOTHING;
            """;

    /**
     * Insère un trade CLÔTURÉ dans trade_strategy_live et retourne son id.
     */
    public long insertClosedTrade(long patternStrategyId, long tsEntry, String dtEntry, double entryPrice, long tsExit,
            String dtExit, double exitPrice, double tpPrice, double slPrice, String result) {
        try (Connection conn = DatabaseAlgoManager.getConnection();
                PreparedStatement pst = conn.prepareStatement(INSERT_TRADE_SQL)) {

            final String now = Helper.isoNow();

            pst.setLong(1, patternStrategyId);
            pst.setLong(2, tsEntry);
            pst.setString(3, dtEntry);
            pst.setDouble(4, entryPrice);

            pst.setLong(5, tsExit);
            pst.setString(6, dtExit);
            pst.setDouble(7, exitPrice);

            pst.setDouble(8, tpPrice);
            pst.setDouble(9, slPrice);
            pst.setString(10, result);
            pst.setString(11, now);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    return rs.getLong("id_trade");
            }
            return 0L;
        } catch (Exception e) {
            throw new RuntimeException("insertClosedTrade failed: " + e.getMessage(), e);
        }
    }

    /**
     * Crée le lien (session ↔ trade ↔ pattern_signal_live). Idempotent grâce à
     * UNIQUE(id_trade).
     */
    public void insertLink(long sessionId, long tradeId, long patternFindingId) {
        try (Connection conn = DatabaseAlgoManager.getConnection();
                PreparedStatement pst = conn.prepareStatement(INSERT_LINK_SQL)) {

            pst.setLong(1, sessionId);
            pst.setLong(2, tradeId);
            pst.setLong(3, patternFindingId);
            pst.setString(4, Helper.isoNow());
            pst.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("insertLink(trading_session_trade_link) failed: " + e.getMessage(), e);
        }
    }

    /**
     * Practical: insert trade CLOS + link dans la même méthode.
     */
    public long insertClosedTradeAndLink(long sessionId, long patternStrategyId, long tsEntry, String dtEntry,
            double entryPrice, long tsExit, String dtExit, double exitPrice, double tpPrice, double slPrice,
            String result, long patternFindingId) {
        long idTrade = insertClosedTrade(patternStrategyId, tsEntry, dtEntry, entryPrice, tsExit, dtExit, exitPrice,
                tpPrice, slPrice, result);
        if (idTrade > 0L) {
            insertLink(sessionId, idTrade, patternFindingId);
        }
        return idTrade;
    }
}
