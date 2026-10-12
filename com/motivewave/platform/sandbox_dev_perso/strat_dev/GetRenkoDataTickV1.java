package sandbox.strat_dev;

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
import com.motivewave.platform.sdk.draw.*;
import com.motivewave.platform.sdk.study.*;

// import sbdev.joy_ai_project.strategy_v8o3.core_pattern.CorePattern;
import sbdev.joy_ai_project.strategy_v8o3.database.DatabaseInitializer;
import sbdev.joy_ai_project.strategy_v8o3.database.StatisticDbManager;
import sbdev.joy_ai_project.strategy_v8o3.util.HistoryManager;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternCheckResult;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.RenkoResult;

import java.awt.Color;
import java.util.List;

@StudyHeader(namespace = "com.sbdev", id = "GET_RENKO_DATA_TICK_V1", name = "Get Renko Data Tick V1", label = "Get Renko Data Tick V1", desc = "Displays a Renko overlay with ADX, Gann HiLo multi-timeframe, and strategy signals", menu = "Sandbox", overlay = true, studyOverlay = false)
public class GetRenkoDataTickV1 extends Study {

    protected HistoryManager history = new HistoryManager();
    private final StatisticDbManager dbManager = new StatisticDbManager();

    protected Double brickSize = null; // Taille de la brique
    protected Double previousClose = null; // Renko open
    protected Double realRenko = null; // Renko close

    final static String GREEN = "green";
    final static String RED = "red";
    final static String ABOVE = "above";
    final static String BELOW = "below";

    protected Double hiLo1 = null;
    protected Double hiLo2 = null;

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

    private Line prevLineUp1 = null, prevLineDown1 = null;

    int highsOffset = 3;
    int lowsOffset = 3;

    // private VWAP vwap;

    @Override
    public void initialize(Defaults defaults) {

        var sd = createSD();

        sd.addTab(initializeGeneralTab(defaults));

        // Quick Settings (Tool Bar and Popup Editor)
        sd.addQuickSettings(RENKO_POINT);
        sd.addQuickSettings(TOP_PATH, BOTTOM_PATH);

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
        // desc.declareSignal(Signals.CROSS_ABOVE, "PRICE_CROSS_ABOVE");
        // desc.declareSignal(Signals.CROSS_BELOW, "PRICE_CROSS_BELOW");

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

    @Override
    public int getMinBars() {
        int p1 = getSettings().getInteger(PERIOD_1);
        int p2 = getSettings().getInteger(PERIOD_2);
        return Math.max(p1, p2) * 2;
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
        long timestamp = series.getEndTime(index);

        debug("timestamp : " + timestamp);

        calculateHiLo1(index, ctx, close);
        calculateHiLo2(index, ctx, close);
        initialiseADX(index, ctx);

        if (settings.getBoolean(RENKO_ENABLED)) {
            calculateRenko(index, ctx, close);

            // debug("history.historyUpdated : " + history.historyUpdated);
            // debug("RenkoResult crossAbove : " + resultRenko.crossAbove);
            // debug("RenkoResult crossBelow : " + resultRenko.crossBelow);

            // if (history.historyUpdated) {
            // if (!history.isArrayAsNull(history.renkoColorHistory)
            // && !history.isArrayAsNull(history.hiLoColor1History)
            // && !history.isArrayAsNull(history.hiLoColor2History)
            // && !history.isArrayAsNull(history.hiLoPrice1History)
            // && !history.isArrayAsNull(history.hiLoPrice2History)) {

            // dbManager.saveRenkoSnapshotLive(ctx.getInstrument().getSymbol(), "ticks", 1,
            // timestamp,
            // history.renkoPriceResHistory[0], history.renkoPriceSupHistory[0],
            // history.renkoColorHistory[0], history.hiLoColor1History[0],
            // history.hiLoColor2History[0],
            // history.hiLoPrice1History[0], history.hiLoPrice2History[0]);
            // }
            // }
            // // displayRenko(index, ctx, close);
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
            hiLo1 = maHigh1;
        } else if (close > maHigh1) {
            series.setDouble(index, Values.MA_HILO_1, maLow1);
            series.setPathColor(index, Values.MA_HILO_1, settings.getColor(LOW_COLOR_1));
            hiLo1 = maLow1;
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
            hiLo2 = maHigh2;
        } else if (close > maHigh2) {
            series.setDouble(index, Values.MA_HILO_2, maLow2);
            series.setPathColor(index, Values.MA_HILO_2, settings.getColor(LOW_COLOR_2));
            hiLo2 = maLow2;
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
        this.brickSize = getSettings().getInteger(RENKO_POINT) / 4.0;

        Double prev = series.getDouble(index - 1, input);
        Double current = series.getDouble(index, input);
        if (prev == null || current == null)
            return;

        crossAbove = false;
        crossBelow = false;

        Double cross = null;

        Double res = null;
        Double supp = null;

        Double currPrice = current;

        if (realRenko == null) {
            Double lowerBound = Math.floor(currPrice / 10) * 10;
            Double upperBound = lowerBound + brickSize;

            if (currPrice > lowerBound + brickSize) {
                lowerBound += brickSize;
                upperBound = lowerBound + brickSize;
            }

            realRenko = upperBound;
            previousClose = lowerBound;
            this.brickSize = Math.abs(realRenko - previousClose);

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
                debug("1");
                debug("timestamp : " + series.getEndTime(index));
                debug("renkoPriceResHistory : " + history.renkoPriceResHistory[0]);
                debug("renkoPriceSupHistory : " + history.renkoPriceSupHistory[0]);
            } else if (history.renkoColorHistory[0].equals(GREEN) && realRenko - currPrice >= 2 * brickSize) {
                realRenko -= Math.floor((realRenko - currPrice) / brickSize) * brickSize;
                previousClose = realRenko + brickSize;
                crossBelow = true;
                history.updateCrossHistory(BELOW);
                history.updateRenkoColorHistory(RED);
                history.autoUpdateRenkoPriceHistory(realRenko, previousClose);
                history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
                history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History, history.hiLoPrice2History);
                debug("2");
                debug("timestamp : " + series.getEndTime(index));
                debug("renkoPriceResHistory : " + history.renkoPriceResHistory[0]);
                debug("renkoPriceSupHistory : " + history.renkoPriceSupHistory[0]);
            } else if (history.renkoColorHistory[0].equals(RED) && currPrice - realRenko >= 2 * brickSize) {
                realRenko += Math.floor((currPrice - realRenko) / brickSize) * brickSize;
                previousClose = realRenko - brickSize;
                crossAbove = true;
                history.updateCrossHistory(ABOVE);
                history.updateRenkoColorHistory(GREEN);
                history.autoUpdateRenkoPriceHistory(realRenko, previousClose);
                history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
                history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History, history.hiLoPrice2History);
                debug("3");
                debug("timestamp : " + series.getEndTime(index));
                debug("renkoPriceResHistory : " + history.renkoPriceResHistory[0]);
                debug("renkoPriceSupHistory : " + history.renkoPriceSupHistory[0]);
            } else if (history.renkoColorHistory[0].equals(RED) && realRenko - currPrice >= brickSize) {
                realRenko -= Math.floor((realRenko - currPrice) / brickSize) * brickSize;
                previousClose = realRenko + brickSize;
                crossBelow = true;
                history.updateCrossHistory(BELOW);
                history.updateRenkoColorHistory(RED);
                history.autoUpdateRenkoPriceHistory(realRenko, previousClose);
                history.autoUpdateHiLoPriceHistory(hiLo1, hiLo2);
                history.autoUpdateHiLoColorHistory(currPrice, history.hiLoPrice1History, history.hiLoPrice2History);
                debug("4");
                debug("timestamp : " + series.getEndTime(index));
                debug("renkoPriceResHistory : " + history.renkoPriceResHistory[0]);
                debug("renkoPriceSupHistory : " + history.renkoPriceSupHistory[0]);
            }
        }

        boolean complete = series.isBarComplete(index);
        if (res == null) {
            res = realRenko;
            complete = false;
        }
        if (supp == null) {
            supp = previousClose;
            complete = false;
        }

        // debug("history.renkoPriceResHistory[0] : " +
        // history.renkoPriceResHistory[0]);
        // debug("history.renkoPriceSupHistory[0] : " +
        // history.renkoPriceSupHistory[0]);

        // Set colors for the Renko bricks
        Color brickColor = history.renkoColorHistory[0] == GREEN ? settings.getColor(UP_COLOR)
                : settings.getColor(DOWN_COLOR);

        // Plot the Renko lines
        series.setPathColor(index - 1, Values.HIGH, brickColor);
        series.setPathColor(index - 1, Values.LOW, brickColor);

        series.setDouble(index, Values.HIGH, history.renkoPriceResHistory[0]);
        series.setDouble(index - 1, Values.LOW, history.renkoPriceSupHistory[0]);

        series.setComplete(index);

        series.setDouble(index, Values.CROSS, cross);

        if (series.isBarComplete(index))
            series.setComplete(index, complete);

        if (!history.isArrayAsNull(history.renkoColorHistory) && !history.isArrayAsNull(history.crossHistory)
                && !history.isArrayAsNull(history.renkoPriceResHistory)
                && !history.isArrayAsNull(history.renkoPriceSupHistory)) {
            manageDowLinesConditionally(index, ctx, realRenko);
        }

        // if (!history.isArrayAsNull(history.renkoColorHistory) &&
        // !history.isArrayAsNull(history.hiLoColor1History)
        // && !history.isArrayAsNull(history.hiLoColor2History)
        // && !history.isArrayAsNull(history.hiLoPrice1History)
        // && !history.isArrayAsNull(history.hiLoPrice2History)) {
        // strategySignal(index, ctx);
        // }

        // if (ctx.getDataSeries().getBarSize().isTick() ) {
        // if (!history.isArrayAsNull(history.renkoColorHistory) &&
        // !history.isArrayAsNull(history.hiLoColor1History)
        // && !history.isArrayAsNull(history.hiLoColor2History)
        // && !history.isArrayAsNull(history.hiLoPrice1History)
        // && !history.isArrayAsNull(history.hiLoPrice2History)) {

        // // debug("saveRenkoSnapshotLive");
        // // debug("timestamp : " + series.getEndTime(index));
        // // debug("renkoPriceResHistory : " + history.renkoPriceResHistory[0]);
        // // debug("renkoPriceSupHistory : " + history.renkoPriceSupHistory[0]);

        // // dbManager.saveRenkoSnapshotLive(ctx.getInstrument().getSymbol(), "ticks",
        // // ctx.getDataSeries().getBarSize().getInterval(), series.getEndTime(index),
        // // history.renkoPriceResHistory[0], history.renkoPriceSupHistory[0],
        // // history.renkoColorHistory[0],
        // // history.hiLoColor1History[0], history.hiLoColor2History[0],
        // // history.hiLoPrice1History[0],
        // // history.hiLoPrice2History[0]);
        // }
        // }
    }

    private void manageDowLinesConditionally(int index, DataContext ctx, Double res) {
        if (history.renkoColorHistory[0].equals(GREEN) && history.crossHistory[0].equals(ABOVE)) {
            if (prevLineUp1 == null || history.dowLineUpPriceHistory[0] < res) {
                if (prevLineUp1 != null)
                    removeFigure(prevLineUp1);
                prevLineUp1 = manageDowLines(index, ctx, res, prevLineUp1, DOW_LINE_UP_1);
            }
        } else if (history.renkoColorHistory[0].equals(RED) && history.crossHistory[0].equals(BELOW)) {
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
        Double priceSup = series.getDouble(index - 1, Values.LOW);
        Double priceRes = series.getDouble(index, Values.HIGH);
        Double priceClose = (double) series.getClose(index);
        if (priceSup == null || priceRes == null)
            return;

        // CorePattern strategyCore = new CorePattern(history.renkoColorHistory,
        // history.hiLoColor1History,
        // history.hiLoColor2History, history.hiLoPrice1History,
        // history.hiLoPrice2History, priceRes, priceClose);

        String popupMsgIndex = "Index : " + index + " => Current : " + format(priceRes);
        String popupMsgInfo = popupMsgIndex;

        // List<PatternCheckResult> results = strategyCore.checkPattern();

        // for (PatternCheckResult result : results) {

        // String popupMsgStrat = "\n Pattern : " + result.patternName + "\n Interval :
        // " + result.interval
        // + "\n TP/SL : " + result.bracket.tp + " / " + result.bracket.sl + "\n Signal
        // : long="
        // + result.okLong() + ", short=" + result.okShort() + "\n Direction : high=" +
        // result.direction.high
        // + ", low=" + result.direction.low + "\n Strength : high=" +
        // result.strength.high + ", low="
        // + result.strength.low + "\n Renko : high=" + result.renko.high + ", low=" +
        // result.renko.low
        // + "\n Hilo : high=" + result.hilo.high + ", low=" + result.hilo.low + "\n ADX
        // : "
        // + series.getDouble(index, Values.ADX);

        // String popupMsgSignal = popupMsgIndex + popupMsgStrat;

        // popupMsgInfo += popupMsgStrat;

        // if (crossAbove || crossBelow) {
        // if (result.okLong()) {
        // var c = new Coordinate(series.getStartTime(index), priceRes);
        // var marker = settings.getMarker(UP_MARKER);
        // String msg = popupMsgSignal;
        // if (marker.isEnabled())
        // addFigure(new Marker(c, crossAbove ? Enums.Position.TOP :
        // Enums.Position.BOTTOM, marker, msg));
        // ctx.signal(index, Signals.CROSS_HIGH, msg, priceSup);
        // }

        // if (result.okShort()) {
        // var c = new Coordinate(series.getStartTime(index), priceRes);
        // var marker = settings.getMarker(DOWN_MARKER);
        // String msg = popupMsgSignal;
        // if (marker.isEnabled())
        // addFigure(new Marker(c, crossAbove ? Enums.Position.TOP :
        // Enums.Position.BOTTOM, marker, msg));
        // ctx.signal(index, Signals.CROSS_LOW, msg, priceSup);
        // }
        // }
        // }

        if (settings.getBoolean(RENKO_ENABLED) && settings.getBoolean(INFO_ENABLED)) {

            if (crossAbove || crossBelow) {

                // popupMsgInfo += "\n hiLoColor1History: " + "\n hiLoColor1History[0] : " +
                // history.hiLoColor1History[0]
                // + "\n hiLoColor1History[1] : " + history.hiLoColor1History[1] + "\n
                // hiLoColor1History[2] : "
                // + history.hiLoColor1History[2] + "\n hiLoColor1History[3] : " +
                // history.hiLoColor1History[3]
                // + "\n hiLoColor1History[4] : " + history.hiLoColor1History[4] + "\n
                // hiLoColor1History[5] : "
                // + history.hiLoColor1History[5] + "\n hiLoColor1History[6] : " +
                // history.hiLoColor1History[6]
                // + "\n hiLoColor1History[7] : " + history.hiLoColor1History[7] + "\n
                // hiLoColor2History : "
                // + "\n hiLoColor2History[0] : " + history.hiLoColor2History[0] + "\n
                // hiLoColor2History[1] : "
                // + history.hiLoColor2History[1] + "\n hiLoColor2History[2] : " +
                // history.hiLoColor2History[2]
                // + "\n hiLoColor2History[3] : " + history.hiLoColor2History[3] + "\n
                // hiLoColor2History[4] : "
                // + history.hiLoColor2History[4] + "\n hiLoColor2History[5] : " +
                // history.hiLoColor2History[5]
                // + "\n hiLoColor2History[6] : " + history.hiLoColor2History[6] + "\n
                // hiLoColor2History[7] : "
                // + history.hiLoColor2History[7];

                var c = new Coordinate(series.getStartTime(index), priceRes);
                var marker = settings.getMarker(INFO_MARKER);
                String msg = popupMsgInfo;
                if (marker.isEnabled())
                    addFigure(new Marker(c, crossAbove ? Enums.Position.TOP : Enums.Position.BOTTOM, marker, msg));
            }
        }
    }

}
