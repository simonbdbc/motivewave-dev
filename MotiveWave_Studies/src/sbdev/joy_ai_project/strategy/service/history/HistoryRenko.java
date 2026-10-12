package sbdev.joy_ai_project.strategy.service.history;

import com.motivewave.platform.sdk.common.*;
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

@StudyHeader(namespace = "com.sbdev", id = "RENKO_HISTORY", name = "Renko History", label = "Renko History", desc = "Renko History", menu = "SB Dev", overlay = true)
public class HistoryRenko extends Study {

    protected HistoryManager history = new HistoryManager();
    private HistoryRenkoDbManager historyRenkoDbManager;

    // Dernière valeur sauvée pour ne pas dupliquer (aussi en RAM pour robustesse)
    private String lastSavedRenkoKey = null;

    protected Double brickSize = null; // Taille de la brique
    protected Double previousRenko = null; // Renko open
    protected Double realRenko = null; // Renko close

    final static String GREEN = "green";
    final static String RED = "red";
    final static String ABOVE = "above";
    final static String BELOW = "below";

    public boolean crossAbove = false;
    public boolean crossBelow = false;

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

    final static String INFO_ENABLED = "infoEnabled";
    final static String INFO_MARKER = "infoMarker";

    enum Values {
        HIGH, LOW, CROSS,
    }

    private static final String RESET_TABLE = "resetTable";

    // private VWAP vwap;

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
        sd.addQuickSettings(TOP_PATH, BOTTOM_PATH);

        var desc = createRD();
        desc.setLabelSettings(INPUT, PERIOD, RENKO_POINT);

        desc.exportValue(new ValueDescriptor(Values.HIGH, "High", new String[] { INPUT, PERIOD }));
        desc.exportValue(new ValueDescriptor(Values.LOW, "Low", new String[] { INPUT, PERIOD }));

        desc.declarePath(Values.HIGH, TOP_PATH);
        desc.declarePath(Values.LOW, BOTTOM_PATH);

        desc.declareIndicator(Values.HIGH, TOP_IND);
        desc.declareIndicator(Values.LOW, BOTTOM_IND);

        desc.setRangeKeys(Values.HIGH, Values.LOW);

    }

    protected SettingTab initializeGeneralTab(Defaults defaults) {
        var tab = new SettingTab("Renko");

        // Section 1
        var section1 = tab.addGroup("Inputs");
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
            HistoryRenkoDbManager.resetHistoryTable();
            debug("Reset Table : history_renko");
            settings.setBoolean(RESET_TABLE, false);
        }
    }

    // @Override
    // protected void postcalculate(DataContext ctx) {
    // realRenko = null;
    // }

    @Override
    protected void calculate(int index, DataContext ctx) {
        var settings = getSettings();
        if (settings == null)
            return;

        calculateRenko(index, ctx);
    }

    private void calculateRenko(int index, DataContext ctx) {
        var settings = getSettings();
        var series = ctx.getDataSeries();

        Object inputClose = Enums.BarInput.CLOSE;

        if (!series.isBarComplete(index)) {
            return; // On ignore si la barre n'est pas finie
        }

        // ticks to points
        double renkoPoints = settings.getInteger(RENKO_POINT) / 4;

        Double prev = series.getDouble(index - 1, inputClose);
        Double current = series.getDouble(index, inputClose);
        if (prev == null || current == null)
            return;

        crossAbove = false;
        crossBelow = false;

        Double cross = null;

        Double priceRes = null;
        Double priceSup = null;

        Double currentClose = current;

        if (realRenko == null) {
            // Initialisation lors de la première exécution
            // Trouver le prix inférieur se terminant par une dizaine
            // Trouver la dizaine inférieure
            double lowerBound = Math.floor(currentClose / 10) * 10;
            // La dizaine suivante se terminant par 5
            double upperBound = lowerBound + renkoPoints;

            // Si le prix est plus proche de la dizaine suivante (10, 20, etc.)
            if (currentClose > lowerBound + renkoPoints) {
                // Ajuster pour commencer à 5
                lowerBound = lowerBound + renkoPoints;
                // Ajuster pour aller jusqu'à la dizaine
                upperBound = lowerBound + renkoPoints;
            }

            realRenko = upperBound;
            previousRenko = lowerBound;
            brickSize = Math.abs(realRenko - previousRenko);
            if (realRenko > previousRenko) {
                history.updateRenkoColorHistory(GREEN);
            } else {
                history.updateRenkoColorHistory(RED);
            }
        } else {
            if (history.renkoColorHistory[0].equals(GREEN)) {

                if (currentClose - realRenko >= brickSize) {
                    realRenko = realRenko + Math.floor((currentClose - realRenko) / brickSize) * brickSize;
                    previousRenko = realRenko - brickSize;
                    crossAbove = true;
                    cross = 1.0;
                    saveRenko(index, ctx, realRenko, previousRenko, GREEN);
                    history.updateRenkoColorHistory(GREEN);
                } else if (realRenko - currentClose >= brickSize * 2) {
                    realRenko = realRenko - Math.floor((realRenko - currentClose) / brickSize) * brickSize;
                    previousRenko = realRenko + brickSize;
                    crossBelow = true;
                    cross = -1.0;
                    saveRenko(index, ctx, realRenko, previousRenko, RED);
                    history.updateRenkoColorHistory(RED);
                }

            } else if (history.renkoColorHistory[0].equals(RED)) {

                if (currentClose - realRenko >= brickSize * 2) {
                    realRenko = realRenko + Math.floor((currentClose - realRenko) / brickSize) * brickSize;
                    previousRenko = realRenko - brickSize;
                    crossAbove = true;
                    cross = 1.0;
                    saveRenko(index, ctx, realRenko, previousRenko, GREEN);
                    history.updateRenkoColorHistory(GREEN);
                } else if (realRenko - currentClose >= brickSize) {
                    realRenko = realRenko - Math.floor((realRenko - currentClose) / brickSize) * brickSize;
                    previousRenko = realRenko + brickSize;
                    crossBelow = true;
                    cross = -1.0;
                    saveRenko(index, ctx, realRenko, previousRenko, RED);
                    history.updateRenkoColorHistory(RED);
                }
            }
        }

        boolean complete = series.isBarComplete(index);
        if (priceRes == null) {
            priceRes = realRenko;
            complete = false;
        }
        if (priceSup == null) {
            priceSup = previousRenko;
            complete = false;
        }

        // Set colors for the Renko bricks
        Color brickColor = history.renkoColorHistory[0] == GREEN ? settings.getColor(UP_COLOR)
                : settings.getColor(DOWN_COLOR);

        // Plot the Renko lines
        series.setPathColor(index - 1, Values.LOW, brickColor);
        series.setPathColor(index - 1, Values.HIGH, brickColor);

        series.setDouble(index - 1, Values.LOW, priceSup);
        series.setDouble(index, Values.HIGH, priceRes);

        series.setDouble(index, Values.CROSS, cross);

        if (series.isBarComplete(index))
            series.setComplete(index, complete);

    }

    private void saveRenko(int index, DataContext ctx, Double priceRes, Double priceSup, String color) {
        // FIFO/BDD via historyRenkoDbManager, anti-duplication en RAM + DB
        String instrument = ctx.getInstrument().getSymbol();
        int interval = ctx.getDataSeries().getBarSize().getInterval();
        long timestamp = ctx.getDataSeries().getEndTime(index);

        String key = instrument + "|" + interval + "|" + timestamp;

        if (key != null && !key.equals(lastSavedRenkoKey)) {
            if (historyRenkoDbManager != null) {
                // debug(instrument + " - " + interval + " - " + priceRes + " - " + priceSup + "
                // - " + color + " - "
                // + index + " - " + timestamp + " - " + new Date(timestamp));
                historyRenkoDbManager.saveSnapshot(instrument, interval, brickSize, priceRes, priceSup, color,
                        timestamp);

                lastSavedRenkoKey = key;
            }
        }
    }

}
