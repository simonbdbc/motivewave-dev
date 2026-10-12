package com.monitoralgo.core.model;

public class Timeframe {
    private Integer id;
    private String scale;
    private Integer value;

    public Timeframe() {
    }

    public Timeframe(Integer id, String scale, Integer value) {
        this.id = id;
        this.scale = scale;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
