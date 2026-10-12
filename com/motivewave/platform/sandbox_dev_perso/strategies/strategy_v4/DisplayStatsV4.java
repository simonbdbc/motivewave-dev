package custom_studies.strategies.strategy_v4;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

import custom_studies.strategies.strategy_core_v4.StrategyAnalysis;

import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.common.DrawContext;

/** Custom Display Stats Indicator */
@StudyHeader(namespace = "com.sbdev", id = "DISPLAY_STATS_V4", name = "Display Stats V4", desc = "Custom Display Stats Indicator", menu = "Custom Studies", overlay = false, studyOverlay = true)

/** Base Class for All Moving Averages. */
public class DisplayStatsV4 extends Study {

  // protected StrategyAnalysis analysis = new StrategyAnalysis();
  protected StrategyAnalysis analysis;

  // enum Values {
  //   MA
  // }

  // enum Signals {
  //   CROSS_ABOVE, CROSS_BELOW
  // }

  // protected String MA_LABEL = "";
  // protected Enums.MAMethod METHOD;

  @Override
  public void initialize(Defaults defaults) {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    // var grp = tab.addGroup(get("LBL_INPUTS"));
    // grp.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    // grp.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1),
    //     new IntegerDescriptor(Inputs.SHIFT, get("LBL_SHIFT"), 0, -999, 999, 1));
    // grp.addRow(new BooleanDescriptor(Inputs.FILL_FORWARD, get("LBL_FILL_FORWARD"), true));

    // var colors = tab.addGroup(get("LBL_DISPLAY"));
    // colors.addRow(
    //     new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, true, false));
    // colors.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // tab = sd.addTab(get("TAB_ADVANCED"));

    // grp = tab.addGroup(get("LBL_BAR_COLOR"));
    // grp.addRow(new BooleanDescriptor(Inputs.ENABLE_BAR_COLOR, get("LBL_ENABLE_BAR_COLOR"), false));
    // grp.addRow(new ColorDescriptor(Inputs.UP_COLOR, get("LBL_UP_COLOR"), defaults.getGreenLine()));
    // grp.addRow(new ColorDescriptor(Inputs.NEUTRAL_COLOR, get("LBL_NEUTRAL_COLOR"), defaults.getLineColor()));
    // grp.addRow(new ColorDescriptor(Inputs.DOWN_COLOR, get("LBL_DOWN_COLOR"), defaults.getRedLine()));

    // grp = tab.addGroup(get("LBL_MARKERS"));
    // grp.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"),
    //     Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));
    // grp.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"),
    //     Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), false, true));

  }

  public void onLoad() {
    getDataStats();
  }

  @Override
  public void onSettingsUpdated(DataContext ctx) {
    getDataStats();
  }

  public void getDataStats() {
    clearFigures();

    String[][] formatedData = analysis.getFormattedStatsTableSortedByProfitFactor();

    debug("formatedData length: " + formatedData.length);
    if (formatedData.length > 1 && formatedData[1].length > 0) {
        debug("formatedData[1][0] : " + formatedData[1][0]);
    } else {
        debug("formatedData is too short or empty");
    }
    
    TableauMotiveWaveFigure tableau = new TableauMotiveWaveFigure(formatedData);
    addFigure(tableau);
  }

  public class TableauMotiveWaveFigure extends Figure {

    private final String[][] data;

    public TableauMotiveWaveFigure(String[][] inputData) {
      // Transposition des données ici
      int rows = inputData.length;
      int cols = inputData[0].length;
      this.data = new String[cols][rows];
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
          this.data[j][i] = inputData[i][j];
        }
      }
    }

    @Override
    public void draw(Graphics2D g2d, DrawContext context) {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setFont(new Font("Arial", Font.PLAIN, 10));

      int rowHeight = 20;
      int colWidth = 120;

      Rectangle bounds = context.getBounds();
      int xStart = bounds.x + bounds.width - (colWidth * data[0].length) - 20;
      int yStart = bounds.y + bounds.height - (rowHeight * data.length) - 20;

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
