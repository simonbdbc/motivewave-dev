package dev;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.common.desc.*;
import com.motivewave.platform.sdk.study.*;

/** Kalman Smoothed ROC & Stochastic */
@StudyHeader(namespace = "com.mycompany", id = "KALMAN_SROC_STOCHASTIC", rb = "study_examples.nls.strings", name = "Kalman Smoothed ROC & Stochastic", label = "K-SROC-ST", desc = "Kalman Smoothed ROC & Stochastic Indicator", menu = "My Studies", overlay = false, studyOverlay = false)
public class KalmanSROCStochastic extends Study {

    enum Values {
        KALMAN, ROC, STOCH_K, STOCH_D, BLEND
    }

    enum Signals {
        CROSS_ABOVE, CROSS_BELOW
    }

    @Override
    public void initialize(Defaults defaults) {
        var sd = createSD();
        var tab = sd.addTab("General");

        // Moving Average Settings
        var grp = tab.addGroup("Moving Average Smoothing");
        grp.addRow(new InputDescriptor("MA_TYPE", "MA Type", Enums.BarInput.CLOSE));
        grp.addRow(new IntegerDescriptor("SMOOTH_LEN", "Smoothing Length", 12, 1, 100, 1));

        // Kalman Filter Settings
        grp = tab.addGroup("Kalman Filter");
        grp.addRow(new InputDescriptor("SOURCE", "Kalman Source", Enums.BarInput.CLOSE));
        grp.addRow(new DoubleDescriptor("SHARPNESS", "Sharpness", 25.0, 0.1, 100.0, 0.1));
        grp.addRow(new DoubleDescriptor("FILTER_PERIOD", "Filter Period", 1.0, 0.1, 100.0, 0.1));

        // Stochastic Settings
        grp = tab.addGroup("Stochastic");
        grp.addRow(new IntegerDescriptor("PERIOD_K", "%K Length", 14, 1, 100, 1));
        grp.addRow(new IntegerDescriptor("SMOOTH_K", "%K Smoothing", 3, 1, 100, 1));
        grp.addRow(new IntegerDescriptor("PERIOD_D", "%D Smoothing", 3, 1, 100, 1));

        // Declare Runtime Behavior
        var desc = createRD();
        desc.setLabelSettings("MA_TYPE", "SMOOTH_LEN");
        desc.exportValue(new ValueDescriptor(Values.BLEND, "Blended ROC & Stochastic",
                new String[] { "MA_TYPE", "SMOOTH_LEN" }));
        desc.declarePath(Values.BLEND, Inputs.PATH);
    }

    @Override
    protected void calculate(int index, DataContext ctx) {
        var series = ctx.getDataSeries();

        // ** Kalman Filter Logic **
        double src = series.getDouble(index, Inputs.INPUT);
        double prevKalman = series.getDouble(index - 1, Values.KALMAN, src);
        double distance = src - prevKalman;
        double sharpness = getSettings().getDouble("SHARPNESS");
        double filterPeriod = getSettings().getDouble("FILTER_PERIOD");

        double error = prevKalman + distance * Math.sqrt(sharpness * filterPeriod / 100.0);
        double velocity = series.getDouble(index - 1, "VELOCITY", 0) + distance * filterPeriod / 100.0;
        double kalman = error + velocity;

        series.setDouble(index, Values.KALMAN, kalman);

        // ** Rate of Change (ROC) **
        int rocLength = getSettings().getInteger("ROC_LENGTH");
        if (index < rocLength)
            return;

        Double prevKalmanRoc = series.getDouble(index - rocLength, Values.KALMAN);
        if (prevKalmanRoc == null || prevKalmanRoc == 0)
            return; // Handle division by zero

        double roc = 100 * (kalman - prevKalmanRoc) / prevKalmanRoc;
        series.setDouble(index, Values.ROC, roc);

        // ** Stochastic Logic **
        int periodK = getSettings().getInteger("PERIOD_K");
        Double stochK = series.stochasticK(index, periodK);
        if (stochK == null)
            return; // Handle missing value for %K
        series.setDouble(index, Values.STOCH_K, stochK);

        int smoothK = getSettings().getInteger("SMOOTH_K");
        int periodD = getSettings().getInteger("PERIOD_D");

        if (index < periodK + smoothK)
            return;

        Double smoothedK = calculateMovingAverage(series, index, smoothK, stochK);
        if (smoothedK == null)
            return; // Handle missing value
        series.setDouble(index, Values.STOCH_K, smoothedK);

        if (index < periodK + smoothK + periodD)
            return;

        Double smoothedD = calculateMovingAverage(series, index, periodD, smoothedK);
        if (smoothedD == null)
            return; // Handle missing value
        series.setDouble(index, Values.STOCH_D, smoothedD);

        // ** Blend Logic (ROC + Stochastic) **
        int smoothLen = getSettings().getInteger("SMOOTH_LEN");
        double blend = (roc + smoothedD) / 2;
        Double smoothedBlend = calculateMovingAverage(series, index, smoothLen, blend);
        if (smoothedBlend == null)
            return; // Handle missing value
        series.setDouble(index, Values.BLEND, smoothedBlend);

        // ** Signals **
        if (crossedAbove(series, index, Values.BLEND, Values.BLEND)) {
            ctx.signal(index, Signals.CROSS_ABOVE, "Blend crossed above previous value", smoothedBlend);
        } else if (crossedBelow(series, index, Values.BLEND, Values.BLEND)) {
            ctx.signal(index, Signals.CROSS_BELOW, "Blend crossed below previous value", smoothedBlend);
        }

        // Complete the index
        series.setComplete(index);
    }

    @Override
    public int getMinBars() {
        return getSettings().getInteger(Inputs.PERIOD) * 2;
    }

    private Double calculateMovingAverage(DataSeries series, int index, int period, Double input) {
        if (input == null)
            return null;
        Double ema = series.ema(index, period, input);
        Double sma = series.sma(index, period, input);
        if (ema == null || sma == null)
            return null;

        // Combine EMA and SMA for a smoother result
        return (ema + sma) / 2;
    }
}