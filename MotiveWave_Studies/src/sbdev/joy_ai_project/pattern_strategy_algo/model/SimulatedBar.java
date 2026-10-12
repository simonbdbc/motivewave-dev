package sbdev.joy_ai_project.pattern_strategy_algo.model;

public class SimulatedBar {
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