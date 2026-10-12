package study_examples;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Enums.BarSizeType;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.NVP;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.FontDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Asia Session High/Low/Mid Lines */
@StudyHeader(namespace = "com.mycompany", id = "ASIA_SESSION_LINES_TV", rb = "study_examples.nls.strings", name = "Asia Session High Low Lines", label = "Asia Session Lines", desc = "Displays high, low, and mid lines for the Asia trading session", menu = "Custom Studies", overlay = true, studyOverlay = true)

public class AsiaSessionHighLowLines extends Study {

    // final static String S_YEAR = "year";
    // final static String S_MONTH = "year";
    // final static String S_DAY = "year";

    final static String INPUT = "input";

    final static String START_HOUR = "startHour";
    final static String START_MINUTE = "startMinute";
    final static String END_HOUR = "endHour";
    final static String END_MINUTE = "endMinute";

    final static String PATH_HIGH_COLOR = "pathHighColor";
    final static String PATH_LOW_COLOR = "pathLowColor";
    final static String PATH_MID_COLOR = "pathMidColor";

    final static String SHOW_MID_LINE = "showMidLine";

    final static String HIGH_COLOR = "highColor";
    final static String LOW_COLOR = "lowColor";
    final static String MID_COLOR = "midColor";

    final static String UP_MARKER = "upMarker";
    final static String DOWN_MARKER = "downMarker";

    enum Values {
        SESSION_HIGH, SESSION_LOW, PATH_HIGH, PATH_LOW, PATH_MID
    }

    enum Signals {
        CROSS_ABOVE, CROSS_BELOW
    }

    @Override
    public void initialize(Defaults defaults) {

        // Paramètres utilisateur
        // sd.addInput("sessionStart", "Début de session (HH:mm)", InputType.TIME,
        // "18:00");
        // sd.addInput("sessionEnd", "Fin de session (HH:mm)", InputType.TIME, "02:00");
        // sd.addColor("highColor", "Couleur du niveau haut", Color.CYAN);
        // sd.addColor("lowColor", "Couleur du niveau bas", Color.CYAN);
        // sd.addColor("midColor", "Couleur du niveau médian", Color.ORANGE);
        // sd.addDouble("lineWidth", "Épaisseur des lignes", 1.0, 1.0, 10.0, 0.1);
        // sd.addBoolean("showMidLine", "Afficher la ligne médiane", true);

        var sd = createSD();
        var tab = sd.addTab("Settings");

        // Inputs Group
        var grp = tab.addGroup("Inputs");
        // grp.addRow(new IntegerDescriptor(S_YEAR, "STARTING_YEAR", 2025, 1980, 2100,
        // 1));
        // grp.addRow(new IntegerDescriptor(S_MONTH, "STARTING_MONTH", 9, 1, 12, 1));
        // grp.addRow(new IntegerDescriptor(S_DAY, "STARTING_DAY", 10, 1, 31, 1));
        grp.addRow(new IntegerDescriptor(START_HOUR, "STARTING_HOUR", 18, 0, 24, 1));
        grp.addRow(new IntegerDescriptor(START_MINUTE, "STARTING_MINUTE", 0, 0, 60, 1));
        grp.addRow(new IntegerDescriptor(END_HOUR, "ENDING_HOUR", 2, 0, 24, 1));
        grp.addRow(new IntegerDescriptor(END_MINUTE, "ENDING_MINUTE", 0, 0, 60, 1));

        grp.addRow(new InputDescriptor(INPUT, get("INPUT"), Enums.BarInput.CLOSE));

        // Colors Group
        tab = sd.addTab(get("Advanced"));

        grp = tab.addGroup("Display");
        grp.addRow(
                new PathDescriptor(PATH_HIGH_COLOR, "Color High",
                        defaults.getLineColor(), 1.0f,
                        null, true, true,
                        false));
        grp.addRow(
                new PathDescriptor(PATH_LOW_COLOR, "Color Low",
                        defaults.getLineColor(), 1.0f,
                        null, true, true,
                        false));
        grp.addRow(
                new PathDescriptor(PATH_MID_COLOR, "Color Mid",
                        defaults.getLineColor(), 1.0f,
                        null, true, true,
                        false));

        grp = tab.addGroup(get("Colors"));
        grp.addRow(new BooleanDescriptor(SHOW_MID_LINE, "Show Mid Line", true));
        grp.addRow(new ColorDescriptor(HIGH_COLOR, get("High Color"), defaults.getRedLine()));
        grp.addRow(new ColorDescriptor(LOW_COLOR, get("Low Color"), defaults.getGreenLine()));
        grp.addRow(new ColorDescriptor(MID_COLOR, get("Mid Color"), defaults.getYellowLine()));

        grp = tab.addGroup(get("Markers"));
        grp.addRow(new MarkerDescriptor(UP_MARKER, "Up marker",
                Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), false,
                true));
        grp.addRow(new MarkerDescriptor(DOWN_MARKER, "Down marker",
                Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), false, true));

        sd.addDependency(
                new EnabledDependency(SHOW_MID_LINE, HIGH_COLOR, LOW_COLOR, MID_COLOR));

        sd.addQuickSettings(INPUT);
        sd.addQuickSettings(SHOW_MID_LINE);
        // sd.addQuickSettings(MA_FILL_FORWARD_HIGH_1, MA_PATH_HIGH_1);

        var desc = createRD();

        desc.setLabelSettings(INPUT);

        // desc.exportValue(
        // new ValueDescriptor(Values.MA_HIGH_LOW_1, "MA High Low 1", new String[] {
        // MA_INPUT_HIGH_1,
        // MA_PERIOD_HIGH_1, MA_SHIFT_HIGH_1, MA_INPUT_LOW_1, MA_PERIOD_LOW_1,
        // MA_SHIFT_LOW_1 }));
        // desc.exportValue(
        // new ValueDescriptor(Values.MA_HIGH_1, "MA High 1", new String[] {
        // MA_INPUT_HIGH_1,
        // MA_PERIOD_HIGH_1, MA_SHIFT_HIGH_1 }));
        // desc.exportValue(
        // new ValueDescriptor(Values.MA_LOW_1, "MA Low 1",
        // new String[] { MA_INPUT_LOW_1, MA_PERIOD_LOW_1, MA_SHIFT_LOW_1 }));

        desc.declarePath(Values.PATH_HIGH, PATH_HIGH_COLOR);
        // desc.setValueSeries(Values.PATH_HIGH, MA_BARSIZE_1);
        desc.declarePath(Values.PATH_LOW, PATH_LOW_COLOR);
        // desc.setValueSeries(Values.MA_LOW_1, MA_BARSIZE_1);

        desc.declarePath(Values.PATH_MID, PATH_MID_COLOR);
        // desc.setValueSeries(Values.MA_HIGH_LOW_1, MA_BARSIZE_1);

        // desc.declarePath(Values.MA, Inputs.PATH);
        // desc.declareIndicator(Values.MA, Inputs.IND);

        // Signals
        desc.declareSignal(Signals.CROSS_ABOVE, "PRICE_CROSS_ABOVE");
        desc.declareSignal(Signals.CROSS_BELOW, "PRICE_CROSS_BELOW");
    }

    @Override
    public void onBarUpdate(DataContext ctx) {
        var series = ctx.getDataSeries();
        int startHour = getSettings().getInteger(START_HOUR);
        int startMinute = getSettings().getInteger(START_MINUTE);
        int endHour = getSettings().getInteger(END_HOUR);
        int endMinute = getSettings().getInteger(END_MINUTE);

        boolean showMidLine = getSettings().getBoolean(SHOW_MID_LINE);

        long barTime = series.getStartTime(series.size() - 1);

        // Calculate session start and end times
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(barTime);

        // Set session start time
        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        calendar.set(Calendar.MINUTE, startMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long sessionStart = calendar.getTimeInMillis();

        // Set session end time
        calendar.set(Calendar.HOUR_OF_DAY, endHour);
        calendar.set(Calendar.MINUTE, endMinute);
        long sessionEnd = calendar.getTimeInMillis();

        // Check if current bar is within the session time
        // if (barTime >= sessionStart && barTime <= sessionEnd) {
        //     double sessionHigh = series.getDouble(Values.SESSION_HIGH, Double.NEGATIVE_INFINITY);
        //     double sessionLow = series.getDouble(Values.SESSION_LOW, Double.POSITIVE_INFINITY);

        //     // Update session high and low
        //     sessionHigh = Math.max(sessionHigh, series.getHigh(series.size() - 1));
        //     sessionLow = Math.min(sessionLow, series.getLow(series.size() - 1));

        //     series.setDouble(Values.SESSION_HIGH, sessionHigh);
        //     series.setDouble(Values.SESSION_LOW, sessionLow);

        //     // Draw session high and low
        //     Color highColor = getSettings().getColor(HIGH_COLOR);
        //     Color lowColor = getSettings().getColor(LOW_COLOR);
        //     series.setPath(Values.SESSION_HIGH, sessionHigh, highColor);
        //     series.setPath(Values.SESSION_LOW, sessionLow, lowColor);

        //     if (showMidLine) {
        //         double midLevel = (sessionHigh + sessionLow) / 2;
        //         Color midColor = getSettings().getColor(MID_COLOR);
        //         series.setPath(Values.MID_LINE, midLevel, midColor);
        //     }
        // }
    }
}