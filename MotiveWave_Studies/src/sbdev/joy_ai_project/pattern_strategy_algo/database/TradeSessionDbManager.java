package sbdev.joy_ai_project.pattern_strategy_algo.database;

import java.text.SimpleDateFormat;
import java.util.Date;

import sbdev.joy_ai_project.pattern_strategy_algo.dao.TradeStrategyLiveDao;
import sbdev.joy_ai_project.pattern_strategy_algo.dao.TradingSessionDao;
import sbdev.joy_ai_project.pattern_strategy_algo.model.TradingSessionState;

/**
 * TradeSessionDbManager (version harmonisée)
 *
 * Objectif : NE PLUS dupliquer les accès SQL. On délègue désormais : - à
 * TradingSessionDao pour la table trading_session - à TradeStrategyLiveDao pour
 * trade_strategy_live et trading_session_trade_link
 *
 * Ce "manager" ne garde donc que des méthodes d’orchestration simples et des
 * helpers pratiques pour ton code existant.
 */
public class TradeSessionDbManager {

    private static final TradingSessionDao sessionDao = new TradingSessionDao();
    private static final TradeStrategyLiveDao tradeDao = new TradeStrategyLiveDao();

    // --------------------------------------------------------------------------
    // Orchestration session
    // --------------------------------------------------------------------------

    /**
     * Crée OU récupère (UPSERT) la session du jour pour ces paramètres, puis
     * retourne l’id de session.
     *
     * @param sessionDate                "YYYY-MM-DD"
     * @param maxWinThreshold            seuil PnL positif
     * @param maxLossThreshold           seuil PnL négatif (valeur positive ici)
     * @param maxSuccessiveLossThreshold plafond pertes consécutives
     * @param successiveLossEnabled      true/false
     * @param startTimeIfNew             timestamp si nouvelle session (YYYY-MM-DD
     *                                   HH:mm:ss)
     */
    public static long getOrCreateTradingSession(String sessionDate, int maxWinThreshold, int maxLossThreshold,
            int maxSuccessiveLossThreshold, boolean successiveLossEnabled, String startTimeIfNew) {
        // On hydrate un TradingSessionState minimal, puis on délègue à upsertAndLoad().
        TradingSessionState ss = new TradingSessionState(maxWinThreshold, maxLossThreshold, successiveLossEnabled,
                maxSuccessiveLossThreshold);
        ss.sessionDate = sessionDate;
        ss.startTime = (startTimeIfNew != null) ? startTimeIfNew : nowStamp();

        sessionDao.upsertAndLoad(ss);
        return ss.id;
    }

    /**
     * Met à jour les métriques de session (après chaque trade). Ici, on passe par
     * le DAO en ré-hydratant l’objet (id + compteurs).
     */
    public static void updateTradingSessionMetrics(long sessionId, Double pnlSession, Integer tradeCount,
            Integer countTp, Integer countSl, Integer successiveLossCount, String endTimeOrNull) {
        TradingSessionState ss = new TradingSessionState();
        ss.id = sessionId;
        if (pnlSession != null)
            ss.pnlSession = pnlSession;
        if (tradeCount != null)
            ss.tradeCount = tradeCount;
        if (countTp != null)
            ss.countTp = countTp;
        if (countSl != null)
            ss.countSl = countSl;
        if (successiveLossCount != null)
            ss.successiveLossCount = successiveLossCount;
        ss.endTime = endTimeOrNull; // peut rester null
        // startTime : laissé tel quel via COALESCE côté DAO
        sessionDao.update(ss);
    }

    // --------------------------------------------------------------------------
    // Orchestration des trades (fermeture uniquement)
    // --------------------------------------------------------------------------

    /**
     * Persist un trade CLÔTURÉ + crée le lien vers la session et le signal pattern.
     * Renvoie id_trade.
     *
     * NB: on ne gère plus l’"insert d’un trade ouvert". Le flux standard est : - on
     * clôture le trade ⇒ on insère directement l’enregistrement complet (entrée +
     * sortie) via TradeStrategyLiveDao.
     */
    public static long persistClosedTradeAndLink(long sessionId, long patternStrategyId, long tsEntry, String dtEntry,
            double entryPrice, long tsExit, String dtExit, double exitPrice, double tpPrice, double slPrice,
            String result, long patternFindingId) {
        long idTrade = tradeDao.insertClosedTrade(patternStrategyId, tsEntry, dtEntry, entryPrice, tsExit, dtExit,
                exitPrice, tpPrice, slPrice, result);
        if (idTrade > 0L) {
            tradeDao.insertLink(sessionId, idTrade, patternFindingId);
        }
        return idTrade;
    }

    // --------------------------------------------------------------------------
    // Méthodes historiques (maintenues pour compatibilité, déconseillées)
    // --------------------------------------------------------------------------

    /**
     * ⚠️ Déconseillé / legacy : Avant : insertTrade enregistrait l’"entrée".
     * Désormais on enregistre le trade une fois CLOS (insertClosedTradeAndLink).
     */
    @Deprecated
    public static long insertTradeLegacy() {
        throw new UnsupportedOperationException(
                "insertTradeLegacy() n'est plus supporté. Utilise persistClosedTradeAndLink(...) via TradeStrategyLiveDao.");
    }

    /**
     * ⚠️ Déconseillé / legacy : Avant : updateTrade complétait l’enregistrement
     * ouvert. Désormais on fait un insert 'closed' en une fois.
     */
    @Deprecated
    public static void updateTradeLegacy() {
        throw new UnsupportedOperationException(
                "updateTradeLegacy() n'est plus supporté. Utilise persistClosedTradeAndLink(...) via TradeStrategyLiveDao.");
    }

    // --------------------------------------------------------------------------
    // Utilitaire timestamp
    // --------------------------------------------------------------------------

    private static String nowStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
