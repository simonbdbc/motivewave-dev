package sbdev.joy_ai_project.strategy_v8o3;

import java.util.ArrayList;
import java.util.List;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Instrument;
import com.motivewave.platform.sdk.common.NVP;
import com.motivewave.platform.sdk.common.TimeFrame;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.TifDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Line;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.common.desc.StringDescriptor;
import com.motivewave.platform.sdk.order_mgmt.Order;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

import sbdev.joy_ai_project.strategy_v8o3.util.HistoryManager;
import sbdev.joy_ai_project.strategy_v8o3.util.InterfaceCorePattern;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternCheckResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternGroupKey;
import sbdev.joy_ai_project.strategy_v8o3.util.Helper;

import java.awt.Color;

@StudyHeader(namespace = "com.sbdev", id = "STRATEGY_BY_INTERVAL_V1", name = "Strategy By Interval V1", label = "Strategy By Interval V1", desc = "Strategy Trade Manager", menu = "SB Dev", overlay = true, signals = true, strategy = true, autoEntry = true, manualEntry = false, supportsStopPL = true, supportsUnrealizedPL = true, supportsRealizedPL = true, supportsTotalPL = true, supportsTargetPL = true, supportsRiskRatio = true, showTradeOptions = true, requiresBarUpdates = true)
public class StrategyByIntervalV1 extends Study {

    protected HistoryManager history = new HistoryManager();

    protected Double brickSize = null; // Taille de la brique
    protected Double previousClose = null; // Renko open
    protected Double realRenko = null; // Renko close

    final static String GREEN = "green";
    final static String RED = "red";
    final static String ABOVE = "above";
    final static String BELOW = "below";

    protected Double hiLo1 = null;
    protected Double hiLo2 = null;

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
        HIGH, LOW, CROSS, MA_HILO_1, MA_HILO_2, ADX, DX, PDM, NDM, TR,
    }

    enum Signals {
        LONG, SHORT, CROSS_ABOVE, CROSS_BELOW
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

    private Line prevLineUp1 = null, prevLineDown1 = null;

    int highsOffset = 3;
    int lowsOffset = 3;

    // private VWAP vwap;

    private List<PatternGroupKey> patternGroups = Helper.loadPatternGroupsFromSingleton();
    public static final String STRATEGY_PATTERN_GROUP = "strategyPatternGroup";
    final static String CROSS_INTERVAL_PATTERN_ENABLED = "crossIntervalPatternEnabled";

    public final static String STEP = "step";
    // Entry inputs
    public final static String TRADE_LOTS = "tradeLots", TIME_IN_FORCE = "tif";
    // Exit inputs
    public final static String EXIT = "exit";
    public final static String MAX_LOSS = "maxLoss";
    // Stop Loss
    public final static String BREAK_EVEN_ENABLED = "breakEvenEnabled", BREAK_EVEN = "breakEvenPips",
            COVER = "coverPips", ATR_PERIOD = "atrPeriod", ATR_MULTIPLIER = "atrMultiplier", ATR_ENABLED = "atrEnabled";
    // Risk Session
    public final static String SESSION_MAX_WIN = "sessionMaxWin", SESSION_MAX_LOSS = "sessionMaxLoss",
            MAX_SUCCESSIVE_LOSS_ENABLED = "maxSuccessiveLossEnabled", MAX_SUCCESSIVE_LOSS = "maxSuccessiveLoss";

    public final static long ENTRY_DELAY = 2000;

    protected double takeProfitPattern = 0.00;
    protected double stopLossPattern = 0.00;

    protected int successiveLoss = 0;
    protected Boolean pnlMax = false;
    protected Double pnlSession = 0.00;
    protected Boolean startSession = false;
    protected Boolean isActivated = false;

    @Override
    public void initialize(Defaults defaults) {
        var sd = createSD();

        sd.addTab(initializeStrategyTab(defaults));
        // sd.addTab(getExitStrategyTab());
        sd.addTab(getRiskTab());

        sd.addTab(initializeGeneralTab(defaults));

        // Quick Settings (Tool Bar and Popup Editor)
        sd.addQuickSettings(RENKO_POINT);
        sd.addQuickSettings(TOP_PATH, BOTTOM_PATH);

        var desc = createRD();
        desc.setLabelSettings(INPUT, PERIOD, RENKO_POINT);

        desc.exportValue(new ValueDescriptor(Values.HIGH, "High", new String[] { INPUT, PERIOD }));
        desc.exportValue(new ValueDescriptor(Values.LOW, "Low", new String[] { INPUT, PERIOD }));
        desc.exportValue(new ValueDescriptor(Signals.LONG, Enums.ValueType.BOOLEAN, "Long"));
        desc.exportValue(new ValueDescriptor(Signals.SHORT, Enums.ValueType.BOOLEAN, "Short"));

        desc.declarePath(Values.HIGH, TOP_PATH);
        desc.declarePath(Values.LOW, BOTTOM_PATH);

        desc.declareIndicator(Values.HIGH, TOP_IND);
        desc.declareIndicator(Values.LOW, BOTTOM_IND);

        // Signals
        desc.declareSignal(Signals.LONG, "Long");
        desc.declareSignal(Signals.SHORT, "Short");

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
        grp1.addRow(new PathDescriptor(PATH_HILO_1, "HiLo Path 1", null, 1.0f, null, true, true, false));

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
        grp2.addRow(new PathDescriptor(PATH_HILO_2, "HiLo Path 2", null, 1.0f, null, true, true, false));

        grp2.addRow(new ColorDescriptor(HIGH_COLOR_2, "High Color 2", defaults.getRedLine()));
        grp2.addRow(new ColorDescriptor(LOW_COLOR_2, "Low Color 2", defaults.getGreenLine()));

        var tab3 = sd.addTab(get("Dow Lines"));
        var colors = tab3.addGroup(get("COLORS"));
        colors.addRow(new PathDescriptor(DOW_LINE_UP_1, "Dow Line Up 1", defaults.getGreen(), 1.0f, null, true, false,
                false));
        colors.addRow(new PathDescriptor(DOW_LINE_DOWN_1, "Dow Line Down 1", defaults.getRed(), 1.0f, null, true, false,
                false));

        // Declare paths for drawing
        desc.declarePath(Values.MA_HILO_1, PATH_HILO_1);
        desc.declarePath(Values.MA_HILO_2, PATH_HILO_2);

        // Signals
        desc.declareSignal(Signals.CROSS_ABOVE, "PRICE_CROSS_ABOVE");
        desc.declareSignal(Signals.CROSS_BELOW, "PRICE_CROSS_BELOW");

        sd.addDependency(
                new EnabledDependency(BREAK_EVEN_ENABLED, BREAK_EVEN, COVER, ATR_ENABLED, ATR_MULTIPLIER, ATR_PERIOD));
        sd.addDependency(new EnabledDependency(ATR_ENABLED, ATR_MULTIPLIER, ATR_PERIOD));
        sd.addDependency(new EnabledDependency(MAX_SUCCESSIVE_LOSS_ENABLED, MAX_SUCCESSIVE_LOSS));
        setSettingsDescriptor(sd);

    }

    protected SettingTab initializeStrategyTab(Defaults defaults) {
        var tab = new SettingTab("Strategy");

        // Section 1
        var section1 = tab.addGroup("Patterns");
        List<NVP> types = new ArrayList<>();
        for (int i = 0; i < patternGroups.size(); i++) {
            PatternGroupKey group = patternGroups.get(i);
            types.add(new NVP("Tick Interval : " + group.intervalValue + " --- Take Profit : " + group.tp
                    + " --- Stop Loss : " + group.sl, group.toKeyString()));
        }
        section1.addRow(new DiscreteDescriptor(STRATEGY_PATTERN_GROUP, "Strategy Pattern Group",
                patternGroups.get(0).toKeyString(), types));

        section1.addRow(new BooleanDescriptor(CROSS_INTERVAL_PATTERN_ENABLED, "Cross Interval Pattern Enabled", false));

        var grp = tab.addGroup("Entry");
        grp.addRow(new IntegerDescriptor(TRADE_LOTS, "Trade Lots", 1, 1, 9999, 1),
                new TifDescriptor(TIME_IN_FORCE, "Time in Force", Enums.TIF.GTC));

        grp = tab.addGroup("Break Even");
        grp.addRow(new IntegerDescriptor(BREAK_EVEN, "Break Even", 80, 0, 999, 1),
                new IntegerDescriptor(COVER, "Cover", 0, 0, 99, 1),
                new BooleanDescriptor(BREAK_EVEN_ENABLED, "Break Even Enabled", false));

        grp = tab.addGroup("ATR Stop");
        grp.addRow(new IntegerDescriptor(ATR_PERIOD, "ATR Period", 40, 1, 999, 1),
                new DoubleDescriptor(ATR_MULTIPLIER, "  ATR Multiplier", 5.0, 0.1, 999, 0.1),
                new BooleanDescriptor(ATR_ENABLED, "  ATR Enabled", false));

        return tab;
    }

    protected SettingTab getRiskTab() {
        var tab = new SettingTab("Risk Session");
        var grp = tab.addGroup("Win");
        grp.addRow(new IntegerDescriptor(SESSION_MAX_WIN, "Max Win Session", 700, 0, 9999, 1));

        grp = tab.addGroup("Loss");
        grp.addRow(new IntegerDescriptor(SESSION_MAX_LOSS, "Max Loss Session", 600, 0, 9999, 1));

        grp = tab.addGroup("Successive Loss");
        grp.addRow(new BooleanDescriptor(MAX_SUCCESSIVE_LOSS_ENABLED, "Max Successive Loss Enabled", true));
        grp.addRow(new IntegerDescriptor(MAX_SUCCESSIVE_LOSS, "Max Successive Loss", 2, 1, 99, 1));
        return tab;
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
        markers.addRow(new MarkerDescriptor(DOWN_MARKER, "Down Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL,
                defaults.getRed(), defaults.getLineColor(), true, true));
        markers.addRow(
                new MarkerDescriptor(INFO_MARKER, "Info Marker", Enums.MarkerType.DIAMOND, Enums.Size.LARGE,
                        defaults.getYellow(), defaults.getLineColor(), true, false),
                new BooleanDescriptor(INFO_ENABLED, "Use Info Marker", false));
        return tab;
    }

    @Override
    public int getMinBars() {
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

        calculateHiLo1(index, ctx, close);
        calculateHiLo2(index, ctx, close);
        initialiseADX(index, ctx);

        if (settings.getBoolean(RENKO_ENABLED)) {
            calculateRenko(index, ctx, close);
        }
    }

    private void calculateHiLo1(int index, DataContext ctx, double close) {
        var settings = getSettings();
        if (!settings.getBoolean(TF_ENABLED_1))
            return;

        int period1 = settings.getInteger(PERIOD_1);
        var tfSeries1 = ctx.getDataSeries(settings.getBarSize(TIMEFRAME_1));
        if (tfSeries1 == null || tfSeries1.size() <= period1)
            return;

        long time = ctx.getDataSeries().getStartTime(index);
        int tfIndex1 = tfSeries1.findIndex(time);
        if (tfIndex1 < 0 || tfIndex1 < period1)
            return;

        Double maHigh1 = tfSeries1.ma(settings.getMAMethod(MA_METHOD_1), tfIndex1, period1, Enums.BarInput.HIGH);
        Double maLow1 = tfSeries1.ma(settings.getMAMethod(MA_METHOD_1), tfIndex1, period1, Enums.BarInput.LOW);
        if (maHigh1 == null || maLow1 == null)
            return;

        var series = ctx.getDataSeries();
        if (close < maLow1) {
            series.setDouble(index, Values.MA_HILO_1, maHigh1);
            series.setPathColor(index, Values.MA_HILO_1, settings.getColor(HIGH_COLOR_1));
            hiLo1 = maHigh1;
        } else if (close > maHigh1) {
            series.setDouble(index, Values.MA_HILO_1, maLow1);
            series.setPathColor(index, Values.MA_HILO_1, settings.getColor(LOW_COLOR_1));
            hiLo1 = maLow1;
        }
        series.setComplete(index);
    }

    private void calculateHiLo2(int index, DataContext ctx, double close) {
        var settings = getSettings();
        if (!settings.getBoolean(TF_ENABLED_2))
            return;

        int period2 = settings.getInteger(PERIOD_2);
        var tfSeries2 = ctx.getDataSeries(settings.getBarSize(TIMEFRAME_2));
        if (tfSeries2 == null || tfSeries2.size() <= period2)
            return;

        long time = ctx.getDataSeries().getStartTime(index);
        int tfIndex2 = tfSeries2.findIndex(time);
        if (tfIndex2 < 0 || tfIndex2 < period2)
            return;

        Double maHigh2 = tfSeries2.ma(settings.getMAMethod(MA_METHOD_2), tfIndex2, period2, Enums.BarInput.HIGH);
        Double maLow2 = tfSeries2.ma(settings.getMAMethod(MA_METHOD_2), tfIndex2, period2, Enums.BarInput.LOW);
        if (maHigh2 == null || maLow2 == null)
            return;

        var series = ctx.getDataSeries();
        if (close < maLow2) {
            series.setDouble(index, Values.MA_HILO_2, maHigh2);
            series.setPathColor(index, Values.MA_HILO_2, settings.getColor(HIGH_COLOR_2));
            hiLo2 = maHigh2;
        } else if (close > maHigh2) {
            series.setDouble(index, Values.MA_HILO_2, maLow2);
            series.setPathColor(index, Values.MA_HILO_2, settings.getColor(LOW_COLOR_2));
            hiLo2 = maLow2;
        }
        series.setComplete(index);
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

    private void calculateRenko(int index, DataContext ctx, double close) {
        var settings = getSettings();
        var series = ctx.getDataSeries();

        Object input = Enums.BarInput.CLOSE;

        if (!series.isBarComplete(index)) {
            return; // On ignore si la barre n'est pas finie
        }

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
        Double sup = null;

        Double currPrice = current;

        if (realRenko == null) {
            // Initialisation lors de la première exécution
            // Trouver le prix inférieur se terminant par une dizaine
            double lowerBound = Math.floor(currPrice / 10) * 10; // Trouver la dizaine inférieure
            double upperBound = lowerBound + renkoPoints; // La dizaine suivante se terminant par 5

            // Si le prix est plus proche de la dizaine suivante (10, 20, etc.)
            if (currPrice > lowerBound + renkoPoints) {
                lowerBound = lowerBound + renkoPoints; // Ajuster pour commencer à 5
                upperBound = lowerBound + renkoPoints; // Ajuster pour aller jusqu'à la dizaine
            }

            realRenko = upperBound;
            previousClose = lowerBound;
            brickSize = Math.abs(realRenko - previousClose);
            if (realRenko > previousClose) {
                history.updateRenkoColorHistory(GREEN);
            } else {
                history.updateRenkoColorHistory(RED);
            }
        } else {
            if (history.renkoColorHistory[0].equals(GREEN) && currPrice - realRenko >= brickSize) {
                realRenko += Math.floor((currPrice - realRenko) / brickSize) * brickSize;
                previousClose = realRenko - brickSize;
                crossAbove = true;
                history.updateCrossHistory(ABOVE);
                history.updateRenkoColorHistory(GREEN);
                history.autoUpdateRenkoPriceHistory(realRenko, previousClose);
                history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
                history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History, history.hiLoPrice2History);
            } else if (history.renkoColorHistory[0].equals(GREEN) && realRenko - currPrice >= 2 * brickSize) {
                realRenko -= Math.floor((realRenko - currPrice) / brickSize) * brickSize;
                previousClose = realRenko + brickSize;
                crossBelow = true;
                history.updateCrossHistory(BELOW);
                history.updateRenkoColorHistory(RED);
                history.autoUpdateRenkoPriceHistory(realRenko, previousClose);
                history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
                history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History, history.hiLoPrice2History);
            } else if (history.renkoColorHistory[0].equals(RED) && currPrice - realRenko >= 2 * brickSize) {
                realRenko += Math.floor((currPrice - realRenko) / brickSize) * brickSize;
                previousClose = realRenko - brickSize;
                crossAbove = true;
                history.updateCrossHistory(ABOVE);
                history.updateRenkoColorHistory(GREEN);
                history.autoUpdateRenkoPriceHistory(realRenko, previousClose);
                history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
                history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History, history.hiLoPrice2History);
            } else if (history.renkoColorHistory[0].equals(RED) && realRenko - currPrice >= brickSize) {
                realRenko -= Math.floor((realRenko - currPrice) / brickSize) * brickSize;
                previousClose = realRenko + brickSize;
                crossBelow = true;
                history.updateCrossHistory(BELOW);
                history.updateRenkoColorHistory(RED);
                history.autoUpdateRenkoPriceHistory(realRenko, previousClose);
                history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
                history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History, history.hiLoPrice2History);
            }
        }

        boolean complete = series.isBarComplete(index);
        if (res == null) {
            res = realRenko;
            complete = false;
        }
        if (sup == null) {
            sup = previousClose;
            complete = false;
        }

        // Set colors for the Renko bricks
        Color brickColor = history.renkoColorHistory[0] == GREEN ? settings.getColor(UP_COLOR)
                : settings.getColor(DOWN_COLOR);

        // Plot the Renko lines
        series.setPathColor(index - 1, Values.LOW, brickColor);
        series.setPathColor(index - 1, Values.HIGH, brickColor);

        series.setDouble(index - 1, Values.LOW, sup);
        series.setDouble(index, Values.HIGH, res);

        series.setDouble(index, Values.CROSS, cross);

        if (series.isBarComplete(index))
            series.setComplete(index, complete);

        if (!history.isArrayAsNull(history.renkoColorHistory) && !history.isArrayAsNull(history.crossHistory)
                && !history.isArrayAsNull(history.renkoPriceResHistory)
                && !history.isArrayAsNull(history.renkoPriceSupHistory)) {
            manageDowLinesConditionally(index, ctx, realRenko);
        }

        if (ctx.getDataSeries().getBarSize().isTick()) {
            if (!history.isArrayAsNull(history.renkoColorHistory) && !history.isArrayAsNull(history.hiLoColor1History)
                    && !history.isArrayAsNull(history.hiLoColor2History)
                    && !history.isArrayAsNull(history.hiLoPrice1History)
                    && !history.isArrayAsNull(history.hiLoPrice2History)
                    && !history.isArrayAsNull(history.renkoPriceResHistory)
                    && !history.isArrayAsNull(history.renkoPriceSupHistory)) {
                strategySignal(index, ctx);
            }
        }
    }

    private void manageDowLinesConditionally(int index, DataContext ctx, Double res) {
        if (history.renkoColorHistory[0].equals(GREEN) && history.crossHistory[0].equals(ABOVE)) {
            if (prevLineUp1 == null || history.dowLineUpPriceHistory[0] < history.renkoPriceResHistory[0]) {
                if (prevLineUp1 != null)
                    removeFigure(prevLineUp1);
                prevLineUp1 = manageDowLines(index, ctx, history.renkoPriceResHistory[0], prevLineUp1, DOW_LINE_UP_1);
            }
        } else if (history.renkoColorHistory[0].equals(RED) && history.crossHistory[0].equals(BELOW)) {
            if (prevLineDown1 == null || history.dowLineDownPriceHistory[0] > history.renkoPriceResHistory[0]) {
                if (prevLineDown1 != null)
                    removeFigure(prevLineDown1);
                prevLineDown1 = manageDowLines(index, ctx, history.renkoPriceResHistory[0], prevLineDown1,
                        DOW_LINE_DOWN_1);
            }
        }
    }

    protected Line manageDowLines(int index, DataContext ctx, double price, Line prevLine, String pathName) {
        var settings = getSettings();
        var series = ctx.getDataSeries();

        var path = settings.getPath(pathName);
        var sl = new Coordinate(series.getStartTime(index), price);
        var el = new Coordinate(series.getStartTime(index + highsOffset), price);
        Line bp = new Line(sl, el, path);
        bp.setExtendRightBounds(true);
        addFigure(bp);
        if (pathName == DOW_LINE_UP_1) {
            history.updateDowLineUpPriceHistory(price);
        }
        if (pathName == DOW_LINE_DOWN_1) {
            history.updateDowLineDownPriceHistory(price);
        }
        return bp;

    }

    private void strategySignal(int index, DataContext ctx) {
        var settings = getSettings();
        if (settings == null)
            return;

        var series = ctx.getDataSeries();
        Double priceSup = series.getDouble(index - 1, Values.LOW);
        Double priceRes = series.getDouble(index, Values.HIGH);
        Double priceClose = (double) series.getClose(index);
        if (priceSup == null || priceRes == null)
            return;

        String strategyPatternGroupKey = getSettings().getString(STRATEGY_PATTERN_GROUP);
        PatternGroupKey patternGroup = PatternGroupKey.fromKeyString(strategyPatternGroupKey);

        int interval = settings.getBoolean(CROSS_INTERVAL_PATTERN_ENABLED) ? patternGroup.intervalValue
                : ctx.getDataSeries().getBarSize().getInterval();

        // debug("patternGroup : " + patternGroup);
        String className = String.format("sbdev.joy_ai_project.strategy_v8o3.core_pattern.CorePattern_%d_%d_%d", interval,
                patternGroup.tp, patternGroup.sl);

        try {
            // On instancie la classe dynamiquement
            InterfaceCorePattern strategyCore = (InterfaceCorePattern) Class.forName(className)
                    .getConstructor(String[].class, String[].class, String[].class, Double[].class, Double[].class,
                            Double.class, Double.class)
                    .newInstance(history.renkoColorHistory, history.hiLoColor1History, history.hiLoColor2History,
                            history.hiLoPrice1History, history.hiLoPrice2History, priceRes, priceClose);

            // Utilisation simple et typée de la méthode :
            List<PatternCheckResult> results = strategyCore.checkPattern();
            // System.out.println("Résultats pour " + className + " : " + results.size() + "
            // patterns trouvés.");
            // debug("Résultats pour " + className + " : " + results.size() + " patterns
            // trouvés.");

            String popupMsgIndex = "Index : " + index + " => Current : " + format(priceRes);
            String popupMsgInfo = popupMsgIndex;

            for (PatternCheckResult result : results) {

                String popupMsgStrat = "\n Pattern: " + result.patternName + "\n Signal : long=" + result.okLong()
                        + ", short=" + result.okShort() + "\n Direction : high=" + result.direction.high + ", low="
                        + result.direction.low + "\n Strength : high=" + result.strength.high + ", low="
                        + result.strength.low + "\n Renko : high=" + result.renko.high + ", low=" + result.renko.low
                        + "\n HiLo : high=" + result.hilo.high + ", low=" + result.hilo.low + "\n ADX : "
                        + series.getDouble(index, Values.ADX);

                String popupMsgSignal = popupMsgIndex + popupMsgStrat;

                // popupMsgInfo += popupMsgStrat;

                if (crossAbove || crossBelow) {
                    if (result.okLong()) {
                        // debug("Signal : okLong= " + result.okLong());
                        // debug("patternName : " + result.patternName);
                        // debug("getSymbol : " + ctx.getInstrument().getSymbol());
                        // debug("patternGroup.intervalValue : " + patternGroup.intervalValue);
                        // debug("patternGroup : tp= " + patternGroup.tp + ", sl= " + patternGroup.sl);

                        var c = new Coordinate(series.getStartTime(index), priceRes);
                        var marker = settings.getMarker(UP_MARKER);
                        String msg = popupMsgSignal;
                        if (marker.isEnabled())
                            addFigure(new Marker(c, crossAbove ? Enums.Position.TOP : Enums.Position.BOTTOM, marker,
                                    msg));
                        ctx.signal(index, Signals.LONG, msg, priceSup);
                    }

                    if (result.okShort()) {
                        // debug("Signal : okShort= " + result.okShort());
                        // debug("patternName : " + result.patternName);
                        // debug("getSymbol : " + ctx.getInstrument().getSymbol());
                        // debug("patternGroup.intervalValue : " + patternGroup.intervalValue);
                        // debug("patternGroup : tp= " + patternGroup.tp + ", sl= " + patternGroup.sl);

                        var c = new Coordinate(series.getStartTime(index), priceRes);
                        var marker = settings.getMarker(DOWN_MARKER);
                        String msg = popupMsgSignal;
                        if (marker.isEnabled())
                            addFigure(new Marker(c, crossAbove ? Enums.Position.TOP : Enums.Position.BOTTOM, marker,
                                    msg));
                        ctx.signal(index, Signals.SHORT, msg, priceSup);
                    }
                }
            }

            if (settings.getBoolean(RENKO_ENABLED) && settings.getBoolean(INFO_ENABLED)) {

                if (crossAbove || crossBelow) {

                    // popupMsgInfo += "\n hiLoColor1History: " + "\n hiLoColor1History[0] : "
                    // + history.hiLoColor1History[0] + "\n hiLoColor1History[1] : " +
                    // history.hiLoColor1History[1]
                    // + "\n hiLoColor1History[2] : " + history.hiLoColor1History[2] + "\n
                    // hiLoColor1History[3] : "
                    // + history.hiLoColor1History[3] + "\n hiLoColor1History[4] : " +
                    // history.hiLoColor1History[4]
                    // + "\n hiLoColor1History[5] : " + history.hiLoColor1History[5] + "\n
                    // hiLoColor1History[6] : "
                    // + history.hiLoColor1History[6] + "\n hiLoColor1History[7] : " +
                    // history.hiLoColor1History[7]
                    // + "\n hiLoColor2History : " + "\n hiLoColor2History[0] : " +
                    // history.hiLoColor2History[0]
                    // + "\n hiLoColor2History[1] : " + history.hiLoColor2History[1] + "\n
                    // hiLoColor2History[2] : "
                    // + history.hiLoColor2History[2] + "\n hiLoColor2History[3] : " +
                    // history.hiLoColor2History[3]
                    // + "\n hiLoColor2History[4] : " + history.hiLoColor2History[4] + "\n
                    // hiLoColor2History[5] : "
                    // + history.hiLoColor2History[5] + "\n hiLoColor2History[6] : " +
                    // history.hiLoColor2History[6]
                    // + "\n hiLoColor2History[7] : " + history.hiLoColor2History[7];

                    var c = new Coordinate(series.getStartTime(index), priceRes);
                    var marker = settings.getMarker(INFO_MARKER);
                    String msg = popupMsgInfo;
                    if (marker.isEnabled())
                        addFigure(new Marker(c, crossAbove ? Enums.Position.TOP : Enums.Position.BOTTOM, marker, msg));
                }
            }
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Classe non trouvée : " + className + " (skip)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        clearOrders();
    }

    @Override
    public void onActivate(OrderContext ctx) {
        debug("onActivate");
        if (getSettings().isEnterOnActivate()) {
            debug("isEnterOnActivate");
        }

        isActivated = true;

        Double pnlTotal = ctx.getTotalRealizedPnL();
        pnlSession = 0.00;
        int max = getSettings().getInteger(SESSION_MAX_WIN);
        int min = getSettings().getInteger(SESSION_MAX_LOSS) * -1;
        debug("Risk Management -> check PnL Total : " + pnlTotal + ", Min: " + min + ", Max: " + max);

    }

    @Override
    public void onSignal(OrderContext ctx, Object signal) {
        if (!isActivated)
            return;

        float position = ctx.getPositionAsFloat();

        Double pnl = ctx.getTotalRealizedPnL();
        Double lastPnl = startSession ? 0.00 : ctx.getRealizedPnL();
        int max = getSettings().getInteger(SESSION_MAX_WIN);
        int min = getSettings().getInteger(SESSION_MAX_LOSS) * -1;
        Boolean successiveLossEnabled = getSettings().getBoolean(MAX_SUCCESSIVE_LOSS_ENABLED);
        int maxSuccessiveLoss = getSettings().getInteger(MAX_SUCCESSIVE_LOSS);

        if (min < pnlSession && pnlSession < max) {
            pnlSession += lastPnl;
            if (min < pnlSession && pnlSession < max) {
                pnlMax = false;
            } else {
                pnlMax = true;
            }
        } else {
            pnlMax = true;
        }

        debug("Risk Management -> check PnL Total : " + pnl + ", PnL Session: " + pnlSession + ", Min: " + min
                + ", Max: " + max);

        if (successiveLossEnabled) {
            if (lastPnl > 0) {
                successiveLoss = 0;
            }
            if (lastPnl < 0) {
                successiveLoss += 1;
            }
            debug("Successive Loss: " + successiveLoss);
        } else {
            successiveLoss = 0;
        }

        if (successiveLoss < maxSuccessiveLoss && !pnlMax) {

            startSession = false;

            String strategyPatternGroupKey = getSettings().getString(STRATEGY_PATTERN_GROUP);
            PatternGroupKey patternGroup = PatternGroupKey.fromKeyString(strategyPatternGroupKey);

            takeProfitPattern = patternGroup.tp / ctx.getInstrument().getTickSize();
            stopLossPattern = patternGroup.sl / ctx.getInstrument().getTickSize();

            debug("takeProfitPattern : " + takeProfitPattern);
            debug("stopLossPattern : " + stopLossPattern);
            debug("tickSize : " + ctx.getInstrument().getTickSize());

            if (position == 0 && signal == Signals.LONG) {
                debug("isLong : " + isLong() + " / signal : " + signal);
                setEntryState(Enums.EntryState.NONE);
                setLong(true);
                onEnterNow(ctx);
            }
            if (position == 0 && signal == Signals.SHORT) {
                debug("isLong : " + isLong() + " / signal : " + signal);
                setEntryState(Enums.EntryState.NONE);
                setLong(false);
                onEnterNow(ctx);
            }

        } else {
            debug("Stop Pnl Session : " + pnlSession);
            if (successiveLossEnabled) {
                debug("Successive Loss: " + successiveLoss);
            }
        }
    }

    public void onSessionStarted(OrderContext ctx, TimeFrame session) {
        debug("onSessionStarted");
        startSession = true;
    }

    @Override
    public void onSessionEnded(OrderContext ctx, TimeFrame session) {
        super.onSessionEnded(ctx, session);
        debug("onSessionEnded");
        successiveLoss = 0;
        pnlMax = false;
        pnlSession = 0.00;
        startSession = false;
    }

    @Override
    public void onDeactivate(OrderContext ctx) {
        super.onDeactivate(ctx);
        createdExitOrders = false;
        clearOrders();
        isActivated = false;
    }

    @Override
    public void onReset(OrderContext ctx) {
        ctx.cancelOrders();
        super.onReset(ctx);
        createdExitOrders = false;
        successiveLoss = 0;
        pnlMax = false;
        pnlSession = 0.00;
        clearOrders();
    }

    @Override
    public void onBarUpdate(OrderContext ctx) {
        // System.out.println("onBarUpdate(): " +
        // com.motivewave.common.Util.formatDateMMMDDHHSS(ServiceInstance.getCurrentTime()));
        if (getEntryState() != Enums.EntryState.OPEN)
            return;
        // Check to see if we have hit the break even state, if so adjust the trail
        // orders.
        Instrument instr = ctx.getInstrument();
        float step = (float) (instr.getTickSize());

        float breakEven = getSettings().getInteger(BREAK_EVEN) * step;
        if (isLong()) {
            if (instr.round(instr.getSellPrice() - ctx.getAvgEntryPrice()) >= instr.round(breakEven)) {
                doBreakEven(ctx);
            }
        } else {
            if (instr.round(ctx.getAvgEntryPrice() - instr.getBuyPrice()) >= instr.round(breakEven)) {
                doBreakEven(ctx);
            }
        }
    }

    @Override
    public void onBarClose(OrderContext ctx) {
        var series = ctx.getDataContext().getDataSeries();
        switch (getEntryState()) {
        case NONE:
            break;
        case PRE_ENTRY:
            if ((isLong() && series.getClose() > series.getOpen())
                    || (isShort() && series.getClose() < series.getOpen())) {
                info("StrategyRenkoScalping::onBarClose() entering at market: " + ctx.getInstrument().getSymbol());
                onEnterNow(ctx);
            }
            break;
        case OPEN:
            moveATROrders(ctx);
            break;
        default:
            break;
        }
    }

    @Override
    public void onOrderFilled(OrderContext ctx, Order order) {

        if (order == entryOrder) {
            info("StrategyRenkoScalping::onOrderFilled() Entry Filled, creating exit orders "
                    + ctx.getInstrument().getSymbol());
            setEntryState(Enums.EntryState.OPEN);
            createExitOrders(ctx);
            clearOrder(order);
            return;
        } else if (order == entryOrder1 || order == entryOrder2) {
            // wait for the third order to be filled
            return;
        }

        if (Util.in(order, trailOrder, trailOrder1))
            trailFilled(ctx, order);
        else if (order == firstOrder)
            firstExitFilled(ctx);

        clearOrder(order);
    }

    @Override
    public void onOrderCancelled(OrderContext ctx, Order order) {
        if (isCancelInProgress())
            return;
        if (Util.in(order, entryOrder, entryOrder1, entryOrder2, entryOrder3)) {
            ctx.cancelOrders();
        }
        clearOrder(order);
    }

    @Override
    public void onEnterNow(OrderContext ctx) {
        if (getEntryState() != Enums.EntryState.NONE)
            return;

        setEntryState(Enums.EntryState.WAITING_ENTRY);

        // Special Case: If hedging is enabled we need to create separate position
        // orders for each exit target
        var instr = ctx.getInstrument();
        // if (ctx.supportsHedging() &&
        // getSettings().getExitPoint(EXIT).isEnabled()) {
        if (ctx.supportsHedging()) {
            var orders = new ArrayList<Order>();
            // var ep = getSettings().getExitPoint(EXIT);
            entryOrder1 = createMktEntry(ctx, isLong(),
                    getSettings().getInteger(TRADE_LOTS) * instr.getDefaultQuantityAsFloat());
            orders.add(entryOrder1);
            ctx.submitOrders(orders);
        } else {
            entryOrder = createMktEntry(ctx, isLong(),
                    getSettings().getInteger(TRADE_LOTS) * instr.getDefaultQuantityAsFloat());
            ctx.submitOrders(entryOrder);
        }
    }

    @Override
    public void onPositionClosed(OrderContext ctx) {
        ctx.cancelOrders();
        createdExitOrders = false;
        clearOrders();
        // setState(Enums.StrategyState.INACTIVE);
    }

    private Order createMktEntry(OrderContext ctx, boolean buy, float qty) {
        if (buy)
            return ctx.createMarketOrder(Enums.OrderAction.BUY, qty);
        return ctx.createMarketOrder(Enums.OrderAction.SELL, qty);
    }

    private void clearOrders() {
        firstOrder = secondOrder = thirdOrder = null;
        entryOrder = entryOrder1 = entryOrder2 = entryOrder3 = null;
        clearTrailOrders();
    }

    private void clearTrailOrders() {
        // trailOrder = trailOrder1 = trailOrder2 = trailOrder3 = null;
        trailOrder = trailOrder1 = trailOrder2 = null;
    }

    private void doBreakEven(OrderContext ctx) {
        if (getEntryState() != Enums.EntryState.OPEN)
            return;
        if (!getSettings().getBoolean(BREAK_EVEN_ENABLED))
            return;
        // if (!getSettings().getBoolean(ATR_ENABLED) )
        // return;

        atrActive = getSettings().getBoolean(ATR_ENABLED);
        breakEvenTrail(ctx, trailOrder);
        breakEvenTrail(ctx, trailOrder1);
        breakEvenTrail(ctx, trailOrder2);
        // breakEvenTrail(ctx, trailOrder3);
    }

    private void breakEvenTrail(OrderContext ctx, Order trail) {
        if (trail == null || trail.isFilled() || !trail.exists())
            return;
        var instr = ctx.getInstrument();
        float step = (float) (instr.getTickSize());

        // Double check the we are not already at break even
        float coverOffset = (float) (getSettings().getDouble(COVER) * step);
        float breakEvenPrice = ctx.getAvgEntryPrice();
        if (coverOffset > 0) {
            if (isLong())
                breakEvenPrice += coverOffset;
            else
                breakEvenPrice -= coverOffset;
        }

        boolean buy = trail.isBuy();
        if (buy) {
            float min = instr.round(instr.getAskPrice() + 2.0f * step);
            if (breakEvenPrice < min) {
                warning("StrategyRenkoScalping::breakEvenTrail() too close to ask price setting 2 pts away: "
                        + instr.getSymbol() + " breakEven: " + min);
                breakEvenPrice = min;
            }
        } else {
            float max = instr.round(instr.getBidPrice() - 2.0f * step);
            if (breakEvenPrice > max) {
                warning("StrategyRenkoScalping::breakEvenTrail() too close to bid price setting 2 pts away: "
                        + instr.getSymbol() + " breakEven: " + max);
                breakEvenPrice = max;
            }
        }

        if ((isLong() && trail.getStopPrice() >= breakEvenPrice)
                || (!isLong() && trail.getStopPrice() <= breakEvenPrice))
            return;

        trail.setAdjStopPrice(breakEvenPrice);
        ctx.submitOrders(trail);
    }

    private void createExitOrders(OrderContext ctx) {
        if (createdExitOrders) {
            warning("StrategyRenkoScalping::createExitOrders() orders already created!");
            return;
        }
        var instr = ctx.getInstrument();
        float step = (float) (instr.getTickSize());

        createdExitOrders = true;

        // Calculate the stop price
        // float offset = (float) (getSettings().getDouble(MAX_LOSS) * step);
        float offset = (float) (stopLossPattern * step);
        float basePrice = ctx.getAvgEntryPrice();
        if (isLong())
            basePrice -= offset;
        else
            basePrice += offset;

        // Make sure this is at least 1 pip away from the current bid/ask
        if (isLong()) {
            float maxPrice = instr.round(instr.getSellPrice() - step);
            if (basePrice > maxPrice)
                basePrice = maxPrice;
        } else {
            float minPrice = instr.round(instr.getBuyPrice() + step);
            if (basePrice < minPrice)
                basePrice = minPrice;
        }

        var action = isLong() ? Enums.OrderAction.SELL : Enums.OrderAction.BUY;

        var orders = new ArrayList<Order>();
        var tif = getSettings().getTIF(TIME_IN_FORCE);
        var entry = entryOrder;

        // Create Target Orders
        // var ep = getSettings().getExitPoint(EXIT);
        float qty = getSettings().getInteger(TRADE_LOTS) * instr.getDefaultQuantityAsFloat();
        float firstExit = 0;
        if (isLong())
            // firstExit = instr.round(ctx.getAvgEntryPrice() +
            // getSettings().getInteger(EXIT) * step);
            firstExit = instr.round(ctx.getAvgEntryPrice() + (float) takeProfitPattern * step);
        else
            // firstExit = instr.round(ctx.getAvgEntryPrice() -
            // getSettings().getInteger(EXIT) * step);
            firstExit = instr.round(ctx.getAvgEntryPrice() - (float) takeProfitPattern * step);

        Object ref = entryOrder1 != null ? entryOrder1.getReferenceID() : null;
        firstOrder = ctx.createLimitOrder(instr, ref, action, tif, qty, firstExit);

        orders.add(firstOrder);

        // Create the stop order(s)
        atrActive = false;
        if (entry != null) {
            // Non-hedged, just create one order
            qty = getSettings().getInteger(TRADE_LOTS) * instr.getDefaultQuantityAsFloat();
            trailOrder = ctx.createStopOrder(instr, action, tif, qty, basePrice);
            orders.add(trailOrder);
        } else {
            qty = getSettings().getInteger(TRADE_LOTS) * instr.getDefaultQuantityAsFloat();
            trailOrder1 = ctx.createStopOrder(instr, entryOrder1.getReferenceID(), action, tif, qty, basePrice);
            orders.add(trailOrder1);

        }

        // To make this faster, submit these orders together
        ctx.submitOrders(orders);
    }

    private void moveATROrders(OrderContext ctx) {
        if (!atrActive) {
            return;
        }

        if (!isValidOrder(trailOrder) && !isValidOrder(trailOrder1) && !isValidOrder(trailOrder2))
            return;

        // Calculate the ATR offset
        var dc = ctx.getDataContext();
        var series = dc.getDataSeries();
        var instr = dc.getInstrument();
        int latest = series.size() - 1;
        int atrPeriod = getSettings().getInteger(ATR_PERIOD);
        double mult = getSettings().getDouble(ATR_MULTIPLIER);
        Double atr = series.atr(latest - 1, atrPeriod);
        if (atr == null)
            return;
        float offset = (float) instr.round(atr * mult);
        float step = (float) (instr.getTickSize());

        boolean buy = true;
        if (isValidOrder(trailOrder))
            buy = trailOrder.isBuy();
        if (isValidOrder(trailOrder1))
            buy = trailOrder1.isBuy();

        float lastPrice = series.getClose(latest - 1);
        float stop = 0;

        if (buy) {
            stop = instr.round(lastPrice + offset);
            float min = instr.round(instr.getAskPrice() + 2.0f * step);
            if (stop < min)
                stop = min;
        } else {
            stop = instr.round(lastPrice - offset);
            float max = instr.round(instr.getBidPrice() - 2.0f * step);
            if (stop > max)
                stop = max;
        }

        double stopPrice = 0;
        if (isValidOrder(trailOrder))
            stopPrice = trailOrder.getStopPrice();
        else if (isValidOrder(trailOrder1))
            stopPrice = trailOrder1.getStopPrice();
        else if (isValidOrder(trailOrder2))
            stopPrice = trailOrder2.getStopPrice();

        if ((buy && stop >= stopPrice) || (!buy && stop <= stopPrice)) {
            // No change in price
            return;
        }

        if (isValidOrder(trailOrder)) {
            trailOrder.setAdjStopPrice(stop);
            ctx.submitOrders(trailOrder);
        } else {
            var orders = new ArrayList<Order>();
            if (isValidOrder(trailOrder1)) {
                trailOrder1.setAdjStopPrice(stop);
                orders.add(trailOrder1);
            }
            if (isValidOrder(trailOrder2)) {
                trailOrder2.setAdjStopPrice(stop);
                orders.add(trailOrder2);
            }
            ctx.submitOrders(orders);
        }
    }

    private void trailFilled(OrderContext ctx, Order order) {
        var cancelList = new ArrayList<Order>();
        if (isValidOrder(firstOrder) && Util.isEmpty(firstOrder.getReferenceID()))
            cancelList.add(firstOrder);
        if (isValidOrder(secondOrder) && Util.isEmpty(secondOrder.getReferenceID()))
            cancelList.add(secondOrder);
        if (isValidOrder(thirdOrder) && Util.isEmpty(thirdOrder.getReferenceID()))
            cancelList.add(thirdOrder);
        firstOrder = secondOrder = thirdOrder = null;
        ctx.cancelOrders(cancelList);
    }

    private void firstExitFilled(OrderContext ctx) {
        try {
            if (trailOrder != null) {
                float qty = firstOrder.getFilledAsFloat();
                if (qty == 0) {
                    // Grab the value from the config
                    warning("StrategyRenkoScalping::firstExitFilled() filled qty is 0!");
                    qty = getSettings().getInteger(TRADE_LOTS) * ctx.getInstrument().getDefaultQuantityAsFloat();
                }
                if (qty >= trailOrder.getQuantityAsFloat()) {
                    var cancelList = new ArrayList<Order>();
                    if (Util.isEmpty(trailOrder.getReferenceID()))
                        cancelList.add(trailOrder);
                    if (secondOrder != null)
                        cancelList.add(secondOrder);
                    if (thirdOrder != null)
                        cancelList.add(thirdOrder);
                    ctx.cancelOrders(cancelList);
                } else {
                    trailOrder.setAdjQuantity(trailOrder.getQuantityAsFloat() - qty);
                    ctx.submitOrders(trailOrder);
                }
            } else if (trailOrder1 != null) {
                if (Util.isEmpty(trailOrder1.getReferenceID()))
                    ctx.cancelOrders(trailOrder1);
            }
        } finally {
            firstOrder = null;
        }
    }

    private void clearOrder(Order order) {
        if (order == entryOrder)
            entryOrder = null;
        if (order == entryOrder1)
            entryOrder1 = null;
        if (order == entryOrder2)
            entryOrder2 = null;
        if (order == entryOrder3)
            entryOrder3 = null;
        if (order == firstOrder)
            firstOrder = null;
        if (order == secondOrder)
            secondOrder = null;
        if (order == thirdOrder)
            thirdOrder = null;
        if (order == trailOrder)
            trailOrder = null;
        if (order == trailOrder1)
            trailOrder1 = null;
        if (order == trailOrder2)
            trailOrder2 = null;
    }

    private boolean isValidOrder(Order order) {
        if (order == null)
            return false;
        if (!order.exists())
            return false;
        if (order.isCancelled() || order.isFilled())
            return false;
        return true;
    }

    private Order entryOrder, entryOrder1, entryOrder2, entryOrder3;

    private Order trailOrder, trailOrder1, trailOrder2;
    private boolean atrActive = false;

    private Order firstOrder, secondOrder, thirdOrder;
    private boolean createdExitOrders = false;

}
