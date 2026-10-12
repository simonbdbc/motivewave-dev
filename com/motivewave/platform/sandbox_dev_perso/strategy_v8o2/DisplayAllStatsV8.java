package sbdev.joy_ai_project.strategy_v8o3;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.*;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.LabelDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

import sbdev.joy_ai_project.strategy_v8o3.database.DatabaseInitializer;
import sbdev.joy_ai_project.strategy_v8o3.database.StrategyDbManager;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.PatternGroupKey;
import sbdev.joy_ai_project.strategy_v8o3.util.FormatResult.StrategyStatsSummary;
import sbdev.joy_ai_project.strategy_v8o3.util.Helper;

import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.common.DrawContext;

/** Custom Display Stats Indicator */
@StudyHeader(namespace = "com.sbdev", id = "DISPLAY_ALL_STATS_V8", name = "Display All Stats V8", desc = "Custom Display Stats Indicator", menu = "SB Dev", overlay = true, studyOverlay = false)

/** Base Class for All Moving Averages. */
public class DisplayAllStatsV8 extends Study {

  private final StrategyDbManager dbManager = new StrategyDbManager();

  private String[][] formatedData;
  private TableauMotiveWaveFigure tableau;

  final static String START_HOUR = "startHour";
  final static String START_MINUTE = "startMinute";
  final static String END_HOUR = "endHour";
  final static String END_MINUTE = "endMinute";
  private String START_TIME = "00:01";
  private String END_TIME = "23:59";

  private String INSTRUMENT = "instrument";
  // private String INTERVAL = "interval";

  // public final static String EXIT = "exit";
  // public final static String MAX_LOSS = "maxLoss";

  private List<PatternGroupKey> patternGroups = Helper.loadPatternGroupsFromSingleton();
  private List<PatternGroupKey> patternGroupsFiltered = new ArrayList<>();

  @Override
  public void initialize(Defaults defaults) {
    var sd = createSD();

    var tab = sd.addTab("Analysis");
    var section1 = tab.addGroup("Interval");

    for (int i = 0; i < patternGroups.size(); i++) {
      PatternGroupKey group = patternGroups.get(i);

      String intervalKey = "TICK_INTERVAL_" + (i + 1);
      String enabledKey = "TICK_INTERVAL_ENABLED_" + (i + 1);

      section1.addRow(new LabelDescriptor("Tick Interval " + (i + 1) + " : " + group.intervalValue),
          new LabelDescriptor(" --- TP/SL : " + group.tp + "/" + group.sl + " --- "),
          new BooleanDescriptor(enabledKey, "Use Tick Interval " + (i + 1), true));

      sd.addDependency(new EnabledDependency(enabledKey, intervalKey));

    }

    // Section 2
    var hours = tab.addGroup("Heures de filtrage");
    hours.addRow(new IntegerDescriptor(START_HOUR, "Heure Début", 0, 0, 23, 1),
        new IntegerDescriptor(START_MINUTE, "Minutes", 0, 0, 59, 1));
    hours.addRow(new IntegerDescriptor(END_HOUR, "Heure Fin", 23, 1, 23, 1),
        new IntegerDescriptor(END_MINUTE, "Minutes", 59, 0, 59, 1));

    var instrument = tab.addGroup("Instruments");
    instrument.addRow(new BooleanDescriptor(INSTRUMENT, "All Intruments", false));
    // var interval = tab.addGroup("Interval");
    // interval.addRow(new BooleanDescriptor(INTERVAL, "All Interval", false));

    // var section1 = tab.addGroup("Bracket");
    // section1.addRow(new DoubleDescriptor(EXIT, "Take Profit", 15.00, 1.00,
    // 1000.00, 0.25));
    // section1.addRow(new DoubleDescriptor(MAX_LOSS, "Stop Loss", 15.00, 1.00,
    // 1000.00, 0.25));

    // sd.addQuickSettings(INSTRUMENT, INTERVAL, EXIT, MAX_LOSS);
    sd.addQuickSettings(INSTRUMENT);

  }

  @Override
  public void precalculate(DataContext ctx) {
    // debug("precalculate");
    getDataStats(ctx);
  }

  @Override
  public void onSettingsUpdated(DataContext ctx) {
    super.onSettingsUpdated(ctx);
    // debug("onSettingsUpdated");
    getDataStats(ctx);
  }

  public void getDataStats(DataContext ctx) {
    // debug("getDataStats");

    clearFigures();

    // debug("getSymbol : " + ctx.getInstrument().getSymbol());
    // debug("isTick : " + ctx.getDataSeries().getBarSize().isTick());
    // debug("getInterval : " + ctx.getDataSeries().getBarSize().getInterval());

    Integer startHour = getSettings().getInteger(START_HOUR);
    Integer startMinute = getSettings().getInteger(START_MINUTE);
    Integer endHour = getSettings().getInteger(END_HOUR);
    Integer endMinute = getSettings().getInteger(END_MINUTE);

    START_TIME = String.format("%02d:%02d", startHour, startMinute);
    END_TIME = String.format("%02d:%02d", endHour, endMinute);

    // debug("START_TIME : " + START_TIME);
    // debug("END_TIME : " + END_TIME);

    String instrument = ctx.getInstrument().getSymbol();
    Boolean allInstrumentEnabled = getSettings().getBoolean(INSTRUMENT);
    if (allInstrumentEnabled) {
      instrument = null;
    }

    // Integer interval = ctx.getDataSeries().getBarSize().getInterval();
    // Boolean allIntervalEnabled = getSettings().getBoolean(INTERVAL);
    // if (allIntervalEnabled) {
    // interval = null;
    // }

    for (int i = 0; i < patternGroups.size(); i++) {
      PatternGroupKey group = patternGroups.get(i);

      String enabledKey = "TICK_INTERVAL_ENABLED_" + (i + 1);
      boolean enabled = getSettings().getBoolean(enabledKey);

      if (enabled) {
        if (!patternGroupsFiltered.contains(group)) {
          patternGroupsFiltered.add(group);
        }
      } else {
        patternGroupsFiltered.remove(group);
      }
    }

    System.err.println("Nombre de patternGroupsfiltred chargées DisplayStatsV8 : " + patternGroupsFiltered);

    if (ctx.getDataSeries().getBarSize().isTick()) {

      formatedData = getFormattedStatsTableSortedByProfitFactor(START_TIME, END_TIME, instrument,
          patternGroupsFiltered);
      // debug("formatedData.length : " + formatedData.length);

      if (formatedData != null && formatedData.length == 0 && formatedData[1].length == 0) {
        debug("formatedData is too short or empty");
      } else {
        tableau = new TableauMotiveWaveFigure(formatedData);
        addFigure(tableau);
      }
    }

  }

  public String[][] getFormattedStatsTableSortedByProfitFactor(String START_TIME, String END_TIME, String instrument,
      List<PatternGroupKey> patternGroupsFiltered) {
    List<StrategyStatsSummary> summaries = dbManager.loadStatsFromDatabase(START_TIME, END_TIME, instrument,
        patternGroupsFiltered);
    // System.err.println("Nombre de summaries chargées DisplayStatsV8 : " +
    // summaries.size());
    // summaries.removeIf(
    // r -> r.takeProfit != getSettings().getDouble(EXIT) && r.stopLoss !=
    // getSettings().getDouble(MAX_LOSS));

    // Tri par profit factor décroissant, puis delta décroissant
    summaries.sort((s1, s2) -> {
      double pf1 = (s1.lossSignals != 0) ? (s1.winSignals * s1.takeProfit) / Math.abs(s1.lossSignals * s1.stopLoss)
          : (s1.winSignals > 0 ? Double.POSITIVE_INFINITY : 0.0);
      double pf2 = (s2.lossSignals != 0) ? (s2.winSignals * s2.takeProfit) / Math.abs(s2.lossSignals * s2.stopLoss)
          : (s2.winSignals > 0 ? Double.POSITIVE_INFINITY : 0.0);

      int cmp = Double.compare(pf2, pf1); // Tri décroissant PF
      if (cmp != 0)
        return cmp;

      // Tri secondaire sur delta décroissant
      int delta1 = s1.winSignals - s1.lossSignals;
      int delta2 = s2.winSignals - s2.lossSignals;
      return Integer.compare(delta2, delta1);
    });

    Integer rowCount = summaries.size() + 1; // +1 for header
    String[][] tableData = new String[rowCount][];

    // Header
    tableData[0] = new String[] { "Strategies", "Instrument", "Interval", "First Trade", "Last Trade", "Total", "Win",
        "Loss", "Delta", "Win Rate", "Profit Factor", "TP pts", "SL pts" };

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    for (Integer i = 0; i < summaries.size(); i++) {
      StrategyStatsSummary summary = summaries.get(i);

      Double totalGain = summary.winSignals * summary.takeProfit;
      Double totalLoss = summary.lossSignals * summary.stopLoss;
      Double profitFactor = (totalLoss != 0) ? totalGain / Math.abs(totalLoss)
          : (totalGain > 0 ? Double.POSITIVE_INFINITY : 0.0);
      String profitFactorStr = String.format("%.2f", profitFactor);
      if (profitFactor < 1.0)
        profitFactorStr += " ⚠️";

      String winRate = (summary.totalSignals > 0) ? (int) (100.0 * summary.winSignals / summary.totalSignals) + "%"
          : "N/A";
      Integer delta = summary.winSignals - summary.lossSignals;

      tableData[i + 1] = new String[] { summary.strategyName, summary.instrument,
          String.valueOf(summary.intervalValue) + " " + summary.intervalScale,
          dateFormat.format(new Date(summary.startTimestamp)), dateFormat.format(new Date(summary.endTimestamp)),
          String.valueOf(summary.totalSignals), String.valueOf(summary.winSignals), String.valueOf(summary.lossSignals),
          String.valueOf(delta), winRate, profitFactorStr, String.valueOf(summary.takeProfit),
          String.valueOf(summary.stopLoss) };
    }

    return tableData;
  }

  public class TableauMotiveWaveFigure extends Figure {

    private final String[][] data;

    public TableauMotiveWaveFigure(String[][] inputData) {
      // Transposition des données ici
      // int rows = inputData.length;
      // int cols = inputData[0].length;
      // this.data = new String[cols][rows];
      // for (int i = 0; i < rows; i++) {
      // for (int j = 0; j < cols; j++) {
      // // debug("tableau : " + inputData[i][j]);
      // this.data[j][i] = inputData[i][j];
      // }
      // }
      this.data = inputData;
    }

    @Override
    public void draw(Graphics2D g2d, DrawContext context) {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setFont(new Font("Arial", Font.PLAIN, 10));

      int rowHeight = 20;
      int colWidth = 90;

      Rectangle bounds = context.getBounds();
      // int xStart = bounds.x + bounds.width - (colWidth * data[0].length) - 20;
      // int yStart = bounds.y + bounds.height - (rowHeight * data.length) - 20;
      int xStart = bounds.x;
      int yStart = bounds.y + bounds.height - (rowHeight * data.length) - 30;
      // int xStart = bounds.x;
      // int yStart = bounds.y + 70;

      for (int row = 0; row < data.length; row++) {
        for (int col = 0; col < data[row].length; col++) {
          int x = xStart + col * colWidth;
          int y = yStart + row * rowHeight;

          g2d.setColor(new Color(33, 33, 33));
          g2d.fillRect(x, y, colWidth, rowHeight);
          g2d.setColor(Color.WHITE);
          g2d.drawRect(x, y, colWidth, rowHeight);

          g2d.setFont(row == 0 || col == 0 ? new Font("Arial", Font.BOLD, 12) : new Font("Arial", Font.PLAIN, 10));
          g2d.drawString(data[row][col], x + 10, y + 15);
        }
      }
    }
  }

}
