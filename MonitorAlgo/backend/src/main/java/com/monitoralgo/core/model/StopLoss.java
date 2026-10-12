package com.monitoralgo.core.model;

public class StopLoss {
    private Integer id;
    private Double valuePoint;
    private Integer valueTick;

    public StopLoss() {
    }

    public StopLoss(Integer id, Double valuePoint, Integer valueTick) {
        this.id = id;
        this.valuePoint = valuePoint;
        this.valueTick = valueTick;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getValuePoint() {
        return valuePoint;
    }

    public void setValuePoint(Double valuePoint) {
        this.valuePoint = valuePoint;
    }

    public Integer getValueTick() {
        return valueTick;
    }

    public void setValueTick(Integer valueTick) {
        this.valueTick = valueTick;
    }
}
