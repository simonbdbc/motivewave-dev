package sbdev.joy_ai_project.strategy_v8o3.statistic;

import com.motivewave.platform.sdk.common.*;
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
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

import sbdev.joy_ai_project.strategy_v8o3.database.StatisticDbManager;
import sbdev.joy_ai_project.strategy_v8o3.util.HistoryManager;
import sbdev.joy_ai_project.strategy_v8o3.util.BarGroupingUtil;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.HiLoResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.RenkoResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.SimulatedBar;
import sbdev.joy_ai_project.strategy_v8o3.util.HiLoManager;
import sbdev.joy_ai_project.strategy_v8o3.util.HiLoManagerStat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@StudyHeader(id = "STATISTIC_ANALYSIS_V1", namespace = "com.sbdev", name = "Statistic Analysis V1", desc = "Statistic Analysis V1", menu = "SB Dev", overlay = true, strategy = true)
public class StatisticAnalysisV1 extends Study {

    private HiLoManagerStat hiloStat1 = new HiLoManagerStat();
    private HiLoManagerStat hiloStat2 = new HiLoManagerStat();

    private final StatisticDbManager dbManager = new StatisticDbManager();

    protected HistoryManager history;

    protected Double brickSize = null; // Taille de la brique
    protected Double previousClose = null; // Renko open
    protected Double realRenko; // Renko close

    final static String GREEN = "green";
    final static String RED = "red";
    final static String ABOVE = "above";
    final static String BELOW = "below";

    protected Double hiLo1 = null;
    protected Double hiLo2 = null;

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
        // sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_5,
        // TICK_INTERVAL_5));
        // sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_6,
        // TICK_INTERVAL_6));

        var desc = createRD();
        desc.setLabelSettings(INPUT, PERIOD, RENKO_POINT);

        desc.exportValue(new ValueDescriptor(Values.HIGH, "High", new String[] { INPUT, PERIOD }));
        desc.exportValue(new ValueDescriptor(Values.LOW, "Low", new String[] { INPUT, PERIOD }));
        desc.exportValue(new ValueDescriptor(Signals.CROSS_HIGH, Enums.ValueType.BOOLEAN, "Cross High"));
        desc.exportValue(new ValueDescriptor(Signals.CROSS_LOW, Enums.ValueType.BOOLEAN, "Cross Low"));

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

    protected SettingTab initializeAnalyseTab(Defaults defaults) {
        var tab = new SettingTab("Analyse");

        // Section 1
        var section1 = tab.addGroup("Interval");
        section1.addRow(new IntegerDescriptor(TICK_INTERVAL_1, "Tick Interval 1", 1, 1, 1000, 1),
                new BooleanDescriptor(TICK_INTERVAL_ENABLED_1, "Use Tick Interval 1", true));
        section1.addRow(new IntegerDescriptor(TICK_INTERVAL_2, "Tick Interval 2", 5, 1, 1000, 1),
                new BooleanDescriptor(TICK_INTERVAL_ENABLED_2, "Use Tick Interval 2", true));
        section1.addRow(new IntegerDescriptor(TICK_INTERVAL_3, "Tick Interval 3", 10, 1, 1000, 1),
                new BooleanDescriptor(TICK_INTERVAL_ENABLED_3, "Use Tick Interval 3", true));
        section1.addRow(new IntegerDescriptor(TICK_INTERVAL_4, "Tick Interval 4", 25, 1, 1000, 1),
                new BooleanDescriptor(TICK_INTERVAL_ENABLED_4, "Use Tick Interval 4", true));
        // section1.addRow(new IntegerDescriptor(TICK_INTERVAL_5, "Tick Interval 5",
        // 100, 1, 1000, 1),
        // new BooleanDescriptor(TICK_INTERVAL_ENABLED_5, "Use Tick Interval 5", true));
        // section1.addRow(new IntegerDescriptor(TICK_INTERVAL_6, "Tick Interval 6",
        // 200, 1, 1000, 1),
        // new BooleanDescriptor(TICK_INTERVAL_ENABLED_6, "Use Tick Interval 6", true));

        // Section 2
        var date = tab.addGroup("Date");
        date.addRow(new IntegerDescriptor(START_DATETIME, "Analysis History (Days ago)", 30, 1, 365, 1));
        date.addRow(new DateTimeDescriptor(END_DATETIME, "Analysis End Date", Instant.now().toEpochMilli(), true));

        return tab;
    }

    @Override
    public void onActivate(OrderContext ctx) {
        debug("StatisticAnalysisV1_onActivate");

        // --- Récupération des paramètres ---
        int daysAgo = getSettings().getInteger(START_DATETIME);
        long endMillis = getSettings().getLong(END_DATETIME);
        Instant endInstant = Instant.ofEpochMilli(endMillis);
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate endAnalyseDay = endInstant.atZone(zoneId).toLocalDate();
        LocalDate startAnalyseDay = endAnalyseDay.minusDays(daysAgo);
        int duration = daysAgo + 1;

        // --- Intervalles actifs ---
        List<Integer> activeIntervals = new ArrayList<>();
        if (getSettings().getBoolean(TICK_INTERVAL_ENABLED_1))
            activeIntervals.add(getSettings().getInteger(TICK_INTERVAL_1));
        if (getSettings().getBoolean(TICK_INTERVAL_ENABLED_2))
            activeIntervals.add(getSettings().getInteger(TICK_INTERVAL_2));
        if (getSettings().getBoolean(TICK_INTERVAL_ENABLED_3))
            activeIntervals.add(getSettings().getInteger(TICK_INTERVAL_3));
        if (getSettings().getBoolean(TICK_INTERVAL_ENABLED_4))
            activeIntervals.add(getSettings().getInteger(TICK_INTERVAL_4));

        BarSize barSize1 = getSettings().getBarSize(TIMEFRAME_1);
        Enums.MAMethod maMethod1 = getSettings().getMAMethod(MA_METHOD_1);
        int period1 = getSettings().getInteger(PERIOD_1);

        BarSize barSize2 = getSettings().getBarSize(TIMEFRAME_2);
        Enums.MAMethod maMethod2 = getSettings().getMAMethod(MA_METHOD_2);
        int period2 = getSettings().getInteger(PERIOD_2);

        int totalsimulatedBars = 0;

        for (int tickInterval : activeIntervals) {
            activeTickInterval = tickInterval;
            realRenko = null;
            history = new HistoryManager();

            Map<LocalDate, List<FormatResult.SimulatedBar>> simulatedBarsByDay = new TreeMap<>();
            Map<LocalDate, Map<Long, HiLoResult>> hiLo1ByDay = new TreeMap<>();
            Map<LocalDate, Map<Long, HiLoResult>> hiLo2ByDay = new TreeMap<>();

            LocalDate today = LocalDate.now(zoneId);
            LocalDateTime now = LocalDateTime.now(zoneId);

            for (int i = 0; i < duration; i++) {
                LocalDate analyseDay = startAnalyseDay.plusDays(i);
                LocalDateTime dayStartDateTime = LocalDateTime.of(analyseDay, LocalTime.MIDNIGHT);
                LocalDateTime dayEndDateTime = analyseDay.equals(today) ? now
                        : LocalDateTime.of(analyseDay, LocalTime.of(23, 59, 59));
                long dayStartMillis = dayStartDateTime.atZone(zoneId).toInstant().toEpochMilli();
                long dayEndMillis = dayEndDateTime.atZone(zoneId).toInstant().toEpochMilli();

                // --- Ticks & SimulatedBars (tickInterval) ---
                List<Tick> dayTicks = getTicksSafe(ctx, dayStartMillis, dayEndMillis);
                if (dayTicks == null || dayTicks.size() < activeTickInterval)
                    continue;
                List<FormatResult.SimulatedBar> simulatedBars = aggregateSimulatedBars(dayTicks, activeTickInterval);
                if (!simulatedBars.isEmpty()) {
                    simulatedBarsByDay.put(analyseDay, simulatedBars);
                    totalsimulatedBars += simulatedBars.size();
                }

                // --- Regroupement 1min & 5min (ou autre) ---
                long intervalMillis1 = barSize1.getInterval() * 60 * 1000L;
                List<FormatResult.SimulatedBar> bars1min = BarGroupingUtil.groupToInterval(simulatedBars,
                        intervalMillis1);
                Map<Long, HiLoResult> hiLo1 = hiloStat1.computeHiLo(bars1min, maMethod1, period1, intervalMillis1);
                if (!hiLo1.isEmpty())
                    hiLo1ByDay.put(analyseDay, hiLo1);

                long intervalMillis2 = barSize2.getInterval() * 60 * 1000L;
                List<FormatResult.SimulatedBar> bars5min = BarGroupingUtil.groupToInterval(simulatedBars,
                        intervalMillis2);
                Map<Long, HiLoResult> hiLo2 = hiloStat2.computeHiLo(bars5min, maMethod2, period2, intervalMillis2);
                if (!hiLo2.isEmpty())
                    hiLo2ByDay.put(analyseDay, hiLo2);
            }

            this.brickSize = getSettings().getInteger(RENKO_POINT) / 4.0;

            // --- Boucle sur chaque journée simulée ---
            for (Map.Entry<LocalDate, List<FormatResult.SimulatedBar>> entry : simulatedBarsByDay.entrySet()) {
                LocalDate analyseDay = entry.getKey();
                List<FormatResult.SimulatedBar> simulatedBars = entry.getValue();
                Map<Long, HiLoResult> HiLoMap1 = hiLo1ByDay.get(analyseDay);
                Map<Long, HiLoResult> HiLoMap2 = hiLo2ByDay.get(analyseDay);
                if (HiLoMap1 == null || HiLoMap2 == null)
                    continue;

                // Listes triées pour getLastHiLo
                List<Long> hilo1Keys = new ArrayList<>(HiLoMap1.keySet());
                List<Long> hilo2Keys = new ArrayList<>(HiLoMap2.keySet());
                Collections.sort(hilo1Keys);
                Collections.sort(hilo2Keys);

                for (FormatResult.SimulatedBar bar : simulatedBars) {
                    HiLoResult resultHiLo1 = HiLoManager.getLastHiLoBefore(HiLoMap1, bar.timestamp);
                    HiLoResult resultHiLo2 = HiLoManager.getLastHiLoBefore(HiLoMap2, bar.timestamp);
                    if (resultHiLo1 == null || resultHiLo2 == null)
                        continue;

                    RenkoResult resultRenko = calculateRenkoTickBased(bar.close, resultHiLo1.hiLo, resultHiLo2.hiLo);

                    if (resultRenko.crossAbove || resultRenko.crossBelow) {
                        if (history.renkoColorHistory[0] != null && history.hiLoColor1History[0] != null
                                && history.hiLoColor2History[0] != null && history.hiLoPrice1History[0] != null
                                && history.hiLoPrice2History[0] != null && history.renkoPriceResHistory[0] != null
                                && history.renkoPriceSupHistory[0] != null) {
                            dbManager.saveRenkoSnapshot(ctx.getInstrument().getSymbol(), "ticks", activeTickInterval,
                                    bar.timestamp, history.renkoPriceResHistory[0], history.renkoPriceSupHistory[0],
                                    history.renkoColorHistory[0], history.hiLoColor1History[0],
                                    history.hiLoColor2History[0], history.hiLoPrice1History[0],
                                    history.hiLoPrice2History[0]);
                        }
                    }
                }
                debug("Date : " + analyseDay + " / Bougies simulées : " + simulatedBars.size());
            }
        }
        debug("Analyse terminée. Total bougies simulées : " + totalsimulatedBars);
        debug("Mise à jour StatisticAnalysisV1 terminée.");
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

}
