package sbdev.joy_ai_project.strategy_v8o3;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Color;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Instrument;
import com.motivewave.platform.sdk.common.MarkerInfo;
import com.motivewave.platform.sdk.common.TimeFrame;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.TifDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.common.desc.StringDescriptor;
import com.motivewave.platform.sdk.order_mgmt.Order;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

import sbdev.joy_ai_project.strategy_v8o3.util.HistoryManager;
import sbdev.joy_ai_project.strategy_v8o3.core_pattern.CorePattern.PatternMatcher;
import sbdev.joy_ai_project.strategy_v8o3.database.PatternDbManager;
import sbdev.joy_ai_project.strategy_v8o3.log.StudyLogger;
import sbdev.joy_ai_project.strategy_v8o3.model.CurrentOrder;
import sbdev.joy_ai_project.strategy_v8o3.model.PatternData;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.HiLoResult;
import sbdev.joy_ai_project.strategy_v8o3.util.Helper;
import sbdev.joy_ai_project.strategy_v8o3.util.HiLoManagerLive;

@StudyHeader(namespace = "com.sbdev", id = "STRATEGY_BY_INTERVAL_V1", name = "Strategy By Interval V1", label = "Strategy By Interval V1", desc = "Strategy By Interval V1", menu = "SB Dev", overlay = true, signals = true, strategy = true, autoEntry = true, manualEntry = false, supportsStopPL = true, supportsUnrealizedPL = true, supportsRealizedPL = true, supportsTotalPL = true, supportsTargetPL = true, supportsRiskRatio = true, showTradeOptions = true, requiresBarUpdates = true)
public class StrategyByIntervalV1 extends Study {

    public static final String STUDY_ID = "STRATEGY_BY_INTERVAL_V1";
    public final String instanceId = String.valueOf(System.identityHashCode(this));
    private StudyLogger logger;

    private HiLoManagerLive hiloLive1 = new HiLoManagerLive();
    private HiLoManagerLive hiloLive2 = new HiLoManagerLive();
    protected HistoryManager history = new HistoryManager();

    protected Double brickSize = null; // Taille de la brique
    protected Double previousClose = null; // Renko open
    protected Double realRenko = null; // Renko close

    private static final String GREEN = "green";
    private static final String RED = "red";
    private static final String ABOVE = "above";
    private static final String BELOW = "below";
    private static final String LONG = "long";
    private static final String SHORT = "short";

    protected Double currentPriceHiLo1 = null;
    protected Double currentPriceHiLo2 = null;
    protected String currentColorHiLo1 = null;
    protected String currentColorHiLo2 = null;

    public boolean crossAbove = false;
    public boolean crossBelow = false;

    private static final String RENKO_ENABLED = "renkoEnabled";

    private static final String INPUT = "input";
    private static final String PERIOD = "period";
    private static final String RENKO_POINT = "renkoPoint";
    private static final String UP_MARKER = "upMarker";
    private static final String DOWN_MARKER = "downMarker";
    private static final String TOP_PATH = "topPath";
    private static final String BOTTOM_PATH = "bottomPath";
    private static final String TOP_IND = "topInd";
    private static final String BOTTOM_IND = "bottomInd";
    private static final String UP_COLOR = "upColor";
    private static final String DOWN_COLOR = "downColor";

    private static final String INFO_ENABLED = "infoEnabled";
    private static final String INFO_MARKER = "infoMarker";

    enum Values {
        HIGH, LOW, CROSS, MA_HILO_1, MA_HILO_2, ADX, DX, PDM, NDM, TR,
    }

    enum Signals {
        LONG_MATCH, SHORT_MATCH, CROSS_ABOVE, CROSS_BELOW
    }

    // Define custom input keys
    private static final String TF_ENABLED_1 = "tfEnabled1";
    private static final String TIMEFRAME_1 = "timeframe1";
    private static final String MA_METHOD_1 = "maMethod1";
    private static final String PATH_HILO_1 = "pathHilo1";
    private static final String PERIOD_1 = "period1";

    private static final String TF_ENABLED_2 = "tfEnabled2";
    private static final String TIMEFRAME_2 = "timeframe2";
    private static final String MA_METHOD_2 = "maMethod2";
    private static final String PATH_HILO_2 = "pathHilo2";
    private static final String PERIOD_2 = "period2";

    private static final String HIGH_COLOR_1 = "highColor1";
    private static final String LOW_COLOR_1 = "lowColor1";
    private static final String HIGH_COLOR_2 = "highColor2";
    private static final String LOW_COLOR_2 = "lowColor2";

    private static final String DOW_LINE_UP_1 = "dowLineUp1";
    private static final String DOW_LINE_DOWN_1 = "dowLineDown1";

    int highsOffset = 3;
    int lowsOffset = 3;

    // private VWAP vwap;

    // Entry inputs
    private static final String TRADE_LOTS = "tradeLots";
    private static final String TIME_IN_FORCE = "tif";
    private static final String BREAK_EVEN_ENABLED = "breakEvenEnabled";
    private static final String BREAK_EVEN = "breakEvenPips";
    private static final String COVER = "coverPips";
    private static final String ATR_PERIOD = "atrPeriod";
    private static final String ATR_MULTIPLIER = "atrMultiplier";
    private static final String ATR_ENABLED = "atrEnabled";
    // Risk Session
    private static final String SESSION_MAX_WIN = "sessionMaxWin";
    private static final String SESSION_MAX_LOSS = "sessionMaxLoss";
    private static final String MAX_SUCCESSIVE_LOSS_ENABLED = "maxSuccessiveLossEnabled";
    private static final String MAX_SUCCESSIVE_LOSS = "maxSuccessiveLoss";
    // private static final long ENTRY_DELAY = 2000;

    // A adapter : heure de clôture du marché (ex : 22h00 Paris)
    private static final String MARKET_CLOSE_HOUR = "marketCloseHour"; // Heure de clôture du marché (heure locale ou
                                                                       // UTC selon ta config)
    private static final String MARKET_CLOSE_MINUTE = "marketCloseMinute"; // Minute de clôture
    private static final String AUTO_CLOSE_BEFORE_MINUTES = "autoCloseBeforeMinute"; // Combien de minutes avant la
                                                                                     // clôture fermer la position

    protected double entryPricePattern = 0.00;
    protected double takeProfitPattern = 0.00;
    protected double stopLossPattern = 0.00;

    protected int successiveLoss = 0;
    protected Boolean pnlMax = false;
    protected Double pnlSession = 0.00;
    protected Boolean startSession = false;
    protected Boolean isStrategyActive = false;
    private Object pendingSignal = null;
    private int lastSignalBarIndex = -1;

    private static final String PATTERN_DATA_UPDATED = "patternDataUpdated";

    List<PatternData> patternDataList = new ArrayList<PatternData>();

    private Map<Long, HiLoResult> hiloCache1 = new HashMap<>();
    private Map<Long, HiLoResult> hiloCache2 = new HashMap<>();

    private boolean renkoEnabled;
    private boolean tfEnabled1;
    private boolean tfEnabled2;
    private boolean infoEnabled;

    @Override
    public void initialize(Defaults defaults) {
        var sd = createSD();

        sd.addTab(initializeStrategyTab(defaults));
        sd.addTab(getRiskTab());

        sd.addTab(initializeGeneralTab(defaults));

        // Quick Settings (Tool Bar and Popup Editor)
        sd.addQuickSettings(RENKO_POINT);
        sd.addQuickSettings(TOP_PATH, BOTTOM_PATH);

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

        sd.addDependency(
                new EnabledDependency(BREAK_EVEN_ENABLED, BREAK_EVEN, COVER, ATR_ENABLED, ATR_MULTIPLIER, ATR_PERIOD));
        sd.addDependency(new EnabledDependency(ATR_ENABLED, ATR_MULTIPLIER, ATR_PERIOD));
        sd.addDependency(new EnabledDependency(MAX_SUCCESSIVE_LOSS_ENABLED, MAX_SUCCESSIVE_LOSS));

        var desc = createRD();
        desc.setLabelSettings(INPUT, PERIOD, RENKO_POINT);

        desc.exportValue(new ValueDescriptor(Values.HIGH, "High", new String[] { INPUT, PERIOD }));
        desc.exportValue(new ValueDescriptor(Values.LOW, "Low", new String[] { INPUT, PERIOD }));
        desc.exportValue(new ValueDescriptor(Signals.LONG_MATCH, Enums.ValueType.BOOLEAN, LONG));
        desc.exportValue(new ValueDescriptor(Signals.SHORT_MATCH, Enums.ValueType.BOOLEAN, SHORT));

        desc.declarePath(Values.HIGH, TOP_PATH);
        desc.declarePath(Values.LOW, BOTTOM_PATH);

        desc.declareIndicator(Values.HIGH, TOP_IND);
        desc.declareIndicator(Values.LOW, BOTTOM_IND);

        desc.setRangeKeys(Values.HIGH, Values.LOW);

        // Declare paths for drawing
        desc.declarePath(Values.MA_HILO_1, PATH_HILO_1);
        desc.declarePath(Values.MA_HILO_2, PATH_HILO_2);

        // Signals
        desc.declareSignal(Signals.CROSS_ABOVE, "PRICE_CROSS_ABOVE");
        desc.declareSignal(Signals.CROSS_BELOW, "PRICE_CROSS_BELOW");
        desc.declareSignal(Signals.LONG_MATCH, LONG);
        desc.declareSignal(Signals.SHORT_MATCH, SHORT);

        // Initialisation du logger
        logger = StudyLogger.getInstance(STUDY_ID, instanceId);
    }

    protected SettingTab initializeStrategyTab(Defaults defaults) {
        var tab = new SettingTab("Strategy");

        // Section 1
        var grp = tab.addGroup("Entry");
        grp.addRow(new IntegerDescriptor(TRADE_LOTS, "Trade Lots", 1, 1, 9999, 1),
                new TifDescriptor(TIME_IN_FORCE, "Time in Force", Enums.TIF.GTC));

        grp = tab.addGroup("Pattern Data");
        grp.addRow(new BooleanDescriptor(PATTERN_DATA_UPDATED, " Pattern Data Updated", false));

        // Section 2
        var hours = tab.addGroup("Market Close");
        hours.addRow(new IntegerDescriptor(MARKET_CLOSE_HOUR, "Market Close Hour", 0, 23, 23, 1),
                new IntegerDescriptor(MARKET_CLOSE_MINUTE, " Minute", 0, 0, 59, 1));
        hours.addRow(new IntegerDescriptor(AUTO_CLOSE_BEFORE_MINUTES, "Auto Close Before Minute", 0, 5, 59, 1));

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
    public void onSettingsUpdated(DataContext ctx) {
        var settings = getSettings();
        if (settings == null)
            return;

        renkoEnabled = settings.getBoolean(RENKO_ENABLED);
        tfEnabled1 = settings.getBoolean(TF_ENABLED_1);
        tfEnabled2 = settings.getBoolean(TF_ENABLED_2);
        infoEnabled = settings.getBoolean(INFO_ENABLED);

        if (settings.getBoolean(PATTERN_DATA_UPDATED) || patternDataList.isEmpty()) {
            try {
                logger.log("loadPatternsFromDb");
                patternDataList = PatternDbManager.loadPatternsFromDb();
                logger.log("Pattern List : " + patternDataList);
                settings.setBoolean(PATTERN_DATA_UPDATED, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getMinBars() {
        int p1 = getSettings().getInteger(PERIOD_1);
        int p2 = getSettings().getInteger(PERIOD_2);
        return Math.max(p1, p2) * 2;
    }

    @Override
    public void onBarUpdate(DataContext ctx) {
        super.onBarUpdate(ctx);
        logger.setContext(ctx.getInstrument().getSymbol(), format(ctx.getDataSeries().getBarSize().getInterval()));
    }

    // @Override
    // protected void postcalculate(DataContext ctx) {
    // realRenko = null;
    // }

    @Override
    protected void calculate(int index, DataContext ctx) {
        var settings = getSettings();
        if (settings == null)
            return;

        if (!ctx.getDataSeries().getBarSize().isTick())
            return;

        calculateHiLo1(index, ctx);
        calculateHiLo2(index, ctx);
        initialiseADX(index, ctx);

        if (renkoEnabled) {
            calculateRenko(index, ctx);
        }

    }

    private void calculateHiLo1(int index, DataContext ctx) {
        var settings = getSettings();
        if (!tfEnabled1)
            return;

        int period1 = settings.getInteger(PERIOD_1);
        var tfSeries1 = ctx.getDataSeries(settings.getBarSize(TIMEFRAME_1));
        if (tfSeries1 == null || tfSeries1.size() <= period1)
            return;

        // Trouver le timestamp de la barre courante
        long time = ctx.getDataSeries().getEndTime(index);
        int tfIndex1 = tfSeries1.findIndex(time);
        if (tfIndex1 < 0 || tfIndex1 < period1)
            return;

        if (!tfSeries1.isBarComplete(tfIndex1))
            return;

        HiLoResult hilo = null;
        if (hiloCache1.containsKey(time)) {
            hilo = hiloCache1.get(time);
        } else {
            hilo = hiloLive1.computeHiLo(tfSeries1, tfIndex1, settings.getMAMethod(MA_METHOD_1), period1);
            hiloCache1.putIfAbsent(time, hilo);
        }

        if (hilo == null)
            return;

        var series = ctx.getDataSeries();
        if (Boolean.TRUE.equals(hilo.isLow)) {
            currentPriceHiLo1 = hilo.hiLo;
            series.setDouble(index, Values.MA_HILO_1, hilo.hiLo);
            currentColorHiLo1 = GREEN;
            series.setPathColor(index, Values.MA_HILO_1, settings.getColor(LOW_COLOR_1));
        } else if (Boolean.TRUE.equals(hilo.isHigh)) {
            currentPriceHiLo1 = hilo.hiLo;
            series.setDouble(index, Values.MA_HILO_1, hilo.hiLo);
            currentColorHiLo1 = RED;
            series.setPathColor(index, Values.MA_HILO_1, settings.getColor(HIGH_COLOR_1));
        }
        series.setComplete(index);
    }

    private void calculateHiLo2(int index, DataContext ctx) {
        var settings = getSettings();
        if (!tfEnabled2)
            return;

        int period2 = settings.getInteger(PERIOD_2);
        var tfSeries2 = ctx.getDataSeries(settings.getBarSize(TIMEFRAME_2));
        if (tfSeries2 == null || tfSeries2.size() <= period2)
            return;

        long time = ctx.getDataSeries().getEndTime(index);
        int tfIndex2 = tfSeries2.findIndex(time);
        if (tfIndex2 < 0 || tfIndex2 < period2)
            return;

        if (!tfSeries2.isBarComplete(tfIndex2))
            return;

        HiLoResult hilo = null;
        if (hiloCache2.containsKey(time)) {
            hilo = hiloCache2.get(time);
        } else {
            hilo = hiloLive2.computeHiLo(tfSeries2, tfIndex2, settings.getMAMethod(MA_METHOD_2), period2);
            hiloCache2.putIfAbsent(time, hilo);
        }

        if (hilo == null)
            return;

        var series = ctx.getDataSeries();
        if (Boolean.TRUE.equals(hilo.isLow)) {
            currentPriceHiLo2 = hilo.hiLo;
            series.setDouble(index, Values.MA_HILO_2, hilo.hiLo);
            currentColorHiLo2 = GREEN;
            series.setPathColor(index, Values.MA_HILO_2, settings.getColor(LOW_COLOR_2));
        } else if (Boolean.TRUE.equals(hilo.isHigh)) {
            currentPriceHiLo2 = hilo.hiLo;
            series.setDouble(index, Values.MA_HILO_2, hilo.hiLo);
            currentColorHiLo2 = RED;
            series.setPathColor(index, Values.MA_HILO_2, settings.getColor(HIGH_COLOR_2));
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

    private void calculateRenko(int index, DataContext ctx) {
        var settings = getSettings();
        var series = ctx.getDataSeries();

        Object barInputClose = Enums.BarInput.CLOSE;

        if (!series.isBarComplete(index)) {
            return; // On ignore si la barre n'est pas finie
        }

        // ticks to points
        double renkoPoints = settings.getInteger(RENKO_POINT) / 4;

        Double preClose = series.getDouble(index - 1, barInputClose);
        Double currentClose = series.getDouble(index, barInputClose);
        if (preClose == null || currentClose == null)
            return;

        crossAbove = false;
        crossBelow = false;

        Double cross = null;

        Double priceRes = null;
        Double priceSup = null;

        if (realRenko == null) {
            // Initialisation lors de la première exécution
            // Trouver le prix inférieur se terminant par une dizaine
            // Trouver la dizaine inférieure
            double lowerBound = Math.floor(currentClose / 10) * 10;
            // La dizaine suivante se terminant par 5
            double upperBound = lowerBound + renkoPoints;

            // Si le prix est plus proche de la dizaine suivante (10, 20, etc.)
            if (currentClose > lowerBound + renkoPoints) {
                // Ajuster pour commencer à 5
                lowerBound = lowerBound + renkoPoints;
                // Ajuster pour aller jusqu'à la dizaine
                upperBound = lowerBound + renkoPoints;
            }

            realRenko = upperBound;
            previousClose = lowerBound;
            brickSize = Math.abs(realRenko - previousClose);
            if (realRenko > previousClose) {
                updateAfterCross(GREEN, realRenko, previousClose);
            } else {
                updateAfterCross(RED, realRenko, previousClose);
            }
        } else {
            if (history.renkoColorHistory[0].equals(GREEN)) {

                if (currentClose - realRenko >= brickSize) {
                    realRenko = realRenko + Math.floor((currentClose - realRenko) / brickSize) * brickSize;
                    previousClose = realRenko - brickSize;
                    crossAbove = true;
                    cross = 1.0;
                    updateAfterCross(GREEN, realRenko, previousClose);
                } else if (realRenko - currentClose >= brickSize * 2) {
                    realRenko = realRenko - Math.floor((realRenko - currentClose) / brickSize) * brickSize;
                    previousClose = realRenko + brickSize;
                    crossBelow = true;
                    cross = -1.0;
                    updateAfterCross(RED, realRenko, previousClose);
                }

            } else if (history.renkoColorHistory[0].equals(RED)) {

                if (currentClose - realRenko >= brickSize * 2) {
                    realRenko = realRenko + Math.floor((currentClose - realRenko) / brickSize) * brickSize;
                    previousClose = realRenko - brickSize;
                    crossAbove = true;
                    cross = 1.0;
                    updateAfterCross(GREEN, realRenko, previousClose);
                } else if (realRenko - currentClose >= brickSize) {
                    realRenko = realRenko - Math.floor((realRenko - currentClose) / brickSize) * brickSize;
                    previousClose = realRenko + brickSize;
                    crossBelow = true;
                    cross = -1.0;
                    updateAfterCross(RED, realRenko, previousClose);
                }
            }
        }

        boolean complete = series.isBarComplete(index);
        if (priceRes == null) {
            priceRes = realRenko;
            complete = false;
        }
        if (priceSup == null) {
            priceSup = previousClose;
            complete = false;
        }

        // Set colors for the Renko bricks
        Color brickColor = history.renkoColorHistory[0] == GREEN ? settings.getColor(UP_COLOR)
                : settings.getColor(DOWN_COLOR);

        // Plot the Renko lines
        series.setPathColor(index - 1, Values.LOW, brickColor);
        series.setPathColor(index - 1, Values.HIGH, brickColor);

        series.setDouble(index - 1, Values.LOW, priceSup);
        series.setDouble(index, Values.HIGH, priceRes);

        series.setDouble(index, Values.CROSS, cross);

        // if (series.isBarComplete(index))
        // series.setComplete(index, complete);

        if (cross != null && index != lastSignalBarIndex) {
            lastSignalBarIndex = index;

            Coordinate coord = new Coordinate(series.getEndTime(index), priceRes);
            String instrument = ctx.getInstrument().getSymbol();
            int interval = ctx.getDataSeries().getBarSize().getInterval();
            Double tickSize = ctx.getInstrument().getTickSize();
            MarkerInfo upMarker = settings.getMarker(UP_MARKER);
            MarkerInfo downMarker = settings.getMarker(DOWN_MARKER);
            MarkerInfo infoMarker = settings.getMarker(INFO_MARKER);

            if (renkoEnabled && infoEnabled) {
                displayInfo(coord, infoMarker);
            }

            if (history.isArrayAsNull(history.renkoColorHistory) && history.isArrayAsNull(history.hiLoColor1History)
                    && history.isArrayAsNull(history.hiLoColor2History)
                    && history.isArrayAsNull(history.hiLoPrice1History)
                    && history.isArrayAsNull(history.hiLoPrice2History)
                    && history.isArrayAsNull(history.renkoPriceResHistory)
                    && history.isArrayAsNull(history.renkoPriceSupHistory)) {
                return;
            }

            PatternData match = strategySignal(priceRes, priceSup, coord, instrument, interval);
            if (match != null) {

                int lastIndex = series.size() - 1;
                long tsLast = series.getEndTime(lastIndex);

                // Limite à 10 secondes avant la dernière bougie
                long limitTs = tsLast - 10000;

                // Pattern trouvé !
                entryPricePattern = priceRes;
                takeProfitPattern = match.bracket.tp / tickSize;
                stopLossPattern = match.bracket.sl / tickSize;

                String msg = match.toString();

                if (crossAbove) {
                    series.setBoolean(index, Signals.CROSS_ABOVE, true);
                    if (upMarker.isEnabled())
                        addFigure(new Marker(coord, Enums.Position.BOTTOM, upMarker, msg));
                    // System.out.println("Ajout marker UP sur bar " + index);
                    if (series.getEndTime(index) >= limitTs) {
                        if (getEntryState() == Enums.EntryState.NONE) {
                            logger.log(
                                    "Signal envoyé: CROSS_ABOVE à l'index: " + index + " match : " + match.toString());
                            pendingSignal = Signals.CROSS_ABOVE;
                        }
                        crossAbove = false;
                        logger.log("Signal accepté pour l'index " + index);
                    } else {
                        pendingSignal = null;
                        // Ici, on ignore les signaux trop anciens
                        // System.out.println("Signal ignoré (trop vieux) pour l'index " + index);
                    }

                } else if (crossBelow) {
                    series.setBoolean(index, Signals.CROSS_BELOW, true);
                    if (downMarker.isEnabled())
                        addFigure(new Marker(coord, Enums.Position.TOP, downMarker, msg));
                    // System.out.println("Ajout marker DOWN sur bar " + index);
                    if (series.getEndTime(index) >= limitTs) {
                        if (getEntryState() == Enums.EntryState.NONE) {
                            logger.log(
                                    "Signal envoyé: CROSS_BELOW à l'index: " + index + " match : " + match.toString());
                            pendingSignal = Signals.CROSS_BELOW;
                        }
                        crossBelow = false;
                        logger.log("Signal accepté pour l'index " + index);
                    } else {
                        pendingSignal = null;
                        // Ici, on ignore les signaux trop anciens
                        // System.out.println("Signal ignoré (trop vieux) pour l'index " + index);
                    }

                } else {
                    pendingSignal = null;
                    match = null;
                    cross = null;
                }
            }
        }

        if (series.isBarComplete(index))
            series.setComplete(index, complete);

    }

    private PatternData strategySignal(Double priceRes, Double priceSup, Coordinate coord, String instrument,
            int interval) {

        // debug("strategySignal");
        // List<PatternData> patterns = PatternDbManager.loadPatternsFromDb();
        // for (PatternData data : patternDataList) {
        // System.out.println("pattern : " + data);
        // }
        PatternMatcher matcher = new PatternMatcher(patternDataList);

        String instrumentKey = Helper.extractInstrumentKeyGroup(instrument);
        PatternData pattern = history.toPatternData(instrumentKey, interval, null, null);

        return matcher.matchFlexible(pattern.instrumentKey, pattern.intervalValue, null, null, pattern.patternRenko,
                pattern.patternHilo1, pattern.patternHilo2);

    }

    // Factorisation de la MAJ historique après chaque croisement
    private void updateAfterCross(String color, double realRenko, double previousClose) {
        history.updateCrossHistory(color.equals(GREEN) ? ABOVE : BELOW);
        history.updateRenkoColorHistory(color);
        history.autoUpdateRenkoPriceHistory(realRenko, previousClose);
        history.autoUpdateHiLoPriceHistory(currentPriceHiLo1, currentPriceHiLo2);
        history.autoUpdateHiLoColorHistory(currentColorHiLo1, currentColorHiLo2);
    }

    public void displayInfo(Coordinate coord, MarkerInfo infoMarker) {

        String infoMsg = "\n renkoColorHistory: " + "\n renkoColorHistory[0] : " + history.renkoColorHistory[0]
                + "\n renkoColorHistory[1] : " + history.renkoColorHistory[1] + "\n renkoColorHistory[2] : "
                + history.renkoColorHistory[2] + "\n renkoColorHistory[3] : " + history.renkoColorHistory[3]
                + "\n renkoColorHistory[4] : " + history.renkoColorHistory[4] + "\n renkoColorHistory[5] : "
                + history.renkoColorHistory[5] + "\n renkoColorHistory[6] : " + history.renkoColorHistory[6]
                + "\n renkoColorHistory[7] : " + history.renkoColorHistory[7] + "\n hiLoColor1History: "
                + "\n hiLoColor1History[0] : " + history.hiLoColor1History[0] + "\n hiLoColor1History[1] : "
                + history.hiLoColor1History[1] + "\n hiLoColor1History[2] : " + history.hiLoColor1History[2]
                + "\n hiLoColor1History[3] : " + history.hiLoColor1History[3] + "\n hiLoColor1History[4] : "
                + history.hiLoColor1History[4] + "\n hiLoColor1History[5] : " + history.hiLoColor1History[5]
                + "\n hiLoColor1History[6] : " + history.hiLoColor1History[6] + "\n hiLoColor1History[7] : "
                + history.hiLoColor1History[7] + "\n hiLoColor2History : " + "\n hiLoColor2History[0] : "
                + history.hiLoColor2History[0] + "\n hiLoColor2History[1] : " + history.hiLoColor2History[1]
                + "\n hiLoColor2History[2] : " + history.hiLoColor2History[2] + "\n hiLoColor2History[3] : "
                + history.hiLoColor2History[3] + "\n hiLoColor2History[4] : " + history.hiLoColor2History[4]
                + "\n hiLoColor2History[5] : " + history.hiLoColor2History[5] + "\n hiLoColor2History[6] : "
                + history.hiLoColor2History[6] + "\n hiLoColor2History[7] : " + history.hiLoColor2History[7];

        addFigure(new Marker(coord, history.renkoColorHistory[0] == GREEN ? Enums.Position.TOP : Enums.Position.BOTTOM,
                infoMarker, infoMsg));

    }

    @Override
    public void destroy() {
        super.destroy();
        clearAllOrders();
        if (logger != null)
            logger.close();
    }

    @Override
    public void onActivate(OrderContext ctx) {
        logger.log("onActivate");
        if (getSettings().isEnterOnActivate()) {
            logger.log("isEnterOnActivate");
        }

        isStrategyActive = true;

        Double pnlTotal = ctx.getTotalRealizedPnL();
        pnlSession = 0.00;
        int max = getSettings().getInteger(SESSION_MAX_WIN);
        int min = getSettings().getInteger(SESSION_MAX_LOSS) * -1;
        logger.log("Risk Management -> check PnL Total : " + pnlTotal + ", Min: " + min + ", Max: " + max);

    }

    @Override
    public void onDeactivate(OrderContext ctx) {
        super.onDeactivate(ctx);
        clearAllOrders();
        isStrategyActive = false;
    }

    // ---- Signaux d'entrée ----
    public void onSessionStarted(OrderContext ctx, TimeFrame session) {
        logger.log("onSessionStarted");
        startSession = true;
    }

    @Override
    public void onSessionEnded(OrderContext ctx, TimeFrame session) {
        super.onSessionEnded(ctx, session);
        logger.log("onSessionEnded");
        clearAllOrders();
        setEntryState(Enums.EntryState.NONE);
        successiveLoss = 0;
        pnlMax = false;
        pnlSession = 0.00;
        startSession = false;
    }

    @Override
    public void onReset(OrderContext ctx) {
        ctx.cancelOrders();
        clearAllOrders();
        setEntryState(Enums.EntryState.NONE);
        super.onReset(ctx);
        successiveLoss = 0;
        pnlMax = false;
        pnlSession = 0.00;
    }

    // ---- Cycle de trading ----
    @Override
    public void onBarClose(OrderContext ctx) {
        // Gestion de signal différé : une seule entrée possible à la fois
        if (pendingSignal != null && getEntryState() == Enums.EntryState.NONE && entryOrder == null) {
            tryEnterPosition(ctx, pendingSignal);
            pendingSignal = null;
        }

        // Filet de sécurité :
        securityStopIfNeeded(ctx);

        Date now = new Date(ctx.getCurrentTime());
        if (ctx.getPositionAsFloat() != 0 && isCloseToMarketClose(now)) {
            logger.log("Clôture automatique : position fermée " + getSettings().getInteger(AUTO_CLOSE_BEFORE_MINUTES)
                    + "min avant la clôture du marché.");
            ctx.closeAtMarket();
            clearAllOrders(); // sécurité pour réinitialiser tes variables d'état
        }
    }

    public void tryEnterPosition(OrderContext ctx, Object signal) {
        logger.log("tryEnterTrade: received signal = " + signal + " (" + (signal != null ? signal.getClass() : "null")
                + ")");
        if (!isStrategyActive)
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

        logger.log("Risk Management -> check PnL Total : " + pnl + ", PnL Session: " + pnlSession + ", Min: " + min
                + ", Max: " + max);

        if (successiveLossEnabled) {
            if (lastPnl > 0) {
                successiveLoss = 0;
            }
            if (lastPnl < 0) {
                successiveLoss += 1;
            }
            logger.log("Successive Loss: " + successiveLoss);
        } else {
            successiveLoss = 0;
        }

        if (successiveLoss < maxSuccessiveLoss && !pnlMax) {
            startSession = false;
            logger.log("takeProfitPattern : " + takeProfitPattern);
            logger.log("stopLossPattern : " + stopLossPattern);

            if (position == 0) {

                if (signal == Signals.LONG_MATCH || signal == Signals.CROSS_ABOVE) {
                    logger.log("isLong : " + isLong() + " / signal : " + signal);
                    setEntryState(Enums.EntryState.NONE);
                    setLong(true);
                    submitEntryOrder(ctx);
                }
                if (signal == Signals.SHORT_MATCH || signal == Signals.CROSS_BELOW) {
                    logger.log("isLong : " + isLong() + " / signal : " + signal);
                    setEntryState(Enums.EntryState.NONE);
                    setLong(false);
                    submitEntryOrder(ctx);
                }
            }
        } else {
            logger.log("Stop Pnl Session : " + pnlSession);
            if (successiveLossEnabled) {
                logger.log("Successive Loss: " + successiveLoss);
            }
        }
    }

    private void submitEntryOrder(OrderContext ctx) {
        if (entryOrder != null || getEntryState() != Enums.EntryState.NONE)
            return;
        setEntryState(Enums.EntryState.WAITING_ENTRY);

        Instrument instr = ctx.getInstrument();
        float qty = getSettings().getInteger(TRADE_LOTS) * instr.getDefaultQuantityAsFloat();
        boolean buy = isLong();

        // Calcule les niveaux pour le nouvel ordre
        double entryPrice = entryPricePattern;
        double tickSize = instr.getTickSize();
        double takeProfit = buy ? entryPrice + takeProfitPattern * tickSize : entryPrice - takeProfitPattern * tickSize;
        double stopLoss = buy ? entryPrice - stopLossPattern * tickSize : entryPrice + stopLossPattern * tickSize;

        currentOrder = new CurrentOrder(entryPrice, takeProfit, stopLoss);

        entryOrder = buy ? ctx.createMarketOrder(Enums.OrderAction.BUY, qty)
                : ctx.createMarketOrder(Enums.OrderAction.SELL, qty);

        logger.log("Entry order submitted: " + entryOrder + " | Entry=" + entryPrice + " TP=" + takeProfit + " SL="
                + stopLoss);
        ctx.submitOrders(entryOrder);
    }

    @Override
    public void onOrderFilled(OrderContext ctx, Order order) {
        logger.log("onOrderFilled: " + order);

        // --- Entrée remplie : création des TP/SL ---
        if (entryOrder != null && order.equals(entryOrder) && currentOrder != null) {
            float qty = entryOrder.getQuantityAsFloat();
            Instrument instr = ctx.getInstrument();
            Enums.OrderAction exitAction = isLong() ? Enums.OrderAction.SELL : Enums.OrderAction.BUY;
            var tif = getSettings().getTIF(TIME_IN_FORCE);

            float tpPrice = (float) currentOrder.takeProfitPrice;
            float slPrice = (float) currentOrder.stopLossPrice;

            takeProfit = ctx.createLimitOrder(instr, order, exitAction, tif, qty, tpPrice);
            stopLoss = ctx.createStopOrder(instr, order, exitAction, tif, qty, slPrice);

            ArrayList<Order> exits = new ArrayList<>();
            exits.add(takeProfit);
            exits.add(stopLoss);
            ctx.submitOrders(exits);

            logger.log("Submitted takeProfit: " + takeProfit + " (price: " + tpPrice + ")");
            logger.log("Submitted stopLoss: " + stopLoss + " (price: " + slPrice + ")");
        }

        // --- TP touché : on ferme le SL et on reset la stratégie
        if (takeProfit != null && order.equals(takeProfit)) {
            logger.log("Take Profit hit! Closing stopLoss order.");
            if (stopLoss != null)
                ctx.cancelOrders(stopLoss);
            clearAllOrders();
            setEntryState(Enums.EntryState.NONE);
            return;
        }

        // --- SL touché : on ferme le TP et on reset la stratégie
        if (stopLoss != null && order.equals(stopLoss)) {
            logger.log("Stop Loss hit! Closing takeProfit order.");
            if (takeProfit != null)
                ctx.cancelOrders(takeProfit);
            clearAllOrders();
            setEntryState(Enums.EntryState.NONE);
            return;
        }

        // Sécurité : nettoyage éventuel
        clearOrder(order);
    }

    private void securityStopIfNeeded(OrderContext ctx) {
        float position = ctx.getPositionAsFloat();
        if (position == 0 || currentOrder == null) {
            securityTpStop = null; // reset si hors position
            return;
        }

        double tpPrice = currentOrder.takeProfitPrice;
        double slPrice = currentOrder.stopLossPrice;
        double lastPrice = ctx.getInstrument().getLastPrice();
        double tickSize = ctx.getInstrument().getTickSize();

        double tpStopPrice = isLong() ? tpPrice - tickSize * 4 : tpPrice + tickSize * 4;

        // TP : si atteint ou dépassé, place stop puis force close si pas exécuté
        if (isLong() && lastPrice >= tpPrice) {
            if (securityTpStop == null || !securityTpStop.exists()) {
                logger.log("Sécurité : TP dépassé (long), placement d'un stop à " + tpStopPrice);
                Instrument instr = ctx.getInstrument();
                float qty = Math.abs(position);
                Enums.OrderAction stopAction = Enums.OrderAction.SELL;
                var tif = getSettings().getTIF(TIME_IN_FORCE);

                if (securityTpStop != null)
                    ctx.cancelOrders(securityTpStop);

                securityTpStop = ctx.createStopOrder(instr, stopAction, tif, qty, (float) tpStopPrice);
                ctx.submitOrders(securityTpStop);
            } else if (!securityTpStop.isFilled() && lastPrice < tpStopPrice) {
                logger.log("Sécurité : TP stop à " + tpStopPrice + " ignoré (long), clôture au marché.");
                ctx.closeAtMarket();
                ctx.cancelOrders(securityTpStop);
                securityTpStop = null;
            }
            return;
        }
        if (!isLong() && lastPrice <= tpPrice) {
            if (securityTpStop == null || !securityTpStop.exists()) {
                logger.log("Sécurité : TP dépassé (short), placement d'un stop à " + tpStopPrice);
                Instrument instr = ctx.getInstrument();
                float qty = Math.abs(position);
                Enums.OrderAction stopAction = Enums.OrderAction.BUY;
                var tif = getSettings().getTIF(TIME_IN_FORCE);

                if (securityTpStop != null)
                    ctx.cancelOrders(securityTpStop);

                securityTpStop = ctx.createStopOrder(instr, stopAction, tif, qty, (float) tpStopPrice);
                ctx.submitOrders(securityTpStop);
            } else if (!securityTpStop.isFilled() && lastPrice > tpStopPrice) {
                logger.log("Sécurité : TP stop à " + tpStopPrice + " ignoré (short), clôture au marché.");
                ctx.closeAtMarket();
                ctx.cancelOrders(securityTpStop);
                securityTpStop = null;
            }
            return;
        }

        // SL : stricte
        if (isLong() && lastPrice < slPrice) {
            logger.log("Sécurité : SL dépassé (long), clôture immédiate au marché.");
            ctx.closeAtMarket();
            if (securityTpStop != null)
                ctx.cancelOrders(securityTpStop);
            securityTpStop = null;
            return;
        }
        if (!isLong() && lastPrice > slPrice) {
            logger.log("Sécurité : SL dépassé (short), clôture immédiate au marché.");
            ctx.closeAtMarket();
            if (securityTpStop != null)
                ctx.cancelOrders(securityTpStop);
            securityTpStop = null;
            return;
        }
    }

    @Override
    public void onOrderCancelled(OrderContext ctx, Order order) {
        logger.log("Order cancelled: " + order);
        clearOrder(order);
    }

    @Override
    public void onPositionClosed(OrderContext ctx) {
        ctx.cancelOrders();
        clearAllOrders();
        setEntryState(Enums.EntryState.NONE);
    }

    // ---- Utilitaires ----
    private void clearAllOrders() {
        entryOrder = null;
        takeProfit = null;
        stopLoss = null;
        securityTpStop = null;
        currentOrder = null;
    }

    private void clearOrder(Order order) {
        if (entryOrder != null && order.equals(entryOrder))
            entryOrder = null;
        if (takeProfit != null && order.equals(takeProfit))
            takeProfit = null;
        if (stopLoss != null && order.equals(stopLoss))
            stopLoss = null;
    }

    private boolean isCloseToMarketClose(Date currentDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        int targetHour = getSettings().getInteger(MARKET_CLOSE_HOUR);
        int targetMinute = getSettings().getInteger(MARKET_CLOSE_MINUTE)
                - getSettings().getInteger(AUTO_CLOSE_BEFORE_MINUTES);

        if (targetMinute < 0) {
            targetHour -= 1;
            targetMinute += 60;
        }

        // Si l'heure est après le seuil, on ferme
        if (hour > targetHour || (hour == targetHour && minute >= targetMinute)) {
            return true;
        }
        return false;
    }

    // Variables d'instance :
    private Order entryOrder;
    private Order takeProfit;
    private Order stopLoss;
    private CurrentOrder currentOrder = null;
    private Order securityTpStop = null; // pour l'ordre stop TP

}
