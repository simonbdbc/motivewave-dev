package backtest.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import backtest.model.GroupKey;
import backtest.model.GroupStat;

public class GroupingUtil {
    public static <T> List<GroupStat<T>> groupAndStat(
            List<T> items,
            Function<T, GroupKey> keyExtractor,
            Function<T, Double> pfExtractor,
            ToIntFunction<T> deltaExtractor,
            Function<T, Double> scoreExtractor
    ) {
        Map<GroupKey, List<T>> byGroup = new HashMap<>();
        for (T item : items) {
            GroupKey key = keyExtractor.apply(item);
            byGroup.computeIfAbsent(key, _ -> new ArrayList<>()).add(item);
        }
        List<GroupStat<T>> groupStats = new ArrayList<>();
        for (Map.Entry<GroupKey, List<T>> e : byGroup.entrySet()) {
            double sumPF = 0.0;
            int sumDelta = 0;
            double sumScore = 0.0;
            int n = e.getValue().size();
            for (T item : e.getValue()) {
                sumPF += pfExtractor.apply(item);
                sumDelta += deltaExtractor.applyAsInt(item);
                sumScore += scoreExtractor.apply(item);
            }
            double avgLogPF = n > 0 ? (sumPF / n) * Math.log(n + 1) : 0.0;
            double avgLogDelta = n > 0 ? (((double) sumDelta) / n) * Math.log(n + 1) : 0.0;
            double avgLogScore = n > 0 ? (sumScore / n) * Math.log(n + 1) : 0.0;
            groupStats.add(new GroupStat<>(e.getKey(), avgLogPF, avgLogDelta, avgLogScore, e.getValue()));
        }
        groupStats.sort(GroupingUtil::groupStatComparator);
        return groupStats;
    }

    public static int groupStatComparator(GroupStat<?> a, GroupStat<?> b) {
        // int cmp = Double.compare(b.avgPF, a.avgPF);
        int cmp = Double.compare(b.avgLogScore, a.avgLogScore);
        if (cmp != 0) return cmp;
        return Double.compare(b.avgLogDelta, a.avgLogDelta);
    }
}
