package study_examples;

import java.io.DataInput;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.awt.Color;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
// import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Support/Resistances */
@StudyHeader(namespace = "com.motivewave", id = "HIGH_LOW_LINES", rb = "com.motivewave.platform.study.nls.strings", name = "High Low Lines", desc = "Displays lines at the high and low from a specified time range", menu = "Custom Studies", overlay = true)
public class HighLowLines extends Study {

    enum Values {
        MA_HIGH_LOW_1, MA_HIGH_1, MA_LOW_1
    }

    enum Signals {
        CROSS_ABOVE, CROSS_BELOW
    }

    final static String MA_BARSIZE_1 = "maBarSize1";
    final static String MA_INPUT_HIGH_1 = "maInputHigh1";
    final static String MA_PERIOD_HIGH_1 = "maPeriodHigh1";
    final static String MA_SHIFT_HIGH_1 = "maShiftHigh1";
    final static String MA_FILL_FORWARD_HIGH_1 = "maFillForwardHigh1";
    final static String MA_PATH_HIGH_1 = "maPathHigh1";

    final static String MA_PATH_HIGH_LOW_1 = "maPathHighLow1";

    final static String MA_INPUT_LOW_1 = "maInputLow1";
    final static String MA_PERIOD_LOW_1 = "maPeriodLow1";
    final static String MA_SHIFT_LOW_1 = "maShiftLow1";
    final static String MA_FILL_FORWARD_LOW_1 = "maFillForwardLow1";
    final static String MA_PATH_LOW_1 = "maPathLow1";

    final static String ENABLE_MA_COLOR = "enableMaColor";

    final static String HIGH_COLOR_1 = "highColor1";
    final static String LOW_COLOR_1 = "lowColor1";

    final static String UP_MARKER_1 = "upMarker1";
    final static String DOWN_MARKER_1 = "downMarker1";

    @Override
    public void initialize(Defaults defaults) {
        var sd = createSD();
        var tab = sd.addTab("Settings");

        // Inputs Group
        var grp = tab.addGroup("Inputs");
        grp.addRow(new BarSizeDescriptor(MA_BARSIZE_1, "Bar Size 1",
                BarSize.getBarSize(60)));
        grp.addRow(new InputDescriptor(MA_INPUT_HIGH_1, "Input High 1",
                Enums.BarInput.HIGH));
        grp.addRow(new IntegerDescriptor(MA_PERIOD_HIGH_1, "Period High 1", 21, 1,
                1000, 1), new IntegerDescriptor(MA_SHIFT_HIGH_1, "Shift High 1", 0, -999, 999, 1));
        grp.addRow(new BooleanDescriptor(MA_FILL_FORWARD_HIGH_1, "Fill Forward High 1", true));

        grp.addRow(new InputDescriptor(MA_INPUT_LOW_1, "Input Low 1",
                Enums.BarInput.LOW));
        grp.addRow(new IntegerDescriptor(MA_PERIOD_LOW_1, "Period Low 1", 21, 1,
                1000, 1), new IntegerDescriptor(MA_SHIFT_LOW_1, "Shift Low 1", 0, -999, 999, 1));
        grp.addRow(new BooleanDescriptor(MA_FILL_FORWARD_LOW_1, "Fill Forward Low 1", true));

        // Colors Group
        tab = sd.addTab(get("Advanced"));

        grp = tab.addGroup("Display");
        grp.addRow(
                new PathDescriptor(MA_PATH_HIGH_LOW_1, "Color HiLo 1",
                        defaults.getLineColor(), 1.0f,
                        null, true, true,
                        false));
        grp.addRow(
                new PathDescriptor(MA_PATH_HIGH_1, "Color High 1",
                        defaults.getLineColor(), 1.0f,
                        null, true, true,
                        false));
        grp.addRow(
                new PathDescriptor(MA_PATH_LOW_1, "Color Low 1",
                        defaults.getLineColor(), 1.0f,
                        null, true, true,
                        false));

        grp = tab.addGroup(get("Colors"));
        grp.addRow(new BooleanDescriptor(ENABLE_MA_COLOR, "Enable MA Color", false));
        grp.addRow(new ColorDescriptor(HIGH_COLOR_1, get("High Color 1"), defaults.getRedLine()));
        grp.addRow(new ColorDescriptor(LOW_COLOR_1, get("Low Color 1"), defaults.getGreenLine()));

        grp = tab.addGroup(get("Markers"));
        grp.addRow(new MarkerDescriptor(UP_MARKER_1, "Up marker 1",
                Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), false,
                true));
        grp.addRow(new MarkerDescriptor(DOWN_MARKER_1, "Down marker 1",
                Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), false, true));
        sd.addDependency(
                new EnabledDependency(ENABLE_MA_COLOR, HIGH_COLOR_1, LOW_COLOR_1));

        sd.addQuickSettings(MA_BARSIZE_1);
        sd.addQuickSettings(MA_INPUT_HIGH_1, MA_PERIOD_HIGH_1);
        sd.addQuickSettings(MA_FILL_FORWARD_HIGH_1, MA_PATH_HIGH_1);

        var desc = createRD();

        desc.setLabelSettings(MA_BARSIZE_1, MA_INPUT_HIGH_1, MA_PERIOD_HIGH_1,
                MA_INPUT_LOW_1, MA_PERIOD_LOW_1, MA_SHIFT_HIGH_1, MA_SHIFT_LOW_1);

        desc.exportValue(
                new ValueDescriptor(Values.MA_HIGH_LOW_1, "MA High Low 1", new String[] { MA_INPUT_HIGH_1,
                        MA_PERIOD_HIGH_1, MA_SHIFT_HIGH_1, MA_INPUT_LOW_1, MA_PERIOD_LOW_1, MA_SHIFT_LOW_1 }));
        desc.exportValue(
                new ValueDescriptor(Values.MA_HIGH_1, "MA High 1", new String[] { MA_INPUT_HIGH_1,
                        MA_PERIOD_HIGH_1, MA_SHIFT_HIGH_1 }));
        desc.exportValue(
                new ValueDescriptor(Values.MA_LOW_1, "MA Low 1",
                        new String[] { MA_INPUT_LOW_1, MA_PERIOD_LOW_1, MA_SHIFT_LOW_1 }));

        desc.declarePath(Values.MA_HIGH_1, MA_PATH_HIGH_1);
        desc.setValueSeries(Values.MA_HIGH_1, MA_BARSIZE_1);
        desc.declarePath(Values.MA_LOW_1, MA_PATH_LOW_1);
        desc.setValueSeries(Values.MA_LOW_1, MA_BARSIZE_1);

        desc.declarePath(Values.MA_HIGH_LOW_1, MA_PATH_HIGH_LOW_1);
        desc.setValueSeries(Values.MA_HIGH_LOW_1, MA_BARSIZE_1);

        // desc.declarePath(Values.MA, Inputs.PATH);
        // desc.declareIndicator(Values.MA, Inputs.IND);

        // Signals
        desc.declareSignal(Signals.CROSS_ABOVE, "PRICE_CROSS_ABOVE");
        desc.declareSignal(Signals.CROSS_BELOW, "PRICE_CROSS_BELOW");
    }

    // @Override
    // public int getMinBars() {
    // // int shift = getSettings().getInteger(Inputs.SHIFT);
    // // int bars = getSettings().getInteger(Inputs.PERIOD) + (shift > 0 ? shift :
    // 0);

    // // // For some moving averages, more data is needed to produce useful
    // results.
    // // // For example, an EMA the first value is based on an SMA. In these cases,
    // // // double the number of required bars
    // // var method = METHOD;
    // // if (Util.in(method, Enums.MAMethod.EMA, Enums.MAMethod.SMMA,
    // // Enums.MAMethod.DEMA, Enums.MAMethod.KAMA,
    // // Enums.MAMethod.MEMA))
    // // bars *= 2;
    // int bars = 2;
    // return bars;
    // }

    private Double highest = null; // Valeur la plus haute de la session asiatique
    private Double lowest = null; // Valeur la plus basse de la session asiatique
    private LocalDateTime lastResetDateTime = null; // Date et heure de la dernière réinitialisation

    /**
     * Méthode qui récupère le prix le plus haut et le plus bas de la session
     * asiatique.
     * Réinitialise les valeurs chaque jour à 00h01 UTC.
     * 
     * @param ctx   Le contexte de données.
     * @param index L'indice actuel.
     * @return Un tableau contenant deux valeurs: [0] pour le plus haut et [1] pour
     *         le plus bas.
     */
    public Double[] getAsiaSessionHighLow(DataContext ctx, BarSize barsize, int index) {
        var series = ctx.getDataSeries(barsize);

        // Définir l'heure de début et de fin de la session asiatique en UTC
        int startHour = 0; // 00:00 UTC
        int startMinute = 0;
        int endHour = 6; // 06:00 UTC
        int endMinute = 0;

        // Récupérer l'heure de la bougie (timestamp) en UTC
        long timestamp = series.getStartTime(index);
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
        LocalTime barTime = dateTime.toLocalTime();

        // Réinitialiser les valeurs à 00h01 chaque jour
        if (lastResetDateTime == null || dateTime.toLocalDate().isAfter(lastResetDateTime.toLocalDate())) {
            if (barTime.isAfter(LocalTime.of(0, 1))) { // Réinitialise à 00:01 UTC
                highest = null;
                lowest = null;
                lastResetDateTime = dateTime;
            }
        }

        // Vérifier si la bougie est dans la session asiatique
        LocalTime startTime = LocalTime.of(startHour, startMinute);
        LocalTime endTime = LocalTime.of(endHour, endMinute);

        if (barTime.isBefore(startTime) || barTime.isAfter(endTime)) {
            return null; // Si la bougie n'est pas dans la session asiatique, renvoyer null
        }

        // Mettre à jour les valeurs les plus hautes et les plus basses
        Double high = series.getDouble(index, Enums.BarInput.HIGH);
        Double low = series.getDouble(index, Enums.BarInput.LOW);

        if (high != null && (highest == null || high > highest))
            highest = high;
        if (low != null && (lowest == null || low < lowest))
            lowest = low;

        return new Double[] { highest, lowest };
    }

    public void calcMaHiLo(DataContext ctx,
            BarSize barsize,
            Color maColor,
            int maPeriod,
            int maShift,
            Object maValue,
            boolean maFillForward,
            boolean isBarUpdates,
            String hiLo) {
        var series = ctx.getDataSeries(barsize);
        int indexMin = 0;
        int indexMax = series.size() - 1;
        if (indexMin <= maPeriod) {
            indexMin = maPeriod - 1;
        }

        if (maShift < 0 && maFillForward) {
            indexMax -= maShift;
        }

        Object maExpHigh = Enums.BarInput.HIGH;
        Object maExpLow = Enums.BarInput.LOW;

        DataInput dataInputHigh = maExpHigh instanceof DataInput ? (DataInput) maExpHigh : null;
        DataInput dataInputLow = maExpLow instanceof DataInput ? (DataInput) maExpLow : null;
        if (dataInputHigh == null || dataInputLow == null) {
            for (int index = indexMin; index <= indexMax; ++index) {
                if (dataInputHigh != null || dataInputLow != null) {
                    return;
                }

                if (index + maShift >= 0 && !series.isComplete(index + maShift, maValue)
                        && (isBarUpdates || series.isBarComplete(index))) {

                    Double[] hiLoSession = getAsiaSessionHighLow(ctx, barsize, index);

                    // debug(" hiLoSession: " + hiLoSession);
                    if (hiLoSession != null) {
                        Double asiaHigh = hiLoSession[0]; // Le plus haut
                        Double asiaLow = hiLoSession[1]; // Le plus bas

                        // debug(" asiaHigh: " + asiaHigh);
                        // debug(" asiaLow: " + asiaLow);
                        // boolean complete = series.isBarComplete(index);
                        if (asiaHigh == null) {
                            asiaHigh = series.highest(index, maPeriod, Enums.BarInput.HIGH);
                            // complete = false;
                        }
                        if (asiaLow == null) {
                            asiaLow = series.lowest(index, maPeriod, Enums.BarInput.LOW);
                            // complete = false;
                        }

                        // series.setDouble(index, maValue, asiaHigh);
                        // series.setDouble(index, maValue, asiaLow);

                        if (hiLo == "high") {
                            series.setDouble(index + maShift, maValue, asiaHigh);
                            series.setPathColor(index, maValue, maColor);
                        }
                        if (hiLo == "low") {
                            series.setDouble(index + maShift, maValue, asiaLow);
                            series.setPathColor(index, maValue, maColor);
                        }
                        // series.setComplete(index, complete);
                        series.setComplete(index + maShift, maValue, index >= 0 && index < series.size() - 1);

                    }

                    // Double maHigh = series.highest(index, maPeriod, maExpHigh);
                    // Double maLow = series.lowest(index, maPeriod, maExpLow);

                    // if (maHigh != null || maLow != null) {

                    // if (hiLo == "high") {
                    // // maHiLo = maHigh;
                    // series.setDouble(index + maShift, maValue, maHigh);
                    // // if (doMaColor)
                    // series.setPathColor(index, maValue, maColor);
                    // }
                    // if (hiLo == "low") {
                    // // maHiLo = maLow;
                    // series.setDouble(index + maShift, maValue, maLow);
                    // series.setPathColor(index, maValue, maColor);
                    // }
                    // // if (maHiLo != null) {
                    // // return;
                    // // }

                    // // debug(" close: " + close);
                    // // debug(" maHigh: " + maHigh);
                    // // debug(" maLow: " + maLow);
                    // // debug(" maHiLo: " + maHiLo);

                    // // series.setDouble(index + maShift, maValue, maHiLo);
                    // series.setComplete(index + maShift, maValue, index >= 0 && index <
                    // series.size() - 1);
                    // }
                }
            }

        }
    }

    @Override
    protected void calculateValues(DataContext ctx) {

        var settings = getSettings();
        if (settings == null)
            return;

        calcMaHiLo(
                ctx,
                getSettings().getBarSize(MA_BARSIZE_1),
                settings.getColor(HIGH_COLOR_1),
                settings.getInteger(MA_PERIOD_HIGH_1),
                settings.getInteger(MA_SHIFT_HIGH_1, 0),
                Values.MA_HIGH_1,
                settings.getBoolean(MA_FILL_FORWARD_HIGH_1, true),
                settings.isBarUpdates(),
                "high");

        calcMaHiLo(
                ctx,
                getSettings().getBarSize(MA_BARSIZE_1),
                settings.getColor(LOW_COLOR_1),
                settings.getInteger(MA_PERIOD_LOW_1),
                settings.getInteger(MA_SHIFT_LOW_1, 0),
                Values.MA_LOW_1,
                settings.getBoolean(MA_FILL_FORWARD_LOW_1, true),
                settings.isBarUpdates(),
                "low");

        clearFigures();
    }

    @Override
    protected void calculate(int index, DataContext ctx) {
        // The calculation logic is implemented in `calculateValues`
    }
}

// final static String INPUT = "input";
// final static String PERIOD = "period";
// final static String UP_MARKER = "upMarker";
// final static String DOWN_MARKER = "downMarker";
// final static String TOP_PATH = "topPath";
// final static String BOTTOM_PATH = "bottomPath";
// final static String TOP_IND = "topInd";
// final static String BOTTOM_IND = "bottomInd";

// enum Values {
// HIGH, LOW
// }

// enum Signals {
// CROSS_HIGH, CROSS_LOW
// }

// @Override
// public void initialize(Defaults defaults) {
// var sd = createSD();
// var tab = sd.addTab(get("TAB_GENERAL"));

// var inputs = tab.addGroup(get("LBL_INPUTS"));
// inputs.addRow(new InputDescriptor(INPUT, get("LBL_INPUT"),
// Enums.BarInput.CLOSE));
// inputs.addRow(new IntegerDescriptor(PERIOD, get("LBL_PERIOD"), 20, 1, 9999,
// 1));

// var markers = tab.addGroup(get("LBL_MARKERS"));
// markers.addRow(new MarkerDescriptor(UP_MARKER, get("LBL_UP_MARKER"),
// Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(),
// defaults.getLineColor(), true, true));
// markers.addRow(new MarkerDescriptor(DOWN_MARKER, get("LBL_DOWN_MARKER"),
// Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(),
// defaults.getLineColor(), true, true));

// var lines = tab.addGroup(get("LBL_LINES"));
// var top = new PathDescriptor(TOP_PATH, get("LBL_RESISTANCE_LINE"),
// defaults.getRed(), Enums.PointType.DOT,
// Enums.Size.VERY_SMALL, true, false, true);
// top.setSupportsBoth(true);
// lines.addRow(top);
// var bottom = new PathDescriptor(BOTTOM_PATH, get("LBL_SUPPORT_LINE"),
// defaults.getBlue(), Enums.PointType.DOT,
// Enums.Size.VERY_SMALL, true, false, true);
// bottom.setSupportsBoth(true);
// lines.addRow(bottom);
// lines.addRow(new IndicatorDescriptor(TOP_IND, get("LBL_RESISTANCE_IND"),
// defaults.getRed(), X11Colors.WHITE,
// false, false, true));
// lines.addRow(new IndicatorDescriptor(BOTTOM_IND, get("LBL_SUPPORT_IND"),
// defaults.getBlue(), X11Colors.WHITE,
// false, false, true));

// // Quick Settings (Tool Bar and Popup Editor)
// sd.addQuickSettings(INPUT);
// // sd.addQuickSettings(
// // new SliderDescriptor(PERIOD, get("LBL_PERIOD"), 20, 1, 9999, true, () ->
// Enums.Icon.SINE_WAVE.get()));

// sd.addQuickSettings(TOP_PATH, BOTTOM_PATH);

// var desc = createRD();
// desc.setLabelSettings(INPUT, PERIOD);

// desc.exportValue(new ValueDescriptor(Values.HIGH, get("LBL_RESISTANCE"), new
// String[] { INPUT, PERIOD }));
// desc.exportValue(new ValueDescriptor(Values.LOW, get("LBL_SUPPORT"), new
// String[] { INPUT, PERIOD }));
// desc.exportValue(
// new ValueDescriptor(Signals.CROSS_HIGH, Enums.ValueType.BOOLEAN,
// get("LBL_CROSS_RESISTANCE"), null));
// desc.exportValue(
// new ValueDescriptor(Signals.CROSS_LOW, Enums.ValueType.BOOLEAN,
// get("LBL_CROSS_SUPPORT"), null));

// desc.declarePath(Values.HIGH, TOP_PATH);
// desc.declarePath(Values.LOW, BOTTOM_PATH);

// desc.declareIndicator(Values.HIGH, TOP_IND);
// desc.declareIndicator(Values.LOW, BOTTOM_IND);

// desc.setRangeKeys(Values.HIGH, Values.LOW);
// // Signals
// desc.declareSignal(Signals.CROSS_HIGH, get("LBL_CROSS_RESISTANCE"));
// desc.declareSignal(Signals.CROSS_LOW, get("LBL_CROSS_SUPPORT"));
// }

// /**
// * Méthode qui récupère le prix le plus haut et le plus bas de la session
// asiatique.
// * La session asiatique est supposée être de 00:00 UTC à 06:00 UTC.
// *
// * @param ctx Le contexte de données.
// * @param index L'indice actuel.
// * @return Un tableau contenant deux valeurs: [0] pour le plus haut et [1]
// pour le plus bas.
// */
// protected double[] getAsiaSessionHighLow(DataContext ctx, int index) {
// var series = ctx.getDataSeries();

// // Définir l'heure de début et de fin de la session asiatique en UTC
// int startHour = 0; // 00:00 UTC
// int startMinute = 0;
// int endHour = 6; // 06:00 UTC
// int endMinute = 0;

// // Récupérer l'heure de la bougie (timestamp) en UTC
// long timestamp = series.getStartTime(index);
// LocalDateTime dateTime =
// LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
// LocalTime barTime = dateTime.toLocalTime();

// // Vérifier si la bougie est dans la session asiatique
// LocalTime startTime = LocalTime.of(startHour, startMinute);
// LocalTime endTime = LocalTime.of(endHour, endMinute);

// debug(" barTime: " + barTime);

// if (barTime.isBefore(startTime) || barTime.isAfter(endTime)) {
// return null; // Si la bougie n'est pas dans la session asiatique, renvoyer
// null
// }

// // Parcourir toutes les bougies de la session asiatique pour trouver les
// valeurs les plus hautes et les plus basses
// double highest = Double.MIN_VALUE;
// double lowest = Double.MAX_VALUE;

// // Rechercher toutes les bougies depuis le début de la session asiatique
// jusqu'à la bougie actuelle
// for (int i = index; i >= 0; i--) {
// long currentTimestamp = series.getStartTime(i);
// LocalDateTime currentDateTime =
// LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTimestamp),
// ZoneId.of("UTC"));
// LocalTime currentTime = currentDateTime.toLocalTime();

// // Vérifier si la bougie est encore dans la session asiatique
// if (currentTime.isBefore(startTime)) break; // Si on sort de la session
// asiatique, on arrête

// // Mettre à jour les valeurs les plus hautes et les plus basses
// double high = series.getDouble(i, Enums.BarInput.HIGH);
// double low = series.getDouble(i, Enums.BarInput.LOW);

// if (high > highest) highest = high;
// if (low < lowest) lowest = low;
// }
// debug(" highest: " + highest);
// debug(" lowest: " + lowest);

// return new double[]{highest, lowest};
// }

// @Override
// protected void calculate(int index, DataContext ctx) {
// int period = getSettings().getInteger(PERIOD);
// if (index <= period)
// return;
// var series = ctx.getDataSeries();
// Object input = getSettings().getInput(INPUT);

// Double prev = series.getDouble(index - 1, input);
// Double current = series.getDouble(index, input);
// if (prev == null || current == null)
// return;
// double sma = series.sma(index - 1, period, input);
// boolean crossAbove = (prev < sma && current >= sma);
// boolean crossBelow = (prev > sma && current <= sma);

// double[] hiLo = getAsiaSessionHighLow(ctx, index);

// debug(" hiLo: " + hiLo);

// Double res = series.getDouble(index - 1, Values.HIGH);
// Double supp = series.getDouble(index - 1, Values.LOW);

// if (crossBelow) {
// // Calculate new resistance point
// res = series.highest(index, period, Enums.BarInput.HIGH);
// }

// if (crossAbove) {
// // Calculate new support point
// supp = series.lowest(index, period, Enums.BarInput.LOW);
// }

// if (supp != null && prev >= supp && current < supp) {
// var c = new Coordinate(series.getStartTime(index), supp);
// // March 6, 2019 Check to see if this has been triggered already, if so don't
// // check again
// if (!series.getBoolean(index, Signals.CROSS_LOW, false)) {
// series.setBoolean(index, Signals.CROSS_LOW, true);
// var marker = getSettings().getMarker(DOWN_MARKER);
// String msg = get("SIGNAL_CROSS_SUPPORT", format(current), format(supp));
// if (marker.isEnabled())
// addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
// ctx.signal(index, Signals.CROSS_LOW, msg, current);
// }
// }

// if (res != null && prev <= res && current > res) {
// var c = new Coordinate(series.getStartTime(index), res);
// if (!series.getBoolean(index, Signals.CROSS_HIGH, false)) {
// series.setBoolean(index, Signals.CROSS_HIGH, true);
// var marker = getSettings().getMarker(UP_MARKER);
// String msg = get("SIGNAL_CROSS_RESISTANCE", format(current), format(res));
// if (marker.isEnabled())
// addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
// ctx.signal(index, Signals.CROSS_HIGH, msg, current);
// }
// }

// boolean complete = series.isBarComplete(index);
// if (res == null) {
// res = series.highest(index, period, Enums.BarInput.HIGH);
// complete = false;
// }
// if (supp == null) {
// supp = series.lowest(index, period, Enums.BarInput.LOW);
// complete = false;
// }

// series.setDouble(index, Values.LOW, supp);
// series.setDouble(index, Values.HIGH, res);
// series.setComplete(index, complete);
// }
// }