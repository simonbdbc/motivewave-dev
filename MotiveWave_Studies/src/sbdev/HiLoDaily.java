package sbdev;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.ColorInfo;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.DrawContext;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.PathInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Implements the HiLo Session study. */
@StudyHeader(namespace = "com.sbdev", id = "HILO_DAILY", name = "HiLo Daily", label = "HiLo Daily", desc = "Displays high, low of the day", menu = "SB Dev", overlay = true, requiresVolume = true, supportsBarUpdates = false)
public class HiLoDaily extends Study {

    final static String RANGE_FILL = "rangeFill";
    final static String RANGE_LINE = "rangeLine";
    final static String HISTORICAL = "historical";
    final static String MAX_DAYS = "maxDays";

    final static String SESSION_START_HOUR = "sessionStartHour";
    final static String SESSION_START_MINUTE = "sessionStartMinute";
    final static String SESSION_END_HOUR = "sessionEndHour";
    final static String SESSION_END_MINUTE = "sessionEndMinute";

    @Override
    public void initialize(Defaults defaults) {
        var sd = createSD();
        var tab = sd.addTab(get("General"));

        var historicTab = tab.addGroup(get("Historical"));
        historicTab.addRow(new IntegerDescriptor(MAX_DAYS, get("Max Days"), 10, 1, 99, 1),
                new BooleanDescriptor(HISTORICAL, "Historical Enabled", true, false));

        // var sessionTab = tab.addGroup(get("Session"));
        // sessionTab.addRow(new IntegerDescriptor(SESSION_START_HOUR, "Session Start
        // Hour", 2, 0, 23, 1),
        // new IntegerDescriptor(SESSION_START_MINUTE, "Minute", 0, 0, 59, 1));
        // sessionTab.addRow(new IntegerDescriptor(SESSION_END_HOUR, "Session End Hour",
        // 7, 0, 23, 1),
        // new IntegerDescriptor(SESSION_END_MINUTE, "Minute", 0, 0, 59, 1));

        var colorTab = tab.addGroup("Colors");
        // colorTab.addRow(new PathDescriptor(RANGE_LINE, "Range Line",
        // defaults.getLineColor(), 1.0f,
        // new float[] { 3f, 3f }, true, false, true));
        colorTab.addRow(
                new PathDescriptor(RANGE_LINE, "Range Line", Util.getAlphaFill(new Color(102, 102, 255), 128), 1.0f,
                        null, true, false, true));
        // colorTab.addRow(new ColorDescriptor(RANGE_FILL, "Range Fill",
        // Util.getAlphaFill(defaults.getGrey()),
        // true, true));
        colorTab.addRow(new ColorDescriptor(RANGE_FILL, "Range Fill", Util.getAlphaFill(new Color(102, 102, 255), 26),
                false, true));

        sd.addQuickSettings(MAX_DAYS, HISTORICAL, RANGE_LINE, RANGE_FILL);
    }

    /**
     * Gets the minimum start time (milliseconds since 1970) required to display
     * this study (null if no minimum is required)
     * 
     * @return minimum start time required to display this study (null if no minimum
     *         is required).
     */
    @Override
    public Long getMinStartTime(DataContext ctx) {
        long tsDayStart = computePeriodStart(ctx, ctx.getCurrentTime());
        return tsDayStart - Util.MILLIS_IN_DAY;
    }

    @Override
    public void onDataSeriesMoved(DataContext ctx) {
        // When historical is not turned on, show the volume profile for the latest day
        // on the visible data
        // We need recalculate every time the data series is moved in this case
        boolean historical = getSettings().getBoolean(HISTORICAL, false);
        if (!historical)
            calculateValues(ctx);
    }

    @Override
    protected void calculateValues(DataContext ctx) {
        DataSeries series = ctx.getDataSeries();
        boolean historical = getSettings().getBoolean(HISTORICAL, false);

        clearFigures();

        BarSize bs = series.getBarSize();
        if (series.size() == 0 || bs.getIntervalMinutes() >= 1440)
            return;

        // For historical, compute the volume profile for each day
        // Otherwise, compute for the latest visible day
        int seriesEnd = series.getEndIndex();
        if (historical)
            seriesEnd = series.size() - 1;
        int seriesStart = series.getStartIndex();
        if (historical || seriesStart < 0)
            seriesStart = 0;

        // Find the start of the day
        // Allow a few bars to build up here
        long time = series.getStartTime(seriesEnd);
        if (seriesEnd == series.size() - 1) {
            time = series.getStartTime(seriesEnd - 4);
        }

        long tsDayStart = computePeriodStart(ctx, time);
        long tsDayEnd = computePeriodEnd(ctx, time);

        int indexDayStart = seriesEnd;
        for (int i = seriesEnd; i >= 0; i--) {
            if (series.getStartTime(i) < tsDayStart)
                break;
            indexDayStart = i;
        }

        // Determine the range
        int indexDayEnd = seriesStart;
        for (int i = seriesStart; i < series.size(); i++) {
            if (series.getStartTime(i) >= tsDayEnd)
                break;
            indexDayEnd = i;
        }

        createAsiaAreaByDay(ctx, tsDayStart, tsDayEnd, indexDayStart, indexDayEnd);
        int count = 1;

        if (!historical)
            return;
        int max = getSettings().getInteger(MAX_DAYS, 5);

        while (indexDayStart > 5) {
            if (count >= max)
                break;
            time = series.getStartTime(indexDayStart - 1);

            tsDayStart = computePeriodStart(ctx, time);
            tsDayEnd = computePeriodEnd(ctx, time);

            int end = indexDayStart - 1;
            indexDayStart--;
            for (int i = end; i >= 0; i--) {
                if (series.getStartTime(i) < tsDayStart)
                    break;
                indexDayStart = i;
            }

            // Determine the range
            for (int i = 0; i < end; i++) {
                if (series.getStartTime(i) >= tsDayEnd)
                    break;
                indexDayEnd = i;
            }
            createAsiaAreaByDay(ctx, tsDayStart, tsDayEnd, indexDayStart, indexDayEnd);
            count++;
        }
    }

    private void createAsiaAreaByDay(DataContext ctx, long tsDayStart, long tsDayEnd, int indexDayStart,
            int indexDayEnd) {
        DataSeries series = ctx.getDataSeries();
        ZoneId zone = ctx.getTimeZone().toZoneId();

        int sessionStartHour = 0;
        int sessionStartMinute = 0;
        int sessionEndHour = 23;
        int sessionEndMinute = 59;

        // tsDayStart est en millisecondes UTC (epoch ms)
        ZonedDateTime zdt = Instant.ofEpochMilli(tsDayStart).atZone(zone);

        // 02h00 du matin
        ZonedDateTime sessionStartDateTime = zdt.toLocalDate().atTime(sessionStartHour, sessionStartMinute)
                .atZone(zone);
        long tsSessionStart = sessionStartDateTime.toInstant().toEpochMilli();

        // 07h00 du matin
        ZonedDateTime sessionEndDateTime = zdt.toLocalDate().atTime(sessionEndHour, sessionEndMinute).atZone(zone);
        long tsSessionEnd = sessionEndDateTime.toInstant().toEpochMilli();

        // int indexSessionStart = indexDayEnd;
        // for (int i = indexDayEnd; i >= 0; i--) {
        // if (series.getStartTime(i) < tsSessionStart)
        // break;
        // indexSessionStart = i;
        // }

        // // Determine the range
        // int indexSessionEnd = indexDayStart;
        // for (int i = indexDayStart; i < series.size(); i++) {
        // if (series.getStartTime(i) >= tsSessionEnd)
        // break;
        // indexSessionEnd = i;
        // }

        // Double sessionTop = series.highest(indexSessionEnd, indexSessionEnd -
        // indexSessionStart - 1,
        // Enums.BarInput.HIGH);
        // Double sessionBottom = series.lowest(indexSessionEnd, indexSessionEnd -
        // indexSessionStart - 1,
        // Enums.BarInput.LOW);

        int len = indexDayEnd - indexDayStart + 1;
        Double sessionBottom = series.lowest(indexDayEnd, len, Enums.BarInput.LOW);
        Double sessionTop = series.highest(indexDayEnd, len, Enums.BarInput.HIGH);

        if (sessionTop == null || sessionBottom == null || sessionTop <= sessionBottom)
            return;

        // Crée une seule zone du High au Low sur toute la session
        RectangleArea area = new RectangleArea(sessionTop, sessionBottom, tsDayStart, tsDayEnd, tsSessionStart,
                tsSessionEnd);
        addFigure(area);
    }

    private long computePeriodStart(DataContext ctx, long time) {
        DataSeries series = ctx.getDataSeries();
        return series.getInstrument().getStartOfDay(time, ctx.isRTH());
    }

    private long computePeriodEnd(DataContext ctx, long time) {
        DataSeries series = ctx.getDataSeries();
        return series.getInstrument().getEndOfDay(time, ctx.isRTH());
    }

    protected class RectangleArea extends Figure {
        private Rectangle rangeArea;
        private double sessionTopRange;
        private double sessionBottomRange;
        private long tsDayStart;
        private long tsDayEnd;
        private long tsSessionStart;
        private long tsSessionEnd;

        RectangleArea(double sessionTop, double sessionBottom, long tsDayStart, long tsDayEnd, long tsSessionStart,
                long tsSessionEnd) {
            this.sessionTopRange = sessionTop;
            this.sessionBottomRange = sessionBottom;
            this.tsDayStart = tsDayStart;
            this.tsDayEnd = tsDayEnd;
            this.tsSessionStart = tsSessionStart;
            this.tsSessionEnd = tsSessionEnd;
        }

        @Override
        public void layout(DrawContext ctx) {
            int sx = ctx.translateTime(tsSessionStart);
            int ex = ctx.translateTime(tsSessionEnd);
            Rectangle gb = ctx.getBounds();
            if (gb == null)
                return;

            boolean historical = getSettings().getBoolean(HISTORICAL);
            int ty = ctx.translateValue(sessionTopRange);
            int by = ctx.translateValue(sessionBottomRange);
            int gw = gb.width;
            int lx = gb.x;
            int rx = gb.x + gb.width;
            // int h = by - ty - 1;
            int h = by - ty;

            // Where do we place these bars?
            if (historical) {
                // Adjust to the end of day
                rx = ex;
                lx = sx;
                gw = rx - lx;
            }

            rangeArea = gb.intersection(new Rectangle(lx, ty, gw, h + 1));
        }

        @Override
        public void draw(Graphics2D gc, DrawContext ctx) {
            ColorInfo rangeColor = getSettings().getColorInfo(RANGE_FILL);
            PathInfo path = getSettings().getPath(RANGE_LINE);

            if (path != null) {
                if (ctx.isSelected())
                    gc.setStroke(path.getSelectedStroke());
                else
                    gc.setStroke(path.getStroke());
            }

            if (rangeArea == null)
                return;
            Rectangle gb = ctx.getBounds();
            if (gb == null)
                return;
            rangeArea = gb.intersection(rangeArea);
            if (rangeColor != null && rangeColor.isEnabled()) {
                gc.setColor(rangeColor.getColor());
                gc.fill(rangeArea);
            }

            if (path.isEnabled()) {

                int sxLine = ctx.translateTime(tsDayStart);
                int exLine = ctx.translateTime(tsDayEnd);

                gc.setColor(path.getColor());
                // gc.drawLine(rangeArea.x, rangeArea.y, (int) rangeArea.getMaxX(),
                // rangeArea.y);
                gc.drawLine(sxLine, rangeArea.y, exLine, rangeArea.y);
                gc.setColor(path.getColor());
                // int y = (int) rangeArea.getMaxY() - 1;
                int y = (int) rangeArea.getMaxY();
                // gc.drawLine(rangeArea.x, y, (int) rangeArea.getMaxX(), y);
                gc.drawLine(sxLine, y, exLine, y);
            }
        }
    }

}
