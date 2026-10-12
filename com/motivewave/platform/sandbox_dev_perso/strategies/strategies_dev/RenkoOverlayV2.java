package custom_studies.strategies.strategies_dev;

import java.awt.Color;

import com.motivewave.platform.sdk.common.BarSize;
// import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.MarkerInfo;
import com.motivewave.platform.sdk.common.Tick;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.StringDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

// /** Renko Simple Overlay */
@StudyHeader(namespace = "com.sbdev", id = "RENKO_OVERLAY_V2", rb = "study_examples.nls.strings", name = "Renko Overlay V2", label = "Renko Overlay V2", desc = "Displays a simple Renko overlay based on user settings", menu = "Custom Studies", overlay = true, signals = true, studyOverlay = true)
public class RenkoOverlayV2 extends Study {

    protected Double brickSize = null; // Taille de la brique
    protected Double previousClose = null; // Renko open
    protected Double realRenko = null; // Renko close

    // derniers états
    protected String[] colorHistory = new String[10]; // Historique des 10 derniers états
    final static String GREEN = "green";
    final static String RED = "red";
    protected String[] crossHistory = new String[10]; // Historique des 10 derniers états
    final static String ABOVE = "above";
    final static String BELOW = "below";

    protected Double[] hiLoPrice1History = new Double[10]; // Historique des 10 derniers états
    protected Double[] hiLoPrice2History = new Double[10]; // Historique des 10 derniers états
    protected String[] hiLoColor1History = new String[10]; // Historique des 10 derniers états
    protected String[] hiLoColor2History = new String[10]; // Historique des 10 derniers états

    // private Boolean inLong = false;
    // private Boolean inShort = false;
    public boolean crossAbove = false;
    public boolean crossBelow = false;

    final static String RENKO_ENABLED = "renkoEnabled";

    final static String INPUT = "input";
    final static String PERIOD = "period";
    final static String RENKO_POINT = "renkoPoint";
    final static String UP_MARKER = "upMarker";
    final static String DOWN_MARKER = "downMarker";
    final static String STOP_UP_MARKER = "stopUpMarker";
    final static String STOP_DOWN_MARKER = "stopDownMarker";
    final static String TOP_PATH = "topPath";
    final static String BOTTOM_PATH = "bottomPath";
    final static String TOP_IND = "topInd";
    final static String BOTTOM_IND = "bottomInd";
    final static String UP_COLOR = "upColor";
    final static String DOWN_COLOR = "downColor";

    enum Values {
        // HIGH, LOW, CROSS, MA_HIGH_LOW_1, MA_HIGH_LOW_2, MA_HIGH_1, MA_LOW_1,
        // MA_HIGH_2, MA_LOW_2
        HIGH, LOW, CROSS, MA_HILO_1, MA_HILO_2

    }

    enum Signals {
        CROSS_HIGH, CROSS_LOW, CROSS_ABOVE, CROSS_BELOW

    }

    protected Enums.MAMethod METHOD;

    // enum Values {
    // MA_HILO_1, MA_HILO_2
    // };

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

    // enum Values {
    // MA_HIGH_LOW_1, MA_HIGH_LOW_2, MA_HIGH_1, MA_LOW_1, MA_HIGH_2, MA_LOW_2
    // }

    // enum Signals {
    // CROSS_ABOVE, CROSS_BELOW
    // }

    // final static String MA_BARSIZE_1 = "maBarSize1";
    // final static String MA_INPUT_HIGH_1 = "maInputHigh1";
    // final static String MA_PERIOD_HIGH_1 = "maPeriodHigh1";
    // final static String MA_PATH_HIGH_1 = "maPathHigh1";

    // final static String MA_PATH_HIGH_LOW_1 = "maPathHighLow1";
    // final static String MA_PATH_HIGH_LOW_2 = "maPathHighLow2";

    // final static String MA_INPUT_LOW_1 = "maInputLow1";
    // final static String MA_PERIOD_LOW_1 = "maPeriodLow1";
    // final static String MA_PATH_LOW_1 = "maPathLow1";

    // final static String MA_BARSIZE_2 = "maBarSize2";
    // final static String MA_INPUT_HIGH_2 = "maInputHigh2";
    // final static String MA_PERIOD_HIGH_2 = "maPeriodHigh2";
    // final static String MA_PATH_HIGH_2 = "maPathHigh2";

    // final static String MA_INPUT_LOW_2 = "maInputLow2";
    // final static String MA_PERIOD_LOW_2 = "maPeriodLow2";
    // final static String MA_PATH_LOW_2 = "maPathLow2";

    // final static String HIGH_COLOR_1 = "highColor1";
    // final static String LOW_COLOR_1 = "lowColor1";
    // final static String HIGH_COLOR_2 = "highColor2";
    // final static String LOW_COLOR_2 = "lowColor2";

    // final static String UP_MARKER_1 = "upMarker1";
    // final static String DOWN_MARKER_1 = "downMarker1";
    // final static String UP_MARKER_2 = "upMarker2";
    // final static String DOWN_MARKER_2 = "downMarker2";

    // public Double mA1 = null;
    // public Double mA2 = null;

    @Override
    public void initialize(Defaults defaults) {

        // gannHighLow = new GannHighLow();
        // gannHighLow.initialize(defaults);

        var sd = createSD();

        sd.addTab(initializeGeneralTab(defaults));
        // sd.addTab(initializeDisplayTab(defaults));

        // Quick Settings (Tool Bar and Popup Editor)
        sd.addQuickSettings(RENKO_POINT);
        sd.addQuickSettings(TOP_PATH, BOTTOM_PATH);

        var desc = createRD();
        desc.setLabelSettings(INPUT, PERIOD, RENKO_POINT);

        desc.exportValue(
                new ValueDescriptor(Values.HIGH, "High", new String[] { INPUT, PERIOD }));
        desc.exportValue(
                new ValueDescriptor(Values.LOW, "Low", new String[] { INPUT, PERIOD }));
        desc.exportValue(
                new ValueDescriptor(Signals.CROSS_HIGH, Enums.ValueType.BOOLEAN, "Cross High"));

        desc.exportValue(
                new ValueDescriptor(Signals.CROSS_LOW, Enums.ValueType.BOOLEAN, "Cross Low"));
        // desc.exportValue(
        // new ValueDescriptor(Values.MA_HIGH_LOW_1, "Ma High Low 1", new String[] {
        // INPUT, PERIOD }));
        // desc.exportValue(
        // new ValueDescriptor(Values.MA_HIGH_LOW_2, "Ma High Low 2", new String[] {
        // INPUT, PERIOD }));

        desc.declarePath(Values.HIGH, TOP_PATH);
        desc.declarePath(Values.LOW, BOTTOM_PATH);

        desc.declareIndicator(Values.HIGH, TOP_IND);
        desc.declareIndicator(Values.LOW, BOTTOM_IND);

        // Signals
        desc.declareSignal(Signals.CROSS_HIGH, "Cross High");
        desc.declareSignal(Signals.CROSS_LOW, "Cross Low");

        desc.setRangeKeys(Values.HIGH, Values.LOW);

        // // GannHiLo

        // var tab = sd.addTab("General Hilo");

        // // Inputs Group
        // var grp = tab.addGroup("First Timeframe");
        // grp.addRow(new BarSizeDescriptor(MA_BARSIZE_1, "Bar Size 1",
        // BarSize.getBarSize(1)));
        // grp.addRow(new InputDescriptor(MA_INPUT_HIGH_1, "Input High 1",
        // Enums.BarInput.HIGH));
        // grp.addRow(new IntegerDescriptor(MA_PERIOD_HIGH_1, "Period High 1", 21, 1,
        // 1000, 1));

        // grp.addRow(new InputDescriptor(MA_INPUT_LOW_1, "Input Low 1",
        // Enums.BarInput.LOW));
        // grp.addRow(new IntegerDescriptor(MA_PERIOD_LOW_1, "Period Low 1", 21, 1,
        // 1000, 1));

        // grp = tab.addGroup("Second Timeframe");

        // grp.addRow(new BarSizeDescriptor(MA_BARSIZE_2, "Bar Size 2",
        // BarSize.getBarSize(5)));
        // grp.addRow(new InputDescriptor(MA_INPUT_HIGH_2, "Input High 2",
        // Enums.BarInput.HIGH));
        // grp.addRow(new IntegerDescriptor(MA_PERIOD_HIGH_2, "Period High 2", 21, 1,
        // 1000, 1));

        // grp.addRow(new InputDescriptor(MA_INPUT_LOW_2, "Input Low 2",
        // Enums.BarInput.LOW));
        // grp.addRow(new IntegerDescriptor(MA_PERIOD_LOW_2, "Period Low 2", 21, 1,
        // 1000, 1));

        // // Colors Group
        // tab = sd.addTab(get("Display Hilo"));

        // grp = tab.addGroup("Colors");
        // grp.addRow(new PathDescriptor(MA_PATH_HIGH_LOW_1, "Path HiLo 1", null, 1.0f,
        // null, true, true, false));
        // grp.addRow(new PathDescriptor(MA_PATH_HIGH_LOW_2, "Path HiLo 2", null, 1.0f,
        // null, true, true, false));

        // // grp = tab.addGroup(get("Colors"));
        // grp.addRow(new ColorDescriptor(HIGH_COLOR_1, get("High Color 1"),
        // defaults.getRedLine()));
        // grp.addRow(new ColorDescriptor(LOW_COLOR_1, get("Low Color 1"),
        // defaults.getGreenLine()));
        // grp.addRow(new ColorDescriptor(HIGH_COLOR_2, get("High Color 2"),
        // defaults.getRedLine()));
        // grp.addRow(new ColorDescriptor(LOW_COLOR_2, get("Low Color 2"),
        // defaults.getGreenLine()));

        // grp = tab.addGroup(get("Markers"));
        // grp.addRow(new MarkerDescriptor(UP_MARKER_1, "Up marker 1",
        // Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(),
        // defaults.getLineColor(), false,
        // true));
        // grp.addRow(new MarkerDescriptor(DOWN_MARKER_1, "Down marker 1",
        // Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(),
        // defaults.getLineColor(),
        // false, true));
        // grp.addRow(new MarkerDescriptor(UP_MARKER_2, "Up marker 2",
        // Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(),
        // defaults.getLineColor(), false,
        // true));
        // grp.addRow(new MarkerDescriptor(DOWN_MARKER_2, "Down marker 2",
        // Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(),
        // defaults.getLineColor(),
        // false, true));

        // sd.addDependency(
        // new EnabledDependency(HIGH_COLOR_1, LOW_COLOR_1, HIGH_COLOR_2, LOW_COLOR_2));

        // sd.addQuickSettings(MA_BARSIZE_1);
        // sd.addQuickSettings(MA_INPUT_HIGH_1, MA_PERIOD_HIGH_1);
        // sd.addQuickSettings(MA_PATH_HIGH_1);

        // // var desc = createRD();

        // desc.setLabelSettings(MA_BARSIZE_1, MA_INPUT_HIGH_1, MA_PERIOD_HIGH_1,
        // MA_INPUT_LOW_1, MA_PERIOD_LOW_1,
        // MA_BARSIZE_2, MA_INPUT_HIGH_2, MA_PERIOD_HIGH_2, MA_INPUT_LOW_2,
        // MA_PERIOD_LOW_2);

        // desc.exportValue(
        // new ValueDescriptor(Values.MA_HIGH_LOW_1, "MA High Low 1",
        // new String[] { MA_INPUT_HIGH_1,
        // MA_PERIOD_HIGH_1, MA_INPUT_LOW_1,
        // MA_PERIOD_LOW_1 }));
        // desc.exportValue(
        // new ValueDescriptor(Values.MA_HIGH_LOW_2, "MA High Low 2",
        // new String[] { MA_INPUT_HIGH_2,
        // MA_PERIOD_HIGH_2, MA_INPUT_LOW_2,
        // MA_PERIOD_LOW_2 }));
        // desc.exportValue(
        // new ValueDescriptor(Values.MA_HIGH_1, "MA High 1", new String[] {
        // MA_INPUT_HIGH_1,
        // MA_PERIOD_HIGH_1 }));
        // desc.exportValue(
        // new ValueDescriptor(Values.MA_LOW_1, "MA Low 1",
        // new String[] { MA_INPUT_LOW_1, MA_PERIOD_LOW_1 }));

        // desc.declarePath(Values.MA_HIGH_1, MA_PATH_HIGH_1);
        // desc.setValueSeries(Values.MA_HIGH_1, MA_BARSIZE_1);
        // desc.declarePath(Values.MA_LOW_1, MA_PATH_LOW_1);
        // desc.setValueSeries(Values.MA_LOW_1, MA_BARSIZE_1);

        // desc.declarePath(Values.MA_HIGH_2, MA_PATH_HIGH_2);
        // desc.setValueSeries(Values.MA_HIGH_2, MA_BARSIZE_2);
        // desc.declarePath(Values.MA_LOW_2, MA_PATH_LOW_2);
        // desc.setValueSeries(Values.MA_LOW_2, MA_BARSIZE_2);

        // desc.declarePath(Values.MA_HIGH_LOW_1, MA_PATH_HIGH_LOW_1);
        // desc.setValueSeries(Values.MA_HIGH_LOW_1, MA_BARSIZE_1);

        // desc.declarePath(Values.MA_HIGH_LOW_2, MA_PATH_HIGH_LOW_2);
        // desc.setValueSeries(Values.MA_HIGH_LOW_2, MA_BARSIZE_2);

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

        // Describe the runtime settings using a 'RuntimeDescriptor'
        // var desc = createRD();

        // Declare paths for drawing
        desc.declarePath(Values.MA_HILO_1, PATH_HILO_1);
        desc.declarePath(Values.MA_HILO_2, PATH_HILO_2);

        // Signals
        desc.declareSignal(Signals.CROSS_ABOVE, "PRICE_CROSS_ABOVE");
        desc.declareSignal(Signals.CROSS_BELOW, "PRICE_CROSS_BELOW");
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
        lines.addRow(new PathDescriptor(TOP_PATH, "High Path", null, Enums.PointType.DOT, Enums.Size.VERY_SMALL,
                true,
                false, true));
        lines.addRow(
                new PathDescriptor(BOTTOM_PATH, "Bottom Path", null, Enums.PointType.DOT,
                        Enums.Size.VERY_SMALL, true,
                        false, true));
        // lines.addRow(new IndicatorDescriptor(TOP_IND, "High Indicator",
        // defaults.getGreen(), X11Colors.WHITE,
        // false,
        // false, true));
        // lines.addRow(new IndicatorDescriptor(BOTTOM_IND, "Bottom Indicator",
        // defaults.getRed(), X11Colors.WHITE,
        // false, false, true));
        lines.addRow(new ColorDescriptor(UP_COLOR, "Up Color", defaults.getGreenLine()));
        lines.addRow(new ColorDescriptor(DOWN_COLOR, "Down Color", defaults.getRedLine()));

        // Section 3
        var markers = tab.addGroup("Markers");
        markers.addRow(new MarkerDescriptor(UP_MARKER, "Up Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL,
                defaults.getGreen(), defaults.getLineColor(), true, true));
        markers.addRow(new MarkerDescriptor(DOWN_MARKER, "Down Marker", Enums.MarkerType.TRIANGLE,
                Enums.Size.SMALL,
                defaults.getRed(), defaults.getLineColor(), true, true));
        markers.addRow(
                new MarkerDescriptor(STOP_UP_MARKER, "Stop Up Marker", Enums.MarkerType.DIAMOND,
                        Enums.Size.LARGE,
                        defaults.getGreen(), defaults.getLineColor(), true, true));
        markers.addRow(
                new MarkerDescriptor(STOP_DOWN_MARKER, "Stop Down Marker", Enums.MarkerType.DIAMOND,
                        Enums.Size.LARGE,
                        defaults.getRed(), defaults.getLineColor(), true, true));

        return tab;
    }

    protected SettingTab initializeDisplayTab(Defaults defaults) {
        var tab = new SettingTab("Display Renko");

        // Section 1
        var markers = tab.addGroup("Markers");
        markers.addRow(new MarkerDescriptor(UP_MARKER, "Up Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL,
                defaults.getGreen(), defaults.getLineColor(), true, true));
        markers.addRow(new MarkerDescriptor(DOWN_MARKER, "Down Marker", Enums.MarkerType.TRIANGLE,
                Enums.Size.SMALL,
                defaults.getRed(), defaults.getLineColor(), true, true));
        markers.addRow(
                new MarkerDescriptor(STOP_UP_MARKER, "Stop Up Marker", Enums.MarkerType.DIAMOND,
                        Enums.Size.LARGE,
                        defaults.getGreen(), defaults.getLineColor(), true, true));
        markers.addRow(
                new MarkerDescriptor(STOP_DOWN_MARKER, "Stop Down Marker", Enums.MarkerType.DIAMOND,
                        Enums.Size.LARGE,
                        defaults.getRed(), defaults.getLineColor(), true, true));

        // Section 2
        var lines = tab.addGroup("Lines");
        lines.addRow(new PathDescriptor(TOP_PATH, "High Path", null, Enums.PointType.DOT, Enums.Size.VERY_SMALL,
                true,
                false, true));
        lines.addRow(
                new PathDescriptor(BOTTOM_PATH, "Bottom Path", null, Enums.PointType.DOT,
                        Enums.Size.VERY_SMALL, true,
                        false, true));
        // lines.addRow(new IndicatorDescriptor(TOP_IND, "High Indicator",
        // defaults.getGreen(), X11Colors.WHITE,
        // false,
        // false, true));
        // lines.addRow(new IndicatorDescriptor(BOTTOM_IND, "Bottom Indicator",
        // defaults.getRed(), X11Colors.WHITE,
        // false, false, true));
        lines.addRow(new ColorDescriptor(UP_COLOR, "Up Color", defaults.getGreenLine()));
        lines.addRow(new ColorDescriptor(DOWN_COLOR, "Down Color", defaults.getRedLine()));

        return tab;
    }

    @Override
    protected void postcalculate(DataContext ctx) {
        realRenko = null;
    }

    // // Méthode pour mettre à jour l'historique de Color
    // private void updateColorHistory(String newColor) {
    // // Décaler toutes les valeurs d'un pas vers la gauche
    // for (int i = 0; i < colorHistory.length - 1; i++) {
    // colorHistory[i] = colorHistory[i + 1];
    // }
    // // Ajouter la nouvelle valeur à la fin du tableau
    // colorHistory[colorHistory.length - 1] = newColor;
    // }

    // // Méthode pour mettre à jour l'historique de Cross
    // private void updateCrossHistory(String newCross) {
    // // Décaler toutes les valeurs d'un pas vers la gauche
    // for (int i = 0; i < crossHistory.length - 1; i++) {
    // crossHistory[i] = crossHistory[i + 1];
    // }
    // // Ajouter la nouvelle valeur à la fin du tableau
    // crossHistory[crossHistory.length - 1] = newCross;
    // }

    // Méthode pour mettre à jour l'historique de Color
    private void updateColorHistory(String newColor) {
        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = colorHistory.length - 1; i > 0; i--) {
            colorHistory[i] = colorHistory[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        colorHistory[0] = newColor;
    }

    // Méthode pour mettre à jour l'historique de Cross
    private void updateCrossHistory(String newCross) {
        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = crossHistory.length - 1; i > 0; i--) {
            crossHistory[i] = crossHistory[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        crossHistory[0] = newCross;
    }

    // Méthode pour mettre à jour l'historique de HiLoPrice1
    private void updateHiLoPrice1History(Double newHiLoPrice1) {
        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = hiLoPrice1History.length - 1; i > 0; i--) {
            hiLoPrice1History[i] = hiLoPrice1History[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        hiLoPrice1History[0] = newHiLoPrice1;
    }

    // Méthode pour mettre à jour l'historique de HiLoPrice2
    private void updateHiLoPrice2History(Double newHiLoPrice2) {
        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = hiLoPrice2History.length - 1; i > 0; i--) {
            hiLoPrice2History[i] = hiLoPrice2History[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        hiLoPrice2History[0] = newHiLoPrice2;
    }

    // Méthode pour mettre à jour l'historique de HiLoColor1
    private void updateHiLoColor1History(String newHiLoColor1) {
        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = hiLoColor1History.length - 1; i > 0; i--) {
            hiLoColor1History[i] = hiLoColor1History[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        hiLoColor1History[0] = newHiLoColor1;
    }

    // Méthode pour mettre à jour l'historique de HiLoColor2
    private void updateHiLoColor2History(String newHiLoColor2) {
        // Décaler toutes les valeurs d'un pas vers la droite
        for (int i = hiLoColor2History.length - 1; i > 0; i--) {
            hiLoColor2History[i] = hiLoColor2History[i - 1];
        }
        // Ajouter la nouvelle valeur au début du tableau
        hiLoColor2History[0] = newHiLoColor2;
    }

    // Méthode pour mettre à jour l'historique de HiLoColor2
    private void autoUpdateHiLoColorHistory(double close) {
        if (hiLoPrice1History[0] != null) {
            if (close < hiLoPrice1History[0]) {
                updateHiLoColor1History(RED);
            } else if (close > hiLoPrice1History[0]) {
                updateHiLoColor1History(GREEN);
            }
        }
        if (hiLoPrice2History[0] != null) {
            if (close < hiLoPrice2History[0]) {
                updateHiLoColor2History(RED);
            } else if (close > hiLoPrice2History[0]) {
                updateHiLoColor2History(GREEN);
            }
        }
    }

    @Override
    protected void calculate(int index, DataContext ctx) {
        var settings = getSettings();
        if (settings == null)
            return;
        var series = ctx.getDataSeries();
        double close = series.getClose(index);

        // GANN HILO

        boolean useTimeframe1 = settings.getBoolean(TF_ENABLED_1);

        if (useTimeframe1) {

            int period1 = settings.getInteger(PERIOD_1);

            // Get data series for the specified timeframe
            var tfSeries1 = ctx.getDataSeries(getSettings().getBarSize(TIMEFRAME_1));
            if (tfSeries1 == null || tfSeries1.size() <= period1)
                return;

            // Get the time of the current bar
            long time = series.getStartTime(index);

            // Find the index in the timeframe data that corresponds to or precedes this
            // time
            int tfIndex1 = tfSeries1.findIndex(time);
            if (tfIndex1 < 0 || tfIndex1 < period1)
                return;

            Double maHigh1 = tfSeries1.ma(settings.getMAMethod(MA_METHOD_1), tfIndex1,
                    period1,
                    Enums.BarInput.HIGH);
            if (maHigh1 == null)
                return;

            Double maLow1 = tfSeries1.ma(settings.getMAMethod(MA_METHOD_1), tfIndex1,
                    period1,
                    Enums.BarInput.LOW);
            if (maLow1 == null)
                return;

            // debug("maHigh1: " + maHigh1);
            // debug("maLow1: " + maLow1);

            if (maHigh1 != null && maLow1 != null) {
                if (close < maLow1) {
                    series.setDouble(index, Values.MA_HILO_1, maHigh1);
                    series.setPathColor(index, Values.MA_HILO_1, settings.getColor(HIGH_COLOR_1));
                    updateHiLoPrice1History(maHigh1);
                    // updateHiLoColor1History(RED);
                } else if (close > maHigh1) {
                    series.setDouble(index, Values.MA_HILO_1, maLow1);
                    series.setPathColor(index, Values.MA_HILO_1, settings.getColor(LOW_COLOR_1));
                    updateHiLoPrice1History(maLow1);
                    // updateHiLoColor1History(GREEN);
                }
            }

        }

        boolean useTimeframe2 = settings.getBoolean(TF_ENABLED_2);

        if (useTimeframe2) {

            int period2 = settings.getInteger(PERIOD_2);

            // Get data series for the specified timeframe
            var tfSeries2 = ctx.getDataSeries(getSettings().getBarSize(TIMEFRAME_2));
            if (tfSeries2 == null || tfSeries2.size() <= period2)
                return;

            // Get the time of the current bar
            long time = series.getStartTime(index);

            // Find the index in the timeframe data that corresponds to or precedes this
            // time
            int tfIndex2 = tfSeries2.findIndex(time);
            if (tfIndex2 < 0 || tfIndex2 < period2)
                return;

            Double maHigh2 = tfSeries2.ma(settings.getMAMethod(MA_METHOD_2), tfIndex2,
                    period2,
                    Enums.BarInput.HIGH);
            if (maHigh2 == null)
                return;

            Double maLow2 = tfSeries2.ma(settings.getMAMethod(MA_METHOD_2), tfIndex2,
                    period2,
                    Enums.BarInput.LOW);
            if (maLow2 == null)
                return;

            // debug("maHigh2: " + maHigh2);
            // debug("maLow2: " + maLow2);

            if (maHigh2 != null && maLow2 != null) {
                if (close < maLow2) {
                    series.setDouble(index, Values.MA_HILO_2, maHigh2);
                    series.setPathColor(index, Values.MA_HILO_2, settings.getColor(HIGH_COLOR_2));
                    updateHiLoPrice2History(maHigh2);
                    // updateHiLoColor2History(RED);
                } else if (close > maHigh2) {
                    series.setDouble(index, Values.MA_HILO_2, maLow2);
                    series.setPathColor(index, Values.MA_HILO_2, settings.getColor(LOW_COLOR_2));
                    updateHiLoPrice2History(maLow2);
                    // updateHiLoColor2History(GREEN);
                }
            }
        }

        // RENKO

        boolean useRenko = settings.getBoolean(RENKO_ENABLED);

        if (useRenko) {

            int period = 20;
            if (index <= period)
                return;
            // Object input = settings.getInput(INPUT);
            Object input = Enums.BarInput.CLOSE;

            // double close = series.getClose(index);

            // ticks to points
            double renkoPoints = settings.getInteger(RENKO_POINT) / 4;

            Double prev = series.getDouble(index - 1, input);
            Double current = series.getDouble(index, input);
            if (prev == null || current == null)
                return;

            crossAbove = false;
            crossBelow = false;

            Double cross = null;

            Double res = null;
            Double supp = null;

            Double currentClose = current;

            if (realRenko == null) {
                // Initialisation lors de la première exécution
                // Trouver le prix inférieur se terminant par une dizaine
                double lowerBound = Math.floor(currentClose / 10) * 10; // Trouver la dizaine inférieure
                double upperBound = lowerBound + renkoPoints; // La dizaine suivante se terminant par 5

                // Si le prix est plus proche de la dizaine suivante (10, 20, etc.)
                if (currentClose > lowerBound + renkoPoints) {
                    lowerBound = lowerBound + renkoPoints; // Ajuster pour commencer à 5
                    upperBound = lowerBound + renkoPoints; // Ajuster pour aller jusqu'à la dizaine
                }

                realRenko = upperBound;
                previousClose = lowerBound;
                brickSize = Math.abs(realRenko - previousClose);
                // updateColorHistory(realRenko > previousClose);
                if (realRenko > previousClose) {
                    updateColorHistory(GREEN);
                    autoUpdateHiLoColorHistory(close);
                } else {
                    updateColorHistory(RED);
                    autoUpdateHiLoColorHistory(close);
                }
            } else {
                // if (colorHistory[9] == GREEN && currentClose - realRenko >= brickSize) {
                if (colorHistory[0] == GREEN && currentClose - realRenko >= brickSize) {
                    realRenko = realRenko + Math.floor((currentClose - realRenko) / brickSize) * brickSize;
                    previousClose = realRenko - brickSize;
                    crossAbove = true;
                    cross = 1.0;
                    // updateColorHistory(true);
                    updateColorHistory(GREEN);
                    updateCrossHistory(ABOVE);
                    autoUpdateHiLoColorHistory(close);
                    // } else if (colorHistory[9] == GREEN && realRenko - currentClose >= brickSize
                    // * 2) {
                } else if (colorHistory[0] == GREEN && realRenko - currentClose >= brickSize * 2) {
                    realRenko = realRenko - Math.floor((realRenko - currentClose) / brickSize) * brickSize;
                    previousClose = realRenko + brickSize;
                    crossBelow = true;
                    cross = -1.0;
                    // updateColorHistory(false);
                    updateColorHistory(RED);
                    updateCrossHistory(BELOW);
                    autoUpdateHiLoColorHistory(close);
                    // } else if (colorHistory[9] == RED && currentClose - realRenko >= brickSize *
                    // 2) {
                } else if (colorHistory[0] == RED && currentClose - realRenko >= brickSize * 2) {
                    realRenko = realRenko + Math.floor((currentClose - realRenko) / brickSize) * brickSize;
                    previousClose = realRenko - brickSize;
                    crossAbove = true;
                    cross = 1.0;
                    // updateColorHistory(true);
                    updateColorHistory(GREEN);
                    updateCrossHistory(ABOVE);
                    autoUpdateHiLoColorHistory(close);
                    // } else if (colorHistory[9] == RED && realRenko - currentClose >= brickSize) {
                } else if (colorHistory[0] == RED && realRenko - currentClose >= brickSize) {
                    realRenko = realRenko - Math.floor((realRenko - currentClose) / brickSize) * brickSize;
                    previousClose = realRenko + brickSize;
                    crossBelow = true;
                    cross = -1.0;
                    // updateColorHistory(false);
                    updateColorHistory(RED);
                    updateCrossHistory(BELOW);
                    autoUpdateHiLoColorHistory(close);
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

            // Set colors for the Renko bricks
            // Color brickColor = colorHistory[9] == GREEN ? settings.getColor(UP_COLOR)
            Color brickColor = colorHistory[0] == GREEN ? settings.getColor(UP_COLOR)
                    : settings.getColor(DOWN_COLOR);

            // Plot the Renko lines
            series.setPathColor(index - 1, Values.LOW, brickColor);
            series.setPathColor(index - 1, Values.HIGH, brickColor);

            series.setDouble(index - 1, Values.LOW, supp);
            series.setDouble(index, Values.HIGH, res);

            series.setDouble(index, Values.CROSS, cross);

            series.setComplete(index, complete);

        }

        // SIGNALS STRATEGY

        // Boolean stratHigh1 = colorHistory[9] == GREEN && colorHistory[8] == RED &&
        // colorHistory[7] == RED
        // && colorHistory[6] == GREEN && colorHistory[5] == GREEN && colorHistory[4] ==
        // GREEN
        // && colorHistory[3] == GREEN && colorHistory[2] == GREEN && colorHistory[1] ==
        // GREEN;
        // // 6 GREEN - retracement 2 - TP 1
        // Boolean stratLow1 = colorHistory[9] == RED && colorHistory[8] == GREEN &&
        // colorHistory[7] == GREEN
        // && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] == RED
        // && colorHistory[3] == RED && colorHistory[2] == RED && colorHistory[1] ==
        // RED;
        // // 6 RED - retracement 2 - TP 1

        // Boolean stratHigh2 = colorHistory[9] == GREEN && colorHistory[8] == RED &&
        // colorHistory[7] == RED
        // && colorHistory[6] == RED && colorHistory[5] == GREEN && colorHistory[4] ==
        // GREEN
        // && colorHistory[3] == GREEN && colorHistory[2] == GREEN && colorHistory[1] ==
        // GREEN;
        // // 7 GREEN - retracement 3 - TP 1
        // Boolean stratLow2 = colorHistory[9] == RED && colorHistory[8] == GREEN &&
        // colorHistory[7] == GREEN
        // && colorHistory[6] == GREEN && colorHistory[5] == RED && colorHistory[4] ==
        // RED
        // && colorHistory[3] == RED && colorHistory[2] == RED && colorHistory[1] ==
        // RED;
        // // 7 RED - retracement 3 - TP 1

        // Boolean stratHigh3 = colorHistory[9] == GREEN && colorHistory[8] == RED &&
        // colorHistory[7] == GREEN
        // && colorHistory[6] == GREEN && colorHistory[5] == GREEN && colorHistory[4] ==
        // GREEN
        // && colorHistory[3] == RED && colorHistory[2] == GREEN && colorHistory[1] ==
        // GREEN;
        // // 4 GREEN - double retracement 1 - TP 1
        // Boolean stratLow3 = colorHistory[9] == RED && colorHistory[8] == GREEN &&
        // colorHistory[7] == RED
        // && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] == RED
        // && colorHistory[3] == GREEN && colorHistory[2] == RED && colorHistory[1] ==
        // RED;
        // // 4 RED - double retracement 1 - TP 1

        // Boolean stratHigh4 = colorHistory[9] == GREEN && colorHistory[8] == RED &&
        // colorHistory[7] == RED
        // && colorHistory[6] == GREEN && colorHistory[5] == GREEN && colorHistory[4] ==
        // GREEN
        // && colorHistory[3] == GREEN && colorHistory[2] == RED && colorHistory[1] ==
        // RED;
        // // 6 GREEN - retracement 2 - TP 1
        // Boolean stratLow4 = colorHistory[9] == RED && colorHistory[8] == GREEN &&
        // colorHistory[7] == GREEN
        // && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] == RED
        // && colorHistory[3] == RED && colorHistory[2] == GREEN && colorHistory[1] ==
        // GREEN;
        // // 6 RED - retracement 2 - TP 1

        // Boolean stratHigh5 = colorHistory[9] == GREEN && colorHistory[8] == RED &&
        // colorHistory[7] == GREEN
        // && colorHistory[6] == GREEN && colorHistory[5] == GREEN && colorHistory[4] ==
        // GREEN
        // && colorHistory[3] == GREEN && colorHistory[2] == GREEN && colorHistory[1] ==
        // GREEN;
        // // 6 GREEN - retracement 2 - TP 1
        // Boolean stratLow5 = colorHistory[9] == RED && colorHistory[8] == GREEN &&
        // colorHistory[7] == RED
        // && colorHistory[6] == RED && colorHistory[5] == RED && colorHistory[4] == RED
        // && colorHistory[3] == RED && colorHistory[2] == RED && colorHistory[1] ==
        // RED;
        // // 6 RED - retracement 2 - TP 1

        Boolean stratHigh1 = colorHistory[0] == GREEN && colorHistory[1] == RED && colorHistory[2] == GREEN
                && colorHistory[3] == GREEN && colorHistory[4] == GREEN && colorHistory[5] == GREEN
                && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] == GREEN
        && colorHistory[9] == GREEN;
        // 8 GREEN - retracement 1 - TP 1
        Boolean stratLow1 = colorHistory[0] == RED && colorHistory[1] == GREEN && colorHistory[2] == RED
                && colorHistory[3] == RED && colorHistory[4] == RED && colorHistory[5] == RED
                && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] == RED
        && colorHistory[9] == RED;
        // 8 RED - retracement 1 - TP 1
        Boolean stratHigh2 = colorHistory[0] == GREEN && colorHistory[1] == RED && colorHistory[2] == RED
                && colorHistory[3] == GREEN && colorHistory[4] == GREEN && colorHistory[5] == GREEN
                && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] == GREEN
        && colorHistory[9] == GREEN;
        // 7 GREEN - retracement 2 - TP 1
        Boolean stratLow2 = colorHistory[0] == RED && colorHistory[1] == GREEN && colorHistory[2] == GREEN
                && colorHistory[3] == RED && colorHistory[4] == RED && colorHistory[5] == RED
                && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] == RED
        && colorHistory[9] == RED;
        // 7 RED - retracement 2 - TP 1
        Boolean stratHigh3 = colorHistory[0] == GREEN && colorHistory[1] == RED && colorHistory[2] == RED
                && colorHistory[3] == RED && colorHistory[4] == GREEN && colorHistory[5] == GREEN
                && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] == GREEN
                && colorHistory[9] == GREEN;
        // 6 GREEN - retracement 3 - TP 1
        Boolean stratLow3 = colorHistory[0] == RED && colorHistory[1] == GREEN && colorHistory[2] == GREEN
                && colorHistory[3] == GREEN && colorHistory[4] == RED && colorHistory[5] == RED
                && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] == RED
                && colorHistory[9] == RED;
        // 6 RED - retracement 3 - TP 1
        Boolean stratHigh4 = colorHistory[0] == GREEN && colorHistory[1] == RED && colorHistory[2] == RED
                && colorHistory[3] == RED && colorHistory[4] == RED && colorHistory[5] == GREEN
                && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] == GREEN
                && colorHistory[9] == GREEN;
        // 5 GREEN - retracement 4 - TP 1
        Boolean stratLow4 = colorHistory[0] == RED && colorHistory[1] == GREEN && colorHistory[2] == GREEN
                && colorHistory[3] == GREEN && colorHistory[4] == GREEN && colorHistory[5] == RED
                && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] == RED
                && colorHistory[9] == RED;
        // 5 RED - retracement 4 - TP 1
        Boolean stratHigh5 = colorHistory[0] == GREEN && colorHistory[1] == RED && colorHistory[2] == RED
                && colorHistory[3] == RED && colorHistory[4] == RED && colorHistory[5] == RED
                && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] == GREEN
                && colorHistory[9] == GREEN;
        // 4 GREEN - retracement 5 - TP 1
        Boolean stratLow5 = colorHistory[0] == RED && colorHistory[1] == GREEN && colorHistory[2] == GREEN
                && colorHistory[3] == GREEN && colorHistory[4] == GREEN && colorHistory[5] == GREEN
                && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] == RED
                && colorHistory[9] == RED;
        // 4 RED - retracement 5 - TP 1
        Boolean stratHigh6 = colorHistory[0] == GREEN && colorHistory[1] == RED && colorHistory[2] == RED
                && colorHistory[3] == RED && colorHistory[4] == RED && colorHistory[5] == RED
                && colorHistory[6] == RED && colorHistory[7] == GREEN && colorHistory[8] == GREEN
                && colorHistory[9] == GREEN;
        // 3 GREEN - retracement 6 - TP 1
        Boolean stratLow6 = colorHistory[0] == RED && colorHistory[1] == GREEN && colorHistory[2] == GREEN
                && colorHistory[3] == GREEN && colorHistory[4] == GREEN && colorHistory[5] == GREEN
                && colorHistory[6] == GREEN && colorHistory[7] == RED && colorHistory[8] == RED
                && colorHistory[9] == RED;
        // 3 RED - retracement 6 - TP 1
        // Boolean stratHigh7 = colorHistory[0] == GREEN && colorHistory[1] == RED && colorHistory[2] == GREEN
        //         && colorHistory[3] == GREEN && colorHistory[4] == RED && colorHistory[5] == GREEN
        //         && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] == GREEN;
        // // 4 GREEN - retracement 2x1 - TP 1
        // Boolean stratLow7 = colorHistory[0] == RED && colorHistory[1] == GREEN && colorHistory[2] == RED
        //         && colorHistory[3] == RED && colorHistory[4] == GREEN && colorHistory[5] == RED
        //         && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] == RED;
        // // 4 RED - retracement 2x1 - TP 1
        // Boolean stratHigh8 = colorHistory[0] == GREEN && colorHistory[1] == RED && colorHistory[2] == GREEN
        //         && colorHistory[3] == GREEN && colorHistory[4] == GREEN && colorHistory[5] == GREEN
        //         && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] == RED
        //         && colorHistory[9] == GREEN;
        // // 6 GREEN - retracement 2x1 - TP 1
        // Boolean stratLow8 = colorHistory[0] == RED && colorHistory[1] == GREEN && colorHistory[2] == RED
        //         && colorHistory[3] == RED && colorHistory[4] == RED && colorHistory[5] == RED
        //         && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] == GREEN
        //         && colorHistory[9] == RED;
        // // 6 RED - retracement 2x1 - TP 1

        Boolean stratHigh = stratHigh1 || stratHigh2 || stratHigh3 || stratHigh4 ||
                stratHigh5 || stratHigh6 
                // || stratHigh7 || stratHigh8
                ;
        Boolean stratLow = stratLow1 || stratLow2 || stratLow3 || stratLow4 ||
                stratLow5 || stratLow6 
                // || stratLow7 || stratLow8
                ;

        Boolean okLong = hiLoColor1History[0] == GREEN
                // && hiLoColor1History[1] == GREEN
                // && hiLoColor1History[2] == GREEN
                // && hiLoColor1History[3] == GREEN
                // && hiLoColor1History[4] == GREEN
                // && hiLoColor1History[5] == GREEN
                // && hiLoColor1History[6] == GREEN
                // && hiLoColor1History[7] == GREEN

                && hiLoColor2History[0] == GREEN
                // && hiLoColor2History[1] == GREEN
                // && hiLoColor2History[2] == GREEN
                // && hiLoColor2History[3] == GREEN
                // && hiLoColor2History[4] == GREEN
                // && hiLoColor2History[5] == GREEN
                // && hiLoColor2History[6] == GREEN
                // && hiLoColor2History[7] == GREEN
                // && hiLoColor2History[8] == GREEN
                // && hiLoColor2History[9] == GREEN
                && hiLoPrice1History[0] < series.getClose(index)
                && hiLoPrice2History[0] < series.getClose(index)
                // && hiLoPrice1History[0] > hiLoPrice2History[0]
                ;
        Boolean okShort = hiLoColor1History[0] == RED
                // && hiLoColor1History[1] == RED
                // && hiLoColor1History[2] == RED
                // && hiLoColor1History[3] == RED
                // && hiLoColor1History[4] == RED
                // && hiLoColor1History[5] == RED
                // && hiLoColor1History[6] == RED
                // && hiLoColor1History[7] == RED

                && hiLoColor2History[0] == RED
                // && hiLoColor2History[1] == RED
                // && hiLoColor2History[2] == RED
                // && hiLoColor2History[3] == RED
                // && hiLoColor2History[4] == RED
                // && hiLoColor2History[5] == RED
                // && hiLoColor2History[6] == RED
                // && hiLoColor2History[7] == RED
                // && hiLoColor2History[8] == RED
                // && hiLoColor2History[9] == RED
                && hiLoPrice1History[0] > series.getClose(index)
                && hiLoPrice2History[0] > series.getClose(index)
                // && hiLoPrice1History[0] < hiLoPrice2History[0]
                ;

        // var series1 = ctx.getDataSeries(getSettings().getBarSize(MA_BARSIZE_1));

        Double priceHiLo1 = series.getDouble(index - 1, Values.MA_HILO_1);
        Double priceHiLo2 = series.getDouble(index - 1, Values.MA_HILO_2);

        if (priceHiLo1 == null || priceHiLo2 == null)
            return;

        Double priceSupp = series.getDouble(index - 1, Values.LOW);
        Double priceRes = series.getDouble(index, Values.HIGH);

        if (priceSupp == null || priceRes == null)
            return;

        // series.setDouble(index - 1, Values.LOW, supp);
        // series.setDouble(index, Values.HIGH, res);

        // if (priceHiLo1 == null)
        // continue;
        // if (crossBelow && colorHistory[8] == GREEN && stratLow) {
        if (crossBelow && stratLow && okShort) {
            // if (crossBelow && stratLow) {
            // if (crossBelow && colorHistory[8] == GREEN && okShort) {
            // if (crossBelow && colorHistory[1] == GREEN && okShort) {
            // var c = new Coordinate(series.getStartTime(index), res);
            var c = new Coordinate(series.getStartTime(index), priceRes);
            var marker = settings.getMarker(DOWN_MARKER);
            // String msg = "CROSS_LOW => current: " + format(current)
            String msg = "CROSS_LOW => current: " + format(priceSupp)
                    + " hiLoPrice1: " + format(priceHiLo1)
                    + " hiLoPrice2: " + format(priceHiLo2)
            // + format(priceHiLo1) + " hiLoPrice2History: "
            // + format(hiLoPrice2History[0]) + " Last Price: " +
            // format(series.getClose(index))
            // + " okLong: " + okLong + " okShort: " + okShort
            ;
            if (marker.isEnabled())
                addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
            // ctx.signal(index, Signals.CROSS_LOW, msg, current);
            ctx.signal(index, Signals.CROSS_LOW, msg, priceSupp);
        }

        // if (crossAbove && colorHistory[8] == RED && stratHigh) {
        if (crossAbove && stratHigh && okLong) {
            // if (crossAbove && stratHigh) {
            // if (crossAbove && colorHistory[8] == RED && okLong) {
            // if (crossAbove && colorHistory[1] == RED && okLong) {
            // var c = new Coordinate(series.getStartTime(index), res);
            var c = new Coordinate(series.getStartTime(index), priceRes);
            var marker = settings.getMarker(UP_MARKER);
            // String msg = "CROSS_HIGH => current: " + format(current)
            String msg = "CROSS_HIGH => current: " + format(priceSupp)
                    + " hiLoPrice1: " + format(priceHiLo1)
                    + " hiLoPrice2: " + format(priceHiLo2)
            // + format(hiLoPrice1History[0]) + " hiLoPrice2History: "
            // + format(hiLoPrice2History[0]) + " Last Price: " +
            // format(series.getClose(index))
            // + " okLong: " + okLong + " okShort: " + okShort
            // + " " + hiLoColor1History[0] + " " + hiLoColor2History[0]
            // + " " + hiLoPrice1History[0]
            // + " " + hiLoPrice2History[0]
            ;
            if (marker.isEnabled())
                addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
            // ctx.signal(index, Signals.CROSS_HIGH, msg, current);
            ctx.signal(index, Signals.CROSS_HIGH, msg, priceSupp);
        }
    }

    @Override
    public int getMinBars() {
        // return getSettings().getInteger(Inputs.PERIOD)*2;
        int p1 = getSettings().getInteger(PERIOD_1);
        int p2 = getSettings().getInteger(PERIOD_2);
        return Math.max(p1, p2) * 2;
    }

    @Override
    public void onLoad(Defaults defaults) {
        DataContext ctx = getDataContext(); // récupération du contexte de données
        getTimeframeContext(ctx);
    }

    // @Override
    // public void onBarUpdate(DataContext ctx) {
    // if (!getSettings().isBarUpdates())
    // return;
    // getTimeframeContext(ctx);

    // // var series = ctx.getDataSeries();
    // // int index = series.size() - 1;
    // // doUpdate(index, ctx);
    // }

    // @Override
    // public void onBarClose(DataContext ctx) {
    // getTimeframeContext(ctx);
    // }

    // @Override
    // public void onTick(DataContext ctx, Tick tick) {
    // getTimeframeContext(ctx);
    // }

    protected void getTimeframeContext(DataContext ctx) {
        var settings = getSettings();
        if (settings.getBoolean(TF_ENABLED_1)) {
            ctx.getDataSeries(settings.getBarSize(TIMEFRAME_1));
        }
        if (settings.getBoolean(TF_ENABLED_2)) {
            ctx.getDataSeries(settings.getBarSize(TIMEFRAME_2));
        }
    }

    // protected void calcMaHiLo(DataContext ctx) {

    // // Get the settings as defined by the user in the study dialog
    // var settings = getSettings();

    // // Get access to the data series of the current context
    // var series = ctx.getDataSeries();

    // debug("calcMaHiLo");
    // if (series == null)
    // return;

    // debug("series: " + series);

    // boolean updates = settings.isBarUpdates();
    // for (int index = 1; index < series.size(); index++) {
    // if (!updates && !series.isBarComplete(index))
    // continue;

    // debug("index: " + index);
    // // if (series.size() <= 1) return;
    // // int index = series.size()-1;
    // double close = series.getClose(index);

    // int period1 = settings.getInteger(PERIOD_1);
    // boolean useTimeframe1 = settings.getBoolean(TF_ENABLED_1);

    // if (useTimeframe1) {

    // // Get data series for the specified timeframe
    // var tfSeries1 = ctx.getDataSeries(getSettings().getBarSize(TIMEFRAME_1));
    // if (tfSeries1 == null || tfSeries1.size() <= period1)
    // return;

    // // Get the time of the current bar
    // long time = series.getStartTime(index);

    // // Find the index in the timeframe data that corresponds to or precedes this
    // // time
    // int tfIndex1 = tfSeries1.findIndex(time);
    // if (tfIndex1 < 0 || tfIndex1 < period1)
    // return;

    // Double maHigh1 = tfSeries1.ma(settings.getMAMethod(MA_METHOD_1), tfIndex1,
    // period1,
    // Enums.BarInput.HIGH);
    // if (maHigh1 == null)
    // return;

    // Double maLow1 = tfSeries1.ma(settings.getMAMethod(MA_METHOD_1), tfIndex1,
    // period1,
    // Enums.BarInput.LOW);
    // if (maLow1 == null)
    // return;

    // debug("maHigh1: " + maHigh1);
    // debug("maLow1: " + maLow1);

    // if (maHigh1 != null && maLow1 != null) {
    // if (close < maLow1) {
    // series.setDouble(index, Values.MA_HILO_1, maHigh1);
    // series.setPathColor(index, Values.MA_HILO_1,
    // settings.getColor(HIGH_COLOR_1));
    // } else if (close > maHigh1) {
    // series.setDouble(index, Values.MA_HILO_1, maLow1);
    // series.setPathColor(index, Values.MA_HILO_1, settings.getColor(LOW_COLOR_1));
    // }
    // }

    // }

    // int period2 = settings.getInteger(PERIOD_2);
    // boolean useTimeframe2 = settings.getBoolean(TF_ENABLED_2);

    // if (useTimeframe2) {

    // // Get data series for the specified timeframe
    // var tfSeries2 = ctx.getDataSeries(getSettings().getBarSize(TIMEFRAME_2));
    // if (tfSeries2 == null || tfSeries2.size() <= period2)
    // return;

    // // Get the time of the current bar
    // long time = series.getStartTime(index);

    // // Find the index in the timeframe data that corresponds to or precedes this
    // // time
    // int tfIndex2 = tfSeries2.findIndex(time);
    // if (tfIndex2 < 0 || tfIndex2 < period2)
    // return;

    // Double maHigh2 = tfSeries2.ma(settings.getMAMethod(MA_METHOD_2), tfIndex2,
    // period2,
    // Enums.BarInput.HIGH);
    // if (maHigh2 == null)
    // return;

    // Double maLow2 = tfSeries2.ma(settings.getMAMethod(MA_METHOD_2), tfIndex2,
    // period2,
    // Enums.BarInput.LOW);
    // if (maLow2 == null)
    // return;

    // debug("maHigh2: " + maHigh2);
    // debug("maLow2: " + maLow2);

    // if (maHigh2 != null && maLow2 != null) {
    // if (close < maLow2) {
    // series.setDouble(index, Values.MA_HILO_2, maHigh2);
    // series.setPathColor(index, Values.MA_HILO_2,
    // settings.getColor(HIGH_COLOR_2));
    // } else if (close > maHigh2) {
    // series.setDouble(index, Values.MA_HILO_2, maLow2);
    // series.setPathColor(index, Values.MA_HILO_2, settings.getColor(LOW_COLOR_2));
    // }
    // }
    // }
    // }
    // }

    // @Override
    // public void onLoad(Defaults defaults) {
    // int p1 = getSettings().getInteger(MA_PERIOD_HIGH_1);
    // int p2 = getSettings().getInteger(MA_PERIOD_HIGH_2);
    // setMinBars(Math.max(p1, p2) * 2);
    // }

    // public void calcMaHiLo(DataContext ctx,
    // BarSize barsize,
    // Enums.MAMethod maMethod,
    // Color maColorHigh,
    // Color maColorLow,
    // int maPeriod,
    // Object maValueHiLo,
    // String hiLoNb) {

    // // Vérifier si la série des données est complète) {

    // var series = ctx.getDataSeries(barsize);
    // int indexMin = 0;
    // int indexMax = series.size() - 1;
    // if (indexMin <= maPeriod) {
    // indexMin = maPeriod - 1;
    // }

    // var settings = getSettings();

    // Double previousValue = null; // Stocker la dernière valeur disponible

    // for (int index = indexMin; index <= indexMax; ++index) {

    // boolean complete = series.isBarComplete(index);

    // Double maHigh = series.ma(maMethod, index, maPeriod, Enums.BarInput.HIGH);
    // Double maLow = series.ma(maMethod, index, maPeriod, Enums.BarInput.LOW);
    // float close = series.getClose(index);

    // Double valueToSet = null;
    // String colorToSet = null;
    // MarkerInfo marker = null;
    // Enums.Position position = null;

    // if (maHigh != null && maLow != null) {
    // if (close < maLow) {
    // valueToSet = maHigh;
    // colorToSet = RED;
    // marker = settings.getMarker(DOWN_MARKER);
    // position = Enums.Position.TOP;

    // // var c = new Coordinate(series.getStartTime(index), valueToSet);
    // // var marker = settings.getMarker(DOWN_MARKER);
    // // String msg = "Close: " + format(series.getClose(index));
    // // if (marker.isEnabled())
    // // addFigure(new Marker(c, Enums.Position.TOP, marker, msg));

    // series.setPathColor(index, maValueHiLo, maColorHigh);
    // complete = false;
    // } else if (close > maHigh) {
    // valueToSet = maLow;
    // colorToSet = GREEN;
    // marker = settings.getMarker(UP_MARKER);
    // position = Enums.Position.BOTTOM;

    // // var c = new Coordinate(series.getStartTime(index), valueToSet);
    // // var marker = settings.getMarker(UP_MARKER);
    // // String msg = "Close: " + format(series.getClose(index));
    // // if (marker.isEnabled())
    // // addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));

    // series.setPathColor(index, maValueHiLo, maColorLow);
    // complete = false;
    // }
    // // } else if (previousValue != null) {
    // // // Utiliser la dernière valeur disponible si maHigh et maLow sont null
    // // valueToSet = previousValue;
    // // }

    // // if (valueToSet != null) {
    // series.setDouble(index, maValueHiLo, valueToSet);
    // previousValue = valueToSet; // Mettre à jour la dernière valeur
    // if (hiLoNb == "1") {

    // updateHiLoPrice1History(valueToSet);
    // if (colorToSet != hiLoColor1History[0]) {
    // updateHiLoColor1History(colorToSet);
    // // var c = new Coordinate(series.getStartTime(index), valueToSet);
    // // String msg = "Close: " + format(series.getClose(index));
    // // if (marker.isEnabled())
    // // addFigure(new Marker(c, position, marker, msg));
    // }
    // }
    // if (hiLoNb == "2") {
    // updateHiLoPrice2History(valueToSet);
    // if (colorToSet != hiLoColor2History[0]) {
    // updateHiLoColor2History(colorToSet);
    // // var c = new Coordinate(series.getStartTime(index), valueToSet);
    // // String msg = "Close: " + format(series.getClose(index));
    // // if (marker.isEnabled())
    // // addFigure(new Marker(c, position, marker, msg));
    // }

    // }
    // }

    // series.setComplete(index, complete);

    // }

    // // debug("previousValue : " + previousValue);

    // }

    // @Override
    // // protected void calculate(int index, DataContext ctx) {
    // protected void calculateValues(DataContext ctx) {
    // var settings = getSettings();
    // if (settings == null)
    // return;

    // calcMaHiLo(
    // ctx,
    // getSettings().getBarSize(MA_BARSIZE_1),
    // METHOD,
    // settings.getColor(HIGH_COLOR_1),
    // settings.getColor(LOW_COLOR_1),
    // settings.getInteger(MA_PERIOD_HIGH_1),
    // Values.MA_HIGH_LOW_1,
    // "1");

    // calcMaHiLo(
    // ctx,
    // getSettings().getBarSize(MA_BARSIZE_2),
    // METHOD,
    // settings.getColor(HIGH_COLOR_2),
    // settings.getColor(LOW_COLOR_2),
    // settings.getInteger(MA_PERIOD_HIGH_2),
    // Values.MA_HIGH_LOW_2,
    // "2");

    // }
}