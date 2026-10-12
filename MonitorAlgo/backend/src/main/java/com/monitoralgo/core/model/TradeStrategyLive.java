package com.monitoralgo.core.model;

public class TradeStrategyLive {
    private Long idTrade; // Passe en Long objet pour accepter null à l’insertion
    private Long patternStrategyId;
    private Long timestampEntry;
    private String dateTimeEntry;
    private Double entryPrice;
    private Long timestampExit;
    private String dateTimeExit;
    private Double exitPrice;
    private Double tpPrice;
    private Double slPrice;
    private String result;
    private String updatedAt;

    public TradeStrategyLive() {
    }

    // Constructeur COMPLET (lecture en BDD)
    public TradeStrategyLive(Long idTrade,
            Long patternStrategyId,
            Long timestampEntry,
            String dateTimeEntry,
            Double entryPrice,
            Long timestampExit,
            String dateTimeExit,
            Double exitPrice,
            Double tpPrice,
            Double slPrice,
            String result,
            String updatedAt) {
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
        this.result = result;
        this.updatedAt = updatedAt;
    }

    // Constructeur SANS idTrade (pour insertion)
    public TradeStrategyLive(
            Long patternStrategyId,
            Long timestampEntry,
            String dateTimeEntry,
            Double entryPrice,
            Long timestampExit,
            String dateTimeExit,
            Double exitPrice,
            Double tpPrice,
            Double slPrice,
            String result) {
        this.idTrade = null;
        this.patternStrategyId = patternStrategyId;
        this.timestampEntry = timestampEntry;
        this.dateTimeEntry = dateTimeEntry;
        this.entryPrice = entryPrice;
        this.timestampExit = timestampExit;
        this.dateTimeExit = dateTimeExit;
        this.exitPrice = exitPrice;
        this.tpPrice = tpPrice;
        this.slPrice = slPrice;
        this.result = result;
    }

    // --- Getters & Setters ---

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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
