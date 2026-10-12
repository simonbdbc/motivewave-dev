package sbdev.joy_ai_project.pattern_strategy_algo.model;

import com.motivewave.platform.sdk.common.Bar;

/**
 * Adapter pour exposer la bougie MotiveWave via l'interface IBar.
 */
public class BarAdapter implements IBar {
    private final Bar bar;

    public BarAdapter(Bar bar) {
        this.bar = bar;
    }

    @Override
    public long getEndTime() {
        return bar.getEndTime();
    }

    @Override
    public double getOpen() {
        return bar.getOpen();
    }

    @Override
    public double getClose() {
        return bar.getClose();
    }

    @Override
    public double getHigh() {
        return bar.getHigh();
    }

    @Override
    public double getLow() {
        return bar.getLow();
    }
}
