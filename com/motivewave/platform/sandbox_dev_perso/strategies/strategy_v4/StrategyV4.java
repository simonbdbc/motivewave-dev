package custom_studies.strategies.strategy_v4;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.common.Enums.StrategyState;
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
import com.motivewave.platform.sdk.common.desc.DateTimeDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.draw.Line;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

import custom_studies.strategies.strategy_core_v4.StrategyAnalysis;
import custom_studies.strategies.strategy_core_v4.StrategyCorePattern;
import custom_studies.strategies.strategy_core_v4.db.StrategyDatabaseInitializer;
import custom_studies.strategies.strategy_core_v4.utils.HistoryManager;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.HiLoResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternCheckResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.RenkoResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.SimulatedBar;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@StudyHeader(id = "STRATEGY_V4", namespace = "com.sbdev", name = "Strategy V4", desc = "Strategy V4", menu = "Custom Strategies", overlay = true, strategy = true, autoEntry = true, manualEntry = false, supportsStopPL = true, supportsUnrealizedPL = true, supportsRealizedPL = true, supportsTotalPL = true, supportsTargetPL = true, supportsRiskRatio = true, showTradeOptions = false, requiresBarUpdates = true)
public class StrategyV4 extends Study {

    // protected HistoryManager history = new HistoryManager();
    // protected StrategyAnalysis analysis = new StrategyAnalysis();
    protected HistoryManager history;
    protected StrategyAnalysis analysis;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    protected Double brickSize = null; // Taille de la brique
    protected Double previousClose = null; // Renko open
    // protected Double realRenko = null; // Renko close
    protected Double realRenko; // Renko close

    final static String GREEN = "green";
    final static String RED = "red";
    final static String ABOVE = "above";
    final static String BELOW = "below";

    protected Double hilo1 = null;
    protected Double hilo2 = null;

    // public boolean crossAbove = false;
    // public boolean crossBelow = false;

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
        ADX, DX, PDM, NDM, TR,
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

    private Line prevLineUp1 = null, prevLineDown1 = null;

    int highsOffset = 3;
    int lowsOffset = 3;

    final static String START_DATETIME = "startDateTime";
    final static String END_DATETIME = "endDateTime";

    public static final String TICK_INTERVAL_1 = "tickInteval1";
    public static final String TICK_INTERVAL_ENABLED_1 = "tickIntevalEnabled1";
    public static final String TICK_INTERVAL_2 = "tickInteval2";
    public static final String TICK_INTERVAL_ENABLED_2 = "tickIntevalEnabled2";
    public static final String TICK_INTERVAL_3 = "tickInteval3";
    public static final String TICK_INTERVAL_ENABLED_3 = "tickIntevalEnabled3";
    public static final String TICK_INTERVAL_4 = "tickInteval4";
    public static final String TICK_INTERVAL_ENABLED_4 = "tickIntevalEnabled4";

    private List<Integer> listActiveTickIntervals = new ArrayList<>();
    private Integer activeTickInterval = null;

    @Override
    public void initialize(Defaults defaults) {

        var sd = createSD();

        sd.addTab(initializeAnalyseTab(defaults));
        sd.addTab(initializeGeneralTab(defaults));

        // Quick Settings (Tool Bar and Popup Editor)
        sd.addQuickSettings(RENKO_POINT);
        sd.addQuickSettings(TOP_PATH, BOTTOM_PATH);

        sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_1, TICK_INTERVAL_1));
        sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_2, TICK_INTERVAL_2));
        sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_3, TICK_INTERVAL_3));
        sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_4, TICK_INTERVAL_4));

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

        // Declare paths for drawing
        desc.declarePath(Values.MA_HILO_1, PATH_HILO_1);
        desc.declarePath(Values.MA_HILO_2, PATH_HILO_2);

        // Signals
        desc.declareSignal(Signals.CROSS_ABOVE, "PRICE_CROSS_ABOVE");
        desc.declareSignal(Signals.CROSS_BELOW, "PRICE_CROSS_BELOW");

        // StrategyDatabaseInitializer.initDatabase(); // Initialise les tables

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

    protected SettingTab initializeAnalyseTab(Defaults defaults) {
        var tab = new SettingTab("Analyse");

        // Section 1
        var section1 = tab.addGroup("Interval");
        section1.addRow(new IntegerDescriptor(TICK_INTERVAL_1, "Tick Interval 1", 25, 1, 1000, 1),
                new BooleanDescriptor(TICK_INTERVAL_ENABLED_1, "Use Tick Interval 1", true));
        section1.addRow(new IntegerDescriptor(TICK_INTERVAL_2, "Tick Interval 2", 50, 1, 1000, 1),
                new BooleanDescriptor(TICK_INTERVAL_ENABLED_2, "Use Tick Interval 2", true));
        section1.addRow(new IntegerDescriptor(TICK_INTERVAL_3, "Tick Interval 3", 100, 1, 1000, 1),
                new BooleanDescriptor(TICK_INTERVAL_ENABLED_3, "Use Tick Interval 3", true));
        section1.addRow(new IntegerDescriptor(TICK_INTERVAL_4, "Tick Interval 4", 200, 1, 1000, 1),
                new BooleanDescriptor(TICK_INTERVAL_ENABLED_4, "Use Tick Interval 4", true));

        // Section 2
        var date = tab.addGroup("Date");
        date.addRow(new DateTimeDescriptor(START_DATETIME, "Start Date", Instant.now().toEpochMilli(), true));
        date.addRow(new IntegerDescriptor(END_DATETIME, "Analysis Duration (Days)", 30, 1, 365, 1));

        // Section 2
        var hours = tab.addGroup("Heures de filtrage");
        hours.addRow(new IntegerDescriptor("startHour", "Heure Début", 0, 0, 23, 1),
                new IntegerDescriptor("startMinute", "Minutes", 0, 0, 59, 1));
        hours.addRow(new IntegerDescriptor("endHour", "Heure Fin", 23, 1, 23, 1),
                new IntegerDescriptor("endMinute", "Minutes", 0, 0, 59, 1));

        return tab;
    }

    // @Override
    // public void onSettingsUpdated(DataContext ctx) {
    //     // TODO Auto-generated method stub
    //     super.onSettingsUpdated(ctx);
    //     getDataStats();
    // }

    private <T> void shiftAndInsert(T[] array, T value) {
        for (int i = array.length - 1; i > 0; i--) {
            array[i] = array[i - 1];
        }
        array[0] = value;
    }

    // Mise à jour de onActivate pour utiliser les bougies simulées
    @Override
    public void onActivate(OrderContext ctx) {
        debug("StrategyTickV2_onActivate");
        debug("StrategyTickV2_onActivate gradle auto");

        StrategyDatabaseInitializer.initDatabase(); // Initialise les tables

        boolean tickIntervalEnabled1 = getSettings().getBoolean(TICK_INTERVAL_ENABLED_1);
        int tickInterval1 = getSettings().getInteger(TICK_INTERVAL_1);
        boolean tickIntervalEnabled2 = getSettings().getBoolean(TICK_INTERVAL_ENABLED_2);
        int tickInterval2 = getSettings().getInteger(TICK_INTERVAL_2);
        boolean tickIntervalEnabled3 = getSettings().getBoolean(TICK_INTERVAL_ENABLED_3);
        int tickInterval3 = getSettings().getInteger(TICK_INTERVAL_3);
        boolean tickIntervalEnabled4 = getSettings().getBoolean(TICK_INTERVAL_ENABLED_4);
        int tickInterval4 = getSettings().getInteger(TICK_INTERVAL_4);

        if (tickIntervalEnabled1)
            listActiveTickIntervals.add(tickInterval1);
        if (tickIntervalEnabled2)
            listActiveTickIntervals.add(tickInterval2);
        if (tickIntervalEnabled3)
            listActiveTickIntervals.add(tickInterval3);
        if (tickIntervalEnabled4)
            listActiveTickIntervals.add(tickInterval4);

        long startMillis = getSettings().getLong(START_DATETIME);
        int duration = getSettings().getInteger(END_DATETIME);

        // boolean tfEnabled1 = getSettings().getBoolean(TF_ENABLED_1);
        BarSize barSize1 = getSettings().getBarSize(TIMEFRAME_1);
        Enums.MAMethod maMethod1 = getSettings().getMAMethod(MA_METHOD_1);
        int period1 = getSettings().getInteger(PERIOD_1);

        // boolean tfEnabled2 = getSettings().getBoolean(TF_ENABLED_2);
        BarSize barSize2 = getSettings().getBarSize(TIMEFRAME_2);
        Enums.MAMethod maMethod2 = getSettings().getMAMethod(MA_METHOD_2);
        int period2 = getSettings().getInteger(PERIOD_2);

        ZoneId zoneId = ZoneId.systemDefault();

        Instant start = Instant.ofEpochMilli(startMillis);
        LocalDate startAnalyseDay = start.atZone(zoneId).toLocalDate();

        int startHour = getSettings().getInteger("startHour");
        int startMinute = getSettings().getInteger("startMinute");
        int endHour = getSettings().getInteger("endHour");
        int endMinute = getSettings().getInteger("endMinute");

        LocalTime sessionStart = LocalTime.of(startHour, startMinute);
        LocalTime sessionEnd = LocalTime.of(endHour, endMinute);

        int totalsimulatedBars = 0;

        for (int tickInterval : listActiveTickIntervals) {
            // System.out.println("Tick interval actif : " + tickInterval);
            debug("Tick interval actif : " + tickInterval);

            activeTickInterval = tickInterval;

            realRenko = null;
            history = new HistoryManager();
            analysis = new StrategyAnalysis(ctx.getInstrument().getSymbol(), "ticks", activeTickInterval);

            Map<LocalDate, List<SimulatedBar>> simulatedBarsByDay = new TreeMap<>();
            Map<LocalDate, Map<Long, HiLoResult>> hiLo1ByDay = new TreeMap<>();
            Map<LocalDate, Map<Long, HiLoResult>> hiLo2ByDay = new TreeMap<>();

            for (int i = 0; i < duration; i++) {
                LocalDate analyseDay = startAnalyseDay.plusDays(i);
                debug("analyseDay : " + analyseDay);

                LocalDateTime dayStartDateTime = LocalDateTime.of(analyseDay, LocalTime.of(0, 0));
                LocalDateTime dayEndDateTime = LocalDateTime.of(analyseDay, LocalTime.of(23, 59));

                long dayStartMillis = dayStartDateTime.atZone(zoneId).toInstant().toEpochMilli();
                long dayEndMillis = dayEndDateTime.atZone(zoneId).toInstant().toEpochMilli();

                debug("Requesting ticks from " + dayStartDateTime + " to " + dayEndDateTime);
                // debug("Timestamp from " + dayStartMillis + " to " + dayEndMillis);

                List<Tick> dayTicks = getTicksSafe(ctx, dayStartMillis, dayEndMillis);
                if (dayTicks == null || dayTicks.size() < activeTickInterval) {
                    debug("continue dayTicks : " + dayTicks.size());
                    continue;
                }

                List<SimulatedBar> simulatedBars = aggregateSimulatedBars(dayTicks, activeTickInterval);

                List<SimulatedBar> filteredSimulatedBars = simulatedBars.stream()
                        .filter(bar -> {
                            LocalTime time = Instant.ofEpochMilli(bar.timestamp).atZone(zoneId).toLocalTime();
                            return !time.isBefore(sessionStart) && !time.isAfter(sessionEnd);
                        })
                        .collect(Collectors.toList());

                if (!filteredSimulatedBars.isEmpty()) {
                    simulatedBarsByDay.put(analyseDay, filteredSimulatedBars);
                    totalsimulatedBars += filteredSimulatedBars.size();
                }

                List<Bar> dayBars1 = getBarsSafe(ctx, dayStartMillis, dayEndMillis, barSize1);
                if (dayBars1 == null || dayBars1.size() < period1) {
                    debug("continue dayBars1 : " + dayBars1.size());
                    continue;
                }
                Map<Long, HiLoResult> hilo1 = buildHilo(dayBars1, maMethod1, period1);

                if (!hilo1.isEmpty()) {
                    hiLo1ByDay.put(analyseDay, hilo1);
                }

                List<Bar> dayBars2 = getBarsSafe(ctx, dayStartMillis, dayEndMillis, barSize2);
                if (dayBars2 == null || dayBars2.size() < period2) {
                    debug("continue dayBars2 : " + dayBars2.size());
                    continue;
                }
                Map<Long, HiLoResult> hilo2 = buildHilo(dayBars2, maMethod2, period2);

                if (!hilo2.isEmpty()) {
                    hiLo2ByDay.put(analyseDay, hilo2);
                }

            }

            this.brickSize = getSettings().getInteger(RENKO_POINT) / 4.0;

            for (Map.Entry<LocalDate, List<SimulatedBar>> entry : simulatedBarsByDay.entrySet()) {
                LocalDate analyseDay = entry.getKey();
                List<SimulatedBar> simulatedBars = entry.getValue();

                for (SimulatedBar bar : simulatedBars) {
                    Map<Long, HiLoResult> HiLoMap1 = hiLo1ByDay.get(analyseDay);
                    long oneMinutesInMillis = 1 * 60 * 1000;
                    long rounded1MinTimestamp = (bar.timestamp / oneMinutesInMillis) * oneMinutesInMillis;
                    HiLoResult resultHiLo1 = HiLoMap1.get(rounded1MinTimestamp);

                    Map<Long, HiLoResult> HiLoMap2 = hiLo2ByDay.get(analyseDay);
                    long fiveMinutesInMillis = 5 * 60 * 1000;
                    long rounded5MinTimestamp = (bar.timestamp / fiveMinutesInMillis) * fiveMinutesInMillis;
                    HiLoResult resultHiLo2 = HiLoMap2.get(rounded5MinTimestamp);

                    if (resultHiLo1 == null || resultHiLo2 == null) {
                        continue;
                    }

                    RenkoResult resultRenko = calculateRenkoTickBased(bar.close, resultHiLo1.hiLo, resultHiLo2.hiLo);

                    if (resultRenko.crossAbove || resultRenko.crossBelow) {

                        // Debug Section
                        // if (resultRenko.crossAbove) {
                        // debug("Cross ABOVE at " + bar.close);
                        // debug("Date : " + dateFormat.format(new Date(bar.timestamp)));
                        // if (resultHiLo1 != null) {
                        // debug("HiLo1 isHigh : " + resultHiLo1.isHigh + " isLow : " +
                        // resultHiLo1.isLow);
                        // debug("HiLo1 close : " + resultHiLo1.close);
                        // debug("HiLo1 hiLo : " + resultHiLo1.hiLo);
                        // }
                        // if (resultHiLo2 != null) {
                        // debug("HiLo2 isHigh : " + resultHiLo2.isHigh + " isLow : " +
                        // resultHiLo2.isLow);
                        // debug("HiLo2 close : " + resultHiLo2.close);
                        // debug("HiLo2 hiLo : " + resultHiLo2.hiLo);
                        // }
                        // } else if (resultRenko.crossBelow) {
                        // debug("Cross BELOW at " + bar.close);
                        // debug("Date : " + dateFormat.format(new Date(bar.timestamp)));
                        // if (resultHiLo1 != null) {
                        // debug("HiLo1 isHigh : " + resultHiLo1.isHigh + " isLow : " +
                        // resultHiLo1.isLow);
                        // debug("HiLo1 close : " + resultHiLo1.close);
                        // debug("HiLo1 hiLo : " + resultHiLo1.hiLo);
                        // }
                        // if (resultHiLo2 != null) {
                        // debug("HiLo2 isHigh : " + resultHiLo2.isHigh + " isLow : " +
                        // resultHiLo2.isLow);
                        // debug("HiLo2 close : " + resultHiLo2.close);
                        // debug("HiLo2 hiLo : " + resultHiLo2.hiLo);
                        // }
                        // }

                        if (!history.isArrayAsNull(history.colorHistory)
                                && !history.isArrayAsNull(history.hiLoColor1History) &&
                                !history.isArrayAsNull(history.hiLoColor2History)
                                && !history.isArrayAsNull(history.hiLoPrice1History)
                                && !history.isArrayAsNull(history.hiLoPrice2History)) {
                            strategySignalOffContext(bar, resultRenko, ctx);
                        }
                    }
                }
                debug("Date : " + analyseDay + " / Bougies simulées : " + simulatedBars.size());
            }
        }

        debug("Analyse terminée. Total bougies simulées : " + totalsimulatedBars);

        // getDataStats();
        debug("Mise à jour terminée.");
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
                history.updateColorHistory(GREEN);
            } else {
                history.updateColorHistory(RED);
            }
        } else {
            if (history.colorHistory[0].equals(GREEN) && currPrice - realRenko >= brickSize) {
                realRenko += Math.floor((currPrice - realRenko) / brickSize) * brickSize;
                previousClose = realRenko - brickSize;
                crossAbove = true;
                history.updateColorHistory(GREEN);
                history.updateCrossHistory(ABOVE);
                history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
                history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History,
                        history.hiLoPrice2History);
            } else if (history.colorHistory[0].equals(GREEN) && realRenko - currPrice >= 2 * brickSize) {
                realRenko -= Math.floor((realRenko - currPrice) / brickSize) * brickSize;
                previousClose = realRenko + brickSize;
                crossBelow = true;
                history.updateColorHistory(RED);
                history.updateCrossHistory(BELOW);
                history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
                history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History,
                        history.hiLoPrice2History);
            } else if (history.colorHistory[0].equals(RED) && currPrice - realRenko >= 2 * brickSize) {
                realRenko += Math.floor((currPrice - realRenko) / brickSize) * brickSize;
                previousClose = realRenko - brickSize;
                crossAbove = true;
                history.updateColorHistory(GREEN);
                history.updateCrossHistory(ABOVE);
                history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
                history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History,
                        history.hiLoPrice2History);
            } else if (history.colorHistory[0].equals(RED) && realRenko - currPrice >= brickSize) {
                realRenko -= Math.floor((realRenko - currPrice) / brickSize) * brickSize;
                previousClose = realRenko + brickSize;
                crossBelow = true;
                history.updateColorHistory(RED);
                history.updateCrossHistory(BELOW);
                history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
                history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History,
                        history.hiLoPrice2History);
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

        Double priceSupp = resultRenko.previousClose;
        Double priceRes = resultRenko.realRenko;

        Double priceClose = bar.close;
        Double priceHigh = bar.high;
        Double priceLow = bar.low;

        if (priceSupp == null || priceRes == null)
            return;

        analysis.analyseSignalEntry(priceHigh, priceLow);

        StrategyCorePattern strategyCore = new StrategyCorePattern(
                history.colorHistory, history.hiLoColor1History, history.hiLoColor2History,
                history.hiLoPrice1History, history.hiLoPrice2History, priceRes, priceClose);

        // String popupMsgIndex = "Date : " + dateFormat.format(new Date(bar.timestamp))
        // + " => Current : "
        // + format(priceRes);
        // String popupMsgAnalyse = analysis.printStats();

        List<PatternCheckResult> results = strategyCore.checkPattern();

        for (PatternCheckResult result : results) {

            // String popupMsgStrat = "\n Pattern: " + result.patternName
            // + "\n Signal : long=" + result.okLong() + ", short=" + result.okShort()
            // + "\n Direction : high=" + result.direction.high + ", low=" +
            // result.direction.low
            // + "\n Strength : high=" + result.strength.high + ", low=" +
            // result.strength.low
            // + "\n Renko : high=" + result.renko.high + ", low=" + result.renko.low
            // + "\n Hilo : high=" + result.hilo.high + ", low=" + result.hilo.low
            // // + "\n ADX : " + series.getDouble(index, Values.ADX)
            // ;

            // String popupMsgSignal = popupMsgIndex + popupMsgStrat + popupMsgAnalyse;

            if (resultRenko.crossAbove && result.okLong()) {
                debug("Signal : crossAbove= " + resultRenko.crossAbove + ", okLong= " + result.okLong());
                // debug(popupMsgSignal);
                // var c = new Coordinate(series.getStartTime(index), priceRes);
                // var marker = settings.getMarker(UP_MARKER);
                // String msg = popupMsgSignal;
                // if (marker.isEnabled())
                // addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
                // ctx.signal(index, Signals.CROSS_HIGH, msg, priceSupp);

                // analysis.setCurrentStrategy(result.patternName, result.bracket.tp,
                // result.bracket.sl);

                debug("setCurrentStrategy : patternName= " + result.patternName);
                debug("setCurrentStrategy : getSymbol= " + ctx.getInstrument().getSymbol());
                debug("setCurrentStrategy : activeTickInterval= " + activeTickInterval);
                debug("setCurrentStrategy : tp= " + result.bracket.tp + ", sl= " + result.bracket.sl);

                analysis.setCurrentStrategy(result.patternName, result.bracket.tp, result.bracket.sl);
                analysis.analyseSignalEntryInit(bar.timestamp, StrategyAnalysis.Direction.HIGH, priceClose);

            }

            if (resultRenko.crossBelow && result.okShort()) {
                debug("Signal : crossBelow= " + resultRenko.crossBelow + ", okShort= " + result.okShort());
                // debug(popupMsgSignal);
                // var c = new Coordinate(series.getStartTime(index), priceRes);
                // var marker = settings.getMarker(DOWN_MARKER);
                // String msg = popupMsgSignal;
                // if (marker.isEnabled())
                // addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
                // ctx.signal(index, Signals.CROSS_LOW, msg, priceSupp);
                debug("setCurrentStrategy : patternName= " + result.patternName);
                debug("setCurrentStrategy : getSymbol= " + ctx.getInstrument().getSymbol());
                debug("setCurrentStrategy : activeTickInterval= " + activeTickInterval);
                debug("setCurrentStrategy : tp= " + result.bracket.tp + ", sl= " + result.bracket.sl);

                analysis.setCurrentStrategy(result.patternName, result.bracket.tp, result.bracket.sl);
                analysis.analyseSignalEntryInit(bar.timestamp, StrategyAnalysis.Direction.LOW, priceClose);

            }
        }

    }

    // public void getDataStats() {
    //     clearFigures();

    //     String[][] formatedData = analysis.getFormattedStatsTableSortedByProfitFactor();

    //     if (formatedData.length == 0 && formatedData[1].length == 0) {
    //         debug("formatedData is too short or empty");
    //     }

    //     TableauMotiveWaveFigure tableau = new TableauMotiveWaveFigure(formatedData);
    //     addFigure(tableau);
    // }

    public class TableauMotiveWaveFigure extends Figure {

        private final String[][] data;

        public TableauMotiveWaveFigure(String[][] inputData) {
            // Transposition des données ici
            int rows = inputData.length;
            int cols = inputData[0].length;
            this.data = new String[cols][rows];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    this.data[j][i] = inputData[i][j];
                }
            }
        }

        @Override
        public void draw(Graphics2D g2d, DrawContext context) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));

            int rowHeight = 20;
            int colWidth = 90;

            Rectangle bounds = context.getBounds();
            // int xStart = bounds.x + bounds.width - (colWidth * data[0].length) - 20;
            // int yStart = bounds.y + bounds.height - (rowHeight * data.length) - 20;
            int xStart = bounds.x;
            int yStart = bounds.y + bounds.height - (rowHeight * data.length) - 20;

            for (int row = 0; row < data.length; row++) {
                for (int col = 0; col < data[row].length; col++) {
                    int x = xStart + col * colWidth;
                    int y = yStart + row * rowHeight;

                    g2d.setColor(new Color(33, 33, 33));
                    g2d.fillRect(x, y, colWidth, rowHeight);
                    g2d.setColor(Color.WHITE);
                    g2d.drawRect(x, y, colWidth, rowHeight);

                    g2d.setFont(row == 0 || col == 0 ? new Font("Arial", Font.BOLD, 12)
                            : new Font("Arial", Font.PLAIN, 10));
                    g2d.drawString(data[row][col], x + 10, y + 15);
                }
            }
        }
    }

    // @Override
    // protected void postcalculate(DataContext ctx) {
    // realRenko = null;
    // }

    // @Override
    // protected void calculate(int index, DataContext ctx) {
    // var settings = getSettings();
    // if (settings == null)
    // return;

    // var series = ctx.getDataSeries();
    // Double close = series.getClose(index);

    // calculateHiLo1(index, ctx, close);
    // calculateHiLo2(index, ctx, close);
    // initialiseADX(index, ctx);

    // if (settings.getBoolean(RENKO_ENABLED)) {
    // calculateRenko(index, ctx, close);
    // }
    // }

    private void calculateHiLo1(int index, DataContext ctx, Double close) {
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
            hilo1 = maHigh1;
        } else if (close > maHigh1) {
            series.setDouble(index, Values.MA_HILO_1, maLow1);
            series.setPathColor(index, Values.MA_HILO_1, settings.getColor(LOW_COLOR_1));
            hilo1 = maLow1;
        }
        series.setComplete(index);
    }

    private void calculateHiLo2(int index, DataContext ctx, Double close) {
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
            hilo2 = maHigh2;
        } else if (close > maHigh2) {
            series.setDouble(index, Values.MA_HILO_2, maLow2);
            series.setPathColor(index, Values.MA_HILO_2, settings.getColor(LOW_COLOR_2));
            hilo2 = maLow2;
        }
        series.setComplete(index);
    }

    // protected void initialiseADX(int index, DataContext ctx) {
    // var seriesADX = ctx.getDataSeries();
    // int period = 14;

    // if (seriesADX == null || seriesADX.size() <= period)
    // return;

    // // Calculate the +DM, -DM and TR
    // Double pDM = seriesADX.getPositiveDM(index);
    // Double nDM = seriesADX.getNegativeDM(index);
    // Double tr = seriesADX.getTrueRange(index);

    // seriesADX.setFloat(index, Values.PDM, pDM);
    // seriesADX.setFloat(index, Values.NDM, nDM);
    // seriesADX.setFloat(index, Values.TR, tr);

    // if (index <= period)
    // return; // not enough data to calculate the first set of averages

    // // Calculate the Average +DM, -DM and TR
    // Double PDMa = seriesADX.smma(index, period, Values.PDM);
    // Double NDMa = seriesADX.smma(index, period, Values.NDM);
    // Double TRa = seriesADX.smma(index, period, Values.TR);

    // if (PDMa == null || NDMa == null || TRa == null)
    // return;

    // // Determine the DX
    // Double DX = Math.abs((PDMa - NDMa)) / (PDMa + NDMa) * 100;
    // seriesADX.setDouble(index, Values.DX, DX);

    // Double ADX = seriesADX.smma(index, period, Values.DX);
    // if (ADX == null)
    // return;

    // seriesADX.setDouble(index, Values.ADX, ADX);
    // // return ADX;

    // // debug("Renko Overlay ADX : " + ADX);

    // seriesADX.setComplete(index);

    // }

    // private void calculateRenko(int index, DataContext ctx, Double close) {
    // var settings = getSettings();
    // var series = ctx.getDataSeries();

    // Object input = Enums.BarInput.CLOSE;

    // if (!series.isBarComplete(index)) {
    // return; // On ignore si la barre n'est pas finie
    // }

    // // ticks to points
    // Double renkoPoints = settings.getInteger(RENKO_POINT) / 4;

    // Double prev = series.getDouble(index - 1, input);
    // Double current = series.getDouble(index, input);
    // if (prev == null || current == null)
    // return;

    // crossAbove = false;
    // crossBelow = false;

    // Double cross = null;

    // Double res = null;
    // Double supp = null;

    // Double currentClose = current;

    // if (realRenko == null) {
    // // Initialisation lors de la première exécution
    // // Trouver le prix inférieur se terminant par une dizaine
    // Double lowerBound = Math.floor(currentClose / 10) * 10; // Trouver la dizaine
    // inférieure
    // Double upperBound = lowerBound + renkoPoints; // La dizaine suivante se
    // terminant par 5

    // // Si le prix est plus proche de la dizaine suivante (10, 20, etc.)
    // if (currentClose > lowerBound + renkoPoints) {
    // lowerBound = lowerBound + renkoPoints; // Ajuster pour commencer à 5
    // upperBound = lowerBound + renkoPoints; // Ajuster pour aller jusqu'à la
    // dizaine
    // }

    // realRenko = upperBound;
    // previousClose = lowerBound;
    // brickSize = Math.abs(realRenko - previousClose);
    // if (realRenko > previousClose) {
    // history.updateColorHistory(GREEN);
    // } else {
    // history.updateColorHistory(RED);
    // }
    // } else {
    // if (history.colorHistory[0] == GREEN && currentClose - realRenko >=
    // brickSize) {
    // realRenko = realRenko + Math.floor((currentClose - realRenko) / brickSize) *
    // brickSize;
    // previousClose = realRenko - brickSize;
    // crossAbove = true;
    // cross = 1.0;
    // history.updateColorHistory(GREEN);
    // history.updateCrossHistory(ABOVE);
    // } else if (history.colorHistory[0] == GREEN && realRenko - currentClose >=
    // brickSize * 2) {
    // realRenko = realRenko - Math.floor((realRenko - currentClose) / brickSize) *
    // brickSize;
    // previousClose = realRenko + brickSize;
    // crossBelow = true;
    // cross = -1.0;
    // history.updateColorHistory(RED);
    // history.updateCrossHistory(BELOW);
    // } else if (history.colorHistory[0] == RED && currentClose - realRenko >=
    // brickSize * 2) {
    // realRenko = realRenko + Math.floor((currentClose - realRenko) / brickSize) *
    // brickSize;
    // previousClose = realRenko - brickSize;
    // crossAbove = true;
    // cross = 1.0;
    // history.updateColorHistory(GREEN);
    // history.updateCrossHistory(ABOVE);
    // } else if (history.colorHistory[0] == RED && realRenko - currentClose >=
    // brickSize) {
    // realRenko = realRenko - Math.floor((realRenko - currentClose) / brickSize) *
    // brickSize;
    // previousClose = realRenko + brickSize;
    // crossBelow = true;
    // cross = -1.0;
    // history.updateColorHistory(RED);
    // history.updateCrossHistory(BELOW);
    // }
    // }

    // history.autoUpdateHiLoPriceHistory(hilo1, hilo2);
    // history.autoUpdateHiLoColorHistory(close, history.hiLoPrice1History,
    // history.hiLoPrice2History);

    // boolean complete = series.isBarComplete(index);
    // if (res == null) {
    // res = realRenko;
    // complete = false;
    // }
    // if (supp == null) {
    // supp = previousClose;
    // complete = false;
    // }

    // // Set colors for the Renko bricks
    // Color brickColor = history.colorHistory[0] == GREEN ?
    // settings.getColor(UP_COLOR)
    // : settings.getColor(DOWN_COLOR);

    // // Plot the Renko lines
    // series.setPathColor(index - 1, Values.LOW, brickColor);
    // series.setPathColor(index - 1, Values.HIGH, brickColor);

    // series.setDouble(index - 1, Values.LOW, supp);
    // series.setDouble(index, Values.HIGH, res);

    // series.setDouble(index, Values.CROSS, cross);

    // if (series.isBarComplete(index))
    // series.setComplete(index, complete);

    // if (!history.isArrayAsNull(history.colorHistory) &&
    // !history.isArrayAsNull(history.crossHistory)) {
    // manageDowLinesConditionally(index, ctx, realRenko);
    // }

    // if (!history.isArrayAsNull(history.colorHistory) &&
    // !history.isArrayAsNull(history.hiLoColor1History) &&
    // !history.isArrayAsNull(history.hiLoColor2History) &&
    // !history.isArrayAsNull(history.hiLoPrice1History)
    // && !history.isArrayAsNull(history.hiLoPrice2History)) {
    // strategySignal(index, ctx);
    // }
    // }

    private void manageDowLinesConditionally(int index, DataContext ctx, Double res) {
        if (history.colorHistory[0].equals(GREEN) && history.crossHistory[0].equals(ABOVE)) {
            if (prevLineUp1 == null || history.dowLineUpPriceHistory[0] < res) {
                if (prevLineUp1 != null)
                    removeFigure(prevLineUp1);
                prevLineUp1 = manageDowLines(index, ctx, res, prevLineUp1, DOW_LINE_UP_1);
            }
        } else if (history.colorHistory[0].equals(RED) && history.crossHistory[0].equals(BELOW)) {
            if (prevLineDown1 == null || history.dowLineDownPriceHistory[0] > res) {
                if (prevLineDown1 != null)
                    removeFigure(prevLineDown1);
                prevLineDown1 = manageDowLines(index, ctx, res, prevLineDown1, DOW_LINE_DOWN_1);
            }
        }
    }

    protected Line manageDowLines(int index, DataContext ctx, Double price, Line prevLine, String pathName) {
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

    // private void strategySignal(int index, DataContext ctx) {
    // var settings = getSettings();
    // if (settings == null)
    // return;

    // var series = ctx.getDataSeries();
    // Double priceSupp = series.getDouble(index - 1, Values.LOW);
    // Double priceRes = series.getDouble(index, Values.HIGH);
    // Double priceClose = series.getClose(index);
    // Double priceHigh = series.getHigh(index);
    // Double priceLow = series.getLow(index);
    // if (priceSupp == null || priceRes == null)
    // return;

    // analysis.analyseSignalEntry(priceHigh, priceLow);

    // StrategyCorePattern strategyCore = new StrategyCorePattern(
    // history.colorHistory, history.hiLoColor1History, history.hiLoColor2History,
    // history.hiLoPrice1History, history.hiLoPrice2History, priceRes, priceClose);

    // String popupMsgIndex = "Index : " + index + " => Current : " +
    // format(priceRes);
    // String popupMsgInfo = popupMsgIndex;
    // String popupMsgAnalyse = analysis.printStats();

    // List<PatternCheckResult> results = strategyCore.checkPattern();

    // for (PatternCheckResult result : results) {

    // String popupMsgStrat = "\n Pattern: " + result.patternName
    // + "\n Signal : long=" + result.okLong() + ", short=" + result.okShort()
    // + "\n Direction : high=" + result.direction.high + ", low=" +
    // result.direction.low
    // + "\n Strength : high=" + result.strength.high + ", low=" +
    // result.strength.low
    // + "\n Renko : high=" + result.renko.high + ", low=" + result.renko.low
    // + "\n Hilo : high=" + result.hilo.high + ", low=" + result.hilo.low
    // // + "\n hiLoPrice1History[0] : " + history.hiLoPrice1History[0]
    // // + "\n hiLoPrice1History[1] : " + history.hiLoPrice1History[1]
    // // + "\n hiLoPrice1History[2] : " + history.hiLoPrice1History[2]
    // // + "\n hiLoPrice2History[0] : " + history.hiLoPrice2History[0]
    // // + "\n hiLoPrice2History[1] : " + history.hiLoPrice2History[1]
    // // + "\n hiLoPrice2History[2] : " + history.hiLoPrice2History[2]
    // + "\n ADX : " + series.getDouble(index, Values.ADX);

    // String popupMsgSignal = popupMsgIndex + popupMsgStrat + popupMsgAnalyse;

    // popupMsgInfo += popupMsgStrat;

    // if (crossAbove && result.okLong()) {
    // var c = new Coordinate(series.getStartTime(index), priceRes);
    // var marker = settings.getMarker(UP_MARKER);
    // String msg = popupMsgSignal;
    // if (marker.isEnabled())
    // addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
    // ctx.signal(index, Signals.CROSS_HIGH, msg, priceSupp);

    // analysis.setCurrentStrategy(result.patternName, result.bracket.tp,
    // result.bracket.sl);
    // analysis.analyseSignalEntryInit(series.getEndTime(index),
    // StrategyAnalysis.Direction.HIGH, priceClose);

    // }

    // if (crossBelow && result.okShort()) {
    // var c = new Coordinate(series.getStartTime(index), priceRes);
    // var marker = settings.getMarker(DOWN_MARKER);
    // String msg = popupMsgSignal;
    // if (marker.isEnabled())
    // addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
    // ctx.signal(index, Signals.CROSS_LOW, msg, priceSupp);

    // analysis.setCurrentStrategy(result.patternName, result.bracket.tp,
    // result.bracket.sl);
    // analysis.analyseSignalEntryInit(series.getEndTime(index),
    // StrategyAnalysis.Direction.LOW, priceClose);

    // }
    // }

    // if (settings.getBoolean(RENKO_ENABLED) && settings.getBoolean(INFO_ENABLED))
    // {

    // if (crossAbove) {
    // var c = new Coordinate(series.getStartTime(index), priceRes);
    // var marker = settings.getMarker(INFO_MARKER);
    // String msg = popupMsgInfo;
    // if (marker.isEnabled())
    // addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
    // }

    // if (crossBelow) {
    // var c = new Coordinate(series.getStartTime(index), priceRes);
    // var marker = settings.getMarker(INFO_MARKER);
    // String msg = popupMsgInfo;
    // if (marker.isEnabled())
    // addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
    // }

    // }
    // }

}
