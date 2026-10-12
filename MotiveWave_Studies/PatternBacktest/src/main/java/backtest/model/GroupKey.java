package backtest.model;

import java.util.Objects;

public class GroupKey {
    public int intervalValue, tp, sl;
    public double renkoSize;

    public GroupKey(int intervalValue, double renkoSize, int tp, int sl) {
        this.intervalValue = intervalValue;
        this.renkoSize = renkoSize;
        this.tp = tp;
        this.sl = sl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GroupKey))
            return false;
        GroupKey that = (GroupKey) o;
        return intervalValue == that.intervalValue && renkoSize == that.renkoSize && tp == that.tp && sl == that.sl;
    }

    @Override
    public int hashCode() {
        return Objects.hash(intervalValue, renkoSize, tp, sl);
    }

    @Override
    public String toString() {
        return intervalValue + "_" + renkoSize + "_" + tp + "_" + sl;
    }
}
