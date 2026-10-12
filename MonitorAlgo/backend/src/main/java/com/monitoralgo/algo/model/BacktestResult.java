package com.monitoralgo.algo.model;

// BacktestResult.java
// Modèle de retour du moteur de backtest (utilisé par le DAO pour insérer l'historique)
public class BacktestResult {
    private Long timestampEntry;
    private String entryDate;
    private double entryPrice;

    private Long timestampExit; // nullable (NoExit => null)
    private String exitDate; // "" ou null toléré par le DAO (inséré en NULL)
    private Double exitPrice; // nullable
    private String result = "NoExit";
    private int bars = 0; // nombre de barres avant la sortie

    private double tpPrice; // niveau de prix TP (entry ± distance)
    private double slPrice; // niveau de prix SL (entry ± distance)

    public BacktestResult() {
    }

    public BacktestResult(Long timestampEntry, String entryDate, double entryPrice) {
        this.timestampEntry = timestampEntry;
        this.entryDate = entryDate;
        this.entryPrice = entryPrice;
    }

    // --- Getters / Setters ---
    public Long getTimestampEntry() {
        return timestampEntry;
    }

    public void setTimestampEntry(Long timestampEntry) {
        this.timestampEntry = timestampEntry;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public Long getTimestampExit() {
        return timestampExit;
    }

    public void setTimestampExit(Long timestampExit) {
        this.timestampExit = timestampExit;
    }

    public String getExitDate() {
        return exitDate;
    }

    public void setExitDate(String exitDate) {
        this.exitDate = exitDate;
    }

    public Double getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(Double exitPrice) {
        this.exitPrice = exitPrice;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getBars() {
        return bars;
    }

    public void setBars(int bars) {
        this.bars = bars;
    }

    public double getTpPrice() {
        return tpPrice;
    }

    public void setTpPrice(double tpPrice) {
        this.tpPrice = tpPrice;
    }

    public double getSlPrice() {
        return slPrice;
    }

    public void setSlPrice(double slPrice) {
        this.slPrice = slPrice;
    }

    @Override
    public String toString() {
        return "BacktestResult{" +
                "timestampEntry=" + timestampEntry +
                ", entryDate='" + entryDate + '\'' +
                ", entryPrice=" + entryPrice +
                ", timestampExit=" + timestampExit +
                ", exitDate='" + exitDate + '\'' +
                ", exitPrice=" + exitPrice +
                ", result='" + result + '\'' +
                ", bars=" + bars +
                ", tpPrice=" + tpPrice +
                ", slPrice=" + slPrice +
                '}';
    }
}
