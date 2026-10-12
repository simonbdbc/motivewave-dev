package sbdev.joy_ai_project.pattern_strategy_algo.model;

public class SimulatedBarAdapter implements IBar {
    private final SimulatedBar bar;

    public SimulatedBarAdapter(SimulatedBar bar) {
        this.bar = bar;
    }

    @Override
    public long getEndTime() {
        return bar.timestamp;
    }

    @Override
    public double getOpen() {
        return bar.open;
    }

    @Override
    public double getClose() {
        return bar.close;
    }

    @Override
    public double getHigh() {
        return bar.high;
    }

    @Override
    public double getLow() {
        return bar.low;
    }
}
