package backtest.model;

import java.time.LocalDate;

public class TradeDetail {
    public LocalDate day;
    public String dayWithLabel;
    public String instrument;
    // public String interval;
    public int intervalValue;
    public Double renkoSize;
    public String patternRenko, patternHilo1, patternHilo2, direction;
    public String entryDateTime, entryTimestamp, entryPrice, exitDateTime, exitPrice, barsToExit, result;
    public PatternResult pattern;
    public int tp;
    public int sl;
}
