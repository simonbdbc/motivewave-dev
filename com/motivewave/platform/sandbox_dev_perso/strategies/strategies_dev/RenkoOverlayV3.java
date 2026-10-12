package custom_studies.strategies.strategies_dev;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.StringDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.draw.Line;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Plot;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

import custom_studies.strategies.strategies_dev.strategies_patterns_dev.PatternStrategyV5;
import custom_studies.strategies.strategies_dev.strategies_patterns_dev.PatternStrategyV6;
import custom_studies.strategies.strategy_core.StrategyCorePattern;
import custom_studies.strategies.strategy_core.HelperFormat.PatternCheckResult;
import custom_studies.strategies.strategy_core.HelperFormat.PatternCoreResult;

// /** Renko Simple Overlay */
@StudyHeader(namespace = "com.sbdev", id = "RENKO_OVERLAY_V3", name = "Renko Overlay V3", label = "Renko Overlay V3", desc = "Displays a simple Renko overlay based on user settings", menu = "Custom Studies", overlay = true, signals = true, studyOverlay = true)
public class RenkoOverlayV3 extends Study {

    protected Double brickSize = null; // Taille de la brique
    protected Double previousClose = null; // Renko open
    protected Double realRenko = null; // Renko close

    // derniers états
    protected String[] colorHistory = new String[20]; // Historique des 10 derniers états
    final static String GREEN = "green";
    final static String RED = "red";
    protected String[] crossHistory = new String[10]; // Historique des 10 derniers états
    final static String ABOVE = "above";
    final static String BELOW = "below";

    protected Double[] hiLoPrice1History = new Double[20]; // Historique des 20 derniers états
    protected Double[] hiLoPrice2History = new Double[20]; // Historique des 20 derniers états

    protected Double hilo1 = null;
    protected Double hilo2 = null;

    protected String[] hiLoColor1History = new String[10]; // Historique des 10 derniers états
    protected String[] hiLoColor2History = new String[10]; // Historique des 10 derniers états

    protected Double[] dowLineUpPriceHistory = new Double[3]; // Historique des 10 derniers états
    protected Double[] dowLineDownPriceHistory = new Double[3]; // Historique des 10 derniers états

    // private Boolean inLong = false;
    // private Boolean inShort = false;
    public boolean crossAbove = false;
    public boolean crossBelow = false;

    final static String RENKO_ENABLED = "renkoEnabled";

    final static String INPUT = "input";
    final static String PERIOD = "period";
    final static String RENKO_POINT = "renkoPoint";
    final static String UP_MARKER = "upMarker";
    final static String DOWN_MARKER = "downMarker";
    final static String TOP_PATH = "topPath";
    final static String BOTTOM_PATH = "bottomPath";
    final static String TOP_IND = "topInd";
    final static String BOTTOM_IND = "bottomInd";
    final static String UP_COLOR = "upColor";
    final static String DOWN_COLOR = "downColor";

    final static String INFO_ENABLED = "infoEnabled";
    final static String INFO_MARKER = "infoMarker";

    enum Values {
        HIGH, LOW, CROSS, MA_HILO_1, MA_HILO_2
    }

    enum Signals {
        CROSS_HIGH, CROSS_LOW, CROSS_ABOVE, CROSS_BELOW

    }

    // Define custom input keys
    public static final String TF_ENABLED_1 = "tfEnabled1";
    public static final String TIMEFRAME_1 = "timeframe1";
    public static final String MA_METHOD_1 = "maMethod1";
    public static final String PATH_HILO_1 = "pathHilo1";
    public static final String PERIOD_1 = "period1";

    public static final String TF_ENABLED_2 = "tfEnabled2";
    public static final String TIMEFRAME_2 = "timeframe2";
    public static final String MA_METHOD_2 = "maMethod2";
    public static final String PATH_HILO_2 = "pathHilo2";
    public static final String PERIOD_2 = "period2";

    final static String HIGH_COLOR_1 = "highColor1";
    final static String LOW_COLOR_1 = "lowColor1";
    final static String HIGH_COLOR_2 = "highColor2";
    final static String LOW_COLOR_2 = "lowColor2";

    final static String DOW_LINE_UP_1 = "dowLineUp1";
    final static String DOW_LINE_DOWN_1 = "dowLineDown1";
    // final static String DOW_LINE_UP_PLOT_1 = "dowLineUpPlot1";
    // final static String DOW_LINE_DOWN_PLOT_1 = "dowLineDownPlot1";

    int highsOffset = 3;
    int lowsOffset = 3;

    // private VWAP vwap;

    // public Double mA1 = null;
    // public Double mA2 = null;

    @Override
    public void initialize(Defaults defaults) {

        var sd = createSD();

        sd.addTab(initializeGeneralTab(defaults));

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

        var tab1 = sd.addTab(get("HiLo 1"));

        var grp1 = tab1.addGroup(get("First Timeframe"));
        // Add timeframe selection options
        grp1.addRow(new BooleanDescriptor(TF_ENABLED_1, "Use Timeframe 1", true));
        grp1.addRow(new BarSizeDescriptor(TIMEFRAME_1, "Timeframe 1", BarSize.getBarSize(1)));
        grp1.addRow(new MAMethodDescriptor(MA_METHOD_1, "MA Method 1", Enums.MAMethod.SMA));
        grp1.addRow(new IntegerDescriptor(PERIOD_1, "Period 1", 21, 1, 9999, 1));

        grp1 = tab1.addGroup(get("Display"));
        // Allow the user to change the settings for the paths
        grp1.addRow(new PathDescriptor(PATH_HILO_1, "Hilo Path 1", null, 1.0f, null, true, true, false));

        grp1.addRow(new ColorDescriptor(HIGH_COLOR_1, "High Color 1", defaults.getRedLine()));
        grp1.addRow(new ColorDescriptor(LOW_COLOR_1, "Low Color 1", defaults.getGreenLine()));

        var tab2 = sd.addTab(get("HiLo 2"));

        var grp2 = tab2.addGroup(get("Second Timeframe"));
        // Add timeframe selection options
        grp2.addRow(new BooleanDescriptor(TF_ENABLED_2, "Use Timeframe 2", true));
        grp2.addRow(new BarSizeDescriptor(TIMEFRAME_2, "Timeframe 2", BarSize.getBarSize(5)));
        grp2.addRow(new MAMethodDescriptor(MA_METHOD_2, "MA Method 2", Enums.MAMethod.SMA));
        grp2.addRow(new IntegerDescriptor(PERIOD_2, "Period 2", 21, 1, 9999, 1));

        grp2 = tab2.addGroup(get("Display"));
        // Allow the user to change the settings for the paths
        grp2.addRow(new PathDescriptor(PATH_HILO_2, "Hilo Path 2", null, 1.0f, null, true, true, false));

        grp2.addRow(new ColorDescriptor(HIGH_COLOR_2, "High Color 2", defaults.getRedLine()));
        grp2.addRow(new ColorDescriptor(LOW_COLOR_2, "Low Color 2", defaults.getGreenLine()));

        var tab3 = sd.addTab(get("Dow Lines"));
        var colors = tab3.addGroup(get("COLORS"));
        colors.addRow(new PathDescriptor(DOW_LINE_UP_1, "Dow Line Up 1", defaults.getGreen(), 1.0f, null, true,
                false, false));
        colors.addRow(new PathDescriptor(DOW_LINE_DOWN_1, "Dow Line Down 1", defaults.getRed(), 1.0f, null, true, false,
                false));

        // Plot dowLineUpPlot1 = new Plot();
        // desc.addPlot(DOW_LINE_UP_PLOT_1, dowLineUpPlot1);
        // Plot dowLineDownPlot1 = new Plot();
        // desc.addPlot(DOW_LINE_DOWN_PLOT_1, dowLineDownPlot1);

        // Declare paths for drawing
        desc.declarePath(Values.MA_HILO_1, PATH_HILO_1);
        desc.declarePath(Values.MA_HILO_2, PATH_HILO_2);

        // Signals
        desc.declareSignal(Signals.CROSS_ABOVE, "PRICE_CROSS_ABOVE");
        desc.declareSignal(Signals.CROSS_BELOW, "PRICE_CROSS_BELOW");
    }

    protected SettingTab initializeGeneralTab(Defaults defaults) {
        var tab = new SettingTab("Renko");

        // Section 1
        var section1 = tab.addGroup("Inputs");
        section1.addRow(new BooleanDescriptor(RENKO_ENABLED, "Use Renko", true));
        section1.addRow(new IntegerDescriptor(RENKO_POINT, "Renko Ticks", 20, 1, 99, 1));
        section1.addRow(new StringDescriptor("note", "Note", "NQ : 4 ticks = 1 point"));

        // Section 2
        var lines = tab.addGroup("Display");
        lines.addRow(new PathDescriptor(TOP_PATH, "High Path", null, 1.0f, null, true, true, false));
        lines.addRow(new PathDescriptor(BOTTOM_PATH, "Bottom Path", null, 1.0f, null, true, true, false));

        lines.addRow(new ColorDescriptor(UP_COLOR, "Up Color", defaults.getGreenLine()));
        lines.addRow(new ColorDescriptor(DOWN_COLOR, "Down Color", defaults.getRedLine()));

        // Section 3
        var markers = tab.addGroup("Markers");
        markers.addRow(new MarkerDescriptor(UP_MARKER, "Up Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL,
                defaults.getGreen(), defaults.getLineColor(), true, true));
        markers.addRow(new MarkerDescriptor(DOWN_MARKER, "Down Marker", Enums.MarkerType.TRIANGLE,
                Enums.Size.SMALL,
                defaults.getRed(), defaults.getLineColor(), true, true));
        markers.addRow(
                new MarkerDescriptor(INFO_MARKER, "Info Marker", Enums.MarkerType.DIAMOND, Enums.Size.LARGE,
                        defaults.getYellow(), defaults.getLineColor(), true, false),
                new BooleanDescriptor(INFO_ENABLED, "Use Info Marker", false));
        return tab;
    }

    @Override
    public int getMinBars() {
        // return getSettings().getInteger(Inputs.PERIOD)*2;
        int p1 = getSettings().getInteger(PERIOD_1);
        int p2 = getSettings().getInteger(PERIOD_2);
        return Math.max(p1, p2) * 2;
    }

    // @Override
    // public void onLoad(Defaults defaults) {
    // DataContext ctx = getDataContext(); // récupération du contexte de données
    // getTimeframeContext(ctx);
    // }

    // @Override
    // public void onBarUpdate(DataContext ctx) {
    // if (!getSettings().isBarUpdates())
    // return;
    // getTimeframeContext(ctx);
    // }

    // @Override
    // public void onBarClose(DataContext ctx) {
    // getTimeframeContext(ctx);
    // }

    // @Override
    // public void onTick(DataContext ctx, Tick tick) {
    // getTimeframeContext(ctx);
    // }

    // protected void getTimeframeContext(DataContext ctx) {
    // var settings = getSettings();
    // if (settings.getBoolean(TF_ENABLED_1)) {
    // ctx.getDataSeries(settings.getBarSize(TIMEFRAME_1));
    // }
    // if (settings.getBoolean(TF_ENABLED_2)) {
    // ctx.getDataSeries(settings.getBarSize(TIMEFRAME_2));
    // }
    // }

    @Override
    protected void postcalculate(DataContext ctx) {
        realRenko = null;
    }

    @Override
    protected void calculate(int index, DataContext ctx) {
        var settings = getSettings();
        if (settings == null)
            return;
        var series = ctx.getDataSeries();
        double close = series.getClose(index);

        // GANN HILO

        boolean useTimeframe1 = settings.getBoolean(TF_ENABLED_1);

        if (useTimeframe1) {

            int period1 = settings.getInteger(PERIOD_1);

            // Get data series for the specified timeframe
            var tfSeries1 = ctx.getDataSeries(getSettings().getBarSize(TIMEFRAME_1));
            if (tfSeries1 == null || tfSeries1.size() <= period1)
                return;

            // Get the time of the current bar
            long time = series.getStartTime(index);

            // Find the index in the timeframe data that corresponds to or precedes this
            // time
            int tfIndex1 = tfSeries1.findIndex(time);
            if (tfIndex1 < 0 || tfIndex1 < period1)
                return;

            Double maHigh1 = tfSeries1.ma(settings.getMAMethod(MA_METHOD_1), tfIndex1,
                    period1,
                    Enums.BarInput.HIGH);
            if (maHigh1 == null || maHigh1 == 0.00)
                return;

            Double maLow1 = tfSeries1.ma(settings.getMAMethod(MA_METHOD_1), tfIndex1,
                    period1,
                    Enums.BarInput.LOW);
            if (maLow1 == null || maLow1 == 0.00)
                return;

            // debug("maHigh1: " + maHigh1);
            // debug("maLow1: " + maLow1);

            // if (maHigh1 != null && maLow1 != null) {
            if (close < maLow1) {
                series.setDouble(index, Values.MA_HILO_1, maHigh1);
                series.setPathColor(index, Values.MA_HILO_1, settings.getColor(HIGH_COLOR_1));
                // updateHiLoPrice1History(maHigh1);
                // hilo1 = maHigh1 != 0.00 ? maHigh1 : hiLoPrice1History[0];
                hilo1 = maHigh1;
                // updateHiLoColor1History(RED);
            } else if (close > maHigh1) {
                series.setDouble(index, Values.MA_HILO_1, maLow1);
                series.setPathColor(index, Values.MA_HILO_1, settings.getColor(LOW_COLOR_1));
                // updateHiLoPrice1History(maLow1);
                // hilo1 = maLow1 != 0.00 ? maLow1 : hiLoPrice1History[0];
                hilo1 = maLow1;
                // updateHiLoColor1History(GREEN);
            }
            // }
            series.setComplete(index);

        }

        boolean useTimeframe2 = settings.getBoolean(TF_ENABLED_2);

        if (useTimeframe2) {

            int period2 = settings.getInteger(PERIOD_2);

            // Get data series for the specified timeframe
            var tfSeries2 = ctx.getDataSeries(getSettings().getBarSize(TIMEFRAME_2));
            if (tfSeries2 == null || tfSeries2.size() <= period2)
                return;

            // Get the time of the current bar
            long time = series.getStartTime(index);

            // Find the index in the timeframe data that corresponds to or precedes this
            // time
            int tfIndex2 = tfSeries2.findIndex(time);
            if (tfIndex2 < 0 || tfIndex2 < period2)
                return;

            Double maHigh2 = tfSeries2.ma(settings.getMAMethod(MA_METHOD_2), tfIndex2,
                    period2,
                    Enums.BarInput.HIGH);
            if (maHigh2 == null || maHigh2 == 0.00)
                return;

            Double maLow2 = tfSeries2.ma(settings.getMAMethod(MA_METHOD_2), tfIndex2,
                    period2,
                    Enums.BarInput.LOW);
            if (maLow2 == null || maLow2 == 0.00)
                return;

            // debug("maHigh2: " + maHigh2);
            // debug("maLow2: " + maLow2);

            // if (maHigh2 != null && maLow2 != null) {
            if (close < maLow2) {
                series.setDouble(index, Values.MA_HILO_2, maHigh2);
                series.setPathColor(index, Values.MA_HILO_2, settings.getColor(HIGH_COLOR_2));
                // updateHiLoPrice2History(maHigh2);
                // hilo2 = maHigh2 != 0.00 ? maHigh2 : hiLoPrice2History[0];
                hilo2 = maHigh2;
                // updateHiLoColor2History(RED);
            } else if (close > maHigh2) {
                series.setDouble(index, Values.MA_HILO_2, maLow2);
                series.setPathColor(index, Values.MA_HILO_2, settings.getColor(LOW_COLOR_2));
                // updateHiLoPrice2History(maLow2);
                // hilo2 = maLow2 != 0.00 ? maLow2 : hiLoPrice2History[0];
                hilo2 = maLow2;
                // updateHiLoColor2History(GREEN);
            }
            // }
            series.setComplete(index);

        }

        // RENKO

        boolean useRenko = settings.getBoolean(RENKO_ENABLED);

        if (useRenko) {

            int period = 20;
            if (index <= period)
                return;
            // Object input = settings.getInput(INPUT);
            Object input = Enums.BarInput.CLOSE;

            if (!series.isBarComplete(index)) {
                return; // On ignore si la barre n'est pas finie
            }

            // double close = series.getClose(index);

            // ticks to points
            double renkoPoints = settings.getInteger(RENKO_POINT) / 4;

            Double prev = series.getDouble(index - 1, input);
            Double current = series.getDouble(index, input);
            if (prev == null || current == null)
                return;

            crossAbove = false;
            crossBelow = false;

            Double cross = null;

            Double res = null;
            Double supp = null;

            Double currentClose = current;

            if (realRenko == null) {
                // Initialisation lors de la première exécution
                // Trouver le prix inférieur se terminant par une dizaine
                double lowerBound = Math.floor(currentClose / 10) * 10; // Trouver la dizaine inférieure
                double upperBound = lowerBound + renkoPoints; // La dizaine suivante se terminant par 5

                // Si le prix est plus proche de la dizaine suivante (10, 20, etc.)
                if (currentClose > lowerBound + renkoPoints) {
                    lowerBound = lowerBound + renkoPoints; // Ajuster pour commencer à 5
                    upperBound = lowerBound + renkoPoints; // Ajuster pour aller jusqu'à la dizaine
                }

                realRenko = upperBound;
                previousClose = lowerBound;
                brickSize = Math.abs(realRenko - previousClose);
                // updateColorHistory(realRenko > previousClose);
                if (realRenko > previousClose) {
                    updateColorHistory(GREEN);
                    autoUpdateHiLoPriceHistory();
                    autoUpdateHiLoColorHistory(close);
                } else {
                    updateColorHistory(RED);
                    autoUpdateHiLoPriceHistory();
                    autoUpdateHiLoColorHistory(close);
                }
            } else {
                if (colorHistory[0] == GREEN && currentClose - realRenko >= brickSize) {
                    realRenko = realRenko + Math.floor((currentClose - realRenko) / brickSize) * brickSize;
                    previousClose = realRenko - brickSize;
                    crossAbove = true;
                    cross = 1.0;
                    // updateColorHistory(true);
                    updateColorHistory(GREEN);
                    updateCrossHistory(ABOVE);
                    autoUpdateHiLoPriceHistory();
                    autoUpdateHiLoColorHistory(close);
                } else if (colorHistory[0] == GREEN && realRenko - currentClose >= brickSize * 2) {
                    realRenko = realRenko - Math.floor((realRenko - currentClose) / brickSize) * brickSize;
                    previousClose = realRenko + brickSize;
                    crossBelow = true;
                    cross = -1.0;
                    // updateColorHistory(false);
                    updateColorHistory(RED);
                    updateCrossHistory(BELOW);
                    autoUpdateHiLoPriceHistory();
                    autoUpdateHiLoColorHistory(close);
                } else if (colorHistory[0] == RED && currentClose - realRenko >= brickSize * 2) {
                    realRenko = realRenko + Math.floor((currentClose - realRenko) / brickSize) * brickSize;
                    previousClose = realRenko - brickSize;
                    crossAbove = true;
                    cross = 1.0;
                    // updateColorHistory(true);
                    updateColorHistory(GREEN);
                    updateCrossHistory(ABOVE);
                    autoUpdateHiLoPriceHistory();
                    autoUpdateHiLoColorHistory(close);
                } else if (colorHistory[0] == RED && realRenko - currentClose >= brickSize) {
                    realRenko = realRenko - Math.floor((realRenko - currentClose) / brickSize) * brickSize;
                    previousClose = realRenko + brickSize;
                    crossBelow = true;
                    cross = -1.0;
                    // updateColorHistory(false);
                    updateColorHistory(RED);
                    updateCrossHistory(BELOW);
                    autoUpdateHiLoPriceHistory();
                    autoUpdateHiLoColorHistory(close);
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

            // Set colors for the Renko bricks
            Color brickColor = colorHistory[0] == GREEN ? settings.getColor(UP_COLOR)
                    : settings.getColor(DOWN_COLOR);

            // Plot the Renko lines
            series.setPathColor(index - 1, Values.LOW, brickColor);
            series.setPathColor(index - 1, Values.HIGH, brickColor);

            series.setDouble(index - 1, Values.LOW, supp);
            series.setDouble(index, Values.HIGH, res);

            series.setDouble(index, Values.CROSS, cross);

            if (series.isBarComplete(index))
                series.setComplete(index, complete);

            // series.setComplete(index, complete);

            // clearFigures();

            // List<Figure> figures = new ArrayList<>();

            if (colorHistory[0] == GREEN && crossHistory[0] == ABOVE) {

                if (prevLineUp1 != null) {
                    if (dowLineUpPriceHistory[0] < res) {

                        // removeFigure(prevLineDown1);
                        // prevLineDown1 = null;

                        removeFigure(prevLineUp1);
                        prevLineUp1 = manageDowLines(index, ctx, res, prevLineUp1, DOW_LINE_UP_1);
                    }
                } else {
                    prevLineUp1 = manageDowLines(index, ctx, res, prevLineUp1, DOW_LINE_UP_1);
                }

            }
            if (colorHistory[0] == RED && crossHistory[0] == BELOW) {

                if (prevLineDown1 != null) {

                    if (dowLineDownPriceHistory[0] > res) {

                        // removeFigure(prevLineUp1);
                        // prevLineUp1 = null;

                        removeFigure(prevLineDown1);
                        prevLineDown1 = manageDowLines(index, ctx, res, prevLineDown1, DOW_LINE_DOWN_1);
                    }
                } else {

                    prevLineDown1 = manageDowLines(index, ctx, res, prevLineDown1, DOW_LINE_DOWN_1);
                }

            }

            // setFigures(0, figures);

            // SIGNALS STRATEGY

            if (isArrayAsNull(colorHistory))
                return;
            if (isArrayAsNull(hiLoColor1History))
                return;
            if (isArrayAsNull(hiLoColor2History))
                return;
            if (isArrayAsNull(hiLoPrice1History))
                return;
            if (isArrayAsNull(hiLoPrice2History))
                return;

            strategySignal(index, ctx);

        }

    }

    // Méthode pour mettre à jour l'historique de Color
    private void updateColorHistory(String newColor) {
        if (newColor == null)
            throw new IllegalArgumentException("updateColorHistory(String newColor) est null.");

        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = colorHistory.length - 1; i > 0; i--) {
            colorHistory[i] = colorHistory[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        colorHistory[0] = newColor;
    }

    // Méthode pour mettre à jour l'historique de Cross
    private void updateCrossHistory(String newCross) {
        if (newCross == null)
            throw new IllegalArgumentException("updateCrossHistory(String newCross) est null.");

        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = crossHistory.length - 1; i > 0; i--) {
            crossHistory[i] = crossHistory[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        crossHistory[0] = newCross;
    }

    // Méthode pour mettre à jour l'historique de HiLoPrice1
    private void updateHiLoPrice1History(Double newHiLoPrice1) {
        if (newHiLoPrice1 == null)
            throw new IllegalArgumentException("updateHiLoPrice1History(Double newHiLoPrice1) est null.");

        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = hiLoPrice1History.length - 1; i > 0; i--) {
            hiLoPrice1History[i] = hiLoPrice1History[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        hiLoPrice1History[0] = newHiLoPrice1;
    }

    // Méthode pour mettre à jour l'historique de HiLoPrice2
    private void updateHiLoPrice2History(Double newHiLoPrice2) {
        if (newHiLoPrice2 == null)
            throw new IllegalArgumentException("updateHiLoPrice2History(Double newHiLoPrice2) est null.");

        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = hiLoPrice2History.length - 1; i > 0; i--) {
            hiLoPrice2History[i] = hiLoPrice2History[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        hiLoPrice2History[0] = newHiLoPrice2;
    }

    // Méthode pour mettre à jour l'historique de HiLoPrice
    private void autoUpdateHiLoPriceHistory() {
        if (hilo1 != null) {
            updateHiLoPrice1History(hilo1);
            // } else {
            // throw new IllegalArgumentException("updateHiLoPrice1History(hilo1) est
            // null.");
        }
        if (hilo2 != null) {
            updateHiLoPrice2History(hilo2);
            // } else {
            // throw new IllegalArgumentException("updateHiLoPrice2History(hilo2) est
            // null.");
        }
    }

    // Méthode pour mettre à jour l'historique de HiLoColor1
    private void updateHiLoColor1History(String newHiLoColor1) {
        if (newHiLoColor1 == null)
            throw new IllegalArgumentException("updateHiLoColor1History(String newHiLoColor1) est null.");

        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = hiLoColor1History.length - 1; i > 0; i--) {
            hiLoColor1History[i] = hiLoColor1History[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        hiLoColor1History[0] = newHiLoColor1;
    }

    // Méthode pour mettre à jour l'historique de HiLoColor2
    private void updateHiLoColor2History(String newHiLoColor2) {
        if (newHiLoColor2 == null)
            throw new IllegalArgumentException("updateHiLoColor2History(String newHiLoColor2) est null.");

        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = hiLoColor2History.length - 1; i > 0; i--) {
            hiLoColor2History[i] = hiLoColor2History[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        hiLoColor2History[0] = newHiLoColor2;
    }

    // Méthode pour mettre à jour l'historique de HiLoColor
    private void autoUpdateHiLoColorHistory(double close) {
        if (hiLoPrice1History[0] != null) {
            if (close < hiLoPrice1History[0]) {
                updateHiLoColor1History(RED);
            } else if (close > hiLoPrice1History[0]) {
                updateHiLoColor1History(GREEN);
            }
        }
        if (hiLoPrice2History[0] != null) {
            if (close < hiLoPrice2History[0]) {
                updateHiLoColor2History(RED);
            } else if (close > hiLoPrice2History[0]) {
                updateHiLoColor2History(GREEN);
            }
        }
    }

    // Vérifie que tous les éléments d’un tableau donné sont non nuls.
    protected static <T> boolean isArrayAsNull(T[] tableau) {
        for (T element : tableau) {
            if (element == null) {
                return true;
            }
        }
        return false;
    }

    // Méthode pour mettre à jour l'historique de dowLineUpPriceHistory
    private void updateDowLineUpPriceHistory(Double newDowLinePrice) {
        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = dowLineUpPriceHistory.length - 1; i > 0; i--) {
            dowLineUpPriceHistory[i] = dowLineUpPriceHistory[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        dowLineUpPriceHistory[0] = newDowLinePrice;
    }

    // Méthode pour mettre à jour l'historique de dowLineDownPriceHistory
    private void updateDowLineDownPriceHistory(Double newDowLinePrice) {
        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = dowLineDownPriceHistory.length - 1; i > 0; i--) {
            dowLineDownPriceHistory[i] = dowLineDownPriceHistory[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        dowLineDownPriceHistory[0] = newDowLinePrice;
    }

    // private void removePrevLines(long time)
    // {
    // if (prevSellLine != null && prevSellLine.getTime() == time) {
    // removeFigure(prevSellLine);
    // prevSellLine = null;
    // }
    // if (prevBuyLine != null && prevBuyLine.getTime() == time) {
    // removeFigure(prevBuyLine);
    // prevBuyLine = null;
    // }
    // }
    protected Line manageDowLines(int index, DataContext ctx, double price, Line prevLine, String pathName) {
        var settings = getSettings();
        // if (settings == null)
        // return;
        var series = ctx.getDataSeries();

        // if (prevLine != null)
        // removeFigure(prevLine);
        var path = settings.getPath(pathName);
        var sl = new Coordinate(series.getStartTime(index), price);
        var el = new Coordinate(series.getStartTime(index + highsOffset), price);
        Line bp = new Line(sl, el, path);
        bp.setExtendRightBounds(true);
        addFigure(bp);
        if (pathName == DOW_LINE_UP_1) {
            updateDowLineUpPriceHistory(price);
        }
        if (pathName == DOW_LINE_DOWN_1) {
            updateDowLineDownPriceHistory(price);
        }
        // prevLine = bp;
        return bp;

    }

    private Line prevLineUp1 = null, prevLineDown1 = null;

    protected void strategySignal(int index, DataContext ctx) {

        var settings = getSettings();
        if (settings == null)
            return;
        var series = ctx.getDataSeries();

        // boolean useTimeframe1 = settings.getBoolean(TF_ENABLED_1);
        // boolean useTimeframe2 = settings.getBoolean(TF_ENABLED_2);

        Double priceSupp = series.getDouble(index - 1, Values.LOW);
        Double priceRes = series.getDouble(index, Values.HIGH);

        if (priceSupp == null || priceRes == null)
            return;

        // SIGNALS STRATEGY

        // Instanciation
        PatternStrategyV6 strat = new PatternStrategyV6(
                colorHistory,
                hiLoColor1History,
                hiLoColor2History,
                hiLoPrice1History,
                hiLoPrice2History,
                priceRes,
                series.getClose(index));

        PatternStrategyV6.DirectionResult direction = strat.checkTrendDirection();
        PatternStrategyV6.StrengthResult strength = strat.checkTrendStrength();
        PatternStrategyV6.RenkoResult renko = strat.checkRenkoPatterns();
        PatternStrategyV6.HiloResult hilo = strat.checkHiloPatterns();

        // PatternStrategyV5 strat = new PatternStrategyV5(
        // colorHistory,
        // hiLoColor1History,
        // hiLoColor2History,
        // hiLoPrice1History,
        // hiLoPrice2History,
        // priceRes,
        // series.getClose(index));

        // PatternStrategyV5.DirectionResult direction = strat.checkTrendDirection();

        // PatternStrategyV5.RenkoResult renkoUltraScalp =
        // strat.checkRenkoUltraScalpPatterns();

        // PatternStrategyV5.RenkoResult renko = strat.checkRenkoPatterns();
        // PatternStrategyV5.HiloResult hilo = strat.checkHiloPatterns();
        // PatternStrategyV5.StrengthResult strength = strat.checkTrendStrength();

        // PatternStrategyUltraScalpV1 strat = new PatternStrategyUltraScalpV1(
        // colorHistory,
        // hiLoColor1History,
        // hiLoColor2History,
        // hiLoPrice1History,
        // hiLoPrice2History,
        // priceRes,
        // series.getClose(index));

        // PatternStrategyUltraScalpV1.DirectionResult direction =
        // strat.checkTrendDirection();

        // PatternStrategyUltraScalpV1.ReverseRenkoResult reverseRenko =
        // strat.checkReverseRenkoPatterns();

        // PatternStrategyUltraScalpV1.RenkoResult renko = strat.checkRenkoPatterns();
        // PatternStrategyUltraScalpV1.HiloResult hilo = strat.checkHiloPatterns();
        // PatternStrategyUltraScalpV1.StrengthResult strength =
        // strat.checkTrendStrength();

        // Double priceHiLo1 = 0.00;
        // if (useTimeframe1) {
        // priceHiLo1 = series.getDouble(index - 1, Values.MA_HILO_1);
        // if (priceHiLo1 == null)
        // priceHiLo1 = 0.00;
        // }
        // Double priceHiLo2 = 0.00;
        // if (useTimeframe2) {
        // priceHiLo2 = series.getDouble(index - 1, Values.MA_HILO_2);
        // if (priceHiLo2 == null)
        // priceHiLo2 = 0.00;
        // }

        if (crossAbove && direction.high && strength.high && renko.high && hilo.high) {
            // if (crossAbove && direction.high && renko.high) {
            // if (crossAbove && direction.high && strength.high || crossAbove &&
            // !strength.low) {
            // if (crossAbove && reverseRenko.high && !strength.high) {
            // if (crossAbove && direction.high) {
            // if (crossAbove) {
            var c = new Coordinate(series.getStartTime(index), priceRes);
            var marker = settings.getMarker(UP_MARKER);
            String msg = "CROSS_HIGH => current: " + format(priceRes)
                    + "\n index: " + index
                    + "\n direction.high: " + direction.high
                    + "\n strength.high: " + strength.high
                    + "\n renko.high: " + renko.high
                    + "\n hilo.high: " + hilo.high;
            if (marker.isEnabled())
                addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
            ctx.signal(index, Signals.CROSS_HIGH, msg, priceSupp);
        }

        if (crossBelow && direction.low && strength.low && renko.low && hilo.low) {
            // if (crossBelow && direction.low && renko.low) {
            // if (crossBelow && direction.low && strength.low || crossBelow &&
            // !strength.high) {
            // if (crossBelow && reverseRenko.low && !strength.low) {
            // if (crossBelow && direction.low) {
            // if (crossBelow) {
            var c = new Coordinate(series.getStartTime(index), priceRes);
            var marker = settings.getMarker(DOWN_MARKER);
            String msg = "CROSS_LOW => current: " + format(priceRes)
                    + "\n index: " + index
                    + "\n direction.low: " + direction.low
                    + "\n strength.low: " + strength.low
                    + "\n renko.low: " + renko.low
                    + "\n hilo.low: " + hilo.low;
            if (marker.isEnabled())
                addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
            ctx.signal(index, Signals.CROSS_LOW, msg, priceSupp);
        }

        boolean useRenko = settings.getBoolean(RENKO_ENABLED);
        boolean useInfo = settings.getBoolean(INFO_ENABLED);

        if (useRenko && useInfo) {

            if (crossAbove) {
                var c = new Coordinate(series.getStartTime(index), priceRes);
                var marker = settings.getMarker(INFO_MARKER);
                String msg = "CROSS_HIGH => current: " + format(priceRes)
                        + "\n index: " + index
                        + "\n colorHistory[0]: " + colorHistory[0]
                        + "\n hiLoColor1History[0]: " + hiLoColor1History[0]
                        + "\n hiLoColor2History[0]: " + hiLoColor2History[0]
                        + "\n hiLoPrice1History[0]: " + hiLoPrice1History[0]
                        + "\n hiLoPrice2History[0]: " + hiLoPrice2History[0]
                        + "\n direction.high: " + direction.high
                        + "\n renko.high: " + renko.high
                        + "\n strength.high: " + strength.high
                        + "\n hilo.high: " + hilo.high;
                if (marker.isEnabled())
                    addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
            }

            if (crossBelow) {
                var c = new Coordinate(series.getStartTime(index), priceRes);
                var marker = settings.getMarker(INFO_MARKER);
                String msg = "CROSS_LOW => current: " + format(priceRes)
                        + "\n index: " + index
                        + "\n colorHistory[0]: " + colorHistory[0]
                        + "\n hiLoColor1History[0]: " + hiLoColor1History[0]
                        + "\n hiLoColor2History[0]: " + hiLoColor2History[0]
                        + "\n hiLoPrice1History[0]: " + hiLoPrice1History[0]
                        + "\n hiLoPrice2History[0]: " + hiLoPrice2History[0]
                        + "\n direction.low: " + direction.low
                        + "\n renko.low: " + renko.low
                        + "\n strength.low: " + strength.low
                        + "\n hilo.low: " + hilo.low;
                if (marker.isEnabled())
                    addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
            }

        }
    }

}