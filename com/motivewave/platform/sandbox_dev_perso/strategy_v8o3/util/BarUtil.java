package sbdev.joy_ai_project.strategy_v8o3.util;

import java.util.*;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.SimulatedBar;

public class BarUtil {

    /**
     * Regroupe des SimulatedBar (tick ou bar) en barres d'intervalle fixe (par
     * exemple 1min, 5min)
     */
    public static List<IBar> groupSimulatedBars(List<SimulatedBar> bars, long timeframeMillis) {
        List<IBar> grouped = new ArrayList<>();
        if (bars == null || bars.isEmpty())
            return grouped;
        bars.sort(Comparator.comparingLong(b -> b.timestamp)); // sécurité
        long barStart = -1;
        double open = 0, high = 0, low = 0, close = 0;
        for (SimulatedBar bar : bars) {
            long currentBarStart = (bar.timestamp / timeframeMillis) * timeframeMillis;
            if (barStart == -1 || currentBarStart != barStart) {
                if (barStart != -1)
                    grouped.add(new BarUtil.SimpleBar(barStart + timeframeMillis - 1, open, high, low, close));
                barStart = currentBarStart;
                open = high = low = close = bar.open != null ? bar.open : bar.close;
            }
            if (bar.high != null && bar.high > high)
                high = bar.high;
            if (bar.low != null && bar.low < low)
                low = bar.low;
            close = bar.close;
        }
        if (barStart != -1)
            grouped.add(new BarUtil.SimpleBar(barStart + timeframeMillis - 1, open, high, low, close));
        return grouped;
    }

    // Simple IBar implémentation pour SimulatedBar regroupé
    public static class SimpleBar implements IBar {
        private final long endTime;
        private final double open, high, low, close;

        public SimpleBar(long endTime, double open, double high, double low, double close) {
            this.endTime = endTime;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
        }

        @Override
        public long getEndTime() {
            return endTime;
        }

        @Override
        public double getOpen() {
            return open;
        }

        @Override
        public double getClose() {
            return close;
        }

        @Override
        public double getHigh() {
            return high;
        }

        @Override
        public double getLow() {
            return low;
        }
    }
}
