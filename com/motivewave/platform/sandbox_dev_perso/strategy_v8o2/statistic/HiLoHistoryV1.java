package sbdev.joy_ai_project.strategy_v8o3.statistic;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.common.desc.*;
import com.motivewave.platform.sdk.study.*;

import sbdev.joy_ai_project.strategy_v8o3.database.DatabaseInitializer;
import sbdev.joy_ai_project.strategy_v8o3.database.DatabaseManager;
import sbdev.joy_ai_project.strategy_v8o3.database.HiLoDbManager;

import java.sql.Connection;

@StudyHeader(namespace = "com.sbdev", id = "HILO_HISTORY_V1", name = "HiLo History V1", label = "HiLo History V1", desc = "Gann HiLo multi-timeframe", menu = "SB Dev", overlay = true, studyOverlay = true)
public class HiLoHistoryV1 extends Study {

    private HiLoDbManager hiLoDbManager;

    final static String GREEN = "green";
    final static String RED = "red";

    protected Double hilo1 = null;
    protected Double hilo2 = null;

    // Dernière valeur sauvée pour ne pas dupliquer (aussi en RAM pour robustesse)
    private Double lastSavedHilo1 = null;
    private String lastSavedColor1 = null;
    private Double lastSavedHilo2 = null;
    private String lastSavedColor2 = null;

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

    @Override
    public void initialize(Defaults defaults) {

        // (inutile de réappeler DatabaseInitializer ici, c'est déjà fait dans le
        // singleton)
        hiLoDbManager = new HiLoDbManager();

        var sd = createSD();

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

        // Déclaration plots
        var desc = createRD();
        desc.declarePath(Values.MA_HILO_1, PATH_HILO_1);
        desc.declarePath(Values.MA_HILO_2, PATH_HILO_2);
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
        var series = ctx.getDataSeries();
        double close = series.getClose(index);
        calculateHiLo1(index, ctx, close);
        calculateHiLo2(index, ctx, close);
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
        Double valueToSave = null;
        String colorToSave = null;

        if (close < maLow1) {
            series.setDouble(index, Values.MA_HILO_1, maHigh1);
            series.setPathColor(index, Values.MA_HILO_1, settings.getColor(HIGH_COLOR_1));
            hilo1 = maHigh1;
            valueToSave = maHigh1;
            colorToSave = RED;
        } else if (close > maHigh1) {
            series.setDouble(index, Values.MA_HILO_1, maLow1);
            series.setPathColor(index, Values.MA_HILO_1, settings.getColor(LOW_COLOR_1));
            hilo1 = maLow1;
            valueToSave = maLow1;
            colorToSave = GREEN;
        }
        series.setComplete(index);

        // FIFO/BDD via hiLoDbManager, anti-duplication en RAM + DB
        if (valueToSave != null && (!valueToSave.equals(lastSavedHilo1) || !colorToSave.equals(lastSavedColor1))) {
            if (hiLoDbManager != null) {
                hiLoDbManager.saveHiloValue(ctx.getInstrument().getSymbol(), "hilo1", valueToSave, colorToSave, index,
                        time, settings.getBarSize(TIMEFRAME_1).getInterval());
            }
            lastSavedHilo1 = valueToSave;
            lastSavedColor1 = colorToSave;
        }
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
        Double valueToSave = null;
        String colorToSave = null;

        if (close < maLow2) {
            series.setDouble(index, Values.MA_HILO_2, maHigh2);
            series.setPathColor(index, Values.MA_HILO_2, settings.getColor(HIGH_COLOR_2));
            hilo2 = maHigh2;
            valueToSave = maHigh2;
            colorToSave = RED;
        } else if (close > maHigh2) {
            series.setDouble(index, Values.MA_HILO_2, maLow2);
            series.setPathColor(index, Values.MA_HILO_2, settings.getColor(LOW_COLOR_2));
            hilo2 = maLow2;
            valueToSave = maLow2;
            colorToSave = GREEN;
        }
        series.setComplete(index);

        if (valueToSave != null && (!valueToSave.equals(lastSavedHilo2) || !colorToSave.equals(lastSavedColor2))) {
            if (hiLoDbManager != null) {
                hiLoDbManager.saveHiloValue(ctx.getInstrument().getSymbol(), "hilo2", valueToSave, colorToSave, index,
                        time, settings.getBarSize(TIMEFRAME_1).getInterval());
            }
            lastSavedHilo2 = valueToSave;
            lastSavedColor2 = colorToSave;
        }
    }
}
