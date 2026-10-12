package backtest.model;

import java.util.Objects;

public class PatternKey {
    public String instrumentKey;
    public String intervalScale;
    public Integer intervalValue;
    public Double renkoSize;
    public String patternRenko;
    public String patternHilo1;
    public String patternHilo2;

    public PatternKey(String instrumentKey, String intervalScale, Integer intervalValue, Double renkoSize,
            String patternRenko, String patternHilo1, String patternHilo2) {
        this.instrumentKey = instrumentKey;
        this.intervalScale = intervalScale;
        this.intervalValue = intervalValue;
        this.renkoSize = renkoSize;
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
        return intervalValue == that.intervalValue && instrumentKey.equals(that.instrumentKey)
                && intervalScale.equals(that.intervalScale) && renkoSize.equals(that.renkoSize)
                && patternRenko.equals(that.patternRenko) && patternHilo1.equals(that.patternHilo1)
                && patternHilo2.equals(that.patternHilo2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrumentKey, intervalScale, intervalValue, renkoSize, patternRenko, patternHilo1,
                patternHilo2);
    }
}
