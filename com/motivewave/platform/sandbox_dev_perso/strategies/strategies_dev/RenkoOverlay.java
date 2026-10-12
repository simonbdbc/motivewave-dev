package custom_studies.strategies.strategies_dev;

import java.awt.Color;

import com.motivewave.platform.sdk.common.BarSize;
// import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.StringDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

import custom_studies.GannHighLow;

// /** Renko Simple Overlay */
@StudyHeader(namespace = "com.mycompany", id = "RENKO_OVERLAY", rb = "study_examples.nls.strings", name = "Renko Overlay", label = "Renko Overlay", desc = "Displays a simple Renko overlay based on user settings", menu = "Custom Studies", overlay = true, signals = true, studyOverlay = true)
public class RenkoOverlay extends Study {

    protected Double brickSize = null; // Taille de la brique
    protected Double previousClose = null; // Renko open
    protected Double realRenko = null; // Renko close

    // private Boolean[] colorHistory = new Boolean[10]; // Historique des 10
    // derniers états
    protected String[] colorHistory = new String[10]; // Historique des 10 derniers états
    final static String GREEN = "green";
    final static String RED = "red";
    protected String[] crossHistory = new String[10]; // Historique des 10 derniers états
    final static String ABOVE = "above";
    final static String BELOW = "below";

    // private Boolean inLong = false;
    // private Boolean inShort = false;
    public boolean crossAbove = false;
    public boolean crossBelow = false;

    final static String INPUT = "input";
    final static String PERIOD = "period";
    final static String RENKO_POINT = "renkoPoint";
    final static String UP_MARKER = "upMarker";
    final static String DOWN_MARKER = "downMarker";
    final static String STOP_UP_MARKER = "stopUpMarker";
    final static String STOP_DOWN_MARKER = "stopDownMarker";
    final static String TOP_PATH = "topPath";
    final static String BOTTOM_PATH = "bottomPath";
    final static String TOP_IND = "topInd";
    final static String BOTTOM_IND = "bottomInd";
    final static String UP_COLOR = "upColor";
    final static String DOWN_COLOR = "downColor";

    enum Values {
        HIGH, LOW, CROSS, MA_HIGH_LOW_1, MA_HIGH_LOW_2
    }

    enum Signals {
        CROSS_HIGH, CROSS_LOW
    }

    private GannHighLow gannHighLow;

    @Override
    public void initialize(Defaults defaults) {

        gannHighLow = new GannHighLow();
        gannHighLow.initialize(defaults);

        var sd = createSD();
        // var tab = sd.addTab("General");

        sd.addTab(initializeGeneralTab());
        sd.addTab(initializeDisplayTab(defaults));

        // tab = sd.addTab("Display");
        // initializeDisplayTab(tab, defaults);

        // Quick Settings (Tool Bar and Popup Editor)
        sd.addQuickSettings(RENKO_POINT);
        sd.addQuickSettings(TOP_PATH, BOTTOM_PATH);

        var desc = createRD();
        desc.setLabelSettings(INPUT, PERIOD, RENKO_POINT);

        desc.exportValue(
                new ValueDescriptor(Values.HIGH, "High", new String[] { INPUT, PERIOD }));
        desc.exportValue(
                new ValueDescriptor(Values.LOW, "Low", new String[] { INPUT, PERIOD }));
        desc.exportValue(
                new ValueDescriptor(Signals.CROSS_HIGH, Enums.ValueType.BOOLEAN, "Cross High"));

        desc.exportValue(
                new ValueDescriptor(Signals.CROSS_LOW, Enums.ValueType.BOOLEAN, "Cross Low"));

        desc.declarePath(Values.HIGH, TOP_PATH);
        desc.declarePath(Values.LOW, BOTTOM_PATH);

        desc.declareIndicator(Values.HIGH, TOP_IND);
        desc.declareIndicator(Values.LOW, BOTTOM_IND);

        // Signals
        desc.declareSignal(Signals.CROSS_HIGH, "Cross High");
        desc.declareSignal(Signals.CROSS_LOW, "Cross Low");

        desc.setRangeKeys(Values.HIGH, Values.LOW);
    }

    protected SettingTab initializeGeneralTab() {
        var tab = new SettingTab("General Renko");

        // Section 1
        var section1 = tab.addGroup("Inputs");
        section1.addRow(new IntegerDescriptor(RENKO_POINT, "Renko Ticks", 20, 1, 99, 1));
        section1.addRow(new StringDescriptor("note", "Note", "NQ : 4 ticks = 1 point"));

        // Section 2
        // var section2 = tab.addGroup("Section 2");
        // section2.addRow(new InputDescriptor(INPUT, "Input", Enums.BarInput.CLOSE));
        // section2.addRow(new IntegerDescriptor(PERIOD, "Period", 20, 1, 9999, 1));
        return tab;
    }

    protected SettingTab initializeDisplayTab(Defaults defaults) {
        var tab = new SettingTab("Display Renko");

        // Section 1
        var markers = tab.addGroup("Markers");
        markers.addRow(new MarkerDescriptor(UP_MARKER, "Up Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL,
                defaults.getGreen(), defaults.getLineColor(), true, true));
        markers.addRow(new MarkerDescriptor(DOWN_MARKER, "Down Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL,
                defaults.getRed(), defaults.getLineColor(), true, true));
        markers.addRow(
                new MarkerDescriptor(STOP_UP_MARKER, "Stop Up Marker", Enums.MarkerType.DIAMOND, Enums.Size.LARGE,
                        defaults.getGreen(), defaults.getLineColor(), true, true));
        markers.addRow(
                new MarkerDescriptor(STOP_DOWN_MARKER, "Stop Down Marker", Enums.MarkerType.DIAMOND, Enums.Size.LARGE,
                        defaults.getRed(), defaults.getLineColor(), true, true));

        // Section 2
        var lines = tab.addGroup("Lines");
        lines.addRow(new PathDescriptor(TOP_PATH, "High Path", null, Enums.PointType.DOT, Enums.Size.VERY_SMALL, true,
                false, true));
        lines.addRow(
                new PathDescriptor(BOTTOM_PATH, "Bottom Path", null, Enums.PointType.DOT, Enums.Size.VERY_SMALL, true,
                        false, true));
        lines.addRow(new IndicatorDescriptor(TOP_IND, "High Indicator", defaults.getGreen(), X11Colors.WHITE, false,
                false, true));
        lines.addRow(new IndicatorDescriptor(BOTTOM_IND, "Bottom Indicator", defaults.getRed(), X11Colors.WHITE,
                false, false, true));
        lines.addRow(new ColorDescriptor(UP_COLOR, "Up Color", defaults.getGreenLine()));
        lines.addRow(new ColorDescriptor(DOWN_COLOR, "Down Color", defaults.getRedLine()));

        return tab;
    }

    @Override
    protected void postcalculate(DataContext ctx) {
        realRenko = null;
    }

    // public void calcMaHiLo(DataContext ctx,
    // BarSize barsize,
    // Enums.MAMethod maMethod,
    // int maPeriod) {

    // var series = ctx.getDataSeries(barsize);
    // int indexMin = 0;
    // int indexMax = series.size() - 1;
    // if (indexMin <= maPeriod) {
    // indexMin = maPeriod - 1;
    // }

    // Double previousValue = null; // Stocker la dernière valeur disponible

    // for (int index = indexMin; index <= indexMax; ++index) {

    // boolean complete = series.isBarComplete(index);

    // Double maHigh = series.ma(maMethod, index, maPeriod, Enums.BarInput.HIGH);
    // Double maLow = series.ma(maMethod, index, maPeriod, Enums.BarInput.LOW);
    // float close = series.getClose(index);

    // Double valueToSet = null;

    // if (maHigh != null && maLow != null) {
    // if (close < maLow) {
    // valueToSet = maHigh;
    // complete = false;
    // } else if (close > maHigh) {
    // valueToSet = maLow;
    // complete = false;
    // }
    // } else if (previousValue != null) {
    // // Utiliser la dernière valeur disponible si maHigh et maLow sont null
    // valueToSet = previousValue;
    // }

    // if (valueToSet != null) {
    // series.setDouble(index, maValueHiLo, valueToSet);
    // previousValue = valueToSet; // Mettre à jour la dernière valeur
    // }

    // series.setComplete(index, complete);
    // }

    // }

    // @Override
    // // protected void calculate(int index, DataContext ctx) {
    // protected void calculateValues(DataContext ctx) {
    // var settings = getSettings();
    // if (settings == null)
    // return;

    // calcMaHiLo(
    // ctx,
    // BarSize.getBarSize(1),
    // Enums.MAMethod.SMA,
    // 21);

    // calcMaHiLo(
    // ctx,
    // BarSize.getBarSize(5),
    // Enums.MAMethod.SMA,
    // 21);

    // }

    // Méthode pour mettre à jour l'historique de Color
    private void updateColorHistory(String newColor) {
        // Décaler toutes les valeurs d'un pas vers la gauche
        for (int i = 0; i < colorHistory.length - 1; i++) {
            colorHistory[i] = colorHistory[i + 1];
        }
        // Ajouter la nouvelle valeur à la fin du tableau
        colorHistory[colorHistory.length - 1] = newColor;
    }

    // Méthode pour mettre à jour l'historique de Cross
    private void updateCrossHistory(String newCross) {
        // Décaler toutes les valeurs d'un pas vers la gauche
        for (int i = 0; i < crossHistory.length - 1; i++) {
            crossHistory[i] = crossHistory[i + 1];
        }
        // Ajouter la nouvelle valeur à la fin du tableau
        crossHistory[crossHistory.length - 1] = newCross;
    }

    @Override
    protected void calculate(int index, DataContext ctx) {
        var settings = getSettings();
        if (settings == null)
            return;
        // int period = settings.getInteger(PERIOD);
        int period = 20;
        if (index <= period)
            return;
        var series = ctx.getDataSeries();
        // Object input = settings.getInput(INPUT);
        Object input = Enums.BarInput.CLOSE;

        // ticks to points
        double renkoPoints = settings.getInteger(RENKO_POINT) / 4;

        // Instanciation de l'overlay Renko directement
        // gannHighLow = new GannHighLow();

        // debug("gannHighLow.mA1 : " + gannHighLow.mA1);
        // debug("gannHighLow.mA2 : " + gannHighLow.mA2);

        // gannHighLow = new VWAP();

        // DataContext ctxHiLo1 = gannHighLow.getDataContext();
        // var seriesHiLo1 = ctxHiLo1.getDataSeries(BarSize.getBarSize(1));

        // Double hiLo1 = seriesHiLo1.getDouble(index, Values.MA_HIGH_LOW_1);
        // debug("hiLo1 : " + hiLo1);
        // if (mA1 == null || gannHighLow.mA2 == null)
        // return;
        // debug("gannHighLow.mA1 : " + gannHighLow.mA1);

        Double prev = series.getDouble(index - 1, input);
        Double current = series.getDouble(index, input);
        if (prev == null || current == null)
            return;

        // boolean crossAbove = false;
        // boolean crossBelow = false;
        crossAbove = false;
        crossBelow = false;

        Double pCross = series.getDouble(index - 1, Values.CROSS);
        // float close = series.getClose(index);
        // float pClose = series.getClose(index-1);

        Double cross = null;
        // if (pDown != null && close > pDown) trend = 1;
        // else if (pUp != null && close < pUp) trend = -1;
        // else if (pTrend != null) trend = pTrend;

        Double res = null;
        Double supp = null;

        Double close = current;

        if (realRenko == null) {
            // Initialisation lors de la première exécution
            // Trouver le prix inférieur se terminant par une dizaine
            double lowerBound = Math.floor(close / 10) * 10; // Trouver la dizaine inférieure
            double upperBound = lowerBound + renkoPoints; // La dizaine suivante se terminant par 5

            // Si le prix est plus proche de la dizaine suivante (10, 20, etc.)
            if (close > lowerBound + renkoPoints) {
                lowerBound = lowerBound + renkoPoints; // Ajuster pour commencer à 5
                upperBound = lowerBound + renkoPoints; // Ajuster pour aller jusqu'à la dizaine
            }

            realRenko = upperBound;
            previousClose = lowerBound;
            brickSize = Math.abs(realRenko - previousClose);
            // updateColorHistory(realRenko > previousClose);
            if (realRenko > previousClose) {
                updateColorHistory(GREEN);
            } else {
                updateColorHistory(RED);
            }
        } else {
            if (colorHistory[9] == GREEN && close - realRenko >= brickSize) {
                realRenko = realRenko + Math.floor((close - realRenko) / brickSize) * brickSize;
                previousClose = realRenko - brickSize;
                crossAbove = true;
                cross = 1.0;
                // updateColorHistory(true);
                updateColorHistory(GREEN);
                updateCrossHistory(ABOVE);
            } else if (colorHistory[9] == GREEN && realRenko - close >= brickSize * 2) {
                realRenko = realRenko - Math.floor((realRenko - close) / brickSize) * brickSize;
                previousClose = realRenko + brickSize;
                crossBelow = true;
                cross = -1.0;
                // updateColorHistory(false);
                updateColorHistory(RED);
                updateCrossHistory(BELOW);
            } else if (colorHistory[9] == RED && close - realRenko >= brickSize * 2) {
                realRenko = realRenko + Math.floor((close - realRenko) / brickSize) * brickSize;
                previousClose = realRenko - brickSize;
                crossAbove = true;
                cross = 1.0;
                // updateColorHistory(true);
                updateColorHistory(GREEN);
                updateCrossHistory(ABOVE);
            } else if (colorHistory[9] == RED && realRenko - close >= brickSize) {
                realRenko = realRenko - Math.floor((realRenko - close) / brickSize) * brickSize;
                previousClose = realRenko + brickSize;
                crossBelow = true;
                cross = -1.0;
                // updateColorHistory(false);
                updateColorHistory(RED);
                updateCrossHistory(BELOW);
            }
        }

        boolean complete = series.isBarComplete(index);
        if (res == null) {
            res = realRenko;
            complete = false;
        }
        if (supp == null) {
            supp = previousClose;
            complete = false;
        }

        // // if (crossBelow && crossHistory[9] == BELOW && colorHistory[8] == RED &&
        // colorHistory[7] == RED && colorHistory[6] == RED && colorHistory[5] == GREEN)
        // {
        // if (inLong == false && crossBelow && colorHistory[8] == RED &&
        // colorHistory[7] == RED && colorHistory[6] == GREEN) {
        // var c = new Coordinate(series.getStartTime(index), res);
        // // if (!series.getBoolean(index, Signals.CROSS_LOW, false)) {
        // // series.setBoolean(index, Signals.CROSS_LOW, true);
        // var marker = settings.getMarker(DOWN_MARKER);
        // String msg = "CROSS_LOW => current: " + format(current) + " res: " +
        // format(res) + " supp: "
        // + format(supp) + " Last Price: " + format(series.getClose(index));
        // if (marker.isEnabled())
        // addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
        // ctx.signal(index, Signals.CROSS_LOW, msg, current);
        // // }
        // inLong = true;
        // }
        // if (inLong == true && crossAbove && colorHistory[8] == GREEN &&
        // colorHistory[7] == RED) {
        // var c = new Coordinate(series.getStartTime(index), res);
        // // if (!series.getBoolean(index, Signals.CROSS_LOW, false)) {
        // // series.setBoolean(index, Signals.CROSS_LOW, true);
        // var marker = settings.getMarker(STOP_DOWN_MARKER);
        // String msg = "CROSS_LOW => current: " + format(current) + " res: " +
        // format(res) + " supp: "
        // + format(supp) + " Last Price: " + format(series.getClose(index));
        // if (marker.isEnabled())
        // addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
        // ctx.signal(index, Signals.CROSS_LOW, msg, current);
        // inLong = false;
        // }

        // // if (crossAbove && crossHistory[9] == ABOVE && colorHistory[8] == GREEN &&
        // colorHistory[7] == GREEN && colorHistory[6] == GREEN && colorHistory[5] ==
        // RED) {
        // if (inShort == false && crossAbove && colorHistory[8] == GREEN &&
        // colorHistory[7] == GREEN && colorHistory[6] == RED) {
        // var c = new Coordinate(series.getStartTime(index), res);
        // // if (!series.getBoolean(index, Signals.CROSS_HIGH, false)) {
        // // series.setBoolean(index, Signals.CROSS_HIGH, true);
        // var marker = settings.getMarker(UP_MARKER);
        // String msg = "CROSS_HIGH => current: " + format(current) + " res: " +
        // format(res) + " supp: "
        // + format(supp) + " Last Price: " + format(series.getClose(index));
        // if (marker.isEnabled())
        // addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
        // ctx.signal(index, Signals.CROSS_HIGH, msg, current);
        // // }
        // inShort = true;
        // }
        // if (inShort == true && crossBelow && colorHistory[8] == RED &&
        // colorHistory[7] == GREEN) {
        // var c = new Coordinate(series.getStartTime(index), res);
        // // if (!series.getBoolean(index, Signals.CROSS_HIGH, false)) {
        // // series.setBoolean(index, Signals.CROSS_HIGH, true);
        // var marker = settings.getMarker(STOP_UP_MARKER);
        // String msg = "CROSS_HIGH => current: " + format(current) + " res: " +
        // format(res) + " supp: "
        // + format(supp) + " Last Price: " + format(series.getClose(index));
        // if (marker.isEnabled())
        // addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
        // ctx.signal(index, Signals.CROSS_HIGH, msg, current);
        // inShort = false;
        // }

        // Boolean stratHigh1 = colorHistory[9] == GREEN && colorHistory[8] == RED &&
        // colorHistory[7] == GREEN
        // && colorHistory[6] == GREEN && colorHistory[5] == GREEN && colorHistory[4] ==
        // GREEN
        // && colorHistory[3] == RED && colorHistory[2] == GREEN && colorHistory[1] ==
        // GREEN;
        // // 4 GREEN - retracement 1 - TP 1
        // Boolean stratLow1 = colorHistory[9] == RED && colorHistory[8] == GREEN &&
        // colorHistory[7] == RED
        // && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] == RED
        // && colorHistory[3] == GREEN && colorHistory[2] == RED && colorHistory[1] ==
        // RED;
        // // 4 RED - retracement 1 - TP 1

        // Boolean stratHigh2 = colorHistory[9] == GREEN && colorHistory[8] == RED &&
        // colorHistory[7] == RED
        // && colorHistory[6] == GREEN && colorHistory[5] == GREEN && colorHistory[4] ==
        // GREEN
        // && colorHistory[3] == GREEN && colorHistory[2] == RED && colorHistory[1] ==
        // RED;
        // // 4 GREEN - retracement 2 - TP 1
        // Boolean stratLow2 = colorHistory[9] == RED && colorHistory[8] == GREEN &&
        // colorHistory[7] == GREEN
        // && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] == RED
        // && colorHistory[3] == RED && colorHistory[2] == GREEN && colorHistory[1] ==
        // GREEN;
        // // 4 RED - retracement 2 - TP 1

        // Boolean stratHigh3 = colorHistory[9] == GREEN && colorHistory[8] == RED &&
        // colorHistory[7] == RED
        // && colorHistory[6] == GREEN && colorHistory[5] == GREEN && colorHistory[4] ==
        // GREEN
        // && colorHistory[3] == GREEN && colorHistory[2] == GREEN;
        // // 5 GREEN - retracement 2 - TP 5
        // Boolean stratLow3 = colorHistory[9] == RED && colorHistory[8] == GREEN &&
        // colorHistory[7] == GREEN
        // && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] == RED
        // && colorHistory[3] == RED && colorHistory[2] == RED;
        // // 5 RED - retracement 2 - TP 5

        // Boolean stratHigh4 = colorHistory[9] == GREEN && colorHistory[8] == RED &&
        // colorHistory[7] == GREEN
        // && colorHistory[6] == GREEN && colorHistory[5] == GREEN && colorHistory[4] ==
        // GREEN
        // && colorHistory[3] == GREEN;
        // // 5 GREEN - retracement 1 - TP 3
        // Boolean stratLow4 = colorHistory[9] == RED && colorHistory[8] == GREEN &&
        // colorHistory[7] == RED
        // && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] == RED
        // && colorHistory[3] == RED;
        // // 5 RED - retracement 1 - TP 3

        // Boolean stratHigh5 = colorHistory[9] == GREEN && colorHistory[8] == RED &&
        // colorHistory[7] == RED
        // && colorHistory[6] == RED && colorHistory[5] == GREEN && colorHistory[4] ==
        // GREEN
        // && colorHistory[3] == GREEN && colorHistory[2] == GREEN && colorHistory[1] ==
        // GREEN
        // && colorHistory[0] == GREEN;
        // // 4 GREEN - retracement 3 - TP 2
        // Boolean stratLow5 = colorHistory[9] == RED && colorHistory[8] == GREEN &&
        // colorHistory[7] == GREEN
        // && colorHistory[6] == GREEN && colorHistory[5] == RED && colorHistory[4] ==
        // RED
        // && colorHistory[3] == RED && colorHistory[2] == RED && colorHistory[1] == RED
        // && colorHistory[0] == RED;
        // // 4 RED - retracement 3 - TP 2

        // Boolean stratHigh6 = colorHistory[9] == GREEN && colorHistory[8] == RED &&
        // colorHistory[7] == GREEN
        // && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] == RED
        // && colorHistory[3] == GREEN && colorHistory[2] == GREEN && colorHistory[1] ==
        // GREEN;
        // // W Bottom - TP 2
        // Boolean stratLow6 = colorHistory[9] == RED && colorHistory[8] == GREEN &&
        // colorHistory[7] == RED
        // && colorHistory[6] == GREEN && colorHistory[5] == GREEN && colorHistory[4] ==
        // GREEN
        // && colorHistory[3] == RED && colorHistory[2] == RED && colorHistory[1] ==
        // RED;
        // // W Top - TP 2

        // Boolean stratHigh7 = colorHistory[9] == GREEN && colorHistory[8] == RED &&
        // colorHistory[7] == GREEN
        // && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] == RED
        // && colorHistory[3] == RED;
        // // W Bottom - TP 2
        // Boolean stratLow7 = colorHistory[9] == RED && colorHistory[8] == GREEN &&
        // colorHistory[7] == RED
        // && colorHistory[6] == GREEN && colorHistory[5] == GREEN && colorHistory[4] ==
        // GREEN
        // && colorHistory[3] == GREEN;
        // // W Top - TP 2

        Boolean stratHigh1 = colorHistory[9] == GREEN && colorHistory[8] == RED && colorHistory[7] == RED
                && colorHistory[6] == GREEN && colorHistory[5] == GREEN && colorHistory[4] == GREEN
                && colorHistory[3] == GREEN && colorHistory[2] == GREEN && colorHistory[1] == GREEN;
        // 6 GREEN - retracement 2 - TP 1
        Boolean stratLow1 = colorHistory[9] == RED && colorHistory[8] == GREEN && colorHistory[7] == GREEN
                && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] == RED
                && colorHistory[3] == RED && colorHistory[2] == RED && colorHistory[1] == RED;
        // 6 RED - retracement 2 - TP 1

        Boolean stratHigh2 = colorHistory[9] == GREEN && colorHistory[8] == RED && colorHistory[7] == RED
                && colorHistory[6] == RED && colorHistory[5] == GREEN && colorHistory[4] == GREEN
                && colorHistory[3] == GREEN && colorHistory[2] == GREEN && colorHistory[1] == GREEN;
        // 7 GREEN - retracement 3 - TP 1
        Boolean stratLow2 = colorHistory[9] == RED && colorHistory[8] == GREEN && colorHistory[7] == GREEN
                && colorHistory[6] == GREEN && colorHistory[5] == RED && colorHistory[4] == RED
                && colorHistory[3] == RED && colorHistory[2] == RED && colorHistory[1] == RED;
        // 7 RED - retracement 3 - TP 1

        Boolean stratHigh3 = colorHistory[9] == GREEN && colorHistory[8] == RED && colorHistory[7] == GREEN
                && colorHistory[6] == GREEN && colorHistory[5] == GREEN && colorHistory[4] == GREEN
                && colorHistory[3] == RED && colorHistory[2] == GREEN && colorHistory[1] == GREEN;
        // 4 GREEN - double retracement 1 - TP 1
        Boolean stratLow3 = colorHistory[9] == RED && colorHistory[8] == GREEN && colorHistory[7] == RED
                && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] == RED
                && colorHistory[3] == GREEN && colorHistory[2] == RED && colorHistory[1] == RED;
        // 4 RED - double retracement 1 - TP 1

        Boolean stratHigh4 = colorHistory[9] == GREEN && colorHistory[8] == RED && colorHistory[7] == RED
                && colorHistory[6] == GREEN && colorHistory[5] == GREEN && colorHistory[4] == GREEN
                && colorHistory[3] == GREEN && colorHistory[2] == RED && colorHistory[1] == RED;
        // 6 GREEN - retracement 2 - TP 1
        Boolean stratLow4 = colorHistory[9] == RED && colorHistory[8] == GREEN && colorHistory[7] == GREEN
                && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] == RED
                && colorHistory[3] == RED && colorHistory[2] == GREEN && colorHistory[1] == GREEN;
        // 6 RED - retracement 2 - TP 1
        
        Boolean stratHigh5 = colorHistory[9] == GREEN && colorHistory[8] == RED && colorHistory[7] == GREEN
                && colorHistory[6] == GREEN && colorHistory[5] == GREEN && colorHistory[4] == GREEN
                && colorHistory[3] == GREEN && colorHistory[2] == GREEN && colorHistory[1] == GREEN;
        // 6 GREEN - retracement 2 - TP 1
        Boolean stratLow5 = colorHistory[9] == RED && colorHistory[8] == GREEN && colorHistory[7] == RED
                && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] == RED
                && colorHistory[3] == RED && colorHistory[2] == RED && colorHistory[1] == RED;
        // 6 RED - retracement 2 - TP 1

        // Boolean stratHigh4 = colorHistory[9] == GREEN && colorHistory[8] == RED &&
        // colorHistory[7] == GREEN
        // && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] ==
        // GREEN
        // && colorHistory[3] == GREEN && colorHistory[2] == GREEN && colorHistory[1] ==
        // GREEN;
        // // W Bottom - retracement 2 - TP 1
        // Boolean stratLow4 = colorHistory[9] == RED && colorHistory[8] == GREEN &&
        // colorHistory[7] == RED
        // && colorHistory[6] == GREEN && colorHistory[5] == RED && colorHistory[4] ==
        // RED
        // && colorHistory[3] == RED && colorHistory[2] == RED && colorHistory[1] ==
        // RED;
        // // W Top - retracement 2 - TP 1

        // Boolean stratHigh = stratHigh1;
        // Boolean stratLow = stratLow1;
        // Boolean stratHigh = stratHigh1 || stratHigh2;
        // Boolean stratLow = stratLow1 || stratLow2;
        // Boolean stratHigh = stratHigh1 || stratHigh2 || stratHigh3;
        // Boolean stratLow = stratLow1 || stratLow2 || stratLow3;
        // Boolean stratHigh = stratHigh1 || stratHigh2 || stratHigh3 || stratHigh4;
        // Boolean stratLow = stratLow1 || stratLow2 || stratLow3 || stratLow4;
        Boolean stratHigh = stratHigh1 || stratHigh2 || stratHigh3 || stratHigh4 ||
        stratHigh5;
        Boolean stratLow = stratLow1 || stratLow2 || stratLow3 || stratLow4 ||
        stratLow5;
        // Boolean stratHigh = stratHigh1 || stratHigh2 || stratHigh3 || stratHigh4 ||
        // stratHigh5
        // || stratHigh6 || stratHigh7;
        // Boolean stratLow = stratLow1 || stratLow2 || stratLow3 || stratLow4 ||
        // stratLow5
        // || stratLow6 || stratLow7;

        // if (crossBelow && crossHistory[9] == BELOW && colorHistory[8] == RED &&
        // colorHistory[7] == RED && colorHistory[6] == RED && colorHistory[5] == GREEN)
        // {
        if (crossBelow && colorHistory[8] == GREEN && stratLow) {
            var c = new Coordinate(series.getStartTime(index), res);
            // if (!series.getBoolean(index, Signals.CROSS_LOW, false)) {
            // series.setBoolean(index, Signals.CROSS_LOW, true);
            var marker = settings.getMarker(DOWN_MARKER);
            String msg = "CROSS_LOW => current: " + format(current) + " res: " + format(res) + " supp: "
                    + format(supp) + " Last Price: " + format(series.getClose(index));
            if (marker.isEnabled())
                addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
            ctx.signal(index, Signals.CROSS_LOW, msg, current);
            // }
        }

        // if (crossAbove && crossHistory[9] == ABOVE && colorHistory[8] == GREEN &&
        // colorHistory[7] == GREEN && colorHistory[6] == GREEN && colorHistory[5] ==
        // RED) {
        if (crossAbove && colorHistory[8] == RED && stratHigh) {
            var c = new Coordinate(series.getStartTime(index), res);
            // if (!series.getBoolean(index, Signals.CROSS_HIGH, false)) {
            // series.setBoolean(index, Signals.CROSS_HIGH, true);
            var marker = settings.getMarker(UP_MARKER);
            String msg = "CROSS_HIGH => current: " + format(current) + " res: " + format(res) + " supp: "
                    + format(supp) + " Last Price: " + format(series.getClose(index));
            if (marker.isEnabled())
                addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
            ctx.signal(index, Signals.CROSS_HIGH, msg, current);
            // }
        }

        // Set colors for the Renko bricks
        Color brickColor = colorHistory[9] == GREEN ? settings.getColor(UP_COLOR) : settings.getColor(DOWN_COLOR);

        // Plot the Renko lines
        series.setPathColor(index - 1, Values.LOW, brickColor);
        series.setPathColor(index - 1, Values.HIGH, brickColor);

        series.setDouble(index - 1, Values.LOW, supp);
        series.setDouble(index, Values.HIGH, res);

        series.setDouble(index, Values.CROSS, cross);

        series.setComplete(index, complete);
    }
}