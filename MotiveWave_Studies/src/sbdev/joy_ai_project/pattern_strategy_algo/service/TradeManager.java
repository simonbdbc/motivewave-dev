package sbdev.joy_ai_project.pattern_strategy_algo.service;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.List;

import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Instrument;
import com.motivewave.platform.sdk.common.TimeFrame;
import com.motivewave.platform.sdk.order_mgmt.Order;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;

import sbdev.joy_ai_project.pattern_strategy_algo.PatternStrategyAlgo;
import sbdev.joy_ai_project.pattern_strategy_algo.dao.TradingSessionDao;
import sbdev.joy_ai_project.pattern_strategy_algo.dao.TradeStrategyLiveDao;
import sbdev.joy_ai_project.pattern_strategy_algo.model.CurrentTradeState;
import sbdev.joy_ai_project.pattern_strategy_algo.model.PatternRenkoMatched;
import sbdev.joy_ai_project.pattern_strategy_algo.model.TradingSessionState;
import sbdev.joy_ai_project.pattern_strategy_algo.util.Helper;
import sbdev.joy_ai_project.pattern_strategy_algo.util.StudyLogger;

/**
 * TradeManager
 *
 * - Gère le cycle de trading (entrée, TP/SL, filet de sécurité) - Gère
 * TradingSessionState (ouverture/fermeture + compteurs) - Persistance via DAO :
 * * TradingSessionDao : upsert/load + update * TradeStrategyLiveDao : insert
 * trade + link vers pattern + session
 *
 * NB : pour éviter l'API inexistante getPrice() sur Order, on utilise les prix
 * connus (TP/SL) portés par CurrentTradeState.
 */
public class TradeManager {

    private final PatternStrategyAlgo strategy;
    private final StudyLogger logger;

    private final TradingSessionDao sessionDao = new TradingSessionDao();
    private final TradeStrategyLiveDao tradeDao = new TradeStrategyLiveDao();

    // État trading courant
    private CurrentTradeState currentTradeState;
    private Order entryOrder;
    private Order takeProfitOrder;
    private Order stopLossOrder;

    // Delay du reset de l'État trading courant sur onClosePosition
    // --- Debounce de la fermeture de position ---
    /** Indique qu'une fermeture de position a été demandée mais est en attente. */
    private volatile boolean flushPendingClear = false;
    /** Timestamp (ms) de la demande de fermeture. */
    private volatile long lastPositionClosedAt = 0L;
    /** Délai de debounce (ms). Ajuste entre 500 et 1000 suivant ton besoin. */
    private static final long MIN_CLEAR_DELAY_MS = 800L; // 0.8s (ou 1000L)

    // État session
    private TradingSessionState sessionState;

    // Signaux en attente (un seul traité à la fois)
    private final Deque<Object> pendingSignals = new ArrayDeque<>();

    public TradeManager(PatternStrategyAlgo strategy, StudyLogger logger) {
        this.strategy = strategy;
        this.logger = logger;
    }

    // ----------------------------
    // Sessions
    // ----------------------------

    /** Ouverture ou réutilisation de la session du jour (clé fonctionnelle). */
    public void onSessionStarted(OrderContext ctx, TimeFrame session) {
        // Paramètres de risque à l’instant t
        int maxWin = strategy.getSettings().getInteger("sessionMaxWin");
        int maxLoss = strategy.getSettings().getInteger("sessionMaxLoss");
        boolean succLossEnabled = strategy.getSettings().getBoolean("maxSuccessiveLossEnabled");
        int maxSuccLoss = strategy.getSettings().getInteger("maxSuccessiveLoss");

        sessionState = new TradingSessionState(maxWin, maxLoss, succLossEnabled, maxSuccLoss);

        // UPSERT : réutilise la session existante si (date + params) identiques
        sessionDao.upsertAndLoad(sessionState);

        logger.log("SESSION STARTED (id_session=" + sessionState.id + ", date=" + sessionState.sessionDate + ", maxWin="
                + sessionState.maxWinThreshold + ", maxLoss=" + sessionState.maxLossThreshold + ", succLossEnabled="
                + sessionState.successiveLossEnabled + ", maxSuccLoss=" + sessionState.maxSuccessiveLossThreshold
                + ")");
    }

    /** Clôture propre de la session (heure de fin + update BDD). */
    public void onSessionEnded(OrderContext ctx, TimeFrame session) {
        if (sessionState == null)
            return;
        sessionState.endTime = Helper.isoNow();
        sessionDao.update(sessionState);
        logger.log("SESSION ENDED (id_session=" + sessionState.id + ", pnl=" + sessionState.pnlSession + ", trades="
                + sessionState.tradeCount + ", TP=" + sessionState.countTp + ", SL=" + sessionState.countSl + ")");
        sessionState = null;
    }

    /** Reset des états d’ordres/signaux en mémoire. */
    public void resetSessionState() {
        entryOrder = null;
        takeProfitOrder = null;
        stopLossOrder = null;
        currentTradeState = null;
        pendingSignals.clear();
    }

    // ----------------------------
    // Pattern → signal
    // ----------------------------

    /** Appelé par l’algo quand un pattern est matché. */
    public void handlePatternMatched(PatternRenkoMatched match) {
        if (strategy.getEntryState() == Enums.EntryState.NONE && currentTradeState == null) {
            boolean isLong = "long".equalsIgnoreCase(match.direction);
            pendingSignals
                    .offer(isLong ? PatternStrategyAlgo.Signals.LONG_MATCH : PatternStrategyAlgo.Signals.SHORT_MATCH);

            double entryPrice = strategy.history.getRenkoPriceResAt(match.timestamp);
            double tpPts = match.bracket.tp;
            double slPts = match.bracket.sl;
            double tpPrice = isLong ? entryPrice + tpPts : entryPrice - tpPts;
            double slPrice = isLong ? entryPrice - slPts : entryPrice + slPts;

            String tradeId = String.format("%d_%s_%d", System.currentTimeMillis(), isLong ? "LONG" : "SHORT",
                    match.hashCode());

            currentTradeState = new CurrentTradeState(entryPrice, tpPrice, slPrice, tpPts, slPts, isLong, match,
                    tradeId);

            logger.log("Signal reçu: " + (isLong ? "LONG" : "SHORT") + " | entry=" + entryPrice + " tp=" + tpPrice
                    + " sl=" + slPrice + " | patternFindingId=" + match.patternFindingId + " | patternStrategyId="
                    + match.patternStrategyId);
        }
    }

    // ----------------------------
    // Cycle bar/ordre
    // ----------------------------

    /** À chaque fin de bar : tenter une entrée si un signal est en attente. */
    public void onBarClose(OrderContext ctx) {

        if (flushPendingClear && strategy.getEntryState() == Enums.EntryState.NONE && ctx.getActiveOrders().size() == 0
                && System.currentTimeMillis() - lastPositionClosedAt > MIN_CLEAR_DELAY_MS) {
            logger.log("Auto Clear: nettoyage final, purge l'état du trade passé");
            clearAllOrders();
            flushPendingClear = false;
        }

        if (!pendingSignals.isEmpty() && strategy.getEntryState() == Enums.EntryState.NONE && entryOrder == null) {
            tryEnterPosition(ctx, pendingSignals.poll());
        }

        // Filet : fermeture auto proche du close (fenêtre par défaut)
        Date now = new Date(ctx.getCurrentTime());
        if (ctx.getPositionAsFloat() != 0 && isCloseToMarketClose(now)) {
            logger.log("Auto Close: fermeture position avant close marché");
            ctx.closeAtMarket();
            clearAllOrders();
            strategy.setEntryState(Enums.EntryState.NONE);
        }
    }

    /** Vérifie les règles de risk gating et envoie l’ordre d’entrée. */
    private void tryEnterPosition(OrderContext ctx, Object signal) {
        if (sessionState == null) {
            logger.log("tryEnterPosition: aucune session active → abandon.");
            currentTradeState = null;
            return;
        }
        if (!Boolean.TRUE.equals(strategy.isStrategyActive)) {
            logger.log("tryEnterPosition: stratégie inactive.");
            return;
        }

        float position = ctx.getPositionAsFloat();

        boolean hitMax = (sessionState.pnlSession >= sessionState.maxWinThreshold)
                || (sessionState.pnlSession <= -sessionState.maxLossThreshold);
        boolean hitSuccLoss = sessionState.successiveLossEnabled
                && (sessionState.successiveLossCount >= sessionState.maxSuccessiveLossThreshold);

        if (hitMax || hitSuccLoss) {
            logger.log("Blocage entrée (Risk): pnlSession=" + sessionState.pnlSession + " (maxWin="
                    + sessionState.maxWinThreshold + ", maxLoss=" + sessionState.maxLossThreshold + "), successiveLoss="
                    + sessionState.successiveLossCount + "/" + sessionState.maxSuccessiveLossThreshold);
            currentTradeState = null;
            return;
        }

        if (position != 0 || currentTradeState == null || currentTradeState.entryPattern == null)
            return;

        Instrument instr = ctx.getInstrument();
        double tickSize = instr.getTickSize();
        double last = instr.getLastPrice();
        double maxDelta = 5 * tickSize;
        double entryRef = currentTradeState.entryPrice;

        if (Math.abs(last - entryRef) > maxDelta) {
            logger.log(String.format("Abandon trade: |Δ|=%.2f > %.2f (%.2f vs %.2f)", Math.abs(last - entryRef),
                    maxDelta, last, entryRef));
            currentTradeState = null;
            strategy.setEntryState(Enums.EntryState.NONE);
            return;
        }

        float qty = strategy.getSettings().getInteger("tradeLots") * instr.getDefaultQuantityAsFloat();
        boolean buy = currentTradeState.isLong;

        strategy.setEntryState(Enums.EntryState.WAITING_ENTRY);
        strategy.setLong(buy);
        entryOrder = buy ? ctx.createMarketOrder(Enums.OrderAction.BUY, qty)
                : ctx.createMarketOrder(Enums.OrderAction.SELL, qty);

        logger.log("ENTRY submit: " + entryOrder + " | entry=" + currentTradeState.entryPrice + " tp="
                + currentTradeState.takeProfitPrice + " (" + currentTradeState.takeProfitPoint + "pts)" + " sl="
                + currentTradeState.stopLossPrice + " (" + currentTradeState.stopLossPoint + "pts)" + " | tradeId="
                + currentTradeState.tradeId);

        ctx.submitOrders(entryOrder);
    }

    /** Filet de sécurité : ferme la position si le prix dépasse TP/SL. */
    public void securityStopIfNeeded(OrderContext ctx) {
        float position = ctx.getPositionAsFloat();
        if (position == 0 || currentTradeState == null)
            return;

        double tp = currentTradeState.takeProfitPrice;
        double sl = currentTradeState.stopLossPrice;
        double last = ctx.getInstrument().getLastPrice();

        if (strategy.isLong() && last > tp) {
            logger.log("SECURITY: TP dépassé (long) → close at market");
            ctx.closeAtMarket();
            clearAllOrders();
            strategy.setEntryState(Enums.EntryState.NONE);
            return;
        }
        if (!strategy.isLong() && last < tp) {
            logger.log("SECURITY: TP dépassé (short) → close at market");
            ctx.closeAtMarket();
            clearAllOrders();
            strategy.setEntryState(Enums.EntryState.NONE);
            return;
        }
        if (strategy.isLong() && last < sl) {
            logger.log("SECURITY: SL dépassé (long) → close at market");
            ctx.closeAtMarket();
            clearAllOrders();
            strategy.setEntryState(Enums.EntryState.NONE);
            return;
        }
        if (!strategy.isLong() && last > sl) {
            logger.log("SECURITY: SL dépassé (short) → close at market");
            ctx.closeAtMarket();
            clearAllOrders();
            strategy.setEntryState(Enums.EntryState.NONE);
            return;
        }
    }

    // ----------------------------
    // Gestion des ordres
    // ----------------------------

    public void onOrderFilled(OrderContext ctx, Order order) {
        logger.log("onOrderFilled: " + order);

        // Entrée remplie → créer TP/SL
        if (entryOrder != null && order.equals(entryOrder) && currentTradeState != null) {
            float qty = entryOrder.getQuantityAsFloat();
            Instrument instr = ctx.getInstrument();
            Enums.OrderAction exitAction = strategy.isLong() ? Enums.OrderAction.SELL : Enums.OrderAction.BUY;
            var tif = strategy.getSettings().getTIF("tif");

            float tpPrice = (float) currentTradeState.takeProfitPrice;
            float slPrice = (float) currentTradeState.stopLossPrice;

            takeProfitOrder = ctx.createLimitOrder(instr, order, exitAction, tif, qty, tpPrice);
            stopLossOrder = ctx.createStopOrder(instr, order, exitAction, tif, qty, slPrice);

            var exits = new java.util.ArrayList<Order>();
            exits.add(takeProfitOrder);
            exits.add(stopLossOrder);
            ctx.submitOrders(exits);

            logger.log(
                    "Submitted exits: " + takeProfitOrder + " TP=" + tpPrice + ", " + stopLossOrder + " SL=" + slPrice);
            return;
        }

        logger.log("takeProfitOrder : " + takeProfitOrder);
        logger.log("order : " + order);
        logger.log("currentTradeState : " + currentTradeState.toString());

        // TP touché → on connaît le prix d'exit (tp connu)
        if (takeProfitOrder != null && order.equals(takeProfitOrder)) {
            logger.log("TP hit → cancel SL, persist, update session");
            if (stopLossOrder != null)
                ctx.cancelOrders(stopLossOrder);
            persistClosedTrade(ctx, "TP",
                    currentTradeState != null ? currentTradeState.takeProfitPrice : ctx.getInstrument().getLastPrice());
            clearAllOrders();
            strategy.setEntryState(Enums.EntryState.NONE);
            return;
        }

        // SL touché → on connaît le prix d'exit (sl connu)
        if (stopLossOrder != null && order.equals(stopLossOrder)) {
            logger.log("SL hit → cancel TP, persist, update session");
            if (takeProfitOrder != null)
                ctx.cancelOrders(takeProfitOrder);
            persistClosedTrade(ctx, "SL",
                    currentTradeState != null ? currentTradeState.stopLossPrice : ctx.getInstrument().getLastPrice());
            clearAllOrders();
            strategy.setEntryState(Enums.EntryState.NONE);
            return;
        }

        clearOrder(order);
    }

    public void onOrderCancelled(OrderContext ctx, Order order) {
        logger.log("onOrderCancelled: " + order);
        clearOrder(order);
    }

    public void onPositionClosed(OrderContext ctx) {
        logger.log("onPositionClosed (manual/safety)");
        ctx.cancelOrders();
        // <-- ARMEMENT du délai à la place de clearAllOrders() immédiat
        flushPendingClear = true;
        lastPositionClosedAt = System.currentTimeMillis();
        strategy.setEntryState(Enums.EntryState.NONE);
    }

    // ----------------------------
    // Persistance via DAO
    // ----------------------------

    /**
     * Insère le trade fermé + lien avec session et pattern, puis met à jour la
     * session.
     */
    private void persistClosedTrade(OrderContext ctx, String result, double exitPrice) {
        if (sessionState == null || currentTradeState == null)
            return;

        // Mise à jour des compteurs de session en mémoire
        double signed = (currentTradeState.isLong ? 1.0 : -1.0);
        double pnlPts = (exitPrice - currentTradeState.entryPrice) * signed;
        // sessionState.pnlSession += pnlPts;
        double pnlTrade = ctx.getRealizedPnL();
        sessionState.pnlSession += pnlTrade;
        sessionState.tradeCount++;

        if ("TP".equalsIgnoreCase(result)) {
            sessionState.countTp++;
            sessionState.successiveLossCount = 0;
        } else if ("SL".equalsIgnoreCase(result)) {
            sessionState.countSl++;
            sessionState.successiveLossCount++;
        }

        // Données de trade à persister
        long tsEntry = (currentTradeState.entryPattern != null) ? currentTradeState.entryPattern.timestamp
                : System.currentTimeMillis();
        String dtEntry = (currentTradeState.entryPattern != null) ? currentTradeState.entryPattern.date
                : Helper.isoDate(tsEntry);
        long tsExit = System.currentTimeMillis();
        String dtExit = Helper.isoNow();

        long patternStrategyId = (currentTradeState.entryPattern != null)
                ? currentTradeState.entryPattern.patternStrategyId
                : 0L;
        long patternFindingId = (currentTradeState.entryPattern != null)
                ? currentTradeState.entryPattern.patternFindingId
                : 0L;

        // Insert trade + link (via DAO)
        long idTrade = tradeDao.insertClosedTradeAndLink(sessionState.id, patternStrategyId, tsEntry, dtEntry,
                currentTradeState.entryPrice, tsExit, dtExit, exitPrice, currentTradeState.takeProfitPrice,
                currentTradeState.stopLossPrice, result, patternFindingId);

        // Push des compteurs de session en base
        sessionDao.update(sessionState);

        logger.log("TRADE persisted (id_trade=" + idTrade + ", result=" + result + ", pnlPts=" + pnlPts + ", pnlTrade="
                + pnlTrade + ", pnlSession=" + sessionState.pnlSession + ", TP=" + sessionState.countTp + ", SL="
                + sessionState.countSl + ", succLoss=" + sessionState.successiveLossCount + ")");

        // Nettoyage état trade courant
        currentTradeState = null;
    }

    // ----------------------------
    // Ménage & utilitaires
    // ----------------------------

    private void clearAllOrders() {
        entryOrder = null;
        takeProfitOrder = null;
        stopLossOrder = null;
        currentTradeState = null;
    }

    private void clearOrder(Order order) {
        if (entryOrder != null && order.equals(entryOrder))
            entryOrder = null;
        if (takeProfitOrder != null && order.equals(takeProfitOrder))
            takeProfitOrder = null;
        if (stopLossOrder != null && order.equals(stopLossOrder))
            stopLossOrder = null;
    }

    // Fenêtre par défaut 02:00–22:00 (fermeture 21:55)
    @SuppressWarnings("deprecation")
    private boolean isCloseToMarketClose(Date currentDate) {
        int h = currentDate.getHours();
        int m = currentDate.getMinutes();
        return (h > 21) || (h == 21 && m >= 55);
    }
}
