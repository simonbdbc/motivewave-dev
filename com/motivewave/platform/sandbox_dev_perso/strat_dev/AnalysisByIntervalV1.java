package sandbox.strat_dev;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.LabelDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.SettingsDescriptor;
import com.motivewave.platform.sdk.common.desc.StringDescriptor;
import com.motivewave.platform.sdk.common.desc.TifDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.common.desc.DateTimeDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.order_mgmt.Order;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

import sbdev.joy_ai_project.strategy_v8o3.core_pattern.CorePattern;
import sbdev.joy_ai_project.strategy_v8o3.database.DatabaseInitializer;
import sbdev.joy_ai_project.strategy_v8o3.strategy.CoreAnalysis;
import sbdev.joy_ai_project.strategy_v8o3.util.HistoryManager;
import sbdev.joy_ai_project.strategy_v8o3.util.InterfaceCorePattern;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.HiLoResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternCheckResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternGroupKey;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.RenkoResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.SimulatedBar;
import sbdev.joy_ai_project.strategy_v8o3.util.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@StudyHeader(id = "ANALYSIS_BY_INTERVAL_V1", namespace = "com.sbdev", name = "Analysis By Interval V1", desc = "Analysis By Interval V1", menu = "Sandbox", overlay = true, signals = true, strategy = true, autoEntry = true, manualEntry = false, supportsStopPL = true, supportsUnrealizedPL = true, supportsRealizedPL = true, supportsTotalPL = true, supportsTargetPL = true, supportsRiskRatio = true, showTradeOptions = true, requiresBarUpdates = true)
public class AnalysisByIntervalV1 extends Study {

    protected HistoryManager history;
    protected sbdev.joy_ai_project.strategy_v8o3.strategy.CoreAnalysis analysis;

    protected Double brickSize = null; // Taille de la brique
    protected Double previousClose = null; // Renko open
    protected Double realRenko; // Renko close

    final static String GREEN = "green";
    final static String RED = "red";
    final static String ABOVE = "above";
    final static String BELOW = "below";

    protected Double hilo1 = null;
    protected Double hilo2 = null;

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

    int highsOffset = 3;
    int lowsOffset = 3;

    final static String START_DATETIME = "startDateTime";
    final static String END_DATETIME = "endDateTime";

    public static final String ANALYSIS_ENABLED = "analysisEnabled";

    // public static final String TICK_INTERVAL_1 = "tickInteval1";
    // public static final String TICK_INTERVAL_ENABLED_1 = "tickIntevalEnabled1";
    // public static final String TICK_INTERVAL_2 = "tickInteval2";
    // public static final String TICK_INTERVAL_ENABLED_2 = "tickIntevalEnabled2";
    // public static final String TICK_INTERVAL_3 = "tickInteval3";
    // public static final String TICK_INTERVAL_ENABLED_3 = "tickIntevalEnabled3";
    // public static final String TICK_INTERVAL_4 = "tickInteval4";
    // public static final String TICK_INTERVAL_ENABLED_4 = "tickIntevalEnabled4";
    // public static final String TICK_INTERVAL_5 = "tickInteval5";
    // public static final String TICK_INTERVAL_ENABLED_5 = "tickIntevalEnabled5";
    // public static final String TICK_INTERVAL_6 = "tickInteval6";
    // public static final String TICK_INTERVAL_ENABLED_6 = "tickIntevalEnabled6";

    private List<Integer> listActiveTickIntervals = new ArrayList<>();
    private Integer activeTickInterval = null;

    private List<PatternGroupKey> patternGroups = Helper.loadPatternGroupsFromSingleton();
    public static final String STRATEGY_PATTERN_GROUP = "strategyPatternGroup";

    public final static String STEP = "step";
    // Entry inputs
    public final static String TRADE_LOTS = "tradeLots", TIME_IN_FORCE = "tif";
    // Exit inputs
    public final static String EXIT = "exit";
    // Stop Loss
    public final static String BREAK_EVEN_ENABLED = "breakEvenEnabled", BREAK_EVEN = "breakEvenPips",
            COVER = "coverPips", MAX_LOSS = "maxLoss", ATR_PERIOD = "atrPeriod", ATR_MULTIPLIER = "atrMultiplier",
            ATR_ENABLED = "atrEnabled";
    // Risk Session
    public final static String SESSION_MAX_WIN = "sessionMaxWin", SESSION_MAX_LOSS = "sessionMaxLoss",
            MAX_SUCCESSIVE_LOSS_ENABLED = "maxSuccessiveLossEnabled", MAX_SUCCESSIVE_LOSS = "maxSuccessiveLoss";

    public final static long ENTRY_DELAY = 2000;

    protected int successiveLoss = 0;
    protected Boolean pnlMax = false;
    protected Double pnlSession = 0.00;
    protected Boolean startSession = false;

    @Override
    public void initialize(Defaults defaults) {

        var sd = createSD();

        sd.addTab(initializeAnalyseTab(sd, defaults));
        sd.addTab(initializeGeneralTab(defaults));

        // Quick Settings (Tool Bar and Popup Editor)
        sd.addQuickSettings(RENKO_POINT);
        sd.addQuickSettings(TOP_PATH, BOTTOM_PATH);

        // sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_1,
        // TICK_INTERVAL_1));
        // sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_2,
        // TICK_INTERVAL_2));
        // sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_3,
        // TICK_INTERVAL_3));
        // sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_4,
        // TICK_INTERVAL_4));
        // sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_5,
        // TICK_INTERVAL_5));
        // sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_6,
        // TICK_INTERVAL_6));

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

        // Study Settings

        sd.addTab(getExitStrategyTab());
        sd.addTab(getRiskTab());

        sd.addDependency(
                new EnabledDependency(BREAK_EVEN_ENABLED, BREAK_EVEN, COVER, ATR_ENABLED, ATR_MULTIPLIER, ATR_PERIOD));
        sd.addDependency(new EnabledDependency(ATR_ENABLED, ATR_MULTIPLIER, ATR_PERIOD));
        sd.addDependency(new EnabledDependency(MAX_SUCCESSIVE_LOSS_ENABLED, MAX_SUCCESSIVE_LOSS));
        setSettingsDescriptor(sd);

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

    protected SettingTab initializeAnalyseTab(SettingsDescriptor sd, Defaults defaults) {
        var tab = new SettingTab("Analyse");

        var analyse = tab.addGroup("Activation");
        analyse.addRow(new BooleanDescriptor(ANALYSIS_ENABLED, "Activate Analysis", false));

        // Section 1
        var section1 = tab.addGroup("Interval");

        List<NVP> types = new ArrayList<>();

        for (int i = 0; i < patternGroups.size(); i++) {
            PatternGroupKey group = patternGroups.get(i);
            types.add(new NVP("Tick Interval : " + group.intervalValue + " --- TP/SL : " + group.tp + "/" + group.sl,
                    group.toKeyString()));
        }

        section1.addRow(new DiscreteDescriptor(STRATEGY_PATTERN_GROUP, "Startegy Patten Group",
                patternGroups.get(0).toKeyString(), types));

        // Section 2
        var date = tab.addGroup("Date");
        date.addRow(new IntegerDescriptor(START_DATETIME, "Analysis History (Days ago)", 30, 1, 365, 1));
        date.addRow(new DateTimeDescriptor(END_DATETIME, "Analysis End Date", Instant.now().toEpochMilli(), true));

        return tab;
    }

    protected SettingTab getExitStrategyTab() {

        var tab = new SettingTab("Exit Strategy");

        var grp = tab.addGroup("Entry");
        grp.addRow(new IntegerDescriptor(TRADE_LOTS, "Trade Lots", 1, 1, 9999, 1),
                new TifDescriptor(TIME_IN_FORCE, "Time in Force", Enums.TIF.GTC));

        grp = tab.addGroup("Bracket");
        grp.addRow(new IntegerDescriptor(EXIT, "Take Profit", 60, 1, 9999, 1));
        grp.addRow(new IntegerDescriptor(MAX_LOSS, "Stop Loss", 60, 0, 999, 1));

        grp = tab.addGroup("Break Even");
        grp.addRow(new IntegerDescriptor(BREAK_EVEN, "Break Even", 80, 0, 999, 1),
                new IntegerDescriptor(COVER, "Cover", 0, 0, 99, 1),
                new BooleanDescriptor(BREAK_EVEN_ENABLED, "Break Even Enabled", false));

        grp = tab.addGroup("ATR Stop");
        grp.addRow(new BooleanDescriptor(ATR_ENABLED, "ATR Enabled", false));
        grp.addRow(new IntegerDescriptor(ATR_PERIOD, "ATR Period", 40, 1, 999, 1),
                new DoubleDescriptor(ATR_MULTIPLIER, "ATR Multiplier", 5.0, 0.1, 999, 0.1));

        return tab;
    }

    protected SettingTab getRiskTab() {
        var tab = new SettingTab("Risk Session");
        var grp = tab.addGroup("Win");
        grp.addRow(new IntegerDescriptor(SESSION_MAX_WIN, "Max Win Session", 700, 0, 9999, 1));

        grp = tab.addGroup("Loss");
        grp.addRow(new IntegerDescriptor(SESSION_MAX_LOSS, "Max Loss Session", 600, 0, 9999, 1));

        grp = tab.addGroup("Successive Loss");
        grp.addRow(new BooleanDescriptor(MAX_SUCCESSIVE_LOSS_ENABLED, "Max Successive Loss Enabled", false));
        grp.addRow(new IntegerDescriptor(MAX_SUCCESSIVE_LOSS, "Max Successive Loss", 3, 1, 99, 1));
        return tab;
    }

    @Override
    public void onSettingsUpdated(DataContext ctx) {
        super.onSettingsUpdated(ctx);
        // debug("onSettingsUpdated");
        getSettings().setBoolean(ANALYSIS_ENABLED, false);
    }

    // Mise à jour de onActivate pour utiliser les bougies simulées
    public void onUpdateAnalysis(OrderContext ctx) {
        debug("onUpdateAnalysis");

        // for (int i = 0; i < patternGroups.size(); i++) {
        // PatternGroupKey group = patternGroups.get(i);

        // String enabledKey = "TICK_INTERVAL_ENABLED_" + (i + 1);
        // boolean enabled = getSettings().getBoolean(enabledKey);

        // if (enabled) {
        // if (!listActiveTickIntervals.contains(group.intervalValue)) {
        // listActiveTickIntervals.add(group.intervalValue);
        // }
        // } else {
        // listActiveTickIntervals.remove(group.intervalValue);
        // }
        // }

        String strategyPatternGroupKey = getSettings().getString(STRATEGY_PATTERN_GROUP);
        PatternGroupKey patternGroup = PatternGroupKey.fromKeyString(strategyPatternGroupKey);

        BarSize barSize1 = getSettings().getBarSize(TIMEFRAME_1);
        Enums.MAMethod maMethod1 = getSettings().getMAMethod(MA_METHOD_1);
        int period1 = getSettings().getInteger(PERIOD_1);

        BarSize barSize2 = getSettings().getBarSize(TIMEFRAME_2);
        Enums.MAMethod maMethod2 = getSettings().getMAMethod(MA_METHOD_2);
        int period2 = getSettings().getInteger(PERIOD_2);

        ZoneId zoneId = ZoneId.systemDefault();

        int daysAgo = getSettings().getInteger(START_DATETIME);
        long endMillis = getSettings().getLong(END_DATETIME);

        Instant endInstant = Instant.ofEpochMilli(endMillis);
        LocalDate endAnalyseDay = endInstant.atZone(zoneId).toLocalDate();
        LocalDate startAnalyseDay = endAnalyseDay.minusDays(daysAgo);
        int duration = daysAgo + 1; // Inclure le jour de fin

        int totalsimulatedBars = 0;

        // for (int tickInterval : listActiveTickIntervals) {
        // debug("Tick interval actif : " + tickInterval);

        // activeTickInterval = groupKey.tickInterval;
        activeTickInterval = patternGroup.intervalValue;

        realRenko = null;
        history = new HistoryManager();
        analysis = new CoreAnalysis(ctx.getInstrument().getSymbol(), ctx.getInstrument().getTickSize(), "ticks",
                activeTickInterval);

        Map<LocalDate, List<SimulatedBar>> simulatedBarsByDay = new TreeMap<>();
        Map<LocalDate, Map<Long, HiLoResult>> hiLo1ByDay = new TreeMap<>();
        Map<LocalDate, Map<Long, HiLoResult>> hiLo2ByDay = new TreeMap<>();

        LocalDate today = LocalDate.now(zoneId);
        LocalDateTime now = LocalDateTime.now(zoneId);

        for (int i = 0; i < duration; i++) {
            LocalDate analyseDay = startAnalyseDay.plusDays(i);
            // debug("analyseDay : " + analyseDay);

            LocalDateTime dayStartDateTime = LocalDateTime.of(analyseDay, LocalTime.MIDNIGHT);
            LocalDateTime dayEndDateTime = analyseDay.equals(today) ? now
                    : LocalDateTime.of(analyseDay, LocalTime.of(23, 59, 59));

            long dayStartMillis = dayStartDateTime.atZone(zoneId).toInstant().toEpochMilli();
            long dayEndMillis = dayEndDateTime.atZone(zoneId).toInstant().toEpochMilli();

            // debug("Requesting ticks from " + dayStartDateTime + " to " + dayEndDateTime);

            List<Tick> dayTicks = getTicksSafe(ctx, dayStartMillis, dayEndMillis);
            if (dayTicks == null || dayTicks.size() < activeTickInterval) {
                debug("continue dayTicks : " + (dayTicks == null ? 0 : dayTicks.size()));
                debug("continue dayTicks analyseDay : " + analyseDay);
                continue;
            }

            List<SimulatedBar> simulatedBars = aggregateSimulatedBars(dayTicks, activeTickInterval);
            if (!simulatedBars.isEmpty()) {
                simulatedBarsByDay.put(analyseDay, simulatedBars);
                totalsimulatedBars += simulatedBars.size();
            }

            List<Bar> dayBars1 = getBarsSafe(ctx, dayStartMillis, dayEndMillis, barSize1);
            if (dayBars1 == null || dayBars1.size() < period1) {
                debug("continue dayBars1 : " + (dayBars1 == null ? 0 : dayBars1.size()));
                debug("continue dayBars1 analyseDay : " + analyseDay);
                continue;
            }
            Map<Long, HiLoResult> hilo1 = buildHilo(dayBars1, maMethod1, period1);
            if (!hilo1.isEmpty())
                hiLo1ByDay.put(analyseDay, hilo1);

            List<Bar> dayBars2 = getBarsSafe(ctx, dayStartMillis, dayEndMillis, barSize2);
            if (dayBars2 == null || dayBars2.size() < period2) {
                debug("continue dayBars2 : " + (dayBars2 == null ? 0 : dayBars2.size()));
                debug("continue dayBars2 analyseDay : " + analyseDay);
                continue;
            }
            Map<Long, HiLoResult> hilo2 = buildHilo(dayBars2, maMethod2, period2);
            if (!hilo2.isEmpty())
                hiLo2ByDay.put(analyseDay, hilo2);
        }

        this.brickSize = getSettings().getInteger(RENKO_POINT) / 4.0;

        for (Map.Entry<LocalDate, List<SimulatedBar>> entry : simulatedBarsByDay.entrySet()) {
            LocalDate analyseDay = entry.getKey();
            List<SimulatedBar> simulatedBars = entry.getValue();

            for (SimulatedBar bar : simulatedBars) {
                Map<Long, HiLoResult> HiLoMap1 = hiLo1ByDay.get(analyseDay);
                Map<Long, HiLoResult> HiLoMap2 = hiLo2ByDay.get(analyseDay);
                if (HiLoMap1 == null || HiLoMap2 == null) {
                    continue;
                }

                long oneMinutesInMillis = 1 * 60 * 1000;
                long rounded1MinTimestamp = (bar.timestamp / oneMinutesInMillis) * oneMinutesInMillis;
                HiLoResult resultHiLo1 = HiLoMap1.get(rounded1MinTimestamp);

                long fiveMinutesInMillis = 5 * 60 * 1000;
                long rounded5MinTimestamp = (bar.timestamp / fiveMinutesInMillis) * fiveMinutesInMillis;
                HiLoResult resultHiLo2 = HiLoMap2.get(rounded5MinTimestamp);

                if (resultHiLo1 == null || resultHiLo2 == null) {
                    continue;
                }

                RenkoResult resultRenko = calculateRenkoTickBased(bar.close, resultHiLo1.hiLo, resultHiLo2.hiLo);

                if (resultRenko.crossAbove || resultRenko.crossBelow) {
                    if (!history.isArrayAsNull(history.renkoColorHistory)
                            && !history.isArrayAsNull(history.hiLoColor1History)
                            && !history.isArrayAsNull(history.hiLoColor2History)
                            && !history.isArrayAsNull(history.hiLoPrice1History)
                            && !history.isArrayAsNull(history.hiLoPrice2History)
                            && !history.isArrayAsNull(history.renkoPriceResHistory)
                            && !history.isArrayAsNull(history.renkoPriceSupHistory)) {
                        strategySignalOffContext(bar, resultRenko, ctx);
                    }
                }
            }
            debug("Date : " + analyseDay + " / Bougies simulées : " + simulatedBars.size());
        }
        // }

        debug("Analyse terminée. Total bougies simulées : " + totalsimulatedBars);
        debug("Mise à jour StrategyAnalysisV8 terminée.");
    }

    // --- Méthode utilitaire de regroupement des ticks ---
    public List<SimulatedBar> aggregateSimulatedBars(List<Tick> ticks, int groupSize) {
        List<SimulatedBar> bars = new ArrayList<>();

        for (int i = 0; i <= ticks.size() - groupSize; i += groupSize) {
            Double open = (double) ticks.get(i).getPrice();
            Double high = open;
            Double low = open;
            Double close = (double) ticks.get(i + groupSize - 1).getPrice();
            long timestamp = ticks.get(i + groupSize - 1).getTime();

            for (int j = i; j < i + groupSize; j++) {
                Double price = (double) ticks.get(j).getPrice();
                if (price > high)
                    high = price;
                if (price < low)
                    low = price;
            }

            // debug("timestamp : " + timestamp + " open : " + open + " high : " + high + "
            // low : " + low + " close : "
            // + close);

            bars.add(new SimulatedBar(timestamp, open, high, low, close));
        }

        return bars;
    }

    private List<Bar> getBarsSafe(OrderContext ctx, long startTime, long endTime, BarSize barSize) {
        try {
            return ctx.getDataContext().getInstrument().getBars(startTime, endTime, barSize, false);
        } catch (Exception e) {
            debug("Erreur chargement bars: " + e.getMessage());
            return null;
        }
    }

    private List<Tick> getTicksSafe(OrderContext ctx, long startTime, long endTime) {
        try {
            return ctx.getDataContext().getInstrument().getTicks(startTime, endTime);
        } catch (Exception e) {
            debug("Erreur chargement ticks: " + e.getMessage());
            return null;
        }
    }

    public RenkoResult calculateRenkoTickBased(Double currPrice, Double hiLo1, Double hiLo2) {
        boolean crossAbove = false;
        boolean crossBelow = false;

        if (realRenko == null) {
            Double lowerBound = Math.floor(currPrice / 10) * 10;
            Double upperBound = lowerBound + brickSize;

            if (currPrice > lowerBound + brickSize) {
                lowerBound += brickSize;
                upperBound = lowerBound + brickSize;
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

        return new RenkoResult(crossAbove, crossBelow, realRenko, previousClose);
    }

    private Map<Long, HiLoResult> buildHilo(List<Bar> dayBars, Enums.MAMethod maMethod, int period) {
        Map<Long, HiLoResult> hiLoResult = new HashMap<>();

        // Extraire high et low en Double
        List<Double> highs = new ArrayList<>();
        List<Double> lows = new ArrayList<>();

        for (Bar bar : dayBars) {
            highs.add((double) bar.getHigh());
            lows.add((double) bar.getLow());
        }

        // debug("highs : " + highs.size());
        // debug("lows : " + lows.size());

        List<Double> maHighs = calculateMA(highs, maMethod, period);
        List<Double> maLows = calculateMA(lows, maMethod, period);

        // debug("maHighs : " + maHighs.size());
        // debug("maLows : " + maLows.size());

        Double hiLo = null;

        for (int i = period - 1; i < dayBars.size(); i++) {
            // for (int i = 0; i < dayBars.size(); i++) {
            Bar current = dayBars.get(i);
            Double close = (double) current.getClose();
            Double high = maHighs.get(i);
            Double low = maLows.get(i);
            Long timestamp = current.getEndTime();

            // debug("timestamp : " + timestamp);
            // debug("current : " + current);
            // debug("close : " + close);
            // debug("high : " + high);
            // debug("low : " + low);

            if (hiLo == null) {
                hiLo = close;
            }

            if (high != null && low != null) {

                hiLo = close > hiLo ? low : close < hiLo ? high : close;
                Boolean isHigh = close > hiLo;
                Boolean isLow = close < hiLo;

                // debug("timestamp : " + timestamp);
                // debug("hiLo : " + hiLo);

                hiLoResult.put(timestamp, new HiLoResult(timestamp, hiLo, close, isHigh, isLow));
            } else {
                hiLoResult.put(timestamp, new HiLoResult(timestamp, hiLo, close, false, false));
            }

        }
        return hiLoResult;
    }

    private List<Double> calculateMA(List<Double> values, Enums.MAMethod method, int period) {
        List<Double> result = new ArrayList<>(Collections.nCopies(values.size(), null));

        switch (method) {
        case SMA:
            for (int i = period - 1; i < values.size(); i++) {
                Double sum = 0.0;
                for (int j = i - period + 1; j <= i; j++) {
                    sum += values.get(j);
                }
                result.set(i, sum / period);
            }
            break;

        case EMA:
            Double multiplier = 2.0 / (period + 1);
            Double emaPrev = null;
            for (int i = 0; i < values.size(); i++) {
                Double val = values.get(i);
                if (val == null)
                    continue;

                if (i == period - 1) {
                    Double sma = 0.0;
                    for (int j = i - period + 1; j <= i; j++) {
                        sma += values.get(j);
                    }
                    emaPrev = sma / period;
                    result.set(i, emaPrev);
                } else if (i >= period) {
                    emaPrev = (val - emaPrev) * multiplier + emaPrev;
                    result.set(i, emaPrev);
                }
            }
            break;

        case WMA: // Weighted Moving Average
            int weightSum = period * (period + 1) / 2;
            for (int i = period - 1; i < values.size(); i++) {
                Double wma = 0.0;
                for (int j = 0; j < period; j++) {
                    wma += values.get(i - j) * (period - j);
                }
                result.set(i, wma / weightSum);
            }
            break;

        case SMMA: // Smoothed MA (souvent utilisée pour RSI)
            Double smmaPrev = null;
            for (int i = 0; i < values.size(); i++) {
                Double val = values.get(i);
                if (val == null)
                    continue;

                if (i == period - 1) {
                    Double sum = 0.0;
                    for (int j = i - period + 1; j <= i; j++) {
                        sum += values.get(j);
                    }
                    smmaPrev = sum / period;
                } else if (i >= period) {
                    smmaPrev = (smmaPrev * (period - 1) + val) / period;
                }
                if (i >= period - 1)
                    result.set(i, smmaPrev);
            }
            break;

        default:
            throw new UnsupportedOperationException("Méthode MA non supportée : " + method);
        }

        return result;
    }

    private void strategySignalOffContext(SimulatedBar bar, RenkoResult resultRenko, OrderContext ctx) {
        var settings = getSettings();
        if (settings == null)
            return;

        // debug("strategySignalOffContext");

        Double priceSup = resultRenko.previousClose;
        Double priceRes = resultRenko.realRenko;

        Double priceClose = bar.close;
        Double priceHigh = bar.high;
        Double priceLow = bar.low;

        if (priceSup == null || priceRes == null)
            return;

        int index = ctx.getDataContext().getDataSeries().findIndex(bar.timestamp);

        // analysis.analyseSignalEntry(priceHigh, priceLow);

        // CorePattern strategyCore = new CorePattern(history.renkoColorHistory,
        // history.hiLoColor1History,
        // history.hiLoColor2History, history.hiLoPrice1History,
        // history.hiLoPrice2History, priceRes, priceClose);

        // List<PatternCheckResult> results = strategyCore.checkPattern();

        String strategyPatternGroupKey = getSettings().getString(STRATEGY_PATTERN_GROUP);
        PatternGroupKey patternGroup = PatternGroupKey.fromKeyString(strategyPatternGroupKey);

        String className = String.format("sbdev.joy_ai_project.strategy_v8o3.core_pattern.CorePattern_%d_%d_%d",
                patternGroup.intervalValue, patternGroup.tp, patternGroup.sl);

        try {
            // On instancie la classe dynamiquement
            InterfaceCorePattern strategyCore = (InterfaceCorePattern) Class.forName(className)
                    .getConstructor(String[].class, String[].class, String[].class, Double[].class, Double[].class,
                            Double.class, Double.class)
                    .newInstance(history.renkoColorHistory, history.hiLoColor1History, history.hiLoColor2History,
                            history.hiLoPrice1History, history.hiLoPrice2History, priceRes, priceClose);

            // Utilisation simple et typée de la méthode :
            List<PatternCheckResult> results = strategyCore.checkPattern();
            System.out.println("Résultats pour " + className + " : " + results.size() + " patterns trouvés.");

            String popupMsgIndex = "Index : " + index + " => Current : " + format(priceRes);
            String popupMsgInfo = popupMsgIndex;

            for (PatternCheckResult result : results) {

                if (result.interval == activeTickInterval) {

                    if (resultRenko.crossAbove || resultRenko.crossBelow) {

                        String popupMsgStrat = "\n Pattern: " + result.patternName + "\n Signal : long="
                                + result.okLong() + ", short=" + result.okShort() + "\n Direction : high="
                                + result.direction.high + ", low=" + result.direction.low + "\n Strength : high="
                                + result.strength.high + ", low=" + result.strength.low + "\n Renko : high="
                                + result.renko.high + ", low=" + result.renko.low + "\n Hilo : high=" + result.hilo.high
                                + ", low=" + result.hilo.low
                        // + "\n ADX : " + series.getDouble(index, Values.ADX)
                        ;

                        String popupMsgSignal = popupMsgIndex + popupMsgStrat;

                        if (result.okLong()) {
                            // debug("Signal : okLong= " + result.okLong());
                            // debug("setCurrentStrategy : patternName= " + result.patternName);
                            // debug("setCurrentStrategy : getSymbol= " + ctx.getInstrument().getSymbol());
                            // debug("setCurrentStrategy : activeTickInterval= " + activeTickInterval);
                            // debug("setCurrentStrategy : tp= " + result.bracket.tp + ", sl= " +
                            // result.bracket.sl);

                            // analysis.setCurrentStrategy(result.patternName, result.bracket.tp,
                            // result.bracket.sl);
                            // analysis.analyseSignalEntryInit(result.patternName, bar.timestamp,
                            // CoreAnalysis.Direction.HIGH, priceClose);

                            var c = new Coordinate(bar.timestamp, priceRes);
                            var marker = settings.getMarker(UP_MARKER);
                            String msg = popupMsgSignal;
                            if (marker.isEnabled())
                                addFigure(new Marker(c,
                                        resultRenko.crossAbove ? Enums.Position.TOP : Enums.Position.BOTTOM, marker,
                                        msg));
                            ctx.getDataContext().signal(index, Signals.LONG, msg, priceSup);
                        }

                        if (result.okShort()) {
                            // debug("Signal : okShort= " + result.okShort());
                            // debug("setCurrentStrategy : patternName= " + result.patternName);
                            // debug("setCurrentStrategy : getSymbol= " + ctx.getInstrument().getSymbol());
                            // debug("setCurrentStrategy : activeTickInterval= " + activeTickInterval);
                            // debug("setCurrentStrategy : tp= " + result.bracket.tp + ", sl= " +
                            // result.bracket.sl);

                            // analysis.setCurrentStrategy(result.patternName, result.bracket.tp,
                            // result.bracket.sl);
                            // analysis.analyseSignalEntryInit(result.patternName, bar.timestamp,
                            // CoreAnalysis.Direction.LOW, priceClose);
                            var c = new Coordinate(bar.timestamp, priceRes);
                            var marker = settings.getMarker(DOWN_MARKER);
                            String msg = popupMsgSignal;
                            if (marker.isEnabled())
                                addFigure(new Marker(c,
                                        resultRenko.crossAbove ? Enums.Position.TOP : Enums.Position.BOTTOM, marker,
                                        msg));
                            ctx.getDataContext().signal(index, Signals.SHORT, msg, priceSup);
                        }
                    }
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

        if (getSettings().getBoolean(ANALYSIS_ENABLED)) {
            onUpdateAnalysis(ctx);
        }

        Double pnlTotal = ctx.getTotalRealizedPnL();
        pnlSession = 0.00;
        int max = getSettings().getInteger(SESSION_MAX_WIN);
        int min = getSettings().getInteger(SESSION_MAX_LOSS) * -1;
        debug("Risk Management -> check PnL Total : " + pnlTotal + ", Min: " + min + ", Max: " + max);

    }

    @Override
    public void onSignal(OrderContext ctx, Object signal) {
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
        // var series = ctx.getDataContext().getDataSeries();

        debug("Risk Management -> check PnL Total : " + pnl + ", PnL Session: " + pnlSession + ", Min: " + min
                + ", Max: " + max);

        if (successiveLossEnabled) {
            if (lastPnl > 0) {
                successiveLoss = 0;
            }
            if (lastPnl < 0) {
                successiveLoss += 1;
                // if (pnlMax == true) {
                // successiveLoss = 99;
                // debug("One Loss after max PnL");
                // }
            }
            debug("Successive Loss: " + successiveLoss);
        } else {
            successiveLoss = 0;
        }

        // debug("avgEntryPrice : " + series.getClose());
        // debug("hiLoPrice1History : " + hiLoPrice1History[9]);
        // debug("hiLoPrice2History : " + hiLoPrice2History[9]);
        // debug("hiLoColor1History : " + hiLoColor1History[9]);
        // debug("hiLoColor2History : " + hiLoColor2History[9]);

        // Boolean okLong = hiLoColor1History[0] == GREEN && hiLoColor2History[0] ==
        // GREEN && hiLoPrice1History[0] < series.getClose()
        // && hiLoPrice2History[0] < series.getClose();
        // Boolean okShort = hiLoColor1History[0] == RED && hiLoColor2History[0] == RED
        // && hiLoPrice1History[0] > series.getClose()
        // && hiLoPrice2History[0] > series.getClose();

        // debug("okLong : " + okLong);
        // debug("okShort : " + okShort);

        // getExportedValue(Values.MA_HIGH_LOW_1);
        // debug("mA1 : " + mA1);
        // debug("mA2 : " + mA2);

        // Boolean pnlMax = false;
        // if (pnl > max) {
        // pnlMax = true;
        // }

        // if (successiveLoss < maxSuccessiveLoss && min < pnlSession && pnlSession <
        // max) {
        if (successiveLoss < maxSuccessiveLoss && !pnlMax) {
            // if (position == 0 && signal == Signals.LONG && okLong) {

            startSession = false;
            // pnlSession += lastPnl;

            if (position == 0 && signal == Signals.LONG) {
                debug("isLong : " + isLong() + " / signal : " + signal);
                // setEntryState(Enums.EntryState.NONE);
                // setLong(true);
                // onEnterNow(ctx);
            }
            // if (position == 0 && signal == Signals.SHORT && okShort) {
            if (position == 0 && signal == Signals.SHORT) {
                debug("isLong : " + isLong() + " / signal : " + signal);
                // setEntryState(Enums.EntryState.NONE);
                // setLong(false);
                // onEnterNow(ctx);
            }

            // Instrument instr = ctx.getInstrument();

            // float qty = (getSettings().getTradeLots() *
            // instr.getDefaultQuantityAsFloat());

            // qty += Math.abs(position); // Stop and Reverse if there is an open position
            // if (position <= 0 && signal == Signals.LONG) {
            // ctx.buy(qty); // Open Long Position
            // }
            // if (position >= 0 && signal == Signals.SHORT) {
            // ctx.sell(qty); // Open Short Position
            // }

        } else {
            debug("Stop Pnl Session : " + pnlSession);
            if (successiveLossEnabled) {
                debug("Successive Loss: " + successiveLoss);
            }
            // onDeactivate(ctx);
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
        float offset = (float) (getSettings().getDouble(MAX_LOSS) * step);
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
            firstExit = instr.round(ctx.getAvgEntryPrice() + getSettings().getInteger(EXIT) * step);
        else
            firstExit = instr.round(ctx.getAvgEntryPrice() - getSettings().getInteger(EXIT) * step);

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
