package com.monitoralgo.core.model;

// TradeStrategyHistory.java
// Modèle simple mappé 1–1 avec la table trade_strategy_history

public class TradeStrategyHistory {
    private Long idTrade; // GENERATED ALWAYS (BIGSERIAL)
    private Long patternStrategyId; // NOT NULL (FK)
    private Long timestampEntry; // NOT NULL
    private String dateTimeEntry; // NOT NULL
    private Double entryPrice; // NOT NULL
    private Long timestampExit; // NULL si NoExit
    private String dateTimeExit; // NULL si NoExit
    private Double exitPrice; // NULL si NoExit
    private Double tpPrice; // nullable
    private Double slPrice; // nullable
    private Integer countBar; // nullable (0 recommandé pour NoExit)
    private String result; // "TP"/"SL"/"NoExit"
    private String createdAt; // texte de timestamp (généré côté SQL)

    public TradeStrategyHistory() {
    }

    // Constructeur COMPLET (lecture BDD)
    public TradeStrategyHistory(
            Long idTrade,
            Long patternStrategyId,
            Long timestampEntry,
            String dateTimeEntry,
            Double entryPrice,
            Long timestampExit,
            String dateTimeExit,
            Double exitPrice,
            Double tpPrice,
            Double slPrice,
            Integer countBar,
            String result,
            String createdAt) {
        this.idTrade = idTrade;
        this.patternStrategyId = patternStrategyId;
        this.timestampEntry = timestampEntry;
        this.dateTimeEntry = dateTimeEntry;
        this.entryPrice = entryPrice;
        this.timestampExit = timestampExit;
        this.dateTimeExit = dateTimeExit;
        this.exitPrice = exitPrice;
        this.tpPrice = tpPrice;
        this.slPrice = slPrice;
        this.countBar = countBar;
        this.result = result;
        this.createdAt = createdAt;
    }

    // Constructeur INSERT (sans idTrade / createdAt)
    public TradeStrategyHistory(
            Long patternStrategyId,
            Long timestampEntry,
            String dateTimeEntry,
            Double entryPrice,
            Long timestampExit,
            String dateTimeExit,
            Double exitPrice,
            Double tpPrice,
            Double slPrice,
            Integer countBar,
            String result) {
        this.patternStrategyId = patternStrategyId;
        this.timestampEntry = timestampEntry;
        this.dateTimeEntry = dateTimeEntry;
        this.entryPrice = entryPrice;
        this.timestampExit = timestampExit;
        this.dateTimeExit = dateTimeExit;
        this.exitPrice = exitPrice;
        this.tpPrice = tpPrice;
        this.slPrice = slPrice;
        this.countBar = countBar;
        this.result = result;
    }

    // --- Getters / Setters ---
    public Long getIdTrade() {
        return idTrade;
    }

    public void setIdTrade(Long idTrade) {
        this.idTrade = idTrade;
    }

    public Long getPatternStrategyId() {
        return patternStrategyId;
    }

    public void setPatternStrategyId(Long patternStrategyId) {
        this.patternStrategyId = patternStrategyId;
    }

    public Long getTimestampEntry() {
        return timestampEntry;
    }

    public void setTimestampEntry(Long timestampEntry) {
        this.timestampEntry = timestampEntry;
    }

    public String getDateTimeEntry() {
        return dateTimeEntry;
    }

    public void setDateTimeEntry(String dateTimeEntry) {
        this.dateTimeEntry = dateTimeEntry;
    }

    public Double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(Double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public Long getTimestampExit() {
        return timestampExit;
    }

    public void setTimestampExit(Long timestampExit) {
        this.timestampExit = timestampExit;
    }

    public String getDateTimeExit() {
        return dateTimeExit;
    }

    public void setDateTimeExit(String dateTimeExit) {
        this.dateTimeExit = dateTimeExit;
    }

    public Double getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(Double exitPrice) {
        this.exitPrice = exitPrice;
    }

    public Double getTpPrice() {
        return tpPrice;
    }

    public void setTpPrice(Double tpPrice) {
        this.tpPrice = tpPrice;
    }

    public Double getSlPrice() {
        return slPrice;
    }

    public void setSlPrice(Double slPrice) {
        this.slPrice = slPrice;
    }

    public Integer getCountBar() {
        return countBar;
    }

    public void setCountBar(Integer countBar) {
        this.countBar = countBar;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "TradeStrategyHistory{" +
                "idTrade=" + idTrade +
                ", patternStrategyId=" + patternStrategyId +
                ", timestampEntry=" + timestampEntry +
                ", dateTimeEntry='" + dateTimeEntry + '\'' +
                ", entryPrice=" + entryPrice +
                ", timestampExit=" + timestampExit +
                ", dateTimeExit='" + dateTimeExit + '\'' +
                ", exitPrice=" + exitPrice +
                ", tpPrice=" + tpPrice +
                ", slPrice=" + slPrice +
                ", countBar=" + countBar +
                ", result='" + result + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
