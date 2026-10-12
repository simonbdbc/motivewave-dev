package dev;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.common.desc.*;
import com.motivewave.platform.sdk.study.*;

import java.awt.Color;

/** ROC n Roll Indicator with Advanced Features */
@StudyHeader(namespace = "study_examples", id = "ROC_N_ROLL_ADVANCED", name = "ROC n Roll Advanced", desc = "Advanced ROC n Roll Indicator with multi-timeframe support", menu = "Custom Studies", overlay = false)
public class RocNRollAdvanced extends Study {

    // Constants for settings keys
    final static String ROC_PERIOD = "rocPeriod";
    final static String EMA_FAST_PERIOD = "emaFast";
    final static String EMA_SLOW_PERIOD = "emaSlow";
    final static String BAR_SIZE = "barSize";
    final static String HISTOGRAM_PATH = "histogramPath";
    final static String UP_COLOR = "upColor";
    final static String DOWN_COLOR = "downColor";
    final static String MID_COLOR = "midColor";

    enum Values {
        ROC, EMA_FAST, EMA_SLOW, HISTOGRAM
    }

    @Override
    public void initialize(Defaults defaults) {
        var sd = createSD();
        var inputs = sd.addTab("Inputs");

        // Input settings
        var inputGroup = inputs.addGroup("Input Settings");
        inputGroup.addRow(new IntegerDescriptor(ROC_PERIOD, "ROC Period", 14, 1, 999, 1));
        inputGroup.addRow(new IntegerDescriptor(EMA_FAST_PERIOD, "Fast EMA Period", 5, 1, 999, 1));
        inputGroup.addRow(new IntegerDescriptor(EMA_SLOW_PERIOD, "Slow EMA Period", 10, 1, 999, 1));
        inputGroup.addRow(new BarSizeDescriptor(BAR_SIZE, "Bar Size", BarSize.getBarSize(1)));

        // Display settings
        var display = sd.addTab("Display");
        var displayGroup = display.addGroup("Display Settings");
        displayGroup.addRow(new PathDescriptor(HISTOGRAM_PATH, "Histogram", defaults.getLineColor(), 1.5f, null, true,
                false, true));
        displayGroup.addRow(new ColorDescriptor(UP_COLOR, "Up Color", defaults.getGreen()));
        displayGroup.addRow(new ColorDescriptor(DOWN_COLOR, "Down Color", defaults.getRed()));
        displayGroup.addRow(new ColorDescriptor(MID_COLOR, "Mid Color", defaults.getBlue()));

        var desc = createRD();
        desc.declarePath(Values.HISTOGRAM, HISTOGRAM_PATH);

        desc.exportValue(new ValueDescriptor(Values.ROC, "ROC", new String[] { ROC_PERIOD }));
        desc.declarePath(Values.ROC, null);

        desc.exportValue(new ValueDescriptor(Values.EMA_FAST, "Fast EMA", new String[] { EMA_FAST_PERIOD }));
        desc.exportValue(new ValueDescriptor(Values.EMA_SLOW, "Slow EMA", new String[] { EMA_SLOW_PERIOD }));

        setSettingsDescriptor(sd);
        setRuntimeDescriptor(desc);
    }

    // @Override
    // protected void calculateValues(DataContext ctx) {
    // var series = ctx.getDataSeries(getSettings().getBarSize(BAR_SIZE));
    // int rocPeriod = getSettings().getInteger(ROC_PERIOD, 14);
    // int emaFastPeriod = getSettings().getInteger(EMA_FAST_PERIOD, 5);
    // int emaSlowPeriod = getSettings().getInteger(EMA_SLOW_PERIOD, 10);

    // // Ensure enough data points
    // for (int i = 0; i < series.size(); i++) {
    // if (series.isComplete(i))
    // continue;

    // // // Calculate ROC
    // // if (i < rocPeriod)
    // // continue;
    // // double price = series.getClose(i);
    // // double prevPrice = series.getClose(i - rocPeriod);
    // // double roc = (price - prevPrice) / prevPrice * 100;
    // // series.setDouble(i, Values.ROC, roc);

    // // Vérifier qu'il y a assez de données pour calculer l'EMA
    // if (i < Math.max(emaFastPeriod, emaSlowPeriod))
    // return;

    // // Calculer le ROC (si non encore défini)
    // if (series.getDouble(i, Values.ROC) == null) {
    // double price = series.getClose(i);
    // double prevPrice = series.getClose(i - rocPeriod);
    // double roc = (price - prevPrice) / prevPrice * 100;
    // series.setDouble(i, Values.ROC, roc);
    // }

    // // Calculer les EMAs
    // double emaFast = series.ema(i, emaFastPeriod, Values.ROC);
    // double emaSlow = series.ema(i, emaSlowPeriod, Values.ROC);

    // // Vérifiez que les EMAs ont des valeurs valides
    // if (Double.isNaN(emaFast) || Double.isNaN(emaSlow))
    // return;

    // // Histogramme
    // double histogram = emaFast - emaSlow;
    // series.setDouble(i, Values.HISTOGRAM, histogram);

    // Color upColor = getSettings().getColor(UP_COLOR,
    // ctx.getDefaults().getGreen());
    // Color downColor = getSettings().getColor(DOWN_COLOR,
    // ctx.getDefaults().getRed());
    // Color midColor = getSettings().getColor(DOWN_COLOR,
    // ctx.getDefaults().getBlue());

    // // Assign colors
    // if (histogram > 0) {
    // series.setBarColor(i, upColor); // Tendance haussière
    // } else if (histogram < 0) {
    // series.setBarColor(i, downColor); // Tendance baissière
    // } else {
    // series.setBarColor(i, midColor); // Aucune tendance claire
    // }

    // series.setComplete(i);
    // }
    // }

    @Override
    protected void calculateValues(DataContext ctx) {
        var series = ctx.getDataSeries(getSettings().getBarSize(BAR_SIZE));
        int rocPeriod = getSettings().getInteger(ROC_PERIOD, 14);
        int emaFastPeriod = getSettings().getInteger(EMA_FAST_PERIOD, 5);
        int emaSlowPeriod = getSettings().getInteger(EMA_SLOW_PERIOD, 10);

        debug("series.size(): " + series.size() + "rocPeriod: " + rocPeriod + " emaFastPeriod: " + emaFastPeriod
                + " emaSlowPeriod: " + emaSlowPeriod);

        // Assurez-vous qu'il y a assez de données
        if (series.size() < Math.max(rocPeriod, Math.max(emaFastPeriod, emaSlowPeriod)))
            return;

        // for (int i = 0; i < series.size(); i++) {
        //     if (series.isComplete(i))
        //         continue;

        //     // Vérifiez qu'il y a assez de données
        //     if (i >= rocPeriod) {
        //         double price = series.getClose(i);
        //         double prevPrice = series.getClose(i - rocPeriod);
        //         double roc = (price - prevPrice) / prevPrice * 100;

        //         if (series.getDouble(i, Values.ROC) == null)
        //             // Stockez la valeur ROC
        //             series.setDouble(i, Values.ROC, roc);
        //         debug("ROC calculé et défini : " + roc);

        //         // Vérifiez immédiatement après
        //         debug("ROC stocké dans series : " + series.getDouble(i, Values.ROC));
        //         // }

        //         // Vérifiez si la valeur est accessible
        //         debug("ROC récupéré depuis series : " + series.getDouble(i, Values.ROC));

        //         if (series.getDouble(i, Values.ROC) == null)
        //             continue;

        //         debug(" emaFastPeriod: " + emaFastPeriod);
        //         debug(" emaSlowPeriod: " + emaSlowPeriod);
        //         debug(" Values.ROC: " + Values.ROC);

        //         // Calculer les EMAs
        //         Double emaFast = series.ema(i, emaFastPeriod, Values.ROC);
        //         Double emaSlow = series.ema(i, emaSlowPeriod, Values.ROC);

        //         debug(" emaFast: " + emaFast);
        //         debug(" emaSlow: " + emaSlow);

        //         // if (Double.isNaN(emaFast) || Double.isNaN(emaSlow))
        //         if (emaFast == null || emaSlow == null)
        //             continue;

        //         // Histogramme
        //         double histogram = emaFast - emaSlow;
        //         series.setDouble(i, Values.HISTOGRAM, histogram);

        //         // Couleurs dynamiques
        //         Color upColor = getSettings().getColor(UP_COLOR, ctx.getDefaults().getGreen());
        //         Color downColor = getSettings().getColor(DOWN_COLOR, ctx.getDefaults().getRed());
        //         Color midColor = getSettings().getColor(MID_COLOR, ctx.getDefaults().getBlue());

        //         if (histogram > 0) {
        //             series.setBarColor(i, upColor);
        //         } else if (histogram < 0) {
        //             series.setBarColor(i, downColor);
        //         } else {
        //             series.setBarColor(i, midColor);
        //         }
        //     }

        //     series.setComplete(i);
        // }

        for (int i = 0; i < series.size(); i++) {
            if (series.isComplete(i)) continue;
        
            // Vérifiez qu'il y a assez de données pour calculer le ROC
            if (i >= rocPeriod) {
                double price = series.getClose(i);
                double prevPrice = series.getClose(i - rocPeriod);
                double roc = (price - prevPrice) / prevPrice * 100;
                series.setDouble(i, Values.ROC, roc);
        
                debug("ROC défini pour i=" + i + ": " + roc);
                debug("ROC récupéré de la série : " + series.getDouble(i, Values.ROC));
            }
        
            if (series.getDouble(i, Values.ROC) == null) {
                debug("ROC manquant pour i=" + i);
                continue;
            }
        
            // Vérifiez qu'il y a assez de données pour l'EMA
            if (i < Math.max(emaFastPeriod, emaSlowPeriod)) {
                debug("Pas assez de données pour calculer les EMAs à i=" + i);
                continue;
            }
        
            // Calculer les EMAs
            Double emaFast = series.ema(i, emaFastPeriod, Values.ROC);
            Double emaSlow = series.ema(i, emaSlowPeriod, Values.ROC);
        
            debug("EMA Fast à i=" + i + ": " + emaFast);
            debug("EMA Slow à i=" + i + ": " + emaSlow);
        
            if (emaFast == null || emaSlow == null) {
                debug("EMAs manquants pour i=" + i);
                continue;
            }
        
            // Calculer l'histogramme
            double histogram = emaFast - emaSlow;
            series.setDouble(i, Values.HISTOGRAM, histogram);
        
            // Couleurs dynamiques
            Color upColor = getSettings().getColor(UP_COLOR, ctx.getDefaults().getGreen());
            Color downColor = getSettings().getColor(DOWN_COLOR, ctx.getDefaults().getRed());
            Color midColor = getSettings().getColor(MID_COLOR, ctx.getDefaults().getBlue());
        
            if (histogram > 0) {
                series.setBarColor(i, upColor);
            } else if (histogram < 0) {
                series.setBarColor(i, downColor);
            } else {
                series.setBarColor(i, midColor);
            }
        
            series.setComplete(i);
        }
    }

    @Override
    protected void calculate(int index, DataContext ctx) {
        // All calculations are handled in calculateValues()
    }
}