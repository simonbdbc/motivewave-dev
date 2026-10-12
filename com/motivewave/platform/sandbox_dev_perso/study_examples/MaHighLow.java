package dev;

import java.awt.Color;
import java.io.DataInput;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;

/** Gann High Low Renko Indicator with Multiple Timeframes */
@StudyHeader(namespace = "com.mycompany", id = "MA_HILO", name = "Ma High Low", desc = "Custom Ma High Low Indicator with Multiple Timeframes", menu = "Custom Studies", overlay = true, studyOverlay = true)

public class MaHighLow extends Study {

    protected Enums.MAMethod METHOD;

    enum Values {
        MA_HIGH_LOW_1, MA_HIGH_LOW_2, MA_HIGH_1, MA_LOW_1, MA_HIGH_2, MA_LOW_2
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
    final static String MA_PATH_HIGH_LOW_2 = "maPathHighLow2";

    final static String MA_INPUT_LOW_1 = "maInputLow1";
    final static String MA_PERIOD_LOW_1 = "maPeriodLow1";
    final static String MA_SHIFT_LOW_1 = "maShiftLow1";
    final static String MA_FILL_FORWARD_LOW_1 = "maFillForwardLow1";
    final static String MA_PATH_LOW_1 = "maPathLow1";

    final static String MA_BARSIZE_2 = "maBarSize2";
    final static String MA_INPUT_HIGH_2 = "maInputHigh2";
    final static String MA_PERIOD_HIGH_2 = "maPeriodHigh2";
    final static String MA_SHIFT_HIGH_2 = "maShiftHigh2";
    final static String MA_FILL_FORWARD_HIGH_2 = "maFillForwardHigh2";
    final static String MA_PATH_HIGH_2 = "maPathHigh2";

    final static String MA_INPUT_LOW_2 = "maInputLow2";
    final static String MA_PERIOD_LOW_2 = "maPeriodLow2";
    final static String MA_SHIFT_LOW_2 = "maShiftLow2";
    final static String MA_FILL_FORWARD_LOW_2 = "maFillForwardLow2";
    final static String MA_PATH_LOW_2 = "maPathLow2";

    final static String ENABLE_MA_COLOR = "enableMaColor";

    final static String HIGH_COLOR_1 = "highColor1";
    final static String LOW_COLOR_1 = "lowColor1";
    final static String HIGH_COLOR_2 = "highColor2";
    final static String LOW_COLOR_2 = "lowColor2";

    final static String UP_MARKER_1 = "upMarker1";
    final static String DOWN_MARKER_1 = "downMarker1";
    final static String UP_MARKER_2 = "upMarker2";
    final static String DOWN_MARKER_2 = "downMarker2";

    @Override
    public void initialize(Defaults defaults) {
        var sd = createSD();
        var tab = sd.addTab("Settings");

        // Inputs Group
        var grp = tab.addGroup("Inputs");
        grp.addRow(new BarSizeDescriptor(MA_BARSIZE_1, "Bar Size 1",
                BarSize.getBarSize(1)));
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

        grp.addRow(new BarSizeDescriptor(MA_BARSIZE_2, "Bar Size 2",
                BarSize.getBarSize(5)));
        grp.addRow(new InputDescriptor(MA_INPUT_HIGH_2, "Input High 2",
                Enums.BarInput.HIGH));
        grp.addRow(new IntegerDescriptor(MA_PERIOD_HIGH_2, "Period High 2", 21, 1,
                1000, 1), new IntegerDescriptor(MA_SHIFT_HIGH_2, "Shift High 2", 0, -999, 999, 1));
        grp.addRow(new BooleanDescriptor(MA_FILL_FORWARD_HIGH_2, "Fill Forward High 2", true));


        grp.addRow(new InputDescriptor(MA_INPUT_LOW_2, "Input Low 2",
                Enums.BarInput.LOW));
        grp.addRow(new IntegerDescriptor(MA_PERIOD_LOW_2, "Period Low 2", 21, 1,
                1000, 1), new IntegerDescriptor(MA_SHIFT_LOW_2, "Shift Low 2", 0, -999, 999, 1));
        grp.addRow(new BooleanDescriptor(MA_FILL_FORWARD_LOW_2, "Fill Forward Low 2", true));

        // Colors Group
        tab = sd.addTab(get("Advanced"));

        grp = tab.addGroup("Display");
        grp.addRow(
                new PathDescriptor(MA_PATH_HIGH_LOW_1, "Color HiLo 1",
                        defaults.getLineColor(), 1.0f,
                        null, true, true,
                        false));
        grp.addRow(
                new PathDescriptor(MA_PATH_HIGH_LOW_2, "Color HiLo 2",
                        defaults.getLineColor(), 1.0f,
                        null, true, true,
                        false));

        grp = tab.addGroup(get("Colors"));
        grp.addRow(new BooleanDescriptor(ENABLE_MA_COLOR, "Enable MA Color", false));
        grp.addRow(new ColorDescriptor(HIGH_COLOR_1, get("High Color 1"), defaults.getRedLine()));
        grp.addRow(new ColorDescriptor(LOW_COLOR_1, get("Low Color 1"), defaults.getGreenLine()));
        grp.addRow(new ColorDescriptor(HIGH_COLOR_2, get("High Color 2"), defaults.getRedLine()));
        grp.addRow(new ColorDescriptor(LOW_COLOR_2, get("Low Color 2"), defaults.getGreenLine()));

        grp = tab.addGroup(get("Markers"));
        grp.addRow(new MarkerDescriptor(UP_MARKER_1, "Up marker 1",
                Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), false,
                true));
        grp.addRow(new MarkerDescriptor(DOWN_MARKER_1, "Down marker 1",
                Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), false, true));
        grp.addRow(new MarkerDescriptor(UP_MARKER_2, "Up marker 2",
                Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), false,
                true));
        grp.addRow(new MarkerDescriptor(DOWN_MARKER_2, "Down marker 2",
                Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), false, true));

        sd.addDependency(
                new EnabledDependency(ENABLE_MA_COLOR, HIGH_COLOR_1, LOW_COLOR_1, HIGH_COLOR_2, LOW_COLOR_2));

        sd.addQuickSettings(MA_BARSIZE_1);
        sd.addQuickSettings(MA_INPUT_HIGH_1, MA_PERIOD_HIGH_1);
        sd.addQuickSettings(MA_FILL_FORWARD_HIGH_1, MA_PATH_HIGH_1);

        var desc = createRD();

        desc.setLabelSettings(MA_BARSIZE_1, MA_INPUT_HIGH_1, MA_PERIOD_HIGH_1,
                MA_INPUT_LOW_1, MA_PERIOD_LOW_1, MA_SHIFT_HIGH_1, MA_SHIFT_LOW_1,
                MA_BARSIZE_2, MA_INPUT_HIGH_2, MA_PERIOD_HIGH_2, MA_INPUT_LOW_2,
                MA_PERIOD_LOW_2, MA_SHIFT_HIGH_2, MA_SHIFT_LOW_2);

        desc.exportValue(
                new ValueDescriptor(Values.MA_HIGH_LOW_1, "MA High Low 1", new String[] { MA_INPUT_HIGH_1,
                        MA_PERIOD_HIGH_1, MA_SHIFT_HIGH_1, MA_INPUT_LOW_1, MA_PERIOD_LOW_1, MA_SHIFT_LOW_1 }));
        desc.exportValue(
                new ValueDescriptor(Values.MA_HIGH_LOW_2, "MA High Low 2", new String[] { MA_INPUT_HIGH_2,
                        MA_PERIOD_HIGH_2, MA_SHIFT_HIGH_2, MA_INPUT_LOW_2, MA_PERIOD_LOW_2, MA_SHIFT_LOW_2 }));
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

        desc.declarePath(Values.MA_HIGH_2, MA_PATH_HIGH_2);
        desc.setValueSeries(Values.MA_HIGH_2, MA_BARSIZE_2);
        desc.declarePath(Values.MA_LOW_2, MA_PATH_LOW_2);
        desc.setValueSeries(Values.MA_LOW_2, MA_BARSIZE_2);

        desc.declarePath(Values.MA_HIGH_LOW_1, MA_PATH_HIGH_LOW_1);
        desc.setValueSeries(Values.MA_HIGH_LOW_1, MA_BARSIZE_1);

        desc.declarePath(Values.MA_HIGH_LOW_2, MA_PATH_HIGH_LOW_2);
        desc.setValueSeries(Values.MA_HIGH_LOW_2, MA_BARSIZE_2);

        // Signals
        desc.declareSignal(Signals.CROSS_ABOVE, "PRICE_CROSS_ABOVE");
        desc.declareSignal(Signals.CROSS_BELOW, "PRICE_CROSS_BELOW");
    }

    @Override
    public int getMinBars() {
        // int shift = getSettings().getInteger(Inputs.SHIFT);
        // int bars = getSettings().getInteger(Inputs.PERIOD) + (shift > 0 ? shift : 0);

        // // For some moving averages, more data is needed to produce useful results.
        // // For example, an EMA the first value is based on an SMA. In these cases,
        // // double the number of required bars
        // var method = METHOD;
        // if (Util.in(method, Enums.MAMethod.EMA, Enums.MAMethod.SMMA,
        // Enums.MAMethod.DEMA, Enums.MAMethod.KAMA,
        // Enums.MAMethod.MEMA))
        // bars *= 2;
        int bars = 2;
        return bars;
    }

    public void calcMaHiLo(DataContext ctx,
            BarSize barsize,
            Enums.MAMethod maMethod,
            Color maColorHigh,
            Color maColorLow,
            int maPeriod,
            int maShift,
            Object maValueHiLo,
            boolean maFillForward,
            boolean isBarUpdates) {
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

                if (index + maShift >= 0 && !series.isComplete(index + maShift, maValueHiLo)
                        && (isBarUpdates || series.isBarComplete(index))) {

                    Double maHigh = series.ma(maMethod, index, maPeriod, maExpHigh);
                    Double maLow = series.ma(maMethod, index, maPeriod, maExpLow);
                    float close = series.getClose(index);
                    // Double maHiLo = null;

                    if (maHigh != null || maLow != null) {

                        if (close < maLow) {
                            // maHiLo = maHigh;
                            series.setDouble(index + maShift, maValueHiLo, maHigh);
                            // if (doMaColor)
                            series.setPathColor(index, maValueHiLo, maColorHigh);
                        }
                        if (close > maHigh) {
                            // maHiLo = maLow;
                            series.setDouble(index + maShift, maValueHiLo, maLow);
                            series.setPathColor(index, maValueHiLo, maColorLow);
                        }
                        // if (maHiLo != null) {
                        // return;
                        // }

                        // debug(" close: " + close);
                        // debug(" maHigh: " + maHigh);
                        // debug(" maLow: " + maLow);
                        // debug(" maHiLo: " + maHiLo);

                        // series.setDouble(index + maShift, maValueHiLo, maHiLo);
                        series.setComplete(index + maShift, maValueHiLo, index >= 0 && index < series.size() - 1);
                    }
                }
            }

        }
    }

    @Override
    protected void calculateValues(DataContext ctx) {

        var settings = getSettings();
        if (settings == null)
            return;

        // debug(" settings: " + settings);

        calcMaHiLo(
                ctx,
                getSettings().getBarSize(MA_BARSIZE_1),
                METHOD,
                settings.getColor(HIGH_COLOR_1),
                settings.getColor(LOW_COLOR_1),
                settings.getInteger(MA_PERIOD_HIGH_1),
                settings.getInteger(MA_SHIFT_HIGH_1, 0),
                Values.MA_HIGH_LOW_1,
                settings.getBoolean(MA_FILL_FORWARD_HIGH_1, true),
                settings.isBarUpdates());

        calcMaHiLo(
                ctx,
                getSettings().getBarSize(MA_BARSIZE_2),
                METHOD,
                settings.getColor(HIGH_COLOR_2),
                settings.getColor(LOW_COLOR_2),
                settings.getInteger(MA_PERIOD_HIGH_2),
                settings.getInteger(MA_SHIFT_HIGH_2, 0),
                Values.MA_HIGH_LOW_2,
                settings.getBoolean(MA_FILL_FORWARD_HIGH_2, true),
                settings.isBarUpdates());

        clearFigures();
    }

    @Override
    protected void calculate(int index, DataContext ctx) {
        // The calculation logic is implemented in `calculateValues`
    }
}
