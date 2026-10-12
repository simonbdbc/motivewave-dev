package custom_studies.strategies.strategy_core_v4.utils;

public class FormatResult {

    public static class PatternCheckResult {
        public final String patternName;
        public final BracketResult bracket;
        public final PatternDirectionResult direction;
        public final PatternStrengthResult strength;
        public final PatternRenkoResult renko;
        public final PatternHiloResult hilo;

        public PatternCheckResult(String patternName,
                BracketResult bracket,
                PatternDirectionResult direction,
                PatternStrengthResult strength,
                PatternRenkoResult renko,
                PatternHiloResult hilo) {
            this.patternName = patternName;
            this.bracket = bracket;
            this.direction = direction;
            this.strength = strength;
            this.renko = renko;
            this.hilo = hilo;
        }

        public boolean okLong() {
            return direction.high && strength.high && renko.high && hilo.high;
        }

        public boolean okShort() {
            return direction.low && strength.low && renko.low && hilo.low;
        }
    }

    public static class PatternCoreResult {
        public final String name;
        public final boolean high;
        public final boolean low;

        public PatternCoreResult(String name, boolean high, boolean low) {
            this.name = name;
            this.high = high;
            this.low = low;
        }
    }

    public static class PatternDirectionResult {
        public final boolean high;
        public final boolean low;

        public PatternDirectionResult(boolean high, boolean low) {
            this.high = high;
            this.low = low;
        }
    }

    public static class PatternStrengthResult {
        public final boolean high;
        public final boolean low;

        public PatternStrengthResult(boolean high, boolean low) {
            this.high = high;
            this.low = low;
        }
    }

    public static class PatternRenkoResult {
        public final boolean high;
        public final boolean low;

        public PatternRenkoResult(boolean high, boolean low) {
            this.high = high;
            this.low = low;
        }
    }

    public static class PatternHiloResult {
        public final boolean high;
        public final boolean low;

        public PatternHiloResult(boolean high, boolean low) {
            this.high = high;
            this.low = low;
        }
    }

    public static class BracketResult {
        public final double tp;
        public final double sl;

        public BracketResult(double tp, double sl) {
            this.tp = tp;
            this.sl = sl;
        }
    }

    public static class ColorCountResult {
        private final int greenCount;
        private final int redCount;

        public ColorCountResult(int greenCount, int redCount) {
            this.greenCount = greenCount;
            this.redCount = redCount;
        }

        public int getGreenCount() {
            return greenCount;
        }

        public int getRedCount() {
            return redCount;
        }
    }

    public static class StrategyStatsSummary {
        public String strategyName;
        public String instrument;
        public String intervalScale;
        public int intervalValue;
        public Long startTimestamp;
        public Long endTimestamp;
        public int totalSignals;
        public int winSignals;
        public int lossSignals;
        public double takeProfit;
        public double stopLoss;

        public StrategyStatsSummary(String strategyName, String instrument, String intervalScale, int intervalValue, Long startTimestamp,
                Long endTimestamp, int totalSignals, int winSignals, int lossSignals, double takeProfit,
                double stopLoss) {
            this.strategyName = strategyName;
            this.instrument = instrument;
            this.intervalScale = intervalScale;
            this.intervalValue = intervalValue;
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
            this.totalSignals = totalSignals;
            this.winSignals = winSignals;
            this.lossSignals = lossSignals;
            this.takeProfit = takeProfit;
            this.stopLoss = stopLoss;
        }
    }

    public static class SimulatedBar {
        public Long timestamp;
        public Double open;
        public Double high;
        public Double low;
        public Double close;

        public SimulatedBar(Long timestamp, Double open, Double high, Double low, Double close) {
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

    public static class HiLoResult {
        public Long timestamp;
        public Double hiLo;
        public Double close;
        public Boolean isHigh;
        public Boolean isLow;

        public HiLoResult(Long timestamp, Double hiLo, Double close, Boolean isHigh, Boolean isLow) {
            this.timestamp = timestamp;
            this.hiLo = hiLo;
            this.close = close;
            this.isHigh = isHigh;
            this.isLow = isLow;
        }
    }

}
