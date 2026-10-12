package backtest.model;

import java.util.List;

public class GroupStat<T> {
    public GroupKey key;
    public double avgLogPF, avgLogDelta, avgLogScore;
    public List<T> items;

    public GroupStat(GroupKey key, double avgLogPF, double avgLogDelta, double avgLogScore, List<T> items) {
        this.key = key;
        this.avgLogPF = avgLogPF;
        this.avgLogDelta = avgLogDelta;
        this.avgLogScore = avgLogScore;
        this.items = items;
    }
}
