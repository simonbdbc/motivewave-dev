package com.monitoralgo.core.model;

/**
 * Modèle mappé 1–1 avec la table trading_session.
 *
 * DDL de référence :
 * - id_session BIGSERIAL PRIMARY KEY
 * - session_date TEXT NOT NULL
 * - pnl_session DOUBLE PRECISION
 * - trade_count INT
 * - count_tp INT
 * - count_sl INT
 * - successive_loss_count INT
 * - max_win_threshold INT NOT NULL
 * - max_loss_threshold INT NOT NULL
 * - max_successive_loss_threshold INT NOT NULL
 * - successive_loss_enabled BOOLEAN NOT NULL
 * - start_time TEXT
 * - end_time TEXT
 * - updated_at TEXT
 * - created_at TEXT
 * - UNIQUE(session_date, max_win_threshold, max_loss_threshold,
 * max_successive_loss_threshold, successive_loss_enabled)
 */
public class TradingSession {

    // --- Identité ---
    private Long idSession; // BIGSERIAL → Long (nullable avant insert)

    // --- Clé logique / infos de session ---
    private String sessionDate; // NOT NULL (ex: "2025-08-13")
    private int maxWinThreshold; // NOT NULL
    private int maxLossThreshold; // NOT NULL
    private int maxSuccessiveLossThreshold; // NOT NULL
    private boolean successiveLossEnabled; // NOT NULL

    // --- Stats (nullable) ---
    private Double pnlSession; // PnL de la session
    private Integer tradeCount;
    private Integer countTp;
    private Integer countSl;
    private Integer successiveLossCount;

    // --- Timestamps (gérés applicativement, en TEXT) ---
    private String startTime; // Peut être null (avant première ouverture)
    private String endTime; // Peut être null (avant fermeture)
    private String updatedAt;
    private String createdAt;

    public TradingSession() {
    }

    /**
     * Constructeur COMPLET (lecture BDD).
     */
    public TradingSession(
            Long idSession,
            String sessionDate,
            Double pnlSession,
            Integer tradeCount,
            Integer countTp,
            Integer countSl,
            Integer successiveLossCount,
            int maxWinThreshold,
            int maxLossThreshold,
            int maxSuccessiveLossThreshold,
            boolean successiveLossEnabled,
            String startTime,
            String endTime,
            String updatedAt,
            String createdAt) {
        this.idSession = idSession;
        this.sessionDate = sessionDate;
        this.pnlSession = pnlSession;
        this.tradeCount = tradeCount;
        this.countTp = countTp;
        this.countSl = countSl;
        this.successiveLossCount = successiveLossCount;
        this.maxWinThreshold = maxWinThreshold;
        this.maxLossThreshold = maxLossThreshold;
        this.maxSuccessiveLossThreshold = maxSuccessiveLossThreshold;
        this.successiveLossEnabled = successiveLossEnabled;
        this.startTime = startTime;
        this.endTime = endTime;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
    }

    /**
     * Constructeur SANS id (pour INSERT).
     * Les champs NOT NULL sont exigés ici en paramètres primitifs/non-null.
     */
    public TradingSession(
            String sessionDate,
            int maxWinThreshold,
            int maxLossThreshold,
            int maxSuccessiveLossThreshold,
            boolean successiveLossEnabled,
            Double pnlSession,
            Integer tradeCount,
            Integer countTp,
            Integer countSl,
            Integer successiveLossCount,
            String startTime,
            String endTime,
            String updatedAt,
            String createdAt) {
        this.sessionDate = sessionDate;
        this.maxWinThreshold = maxWinThreshold;
        this.maxLossThreshold = maxLossThreshold;
        this.maxSuccessiveLossThreshold = maxSuccessiveLossThreshold;
        this.successiveLossEnabled = successiveLossEnabled;
        this.pnlSession = pnlSession;
        this.tradeCount = tradeCount;
        this.countTp = countTp;
        this.countSl = countSl;
        this.successiveLossCount = successiveLossCount;
        this.startTime = startTime;
        this.endTime = endTime;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
    }

    // --- Getters & Setters ---

    public Long getIdSession() {
        return idSession;
    }

    public void setIdSession(Long idSession) {
        this.idSession = idSession;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Double getPnlSession() {
        return pnlSession;
    }

    public void setPnlSession(Double pnlSession) {
        this.pnlSession = pnlSession;
    }

    public Integer getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(Integer tradeCount) {
        this.tradeCount = tradeCount;
    }

    public Integer getCountTp() {
        return countTp;
    }

    public void setCountTp(Integer countTp) {
        this.countTp = countTp;
    }

    public Integer getCountSl() {
        return countSl;
    }

    public void setCountSl(Integer countSl) {
        this.countSl = countSl;
    }

    public Integer getSuccessiveLossCount() {
        return successiveLossCount;
    }

    public void setSuccessiveLossCount(Integer successiveLossCount) {
        this.successiveLossCount = successiveLossCount;
    }

    public int getMaxWinThreshold() {
        return maxWinThreshold;
    }

    public void setMaxWinThreshold(int maxWinThreshold) {
        this.maxWinThreshold = maxWinThreshold;
    }

    public int getMaxLossThreshold() {
        return maxLossThreshold;
    }

    public void setMaxLossThreshold(int maxLossThreshold) {
        this.maxLossThreshold = maxLossThreshold;
    }

    public int getMaxSuccessiveLossThreshold() {
        return maxSuccessiveLossThreshold;
    }

    public void setMaxSuccessiveLossThreshold(int maxSuccessiveLossThreshold) {
        this.maxSuccessiveLossThreshold = maxSuccessiveLossThreshold;
    }

    public boolean isSuccessiveLossEnabled() {
        return successiveLossEnabled;
    }

    public void setSuccessiveLossEnabled(boolean successiveLossEnabled) {
        this.successiveLossEnabled = successiveLossEnabled;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
