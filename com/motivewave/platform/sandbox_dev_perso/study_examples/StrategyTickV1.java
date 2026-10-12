package custom_studies;

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

import com.motivewave.platform.sdk.draw.Line;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

import custom_studies.strategies.strategy_core_v2.StrategyAnalysis;
import custom_studies.strategies.strategy_core_v2.StrategyCorePattern;
import custom_studies.strategies.strategy_core_v2.utils.HistoryManager;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.PatternCheckResult;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.RenkoResult;
import custom_studies.strategies.strategy_core_v2.utils.FormatResult.SimulatedBar;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@StudyHeader(id = "STRATEGY_TICK_V1", namespace = "com.sbdev", name = "Strategy Tick V1", desc = "Strategy Tick V1", menu = "Custom Strategies", overlay = true, strategy = true, autoEntry = true, manualEntry = false, supportsStopPL = true, supportsUnrealizedPL = true, supportsRealizedPL = true, supportsTotalPL = true, supportsTargetPL = true, supportsRiskRatio = true, showTradeOptions = false, requiresBarUpdates = true)
public class StrategyTickV1 extends Study {

    protected HistoryManager history = new HistoryManager();
    protected StrategyAnalysis analysis = new StrategyAnalysis();

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    protected Double brickSize = null; // Taille de la brique
    protected Double previousClose = null; // Renko open
    protected Double realRenko = null; // Renko close

    final static String GREEN = "green";
    final static String RED = "red";
    final static String ABOVE = "above";
    final static String BELOW = "below";

    protected Double hilo1 = null;
    protected Double hilo2 = null;

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
        ADX, DX, PDM, NDM, TR,
    }

    enum Signals {
        CROSS_HIGH, CROSS_LOW, CROSS_ABOVE, CROSS_BELOW
    }

    public static final String TICK_SIZE = "tickSize";

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

    @Override
    public void initialize(Defaults defaults) {

        var sd = createSD();

        sd.addTab(initializeGeneralTab(defaults));
        sd.addTab(initializeAnalyseTab(defaults));

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

    protected SettingTab initializeAnalyseTab(Defaults defaults) {
        var tab = new SettingTab("Analyse");

        // Section 1
        var section1 = tab.addGroup("Interval");
        // section1.addRow(new BarSizeDescriptor(TICK_SIZE, "Ticks",
        // BarSize.getBarSize(Enums.BarSizeType.TICK, Enums.IntervalType.TICK, 25)));
        section1.addRow(new IntegerDescriptor(TICK_SIZE, "Tick Size", 25, 1, 999, 1));

        // Section 1
        var date = tab.addGroup("Date");
        date.addRow(new DateTimeDescriptor(START_DATETIME, "Start Date",
                Instant.now().toEpochMilli(), true)); // 1
        // date.addRow(new DateTimeDescriptor(END_DATETIME, "End Date/Time",
        // Instant.now().toEpochMilli(), false)); // Date
        // date.addRow(new IntegerDescriptor(START_DATETIME, "Started Ago (Days)", 30,
        // 1, 365, 1));
        date.addRow(new IntegerDescriptor(END_DATETIME, "Analysis Duration (Days)", 30, 1, 365, 1));

        var hours = tab.addGroup("Heures de filtrage");
        hours.addRow(new IntegerDescriptor("startHour", "Heure Début", 0, 0, 23, 1));
        hours.addRow(new IntegerDescriptor("endHour", "Heure Fin", 23, 1, 23, 1));

        // Section 2
        // var lines = tab.addGroup("Display");
        return tab;
    }

    // @Override
    // public void onActivate(OrderContext ctx) {
    // debug("TickEMACrossStrategy");

    // long startDateTime = getSettings().getLong(START_DATETIME);
    // long endDateTime = getSettings().getLong(END_DATETIME);

    // // if (startDateTime == null || endDateTime == null ||
    // // endDateTime.isBefore(startDateTime)) {
    // // debug("⚠️ Dates invalides. Veuillez vérifier les paramètres.");
    // // return;
    // // }

    // // long startTime = startDateTime.getMillis();
    // // long endTime = endDateTime.getMillis();
    // long startTime = startDateTime;
    // long endTime = endDateTime;

    // BarSize tf1 = getSettings().getBarSize(TIMEFRAME_1);
    // BarSize tf2 = getSettings().getBarSize(TIMEFRAME_2);

    // List<Bar> bars1m = getBarsSafe(ctx, startTime, endTime, tf1);
    // List<Bar> bars5m = getBarsSafe(ctx, startTime, endTime, tf2);
    // List<Tick> ticks = getTicksSafe(ctx, startTime, endTime);

    // if (bars1m == null || bars1m.size() < 22 || bars5m == null || bars5m.size() <
    // 22 || ticks == null
    // || ticks.size() < 525) {
    // debug("Pas assez de données pour analyser.");
    // return;
    // }

    // List<Float> aggregatedCloses = new ArrayList<>();
    // List<Long> timestamps = new ArrayList<>();

    // for (int i = 0; i <= ticks.size() - 25; i += 25) {
    // Tick lastTick = ticks.get(i + 24);
    // aggregatedCloses.add(lastTick.getPrice());
    // timestamps.add(lastTick.getTime());
    // }

    // DataContext dataSeries = ctx.getDataContext();

    // for (int i = 1; i < aggregatedCloses.size(); i++) {
    // float prevPrice = aggregatedCloses.get(i - 1);
    // float currPrice = aggregatedCloses.get(i);
    // long currTs = timestamps.get(i);

    // RenkoResult result = calculateRenkoTickBased(currPrice);

    // if (result.crossAbove) {
    // // debug("Cross ABOVE at " + currPrice + " ts=" + currTs);
    // debug("Cross ABOVE at " + currPrice + " date=" + dateFormat.format(new
    // Date(currTs)));
    // } else if (result.crossBelow) {
    // // debug("Cross BELOW at " + currPrice + " ts=" + currTs);
    // debug("Cross BELOW at " + currPrice + " date=" + dateFormat.format(new
    // Date(currTs)));
    // }

    // }

    // debug("Analyse terminée. Total blocs tick : " + aggregatedCloses.size());
    // }

    // Mise à jour de onActivate pour utiliser les bougies simulées
    @Override
    public void onActivate(OrderContext ctx) {
        debug("TickEMACrossStrategy");

        int tickSize = getSettings().getInteger(TICK_SIZE);

        long startMillis = getSettings().getLong(START_DATETIME);
        int duration = getSettings().getInteger(END_DATETIME);

        Instant start = Instant.ofEpochMilli(startMillis);
        ZonedDateTime startDay = start.atZone(ZoneId.systemDefault()).toLocalDate()
                .atStartOfDay(ZoneId.systemDefault());

        int startHour = getSettings().getInteger("startHour");
        int endHour = getSettings().getInteger("endHour");

        List<SimulatedBar> simulatedBars = new ArrayList<>();

        for (int i = 0; i < duration; i++) {
            ZonedDateTime dayStart = startDay.plusDays(i);
            ZonedDateTime dayEnd = dayStart.plusDays(1);

            debug(dayStart +" / "+ dayEnd);

            long dayStartMillis = dayStart.toInstant().toEpochMilli();
            long dayEndMillis = dayEnd.toInstant().toEpochMilli();

            List<Tick> dayTicks = getTicksSafe(ctx, dayStartMillis, dayEndMillis);
            if (dayTicks == null || dayTicks.size() < tickSize)
                continue;

            List<Tick> filteredTicks = dayTicks.stream()
                    .filter(tick -> {
                        LocalTime time = Instant.ofEpochMilli(tick.getTime())
                                .atZone(ZoneId.systemDefault()).toLocalTime();
                        return !time.isBefore(LocalTime.of(startHour, 0)) && !time.isAfter(LocalTime.of(endHour, 0));
                    })
                    .collect(Collectors.toList());

            if (filteredTicks.size() >= tickSize) {
                simulatedBars.addAll(aggregateSimulatedBarsByDay(filteredTicks, tickSize));
            }
        }

        this.brickSize = getSettings().getInteger(RENKO_POINT) / 4.0;

        for (SimulatedBar bar : simulatedBars) {
            RenkoResult result = calculateRenkoTickBased(bar.close);

            if (result.crossAbove) {
                debug("Cross ABOVE at " + bar.close + " date=" + dateFormat.format(new Date(bar.timestamp)));
            } else if (result.crossBelow) {
                debug("Cross BELOW at " + bar.close + " date=" + dateFormat.format(new Date(bar.timestamp)));
            }
        }

        debug("Analyse terminée. Total bougies simulées : " + simulatedBars.size());
    }

    // --- Méthode utilitaire pour regrouper les ticks par blocs de 25, réinitialisée chaque jour ---
    public List<SimulatedBar> aggregateSimulatedBarsByDay(List<Tick> ticks, int groupSize) {
        List<SimulatedBar> bars = new ArrayList<>();
        List<Tick> dayTicks = new ArrayList<>();
        long currentDayStart = -1;

        for (Tick tick : ticks) {
            long ts = tick.getTime();
            ZonedDateTime tickTime = Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault());
            long dayStart = tickTime.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

            if (dayStart != currentDayStart) {
                if (!dayTicks.isEmpty()) {
                    bars.addAll(aggregateSimulatedBars(dayTicks, groupSize));
                    dayTicks.clear();
                }
                currentDayStart = dayStart;
            }
            dayTicks.add(tick);
        }

        if (!dayTicks.isEmpty()) {
            bars.addAll(aggregateSimulatedBars(dayTicks, groupSize));
        }

        return bars;
    }

    // --- Méthode existante mise à jour pour inclure les bougies incomplètes ---
    public List<SimulatedBar> aggregateSimulatedBars(List<Tick> ticks, int groupSize) {
        List<SimulatedBar> bars = new ArrayList<>();

        int i = 0;
        for (; i <= ticks.size() - groupSize; i += groupSize) {
            float open = ticks.get(i).getPrice();
            float high = open;
            float low = open;
            float close = ticks.get(i + groupSize - 1).getPrice();
            long timestamp = ticks.get(i + groupSize - 1).getTime();

            for (int j = i; j < i + groupSize; j++) {
                float price = ticks.get(j).getPrice();
                if (price > high) high = price;
                if (price < low) low = price;
            }

            bars.add(new SimulatedBar(timestamp, open, high, low, close));
        }

        // Bougie incomplète si au moins 2 ticks restants
        if (i < ticks.size() && (ticks.size() - i) >= 2) {
            float open = ticks.get(i).getPrice();
            float high = open;
            float low = open;
            float close = ticks.get(ticks.size() - 1).getPrice();
            long timestamp = ticks.get(ticks.size() - 1).getTime();

            for (int j = i; j < ticks.size(); j++) {
                float price = ticks.get(j).getPrice();
                if (price > high) high = price;
                if (price < low) low = price;
            }

            bars.add(new SimulatedBar(timestamp, open, high, low, close));
        }

        return bars;
    }


    // @Override
    // public void onActivate(OrderContext ctx) {
    // debug("TickEMACrossStrategy");

    // long startDateTime = getSettings().getLong(START_DATETIME);
    // long endDateTime = getSettings().getLong(END_DATETIME);

    // long startTime = startDateTime;
    // long endTime = endDateTime;

    // BarSize tickBar = BarSize.getBarSize(Enums.BarSizeType.TICK,
    // getSettings().getInteger(TICK_SIZE));
    // BarSize tf1 = getSettings().getBarSize(TIMEFRAME_1);
    // BarSize tf2 = getSettings().getBarSize(TIMEFRAME_2);

    // List<Bar> bars1m = getBarsSafe(ctx, startTime, endTime, tf1);
    // List<Bar> bars5m = getBarsSafe(ctx, startTime, endTime, tf2);
    // List<Bar> tickBars25 = getBarsSafe(ctx, startTime, endTime, tickBar);

    // if (bars1m == null || bars5m == null || tickBars25 == null) {
    // debug("Erreur de chargement des données.");
    // return;
    // }

    // debug("bars1m.size = " + bars1m.size());
    // debug("bars5m.size = " + bars5m.size());
    // debug("tickBars25.size = " + tickBars25.size());

    // if (bars1m == null || bars1m.size() < 22 || bars5m == null || bars5m.size() <
    // 22 || tickBars25 == null
    // || tickBars25.size() < getSettings().getInteger(TICK_SIZE)) {
    // debug("Pas assez de données pour analyser.");
    // return;
    // }

    // this.brickSize = getSettings().getInteger(RENKO_POINT) / 4.0;

    // for (Bar bar : tickBars25) {
    // float close = bar.getClose();
    // long timestamp = bar.getEndTime();
    // RenkoResult result = calculateRenkoTickBased(close);

    // if (result.crossAbove) {
    // debug("Cross ABOVE at " + close + " date=" + dateFormat.format(new
    // Date(timestamp)));
    // } else if (result.crossBelow) {
    // debug("Cross BELOW at " + close + " date=" + dateFormat.format(new
    // Date(timestamp)));
    // }
    // }

    // debug("Analyse terminée. Total bougies T25 : " + tickBars25.size());
    // }

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

    public RenkoResult calculateRenkoTickBased(float currPrice) {
        boolean crossAbove = false;
        boolean crossBelow = false;

        if (realRenko == null) {
            double lowerBound = Math.floor(currPrice / 10) * 10;
            double upperBound = lowerBound + brickSize;

            if (currPrice > lowerBound + brickSize) {
                lowerBound += brickSize;
                upperBound = lowerBound + brickSize;
            }

            realRenko = upperBound;
            previousClose = lowerBound;
            brickSize = Math.abs(realRenko - previousClose);

            history.updateColorHistory(GREEN);
        } else {
            if (history.colorHistory[0].equals(GREEN) && currPrice - realRenko >= brickSize) {
                realRenko += Math.floor((currPrice - realRenko) / brickSize) * brickSize;
                previousClose = realRenko - brickSize;
                crossAbove = true;
                history.updateColorHistory(GREEN);
                history.updateCrossHistory(ABOVE);
            } else if (history.colorHistory[0].equals(GREEN) && realRenko - currPrice >= 2 * brickSize) {
                realRenko -= Math.floor((realRenko - currPrice) / brickSize) * brickSize;
                previousClose = realRenko + brickSize;
                crossBelow = true;
                history.updateColorHistory(RED);
                history.updateCrossHistory(BELOW);
            } else if (history.colorHistory[0].equals(RED) && currPrice - realRenko >= 2 * brickSize) {
                realRenko += Math.floor((currPrice - realRenko) / brickSize) * brickSize;
                previousClose = realRenko - brickSize;
                crossAbove = true;
                history.updateColorHistory(GREEN);
                history.updateCrossHistory(ABOVE);
            } else if (history.colorHistory[0].equals(RED) && realRenko - currPrice >= brickSize) {
                realRenko -= Math.floor((realRenko - currPrice) / brickSize) * brickSize;
                previousClose = realRenko + brickSize;
                crossBelow = true;
                history.updateColorHistory(RED);
                history.updateCrossHistory(BELOW);
            }
        }

        return new RenkoResult(crossAbove, crossBelow, realRenko, previousClose);
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
            hilo1 = maHigh1;
        } else if (close > maHigh1) {
            series.setDouble(index, Values.MA_HILO_1, maLow1);
            series.setPathColor(index, Values.MA_HILO_1, settings.getColor(LOW_COLOR_1));
            hilo1 = maLow1;
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
            hilo2 = maHigh2;
        } else if (close > maHigh2) {
            series.setDouble(index, Values.MA_HILO_2, maLow2);
            series.setPathColor(index, Values.MA_HILO_2, settings.getColor(LOW_COLOR_2));
            hilo2 = maLow2;
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
            if (realRenko > previousClose) {
                history.updateColorHistory(GREEN);
            } else {
                history.updateColorHistory(RED);
            }
        } else {
            if (history.colorHistory[0] == GREEN && currentClose - realRenko >= brickSize) {
                realRenko = realRenko + Math.floor((currentClose - realRenko) / brickSize) * brickSize;
                previousClose = realRenko - brickSize;
                crossAbove = true;
                cross = 1.0;
                history.updateColorHistory(GREEN);
                history.updateCrossHistory(ABOVE);
            } else if (history.colorHistory[0] == GREEN && realRenko - currentClose >= brickSize * 2) {
                realRenko = realRenko - Math.floor((realRenko - currentClose) / brickSize) * brickSize;
                previousClose = realRenko + brickSize;
                crossBelow = true;
                cross = -1.0;
                history.updateColorHistory(RED);
                history.updateCrossHistory(BELOW);
            } else if (history.colorHistory[0] == RED && currentClose - realRenko >= brickSize * 2) {
                realRenko = realRenko + Math.floor((currentClose - realRenko) / brickSize) * brickSize;
                previousClose = realRenko - brickSize;
                crossAbove = true;
                cross = 1.0;
                history.updateColorHistory(GREEN);
                history.updateCrossHistory(ABOVE);
            } else if (history.colorHistory[0] == RED && realRenko - currentClose >= brickSize) {
                realRenko = realRenko - Math.floor((realRenko - currentClose) / brickSize) * brickSize;
                previousClose = realRenko + brickSize;
                crossBelow = true;
                cross = -1.0;
                history.updateColorHistory(RED);
                history.updateCrossHistory(BELOW);
            }
        }

        history.autoUpdateHiLoPriceHistory(hilo1, hilo2);
        history.autoUpdateHiLoColorHistory(close, history.hiLoPrice1History, history.hiLoPrice2History);

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
        Color brickColor = history.colorHistory[0] == GREEN ? settings.getColor(UP_COLOR)
                : settings.getColor(DOWN_COLOR);

        // Plot the Renko lines
        series.setPathColor(index - 1, Values.LOW, brickColor);
        series.setPathColor(index - 1, Values.HIGH, brickColor);

        series.setDouble(index - 1, Values.LOW, supp);
        series.setDouble(index, Values.HIGH, res);

        series.setDouble(index, Values.CROSS, cross);

        if (series.isBarComplete(index))
            series.setComplete(index, complete);

        if (!history.isArrayAsNull(history.colorHistory) && !history.isArrayAsNull(history.crossHistory)) {
            manageDowLinesConditionally(index, ctx, realRenko);
        }

        if (!history.isArrayAsNull(history.colorHistory) && !history.isArrayAsNull(history.hiLoColor1History) &&
                !history.isArrayAsNull(history.hiLoColor2History) && !history.isArrayAsNull(history.hiLoPrice1History)
                && !history.isArrayAsNull(history.hiLoPrice2History)) {
            strategySignal(index, ctx);
        }
    }

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
        Double priceSupp = series.getDouble(index - 1, Values.LOW);
        Double priceRes = series.getDouble(index, Values.HIGH);
        Float priceClose = series.getClose(index);
        Float priceHigh = series.getHigh(index);
        Float priceLow = series.getLow(index);
        if (priceSupp == null || priceRes == null)
            return;

        analysis.analyseSignalEntry(priceHigh, priceLow);

        StrategyCorePattern strategyCore = new StrategyCorePattern(
                history.colorHistory, history.hiLoColor1History, history.hiLoColor2History,
                history.hiLoPrice1History, history.hiLoPrice2History, priceRes, priceClose);

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
                    // + "\n hiLoPrice1History[0] : " + history.hiLoPrice1History[0]
                    // + "\n hiLoPrice1History[1] : " + history.hiLoPrice1History[1]
                    // + "\n hiLoPrice1History[2] : " + history.hiLoPrice1History[2]
                    // + "\n hiLoPrice2History[0] : " + history.hiLoPrice2History[0]
                    // + "\n hiLoPrice2History[1] : " + history.hiLoPrice2History[1]
                    // + "\n hiLoPrice2History[2] : " + history.hiLoPrice2History[2]
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

                analysis.setCurrentStrategy(result.patternName, ctx.getInstrument().getSymbol(), getSettings().getInteger(TICK_SIZE), result.bracket.tp, result.bracket.sl);
                analysis.analyseSignalEntryInit(series.getEndTime(index), StrategyAnalysis.Direction.HIGH, priceClose);

            }

            if (crossBelow && result.okShort()) {
                var c = new Coordinate(series.getStartTime(index), priceRes);
                var marker = settings.getMarker(DOWN_MARKER);
                String msg = popupMsgSignal;
                if (marker.isEnabled())
                    addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
                ctx.signal(index, Signals.CROSS_LOW, msg, priceSupp);

                analysis.setCurrentStrategy(result.patternName, ctx.getInstrument().getSymbol(), getSettings().getInteger(TICK_SIZE), result.bracket.tp, result.bracket.sl);
                analysis.analyseSignalEntryInit(series.getEndTime(index), StrategyAnalysis.Direction.LOW, priceClose);

            }
        }

        if (settings.getBoolean(RENKO_ENABLED) && settings.getBoolean(INFO_ENABLED)) {

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
