// package study_examples;

// import com.motivewave.platform.sdk.common.*;
// import com.motivewave.platform.sdk.common.desc.*;
// import com.motivewave.platform.sdk.draw.Marker;
// import com.motivewave.platform.sdk.order_mgmt.OrderContext;
// import com.motivewave.platform.sdk.study.Study;
// import com.motivewave.platform.sdk.study.StudyHeader;

// import java.time.Instant;
// import java.util.*;

// @StudyHeader(id = "TICK_DATA_TEST", namespace = "com.sbdev", name = "Tick Data Test", desc = "Tick Data Test", menu = "Custom Strategies", overlay = true, strategy = true, autoEntry = true, manualEntry = false, supportsStopPL = true, supportsUnrealizedPL = true, supportsRealizedPL = true, supportsTotalPL = true, supportsTargetPL = true, supportsRiskRatio = true, showTradeOptions = false, requiresBarUpdates = true)
// public class TickEMACrossStrategy extends Study {

//     final static String UP_MARKER = "upMarker";
//     final static String DOWN_MARKER = "downMarker";

//     final static String TF_ENABLED_1 = "tfEnabled1";
//     final static String TIMEFRAME_1 = "tf1";
//     final static String MA_METHOD_1 = "maMethod1";
//     final static String PERIOD_1 = "period1";
//     final static String TF_ENABLED_2 = "tfEnabled2";
//     final static String TIMEFRAME_2 = "tf2";
//     final static String MA_METHOD_2 = "maMethod2";
//     final static String PERIOD_2 = "period2";
//     final static String START_DATETIME = "startDateTime";
//     final static String END_DATETIME = "endDateTime";

//     final static String MA_SERIES_1 = "ma_series_1";
//     final static String MA_SERIES_2 = "ma_series_2";

//     @Override
//     public void initialize(Defaults defaults) {
//         var desc = createSD();
//         var tab = new SettingTab("Général");

//         var grp1 = tab.addGroup(get("First Timeframe"));
//         grp1.addRow(new BooleanDescriptor(TF_ENABLED_1, "Use Timeframe 1", true));
//         grp1.addRow(new BarSizeDescriptor(TIMEFRAME_1, "Timeframe 1", BarSize.getBarSize(1)));
//         grp1.addRow(new MAMethodDescriptor(MA_METHOD_1, "MA Method 1", Enums.MAMethod.SMA));
//         grp1.addRow(new IntegerDescriptor(PERIOD_1, "Period 1", 21, 1, 9999, 1));

//         var grp2 = tab.addGroup(get("Second Timeframe"));
//         grp2.addRow(new BooleanDescriptor(TF_ENABLED_2, "Use Timeframe 2", true));
//         grp2.addRow(new BarSizeDescriptor(TIMEFRAME_2, "Timeframe 2", BarSize.getBarSize(5)));
//         grp2.addRow(new MAMethodDescriptor(MA_METHOD_2, "MA Method 2", Enums.MAMethod.SMA));
//         grp2.addRow(new IntegerDescriptor(PERIOD_2, "Period 2", 21, 1, 9999, 1));

//         var date = tab.addGroup("Date");
//         // Création des objets DateTime avec DateTime.now() ou une autre valeur
//         // spécifique
//         date.addRow(new DateTimeDescriptor(START_DATETIME, "Start Date/Time", Instant.now().toEpochMilli(), false)); // 1 jour
//                                                                                                               // avant
//         date.addRow(new DateTimeDescriptor(END_DATETIME, "End Date/Time", Instant.now().toEpochMilli(), false)); // Date
//                                                                                                           // actuelle

//         var markers = tab.addGroup("Markers");
//         markers.addRow(new MarkerDescriptor(UP_MARKER, "Up Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL,
//                 defaults.getGreen(), defaults.getLineColor(), true, true));
//         markers.addRow(new MarkerDescriptor(DOWN_MARKER, "Down Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL,
//                 defaults.getRed(), defaults.getLineColor(), true, true));

//         desc.addTab(tab);
//         setSettingsDescriptor(desc);
//     }

//     @Override
//     public void onActivate(OrderContext ctx) {
//         debug("TickEMACrossStrategy");

//         long startDateTime = getSettings().getLong(START_DATETIME);
//         long endDateTime = getSettings().getLong(END_DATETIME);

//         // if (startDateTime == null || endDateTime == null ||
//         // endDateTime.isBefore(startDateTime)) {
//         // debug("⚠️ Dates invalides. Veuillez vérifier les paramètres.");
//         // return;
//         // }

//         // long startTime = startDateTime.getMillis();
//         // long endTime = endDateTime.getMillis();
//         long startTime = startDateTime;
//         long endTime = endDateTime;

//         BarSize tf1 = getSettings().getBarSize(TIMEFRAME_1);
//         BarSize tf2 = getSettings().getBarSize(TIMEFRAME_2);

//         List<Bar> bars1m = getBarsSafe(ctx, startTime, endTime, tf1);
//         List<Bar> bars5m = getBarsSafe(ctx, startTime, endTime, tf2);
//         List<Tick> ticks = getTicksSafe(ctx, startTime, endTime);

//         if (bars1m == null || bars1m.size() < 22 || bars5m == null || bars5m.size() < 22 || ticks == null
//                 || ticks.size() < 525) {
//             debug("Pas assez de données pour analyser.");
//             return;
//         }

//         computeMA(ctx, MA_METHOD_1, bars1m, PERIOD_1, MA_SERIES_1);
//         computeMA(ctx, MA_METHOD_2, bars5m, PERIOD_2, MA_SERIES_2);

//         List<Float> aggregatedCloses = new ArrayList<>();
//         List<Long> timestamps = new ArrayList<>();

//         for (int i = 0; i <= ticks.size() - 25; i += 25) {
//             Tick lastTick = ticks.get(i + 24);
//             aggregatedCloses.add(lastTick.getPrice());
//             timestamps.add(lastTick.getTime());
//         }

//         DataContext dataSeries = ctx.getDataContext();

//         for (int i = 1; i < aggregatedCloses.size(); i++) {
//             float prevPrice = aggregatedCloses.get(i - 1);
//             float currPrice = aggregatedCloses.get(i);
//             long currTs = timestamps.get(i);

//             Float currMA1 = dataSeries.getDataSeries(tf1).getFloat(MA_SERIES_1, i);
//             Float prevMA1 = dataSeries.getDataSeries(tf1).getFloat(MA_SERIES_1, i - 1);

//             Float currMA2 = dataSeries.getDataSeries(tf2).getFloat(MA_SERIES_2, i);
//             Float prevMA2 = dataSeries.getDataSeries(tf2).getFloat(MA_SERIES_2, i - 1);

//             if (currMA1 != null && prevMA1 != null) {
//                 boolean crossedUp = prevPrice < prevMA1 && currPrice > currMA1;
//                 boolean crossedDown = prevPrice > prevMA1 && currPrice < currMA1;

//                 if (crossedUp) {
//                     String msg = "🔼 MA1 HAUSSIER à " + new Date(currTs) + " | Prix: " + currPrice + " / MA: "
//                             + currMA1;
//                     debug(msg);
//                     var c = new Coordinate(currTs, currPrice);
//                     var marker = getSettings().getMarker(UP_MARKER);
//                     addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
//                 } else if (crossedDown) {
//                     String msg = "🔽 MA1 BAISSIER à " + new Date(currTs) + " | Prix: " + currPrice + " / MA: "
//                             + currMA1;
//                     debug(msg);
//                     var c = new Coordinate(currTs, currPrice);
//                     var marker = getSettings().getMarker(DOWN_MARKER);
//                     addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
//                 }
//             }

//             if (currMA2 != null && prevMA2 != null) {
//                 boolean crossedUp = prevPrice < prevMA2 && currPrice > currMA2;
//                 boolean crossedDown = prevPrice > prevMA2 && currPrice < currMA2;

//                 if (crossedUp) {
//                     String msg = "🔼 MA2 HAUSSIER à " + new Date(currTs) + " | Prix: " + currPrice + " / MA: "
//                             + currMA2;
//                     debug(msg);
//                     var c = new Coordinate(currTs, currPrice);
//                     var marker = getSettings().getMarker(UP_MARKER);
//                     addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
//                 } else if (crossedDown) {
//                     String msg = "🔽 MA2 BAISSIER à " + new Date(currTs) + " | Prix: " + currPrice + " / MA: "
//                             + currMA2;
//                     debug(msg);
//                     var c = new Coordinate(currTs, currPrice);
//                     var marker = getSettings().getMarker(DOWN_MARKER);
//                     addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
//                 }
//             }
//         }

//         debug("Analyse terminée. Total blocs tick : " + aggregatedCloses.size());
//     }

//     private void computeMA(OrderContext ctx, String methodKey, List<Bar> bars, String periodKey, String valKey) {
//         Enums.MAMethod method = getSettings().getMAMethod(methodKey);
//         int period = getSettings().getInteger(periodKey);

//         for (int i = 0; i < bars.size(); i++) {
//             Util.calcMAAt(i, ctx.getDataContext(), method, Enums.BarInput.CLOSE, period, 0, valKey);
//         }
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
// }

// package study_examples;

// import com.motivewave.platform.sdk.common.*;
// import com.motivewave.platform.sdk.common.desc.*;
// import com.motivewave.platform.sdk.draw.Marker;
// import com.motivewave.platform.sdk.order_mgmt.OrderContext;
// import com.motivewave.platform.sdk.study.Study;
// import com.motivewave.platform.sdk.study.StudyHeader;

// import java.util.*;

// @StudyHeader(id = "TICK_DATA_TEST", namespace = "com.sbdev", name = "Tick Data Test", desc = "Tick Data Test", menu = "Custom Strategies", overlay = true, strategy = true, autoEntry = true, manualEntry = false, supportsStopPL = true, supportsUnrealizedPL = true, supportsRealizedPL = true, supportsTotalPL = true, supportsTargetPL = true, supportsRiskRatio = true, showTradeOptions = false, requiresBarUpdates = true)
// public class TickEMACrossStrategy extends Study {

//     final static String UP_MARKER = "upMarker";
//     final static String DOWN_MARKER = "downMarker";

//     final static String TF_ENABLED_1 = "tfEnabled1";
//     final static String TIMEFRAME_1 = "tf1";
//     final static String MA_METHOD_1 = "maMethod1";
//     final static String PERIOD_1 = "period1";
//     final static String TF_ENABLED_2 = "tfEnabled2";
//     final static String TIMEFRAME_2 = "tf2";
//     final static String MA_METHOD_2 = "maMethod2";
//     final static String PERIOD_2 = "period2";
//     final static String START_TIME = "startTime";
//     final static String END_TIME = "endTime";

//     final static String MA_SERIES_1 = "maSeries1";
//     final static String MA_SERIES_2 = "maSeries2";

//     @Override
//     public void initialize(Defaults defaults) {
//         var desc = createSD();
//         var tab = new SettingTab("Général");

//         var grp1 = tab.addGroup(get("First Timeframe"));
//         grp1.addRow(new BooleanDescriptor(TF_ENABLED_1, "Use Timeframe 1", true));
//         grp1.addRow(new BarSizeDescriptor(TIMEFRAME_1, "Timeframe 1", BarSize.getBarSize(1)));
//         grp1.addRow(new MAMethodDescriptor(MA_METHOD_1, "MA Method 1", Enums.MAMethod.SMA));
//         grp1.addRow(new IntegerDescriptor(PERIOD_1, "Period 1", 21, 1, 9999, 1));

//         var grp2 = tab.addGroup(get("Second Timeframe"));
//         grp2.addRow(new BooleanDescriptor(TF_ENABLED_2, "Use Timeframe 2", true));
//         grp2.addRow(new BarSizeDescriptor(TIMEFRAME_2, "Timeframe 2", BarSize.getBarSize(5)));
//         grp2.addRow(new MAMethodDescriptor(MA_METHOD_2, "MA Method 2", Enums.MAMethod.SMA));
//         grp2.addRow(new IntegerDescriptor(PERIOD_2, "Period 2", 21, 1, 9999, 1));

//         var date = tab.addGroup("Date");
//         date.addRow(new IntegerDescriptor(START_TIME, "Start Time (Jours)", 30, 1, 365, 1));
//         date.addRow(new IntegerDescriptor(END_TIME, "Durée Analyse (Jours)", 1, 1, 365, 1));

//         var markers = tab.addGroup("Markers");
//         markers.addRow(new MarkerDescriptor(UP_MARKER, "Up Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL,
//                 defaults.getGreen(), defaults.getLineColor(), true, true));
//         markers.addRow(new MarkerDescriptor(DOWN_MARKER, "Down Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL,
//                 defaults.getRed(), defaults.getLineColor(), true, true));

//         desc.addTab(tab);
//         setSettingsDescriptor(desc);
//     }

//     @Override
//     public void onActivate(OrderContext ctx) {
//         debug("TickEMACrossStrategy");

//         int startDays = getSettings().getInteger(START_TIME);
//         int durationDays = getSettings().getInteger(END_TIME);

//         long oneDay = 24 * 60 * 60 * 1000L;
//         long now = System.currentTimeMillis();

//         long startTime = now - (startDays * oneDay);
//         long endTime = startTime + (durationDays * oneDay);

//         BarSize tf1 = getSettings().getBarSize(TIMEFRAME_1);
//         BarSize tf2 = getSettings().getBarSize(TIMEFRAME_2);

//         List<Bar> bars1m = getBarsSafe(ctx, startTime, endTime, tf1);
//         List<Bar> bars5m = getBarsSafe(ctx, startTime, endTime, tf2);
//         List<Tick> ticks = getTicksSafe(ctx, startTime, endTime);

//         if (bars1m == null || bars1m.size() < 22 || bars5m == null || bars5m.size() < 22 || ticks == null
//                 || ticks.size() < 525) {
//             debug("Pas assez de données pour analyser.");
//             return;
//         }

//         Map<Long, Float> ema1m = computeMA(bars1m, getSettings().getInteger(PERIOD_1), getSettings().getMAMethod(MA_METHOD_1));
//         Map<Long, Float> ema5m = computeMA(bars5m, getSettings().getInteger(PERIOD_2), getSettings().getMAMethod(MA_METHOD_2));

//         List<Float> aggregatedCloses = new ArrayList<>();
//         List<Long> timestamps = new ArrayList<>();

//         for (int i = 0; i <= ticks.size() - 25; i += 25) {
//             Tick lastTick = ticks.get(i + 24);
//             aggregatedCloses.add(lastTick.getPrice());
//             timestamps.add(lastTick.getTime());
//         }

//         for (int i = 1; i < aggregatedCloses.size(); i++) {
//             float prevPrice = aggregatedCloses.get(i - 1);
//             float currPrice = aggregatedCloses.get(i);
//             long currTs = timestamps.get(i);

//             long roundedMinute = currTs - (currTs % (60 * 1000));

//             Float currEMA1 = ema1m.get(roundedMinute);
//             Float prevEMA1 = ema1m.get(roundedMinute - 60_000);

//             Float currEMA2 = ema5m.get(roundedMinute);
//             Float prevEMA2 = ema5m.get(roundedMinute - 60_000);

//             if (currEMA1 != null && prevEMA1 != null) {
//                 boolean crossedUp = prevPrice < prevEMA1 && currPrice > currEMA1;
//                 boolean crossedDown = prevPrice > prevEMA1 && currPrice < currEMA1;

//                 if (crossedUp) {
//                     String msg = "🔼 TF1 HAUSSIER à " + new Date(currTs) + " | Prix: " + currPrice + " / MA1: " + currEMA1;
//                     debug(msg);
//                     var c = new Coordinate(currTs, currPrice);
//                     var marker = getSettings().getMarker(UP_MARKER);
//                     addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
//                 } else if (crossedDown) {
//                     String msg = "🔽 TF1 BAISSIER à " + new Date(currTs) + " | Prix: " + currPrice + " / MA1: " + currEMA1;
//                     debug(msg);
//                     var c = new Coordinate(currTs, currPrice);
//                     var marker = getSettings().getMarker(DOWN_MARKER);
//                     addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
//                 }
//             }

//             if (currEMA2 != null && prevEMA2 != null) {
//                 boolean crossedUp = prevPrice < prevEMA2 && currPrice > currEMA2;
//                 boolean crossedDown = prevPrice > prevEMA2 && currPrice < currEMA2;

//                 if (crossedUp) {
//                     String msg = "🔼 TF2 HAUSSIER à " + new Date(currTs) + " | Prix: " + currPrice + " / MA2: " + currEMA2;
//                     debug(msg);
//                 } else if (crossedDown) {
//                     String msg = "🔽 TF2 BAISSIER à " + new Date(currTs) + " | Prix: " + currPrice + " / MA2: " + currEMA2;
//                     debug(msg);
//                 }
//             }
//         }

//         debug("Analyse terminée. Total blocs tick : " + aggregatedCloses.size());
//     }

//     private Map<Long, Float> computeMA(List<Bar> bars, int period, Enums.MAMethod method) {
//         Map<Long, Float> maMap = new TreeMap<>();
//         float multiplier = 2f / (period + 1);
//         float ema = bars.get(0).getClose();

//         for (int i = 0; i < bars.size(); i++) {
//             float close = bars.get(i).getClose();
//             long ts = bars.get(i).getEndTime();
//             if (i == 0) {
//                 maMap.put(ts, ema);
//             } else {
//                 ema = (close - ema) * multiplier + ema;
//                 maMap.put(ts, ema);
//             }
//         }
//         return maMap;
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
// }

package custom_studies;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.common.desc.*;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

// import java.awt.*;
import java.util.*;

@StudyHeader(id = "TICK_DATA_TEST", namespace = "com.sbdev", name = "Tick Data Test", desc = "Tick Data Test", menu = "Custom Strategies", overlay = true, strategy = true, autoEntry = true, manualEntry = false, supportsStopPL = true, supportsUnrealizedPL = true, supportsRealizedPL = true, supportsTotalPL = true, supportsTargetPL = true, supportsRiskRatio = true, showTradeOptions = false, requiresBarUpdates = true)
public class TickEMACrossStrategy extends Study {

    final static String UP_MARKER = "upMarker";
    final static String DOWN_MARKER = "downMarker";

    final static String TIMEFRAME1 = "tf1";
    final static String TIMEFRAME2 = "tf2";
    final static String START_TIME = "startTime";
    final static String END_TIME = "endTime";

    @Override
    public void initialize(Defaults defaults) {
        var desc = createSD();
        var tab = new SettingTab("Général");

        var markers = tab.addGroup("Markers");
        markers.addRow(new MarkerDescriptor(UP_MARKER, "Up Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL,
                defaults.getGreen(), defaults.getLineColor(), true, true));
        markers.addRow(new MarkerDescriptor(DOWN_MARKER, "Down Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL,
                defaults.getRed(), defaults.getLineColor(), true, true));

        var settings = tab.addGroup("Paramètres Timeframe et Période");
        settings.addRow(new BarSizeDescriptor(TIMEFRAME1, "Timeframe 1", BarSize.getBarSize(1)));
        settings.addRow(new BarSizeDescriptor(TIMEFRAME2, "Timeframe 1", BarSize.getBarSize(5)));
        settings.addRow(new IntegerDescriptor(START_TIME, "Start Time (Jours)", 30, 1, 365, 1));
        settings.addRow(new IntegerDescriptor(END_TIME, "Durée Analyse (Jours)", 1, 1, 365, 1));

        desc.addTab(tab);
        setSettingsDescriptor(desc);
    }

    @Override
    public void onActivate(OrderContext ctx) {
        debug("TickEMACrossStrategy");

        int startDays = getSettings().getInteger(START_TIME);
        int durationDays = getSettings().getInteger(END_TIME);

        long oneDay = 24 * 60 * 60 * 1000L;
        long now = System.currentTimeMillis();

        long startTime = now - (startDays * oneDay);
        long endTime = startTime + (durationDays * oneDay);

        BarSize tf1 = getSettings().getBarSize(TIMEFRAME1);
        BarSize tf2 = getSettings().getBarSize(TIMEFRAME2);

        List<Bar> bars1m = getBarsSafe(ctx, startTime, endTime, tf1);
        List<Bar> bars5m = getBarsSafe(ctx, startTime, endTime, tf2);
        List<Tick> ticks = getTicksSafe(ctx, startTime, endTime);

        if (bars1m == null || bars1m.size() < 22 || bars5m == null || bars5m.size() < 22 || ticks == null
                || ticks.size() < 525) {
            debug("Pas assez de données pour analyser.");
            return;
        }

        Map<Long, Float> maHigh1Map = computeMA(bars1m, 21, Enums.BarInput.HIGH);
        Map<Long, Float> maLow1Map = computeMA(bars1m, 21, Enums.BarInput.LOW);

        List<Float> aggregatedCloses = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>();

        for (int i = 0; i <= ticks.size() - 25; i += 25) {
            Tick lastTick = ticks.get(i + 24);
            aggregatedCloses.add(lastTick.getPrice());
            timestamps.add(lastTick.getTime());
        }

        Enums.MarkerType upType = getSettings().getMarker(UP_MARKER).getType();
        Enums.MarkerType downType = getSettings().getMarker(DOWN_MARKER).getType();

        for (int i = 1; i < aggregatedCloses.size(); i++) {
            float prevPrice = aggregatedCloses.get(i - 1);
            float currPrice = aggregatedCloses.get(i);
            long currTs = timestamps.get(i);

            long roundedMinute = currTs - (currTs % (60 * 1000));
            Float maHigh = maHigh1Map.get(roundedMinute);
            Float maLow = maLow1Map.get(roundedMinute);

            if (maHigh == null || maLow == null)
                continue;

            boolean crossedUp = prevPrice < maHigh && currPrice > maHigh;
            boolean crossedDown = prevPrice > maLow && currPrice < maLow;

            if (crossedUp) {
                String msg = "🔼 HAUSSIER (GannHiLo) à " + new Date(currTs) + " | Prix: " + currPrice + " / Hi: "
                        + maHigh;
                debug(msg);
                var marker = getSettings().getMarker(UP_MARKER);
                var c = new Coordinate(currTs, currPrice);
                addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
            } else if (crossedDown) {
                String msg = "🔽 BAISSIER (GannHiLo) à " + new Date(currTs) + " | Prix: " + currPrice + " / Lo: "
                        + maLow;
                debug(msg);
                var marker = getSettings().getMarker(DOWN_MARKER);
                var c = new Coordinate(currTs, currPrice);
                addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
            }
        }

        debug("Analyse terminée. Total blocs tick : " + aggregatedCloses.size());
    }

    private Map<Long, Float> computeMA(List<Bar> bars, int period, Enums.BarInput input) {
        Map<Long, Float> maMap = new TreeMap<>();
        for (int i = period - 1; i < bars.size(); i++) {
            float sum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                Bar bar = bars.get(j);
                float value = switch (input) {
                    case HIGH -> bar.getHigh();
                    case LOW -> bar.getLow();
                    default -> bar.getClose();
                };
                sum += value;
            }
            long ts = bars.get(i).getEndTime();
            maMap.put(ts, sum / period);
        }
        return maMap;
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

    @Override
    protected void calculate(int index, DataContext ctx) {
        // plus nécessaire ici, le traitement a été déplacé dans onActivate()
    }
}
