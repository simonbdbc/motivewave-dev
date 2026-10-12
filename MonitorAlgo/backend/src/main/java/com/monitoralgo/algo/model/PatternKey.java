package com.monitoralgo.algo.model;

import java.util.Objects;

public class PatternKey {
    public String instrumentKey;
    public String timeframeScale;
    public Integer timeframeValue;
    public Double renkoSizeValuePoint;
    public String patternRenko;
    public String patternHilo1;
    public String patternHilo2;

    public PatternKey(String instrumentKey, String timeframeScale, Integer timeframeValue, Double renkoSizeValuePoint,
            String patternRenko, String patternHilo1, String patternHilo2) {
        this.instrumentKey = instrumentKey;
        this.timeframeScale = timeframeScale;
        this.timeframeValue = timeframeValue;
        this.renkoSizeValuePoint = renkoSizeValuePoint;
        this.patternRenko = patternRenko;
        this.patternHilo1 = patternHilo1;
        this.patternHilo2 = patternHilo2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PatternKey))
            return false;
        PatternKey that = (PatternKey) o;
        return timeframeValue == that.timeframeValue && instrumentKey.equals(that.instrumentKey)
                && timeframeScale.equals(that.timeframeScale) && renkoSizeValuePoint.equals(that.renkoSizeValuePoint)
                && patternRenko.equals(that.patternRenko) && patternHilo1.equals(that.patternHilo1)
                && patternHilo2.equals(that.patternHilo2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrumentKey, timeframeScale, timeframeValue, renkoSizeValuePoint, patternRenko, patternHilo1,
                patternHilo2);
    }
}
