package sbdev.joy_ai_project.pattern_strategy_algo.service.statistic;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.common.desc.*;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

import sbdev.joy_ai_project.pattern_strategy_algo.database.StatisticDbManager;
import sbdev.joy_ai_project.pattern_strategy_algo.service.*;
import sbdev.joy_ai_project.pattern_strategy_algo.util.*;
import sbdev.joy_ai_project.pattern_strategy_algo.model.*;

import java.util.*;
import java.sql.SQLException;
import java.time.*;

/**
 * Snapshot History V1 — version low-memory: - Calcul HiLo par JOUR avec petit
 * lookback (configurable) => pas de rupture à minuit, et RAM limitée au strict
 * nécessaire. - Continuité Renko sur toute la plage (reset par intervalle
 * seulement). - Factorisation HiLo via HiLoManager + adapters.
 */
@StudyHeader(namespace = "com.sbdev", id = "SNAPSHOT_HISTORY_DATA", name = "Snapshot History Data", label = "Snapshot History Data", desc = "Snapshot History Data", menu = "SB Dev", overlay = true, strategy = true)
public class SnapshotHistoryData extends Study {

    // ========= Réglage lookback (jours) =========
    // Augmente si tu utilises de grandes périodes / gros timeframes.
    private static final int HILO_LOOKBACK_DAYS_DEFAULT = 3;

    private final HiLoManager hiloCore = new HiLoManager();
    private final StatisticDbManager dbManager = new StatisticDbManager();

    protected SnapshotHistoryBufferManager history;

    protected Double brickSize = null; // Taille de la brique Renko (en points)
    protected Double previousClose = null;
    protected Double realRenko;

    // Codes couleurs/états
    final static String GREEN = "green";
    final static String RED = "red";
    final static String ABOVE = "above";
    final static String BELOW = "below";

    protected Double hiLo1 = null;
    protected Double hiLo2 = null;

    // Settings Keys (identiques à ta version)
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
        HIGH, LOW, CROSS, MA_HILO_1, MA_HILO_2, ADX, DX, PDM, NDM, TR
    }

    enum Signals {
        CROSS_HIGH, CROSS_LOW, CROSS_ABOVE, CROSS_BELOW
    }

    // HiLo 1
    public static final String TF_ENABLED_1 = "tfEnabled1";
    public static final String TIMEFRAME_1 = "timeframe1";
    public static final String MA_METHOD_1 = "maMethod1";
    public static final String PATH_HILO_1 = "pathHilo1";
    public static final String PERIOD_1 = "period1";

    // HiLo 2
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
    final static String DOW_LINE_DOWN_1 = "dowLineDown_1";

    final static String START_DATETIME = "startDateTime";
    final static String END_DATETIME = "endDateTime";

    // Ticks
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

        // Quick Settings
        sd.addQuickSettings(RENKO_POINT);
        sd.addQuickSettings(TOP_PATH, BOTTOM_PATH);

        sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_1, TICK_INTERVAL_1));
        sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_2, TICK_INTERVAL_2));
        sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_3, TICK_INTERVAL_3));
        sd.addDependency(new EnabledDependency(TICK_INTERVAL_ENABLED_4, TICK_INTERVAL_4));

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

        desc.declareSignal(Signals.CROSS_HIGH, "Cross High");
        desc.declareSignal(Signals.CROSS_LOW, "Cross Low");

        desc.setRangeKeys(Values.HIGH, Values.LOW);

        // HiLo 1
        var tab1 = sd.addTab(get("HiLo 1"));
        var grp1 = tab1.addGroup(get("First Timeframe"));
        grp1.addRow(new BooleanDescriptor(TF_ENABLED_1, "Use Timeframe 1", true));
        grp1.addRow(new BarSizeDescriptor(TIMEFRAME_1, "Timeframe 1", BarSize.getBarSize(1)));
        grp1.addRow(new MAMethodDescriptor(MA_METHOD_1, "MA Method 1", Enums.MAMethod.SMA));
        grp1.addRow(new IntegerDescriptor(PERIOD_1, "Period 1", 21, 1, 9999, 1));
        grp1 = tab1.addGroup(get("Display"));
        grp1.addRow(new PathDescriptor(PATH_HILO_1, "Hilo Path 1", null, 1.0f, null, true, true, false));
        grp1.addRow(new ColorDescriptor(HIGH_COLOR_1, "High Color 1", defaults.getRedLine()));
        grp1.addRow(new ColorDescriptor(LOW_COLOR_1, "Low Color 1", defaults.getGreenLine()));

        // HiLo 2
        var tab2 = sd.addTab(get("HiLo 2"));
        var grp2 = tab2.addGroup(get("Second Timeframe"));
        grp2.addRow(new BooleanDescriptor(TF_ENABLED_2, "Use Timeframe 2", true));
        grp2.addRow(new BarSizeDescriptor(TIMEFRAME_2, "Timeframe 2", BarSize.getBarSize(5)));
        grp2.addRow(new MAMethodDescriptor(MA_METHOD_2, "MA Method 2", Enums.MAMethod.SMA));
        grp2.addRow(new IntegerDescriptor(PERIOD_2, "Period 2", 21, 1, 9999, 1));
        grp2 = tab2.addGroup(get("Display"));
        grp2.addRow(new PathDescriptor(PATH_HILO_2, "Hilo Path 2", null, 1.0f, null, true, true, false));
        grp2.addRow(new ColorDescriptor(HIGH_COLOR_2, "High Color 2", defaults.getRedLine()));
        grp2.addRow(new ColorDescriptor(LOW_COLOR_2, "Low Color 2", defaults.getGreenLine()));

        // Déclarations de chemins HiLo
        desc.declarePath(Values.MA_HILO_1, PATH_HILO_1);
        desc.declarePath(Values.MA_HILO_2, PATH_HILO_2);

        // Signals supplémentaires
        desc.declareSignal(Signals.CROSS_ABOVE, "PRICE_CROSS_ABOVE");
        desc.declareSignal(Signals.CROSS_BELOW, "PRICE_CROSS_BELOW");
    }

    protected SettingTab initializeGeneralTab(Defaults defaults) {
        var tab = new SettingTab("Renko");

        var section1 = tab.addGroup("Inputs");
        section1.addRow(new BooleanDescriptor(RENKO_ENABLED, "Use Renko", true));
        section1.addRow(new IntegerDescriptor(RENKO_POINT, "Renko Ticks", 20, 1, 99, 1));
        section1.addRow(new StringDescriptor("note", "Note", "NQ : 4 ticks = 1 point"));

        var lines = tab.addGroup("Display");
        lines.addRow(new PathDescriptor(TOP_PATH, "High Path", null, 1.0f, null, true, true, false));
        lines.addRow(new PathDescriptor(BOTTOM_PATH, "Bottom Path", null, 1.0f, null, true, true, false));
        lines.addRow(new ColorDescriptor(UP_COLOR, "Up Color", defaults.getGreenLine()));
        lines.addRow(new ColorDescriptor(DOWN_COLOR, "Down Color", defaults.getRedLine()));

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

        var section1 = tab.addGroup("Interval");
        section1.addRow(new IntegerDescriptor(TICK_INTERVAL_1, "Tick Interval 1", 1, 1, 1000, 1),
                new BooleanDescriptor(TICK_INTERVAL_ENABLED_1, "Use Tick Interval 1", true));
        section1.addRow(new IntegerDescriptor(TICK_INTERVAL_2, "Tick Interval 2", 5, 1, 1000, 1),
                new BooleanDescriptor(TICK_INTERVAL_ENABLED_2, "Use Tick Interval 2", true));
        section1.addRow(new IntegerDescriptor(TICK_INTERVAL_3, "Tick Interval 3", 10, 1, 1000, 1),
                new BooleanDescriptor(TICK_INTERVAL_ENABLED_3, "Use Tick Interval 3", true));
        section1.addRow(new IntegerDescriptor(TICK_INTERVAL_4, "Tick Interval 4", 25, 1, 1000, 1),
                new BooleanDescriptor(TICK_INTERVAL_ENABLED_4, "Use Tick Interval 4", true));

        var date = tab.addGroup("Date");
        date.addRow(new IntegerDescriptor(START_DATETIME, "Analysis History (Days ago)", 30, 1, 365, 1));
        date.addRow(new DateTimeDescriptor(END_DATETIME, "Analysis End Date", Instant.now().toEpochMilli(), true));

        return tab;
    }

    @Override
    public void onActivate(OrderContext ctx) {
        debug("SnapshotHistory_onActivate (low-memory, chunk per day)");
        long t0 = System.currentTimeMillis();

        int daysAgo = getSettings().getInteger(START_DATETIME);
        long endMillis = getSettings().getLong(END_DATETIME);
        Instant endInstant = Instant.ofEpochMilli(endMillis);
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate endAnalyseDay = endInstant.atZone(zoneId).toLocalDate();
        LocalDate startAnalyseDay = endAnalyseDay.minusDays(daysAgo);
        int durationDays = daysAgo + 1;

        // Intervalles de ticks actifs
        List<Integer> activeIntervals = new ArrayList<>();
        if (getSettings().getBoolean(TICK_INTERVAL_ENABLED_1))
            activeIntervals.add(getSettings().getInteger(TICK_INTERVAL_1));
        if (getSettings().getBoolean(TICK_INTERVAL_ENABLED_2))
            activeIntervals.add(getSettings().getInteger(TICK_INTERVAL_2));
        if (getSettings().getBoolean(TICK_INTERVAL_ENABLED_3))
            activeIntervals.add(getSettings().getInteger(TICK_INTERVAL_3));
        if (getSettings().getBoolean(TICK_INTERVAL_ENABLED_4))
            activeIntervals.add(getSettings().getInteger(TICK_INTERVAL_4));

        // Paramètres HiLo
        BarSize barSize1 = getSettings().getBarSize(TIMEFRAME_1);
        Enums.MAMethod maMethod1 = getSettings().getMAMethod(MA_METHOD_1);
        int period1 = getSettings().getInteger(PERIOD_1);

        BarSize barSize2 = getSettings().getBarSize(TIMEFRAME_2);
        Enums.MAMethod maMethod2 = getSettings().getMAMethod(MA_METHOD_2);
        int period2 = getSettings().getInteger(PERIOD_2);

        // Renko: 1 point = 4 ticks (NQ); renkoPoint param = nombre de ticks
        this.brickSize = getSettings().getInteger(RENKO_POINT) / 4.0;

        LocalDate today = LocalDate.now(zoneId);
        LocalDateTime now = LocalDateTime.now(zoneId);

        // === LOGIQUE LOW-MEMORY ===
        // On boucle par intervalle, mais on traite les journées UNE PAR UNE.
        // => Continuité Renko assurée sur l'ensemble de la plage (reset par intervalle
        // seulement).
        int totalsimulatedBars = 0;

        for (int tickInterval : activeIntervals) {
            activeTickInterval = tickInterval;

            // Reset Renko/historique pour l'intervalle entier (continuité sur tous les
            // jours).
            realRenko = null;
            previousClose = null;
            history = new SnapshotHistoryBufferManager();

            // Etats HiLo "prev" propagés jour->jour (un par timeframe)
            Double prevHiLoTF1 = null;
            Double prevHiLoTF2 = null;

            // Batch d'écriture DB
            List<RenkoSnapshot> renkoBatch = new ArrayList<>(1000);
            int totalsimulatedBarsByInterval = 0;

            for (int i = 0; i < durationDays; i++) {
                LocalDate analyseDay = startAnalyseDay.plusDays(i);
                LocalDateTime dayStart = LocalDateTime.of(analyseDay, LocalTime.MIDNIGHT);
                LocalDateTime dayEnd = analyseDay.equals(today) ? now
                        : LocalDateTime.of(analyseDay, LocalTime.of(23, 59, 59));
                long dayStartMillis = dayStart.atZone(zoneId).toInstant().toEpochMilli();
                long dayEndMillis = dayEnd.atZone(zoneId).toInstant().toEpochMilli();

                // 1) Calculer HiLo du jour (avec lookback et propagation prevHiLo)
                HiLoDayChunk hiloDay1 = computeHiLoForDay(ctx, analyseDay, zoneId, barSize1, maMethod1, period1,
                        prevHiLoTF1);
                HiLoDayChunk hiloDay2 = computeHiLoForDay(ctx, analyseDay, zoneId, barSize2, maMethod2, period2,
                        prevHiLoTF2);
                TreeMap<Long, HiLoResult> HiLoMap1 = hiloDay1.mapDay;
                TreeMap<Long, HiLoResult> HiLoMap2 = hiloDay2.mapDay;
                prevHiLoTF1 = hiloDay1.lastHiLo; // propagation pour le jour suivant
                prevHiLoTF2 = hiloDay2.lastHiLo;

                if (HiLoMap1 == null || HiLoMap1.isEmpty() || HiLoMap2 == null || HiLoMap2.isEmpty()) {
                    debug("Pas de HiLo suffisant pour " + analyseDay + " (TF1/TF2). On saute.");
                    continue;
                }

                // 2) Récup ticks du jour puis agrégation en bougies simulées
                List<Tick> dayTicks = getTicksSafe(ctx, dayStartMillis, dayEndMillis);
                if (dayTicks == null || dayTicks.size() < activeTickInterval) {
                    debug("Pas de ticks suffisants pour " + analyseDay + " @ interval=" + activeTickInterval);
                    continue;
                }

                List<SimulatedBar> simulatedBars = aggregateSimulatedBars(dayTicks, activeTickInterval);
                totalsimulatedBarsByInterval += simulatedBars.size();

                // 3) Traitement Renko + snapshot
                for (SimulatedBar bar : simulatedBars) {

                    HiLoResult r1 = HiLoManager.getLastHiLoBefore(HiLoMap1, bar.timestamp);
                    HiLoResult r2 = HiLoManager.getLastHiLoBefore(HiLoMap2, bar.timestamp);
                    if (r1 == null || r2 == null)
                        continue;

                    RenkoResult renko = calculateRenkoTickBased(bar.close, r1.hiLo, r2.hiLo);

                    if (renko.crossAbove || renko.crossBelow) {
                        if (history.renkoColorHistory[0] != null && history.hiLoColor1History[0] != null
                                && history.hiLoColor2History[0] != null && history.hiLoPrice1History[0] != null
                                && history.hiLoPrice2History[0] != null && history.renkoPriceResHistory[0] != null
                                && history.renkoPriceSupHistory[0] != null) {

                            String instrumentKey = Helper.extractInstrumentKeyGroup(ctx.getInstrument().getSymbol());
                            String instrumentName = ctx.getInstrument().getSymbol();
                            String intervalScale = "ticks";
                            int intervalValue = activeTickInterval;
                            double renkoSize = this.brickSize;
                            long timestamp = bar.timestamp;
                            String dateTime = Helper.localFormatDate(timestamp);
                            String updatedAt = Helper.isoNow();
                            double priceRes = history.renkoPriceResHistory[0];
                            double priceSup = history.renkoPriceSupHistory[0];
                            String renkoColor = history.renkoColorHistory[0];
                            String hiloColor1 = history.hiLoColor1History[0];
                            String hiloColor2 = history.hiLoColor2History[0];
                            double hiloPrice1 = history.hiLoPrice1History[0];
                            double hiloPrice2 = history.hiLoPrice2History[0];

                            renkoBatch.add(new RenkoSnapshot(instrumentKey, instrumentName, intervalScale,
                                    intervalValue, renkoSize, timestamp, dateTime, priceRes, priceSup, renkoColor,
                                    hiloColor1, hiloColor2, hiloPrice1, hiloPrice2, updatedAt));

                            if (renkoBatch.size() >= 500) {
                                try {
                                    dbManager.insertRenkoSnapshotsBatch(renkoBatch);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                renkoBatch.clear();
                            }
                        }
                    }
                }

                debug("Date : " + analyseDay + " -> Bougies simulées : " + simulatedBars.size() + " @ interval="
                        + activeTickInterval);
            } // fin boucle jours

            // Flush restant à la fin de l'intervalle
            if (!renkoBatch.isEmpty()) {
                try {
                    dbManager.insertRenkoSnapshotsBatch(renkoBatch);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                renkoBatch.clear();
            }

            debug("Interval terminé : " + tickInterval + " -> Total bougies simulées : "
                    + totalsimulatedBarsByInterval);
            totalsimulatedBars += totalsimulatedBarsByInterval;
        } // fin boucle intervalles

        long elapsed = System.currentTimeMillis() - t0;
        long minutes = elapsed / 60000;
        long seconds = (elapsed % 60000) / 1000;
        debug("Analyse réalisée en " + minutes + "m" + seconds + "s. Total bougies simulées : " + totalsimulatedBars);
        debug("Mise à jour SnapshotHistory terminée.");
    }

    // ================== UTILITAIRES ==================

    private static class HiLoDayChunk {
        final TreeMap<Long, HiLoResult> mapDay;
        final Double lastHiLo;

        HiLoDayChunk(TreeMap<Long, HiLoResult> mapDay, Double lastHiLo) {
            this.mapDay = mapDay;
            this.lastHiLo = lastHiLo;
        }
    }

    /**
     * Calcule le HiLo du JOUR (avec lookback jours avant), propage prevHiLo en
     * entrée, et renvoie: - mapDay: TreeMap ts->HiLoResult sur [jour 00:00 .. fin
     * du jour (ou now)] - lastHiLo: dernier HiLo atteint sur la période calculée
     * (pour propager au jour suivant)
     */
    private HiLoDayChunk computeHiLoForDay(OrderContext ctx, LocalDate day, ZoneId zoneId, BarSize barSize,
            Enums.MAMethod method, int period, Double prevHiLoIn) {
        if (barSize == null)
            return new HiLoDayChunk(new TreeMap<>(), prevHiLoIn);

        LocalDate today = LocalDate.now(zoneId);
        LocalDateTime now = LocalDateTime.now(zoneId);

        // Fenêtre temps: [dayStart - lookback .. dayEnd]
        int lookbackDays = HILO_LOOKBACK_DAYS_DEFAULT;
        LocalDate lookbackStartDay = day.minusDays(lookbackDays);

        long from = LocalDateTime.of(lookbackStartDay, LocalTime.MIDNIGHT).atZone(zoneId).toInstant().toEpochMilli();
        long to = (day.equals(today) ? now : LocalDateTime.of(day, LocalTime.of(23, 59, 59))).atZone(zoneId).toInstant()
                .toEpochMilli();

        List<Bar> bars = getBarsSafe(ctx, from, to, barSize);
        if (bars == null || bars.size() < period) {
            return new HiLoDayChunk(new TreeMap<>(), prevHiLoIn);
        }

        // Adaptation -> IBar
        List<IBar> adapted = new ArrayList<>(bars.size());
        for (Bar b : bars)
            adapted.add(new BarAdapter(b));

        // Calcul HiLo (avec propagation du prevHiLo)
        Map<Long, HiLoResult> full = hiloCore.computeHiLo(adapted, method, period, prevHiLoIn);
        if (full.isEmpty())
            return new HiLoDayChunk(new TreeMap<>(), prevHiLoIn);

        // Filtre: ne garder que les timestamps du JOUR (sans le lookback)
        long dayStartMillis = LocalDateTime.of(day, LocalTime.MIDNIGHT).atZone(zoneId).toInstant().toEpochMilli();
        long dayEndMillis = (day.equals(today) ? now : LocalDateTime.of(day, LocalTime.of(23, 59, 59))).atZone(zoneId)
                .toInstant().toEpochMilli();

        TreeMap<Long, HiLoResult> tm = new TreeMap<>();
        for (Map.Entry<Long, HiLoResult> e : full.entrySet()) {
            long ts = e.getKey();
            if (ts >= dayStartMillis && ts <= dayEndMillis) {
                tm.put(ts, e.getValue());
            }
        }

        // Déterminer le dernier HiLo (pour propager) = floor(dayEnd)
        Double lastHiLo = prevHiLoIn;
        Map.Entry<Long, HiLoResult> floor = new TreeMap<>(full).floorEntry(dayEndMillis);
        if (floor != null && floor.getValue() != null) {
            lastHiLo = floor.getValue().hiLo;
        }

        return new HiLoDayChunk(tm, lastHiLo);
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

    /** Agrège les ticks N par N en bougies simulées OHLC. */
    public List<SimulatedBar> aggregateSimulatedBars(List<Tick> ticks, int groupSize) {
        List<SimulatedBar> bars = new ArrayList<>();
        for (int i = 0; i <= ticks.size() - groupSize; i += groupSize) {
            double open = ticks.get(i).getPrice();
            double high = open;
            double low = open;
            double close = ticks.get(i + groupSize - 1).getPrice();
            long timestamp = ticks.get(i + groupSize - 1).getTime();

            for (int j = i; j < i + groupSize; j++) {
                double price = ticks.get(j).getPrice();
                if (price > high)
                    high = price;
                if (price < low)
                    low = price;
            }
            bars.add(new SimulatedBar(timestamp, open, high, low, close));
        }
        return bars;
    }

    /** Renko tick-based (identique à ta logique). */
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

            if (realRenko > previousClose)
                history.updateRenkoColorHistory(GREEN);
            else
                history.updateRenkoColorHistory(RED);
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
