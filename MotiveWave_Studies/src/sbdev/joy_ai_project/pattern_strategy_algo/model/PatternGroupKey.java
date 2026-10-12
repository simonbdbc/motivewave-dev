package sbdev.joy_ai_project.pattern_strategy_algo.model;

import java.util.Objects;

public class PatternGroupKey {
    public int intervalValue, tp, sl;

    public PatternGroupKey(int intervalValue, int tp, int sl) {
        this.intervalValue = intervalValue;
        this.tp = tp;
        this.sl = sl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PatternGroupKey))
            return false;
        PatternGroupKey groupKey = (PatternGroupKey) o;
        return intervalValue == groupKey.intervalValue && tp == groupKey.tp && sl == groupKey.sl;
    }

    @Override
    public int hashCode() {
        return Objects.hash(intervalValue, tp, sl);
    }

    @Override
    public String toString() {
        return intervalValue + "," + tp + "," + sl;
    }

    public String toKeyString() {
        return intervalValue + "_" + tp + "_" + sl;
    }

    public static PatternGroupKey fromKeyString(String keyString) {
        if (keyString == null)
            throw new IllegalArgumentException("keyString cannot be null");
        String[] parts = keyString.trim().split("_");
        if (parts.length != 3)
            throw new IllegalArgumentException("keyString format must be interval_tp_sl");
        try {
            int interval = Integer.parseInt(parts[0]);
            int tp = Integer.parseInt(parts[1]);
            int sl = Integer.parseInt(parts[2]);
            return new PatternGroupKey(interval, tp, sl);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("All elements must be integers in keyString: " + keyString, e);
        }
    }

}