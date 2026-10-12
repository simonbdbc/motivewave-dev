package sbdev.joy_ai_project.pattern_strategy_algo;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
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
import com.motivewave.platform.sdk.common.desc.StringDescriptor;
import com.motivewave.platform.sdk.common.desc.TifDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.order_mgmt.Order;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

import sbdev.joy_ai_project.pattern_strategy_algo.service.HiLoManagerLive; // Live wrapper (DataSeries -> IBar -> HiLoManager)
import sbdev.joy_ai_project.pattern_strategy_algo.model.HiLoResult; // Résultat HiLo (timestamp, hiLo, close, isHigh, isLow)

import sbdev.joy_ai_project.pattern_strategy_algo.service.StrategyHistoryBufferManager;
import sbdev.joy_ai_project.pattern_strategy_algo.util.StudyLogger;
import sbdev.joy_ai_project.pattern_strategy_algo.database.HistoryBufferPatternDbManagerV4;
import sbdev.joy_ai_project.pattern_strategy_algo.model.PatternRenkoMatched;
import sbdev.joy_ai_project.pattern_strategy_algo.PatternStrategyAlgo.Values;
import sbdev.joy_ai_project.pattern_strategy_algo.service.TradeManager;

/**
 * PatternStrategyAlgo — HiLo “idempotent + live” aligné SnapshotHistory
 *
 * - Idempotence: on calcule/fige le HiLo à chaque CLOSE de la bougie TF via
 * HiLoManagerLive (même logique que SnapshotHistory). - Live intrabar: entre 2
 * closes TF, si le PRIX COURANT croise prevHiLo, on bascule immédiatement le
 * NIVEAU HiLo au MA(high/low) du TF courant (calculé avec la bougie TF EN
 * COURS), et on met à jour PRIX + COULEUR + history. - Affichage: le chemin
 * HiLo sur le graphe est mis à jour à chaque tick (prix + couleur). - Snapshots
 * Renko: on pousse toujours l’état HiLo “live” (prix & couleur) au moment du
 * cross Renko.
 */
@StudyHeader(namespace = "com.sbdev", id = "PATTERN_STRATEGY_ALGO", name = "Pattern Strategy Algo", label = "Pattern Strategy Algo", desc = "Pattern Strategy Algo", menu = "SB Dev", overlay = true, signals = true, strategy = true, autoEntry = true, manualEntry = false, supportsStopPL = true, supportsUnrealizedPL = true, supportsRealizedPL = true, supportsTotalPL = true, supportsTargetPL = true, supportsRiskRatio = true, showTradeOptions = true, requiresBarUpdates = true)
public class PatternStrategyAlgo extends Study {

    public static final String STUDY_ID = "PATTERN_STRATEGY_ALGO";
    public final String instanceId = String.valueOf(System.identityHashCode(this));

    // --- Etats & services ---
    public StrategyHistoryBufferManager history = new StrategyHistoryBufferManager();
    private HistoryBufferPatternDbManagerV4 patternDbManager;
    private TradeManager tradeManager;
    private StudyLogger logger;

    // --- Renko (état) ---
    protected Double brickSize = null;
    protected Double previousClose = null;
    protected Double realRenko = null;
    protected String[] colorRenkoDisplay = new String[10];

    // --- HiLo core (mêmes algorithmes que SnapshotHistory) ---
    private final HiLoManagerLive hiloLive1 = new HiLoManagerLive();
    private final HiLoManagerLive hiloLive2 = new HiLoManagerLive();

    /**
     * Cache HiLo pour les BOUJIES TF CLOSE: key = tfEndTime, value = HiLoResult
     * (fermée).
     */
    private final Map<Long, HiLoResult> hiloCache1 = new HashMap<>();
    private final Map<Long, HiLoResult> hiloCache2 = new HashMap<>();

    /**
     * Etats HiLo "live" (prix + couleur) utilisés pour l'affichage et les snapshots
     * Renko.
     */
    private Double currentPriceHiLo1 = null;
    private Double currentPriceHiLo2 = null;
    private String currentColorHiLo1 = null; // "green" ou "red"
    private String currentColorHiLo2 = null; // "green" ou "red"

    // --- Constantes ---
    private static final String GREEN = "green";
    private static final String RED = "red";
    private static final String ABOVE = "above";
    private static final String BELOW = "below";
    private static final String LONG = "long";
    private static final String SHORT = "short";

    public boolean crossAbove = false;
    public boolean crossBelow = false;

    // --- Settings UI (inchangés) ---
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

    private static final String TRADE_LOTS = "tradeLots";
    private static final String TIME_IN_FORCE = "tif";
    private static final String BREAK_EVEN_ENABLED = "breakEvenEnabled";
    private static final String BREAK_EVEN = "breakEvenPips";
    private static final String COVER = "coverPips";
    private static final String ATR_PERIOD = "atrPeriod";
    private static final String ATR_MULTIPLIER = "atrMultiplier";
    private static final String ATR_ENABLED = "atrEnabled";
    private static final String HISTORY_BUFFER_CLEAR = "historyBufferClear";
    private static final String HISTORY_BUFFER_PURGE_DB = "historyBufferPurgeDb";

    private static final String SESSION_MAX_WIN = "sessionMaxWin";
    private static final String SESSION_MAX_LOSS = "sessionMaxLoss";
    private static final String MAX_SUCCESSIVE_LOSS_ENABLED = "maxSuccessiveLossEnabled";
    private static final String MAX_SUCCESSIVE_LOSS = "maxSuccessiveLoss";

    private static final String MARKET_CLOSE_HOUR = "marketCloseHour";
    private static final String MARKET_CLOSE_MINUTE = "marketCloseMinute";
    private static final String AUTO_CLOSE_BEFORE_MINUTES = "autoCloseBeforeMinute";

    private static final String AUTO_SESSION_DEFAULT_ENABLED = "autoSessionDefaultEnabled";
    private static final String AUTO_SESSION_TZ = "autoSessionTimeZone";
    private static final String AUTO_SESSION_START_HOUR = "autoSessionStartHour";
    private static final String AUTO_SESSION_START_MINUTE = "autoSessionStartMinute";
    private static final String AUTO_SESSION_END_HOUR = "autoSessionEndHour";
    private static final String AUTO_SESSION_END_MINUTE = "autoSessionEndMinute";

    enum Values {
        HIGH, LOW, CROSS, MA_HILO_1, MA_HILO_2, ADX, DX, PDM, NDM, TR
    }

    public enum Signals {
        LONG_MATCH, SHORT_MATCH, CROSS_ABOVE, CROSS_BELOW
    }

    protected double tickSize = 0.00;
    private boolean renkoEnabled, tfEnabled1, tfEnabled2, infoEnabled;

    private boolean defaultSessionActive = false;
    public boolean isStrategyActive = false;

    // =====================================================================================
    // INIT UI
    // =====================================================================================
    @Override
    public void initialize(Defaults defaults) {
        var sd = createSD();

        sd.addTab(initializeStrategyTab(defaults));
        sd.addTab(getRiskTab());
        sd.addTab(initializeGeneralTab(defaults));
        sd.addTab(initializeSessionDefaultsTab());

        sd.addQuickSettings(RENKO_POINT);
        sd.addQuickSettings(TOP_PATH, BOTTOM_PATH);

        var tab1 = sd.addTab(get("HiLo 1"));
        var grp1 = tab1.addGroup(get("First Timeframe"));
        grp1.addRow(new BooleanDescriptor(TF_ENABLED_1, "Use Timeframe 1", true));
        grp1.addRow(new BarSizeDescriptor(TIMEFRAME_1, "Timeframe 1", BarSize.getBarSize(1)));
        grp1.addRow(new MAMethodDescriptor(MA_METHOD_1, "MA Method 1", Enums.MAMethod.SMA));
        grp1.addRow(new IntegerDescriptor(PERIOD_1, "Period 1", 21, 1, 9999, 1));

        grp1 = tab1.addGroup(get("Display"));
        grp1.addRow(new PathDescriptor(PATH_HILO_1, "HiLo Path 1", null, 1.0f, null, true, true, false));
        grp1.addRow(new ColorDescriptor(HIGH_COLOR_1, "High Color 1", defaults.getRedLine()));
        grp1.addRow(new ColorDescriptor(LOW_COLOR_1, "Low Color 1", defaults.getGreenLine()));

        var tab2 = sd.addTab(get("HiLo 2"));
        var grp2 = tab2.addGroup(get("Second Timeframe"));
        grp2.addRow(new BooleanDescriptor(TF_ENABLED_2, "Use Timeframe 2", true));
        grp2.addRow(new BarSizeDescriptor(TIMEFRAME_2, "Timeframe 2", BarSize.getBarSize(5)));
        grp2.addRow(new MAMethodDescriptor(MA_METHOD_2, "MA Method 2", Enums.MAMethod.SMA));
        grp2.addRow(new IntegerDescriptor(PERIOD_2, "Period 2", 21, 1, 9999, 1));

        grp2 = tab2.addGroup(get("Display"));
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
        desc.declarePath(Values.MA_HILO_1, PATH_HILO_1);
        desc.declarePath(Values.MA_HILO_2, PATH_HILO_2);
        desc.declareSignal(Signals.CROSS_ABOVE, "PRICE_CROSS_ABOVE");
        desc.declareSignal(Signals.CROSS_BELOW, "PRICE_CROSS_BELOW");
        desc.declareSignal(Signals.LONG_MATCH, LONG);
        desc.declareSignal(Signals.SHORT_MATCH, SHORT);

        logger = StudyLogger.getInstance(STUDY_ID, instanceId);
        patternDbManager = new HistoryBufferPatternDbManagerV4(this, logger);
        tradeManager = new TradeManager(this, logger);
    }

    protected SettingTab initializeStrategyTab(Defaults defaults) {
        var tab = new SettingTab("Strategy");
        var grp = tab.addGroup("Entry");
        grp.addRow(new IntegerDescriptor(TRADE_LOTS, "Trade Lots", 1, 1, 9999, 1),
                new TifDescriptor(TIME_IN_FORCE, "Time in Force", Enums.TIF.GTC));

        grp = tab.addGroup("History Buffer");
        grp.addRow(new BooleanDescriptor(HISTORY_BUFFER_CLEAR, "Clear History Buffer", false));
        grp.addRow(new BooleanDescriptor(HISTORY_BUFFER_PURGE_DB, "Purge DB (for Replay Mode)", false));

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

    protected SettingTab initializeSessionDefaultsTab() {
        var tab = new SettingTab("Session Defaults");
        var grp = tab.addGroup("Auto Session (if no Trading Options session is used)");
        grp.addRow(new BooleanDescriptor(AUTO_SESSION_DEFAULT_ENABLED, "Enable Auto Session", true));
        grp.addRow(new StringDescriptor(AUTO_SESSION_TZ, "Time Zone (info)", "Europe/Paris"));
        grp.addRow(new IntegerDescriptor(AUTO_SESSION_START_HOUR, "Start Hour", 2, 0, 23, 1),
                new IntegerDescriptor(AUTO_SESSION_START_MINUTE, "Minute", 0, 0, 59, 1));
        grp.addRow(new IntegerDescriptor(AUTO_SESSION_END_HOUR, "End Hour", 22, 0, 23, 1),
                new IntegerDescriptor(AUTO_SESSION_END_MINUTE, "Minute", 0, 0, 59, 1));
        return tab;
    }

    // =====================================================================================
    // LIFE CYCLE / SETTINGS
    // =====================================================================================
    @Override
    public void onSettingsUpdated(DataContext ctx) {
        var settings = getSettings();
        if (settings == null)
            return;

        renkoEnabled = settings.getBoolean(RENKO_ENABLED);
        tfEnabled1 = settings.getBoolean(TF_ENABLED_1);
        tfEnabled2 = settings.getBoolean(TF_ENABLED_2);
        infoEnabled = settings.getBoolean(INFO_ENABLED);

        tickSize = ctx.getInstrument().getTickSize();

        if (settings.getBoolean(HISTORY_BUFFER_CLEAR)) {
            history.clearAllHistoryBuffers();
            logger.log("Clear History Buffers");
            settings.setBoolean(HISTORY_BUFFER_CLEAR, false);
        }
        if (settings.getBoolean(HISTORY_BUFFER_PURGE_DB)) {
            patternDbManager.resetHistoryTable();
            logger.log("Purge DB (for Replay Mode)");
            settings.setBoolean(HISTORY_BUFFER_PURGE_DB, false);
        }

        // Important : on réinitialise les caches/états HiLo pour éviter toute pollution
        hiloCache1.clear();
        hiloCache2.clear();
        currentPriceHiLo1 = currentPriceHiLo2 = null;
        currentColorHiLo1 = currentColorHiLo2 = null;
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

    // =====================================================================================
    // MAIN CALC LOOP
    // =====================================================================================
    @Override
    protected void calculate(int index, DataContext ctx) {
        var settings = getSettings();
        if (settings == null)
            return;

        // Cette étude opère sur une série TICK
        if (!ctx.getDataSeries().getBarSize().isTick())
            return;

        // HiLo TF1 et TF2 : idempotent à la close TF + live intrabar
        calculateHiLo1(index, ctx);
        calculateHiLo2(index, ctx);

        initialiseADX(index, ctx);

        if (renkoEnabled) {
            calculateRenko(index, ctx);
        }
    }

    // =====================================================================================
    // HILO — TIMEFRAME 1
    // =====================================================================================
    private void calculateHiLo1(int index, DataContext ctx) {
        var settings = getSettings();
        if (!tfEnabled1)
            return;

        int period = settings.getInteger(PERIOD_1);
        var tfBS = settings.getBarSize(TIMEFRAME_1);
        var tf = ctx.getDataSeries(tfBS);
        if (tf == null || tf.size() <= period)
            return;

        long tickTs = ctx.getDataSeries().getEndTime(index);
        int tfIdx = tf.findIndex(tickTs);
        if (tfIdx < 0 || tfIdx < period - 1)
            return;

        var method = settings.getMAMethod(MA_METHOD_1);

        // 1) Si la bougie TF est CLOSE → on fige le HiLo avec HiLoManagerLive
        // (idempotent)
        if (tf.isBarComplete(tfIdx)) {
            HiLoResult closed = hiloCache1.get(tf.getEndTime(tfIdx));
            if (closed == null) {
                closed = hiloLive1.computeHiLo(tf, tfIdx, method, period);
                if (closed != null)
                    hiloCache1.put(tf.getEndTime(tfIdx), closed);
            }
            if (closed == null)
                return;

            // Affiche et fixe l'état courant (prix & couleur par rapport au prix actuel)
            Double curr = ctx.getDataSeries().getDouble(index, Enums.BarInput.CLOSE);
            applyLiveHiLoToSeries(ctx, index, /* isTF1 */true, closed.hiLo, decideColor(curr, closed.hiLo), method,
                    period, tf, tfIdx);
            return;
        }

        // 2) Bougie TF en COURS → live intrabar:
        // on lit prevHiLo (dernier TF close), on calcule MA(high/low) @ tfIdx, on
        // décide le prochain niveau.
        int prevCloseIdx = tfIdx - 1;
        if (prevCloseIdx < period - 1)
            return;

        HiLoResult prevClosed = hiloCache1.get(tf.getEndTime(prevCloseIdx));
        if (prevClosed == null) {
            prevClosed = hiloLive1.computeHiLo(tf, prevCloseIdx, method, period);
            if (prevClosed != null)
                hiloCache1.put(tf.getEndTime(prevCloseIdx), prevClosed);
        }
        if (prevClosed == null || prevClosed.hiLo == null)
            return;

        Double prevHiLo = prevClosed.hiLo;

        // MA(high/low) calculées AU TF INDEX COURANT (inclut la bougie en formation)
        Double mHigh = maAt(tf, tfIdx, period, method, Enums.BarInput.HIGH);
        Double mLow = maAt(tf, tfIdx, period, method, Enums.BarInput.LOW);
        if (mHigh == null || mLow == null)
            return;

        Double curr = ctx.getDataSeries().getDouble(index, Enums.BarInput.CLOSE);
        if (curr == null)
            return;

        // Décision "live": comme HiLoManager (comparaison au PREV HiLo)
        Double nextHiLo = prevHiLo;
        if (curr > prevHiLo)
            nextHiLo = mLow;
        else if (curr < prevHiLo)
            nextHiLo = mHigh;

        // Affiche & mémorise (prix + couleur)
        applyLiveHiLoToSeries(ctx, index, /* isTF1 */true, nextHiLo, decideColor(curr, nextHiLo), method, period, tf,
                tfIdx);
    }

    // =====================================================================================
    // HILO — TIMEFRAME 2
    // =====================================================================================
    private void calculateHiLo2(int index, DataContext ctx) {
        var settings = getSettings();
        if (!tfEnabled2)
            return;

        int period = settings.getInteger(PERIOD_2);
        var tfBS = settings.getBarSize(TIMEFRAME_2);
        var tf = ctx.getDataSeries(tfBS);
        if (tf == null || tf.size() <= period)
            return;

        long tickTs = ctx.getDataSeries().getEndTime(index);
        int tfIdx = tf.findIndex(tickTs);
        if (tfIdx < 0 || tfIdx < period - 1)
            return;

        var method = settings.getMAMethod(MA_METHOD_2);

        if (tf.isBarComplete(tfIdx)) {
            HiLoResult closed = hiloCache2.get(tf.getEndTime(tfIdx));
            if (closed == null) {
                closed = hiloLive2.computeHiLo(tf, tfIdx, method, period);
                if (closed != null)
                    hiloCache2.put(tf.getEndTime(tfIdx), closed);
            }
            if (closed == null)
                return;

            Double curr = ctx.getDataSeries().getDouble(index, Enums.BarInput.CLOSE);
            applyLiveHiLoToSeries(ctx, index, /* isTF1 */false, closed.hiLo, decideColor(curr, closed.hiLo), method,
                    period, tf, tfIdx);
            return;
        }

        int prevCloseIdx = tfIdx - 1;
        if (prevCloseIdx < period - 1)
            return;

        HiLoResult prevClosed = hiloCache2.get(tf.getEndTime(prevCloseIdx));
        if (prevClosed == null) {
            prevClosed = hiloLive2.computeHiLo(tf, prevCloseIdx, method, period);
            if (prevClosed != null)
                hiloCache2.put(tf.getEndTime(prevCloseIdx), prevClosed);
        }
        if (prevClosed == null || prevClosed.hiLo == null)
            return;

        Double prevHiLo = prevClosed.hiLo;

        Double mHigh = maAt(tf, tfIdx, period, method, Enums.BarInput.HIGH);
        Double mLow = maAt(tf, tfIdx, period, method, Enums.BarInput.LOW);
        if (mHigh == null || mLow == null)
            return;

        Double curr = ctx.getDataSeries().getDouble(index, Enums.BarInput.CLOSE);
        if (curr == null)
            return;

        Double nextHiLo = prevHiLo;
        if (curr > prevHiLo)
            nextHiLo = mLow;
        else if (curr < prevHiLo)
            nextHiLo = mHigh;

        applyLiveHiLoToSeries(ctx, index, /* isTF1 */false, nextHiLo, decideColor(curr, nextHiLo), method, period, tf,
                tfIdx);
    }

    /**
     * Applique le HiLo "live" (prix ET couleur) au chart et mémorise l'état
     * courant. - Ecrit la valeur (setDouble) pour dessiner la ligne. - Applique la
     * couleur au path. - Met à jour currentPriceHiLo* et currentColorHiLo*
     * (utilisés par les snapshots Renko).
     */
    private void applyLiveHiLoToSeries(DataContext ctx, int index, boolean isTF1, Double hiLoValue, String colorName,
            Enums.MAMethod method, int period, com.motivewave.platform.sdk.common.DataSeries tf, int tfIdx) {
        var settings = getSettings();
        var series = ctx.getDataSeries();

        if (isTF1) {
            series.setDouble(index, Values.MA_HILO_1, hiLoValue);
        } else {
            series.setDouble(index, Values.MA_HILO_2, hiLoValue);
        }

        Color pathColor = settings.getColor(isTF1 ? (GREEN.equals(colorName) ? LOW_COLOR_1 : HIGH_COLOR_1)
                : (GREEN.equals(colorName) ? LOW_COLOR_2 : HIGH_COLOR_2));

        if (isTF1) {
            series.setPathColor(index, Values.MA_HILO_1, pathColor);
            currentPriceHiLo1 = hiLoValue;
            currentColorHiLo1 = colorName;
        } else {
            series.setPathColor(index, Values.MA_HILO_2, pathColor);
            currentPriceHiLo2 = hiLoValue;
            currentColorHiLo2 = colorName;
        }

        series.setComplete(index);
    }

    /** Décide la couleur: prix courant vs niveau HiLo live. */
    private static String decideColor(Double curr, Double hiLo) {
        if (curr == null || hiLo == null)
            return GREEN; // défaut
        return (curr < hiLo) ? RED : GREEN;
    }

    /**
     * MA à l'index donné pour un input (HIGH/LOW) et une méthode donnée. Utilise
     * les méthodes natives DataSeries lorsque disponibles (SMA/EMA/WMA/SMMA).
     * Retourne null si insuffisant (index < period-1).
     */
    private Double maAt(com.motivewave.platform.sdk.common.DataSeries ds, int index, int period, Enums.MAMethod method,
            Enums.BarInput input) {
        if (index < period - 1)
            return null;
        try {
            return switch (method) {
            case SMA -> ds.sma(index, period, input);
            case EMA -> ds.ema(index, period, input);
            case WMA -> ds.wma(index, period, input);
            case SMMA -> ds.smma(index, period, input);
            default -> null;
            };
        } catch (Throwable t) {
            // Au besoin, on peut implémenter un fallback manuel ici.
            return null;
        }
    }

    // =====================================================================================
    // ADX (inchangé)
    // =====================================================================================
    protected void initialiseADX(int index, DataContext ctx) {
        var seriesADX = ctx.getDataSeries();
        int period = 14;
        if (seriesADX == null || seriesADX.size() <= period)
            return;

        Float pDM = seriesADX.getPositiveDM(index);
        Float nDM = seriesADX.getNegativeDM(index);
        Float tr = seriesADX.getTrueRange(index);

        seriesADX.setFloat(index, Values.PDM, pDM);
        seriesADX.setFloat(index, Values.NDM, nDM);
        seriesADX.setFloat(index, Values.TR, tr);

        if (index <= period)
            return;

        Double PDMa = seriesADX.smma(index, period, Values.PDM);
        Double NDMa = seriesADX.smma(index, period, Values.NDM);
        Double TRa = seriesADX.smma(index, period, Values.TR);
        if (PDMa == null || NDMa == null || TRa == null)
            return;

        double DX = Math.abs((PDMa - NDMa)) / (PDMa + NDMa) * 100;
        seriesADX.setDouble(index, Values.DX, DX);

        Double ADX = seriesADX.smma(index, period, Values.DX);
        if (ADX == null)
            return;

        seriesADX.setDouble(index, Values.ADX, ADX);
        seriesADX.setComplete(index);
    }

    // =====================================================================================
    // RENKO (inchangé fonctionnellement; snapshots utilisent états HiLo "live")
    // =====================================================================================
    private void calculateRenko(int index, DataContext ctx) {
        var settings = getSettings();
        var series = ctx.getDataSeries();
        if (index == 0)
            return;
        if (!series.isBarComplete(index))
            return;

        double tickSize = ctx.getInstrument().getTickSize();
        int renkoTicks = settings.getInteger(RENKO_POINT);
        double renkoPoints = renkoTicks * tickSize;

        Double prevClose = series.getDouble(index - 1, Enums.BarInput.CLOSE);
        Double currClose = series.getDouble(index, Enums.BarInput.CLOSE);
        Long timestamp = series.getEndTime(index);
        if (prevClose == null || currClose == null)
            return;

        crossAbove = false;
        crossBelow = false;
        Double cross = null;
        Double priceRes = null, priceSup = null;

        if (realRenko == null) {
            double lowerBound = Math.floor(currClose / renkoPoints) * renkoPoints;
            double upperBound = lowerBound + renkoPoints;
            if (currClose > lowerBound + renkoPoints) {
                lowerBound += renkoPoints;
                upperBound = lowerBound + renkoPoints;
            }
            realRenko = upperBound;
            previousClose = lowerBound;
            brickSize = Math.abs(realRenko - previousClose);
            if (realRenko > previousClose) {
                updateColorHistory(GREEN);
                updateHistoryBufferAfterCross(ctx, timestamp, GREEN, realRenko, previousClose);
            } else {
                updateColorHistory(RED);
                updateHistoryBufferAfterCross(ctx, timestamp, RED, realRenko, previousClose);
            }
        } else {
            if (GREEN.equals(colorRenkoDisplay[0])) {
                if (currClose - realRenko >= brickSize) {
                    realRenko += Math.floor((currClose - realRenko) / brickSize) * brickSize;
                    previousClose = realRenko - brickSize;
                    crossAbove = true;
                    cross = 1.0;
                    updateColorHistory(GREEN);
                    updateHistoryBufferAfterCross(ctx, timestamp, GREEN, realRenko, previousClose);
                } else if (realRenko - currClose >= brickSize * 2) {
                    realRenko -= Math.floor((realRenko - currClose) / brickSize) * brickSize;
                    previousClose = realRenko + brickSize;
                    crossBelow = true;
                    cross = -1.0;
                    updateColorHistory(RED);
                    updateHistoryBufferAfterCross(ctx, timestamp, RED, realRenko, previousClose);
                }
            } else if (RED.equals(colorRenkoDisplay[0])) {
                if (currClose - realRenko >= brickSize * 2) {
                    realRenko += Math.floor((currClose - realRenko) / brickSize) * brickSize;
                    previousClose = realRenko - brickSize;
                    crossAbove = true;
                    cross = 1.0;
                    updateColorHistory(GREEN);
                    updateHistoryBufferAfterCross(ctx, timestamp, GREEN, realRenko, previousClose);
                } else if (realRenko - currClose >= brickSize) {
                    realRenko -= Math.floor((realRenko - currClose) / brickSize) * brickSize;
                    previousClose = realRenko + brickSize;
                    crossBelow = true;
                    cross = -1.0;
                    updateColorHistory(RED);
                    updateHistoryBufferAfterCross(ctx, timestamp, RED, realRenko, previousClose);
                }
            }
        }

        if (priceRes == null)
            priceRes = realRenko;
        if (priceSup == null)
            priceSup = previousClose;

        Color brickColor = GREEN.equals(colorRenkoDisplay[0]) ? settings.getColor(UP_COLOR)
                : settings.getColor(DOWN_COLOR);
        series.setPathColor(index - 1, Values.LOW, brickColor);
        series.setPathColor(index - 1, Values.HIGH, brickColor);
        series.setDouble(index - 1, Values.LOW, priceSup);
        series.setDouble(index, Values.HIGH, priceRes);
        series.setDouble(index, Values.CROSS, cross);

        if (settings.getBoolean(INFO_ENABLED)) {
            var infoMarker = settings.getMarker(INFO_MARKER);
            for (var entry : history.getAllEntriesForBuffer("renkoPriceResHistory").entrySet()) {
                addFigure(new Marker(new Coordinate(entry.getKey(), (double) entry.getValue()),
                        history.getRenkoColorAt(entry.getKey()).equals(GREEN) ? Enums.Position.TOP
                                : Enums.Position.BOTTOM,
                        infoMarker, history.toString(entry.getKey(), "")));
            }
        }

        if (series.isBarComplete(index))
            series.setComplete(index);
    }

    /**
     * Au moment d'un cross Renko, on pousse l’instantané en history en utilisant
     * les ETATS HILO "LIVE" (prix + couleur) calculés ci-dessus.
     */
    private void updateHistoryBufferAfterCross(DataContext ctx, Long timestamp, String color, double renkoRes,
            double renkoSup) {
        boolean isNew = history.updateCrossHistory(timestamp, color.equals(GREEN) ? ABOVE : BELOW);
        if (!isNew)
            return;
        if (!(isNew = history.updateRenkoColorHistory(timestamp, color)))
            return;
        if (!(isNew = history.updateRenkoPriceResHistory(timestamp, renkoRes)))
            return;
        if (!(isNew = history.updateRenkoPriceSupHistory(timestamp, renkoSup)))
            return;

        // Etats HiLo courants "live" (prix + couleurs) — c'est le point clé de
        // l’alignement demandé
        if (!(isNew = history.updateHiLoColor1History(timestamp, currentColorHiLo1)))
            return;
        if (!(isNew = history.updateHiLoColor2History(timestamp, currentColorHiLo2)))
            return;
        if (!(isNew = history.updateHiLoPrice1History(timestamp, currentPriceHiLo1)))
            return;
        if (!(isNew = history.updateHiLoPrice2History(timestamp, currentPriceHiLo2)))
            return;

        if (isNew && brickSize != null) {
            String instrumentName = ctx.getInstrument().getSymbol();
            String tfScale = "ticks";
            int tfInterval = ctx.getDataSeries().getBarSize().getInterval();
            String patternRenko = history.getLastRenkoPattern(8);
            String patternHiLo1 = history.getLastHiLo1Pattern(8);
            String patternHiLo2 = history.getLastHiLo2Pattern(8);
            patternDbManager.saveSnapshotAndFindMatchPattern(instrumentName, tfScale, tfInterval, brickSize,
                    patternRenko, patternHiLo1, patternHiLo2, timestamp);
        }
    }

    private void updateColorHistory(String newColor) {
        for (int i = colorRenkoDisplay.length - 1; i > 0; i--) {
            colorRenkoDisplay[i] = colorRenkoDisplay[i - 1];
        }
        colorRenkoDisplay[0] = newColor;
    }

    // =====================================================================================
    // LIFECYCLE
    // =====================================================================================
    @Override
    public void destroy() {
        super.destroy();
        if (logger != null)
            logger.close();
    }

    @Override
    public void onActivate(OrderContext ctx) {
        logger.log("onActivate");
        isStrategyActive = true;

        if (getSettings().getBoolean(AUTO_SESSION_DEFAULT_ENABLED)) {
            boolean inside = isWithinDefaultSession(nowParis());
            defaultSessionActive = inside;
            if (inside) {
                tradeManager.onSessionStarted(ctx, null);
                hiloCache1.clear();
                hiloCache2.clear();
                currentPriceHiLo1 = currentPriceHiLo2 = null;
                currentColorHiLo1 = currentColorHiLo2 = null;
            }
        }

        Double totalPnl = ctx.getTotalRealizedPnL();
        int max = getSettings().getInteger(SESSION_MAX_WIN);
        int min = getSettings().getInteger(SESSION_MAX_LOSS) * -1;
        logger.log("Risk Management -> TotalPnL=" + totalPnl + ", SessionMin=" + min + ", SessionMax=" + max);
    }

    @Override
    public void onDeactivate(OrderContext ctx) {
        super.onDeactivate(ctx);
        if (defaultSessionActive) {
            tradeManager.onSessionEnded(ctx, null);
            defaultSessionActive = false;
        }
        isStrategyActive = false;
    }

    public void onSessionStarted(OrderContext ctx, TimeFrame session) {
        tradeManager.onSessionStarted(ctx, session);
        hiloCache1.clear();
        hiloCache2.clear();
        currentPriceHiLo1 = currentPriceHiLo2 = null;
        currentColorHiLo1 = currentColorHiLo2 = null;
    }

    @Override
    public void onSessionEnded(OrderContext ctx, TimeFrame session) {
        tradeManager.onSessionEnded(ctx, session);
        super.onSessionEnded(ctx, session);
    }

    @Override
    public void onBarClose(OrderContext ctx) {
        tradeManager.onBarClose(ctx);
        tradeManager.securityStopIfNeeded(ctx);

        if (getSettings().getBoolean(AUTO_SESSION_DEFAULT_ENABLED)) {
            boolean inside = isWithinDefaultSession(nowParis());
            if (inside && !defaultSessionActive) {
                tradeManager.onSessionStarted(ctx, null);
                defaultSessionActive = true;
                logger.log("Auto Session: START (default window)");
                hiloCache1.clear();
                hiloCache2.clear();
                currentPriceHiLo1 = currentPriceHiLo2 = null;
                currentColorHiLo1 = currentColorHiLo2 = null;
            } else if (!inside && defaultSessionActive) {
                tradeManager.onSessionEnded(ctx, null);
                defaultSessionActive = false;
                logger.log("Auto Session: END (default window)");
            }
        }

        Date now = new Date(ctx.getCurrentTime());
        if (ctx.getPositionAsFloat() != 0 && isCloseToMarketClose(now)) {
            logger.log("Clôture auto position avant market close ("
                    + getSettings().getInteger(AUTO_CLOSE_BEFORE_MINUTES) + " min).");
            ctx.closeAtMarket();
        }
    }

    @Override
    public void onOrderFilled(OrderContext ctx, Order order) {
        tradeManager.onOrderFilled(ctx, order);
    }

    @Override
    public void onOrderCancelled(OrderContext ctx, Order order) {
        tradeManager.onOrderCancelled(ctx, order);
    }

    @Override
    public void onPositionClosed(OrderContext ctx) {
        tradeManager.onPositionClosed(ctx);
    }

    @Override
    public void onReset(OrderContext ctx) {
        ctx.cancelOrders();
        tradeManager.resetSessionState();
        super.onReset(ctx);
        isStrategyActive = false;
    }

    public void onPatternMatched(PatternRenkoMatched match) {
        tradeManager.handlePatternMatched(match);
    }

    // =====================================================================================
    // HELPERS (session/time)
    // =====================================================================================
    private Date nowParis() {
        return new Date();
    }

    private boolean isWithinDefaultSession(Date now) {
        var tz = TimeZone.getTimeZone("Europe/Paris");

        int startHour = getSettings().getInteger(AUTO_SESSION_START_HOUR);
        int startMinute = getSettings().getInteger(AUTO_SESSION_START_MINUTE);
        int endHour = getSettings().getInteger(AUTO_SESSION_END_HOUR);
        int endMinute = getSettings().getInteger(AUTO_SESSION_END_MINUTE);

        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(now);
        int h = cal.get(Calendar.HOUR_OF_DAY);
        int m = cal.get(Calendar.MINUTE);

        int t = h * 60 + m;
        int start = startHour * 60 + startMinute;
        int end = endHour * 60 + endMinute;

        if (start <= end)
            return t >= start && t < end;
        else
            return t >= start || t < end;
    }

    private boolean isCloseToMarketClose(Date currentDate) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
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
        return (hour > targetHour) || (hour == targetHour && minute >= targetMinute);
    }
}
