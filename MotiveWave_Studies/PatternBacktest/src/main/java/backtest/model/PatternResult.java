package backtest.model;

public class PatternResult {
    public String instrumentKey;
    public String intervalScale;
    public int intervalValue;
    public double renkoSize;
    public String patternRenko;
    public String patternHilo1;
    public String patternHilo2;
    public String direction;
    public int count;
    public String lastDateTimeEntry;
    public int tp;
    public int sl;
    public int countTp;
    public int countSl;
    public int delta;
    public String winrate;
    public double profitFactor;
    public double score;

    public PatternResult(String instrumentKey, String intervalScale, int intervalValue, double renkoSize,
            String patternRenko, String patternHilo1, String patternHilo2, String direction, int count,
            String lastDateTimeEntry, int tp, int sl, int countTp, int countSl, int delta, String winrate,
            double profitFactor, double score) {
        this.instrumentKey = instrumentKey;
        this.intervalScale = intervalScale;
        this.intervalValue = intervalValue;
        this.renkoSize = renkoSize;
        this.patternRenko = patternRenko;
        this.patternHilo1 = patternHilo1;
        this.patternHilo2 = patternHilo2;
        this.direction = direction;
        this.count = count;
        this.lastDateTimeEntry = lastDateTimeEntry;
        this.tp = tp;
        this.sl = sl;
        this.countTp = countTp;
        this.countSl = countSl;
        this.delta = delta;
        this.winrate = winrate;
        this.profitFactor = profitFactor;
        this.score = score;
    }
}
