package dev;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.common.desc.*;
import com.motivewave.platform.sdk.study.*;

import java.awt.Color;

@StudyHeader(
    namespace = "study_examples",
    id = "ROCNROLL",
    name = "ROC n Roll",
    desc = "Indicateur ROC n Roll combinant ROC et MME",
    menu = "Custom Studies",
    overlay = false
)
public class RocNRoll extends Study {

    // Clés des paramètres
    final static String ROC_PERIOD = "rocPeriod";
    final static String EMA_FAST_PERIOD = "emaFast";
    final static String EMA_SLOW_PERIOD = "emaSlow";
    final static String HISTOGRAM_PATH = "histogramPath";
    final static String UP_COLOR = "upColor";
    final static String DOWN_COLOR = "downColor";
    final static String NEUTRAL_COLOR = "neutralColor";

    enum Values {
        ROC, EMA_FAST, EMA_SLOW, HISTOGRAM, SIGNAL
    }

    @Override
    public void initialize(Defaults defaults) {
        var sd = createSD();
        var inputs = sd.addTab("Paramètres");

        // Paramètres d'entrée
        var inputGroup = inputs.addGroup("Paramètres d'entrée");
        inputGroup.addRow(new IntegerDescriptor(ROC_PERIOD, "Période du ROC", 14, 1, 999, 1));
        inputGroup.addRow(new IntegerDescriptor(EMA_FAST_PERIOD, "Période de la MME rapide", 5, 1, 999, 1));
        inputGroup.addRow(new IntegerDescriptor(EMA_SLOW_PERIOD, "Période de la MME lente", 10, 1, 999, 1));

        // Paramètres d'affichage
        var display = sd.addTab("Affichage");
        var displayGroup = display.addGroup("Paramètres d'affichage");
        displayGroup.addRow(new PathDescriptor(HISTOGRAM_PATH, "Histogramme", defaults.getLineColor(), 1.5f, null, true, false, true));
        displayGroup.addRow(new ColorDescriptor(UP_COLOR, "Couleur haussière", defaults.getGreen()));
        displayGroup.addRow(new ColorDescriptor(DOWN_COLOR, "Couleur baissière", defaults.getRed()));
        displayGroup.addRow(new ColorDescriptor(NEUTRAL_COLOR, "Couleur neutre", defaults.getBlue()));

        var desc = createRD();
        desc.declarePath(Values.HISTOGRAM, HISTOGRAM_PATH);

        setSettingsDescriptor(sd);
        setRuntimeDescriptor(desc);
    }

    @Override
    protected void calculateValues(DataContext ctx) {
        var series = ctx.getDataSeries();
        int rocPeriod = getSettings().getInteger(ROC_PERIOD, 14);
        int emaFastPeriod = getSettings().getInteger(EMA_FAST_PERIOD, 5);
        int emaSlowPeriod = getSettings().getInteger(EMA_SLOW_PERIOD, 10);

        // Vérifier qu'il y a suffisamment de données
        if (series.size() < Math.max(rocPeriod, Math.max(emaFastPeriod, emaSlowPeriod))) return;

        for (int i = 0; i < series.size(); i++) {
            if (series.isComplete(i)) continue;

            // Calculer le ROC
            if (i >= rocPeriod) {
                double price = series.getClose(i);
                double prevPrice = series.getClose(i - rocPeriod);
                double roc = ((price - prevPrice) / prevPrice) * 100;
                series.setDouble(i, Values.ROC, roc);
            }

            // Calculer les MME
            if (i >= emaSlowPeriod) {
                Double emaFast = series.ema(i, emaFastPeriod, Values.ROC);
                Double emaSlow = series.ema(i, emaSlowPeriod, Values.ROC);

                if (emaFast != null && emaSlow != null) {
                    // Calculer l'histogramme
                    double histogram = emaFast - emaSlow;
                    series.setDouble(i, Values.HISTOGRAM, histogram);

                    // Déterminer le signal
                    int signal = 0;
                    if (histogram > 0) {
                        signal = 1; // Signal haussier
                        series.setBarColor(i, getSettings().getColor(UP_COLOR));
                    } else if (histogram < 0) {
                        signal = 2; // Signal baissier
                        series.setBarColor(i, getSettings().getColor(DOWN_COLOR));
                    } else {
                        signal = -1; // Signal neutre
                        series.setBarColor(i, getSettings().getColor(NEUTRAL_COLOR));
                    }
                    series.setInt(i, Values.SIGNAL, signal);
                }
            }

            series.setComplete(i);
        }
    }
}