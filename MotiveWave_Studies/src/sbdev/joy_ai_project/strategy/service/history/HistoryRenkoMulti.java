package sbdev.joy_ai_project.strategy.service.history;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.StringDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.*;

import sbdev.joy_ai_project.strategy.database.HistoryRenkoDbManager;
import sbdev.joy_ai_project.strategy.util.HistoryManager;

import java.awt.Color;

@StudyHeader(namespace = "com.sbdev", id = "RENKO_HISTORY_MULTI", name = "Renko History Multi Timeframe", label = "Renko History Multi Timeframe", desc = "Renko History Multi Timeframe", menu = "SB Dev", overlay = true)
public class HistoryRenkoMulti extends Study {

    protected HistoryManager history = new HistoryManager();
    private HistoryRenkoDbManager historyRenkoDbManager;

    // Dernière valeur sauvée pour ne pas dupliquer (aussi en RAM pour robustesse)
    private String lastSavedRenkoKey1 = null;
    private String lastSavedRenkoKey2 = null;
    private String lastSavedRenkoKey3 = null;
    private String lastSavedRenkoKey4 = null;

    protected Double brickSize1 = null; // Taille de la brique
    protected Double previousRenko1 = null; // Renko open
    protected Double realRenko1 = null; // Renko close
    protected Double brickSize2 = null; // Taille de la brique
    protected Double previousRenko2 = null; // Renko open
    protected Double realRenko2 = null; // Renko close
    protected Double brickSize3 = null; // Taille de la brique
    protected Double previousRenko3 = null; // Renko open
    protected Double realRenko3 = null; // Renko close
    protected Double brickSize4 = null; // Taille de la brique
    protected Double previousRenko4 = null; // Renko open
    protected Double realRenko4 = null; // Renko close

    final static String GREEN = "green";
    final static String RED = "red";
    final static String ABOVE = "above";
    final static String BELOW = "below";

    public boolean crossAbove1 = false;
    public boolean crossBelow1 = false;
    public boolean crossAbove2 = false;
    public boolean crossBelow2 = false;
    public boolean crossAbove3 = false;
    public boolean crossBelow3 = false;
    public boolean crossAbove4 = false;
    public boolean crossBelow4 = false;

    private static final String RENKO_ENABLED = "renkoEnabled";

    final static String INPUT = "inputClose";
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

    private static final String TF_ENABLED_1 = "tfEnabled1";
    private static final String TIMEFRAME_1 = "timeframe1";
    private static final String MA_METHOD_1 = "maMethod1";
    private static final String PATH_HILO_1 = "pathHilo1";
    private static final String PERIOD_1 = "period1";

    final static String INFO_ENABLED = "infoEnabled";
    final static String INFO_MARKER = "infoMarker";

    enum Values {
        HIGH1, LOW1, CROSS1, HIGH2, LOW2, CROSS2, HIGH3, LOW3, CROSS3, HIGH4, LOW4, CROSS4, REAL_RENKO, PREV_CLOSE
    }

    private static final String RESET_TABLE = "resetTable";

    // private VWAP vwap;

    private RenkoTickOperation renkoCalculator;

    @Override
    public void initialize(Defaults defaults) {

        // (inutile de réappeler DatabaseInitializer ici, c'est déjà fait dans le
        // singleton)
        historyRenkoDbManager = new HistoryRenkoDbManager();

        var sd = createSD();

        sd.addTab(initializeStrategyTab(defaults));

        sd.addTab(initializeGeneralTab(defaults));

        // Quick Settings (Tool Bar and Popup Editor)
        sd.addQuickSettings(RENKO_POINT);

        var desc = createRD();
        desc.setLabelSettings(INPUT, PERIOD, RENKO_POINT);

        // desc.exportValue(new ValueDescriptor(Values.HIGH1, "High1", new String[] {
        // INPUT, PERIOD }));
        // desc.exportValue(new ValueDescriptor(Values.LOW1, "Low1", new String[] {
        // INPUT, PERIOD }));

        // desc.declarePath(Values.HIGH1, TOP_PATH);
        // desc.declarePath(Values.LOW1, BOTTOM_PATH);

        // desc.declareIndicator(Values.HIGH1, TOP_IND);
        // desc.declareIndicator(Values.LOW1, BOTTOM_IND);

        desc.declarePath(RenkoValues.REAL_RENKO, "Renko Path");
        desc.declarePath(RenkoValues.PREV_CLOSE, "Renko Prev Close");

        desc.declareIndicator(RenkoValues.REAL_RENKO, "Renko Path");
        desc.declareIndicator(RenkoValues.PREV_CLOSE, "Renko Prev Close");

        // desc.setRangeKeys(Values.HIGH1, Values.LOW1);

    }

    protected SettingTab initializeGeneralTab(Defaults defaults) {
        var tab = new SettingTab("Renko");

        // Section 1
        var section1 = tab.addGroup(get("First Timeframe"));
        // Add timeframe selection options
        section1.addRow(new BooleanDescriptor(TF_ENABLED_1, "Use Timeframe 1", true));
        section1.addRow(
                new BarSizeDescriptor(TIMEFRAME_1, "Timeframe 1", BarSize.getBarSize(Enums.BarSizeType.TICK, 1)));
        section1.addRow(new IntegerDescriptor(RENKO_POINT, "Renko Ticks", 20, 1, 99, 1));
        section1.addRow(new StringDescriptor("note", "Note", "NQ : 4 ticks = 1 point"));

        // Section 2
        var lines = tab.addGroup("Display");
        lines.addRow(new PathDescriptor(TOP_PATH, "High Path", null, 1.0f, null, true, true, false));
        lines.addRow(new PathDescriptor(BOTTOM_PATH, "Bottom Path", null, 1.0f, null, true, true, false));

        lines.addRow(new ColorDescriptor(UP_COLOR, "Up Color", defaults.getGreenLine()));
        lines.addRow(new ColorDescriptor(DOWN_COLOR, "Down Color", defaults.getRedLine()));

        return tab;
    }

    protected SettingTab initializeStrategyTab(Defaults defaults) {
        var tab = new SettingTab("Strategy");

        // Section 1
        var grp = tab.addGroup("Database");
        grp.addRow(new BooleanDescriptor(RESET_TABLE, " Reset Table", false));

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
    public void onSettingsUpdated(DataContext ctx) {
        var settings = getSettings();
        if (settings == null)
            return;

        if (settings.getBoolean(RESET_TABLE)) {
            HistoryRenkoDbManager.resetHistoryTable();
            debug("Reset Table : history_renko");
            settings.setBoolean(RESET_TABLE, false);
        }
    }

    @Override
    protected void calculateValues(DataContext ctx) {
        var settings = getSettings();
        if (settings == null)
            return;
        var series = ctx.getDataSeries();

        if (renkoCalculator == null)
            renkoCalculator = new RenkoTickOperation(series, 10, settings.getInteger(RENKO_POINT) / 4); // par exemple

        // var tfSeries1 = ctx.getDataSeries(settings.getBarSize(TIMEFRAME_1));
        // long time = ctx.getDataSeries().getEndTime(index);
        // int tfIndex1 = tfSeries1.findIndex(time);
        // if (tfIndex1 < 0)
        // return;

        // calculateRenko(ctx);
    }

    @Override
    public void onTick(DataContext ctx, Tick tick) {
        if (renkoCalculator == null)
            return;
        renkoCalculator.onTick(tick);
    }

    // private void calculateRenko(int index, DataContext ctx) {
    // var settings = getSettings();
    // var series = ctx.getDataSeries();
    // var tfSeries1 = ctx.getDataSeries(settings.getBarSize(TIMEFRAME_1));

    // if (tfSeries1 == null)
    // return;

    // // Trouver le timestamp de la barre courante
    // long time = ctx.getDataSeries().getEndTime(index);
    // int tfIndex1 = tfSeries1.findIndex(time);
    // if (tfIndex1 < 0)
    // return;

    // Object inputClose = Enums.BarInput.CLOSE;

    // if (!tfSeries1.isBarComplete(tfIndex1)) {
    // return; // On ignore si la barre n'est pas finie
    // }

    // // ticks to points
    // double renkoPoints = settings.getInteger(RENKO_POINT) / 4;

    // Double prev = tfSeries1.getDouble(tfIndex1 - 1, inputClose);
    // Double current = tfSeries1.getDouble(tfIndex1, inputClose);
    // if (prev == null || current == null)
    // return;

    // crossAbove1 = false;
    // crossBelow1 = false;

    // Double cross = null;

    // Double priceRes = null;
    // Double priceSup = null;

    // Double currentClose = current;

    // if (realRenko1 == null) {
    // // Initialisation lors de la première exécution
    // // Trouver le prix inférieur se terminant par une dizaine
    // // Trouver la dizaine inférieure
    // double lowerBound = Math.floor(currentClose / 10) * 10;
    // // La dizaine suivante se terminant par 5
    // double upperBound = lowerBound + renkoPoints;

    // // Si le prix est plus proche de la dizaine suivante (10, 20, etc.)
    // if (currentClose > lowerBound + renkoPoints) {
    // // Ajuster pour commencer à 5
    // lowerBound = lowerBound + renkoPoints;
    // // Ajuster pour aller jusqu'à la dizaine
    // upperBound = lowerBound + renkoPoints;
    // }

    // realRenko1 = upperBound;
    // previousRenko1 = lowerBound;
    // brickSize1 = Math.abs(realRenko1 - previousRenko1);
    // if (realRenko1 > previousRenko1) {
    // history.updateRenkoColorHistory(GREEN);
    // } else {
    // history.updateRenkoColorHistory(RED);
    // }
    // } else {
    // if (history.renkoColorHistory[0].equals(GREEN)) {

    // if (currentClose - realRenko1 >= brickSize1) {
    // realRenko1 = realRenko1 + Math.floor((currentClose - realRenko1) /
    // brickSize1) * brickSize1;
    // previousRenko1 = realRenko1 - brickSize1;
    // crossAbove1 = true;
    // cross = 1.0;
    // saveRenko(index, ctx, realRenko1, previousRenko1, GREEN);
    // history.updateRenkoColorHistory(GREEN);
    // } else if (realRenko1 - currentClose >= brickSize1 * 2) {
    // realRenko1 = realRenko1 - Math.floor((realRenko1 - currentClose) /
    // brickSize1) * brickSize1;
    // previousRenko1 = realRenko1 + brickSize1;
    // crossBelow1 = true;
    // cross = -1.0;
    // saveRenko(index, ctx, realRenko1, previousRenko1, RED);
    // history.updateRenkoColorHistory(RED);
    // }

    // } else if (history.renkoColorHistory[0].equals(RED)) {

    // if (currentClose - realRenko1 >= brickSize1 * 2) {
    // realRenko1 = realRenko1 + Math.floor((currentClose - realRenko1) /
    // brickSize1) * brickSize1;
    // previousRenko1 = realRenko1 - brickSize1;
    // crossAbove1 = true;
    // cross = 1.0;
    // saveRenko(index, ctx, realRenko1, previousRenko1, GREEN);
    // history.updateRenkoColorHistory(GREEN);
    // } else if (realRenko1 - currentClose >= brickSize1) {
    // realRenko1 = realRenko1 - Math.floor((realRenko1 - currentClose) /
    // brickSize1) * brickSize1;
    // previousRenko1 = realRenko1 + brickSize1;
    // crossBelow1 = true;
    // cross = -1.0;
    // saveRenko(index, ctx, realRenko1, previousRenko1, RED);
    // history.updateRenkoColorHistory(RED);
    // }
    // }
    // }

    // boolean complete = tfSeries1.isBarComplete(tfIndex1);
    // if (priceRes == null) {
    // priceRes = realRenko1;
    // complete = false;
    // }
    // if (priceSup == null) {
    // priceSup = previousRenko1;
    // complete = false;
    // }

    // // Set colors for the Renko bricks
    // Color brickColor = history.renkoColorHistory[0] == GREEN ?
    // settings.getColor(UP_COLOR)
    // : settings.getColor(DOWN_COLOR);

    // // Plot the Renko lines
    // series.setPathColor(index - 1, Values.LOW1, brickColor);
    // series.setPathColor(index - 1, Values.HIGH1, brickColor);

    // series.setDouble(index - 1, Values.LOW1, priceSup);
    // series.setDouble(index, Values.HIGH1, priceRes);

    // series.setDouble(index, Values.CROSS1, cross);

    // if (series.isBarComplete(index))
    // series.setComplete(index, complete);

    // }

    // private void saveRenko(int index, DataContext ctx, Double priceRes, Double
    // priceSup, String color) {
    // // FIFO/BDD via historyRenkoDbManager, anti-duplication en RAM + DB
    // String instrument = ctx.getInstrument().getSymbol();
    // int interval = ctx.getDataSeries().getBarSize().getInterval();
    // long timestamp = ctx.getDataSeries().getEndTime(index);

    // String key = instrument + "|" + interval + "|" + timestamp;

    // // if (key != null && !key.equals(lastSavedRenkoKey)) {
    // // if (historyRenkoDbManager != null) {
    // // // debug(instrument + " - " + interval + " - " + priceRes + " - " +
    // priceSup
    // // + "
    // // // - " + color + " - "
    // // // + index + " - " + timestamp + " - " + new Date(timestamp));
    // // historyRenkoDbManager.saveSnapshot(instrument, interval, priceRes,
    // priceSup,
    // // color, timestamp);

    // // lastSavedRenkoKey = key;
    // // }
    // // }
    // }

}
