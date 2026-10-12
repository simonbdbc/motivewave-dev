package com.monitoralgo.stats.model;

public class SnapshotAioByRenko {
    private Integer id;
    private String instrumentKey;
    private String instrumentName;
    private String tfScale;
    private Integer tfInterval;
    private Double renkoSize;
    private Long timestamp;
    private String dateTime;
    private Double priceRes;
    private Double priceSup;
    private String renkoColor;
    private String hiloColor1;
    private String hiloColor2;
    private Double hiloPrice1;
    private Double hiloPrice2;
    private String updatedAt;

    public SnapshotAioByRenko() {
    }

    // Getters & Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public String getTfScale() {
        return tfScale;
    }

    public void setTfScale(String tfScale) {
        this.tfScale = tfScale;
    }

    public Integer getTfInterval() {
        return tfInterval;
    }

    public void setTfInterval(Integer tfInterval) {
        this.tfInterval = tfInterval;
    }

    public Double getRenkoSize() {
        return renkoSize;
    }

    public void setRenkoSize(Double renkoSize) {
        this.renkoSize = renkoSize;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Double getPriceRes() {
        return priceRes;
    }

    public void setPriceRes(Double priceRes) {
        this.priceRes = priceRes;
    }

    public Double getPriceSup() {
        return priceSup;
    }

    public void setPriceSup(Double priceSup) {
        this.priceSup = priceSup;
    }

    public String getRenkoColor() {
        return renkoColor;
    }

    public void setRenkoColor(String renkoColor) {
        this.renkoColor = renkoColor;
    }

    public String getHiloColor1() {
        return hiloColor1;
    }

    public void setHiloColor1(String hiloColor1) {
        this.hiloColor1 = hiloColor1;
    }

    public String getHiloColor2() {
        return hiloColor2;
    }

    public void setHiloColor2(String hiloColor2) {
        this.hiloColor2 = hiloColor2;
    }

    public Double getHiloPrice1() {
        return hiloPrice1;
    }

    public void setHiloPrice1(Double hiloPrice1) {
        this.hiloPrice1 = hiloPrice1;
    }

    public Double getHiloPrice2() {
        return hiloPrice2;
    }

    public void setHiloPrice2(Double hiloPrice2) {
        this.hiloPrice2 = hiloPrice2;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
