package com.monitoralgo.core.model;

public class Instrument {
    private Integer id;
    private String label;
    private String key;
    private Double tickSize;

    public Instrument() {
    }

    public Instrument(Integer id, String label, String key, Double tickSize) {
        this.id = id;
        this.label = label;
        this.key = key;
        this.tickSize = tickSize;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Double getTickSize() {
        return tickSize;
    }

    public void setTickSize(Double tickSize) {
        this.tickSize = tickSize;
    }
}
