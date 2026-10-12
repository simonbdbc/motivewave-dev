package sbdev.joy_ai_project.strategy.service.history;

import java.util.HashMap;
import java.util.Map;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.common.desc.*;
import com.motivewave.platform.sdk.study.*;

import sbdev.joy_ai_project.strategy.database.HistoryHiLoDbManager;
import sbdev.joy_ai_project.strategy.model.HiLoResult;
import sbdev.joy_ai_project.strategy.util.HiLoManagerLive;

@StudyHeader(namespace = "com.sbdev", id = "HILO_HISTORY", name = "HiLo History", label = "HiLo History", desc = "HiLo History", menu = "SB Dev", overlay = true)
public class HistoryHiLo extends Study {

    private HiLoManagerLive hiloLive1 = new HiLoManagerLive();
    private HiLoManagerLive hiloLive2 = new HiLoManagerLive();
    private HistoryHiLoDbManager historyHiLoDbManager;

    protected final static String HILO1 = "hilo1";
    protected final static String HILO2 = "hilo2";

    protected final static String GREEN = "green";
    protected final static String RED = "red";

    protected Double hilo1 = null;
    protected Double hilo2 = null;

    // Dernière valeur sauvée pour ne pas dupliquer (aussi en RAM pour robustesse)
    private String lastSavedRenkoKey = null;
    protected Double currentPriceHiLo1 = null;
    protected Double currentPriceHiLo2 = null;
    protected String currentColorHiLo1 = null;
    protected String currentColorHiLo2 = null;

    private Map<Long, HiLoResult> hiloCache1 = new HashMap<>();
    private Map<Long, HiLoResult> hiloCache2 = new HashMap<>();

    enum Values {
        MA_HILO_1, MA_HILO_2,
    }

    // Paramètres UI
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

    private static final String RESET_TABLE = "resetTable";

    @Override
    public void initialize(Defaults defaults) {

        // (inutile de réappeler DatabaseInitializer ici, c'est déjà fait dans le
        // singleton)
        historyHiLoDbManager = new HistoryHiLoDbManager();

        var sd = createSD();

        sd.addTab(initializeStrategyTab(defaults));

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

        // Quick Settings (Tool Bar and Popup Editor)
        sd.addQuickSettings(TIMEFRAME_1);
        sd.addQuickSettings(TIMEFRAME_2);

        // Déclaration plots
        var desc = createRD();
        desc.declarePath(Values.MA_HILO_1, PATH_HILO_1);
        desc.declarePath(Values.MA_HILO_2, PATH_HILO_2);

    }

    protected SettingTab initializeStrategyTab(Defaults defaults) {
        var tab = new SettingTab("Strategy");

        // Section 1
        var grp = tab.addGroup("Database");
        grp.addRow(new BooleanDescriptor(RESET_TABLE, " Reset Table", false));

        return tab;
    }

    @Override
    public void onSettingsUpdated(DataContext ctx) {
        var settings = getSettings();
        if (settings == null)
            return;

        if (settings.getBoolean(RESET_TABLE)) {
            HistoryHiLoDbManager.resetHistoryTable();
            debug("Reset Table : history_hilo");
            settings.setBoolean(RESET_TABLE, false);
        }
    }

    @Override
    public int getMinBars() {
        int p1 = getSettings().getInteger(PERIOD_1);
        int p2 = getSettings().getInteger(PERIOD_2);
        return Math.max(p1, p2) * 2;
    }

    @Override
    protected void calculate(int index, DataContext ctx) {
        var settings = getSettings();
        if (settings == null)
            return;

        calculateHiLo1(index, ctx);
        calculateHiLo2(index, ctx);
    }

    private void calculateHiLo1(int index, DataContext ctx) {
        var settings = getSettings();

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
            saveHiLo(index, ctx, HILO1, currentPriceHiLo1, currentColorHiLo1,
                    settings.getBarSize(TIMEFRAME_1).getInterval());
        } else if (Boolean.TRUE.equals(hilo.isHigh)) {
            currentPriceHiLo1 = hilo.hiLo;
            series.setDouble(index, Values.MA_HILO_1, hilo.hiLo);
            currentColorHiLo1 = RED;
            series.setPathColor(index, Values.MA_HILO_1, settings.getColor(HIGH_COLOR_1));
            saveHiLo(index, ctx, HILO1, currentPriceHiLo1, currentColorHiLo1,
                    settings.getBarSize(TIMEFRAME_1).getInterval());
        }
        series.setComplete(index);
    }

    private void calculateHiLo2(int index, DataContext ctx) {
        var settings = getSettings();

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
            saveHiLo(index, ctx, HILO2, currentPriceHiLo2, currentColorHiLo2,
                    settings.getBarSize(TIMEFRAME_2).getInterval());
        } else if (Boolean.TRUE.equals(hilo.isHigh)) {
            currentPriceHiLo2 = hilo.hiLo;
            series.setDouble(index, Values.MA_HILO_2, hilo.hiLo);
            currentColorHiLo2 = RED;
            series.setPathColor(index, Values.MA_HILO_2, settings.getColor(HIGH_COLOR_2));
            saveHiLo(index, ctx, HILO2, currentPriceHiLo2, currentColorHiLo2,
                    settings.getBarSize(TIMEFRAME_2).getInterval());
        }
        series.setComplete(index);
    }

    private void saveHiLo(int index, DataContext ctx, String hiloType, Double price, String color, int tfMinutes) {
        // FIFO/BDD via renkoHistoryDbManager, anti-duplication en RAM + DB
        String instrument = ctx.getInstrument().getSymbol();
        long timestamp = ctx.getDataSeries().getEndTime(index);
        long minutesInMillis = tfMinutes * 60 * 1000;
        long roundedMinTimestamp = (timestamp / minutesInMillis) * minutesInMillis;

        String key = instrument + "|" + hiloType + "|" + roundedMinTimestamp;

        if (key != null && !key.equals(lastSavedRenkoKey)) {
            if (historyHiLoDbManager != null) {
                // debug(instrument + " - " + interval + " - " + priceRes + " - " + priceSup + "
                // - " + color + " - "
                // + index + " - " + timestamp + " - " + new Date(timestamp));
                historyHiLoDbManager.saveSnapshot(instrument, hiloType, price, color, roundedMinTimestamp, timestamp);

                lastSavedRenkoKey = key;
            }
        }
    }
}
