package study_examples;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums.BarSizeType;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.NVP;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.FontDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Asia Session High/Low/Mid Lines */
@StudyHeader(namespace = "com.mycompany", id = "ASIA_SESSION_LINES", rb = "study_examples.nls.strings", name = "Asia Session High Low Mid Lines", label = "Asia Session Levels", desc = "Displays high, low, and mid lines for the Asia trading session", menu = "Custom Studies", overlay = true, studyOverlay = true)
public class AsiaSessionHighLowMidLines extends Study {

    final static String RESIST_IND = "resistInd", PIVOT_IND = "pivotInd", SUPPORT_IND = "supportInd";

    enum Values {
        P, R1, S1
    }

    @Override
    public void initialize(Defaults defaults) {
        var sd = createSD();
        var tab = sd.addTab(get("TAB_GENERAL"));

        List<NVP> types = List.of(new NVP(get("LBL_CLASSIC"), PivotSet.CLASSIC));

        var grp = tab.addGroup(get("LBL_INPUTS"));
        grp.addRow(new BarSizeDescriptor(Inputs.BARSIZE, get("LBL_TIMEFRAME"),
                BarSize.getBarSize(BarSizeType.LINEAR, 1440)));
        grp.addRow(new DiscreteDescriptor(PivotSet.PIVOT_TYPE, get("LBL_PIVOT_TYPE"), PivotSet.CLASSIC, types));
        grp.addRow(new BooleanDescriptor(PivotSet.SHOW_ALL_PIVOTS, get("LBL_SHOW_ALL_PIVOTS"), false, false));
        grp.addRow(new BooleanDescriptor(PivotSet.EXTEND_RIGHT, get("LBL_EXTEND_RIGHT"), false, false));
        grp.addRow(new BooleanDescriptor(PivotSet.SHOW_LABELS, get("LBL_SHOW_PIVOT_LABELS"), true, false));
        grp.addRow(new BooleanDescriptor(PivotSet.SHOW_PRICES, get("LBL_SHOW_PIVOT_PRICES"), false, false));

        tab = sd.addTab(get("TAB_DISPLAY"));

        grp = tab.addGroup(get("LBL_DISPLAY"));
        var path = new PathDescriptor(Inputs.PATH, get("LBL_RESISTANCE_LINE"), defaults.getGreenLine(), 2.0f, null,
                true, false, false, true);
        path.setSupportsShadeType(false);
        path.setSupportsColorPolicy(false);
        path.setSupportsShowPoints(false);
        path.setSupportsShowAsBars(false);
        path.setSupportsTag(false);
        grp.addRow(path);

        path = new PathDescriptor(Inputs.PATH2, get("LBL_PIVOT_LINE"), defaults.getLineColor(), 2.0f, null, true, false,
                false, true);
        path.setSupportsShadeType(false);
        path.setSupportsColorPolicy(false);
        path.setSupportsShowPoints(false);
        path.setSupportsShowAsBars(false);
        path.setSupportsTag(false);
        grp.addRow(path);

        path = new PathDescriptor(Inputs.PATH3, get("LBL_SUPPORT_LINE"), defaults.getRedLine(), 2.0f, null, true, false,
                false, true);
        path.setSupportsShadeType(false);
        path.setSupportsColorPolicy(false);
        path.setSupportsShowPoints(false);
        path.setSupportsShowAsBars(false);
        path.setSupportsTag(false);
        grp.addRow(path);

        path = new PathDescriptor(Inputs.PATH4, get("LBL_MIDDLE_LINES"), defaults.getGrey(), 1.0f, null, false, false,
                false, true);
        path.setSupportsColorPolicy(false);
        path.setSupportsShadeType(false);
        path.setSupportsShowPoints(false);
        path.setSupportsShowAsBars(false);
        path.setSupportsTag(false);
        grp.addRow(path);

        grp.addRow(new FontDescriptor(Inputs.FONT, get("LBL_FONT"), new Font("Arial", Font.BOLD, 12)));
        grp.addRow(new IndicatorDescriptor(RESIST_IND, get("LBL_RESIST_IND"), defaults.getGreen(), null, false, false,
                true));
        grp.addRow(new IndicatorDescriptor(PIVOT_IND, get("LBL_PIVOT_IND"), null, null, false, false, true));
        grp.addRow(new IndicatorDescriptor(SUPPORT_IND, get("LBL_SUPPORT_IND"), defaults.getRed(), null, false, false,
                true));

        sd.addDependency(new EnabledDependency(PivotSet.SHOW_LABELS, PivotSet.SHOW_PRICES));

        // Quick Settings (Tool Bar and Popup Editor)
        sd.addQuickSettings(Inputs.BARSIZE, PivotSet.PIVOT_TYPE, PivotSet.SHOW_ALL_PIVOTS, PivotSet.EXTEND_RIGHT,
                PivotSet.SHOW_LABELS, PivotSet.SHOW_PRICES);
        sd.addQuickSettings(Inputs.PATH, Inputs.PATH2, Inputs.PATH3, Inputs.PATH4);

        var desc = createRD();
        desc.setLabelSettings(Inputs.BARSIZE, PivotSet.PIVOT_TYPE);
        desc.declareIndicator(Values.P, PIVOT_IND);
        desc.declareIndicator(Values.R1, RESIST_IND);
        desc.declareIndicator(Values.S1, SUPPORT_IND);
        setMinBars(2);
    }

    @Override
    public void clearState() {
        super.clearState();
        pivotSets.clear();
        lastPivot = null;
    }

    @Override
    public void onBarClose(DataContext ctx) {
        calculateValues(ctx);
    }

    @Override
    protected void calculateValues(DataContext ctx) {
        var barSize = getSettings().getBarSize(Inputs.BARSIZE);
        var series2 = ctx.getDataSeries(barSize);
        if (series2.size() < 2)
            return;
        if (lastPivot != null && lastPivot.end > ctx.getCurrentTime())
            return;
        // System.err.println("Calculating pivots: " +
        // Util.formatMMDDYYYYHHMMSS(ctx.getCurrentTime()));
        boolean showOlderPivots = getSettings().getBoolean(PivotSet.SHOW_ALL_PIVOTS);
        boolean extendRight = getSettings().getBoolean(PivotSet.EXTEND_RIGHT);
        var pivotType = getSettings().getString(PivotSet.PIVOT_TYPE);
        int start = 0;
        if (!showOlderPivots)
            start = series2.size() - 1;
        pivotSets.clear();
        List<Figure> figures = new ArrayList<>();

        var instr = series2.getInstrument();
        for (int i = start; i < series2.size(); i++) {
            long time = series2.getStartTime(i);
            long end = series2.getEndTime(i);
            if (barSize.isLinear() && barSize.getIntervalMinutes() >= 1440) {
                // This is a bit of a hack.
                // If the trading hours are off by a little bit, its possible that the time from
                // the daily bar
                // is actually on the previous day. Add a bit of time here to work around
                time = instr.getStartOfDay(time + 6 * Util.MILLIS_IN_HOUR, ctx.isRTH());
                if (ctx.isRTH() && time < series2.getStartTime(i) - 6 * Util.MILLIS_IN_HOUR)
                    time += Util.MILLIS_IN_DAY;
                end = Util.getEndOfBar(time, time + barSize.getSizeMillis(), instr, barSize, ctx.isRTH());
            }
            if (pivotSets.containsKey(time))
                continue; // already created the pivot set for this time period
            double H = series2.getHigh(i - 1);
            double L = series2.getLow(i - 1);
            double C = series2.getClose(i - 1);
            double TO = series2.getOpen(i - 1);
            double P = (H + L + C) / 3;
            double range = H - L;
            double R1 = 0;
            double S1 = 0;
            PivotSet ps = null;

            // Classical Pivot
            R1 = (P * 2) - L;
            S1 = (P * 2) - H;
            ps = new PivotSet(time, end, P, R1, S1);

            ps.setExtendLines(extendRight && i == series2.size() - 1);

            pivotSets.put(time, ps);
            figures.add(ps);
        }

        // Update the indicator values for the last pivot
        PivotSet ps = null;
        for (PivotSet _ps : pivotSets.values()) {
            if (ps == null)
                ps = _ps;
            else {
                if (_ps.start > ps.start)
                    ps = _ps;
            }
        }

        if (ps != null) {
            var series = ctx.getDataSeries();
            series.setDouble(Values.P, ps.getP() == 0 ? null : ps.getP());
            series.setDouble(Values.R1, ps.getR1() == 0 ? null : ps.getR1());
            series.setDouble(Values.S1, ps.getS1() == 0 ? null : ps.getS1());
        }

        lastPivot = ps;
        // System.err.println("last pivot: " + Util.formatMMDDYYYYHHMM(lastPivot.start)
        // + " - " + Util.formatMMDDYYYYHHMM(lastPivot.end));
        beginFigureUpdate();
        setFigures(figures);
        endFigureUpdate();
    }

    private PivotSet lastPivot;
    protected Map<Long, PivotSet> pivotSets = Collections.synchronizedMap(new HashMap());
}
