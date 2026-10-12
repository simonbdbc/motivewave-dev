// package sbdev.joy_ai_project.strategy.service.analysis;

// import com.motivewave.platform.sdk.common.*;
// import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
// import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
// import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
// import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
// import com.motivewave.platform.sdk.common.desc.LabelDescriptor;
// import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
// import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
// import com.motivewave.platform.sdk.common.desc.PathDescriptor;
// import com.motivewave.platform.sdk.common.desc.SettingTab;
// import com.motivewave.platform.sdk.common.desc.SettingsDescriptor;
// import com.motivewave.platform.sdk.common.desc.StringDescriptor;
// import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
// import com.motivewave.platform.sdk.common.desc.DateTimeDescriptor;
// import com.motivewave.platform.sdk.common.desc.EnabledDependency;
// import com.motivewave.platform.sdk.order_mgmt.OrderContext;
// import com.motivewave.platform.sdk.study.Study;
// import com.motivewave.platform.sdk.study.StudyHeader;

// import sbdev.joy_ai_project.strategy.util.Helper;
// import sbdev.joy_ai_project.strategy.util.HistoryManager;
// import sbdev.joy_ai_project.strategy.util.InterfaceCorePattern;
// import sbdev.joy_ai_project.strategy.util.FormatResult.HiLoResult;
// import sbdev.joy_ai_project.strategy.util.FormatResult.PatternCheckResult;
// import sbdev.joy_ai_project.strategy.util.FormatResult.PatternGroupKey;
// import sbdev.joy_ai_project.strategy.util.FormatResult.RenkoResult;
// import sbdev.joy_ai_project.strategy.util.FormatResult.SimulatedBar;

// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.TreeMap;
// import java.time.Instant;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.LocalTime;
// import java.time.ZoneId;

// @StudyHeader(id = "SIGNAL_ANALYSIS_V1", namespace = "com.sbdev", name = "Signal Analysis V1", desc = "Signal Analysis V1", menu = "SB Dev", overlay = true, strategy = true)
// public class SignalAnalysisV1 extends Study {

//     protected HistoryManager history;
//     protected CoreAnalysis analysis;

//     protected Double brickSize = null; // Taille de la brique
//     protected Double previousClose = null; // Renko open
//     protected Double realRenko; // Renko close

//     final static String GREEN = "green";
//     final static String RED = "red";
//     final static String ABOVE = "above";
//     final static String BELOW = "below";

//     protected Double hiLo1 = null;
//     protected Double hiLo2 = null;

//     final static String RENKO_ENABLED = "renkoEnabled";

//     final static String INPUT = "input";
//     final static String PERIOD = "period";
//     final static String RENKO_POINT = "renkoPoint";
//     final static String UP_MARKER = "upMarker";
//     final static String DOWN_MARKER = "downMarker";
//     final static String TOP_PATH = "topPath";
//     final static String BOTTOM_PATH = "bottomPath";
//     final static String TOP_IND = "topInd";
//     final static String BOTTOM_IND = "bottomInd";
//     final static String UP_COLOR = "upColor";
//     final static String DOWN_COLOR = "downColor";

//     final static String INFO_ENABLED = "infoEnabled";
//     final static String INFO_MARKER = "infoMarker";

//     enum Values {
//         HIGH, LOW, CROSS, MA_HILO_1, MA_HILO_2, ADX, DX, PDM, NDM, TR,
//     }

//     enum Signals {
//         CROSS_HIGH, CROSS_LOW, CROSS_ABOVE, CROSS_BELOW
//     }

//     // Define custom input keys
//     public static final String TF_ENABLED_1 = "tfEnabled1";
//     public static final String TIMEFRAME_1 = "timeframe1";
//     public static final String MA_METHOD_1 = "maMethod1";
//     public static final String PATH_HILO_1 = "pathHilo1";
//     public static final String PERIOD_1 = "period1";

//     public static final String TF_ENABLED_2 = "tfEnabled2";
//     public static final String TIMEFRAME_2 = "timeframe2";
//     public static final String MA_METHOD_2 = "maMethod2";
//     public static final String PATH_HILO_2 = "pathHilo2";
//     public static final String PERIOD_2 = "period2";

//     final static String HIGH_COLOR_1 = "highColor1";
//     final static String LOW_COLOR_1 = "lowColor1";
//     final static String HIGH_COLOR_2 = "highColor2";
//     final static String LOW_COLOR_2 = "lowColor2";

//     final static String DOW_LINE_UP_1 = "dowLineUp1";
//     final static String DOW_LINE_DOWN_1 = "dowLineDown1";

//     int highsOffset = 3;
//     int lowsOffset = 3;

//     final static String START_DATETIME = "startDateTime";
//     final static String END_DATETIME = "endDateTime";

//     // public static final String TICK_INTERVAL_1 = "tickInteval1";
//     // public static final String TICK_INTERVAL_ENABLED_1 = "tickIntevalEnabled1";
//     // public static final String TICK_INTERVAL_2 = "tickInteval2";
//     // public static final String TICK_INTERVAL_ENABLED_2 = "tickIntevalEnabled2";
//     // public static final String TICK_INTERVAL_3 = "tickInteval3";
//     // public static final String TICK_INTERVAL_ENABLED_3 = "tickIntevalEnabled3";
//     // public static final String TICK_INTERVAL_4 = "tickInteval4";
//     // public static final String TICK_INTERVAL_ENABLED_4 = "tickIntevalEnabled4";
//     // public static final String TICK_INTERVAL_5 = "tickInteval5";
//     // public static final String TICK_INTERVAL_ENABLED_5 = "tickIntevalEnabled5";
//     // public static final String TICK_INTERVAL_6 = "tickInteval6";
//     // public static final String TICK_INTERVAL_ENABLED_6 = "tickIntevalEnabled6";

//     private List<Integer> listActiveTickIntervals = new ArrayList<>();
//     private Integer activeTickInterval = null;

//     // private List<PatternGroupKey> patternGroups = Helper.loadPatternGroupsFromSingleton();

//     @Override
//     public void initialize(Defaults defaults) {

//         SettingsDescriptor sd = createSD();

//         sd.addTab(initializeAnalyseTab(sd, defaults));
//         sd.addTab(initializeGeneralTab(defaults));

//         // Quick Settings (Tool Bar and Popup Editor)
//         sd.addQuickSettings(RENKO_POINT);
//         sd.addQuickSettings(TOP_PATH, BOTTOM_PATH);

//         // sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_1,
//         // TICK_INTERVAL_1));
//         // sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_2,
//         // TICK_INTERVAL_2));
//         // sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_3,
//         // TICK_INTERVAL_3));
//         // sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_4,
//         // TICK_INTERVAL_4));
//         // sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_5,
//         // TICK_INTERVAL_5));
//         // sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_6,
//         // TICK_INTERVAL_6));

//         var desc = createRD();
//         desc.setLabelSettings(INPUT, PERIOD, RENKO_POINT);

//         desc.exportValue(new ValueDescriptor(Values.HIGH, "High", new String[] { INPUT, PERIOD }));
//         desc.exportValue(new ValueDescriptor(Values.LOW, "Low", new String[] { INPUT, PERIOD }));
//         desc.exportValue(new ValueDescriptor(Signals.CROSS_HIGH, Enums.ValueType.BOOLEAN, "Cross High"));
//         desc.exportValue(new ValueDescriptor(Signals.CROSS_LOW, Enums.ValueType.BOOLEAN, "Cross Low"));

//         desc.declarePath(Values.HIGH, TOP_PATH);
//         desc.declarePath(Values.LOW, BOTTOM_PATH);

//         desc.declareIndicator(Values.HIGH, TOP_IND);
//         desc.declareIndicator(Values.LOW, BOTTOM_IND);

//         // Signals
//         desc.declareSignal(Signals.CROSS_HIGH, "Cross High");
//         desc.declareSignal(Signals.CROSS_LOW, "Cross Low");

//         desc.setRangeKeys(Values.HIGH, Values.LOW);

//         var tab1 = sd.addTab(get("HiLo 1"));

//         var grp1 = tab1.addGroup(get("First Timeframe"));
//         // Add timeframe selection options
//         grp1.addRow(new BooleanDescriptor(TF_ENABLED_1, "Use Timeframe 1", true));
//         grp1.addRow(new BarSizeDescriptor(TIMEFRAME_1, "Timeframe 1", BarSize.getBarSize(1)));
//         grp1.addRow(new MAMethodDescriptor(MA_METHOD_1, "MA Method 1", Enums.MAMethod.SMA));
//         grp1.addRow(new IntegerDescriptor(PERIOD_1, "Period 1", 21, 1, 9999, 1));

//         grp1 = tab1.addGroup(get("Display"));
//         // Allow the user to change the settings for the paths
//         grp1.addRow(new PathDescriptor(PATH_HILO_1, "Hilo Path 1", null, 1.0f, null, true, true, false));

//         grp1.addRow(new ColorDescriptor(HIGH_COLOR_1, "High Color 1", defaults.getRedLine()));
//         grp1.addRow(new ColorDescriptor(LOW_COLOR_1, "Low Color 1", defaults.getGreenLine()));

//         var tab2 = sd.addTab(get("HiLo 2"));

//         var grp2 = tab2.addGroup(get("Second Timeframe"));
//         // Add timeframe selection options
//         grp2.addRow(new BooleanDescriptor(TF_ENABLED_2, "Use Timeframe 2", true));
//         grp2.addRow(new BarSizeDescriptor(TIMEFRAME_2, "Timeframe 2", BarSize.getBarSize(5)));
//         grp2.addRow(new MAMethodDescriptor(MA_METHOD_2, "MA Method 2", Enums.MAMethod.SMA));
//         grp2.addRow(new IntegerDescriptor(PERIOD_2, "Period 2", 21, 1, 9999, 1));

//         grp2 = tab2.addGroup(get("Display"));
//         // Allow the user to change the settings for the paths
//         grp2.addRow(new PathDescriptor(PATH_HILO_2, "Hilo Path 2", null, 1.0f, null, true, true, false));

//         grp2.addRow(new ColorDescriptor(HIGH_COLOR_2, "High Color 2", defaults.getRedLine()));
//         grp2.addRow(new ColorDescriptor(LOW_COLOR_2, "Low Color 2", defaults.getGreenLine()));

//         var tab3 = sd.addTab(get("Dow Lines"));
//         var colors = tab3.addGroup(get("COLORS"));
//         colors.addRow(new PathDescriptor(DOW_LINE_UP_1, "Dow Line Up 1", defaults.getGreen(), 1.0f, null, true, false,
//                 false));
//         colors.addRow(new PathDescriptor(DOW_LINE_DOWN_1, "Dow Line Down 1", defaults.getRed(), 1.0f, null, true, false,
//                 false));

//         // Declare paths for drawing
//         desc.declarePath(Values.MA_HILO_1, PATH_HILO_1);
//         desc.declarePath(Values.MA_HILO_2, PATH_HILO_2);

//         // Signals
//         desc.declareSignal(Signals.CROSS_ABOVE, "PRICE_CROSS_ABOVE");
//         desc.declareSignal(Signals.CROSS_BELOW, "PRICE_CROSS_BELOW");

//     }

//     protected SettingTab initializeGeneralTab(Defaults defaults) {
//         var tab = new SettingTab("Renko");

//         // Section 1
//         var section1 = tab.addGroup("Inputs");
//         section1.addRow(new BooleanDescriptor(RENKO_ENABLED, "Use Renko", true));
//         section1.addRow(new IntegerDescriptor(RENKO_POINT, "Renko Ticks", 20, 1, 99, 1));
//         section1.addRow(new StringDescriptor("note", "Note", "NQ : 4 ticks = 1 point"));

//         // Section 2
//         var lines = tab.addGroup("Display");
//         lines.addRow(new PathDescriptor(TOP_PATH, "High Path", null, 1.0f, null, true, true, false));
//         lines.addRow(new PathDescriptor(BOTTOM_PATH, "Bottom Path", null, 1.0f, null, true, true, false));

//         lines.addRow(new ColorDescriptor(UP_COLOR, "Up Color", defaults.getGreenLine()));
//         lines.addRow(new ColorDescriptor(DOWN_COLOR, "Down Color", defaults.getRedLine()));

//         // Section 3
//         var markers = tab.addGroup("Markers");
//         markers.addRow(new MarkerDescriptor(UP_MARKER, "Up Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL,
//                 defaults.getGreen(), defaults.getLineColor(), true, true));
//         markers.addRow(new MarkerDescriptor(DOWN_MARKER, "Down Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL,
//                 defaults.getRed(), defaults.getLineColor(), true, true));
//         markers.addRow(
//                 new MarkerDescriptor(INFO_MARKER, "Info Marker", Enums.MarkerType.DIAMOND, Enums.Size.LARGE,
//                         defaults.getYellow(), defaults.getLineColor(), true, false),
//                 new BooleanDescriptor(INFO_ENABLED, "Use Info Marker", false));
//         return tab;
//     }

//     protected SettingTab initializeAnalyseTab(SettingsDescriptor sd, Defaults defaults) {

//         var tab = new SettingTab("Analysis");
//         var section1 = tab.addGroup("Interval");

//         // for (int i = 0; i < patternGroups.size(); i++) {
//         //     PatternGroupKey group = patternGroups.get(i);

//         //     String intervalKey = "TICK_INTERVAL_" + (i + 1);
//         //     String enabledKey = "TICK_INTERVAL_ENABLED_" + (i + 1);

//         //     section1.addRow(new LabelDescriptor("Tick Interval " + (i + 1) + " : " + group.intervalValue),
//         //             new LabelDescriptor(" --- TP/SL : " + group.tp + "/" + group.sl + " --- "),
//         //             new BooleanDescriptor(enabledKey, "Use Tick Interval " + (i + 1), true));

//         //     sd.addDependency(new EnabledDependency(enabledKey, intervalKey));

//         // }

//         // // Section 1
//         // var section1 = tab.addGroup("Interval");
//         // section1.addRow(new IntegerDescriptor(TICK_INTERVAL_1, "Tick Interval 1", 1,
//         // 1, 1000, 1),
//         // new BooleanDescriptor(TICK_INTERVAL_ENABLED_1, "Use Tick Interval 1", true));
//         // section1.addRow(new IntegerDescriptor(TICK_INTERVAL_2, "Tick Interval 2", 10,
//         // 1, 1000, 1),
//         // new BooleanDescriptor(TICK_INTERVAL_ENABLED_2, "Use Tick Interval 2", true));
//         // section1.addRow(new IntegerDescriptor(TICK_INTERVAL_3, "Tick Interval 3", 25,
//         // 1, 1000, 1),
//         // new BooleanDescriptor(TICK_INTERVAL_ENABLED_3, "Use Tick Interval 3", true));
//         // section1.addRow(new IntegerDescriptor(TICK_INTERVAL_4, "Tick Interval 4", 50,
//         // 1, 1000, 1),
//         // new BooleanDescriptor(TICK_INTERVAL_ENABLED_4, "Use Tick Interval 4", true));
//         // section1.addRow(new IntegerDescriptor(TICK_INTERVAL_5, "Tick Interval 5",
//         // 100, 1, 1000, 1),
//         // new BooleanDescriptor(TICK_INTERVAL_ENABLED_5, "Use Tick Interval 5", true));
//         // section1.addRow(new IntegerDescriptor(TICK_INTERVAL_6, "Tick Interval 6",
//         // 200, 1, 1000, 1),
//         // new BooleanDescriptor(TICK_INTERVAL_ENABLED_6, "Use Tick Interval 6", true));

//         // Section 2
//         var date = tab.addGroup("Date");
//         date.addRow(new IntegerDescriptor(START_DATETIME, "Analysis History (Days ago)", 30, 1, 365, 1));
//         date.addRow(new DateTimeDescriptor(END_DATETIME, "Analysis End Date", Instant.now().toEpochMilli(), true));

//         return tab;
//     }

//     // Mise à jour de onActivate pour utiliser les bougies simulées
//     @Override
//     public void onActivate(OrderContext ctx) {
//         debug("StrategyAnalysisV8_onActivate");

//         // for (int i = 0; i < patternGroups.size(); i++) {
//         //     PatternGroupKey group = patternGroups.get(i);

//         //     String enabledKey = "TICK_INTERVAL_ENABLED_" + (i + 1);
//         //     boolean enabled = getSettings().getBoolean(enabledKey);

//         //     if (enabled) {
//         //         if (!listActiveTickIntervals.contains(group.intervalValue)) {
//         //             listActiveTickIntervals.add(group.intervalValue);
//         //         }
//         //     } else {
//         //         if (listActiveTickIntervals.contains(group.intervalValue)) {
//         //             listActiveTickIntervals.remove(Integer.valueOf(group.intervalValue));
//         //         }
//         //     }
//         // }

//         // debug("patternGroups : " + patternGroups);

//         debug("listActiveTickIntervals : " + listActiveTickIntervals);

//         // boolean tfEnabled1 = getSettings().getBoolean(TF_ENABLED_1);
//         BarSize barSize1 = getSettings().getBarSize(TIMEFRAME_1);
//         Enums.MAMethod maMethod1 = getSettings().getMAMethod(MA_METHOD_1);
//         int period1 = getSettings().getInteger(PERIOD_1);

//         // boolean tfEnabled2 = getSettings().getBoolean(TF_ENABLED_2);
//         BarSize barSize2 = getSettings().getBarSize(TIMEFRAME_2);
//         Enums.MAMethod maMethod2 = getSettings().getMAMethod(MA_METHOD_2);
//         int period2 = getSettings().getInteger(PERIOD_2);

//         ZoneId zoneId = ZoneId.systemDefault();

//         int daysAgo = getSettings().getInteger(START_DATETIME);
//         long endMillis = getSettings().getLong(END_DATETIME);

//         Instant endInstant = Instant.ofEpochMilli(endMillis);
//         LocalDate endAnalyseDay = endInstant.atZone(zoneId).toLocalDate();
//         LocalDate startAnalyseDay = endAnalyseDay.minusDays(daysAgo);
//         int duration = daysAgo + 1; // Inclure le jour de fin

//         int totalsimulatedBars = 0;

//         for (int tickInterval : listActiveTickIntervals) {
//             // debug("Tick interval actif : " + tickInterval);

//             activeTickInterval = tickInterval;

//             realRenko = null;
//             history = new HistoryManager();
//             analysis = new CoreAnalysis(ctx.getInstrument().getSymbol(), ctx.getInstrument().getTickSize(), "ticks",
//                     activeTickInterval);

//             Map<LocalDate, List<SimulatedBar>> simulatedBarsByDay = new TreeMap<>();
//             Map<LocalDate, Map<Long, HiLoResult>> hiLo1ByDay = new TreeMap<>();
//             Map<LocalDate, Map<Long, HiLoResult>> hiLo2ByDay = new TreeMap<>();

//             LocalDate today = LocalDate.now(zoneId);
//             LocalDateTime now = LocalDateTime.now(zoneId);

//             for (int i = 0; i < duration; i++) {
//                 LocalDate analyseDay = startAnalyseDay.plusDays(i);
//                 // debug("analyseDay : " + analyseDay);

//                 LocalDateTime dayStartDateTime = LocalDateTime.of(analyseDay, LocalTime.MIDNIGHT);
//                 LocalDateTime dayEndDateTime = analyseDay.equals(today) ? now
//                         : LocalDateTime.of(analyseDay, LocalTime.of(23, 59, 59));

//                 long dayStartMillis = dayStartDateTime.atZone(zoneId).toInstant().toEpochMilli();
//                 long dayEndMillis = dayEndDateTime.atZone(zoneId).toInstant().toEpochMilli();

//                 // debug("Requesting ticks from " + dayStartDateTime + " to " + dayEndDateTime);

//                 List<Tick> dayTicks = getTicksSafe(ctx, dayStartMillis, dayEndMillis);
//                 if (dayTicks == null || dayTicks.size() < activeTickInterval) {
//                     debug("continue dayTicks : " + (dayTicks == null ? 0 : dayTicks.size()));
//                     continue;
//                 }

//                 List<SimulatedBar> simulatedBars = aggregateSimulatedBars(dayTicks, activeTickInterval);
//                 if (!simulatedBars.isEmpty()) {
//                     simulatedBarsByDay.put(analyseDay, simulatedBars);
//                     totalsimulatedBars += simulatedBars.size();
//                 }

//                 List<Bar> dayBars1 = getBarsSafe(ctx, dayStartMillis, dayEndMillis, barSize1);
//                 if (dayBars1 == null || dayBars1.size() < period1) {
//                     debug("continue dayBars1 : " + (dayBars1 == null ? 0 : dayBars1.size()));
//                     continue;
//                 }
//                 Map<Long, HiLoResult> hiLo1 = buildHilo(dayBars1, maMethod1, period1);
//                 if (!hiLo1.isEmpty())
//                     hiLo1ByDay.put(analyseDay, hiLo1);

//                 List<Bar> dayBars2 = getBarsSafe(ctx, dayStartMillis, dayEndMillis, barSize2);
//                 if (dayBars2 == null || dayBars2.size() < period2) {
//                     debug("continue dayBars2 : " + (dayBars2 == null ? 0 : dayBars2.size()));
//                     continue;
//                 }
//                 Map<Long, HiLoResult> hiLo2 = buildHilo(dayBars2, maMethod2, period2);
//                 if (!hiLo2.isEmpty())
//                     hiLo2ByDay.put(analyseDay, hiLo2);
//             }

//             this.brickSize = getSettings().getInteger(RENKO_POINT) / 4.0;

//             for (Map.Entry<LocalDate, List<SimulatedBar>> entry : simulatedBarsByDay.entrySet()) {
//                 LocalDate analyseDay = entry.getKey();
//                 List<SimulatedBar> simulatedBars = entry.getValue();

//                 for (SimulatedBar bar : simulatedBars) {
//                     Map<Long, HiLoResult> HiLoMap1 = hiLo1ByDay.get(analyseDay);
//                     Map<Long, HiLoResult> HiLoMap2 = hiLo2ByDay.get(analyseDay);
//                     // debug("HiLoMap1 : " + HiLoMap1);
//                     // debug("HiLoMap2 : " + HiLoMap2);
//                     if (HiLoMap1 == null || HiLoMap2 == null) {
//                         continue;
//                     }

//                     long oneMinutesInMillis = 1 * 60 * 1000;
//                     long rounded1MinTimestamp = (bar.timestamp / oneMinutesInMillis) * oneMinutesInMillis;
//                     HiLoResult resultHiLo1 = HiLoMap1.get(rounded1MinTimestamp);

//                     long fiveMinutesInMillis = 5 * 60 * 1000;
//                     long rounded5MinTimestamp = (bar.timestamp / fiveMinutesInMillis) * fiveMinutesInMillis;
//                     HiLoResult resultHiLo2 = HiLoMap2.get(rounded5MinTimestamp);

//                     // debug("resultHiLo1 : " + resultHiLo1);
//                     // debug("resultHiLo2 : " + resultHiLo2);
//                     if (resultHiLo1 == null || resultHiLo2 == null) {
//                         continue;
//                     }

//                     RenkoResult resultRenko = calculateRenkoTickBased(bar.close, resultHiLo1.hiLo, resultHiLo2.hiLo);

//                     if (resultRenko.crossAbove || resultRenko.crossBelow) {
//                         // debug("history.renkoColorHistory[0] : " + history.renkoColorHistory[0]);
//                         // debug("history.hiLoColor1History[0] : " + history.hiLoColor1History[0]);
//                         // debug("history.hiLoColor2History[0] : " + history.hiLoColor2History[0]);
//                         // debug("history.hiLoPrice1History[0] : " + history.hiLoPrice1History[0]);
//                         // debug("history.hiLoPrice2History[0] : " + history.hiLoPrice2History[0]);
//                         // debug("history.renkoPriceResHistory[0] : " +
//                         // history.renkoPriceResHistory[0]);
//                         // debug("history.renkoPriceSupHistory[0] : " +
//                         // history.renkoPriceSupHistory[0]);
//                         if (!history.isArrayAsNull(history.renkoColorHistory)
//                                 && !history.isArrayAsNull(history.hiLoColor1History)
//                                 && !history.isArrayAsNull(history.hiLoColor2History)
//                                 && !history.isArrayAsNull(history.hiLoPrice1History)
//                                 && !history.isArrayAsNull(history.hiLoPrice2History)
//                                 && !history.isArrayAsNull(history.renkoPriceResHistory)
//                                 && !history.isArrayAsNull(history.renkoPriceSupHistory)) {
//                             // debug("resultRenko : " + resultRenko);

//                             strategySignalOffContext(bar, resultRenko, ctx);
//                         }
//                     }
//                 }
//                 debug("Date : " + analyseDay + " / Bougies simulées : " + simulatedBars.size());
//             }
//         }

//         debug("Analyse terminée. Total bougies simulées : " + totalsimulatedBars);
//         debug("Mise à jour StrategyAnalysisV8 terminée.");
//     }

//     // --- Méthode utilitaire de regroupement des ticks ---
//     public List<SimulatedBar> aggregateSimulatedBars(List<Tick> ticks, int groupSize) {
//         List<SimulatedBar> bars = new ArrayList<>();

//         for (int i = 0; i <= ticks.size() - groupSize; i += groupSize) {
//             Double open = (double) ticks.get(i).getPrice();
//             Double high = open;
//             Double low = open;
//             Double close = (double) ticks.get(i + groupSize - 1).getPrice();
//             long timestamp = ticks.get(i + groupSize - 1).getTime();

//             for (int j = i; j < i + groupSize; j++) {
//                 Double price = (double) ticks.get(j).getPrice();
//                 if (price > high)
//                     high = price;
//                 if (price < low)
//                     low = price;
//             }

//             // debug("timestamp : " + timestamp + " open : " + open + " high : " + high + "
//             // low : " + low + " close : "
//             // + close);

//             bars.add(new SimulatedBar(timestamp, open, high, low, close));
//         }

//         return bars;
//     }

//     private List<Bar> getBarsSafe(OrderContext ctx, long startTime, long endTime, BarSize barSize) {
//         try {
//             return ctx.getDataContext().getInstrument().getBars(startTime, endTime, barSize, false);
//         } catch (Exception e) {
//             debug("Erreur chargement bars: " + e.getMessage());
//             return null;
//         }
//     }

//     private List<Tick> getTicksSafe(OrderContext ctx, long startTime, long endTime) {
//         try {
//             return ctx.getDataContext().getInstrument().getTicks(startTime, endTime);
//         } catch (Exception e) {
//             debug("Erreur chargement ticks: " + e.getMessage());
//             return null;
//         }
//     }

//     public RenkoResult calculateRenkoTickBased(Double currPrice, Double hiLo1, Double hiLo2) {
//         boolean crossAbove = false;
//         boolean crossBelow = false;

//         if (realRenko == null) {
//             Double lowerBound = Math.floor(currPrice / 10) * 10;
//             Double upperBound = lowerBound + brickSize;

//             if (currPrice > lowerBound + brickSize) {
//                 lowerBound += brickSize;
//                 upperBound = lowerBound + brickSize;
//             }

//             realRenko = upperBound;
//             previousClose = lowerBound;
//             brickSize = Math.abs(realRenko - previousClose);

//             if (realRenko > previousClose) {
//                 history.updateRenkoColorHistory(GREEN);
//             } else {
//                 history.updateRenkoColorHistory(RED);
//             }
//         } else {
//             if (history.renkoColorHistory[0].equals(GREEN) && currPrice - realRenko >= brickSize) {
//                 realRenko += Math.floor((currPrice - realRenko) / brickSize) * brickSize;
//                 previousClose = realRenko - brickSize;
//                 crossAbove = true;
//                 history.updateCrossHistory(ABOVE);
//                 history.updateRenkoColorHistory(GREEN);
//                 history.autoUpdateRenkoPriceHistory(realRenko, previousClose);
//                 history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
//                 history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History, history.hiLoPrice2History);
//             } else if (history.renkoColorHistory[0].equals(GREEN) && realRenko - currPrice >= 2 * brickSize) {
//                 realRenko -= Math.floor((realRenko - currPrice) / brickSize) * brickSize;
//                 previousClose = realRenko + brickSize;
//                 crossBelow = true;
//                 history.updateCrossHistory(BELOW);
//                 history.updateRenkoColorHistory(RED);
//                 history.autoUpdateRenkoPriceHistory(realRenko, previousClose);
//                 history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
//                 history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History, history.hiLoPrice2History);
//             } else if (history.renkoColorHistory[0].equals(RED) && currPrice - realRenko >= 2 * brickSize) {
//                 realRenko += Math.floor((currPrice - realRenko) / brickSize) * brickSize;
//                 previousClose = realRenko - brickSize;
//                 crossAbove = true;
//                 history.updateCrossHistory(ABOVE);
//                 history.updateRenkoColorHistory(GREEN);
//                 history.autoUpdateRenkoPriceHistory(realRenko, previousClose);
//                 history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
//                 history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History, history.hiLoPrice2History);
//             } else if (history.renkoColorHistory[0].equals(RED) && realRenko - currPrice >= brickSize) {
//                 realRenko -= Math.floor((realRenko - currPrice) / brickSize) * brickSize;
//                 previousClose = realRenko + brickSize;
//                 crossBelow = true;
//                 history.updateCrossHistory(BELOW);
//                 history.updateRenkoColorHistory(RED);
//                 history.autoUpdateRenkoPriceHistory(realRenko, previousClose);
//                 history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
//                 history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History, history.hiLoPrice2History);
//             }
//         }

//         return new RenkoResult(crossAbove, crossBelow, realRenko, previousClose);
//     }

//     private Map<Long, HiLoResult> buildHilo(List<Bar> dayBars, Enums.MAMethod maMethod, int period) {
//         Map<Long, HiLoResult> hiLoResult = new HashMap<>();

//         // Extraire high et low en Double
//         List<Double> highs = new ArrayList<>();
//         List<Double> lows = new ArrayList<>();

//         for (Bar bar : dayBars) {
//             highs.add((double) bar.getHigh());
//             lows.add((double) bar.getLow());
//         }

//         // debug("highs : " + highs.size());
//         // debug("lows : " + lows.size());

//         List<Double> maHighs = calculateMA(highs, maMethod, period);
//         List<Double> maLows = calculateMA(lows, maMethod, period);

//         // debug("maHighs : " + maHighs.size());
//         // debug("maLows : " + maLows.size());

//         Double hiLo = null;

//         for (int i = period - 1; i < dayBars.size(); i++) {
//             // for (int i = 0; i < dayBars.size(); i++) {
//             Bar current = dayBars.get(i);
//             Double close = (double) current.getClose();
//             Double high = maHighs.get(i);
//             Double low = maLows.get(i);
//             Long timestamp = current.getEndTime();

//             // debug("timestamp : " + timestamp);
//             // debug("current : " + current);
//             // debug("close : " + close);
//             // debug("high : " + high);
//             // debug("low : " + low);

//             if (hiLo == null) {
//                 hiLo = close;
//             }

//             if (high != null && low != null) {

//                 hiLo = close > hiLo ? low : close < hiLo ? high : close;
//                 Boolean isHigh = close > hiLo;
//                 Boolean isLow = close < hiLo;

//                 // debug("timestamp : " + timestamp);
//                 // debug("hiLo : " + hiLo);

//                 hiLoResult.put(timestamp, new HiLoResult(timestamp, hiLo, close, isHigh, isLow));
//             } else {
//                 hiLoResult.put(timestamp, new HiLoResult(timestamp, hiLo, close, false, false));
//             }

//         }
//         return hiLoResult;
//     }

//     private List<Double> calculateMA(List<Double> values, Enums.MAMethod method, int period) {
//         List<Double> result = new ArrayList<>(Collections.nCopies(values.size(), null));

//         switch (method) {
//         case SMA:
//             for (int i = period - 1; i < values.size(); i++) {
//                 Double sum = 0.0;
//                 for (int j = i - period + 1; j <= i; j++) {
//                     sum += values.get(j);
//                 }
//                 result.set(i, sum / period);
//             }
//             break;

//         case EMA:
//             Double multiplier = 2.0 / (period + 1);
//             Double emaPrev = null;
//             for (int i = 0; i < values.size(); i++) {
//                 Double val = values.get(i);
//                 if (val == null)
//                     continue;

//                 if (i == period - 1) {
//                     Double sma = 0.0;
//                     for (int j = i - period + 1; j <= i; j++) {
//                         sma += values.get(j);
//                     }
//                     emaPrev = sma / period;
//                     result.set(i, emaPrev);
//                 } else if (i >= period) {
//                     emaPrev = (val - emaPrev) * multiplier + emaPrev;
//                     result.set(i, emaPrev);
//                 }
//             }
//             break;

//         case WMA: // Weighted Moving Average
//             int weightSum = period * (period + 1) / 2;
//             for (int i = period - 1; i < values.size(); i++) {
//                 Double wma = 0.0;
//                 for (int j = 0; j < period; j++) {
//                     wma += values.get(i - j) * (period - j);
//                 }
//                 result.set(i, wma / weightSum);
//             }
//             break;

//         case SMMA: // Smoothed MA (souvent utilisée pour RSI)
//             Double smmaPrev = null;
//             for (int i = 0; i < values.size(); i++) {
//                 Double val = values.get(i);
//                 if (val == null)
//                     continue;

//                 if (i == period - 1) {
//                     Double sum = 0.0;
//                     for (int j = i - period + 1; j <= i; j++) {
//                         sum += values.get(j);
//                     }
//                     smmaPrev = sum / period;
//                 } else if (i >= period) {
//                     smmaPrev = (smmaPrev * (period - 1) + val) / period;
//                 }
//                 if (i >= period - 1)
//                     result.set(i, smmaPrev);
//             }
//             break;

//         default:
//             throw new UnsupportedOperationException("Méthode MA non supportée : " + method);
//         }

//         return result;
//     }

//     private void strategySignalOffContext(SimulatedBar bar, RenkoResult resultRenko, OrderContext ctx) {
//         var settings = getSettings();
//         if (settings == null)
//             return;

//         debug("strategySignalOffContext");
//         // debug("activeTickInterval : " + activeTickInterval);

//         Double priceSup = resultRenko.previousClose;
//         Double priceRes = resultRenko.realRenko;
//         // debug("priceSup : " + priceSup);
//         // debug("priceRes : " + priceRes);

//         Double priceClose = bar.close;
//         Double priceHigh = bar.high;
//         Double priceLow = bar.low;

//         if (priceSup == null || priceRes == null)
//             return;

//         analysis.analyseSignalEntry(priceHigh, priceLow);

//         // System.out.println("patternGroups : " + patternGroups);
//         // List<PatternGroupKey> groupFiltered = new ArrayList<>(patternGroups);

//         // debug("activeTickInterval : " + activeTickInterval);
//         // groupFiltered.removeIf(g -> g.intervalValue != activeTickInterval);

//         // debug("groupFiltered : " + groupFiltered);
//         // // for (PatternGroupKey group : patternGroups) {
//         // for (PatternGroupKey group : groupFiltered) {
//         //     String className = String.format("sbdev.joy_ai_project.strategy.core_pattern.CorePattern_%d_%d_%d",
//         //             group.intervalValue, group.tp, group.sl);

//         //     try {
//         //         // On instancie la classe dynamiquement
//         //         InterfaceCorePattern strategyCore = (InterfaceCorePattern) Class.forName(className)
//         //                 .getConstructor(String[].class, String[].class, String[].class, Double[].class, Double[].class,
//         //                         Double.class, Double.class)
//         //                 .newInstance(history.renkoColorHistory, history.hiLoColor1History, history.hiLoColor2History,
//         //                         history.hiLoPrice1History, history.hiLoPrice2History, priceRes, priceClose);

//         //         // Utilisation simple et typée de la méthode :
//         //         List<PatternCheckResult> results = strategyCore.checkPattern();
//         //         // System.out.println("Résultats pour " + className + " : " + results.size() + "
//         //         // patterns trouvés.");
//         //         debug("Résultats pour " + className + " : " + results.size() + " patterns trouvés.");

//         //         // CorePattern strategyCore = new CorePattern(history.renkoColorHistory,
//         //         // history.hiLoColor1History,
//         //         // history.hiLoColor2History, history.hiLoPrice1History,
//         //         // history.hiLoPrice2History, priceRes,
//         //         // priceClose);

//         //         // List<PatternCheckResult> results = strategyCore.checkPattern();

//         //         for (PatternCheckResult result : results) {

//         //             debug("result.interval : " + result.interval + " activeTickInterval : " + activeTickInterval);
//         //             debug("resultRenko.crossAbove : " + resultRenko.crossAbove + " resultRenko.crossBelow : "
//         //                     + resultRenko.crossBelow);
//         //             // if (result.interval == activeTickInterval) {

//         //             if (resultRenko.crossAbove || resultRenko.crossBelow) {
//         //                 if (result.okLong()) {
//         //                     debug("Signal : okLong= " + result.okLong());
//         //                     debug("setCurrentStrategy : patternName= " + result.patternName);
//         //                     debug("setCurrentStrategy : getSymbol= " + ctx.getInstrument().getSymbol());
//         //                     debug("setCurrentStrategy : activeTickInterval= " + activeTickInterval);
//         //                     debug("setCurrentStrategy : tp= " + result.bracket.tp + ", sl= " + result.bracket.sl);

//         //                     analysis.setCurrentStrategy(result.patternName, result.bracket.tp, result.bracket.sl);
//         //                     analysis.analyseSignalEntryInit(result.patternName, bar.timestamp,
//         //                             CoreAnalysis.Direction.HIGH, priceClose);
//         //                 }
//         //                 if (result.okShort()) {
//         //                     debug("Signal : okShort= " + result.okShort());
//         //                     debug("setCurrentStrategy : patternName= " + result.patternName);
//         //                     debug("setCurrentStrategy : getSymbol= " + ctx.getInstrument().getSymbol());
//         //                     debug("setCurrentStrategy : activeTickInterval= " + activeTickInterval);
//         //                     debug("setCurrentStrategy : tp= " + result.bracket.tp + ", sl= " + result.bracket.sl);

//         //                     analysis.setCurrentStrategy(result.patternName, result.bracket.tp, result.bracket.sl);
//         //                     analysis.analyseSignalEntryInit(result.patternName, bar.timestamp,
//         //                             CoreAnalysis.Direction.LOW, priceClose);
//         //                 }
//         //             }
//         //             // }
//         //         }

//         //     } catch (ClassNotFoundException cnfe) {
//         //         System.err.println("Classe non trouvée : " + className + " (skip)");
//         //     } catch (Exception e) {
//         //         e.printStackTrace();
//         //     }
//         // }
//     }

// }
