package sbdev.joy_ai_project.pattern_strategy_algo.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Représente la session de trading active (une par jour et par jeu de
 * paramètres de risque).
 *
 * Alignement exact avec la table SQL "trading_session" : - id_session → id -
 * session_date → sessionDate (YYYY-MM-DD) - pnl_session → pnlSession -
 * trade_count → tradeCount - count_tp → countTp - count_sl → countSl -
 * successive_loss_count → successiveLossCount - max_win_threshold →
 * maxWinThreshold - max_loss_threshold → maxLossThreshold -
 * max_successive_loss_threshold → maxSuccessiveLossThreshold -
 * successive_loss_enabled → successiveLossEnabled - start_time → startTime
 * (YYYY-MM-DD HH:mm:ss) - end_time → endTime (nullable tant que session
 * ouverte) - created_at / updated_at → createdAt / updatedAt
 *
 * Contrainte d'unicité (clé fonctionnelle de session) : UNIQUE(session_date,
 * max_win_threshold, max_loss_threshold, max_successive_loss_threshold,
 * successive_loss_enabled)
 *
 * NOTE : Le champ "pnlMax" (indicateur "stop de session atteint") reste côté
 * modèle pour la logique applicative ; il n’est pas stocké tel quel en BDD.
 */
public class TradingSessionState {

    // --- Identité & horodatage ---
    /** Identifiant BDD (id_session). 0 tant que non inséré */
    public long id;
    /** Date de session "YYYY-MM-DD" (timezone applicative) */
    public String sessionDate;
    /** Heure de début de session "YYYY-MM-DD HH:mm:ss" */
    public String startTime;
    /**
     * Heure de fin de session "YYYY-MM-DD HH:mm:ss" (null tant que non clôturée)
     */
    public String endTime;
    /** created_at / updated_at (format "YYYY-MM-DD HH:mm:ss") */
    public String createdAt;
    public String updatedAt;

    // --- Agrégats de session ---
    /** PnL cumulé de la session (points ou devise selon ton référentiel) */
    public double pnlSession;
    /** Nombre de trades effectués */
    public int tradeCount;
    /** Nombre de TP */
    public int countTp;
    /** Nombre de SL */
    public int countSl;
    /** Pertes consécutives en cours */
    public int successiveLossCount;

    // --- Paramètres de risque (définissent l’unicité de la session) ---
    /** Seuil de gain max de session */
    public int maxWinThreshold;
    /** Seuil de perte max de session (valeur positive) */
    public int maxLossThreshold;
    /** Seuil de pertes consécutives max */
    public int maxSuccessiveLossThreshold;
    /** Activation du plafond de pertes consécutives */
    public boolean successiveLossEnabled;

    // --- État applicatif non stocké tel quel en BDD ---
    /** true si un plafond est atteint (gain/perte/pertes consécutives) */
    public boolean pnlMax;

    // --- Journal léger (mémoire) des trades liés à cette session ---
    public List<TradeRecord> trades = new ArrayList<>();

    // -----------------------------------------------------------------------------------------
    // CONSTRUCTEURS
    // -----------------------------------------------------------------------------------------

    /** Constructeur par défaut : initialise timestamps et compteurs à 0. */
    public TradingSessionState() {
        Date now = new Date();
        this.sessionDate = new SimpleDateFormat("yyyy-MM-dd").format(now);
        this.startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);
        this.createdAt = this.startTime;
        this.updatedAt = this.startTime;

        this.pnlSession = 0.0;
        this.tradeCount = 0;
        this.countTp = 0;
        this.countSl = 0;
        this.successiveLossCount = 0;
        this.pnlMax = false;
    }

    /**
     * Constructeur aligné avec l’existant (appel historique).
     * 
     * @param maxWin                ex- maxWin → maxWinThreshold
     * @param maxLoss               ex- maxLoss → maxLossThreshold
     * @param successiveLossEnabled actif/inactif
     * @param maxSuccessiveLoss     ex- maxSuccessiveLoss →
     *                              maxSuccessiveLossThreshold
     */
    public TradingSessionState(int maxWin, int maxLoss, boolean successiveLossEnabled, int maxSuccessiveLoss) {
        this();
        this.maxWinThreshold = maxWin;
        this.maxLossThreshold = maxLoss;
        this.maxSuccessiveLossThreshold = maxSuccessiveLoss;
        this.successiveLossEnabled = successiveLossEnabled;
    }

    /**
     * Constructeur complet aligné BDD + clé fonctionnelle (utile quand on
     * recharge).
     */
    public TradingSessionState(String sessionDate, int maxWinThreshold, int maxLossThreshold,
            int maxSuccessiveLossThreshold, boolean successiveLossEnabled, String startTime) {
        this.maxWinThreshold = maxWinThreshold;
        this.maxLossThreshold = maxLossThreshold;
        this.maxSuccessiveLossThreshold = maxSuccessiveLossThreshold;
        this.successiveLossEnabled = successiveLossEnabled;

        this.sessionDate = sessionDate;
        this.startTime = startTime != null ? startTime : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.createdAt = this.startTime;
        this.updatedAt = this.startTime;

        this.pnlSession = 0.0;
        this.tradeCount = 0;
        this.countTp = 0;
        this.countSl = 0;
        this.successiveLossCount = 0;
        this.pnlMax = false;
    }

    // -----------------------------------------------------------------------------------------
    // MÉTHODES MÉTIER
    // -----------------------------------------------------------------------------------------

    /** Ajoute un trade au journal en mémoire, met à jour compteurs + PnL. */
    public void addTrade(long tradeId, long patternFindingId, boolean isTp, double pnlDelta) {
        this.trades.add(new TradeRecord(tradeId, patternFindingId));
        this.tradeCount += 1;

        if (isTp) {
            this.countTp += 1;
            this.successiveLossCount = 0; // reset sur TP
        } else {
            this.countSl += 1;
            this.successiveLossCount += 1;
        }

        this.pnlSession += pnlDelta;
        this.updatedAt = nowStamp();
        recomputePnlMaxFlag();
    }

    /** Marque la session comme clôturée (renseigne endTime). */
    public void closeSession() {
        this.endTime = nowStamp();
        this.updatedAt = this.endTime;
    }

    /** Recalcule le flag "pnlMax" en fonction des seuils actifs. */
    public void recomputePnlMaxFlag() {
        boolean stopOnProfit = (maxWinThreshold > 0) && (pnlSession >= maxWinThreshold);
        boolean stopOnLoss = (maxLossThreshold > 0) && (pnlSession <= -Math.abs(maxLossThreshold));
        boolean stopOnSucc = successiveLossEnabled && (maxSuccessiveLossThreshold > 0)
                && (successiveLossCount >= maxSuccessiveLossThreshold);
        this.pnlMax = stopOnProfit || stopOnLoss || stopOnSucc;
    }

    /** Clé fonctionnelle d’unicité (utile pour logs). */
    public String uniqueKey() {
        return sessionDate + "|" + maxWinThreshold + "|" + maxLossThreshold + "|" + maxSuccessiveLossThreshold + "|"
                + successiveLossEnabled;
    }

    /** Timestamp "YYYY-MM-DD HH:mm:ss". */
    private static String nowStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    // -----------------------------------------------------------------------------------------
    // Classe interne : TradeRecord
    // -----------------------------------------------------------------------------------------

    /**
     * Lien (mémoire) entre un trade live et le pattern signal d’origine. En BDD ce
     * lien est matérialisé par trading_session_trade_link(id_session, id_trade,
     * id_pattern_strategy_finding_live).
     */
    public static class TradeRecord {
        public long tradeId; // trade_strategy_live.id_trade
        public long patternFindingId; // pattern_strategy_finding_live.id_pattern_strategy_finding_live

        public TradeRecord(long tradeId, long patternFindingId) {
            this.tradeId = tradeId;
            this.patternFindingId = patternFindingId;
        }

        @Override
        public String toString() {
            return "(Trade #" + tradeId + ", PatternSignal #" + patternFindingId + ")";
        }
    }

    // -----------------------------------------------------------------------------------------
    // toString
    // -----------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return "TradingSessionState{" + "id=" + id + ", sessionDate='" + sessionDate + '\'' + ", startTime='"
                + startTime + '\'' + ", endTime='" + endTime + '\'' + ", pnlSession=" + pnlSession + ", tradeCount="
                + tradeCount + ", countTp=" + countTp + ", countSl=" + countSl + ", successiveLossCount="
                + successiveLossCount + ", maxWinThreshold=" + maxWinThreshold + ", maxLossThreshold="
                + maxLossThreshold + ", maxSuccessiveLossThreshold=" + maxSuccessiveLossThreshold
                + ", successiveLossEnabled=" + successiveLossEnabled + ", pnlMax=" + pnlMax + ", updatedAt='"
                + updatedAt + '\'' + '}';
    }
}
