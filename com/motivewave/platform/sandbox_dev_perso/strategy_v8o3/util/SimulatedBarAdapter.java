package sbdev.joy_ai_project.strategy_v8o3.util;

public class SimulatedBarAdapter implements IBar {
    private final FormatResult.SimulatedBar bar;

    public SimulatedBarAdapter(FormatResult.SimulatedBar bar) {
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
