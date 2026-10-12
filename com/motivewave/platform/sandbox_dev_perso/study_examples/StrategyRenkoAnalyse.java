package custom_studies;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import custom_studies.strategies.strategy_core_v2.StrategyAnalysis;
import custom_studies.strategies.strategy_core_v2.StrategyCorePattern;
import custom_studies.strategies.strategy_core_v2.utils.Helper;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.PatternCheckResult;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.PatternCoreResult;

// /** Renko Simple Overlay */
@StudyHeader(namespace = "com.sbdev", id = "STRATEGY_RENKO_ANALYSE", name = "Startegy Renko Analyse", label = "Startegy Renko Analyse", desc = "Startegy Renko Analyse", menu = "Custom Strategies", overlay = true, signals = true, strategy = true, autoEntry = true, manualEntry = false, supportsStopPL = true, supportsUnrealizedPL = true, supportsRealizedPL = true, supportsTotalPL = true, supportsTargetPL = true, supportsRiskRatio = true, showTradeOptions = true, requiresBarUpdates = true)
public class StrategyRenkoAnalyse extends Study {

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

    protected String[] hiLoColor1History = new String[20]; // Historique des 10 derniers états
    protected String[] hiLoColor2History = new String[20]; // Historique des 10 derniers états

    protected Double[] dowLineUpPriceHistory = new Double[3]; // Historique des 10 derniers états
    protected Double[] dowLineDownPriceHistory = new Double[3]; // Historique des 10 derniers états

    protected Double[] adxHistory = new Double[20]; // Historique des 20 derniers états

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
        HIGH, LOW, CROSS, MA_HILO_1, MA_HILO_2,
        ADX,
        // PDI,
        // NDI,
        DX,
        PDM,
        NDM,
        TR,
        // HIST
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

    // String signalStrategyName = null;
    StrategyAnalysis analysis = new StrategyAnalysis();

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

        // ADX
        // if (useRenko) {

        initialiseADX(index, ctx);
        // }

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

    protected void initialiseADX(int index, DataContext ctx) {
        var seriesADX = ctx.getDataSeries();
        int period = 14;

        if (seriesADX == null || seriesADX.size() <= period)
            return;

        // Calculate the +DM, -DM and TR
        Float pDM = seriesADX.getPositiveDM(index);
        Float nDM = seriesADX.getNegativeDM(index);
        Float tr = seriesADX.getTrueRange(index);

        seriesADX.setFloat(index, Values.PDM, pDM);
        seriesADX.setFloat(index, Values.NDM, nDM);
        seriesADX.setFloat(index, Values.TR, tr);

        if (index <= period)
            return; // not enough data to calculate the first set of averages

        // Calculate the Average +DM, -DM and TR
        Double PDMa = seriesADX.smma(index, period, Values.PDM);
        Double NDMa = seriesADX.smma(index, period, Values.NDM);
        Double TRa = seriesADX.smma(index, period, Values.TR);

        if (PDMa == null || NDMa == null || TRa == null)
            return;

        // Determine the DX
        double DX = Math.abs((PDMa - NDMa)) / (PDMa + NDMa) * 100;
        seriesADX.setDouble(index, Values.DX, DX);

        Double ADX = seriesADX.smma(index, period, Values.DX);
        if (ADX == null)
            return;

        seriesADX.setDouble(index, Values.ADX, ADX);
        // return ADX;

        // debug("Renko Overlay ADX : " + ADX);

        seriesADX.setComplete(index);

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

    // Méthode pour mettre à jour l'historique de dowLineUpPriceHistory
    private void updateAdxHistory(Double newAdx) {
        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = adxHistory.length - 1; i > 0; i--) {
            adxHistory[i] = adxHistory[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        adxHistory[0] = newAdx;
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

    protected <getTimestampFromIndex> void strategySignal(int index, DataContext ctx) {

        var settings = getSettings();
        if (settings == null)
            return;
        var series = ctx.getDataSeries();

        // boolean useTimeframe1 = settings.getBoolean(TF_ENABLED_1);
        // boolean useTimeframe2 = settings.getBoolean(TF_ENABLED_2);

        Double priceSupp = series.getDouble(index - 1, Values.LOW);
        Double priceRes = series.getDouble(index, Values.HIGH);
        Float priceClose = series.getClose(index);
        // Double close = Double.valueOf(series.getClose(index).toString()) ;
        Float priceHigh = series.getHigh(index);
        Float priceLow = series.getLow(index);

        if (priceSupp == null || priceRes == null)
            return;

        // ANALYSE STRATEGY

        // analysis.analyseSignalEntry(null, priceRes);
        analysis.analyseSignalEntry(priceHigh, priceLow);

        // SIGNALS STRATEGY
        // debug("ADX strategy signal : " + series.getDouble(index, Values.ADX));
        // Instancier le moteur de stratégie
        StrategyCorePattern strategyCore = new StrategyCorePattern(
                colorHistory,
                hiLoColor1History,
                hiLoColor2History,
                hiLoPrice1History,
                hiLoPrice2History,
                priceRes,
                priceClose);

        String popupMsgIndex = "Index : " + index + " => Current : " + format(priceRes);
        String popupMsgInfo = popupMsgIndex;
        String popupMsgAnalyse = analysis.printStats();

        List<PatternCheckResult> results = strategyCore.checkPattern();

        for (PatternCheckResult result : results) {

            String popupMsgStrat = "\n Pattern: " + result.patternName
                    + "\n Signal : long=" + result.okLong() + ", short=" + result.okShort()
                    + "\n Direction : high=" + result.direction.high + ", low=" + result.direction.low
                    + "\n Strength : high=" + result.strength.high + ", low=" + result.strength.low
                    + "\n Renko : high=" + result.renko.high + ", low=" + result.renko.low
                    + "\n Hilo : high=" + result.hilo.high + ", low=" + result.hilo.low
                    + "\n hiLoPrice1History[0] : " + hiLoPrice1History[0]
                    + "\n hiLoPrice1History[1] : " + hiLoPrice1History[1]
                    + "\n hiLoPrice1History[2] : " + hiLoPrice1History[2]
                    + "\n hiLoPrice2History[0] : " + hiLoPrice2History[0]
                    + "\n hiLoPrice2History[1] : " + hiLoPrice2History[1]
                    + "\n hiLoPrice2History[2] : " + hiLoPrice2History[2]
                    + "\n ADX : " + series.getDouble(index, Values.ADX);

            String popupMsgSignal = popupMsgIndex + popupMsgStrat + popupMsgAnalyse;

            popupMsgInfo += popupMsgStrat;

            if (crossAbove && result.okLong()) {
                var c = new Coordinate(series.getStartTime(index), priceRes);
                var marker = settings.getMarker(UP_MARKER);
                String msg = popupMsgSignal;
                if (marker.isEnabled())
                    addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
                ctx.signal(index, Signals.CROSS_HIGH, msg, priceSupp);

                analysis.setCurrentStrategy(result.patternName, ctx.getInstrument().getSymbol(), 0, result.bracket.tp, result.bracket.sl);
                analysis.analyseSignalEntryInit(series.getEndTime(index), StrategyAnalysis.Direction.HIGH, priceClose);

            }

            if (crossBelow && result.okShort()) {
                var c = new Coordinate(series.getStartTime(index), priceRes);
                var marker = settings.getMarker(DOWN_MARKER);
                String msg = popupMsgSignal;
                if (marker.isEnabled())
                    addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
                ctx.signal(index, Signals.CROSS_LOW, msg, priceSupp);

                analysis.setCurrentStrategy(result.patternName, ctx.getInstrument().getSymbol(), 0, result.bracket.tp, result.bracket.sl);
                analysis.analyseSignalEntryInit(series.getEndTime(index), StrategyAnalysis.Direction.LOW, priceClose);

            }
        }

        boolean useRenko = settings.getBoolean(RENKO_ENABLED);
        boolean useInfo = settings.getBoolean(INFO_ENABLED);

        if (useRenko && useInfo) {

            if (crossAbove) {
                var c = new Coordinate(series.getStartTime(index), priceRes);
                var marker = settings.getMarker(INFO_MARKER);
                String msg = popupMsgInfo;
                if (marker.isEnabled())
                    addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
            }

            if (crossBelow) {
                var c = new Coordinate(series.getStartTime(index), priceRes);
                var marker = settings.getMarker(INFO_MARKER);
                String msg = popupMsgInfo;
                if (marker.isEnabled())
                    addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
            }

        }

    }

}