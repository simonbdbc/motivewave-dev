package sbdev.joy_ai_project.pattern_strategy_algo.model;

public class StrategyStatsSummary {
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

    public StrategyStatsSummary(String strategyName, String instrument, String intervalScale, int intervalValue,
            Long startTimestamp, Long endTimestamp, int totalSignals, int winSignals, int lossSignals,
            double takeProfit, double stopLoss) {
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