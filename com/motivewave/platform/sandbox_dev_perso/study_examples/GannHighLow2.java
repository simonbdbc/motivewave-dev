// package study_examples;

// import java.awt.Color;

// import com.motivewave.platform.sdk.common.BarSize;
// import com.motivewave.platform.sdk.common.DataContext;
// import com.motivewave.platform.sdk.common.DataSeries;
// import com.motivewave.platform.sdk.common.Defaults;
// import com.motivewave.platform.sdk.common.Enums;
// import com.motivewave.platform.sdk.common.Inputs;
// import com.motivewave.platform.sdk.common.PathInfo;
// import com.motivewave.platform.sdk.common.TimeFrame;
// import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
// import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
// import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
// import com.motivewave.platform.sdk.common.desc.EnabledDependency;
// import com.motivewave.platform.sdk.common.desc.InputDescriptor;
// import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
// import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
// import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
// import com.motivewave.platform.sdk.common.desc.PathDescriptor;
// import com.motivewave.platform.sdk.common.desc.SettingDescriptor;
// import com.motivewave.platform.sdk.common.desc.SettingGroup;
// import com.motivewave.platform.sdk.common.desc.SettingTab;
// import com.motivewave.platform.sdk.common.desc.TimeFrameDescriptor;
// import com.motivewave.platform.sdk.study.Plot;
// import com.motivewave.platform.sdk.study.Study;
// import com.motivewave.platform.sdk.study.StudyHeader;

// import com.motivewave.platform.sdk.common.desc.ValueDescriptor;

// /** Gann High Low Renko Indicator with Multiple Timeframes */
// @StudyHeader(namespace = "com.sbdev", id = "GANN_HILO_2", name = "Gann High Low 2", desc = "Custom Gann High Low Indicator with Multiple Timeframes", menu = "Custom Studies", overlay = true, signals = true, studyOverlay = true)

// public class GannHighLow2 extends Study {

//     // protected Enums.MAMethod METHOD;

//     // enum Values {
//     // MA_HIGH_LOW_1, MA_HIGH_LOW_2
//     // , MA_HIGH_1, MA_LOW_1, MA_HIGH_2, MA_LOW_2
//     // }

//     // enum Signals {
//     // CROSS_ABOVE, CROSS_BELOW
//     // }

//     // final static String MA_BARSIZE_1 = "maBarSize1";
//     // final static String MA_INPUT_HIGH_1 = "maInputHigh1";
//     // final static String MA_PERIOD_HIGH_1 = "maPeriodHigh1";
//     // final static String MA_PATH_HIGH_1 = "maPathHigh1";

//     // final static String MA_PATH_HIGH_LOW_1 = "maPathHighLow1";
//     // final static String MA_PATH_HIGH_LOW_2 = "maPathHighLow2";

//     // final static String MA_INPUT_LOW_1 = "maInputLow1";
//     // final static String MA_PERIOD_LOW_1 = "maPeriodLow1";
//     // final static String MA_PATH_LOW_1 = "maPathLow1";

//     // final static String MA_BARSIZE_2 = "maBarSize2";
//     // final static String MA_INPUT_HIGH_2 = "maInputHigh2";
//     // final static String MA_PERIOD_HIGH_2 = "maPeriodHigh2";
//     // final static String MA_PATH_HIGH_2 = "maPathHigh2";

//     // final static String MA_INPUT_LOW_2 = "maInputLow2";
//     // final static String MA_PERIOD_LOW_2 = "maPeriodLow2";
//     // final static String MA_PATH_LOW_2 = "maPathLow2";

//     // final static String HIGH_COLOR_1 = "highColor1";
//     // final static String LOW_COLOR_1 = "lowColor1";
//     // final static String HIGH_COLOR_2 = "highColor2";
//     // final static String LOW_COLOR_2 = "lowColor2";

//     // final static String UP_MARKER = "upMarker";
//     // final static String DOWN_MARKER = "downMarker";
//     // // final static String UP_MARKER_1 = "upMarker1";
//     // // final static String DOWN_MARKER_1 = "downMarker1";
//     // // final static String UP_MARKER_2 = "upMarker2";
//     // // final static String DOWN_MARKER_2 = "downMarker2";

//     // public Double mA1 = null;
//     // public Double mA2 = null;

//     // @Override
//     // public void initialize(Defaults defaults) {
//     // var sd = createSD();
//     // var tab = sd.addTab("General Hilo");

//     // // Inputs Group
//     // var grp = tab.addGroup("First Timeframe");
//     // grp.addRow(new BarSizeDescriptor(MA_BARSIZE_1, "Bar Size 1",
//     // BarSize.getBarSize(1)));
//     // grp.addRow(new InputDescriptor(MA_INPUT_HIGH_1, "Input High 1",
//     // Enums.BarInput.HIGH));
//     // grp.addRow(new IntegerDescriptor(MA_PERIOD_HIGH_1, "Period High 1", 21, 1,
//     // 1000, 1));

//     // grp.addRow(new InputDescriptor(MA_INPUT_LOW_1, "Input Low 1",
//     // Enums.BarInput.LOW));
//     // grp.addRow(new IntegerDescriptor(MA_PERIOD_LOW_1, "Period Low 1", 21, 1,
//     // 1000, 1));

//     // grp = tab.addGroup("Second Timeframe");

//     // grp.addRow(new BarSizeDescriptor(MA_BARSIZE_2, "Bar Size 2",
//     // BarSize.getBarSize(5)));
//     // grp.addRow(new InputDescriptor(MA_INPUT_HIGH_2, "Input High 2",
//     // Enums.BarInput.HIGH));
//     // grp.addRow(new IntegerDescriptor(MA_PERIOD_HIGH_2, "Period High 2", 21, 1,
//     // 1000, 1));

//     // grp.addRow(new InputDescriptor(MA_INPUT_LOW_2, "Input Low 2",
//     // Enums.BarInput.LOW));
//     // grp.addRow(new IntegerDescriptor(MA_PERIOD_LOW_2, "Period Low 2", 21, 1,
//     // 1000, 1));

//     // // Colors Group
//     // tab = sd.addTab(get("Display Hilo"));

//     // grp = tab.addGroup("Colors");
//     // grp.addRow(new PathDescriptor(MA_PATH_HIGH_LOW_1, "Path HiLo 1", null, 1.0f,
//     // null, true, true, false));
//     // grp.addRow(new PathDescriptor(MA_PATH_HIGH_LOW_2, "Path HiLo 2", null, 1.0f,
//     // null, true, true, false));

//     // // grp = tab.addGroup(get("Colors"));
//     // grp.addRow(new ColorDescriptor(HIGH_COLOR_1, get("High Color 1"),
//     // defaults.getRedLine()));
//     // grp.addRow(new ColorDescriptor(LOW_COLOR_1, get("Low Color 1"),
//     // defaults.getGreenLine()));
//     // grp.addRow(new ColorDescriptor(HIGH_COLOR_2, get("High Color 2"),
//     // defaults.getRedLine()));
//     // grp.addRow(new ColorDescriptor(LOW_COLOR_2, get("Low Color 2"),
//     // defaults.getGreenLine()));

//     // // grp = tab.addGroup(get("Markers"));
//     // // grp.addRow(new MarkerDescriptor(UP_MARKER, "Up marker",
//     // // Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(),
//     // // defaults.getLineColor(), false, true));
//     // // grp.addRow(new MarkerDescriptor(DOWN_MARKER, "Down marker",
//     // // Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(),
//     // defaults.getLineColor(),
//     // // false, true));
//     // // grp.addRow(new MarkerDescriptor(UP_MARKER_1, "Up marker 1",
//     // // Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(),
//     // // defaults.getLineColor(), false,
//     // // true));
//     // // grp.addRow(new MarkerDescriptor(DOWN_MARKER_1, "Down marker 1",
//     // // Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(),
//     // // defaults.getLineColor(),
//     // // false, true));
//     // // grp.addRow(new MarkerDescriptor(UP_MARKER_2, "Up marker 2",
//     // // Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(),
//     // // defaults.getLineColor(), false,
//     // // true));
//     // // grp.addRow(new MarkerDescriptor(DOWN_MARKER_2, "Down marker 2",
//     // // Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(),
//     // // defaults.getLineColor(),
//     // // false, true));

//     // sd.addDependency(
//     // new EnabledDependency(HIGH_COLOR_1, LOW_COLOR_1, HIGH_COLOR_2, LOW_COLOR_2));

//     // sd.addQuickSettings(MA_BARSIZE_1);
//     // sd.addQuickSettings(MA_INPUT_HIGH_1, MA_PERIOD_HIGH_1);
//     // sd.addQuickSettings(MA_PATH_HIGH_1);

//     // var desc = createRD();

//     // desc.setLabelSettings(MA_BARSIZE_1, MA_INPUT_HIGH_1, MA_PERIOD_HIGH_1,
//     // MA_INPUT_LOW_1, MA_PERIOD_LOW_1,
//     // MA_BARSIZE_2, MA_INPUT_HIGH_2, MA_PERIOD_HIGH_2, MA_INPUT_LOW_2,
//     // MA_PERIOD_LOW_2);

//     // desc.exportValue(
//     // new ValueDescriptor(Values.MA_HIGH_LOW_1, "MA High Low 1",
//     // new String[] { MA_INPUT_HIGH_1,
//     // MA_PERIOD_HIGH_1, MA_INPUT_LOW_1,
//     // MA_PERIOD_LOW_1 }));
//     // desc.exportValue(
//     // new ValueDescriptor(Values.MA_HIGH_LOW_2, "MA High Low 2",
//     // new String[] { MA_INPUT_HIGH_2,
//     // MA_PERIOD_HIGH_2, MA_INPUT_LOW_2,
//     // MA_PERIOD_LOW_2 }));
//     // desc.exportValue(
//     // new ValueDescriptor(Values.MA_HIGH_1, "MA High 1", new String[] {
//     // MA_INPUT_HIGH_1,
//     // MA_PERIOD_HIGH_1 }));
//     // desc.exportValue(
//     // new ValueDescriptor(Values.MA_LOW_1, "MA Low 1",
//     // new String[] { MA_INPUT_LOW_1, MA_PERIOD_LOW_1 }));

//     // desc.declarePath(Values.MA_HIGH_1, MA_PATH_HIGH_1);
//     // desc.setValueSeries(Values.MA_HIGH_1, MA_BARSIZE_1);
//     // desc.declarePath(Values.MA_LOW_1, MA_PATH_LOW_1);
//     // desc.setValueSeries(Values.MA_LOW_1, MA_BARSIZE_1);

//     // desc.declarePath(Values.MA_HIGH_2, MA_PATH_HIGH_2);
//     // desc.setValueSeries(Values.MA_HIGH_2, MA_BARSIZE_2);
//     // desc.declarePath(Values.MA_LOW_2, MA_PATH_LOW_2);
//     // desc.setValueSeries(Values.MA_LOW_2, MA_BARSIZE_2);

//     // desc.declarePath(Values.MA_HIGH_LOW_1, MA_PATH_HIGH_LOW_1);
//     // desc.setValueSeries(Values.MA_HIGH_LOW_1, MA_BARSIZE_1);

//     // desc.declarePath(Values.MA_HIGH_LOW_2, MA_PATH_HIGH_LOW_2);
//     // desc.setValueSeries(Values.MA_HIGH_LOW_2, MA_BARSIZE_2);

//     // // Signals
//     // desc.declareSignal(Signals.CROSS_ABOVE, "PRICE_CROSS_ABOVE");
//     // desc.declareSignal(Signals.CROSS_BELOW, "PRICE_CROSS_BELOW");
//     // }

//     // @Override
//     // public void onLoad(Defaults defaults) {
//     // int p1 = getSettings().getInteger(MA_PERIOD_HIGH_1);
//     // int p2 = getSettings().getInteger(MA_PERIOD_HIGH_2);
//     // setMinBars(Math.max(p1, p2) * 2);
//     // }

//     // public void calcMaHiLo(int index,
//     // DataContext ctx,
//     // BarSize barsize,
//     // Enums.MAMethod maMethod,
//     // Color maColorHigh,
//     // Color maColorLow,
//     // int maPeriod,
//     // Object maValueHiLo,
//     // String hiLoNb) {

//     // // Vérifier si la série des données est complète) {

//     // var series = ctx.getDataSeries();

//     // var seriesHTF = ctx.getDataSeries(barsize);

//     // // Trouver l'index correspondant à l'instant actuel dans la série 15 min
//     // long currentTime = series.getStartTime(index);
//     // int indexHTF = seriesHTF.findIndex(currentTime);

//     // if (indexHTF < 0)
//     // return; // Protection si index inexistant

//     // // Récupération d'une donnée depuis la série 15 min
//     // double closeHTF = seriesHTF.getClose(indexHTF);

//     // Double maHighHTF = seriesHTF.ma(maMethod, indexHTF, maPeriod,
//     // Enums.BarInput.HIGH);
//     // Double maLowHTF = seriesHTF.ma(maMethod, indexHTF, maPeriod,
//     // Enums.BarInput.LOW);
//     // // float close = series.getClose(index);

//     // Double valueToSet = null;

//     // if (maHighHTF != null && maLowHTF != null) {
//     // if (closeHTF < maLowHTF) {
//     // valueToSet = maHighHTF;
//     // series.setPathColor(index, maValueHiLo, maColorHigh);
//     // // complete = false;
//     // } else if (closeHTF > maHighHTF) {
//     // valueToSet = maLowHTF;
//     // series.setPathColor(index, maValueHiLo, maColorLow);
//     // // complete = false;
//     // }
//     // }

//     // if (valueToSet != null) {
//     // series.setDouble(index, maValueHiLo, valueToSet);
//     // if (hiLoNb == "1") {
//     // mA1 = valueToSet;
//     // }
//     // if (hiLoNb == "2") {
//     // mA2 = valueToSet;
//     // }
//     // }

//     // // Exemple : stockage ou utilisation immédiate
//     // // series.setDouble(index, "Close_HTF", closeHTF);

//     // // int indexMin = 0;
//     // // int indexMax = series.size() - 1;
//     // // if (indexMin <= maPeriod) {
//     // // indexMin = maPeriod - 1;
//     // // }

//     // // Double previousValue = null; // Stocker la dernière valeur disponible

//     // // for (int index = indexMin; index <= indexMax; ++index) {

//     // // boolean complete = series.isBarComplete(index);

//     // // Double maHigh = series.ma(maMethod, index, maPeriod, Enums.BarInput.HIGH);
//     // // Double maLow = series.ma(maMethod, index, maPeriod, Enums.BarInput.LOW);
//     // // float close = series.getClose(index);

//     // // Double valueToSet = null;

//     // // if (maHigh != null && maLow != null) {
//     // // if (close < maLow) {
//     // // valueToSet = maHigh;
//     // // series.setPathColor(index, maValueHiLo, maColorHigh);
//     // // complete = false;
//     // // } else if (close > maHigh) {
//     // // valueToSet = maLow;
//     // // series.setPathColor(index, maValueHiLo, maColorLow);
//     // // complete = false;
//     // // }
//     // // } else if (previousValue != null) {
//     // // // Utiliser la dernière valeur disponible si maHigh et maLow sont null
//     // // valueToSet = previousValue;
//     // // }

//     // // if (valueToSet != null) {
//     // // series.setDouble(index, maValueHiLo, valueToSet);
//     // // previousValue = valueToSet; // Mettre à jour la dernière valeur
//     // // if (hiLoNb == "1") {
//     // // mA1 = valueToSet;
//     // // }
//     // // if (hiLoNb == "2") {
//     // // mA2 = valueToSet;
//     // // }
//     // // }

//     // // series.setComplete(index, complete);

//     // // }

//     // // debug("previousValue : " + previousValue);

//     // }

//     // @Override
//     // protected void calculate(int index, DataContext ctx) {
//     // // protected void calculateValues(DataContext ctx) {
//     // var settings = getSettings();
//     // if (settings == null)
//     // return;

//     // calcMaHiLo(index, ctx,
//     // getSettings().getBarSize(MA_BARSIZE_1),
//     // METHOD,
//     // settings.getColor(HIGH_COLOR_1),
//     // settings.getColor(LOW_COLOR_1),
//     // settings.getInteger(MA_PERIOD_HIGH_1),
//     // Values.MA_HIGH_LOW_1,
//     // "1");

//     // calcMaHiLo(index, ctx,
//     // getSettings().getBarSize(MA_BARSIZE_2),
//     // METHOD,
//     // settings.getColor(HIGH_COLOR_2),
//     // settings.getColor(LOW_COLOR_2),
//     // settings.getInteger(MA_PERIOD_HIGH_2),
//     // Values.MA_HIGH_LOW_2,
//     // "2");

//     // }

//     enum Values { MA };

//     // Define custom input keys
//     public static final String TF_ENABLED = "tfEnabled";
//     public static final String TIMEFRAME = "timeframe";

//     @Override
//     public void initialize(Defaults defaults)
//     {
//       // Describe the settings that may be configured by the user.
//       // Settings may be organized using a combination of tabs and groups.  
//       var sd = createSD();
//       var tab = sd.addTab(get("TAB_GENERAL"));

//       var grp = tab.addGroup(get("LBL_INPUTS"));
//       // Declare the inputs that are used to calculate the moving average.
//       grp.addRow(new InputDescriptor(Inputs.INPUT, get("Input"), Enums.BarInput.CLOSE));
//       grp.addRow(new IntegerDescriptor(Inputs.PERIOD, get("Period"), 20, 1, 9999, 1));

//       // Add timeframe selection options
//       var tfGrp = tab.addGroup("Timeframe");
//       tfGrp.addRow(new BooleanDescriptor(TF_ENABLED, "Use Different Timeframe", false));
//       tfGrp.addRow(new BarSizeDescriptor(TIMEFRAME, "Timeframe", BarSize.getBarSize(1)));

//       grp = tab.addGroup(get("TAB_DISPLAY"));
//       // Allow the user to change the settings for the path
//       grp.addRow(new PathDescriptor(Inputs.PATH, get("Path"), defaults.getLineColor(), 1.0f, null, true, true, false));

//       // Describe the runtime settings using a 'RuntimeDescriptor'
//       var desc = createRD();

//       // Describe how to create the label
//       desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD);

//       // Exported values
//       desc.exportValue(new ValueDescriptor(Values.MA, get("My MA"), 
//                        new String[] {Inputs.INPUT, Inputs.PERIOD}));

//       // Declare path for drawing
//       desc.declarePath(Values.MA, Inputs.PATH);
//     }

//     @Override
//     public int getMinBars()
//     {
//       return getSettings().getInteger(Inputs.PERIOD)*2;
//     }

//     /** This method calculates the moving average for the given index in the data series. */
//     @Override
//     protected void calculate(int index, DataContext ctx)
//     {
//       // Get the settings as defined by the user in the study dialog
//       var settings = getSettings();
//       Object input = settings.getInput(Inputs.INPUT);
//       int period = settings.getInteger(Inputs.PERIOD);
//       boolean useDifferentTimeframe = settings.getBoolean(TF_ENABLED);

//       // Get access to the data series of the current context
//       var series = ctx.getDataSeries();

//       // If not using a different timeframe, calculate MA on the current timeframe
//       if (!useDifferentTimeframe) {
//         if (index < period) return;

//         Double average1 = series.ema(index, period, input);
//         Double average2 = series.sma(index, period, input);
//         if (average1 == null || average2 == null) return;

//         double ma = (average1 + average2)/2;
//         series.setDouble(index, Values.MA, ma);
//         return;
//       }

//       // Get data series for the specified timeframe
//       var tfSeries = ctx.getDataSeries(getSettings().getBarSize(TIMEFRAME));
//       if (tfSeries == null || tfSeries.size() <= period) return;

//       // Get the time of the current bar
//       long time = series.getStartTime(index);

//       // Find the index in the timeframe data that corresponds to or precedes this time
//       int tfIndex = tfSeries.findIndex(time);
//       if (tfIndex < 0 || tfIndex < period) return;

//       // Calculate MA on the different timeframe
//       Double average1 = tfSeries.ema(tfIndex, period, input);
//       Double average2 = tfSeries.sma(tfIndex, period, input);
//       if (average1 == null || average2 == null) return;

//       double ma = (average1 + average2)/2;

//       // Store the result in the current timeframe's data series
//       series.setDouble(index, Values.MA, ma);
//     }

// }

package custom_studies;

import java.awt.Color;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.PathInfo;
import com.motivewave.platform.sdk.common.Tick;
import com.motivewave.platform.sdk.common.TimeFrame;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingGroup;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.TimeFrameDescriptor;
import com.motivewave.platform.sdk.study.Plot;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

import com.motivewave.platform.sdk.common.desc.ValueDescriptor;

@StudyHeader(namespace = "com.sbdev", id = "GANN_HILO_2", name = "Gann High Low 2", desc = "Custom Gann High Low Indicator with Multiple Timeframes", menu = "Custom Studies", overlay = true, signals = true, studyOverlay = true)

public class GannHighLow2 extends Study {

  enum Values {
    MA_CLOSE_1, MA_HIGH_1, MA_LOW_1, MA_HILO_1, MA_CLOSE_2, MA_HIGH_2, MA_LOW_2, MA_HILO_2
  };

  // protected Enums.MAMethod METHOD;

  // Define custom input keys
  public static final String TF_ENABLED_1 = "tfEnabled1";
  public static final String TIMEFRAME_1 = "timeframe1";
  public static final String MA_METHOD_1 = "maMethod1";
  public static final String PATH_HIGH_1 = "pathHigh1";
  public static final String PATH_LOW_1 = "pathLow1";
  public static final String PATH_CLOSE_1 = "pathClose1";
  public static final String PATH_HILO_1 = "pathHilo1";
  public static final String INPUT_1 = "input1";
  public static final String PERIOD_1 = "period1";

  public static final String TF_ENABLED_2 = "tfEnabled2";
  public static final String TIMEFRAME_2 = "timeframe2";
  public static final String MA_METHOD_2 = "maMethod2";
  public static final String PATH_HIGH_2 = "pathHigh2";
  public static final String PATH_LOW_2 = "pathLow2";
  public static final String PATH_CLOSE_2 = "pathClose2";
  public static final String PATH_HILO_2 = "pathHilo2";
  public static final String INPUT_2 = "input2";
  public static final String PERIOD_2 = "period2";

  final static String HIGH_COLOR_1 = "highColor1";
  final static String LOW_COLOR_1 = "lowColor1";
  final static String HIGH_COLOR_2 = "highColor2";
  final static String LOW_COLOR_2 = "lowColor2";

  @Override
  public void initialize(Defaults defaults) {
    // Describe the settings that may be configured by the user.
    // Settings may be organized using a combination of tabs and groups.
    var sd = createSD();
    var tab1 = sd.addTab(get("HiLo 1"));

    var grp1 = tab1.addGroup(get("First Timeframe"));
    // Add timeframe selection options
    grp1.addRow(new BooleanDescriptor(TF_ENABLED_1, "Use Timeframe 1", true));
    grp1.addRow(new BarSizeDescriptor(TIMEFRAME_1, "Timeframe 1", BarSize.getBarSize(1)));
    grp1.addRow(new MAMethodDescriptor(MA_METHOD_1, "MA Method 1", Enums.MAMethod.SMA));
    grp1.addRow(new InputDescriptor(INPUT_1, "Input 1", Enums.BarInput.CLOSE));
    grp1.addRow(new IntegerDescriptor(PERIOD_1, "Period 1", 21, 1, 9999, 1));

    grp1 = tab1.addGroup(get("Display"));
    // Allow the user to change the settings for the paths
    grp1.addRow(
        new PathDescriptor(PATH_CLOSE_1, "Close Path 1", defaults.getLineColor(), 1.0f, null, true, true, false));
    grp1.addRow(new PathDescriptor(PATH_HIGH_1, "High Path 1", defaults.getGreenLine(), 1.0f, null, true, true, false));
    grp1.addRow(new PathDescriptor(PATH_LOW_1, "Low Path 1", defaults.getRedLine(), 1.0f, null, true, true, false));
    grp1.addRow(new PathDescriptor(PATH_HILO_1, "Hilo Path 1", null, 1.0f, null, true, true, false));

    grp1.addRow(new ColorDescriptor(HIGH_COLOR_1, "High Color 1", defaults.getRedLine()));
    grp1.addRow(new ColorDescriptor(LOW_COLOR_1, "Low Color 1", defaults.getGreenLine()));

    var tab2 = sd.addTab(get("HiLo 2"));

    var grp2 = tab2.addGroup(get("Second Timeframe"));
    // Add timeframe selection options
    grp2.addRow(new BooleanDescriptor(TF_ENABLED_2, "Use Timeframe 2", true));
    grp2.addRow(new BarSizeDescriptor(TIMEFRAME_2, "Timeframe 2", BarSize.getBarSize(5)));
    grp2.addRow(new MAMethodDescriptor(MA_METHOD_2, "MA Method 2", Enums.MAMethod.SMA));
    grp2.addRow(new InputDescriptor(INPUT_2, "Input 2", Enums.BarInput.CLOSE));
    grp2.addRow(new IntegerDescriptor(PERIOD_2, "Period 2", 21, 1, 9999, 1));

    grp2 = tab2.addGroup(get("Display"));
    // Allow the user to change the settings for the paths
    grp2.addRow(
        new PathDescriptor(PATH_CLOSE_2, "Close Path 2", defaults.getLineColor(), 1.0f, null, true, true, false));
    grp2.addRow(new PathDescriptor(PATH_HIGH_2, "High Path 2", defaults.getGreenLine(), 1.0f, null, true, true, false));
    grp2.addRow(new PathDescriptor(PATH_LOW_2, "Low Path 2", defaults.getRedLine(), 1.0f, null, true, true, false));
    grp2.addRow(new PathDescriptor(PATH_HILO_2, "Hilo Path 2", null, 1.0f, null, true, true, false));

    grp2.addRow(new ColorDescriptor(HIGH_COLOR_2, "High Color 2", defaults.getRedLine()));
    grp2.addRow(new ColorDescriptor(LOW_COLOR_2, "Low Color 2", defaults.getGreenLine()));

    // Describe the runtime settings using a 'RuntimeDescriptor'
    var desc = createRD();

    // Describe how to create the label
    desc.setLabelSettings(INPUT_1, PERIOD_1);
    desc.setLabelSettings(INPUT_2, PERIOD_2);

    // Exported values
    desc.exportValue(new ValueDescriptor(Values.MA_CLOSE_1, "Close MA_1",
        new String[] { INPUT_1, PERIOD_1 }));
    desc.exportValue(new ValueDescriptor(Values.MA_HIGH_1, "High MA_1",
        new String[] { INPUT_1, PERIOD_1 }));
    desc.exportValue(new ValueDescriptor(Values.MA_LOW_1, "Low MA_1",
        new String[] { INPUT_1, PERIOD_1 }));
    desc.exportValue(new ValueDescriptor(Values.MA_CLOSE_2, "Close MA_2",
        new String[] { INPUT_2, PERIOD_2 }));
    desc.exportValue(new ValueDescriptor(Values.MA_HIGH_2, "High MA_2",
        new String[] { INPUT_2, PERIOD_2 }));
    desc.exportValue(new ValueDescriptor(Values.MA_LOW_2, "Low MA_2",
        new String[] { INPUT_2, PERIOD_2 }));

    // desc.exportValue(new ValueDescriptor(Values.MA_HILO_1, "Hilo MA_1",
    //     new String[] { INPUT_1, PERIOD_1 }));
    // desc.exportValue(new ValueDescriptor(Values.MA_HILO_2, "Hilo MA_2",
    //     new String[] { INPUT_2, PERIOD_2 }));

    // Declare paths for drawing
    desc.declarePath(Values.MA_CLOSE_1, PATH_CLOSE_1);
    desc.declarePath(Values.MA_HIGH_1, PATH_HIGH_1);
    desc.declarePath(Values.MA_LOW_1, PATH_LOW_1);
    desc.declarePath(Values.MA_HILO_1, PATH_HILO_1);
    desc.declarePath(Values.MA_CLOSE_2, PATH_CLOSE_2);
    desc.declarePath(Values.MA_HIGH_2, PATH_HIGH_2);
    desc.declarePath(Values.MA_LOW_2, PATH_LOW_2);
    desc.declarePath(Values.MA_HILO_2, PATH_HILO_2);

  }

  @Override
  public int getMinBars() {
    // return getSettings().getInteger(Inputs.PERIOD)*2;
    int p1 = getSettings().getInteger(PERIOD_1);
    int p2 = getSettings().getInteger(PERIOD_2);
    return Math.max(p1, p2) * 2;
  }

  // @Override
  // public void onLoad(Defaults defaults) {
  // DataContext ctx = getDataContext(); // récupération du contexte de données
  // doUpdate(ctx);
  // }

  // @Override
  // public void onBarUpdate(DataContext ctx) {
  // if (!getSettings().isBarUpdates())
  // return;
  // doUpdate(ctx);
  // }

  // @Override
  // public void onBarClose(DataContext ctx) {
  // doUpdate(ctx);
  // }

  // @Override
  // public void onTick(DataContext ctx, Tick tick) {
  // doUpdate(ctx);
  // }

  // protected void doUpdate(DataContext ctx) {
  // var settings = getSettings();
  // if (settings.getBoolean(TF_ENABLED_1)) {
  // ctx.getDataSeries(settings.getBarSize(TIMEFRAME_1));
  // }
  // if (settings.getBoolean(TF_ENABLED_2)) {
  // ctx.getDataSeries(settings.getBarSize(TIMEFRAME_2));
  // }
  // }

  /**
   * This method calculates the moving average for the given index in the data
   * series.
   */
  @Override
  protected void calculate(int index, DataContext ctx) {
    // Get the settings as defined by the user in the study dialog
    var settings = getSettings();

    // Get access to the data series of the current context
    var series = ctx.getDataSeries();

    Object input1 = settings.getInput(INPUT_1);
    int period1 = settings.getInteger(PERIOD_1);
    boolean useTimeframe1 = settings.getBoolean(TF_ENABLED_1);

    double close = series.getClose(index);

    if (useTimeframe1) {

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

      Double maClose1 = tfSeries1.ma(settings.getMAMethod(MA_METHOD_1), tfIndex1, period1, input1);
      if (maClose1 == null)
        return;

      // series.setDouble(index, Values.MA_CLOSE_1, maClose1);

      Double maHigh1 = tfSeries1.ma(settings.getMAMethod(MA_METHOD_1), tfIndex1, period1, Enums.BarInput.HIGH);
      if (maHigh1 == null)
        return;

      // series.setDouble(index, Values.MA_HIGH_1, maHigh1);

      Double maLow1 = tfSeries1.ma(settings.getMAMethod(MA_METHOD_1), tfIndex1, period1, Enums.BarInput.LOW);
      if (maLow1 == null)
        return;

      // series.setDouble(index, Values.MA_LOW_1, maLow1);

      if (maHigh1 != null && maLow1 != null) {
        if (close < maLow1) {
          series.setDouble(index, Values.MA_HILO_1, maHigh1);
          series.setPathColor(index, Values.MA_HILO_1, settings.getColor(HIGH_COLOR_1));
        } else if (close > maHigh1) {
          series.setDouble(index, Values.MA_HILO_1, maLow1);
          series.setPathColor(index, Values.MA_HILO_1, settings.getColor(LOW_COLOR_1));
        }
      }

    }

    Object input2 = settings.getInput(INPUT_2);
    int period2 = settings.getInteger(PERIOD_2);
    boolean useTimeframe2 = settings.getBoolean(TF_ENABLED_2);

    if (useTimeframe2) {

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

      Double maClose2 = tfSeries2.ma(settings.getMAMethod(MA_METHOD_2), tfIndex2, period2, input2);
      if (maClose2 == null)
        return;

      // series.setDouble(index, Values.MA_CLOSE_2, maClose2);

      Double maHigh2 = tfSeries2.ma(settings.getMAMethod(MA_METHOD_2), tfIndex2, period2, Enums.BarInput.HIGH);
      if (maHigh2 == null)
        return;

      // series.setDouble(index, Values.MA_HIGH_2, maHigh2);

      Double maLow2 = tfSeries2.ma(settings.getMAMethod(MA_METHOD_2), tfIndex2, period2, Enums.BarInput.LOW);
      if (maLow2 == null)
        return;

      // series.setDouble(index, Values.MA_LOW_2, maLow2);

      if (maHigh2 != null && maLow2 != null) {
        if (close < maLow2) {
          series.setDouble(index, Values.MA_HILO_2, maHigh2);
          series.setPathColor(index, Values.MA_HILO_2, settings.getColor(HIGH_COLOR_2));
        } else if (close > maHigh2) {
          series.setDouble(index, Values.MA_HILO_2, maLow2);
          series.setPathColor(index, Values.MA_HILO_2, settings.getColor(LOW_COLOR_2));
        }
      }
    }
  }
}