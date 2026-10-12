package sbdev.joy_ai_project.strategy.service.history;

import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Tick;
import com.motivewave.platform.sdk.common.TickOperation;

import java.util.ArrayList;
import java.util.List;

public class RenkoTickOperation implements TickOperation {

    private DataSeries series;
    private int groupSize;
    private double brickSize;
    private List<Tick> tickBuffer = new ArrayList<>();
    private Double realRenko = null;
    private Double previousClose = null;
    private String lastRenkoColor = null;

    public RenkoTickOperation(DataSeries series, int groupSize, double brickSize) {
        this.series = series;
        this.groupSize = groupSize;
        this.brickSize = brickSize;
    }

    @Override
    public void onTick(Tick tick) {
        tickBuffer.add(tick);
        if (tickBuffer.size() < groupSize)
            return;

        SimulatedBar bar = aggregateSimulatedBar(tickBuffer);
        tickBuffer.clear();

        RenkoResult result = calculateRenkoTickBased(bar.close);

        int idx = series.size() - 1;
        if (idx < 0)
            return; // sécurité

        series.setDouble(idx, RenkoValues.REAL_RENKO, result.realRenko);
        series.setDouble(idx, RenkoValues.PREV_CLOSE, result.previousClose);

        if (result.crossAbove)
            series.setBoolean(idx, RenkoValues.CROSS_ABOVE, true);
        if (result.crossBelow)
            series.setBoolean(idx, RenkoValues.CROSS_BELOW, true);
    }

    private SimulatedBar aggregateSimulatedBar(List<Tick> ticks) {
        double open = ticks.get(0).getPrice();
        double high = open, low = open;
        double close = ticks.get(ticks.size() - 1).getPrice();
        long timestamp = ticks.get(ticks.size() - 1).getTime();
        for (Tick t : ticks) {
            double price = t.getPrice();
            if (price > high)
                high = price;
            if (price < low)
                low = price;
        }
        return new SimulatedBar(timestamp, open, high, low, close);
    }

    private RenkoResult calculateRenkoTickBased(double currPrice) {
        boolean crossAbove = false, crossBelow = false;

        if (realRenko == null) {
            double lowerBound = Math.floor(currPrice / brickSize) * brickSize;
            double upperBound = lowerBound + brickSize;
            realRenko = upperBound;
            previousClose = lowerBound;
            lastRenkoColor = (realRenko > previousClose) ? "green" : "red";
        } else {
            if ("green".equals(lastRenkoColor) && currPrice - realRenko >= brickSize) {
                realRenko += Math.floor((currPrice - realRenko) / brickSize) * brickSize;
                previousClose = realRenko - brickSize;
                crossAbove = true;
                lastRenkoColor = "green";
            } else if ("green".equals(lastRenkoColor) && realRenko - currPrice >= 2 * brickSize) {
                realRenko -= Math.floor((realRenko - currPrice) / brickSize) * brickSize;
                previousClose = realRenko + brickSize;
                crossBelow = true;
                lastRenkoColor = "red";
            } else if ("red".equals(lastRenkoColor) && currPrice - realRenko >= 2 * brickSize) {
                realRenko += Math.floor((currPrice - realRenko) / brickSize) * brickSize;
                previousClose = realRenko - brickSize;
                crossAbove = true;
                lastRenkoColor = "green";
            } else if ("red".equals(lastRenkoColor) && realRenko - currPrice >= brickSize) {
                realRenko -= Math.floor((realRenko - currPrice) / brickSize) * brickSize;
                previousClose = realRenko + brickSize;
                crossBelow = true;
                lastRenkoColor = "red";
            }
        }
        return new RenkoResult(crossAbove, crossBelow, realRenko, previousClose);
    }

    public static class SimulatedBar {
        public final long timestamp;
        public final double open, high, low, close;

        public SimulatedBar(long timestamp, double open, double high, double low, double close) {
            this.timestamp = timestamp;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
        }
    }

    public static class RenkoResult {
        public final boolean crossAbove;
        public final boolean crossBelow;
        public final double realRenko;
        public final double previousClose;

        public RenkoResult(boolean crossAbove, boolean crossBelow, double realRenko, double previousClose) {
            this.crossAbove = crossAbove;
            this.crossBelow = crossBelow;
            this.realRenko = realRenko;
            this.previousClose = previousClose;
        }
    }
}
