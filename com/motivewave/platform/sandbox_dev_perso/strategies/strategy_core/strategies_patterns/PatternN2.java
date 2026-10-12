package custom_studies.strategies.strategy_core.strategies_patterns;

import custom_studies.strategies.strategy_core.InterfacePattern;
import custom_studies.strategies.strategy_core.HelperFormat.BracketResult;
import custom_studies.strategies.strategy_core.HelperFormat.DirectionResult;
import custom_studies.strategies.strategy_core.HelperFormat.HiloResult;
import custom_studies.strategies.strategy_core.HelperFormat.RenkoResult;
import custom_studies.strategies.strategy_core.HelperFormat.StrengthResult;

// Retracement de Brick sur Tendance
public class PatternN2 implements InterfacePattern {

    private static final String GREEN = "green";
    private static final String RED = "red";

    private String[] colorHistory;
    private String[] hiLoColor1History;
    private String[] hiLoColor2History;
    private Double[] hiLoPrice1History;
    private Double[] hiLoPrice2History;
    private Double price;
    private float close;

    public PatternN2(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
            Double[] hiLoPrice1History, Double[] hiLoPrice2History, Double price, float close) {
        this.colorHistory = colorHistory;
        this.hiLoColor1History = hiLoColor1History;
        this.hiLoColor2History = hiLoColor2History;
        this.hiLoPrice1History = hiLoPrice1History;
        this.hiLoPrice2History = hiLoPrice2History;
        this.price = price;
        this.close = close;
    }

    public BracketResult getBracket() {
        Double tpTarget = 30.00; // 30 points / 120 ticks
        Double slTarget = 15.00; // 15 points / 60 ticks

        return new BracketResult(tpTarget, slTarget);
    }

    public RenkoResult checkRenkoPatterns() {

        // 8 GREEN - retracement 1 - TP 1
        Boolean patternHigh1 = colorHistory[0].equals(GREEN) && colorHistory[1].equals(RED)
                && colorHistory[2].equals(GREEN)
                && colorHistory[3].equals(GREEN) && colorHistory[4].equals(GREEN)
                && colorHistory[5].equals(GREEN)
                && colorHistory[6].equals(GREEN) && colorHistory[7].equals(GREEN)
                && colorHistory[8].equals(GREEN)
        // && colorHistory[9].equals(GREEN)
        ;
        // 8 RED - retracement 1 - TP 1
        Boolean patternLow1 = colorHistory[0].equals(RED) && colorHistory[1].equals(GREEN)
                && colorHistory[2].equals(RED)
                && colorHistory[3].equals(RED) && colorHistory[4].equals(RED)
                && colorHistory[5].equals(RED)
                && colorHistory[6].equals(RED) && colorHistory[7].equals(RED)
                && colorHistory[8].equals(RED)
        // && colorHistory[9].equals(RED)
        ;
        // 7 GREEN - retracement 2 - TP 1
        Boolean patternHigh2 = colorHistory[0].equals(GREEN) && colorHistory[1].equals(RED)
                && colorHistory[2].equals(RED)
                && colorHistory[3].equals(GREEN) && colorHistory[4].equals(GREEN)
                && colorHistory[5].equals(GREEN)
                && colorHistory[6].equals(GREEN) && colorHistory[7].equals(GREEN)
                && colorHistory[8].equals(GREEN)
        // && colorHistory[9].equals(GREEN)
        ;
        // 7 RED - retracement 2 - TP 1
        Boolean patternLow2 = colorHistory[0].equals(RED) && colorHistory[1].equals(GREEN)
                && colorHistory[2].equals(GREEN)
                && colorHistory[3].equals(RED) && colorHistory[4].equals(RED)
                && colorHistory[5].equals(RED)
                && colorHistory[6].equals(RED) && colorHistory[7].equals(RED)
                && colorHistory[8].equals(RED)
        // && colorHistory[9].equals(RED)
        ;
        // 6 GREEN - retracement 3 - TP 1
        Boolean patternHigh3 = colorHistory[0].equals(GREEN) && colorHistory[1].equals(RED)
                && colorHistory[2].equals(RED)
                && colorHistory[3].equals(RED) && colorHistory[4].equals(GREEN)
                && colorHistory[5].equals(GREEN)
                && colorHistory[6].equals(GREEN) && colorHistory[7].equals(GREEN)
                && colorHistory[8].equals(GREEN)
                && colorHistory[9].equals(GREEN);
        // 6 RED - retracement 3 - TP 1
        Boolean patternLow3 = colorHistory[0].equals(RED) && colorHistory[1].equals(GREEN)
                && colorHistory[2].equals(GREEN)
                && colorHistory[3].equals(GREEN) && colorHistory[4].equals(RED)
                && colorHistory[5].equals(RED)
                && colorHistory[6].equals(RED) && colorHistory[7].equals(RED)
                && colorHistory[8].equals(RED)
                && colorHistory[9].equals(RED);
        // 5 GREEN - retracement 4 - TP 1
        Boolean patternHigh4 = colorHistory[0].equals(GREEN) && colorHistory[1].equals(RED)
                && colorHistory[2].equals(RED)
                && colorHistory[3].equals(RED) && colorHistory[4].equals(RED)
                && colorHistory[5].equals(GREEN)
                && colorHistory[6].equals(GREEN) && colorHistory[7].equals(GREEN)
                && colorHistory[8].equals(GREEN)
                && colorHistory[9].equals(GREEN);
        // 5 RED - retracement 4 - TP 1
        Boolean patternLow4 = colorHistory[0].equals(RED) && colorHistory[1].equals(GREEN)
                && colorHistory[2].equals(GREEN)
                && colorHistory[3].equals(GREEN) && colorHistory[4].equals(GREEN)
                && colorHistory[5].equals(RED)
                && colorHistory[6].equals(RED) && colorHistory[7].equals(RED)
                && colorHistory[8].equals(RED)
                && colorHistory[9].equals(RED);
        // 4 GREEN - retracement 5 - TP 1
        Boolean patternHigh5 = colorHistory[0].equals(GREEN) && colorHistory[1].equals(RED)
                && colorHistory[2].equals(RED)
                && colorHistory[3].equals(RED) && colorHistory[4].equals(RED)
                && colorHistory[5].equals(RED)
                && colorHistory[6].equals(GREEN) && colorHistory[7].equals(GREEN)
                && colorHistory[8].equals(GREEN)
                && colorHistory[9].equals(GREEN) && colorHistory[10].equals(GREEN)
                && colorHistory[11].equals(GREEN)
                && colorHistory[12].equals(GREEN);
        // 4 RED - retracement 5 - TP 1
        Boolean patternLow5 = colorHistory[0].equals(RED) && colorHistory[1].equals(GREEN)
                && colorHistory[2].equals(GREEN)
                && colorHistory[3].equals(GREEN) && colorHistory[4].equals(GREEN)
                && colorHistory[5].equals(GREEN)
                && colorHistory[6].equals(RED) && colorHistory[7].equals(RED)
                && colorHistory[8].equals(RED)
                && colorHistory[9].equals(RED) && colorHistory[10].equals(RED)
                && colorHistory[11].equals(RED)
                && colorHistory[12].equals(RED);
        // 3 GREEN - retracement 6 - TP 1
        Boolean patternHigh6 = colorHistory[0].equals(GREEN) && colorHistory[1].equals(RED)
                && colorHistory[2].equals(RED)
                && colorHistory[3].equals(RED) && colorHistory[4].equals(RED)
                && colorHistory[5].equals(RED)
                && colorHistory[6].equals(RED) && colorHistory[7].equals(GREEN)
                && colorHistory[8].equals(GREEN)
                && colorHistory[9].equals(GREEN) && colorHistory[10].equals(GREEN)
                && colorHistory[11].equals(GREEN)
                && colorHistory[12].equals(GREEN) && colorHistory[13].equals(GREEN)
                && colorHistory[14].equals(GREEN);
        // 3 RED - retracement 6 - TP 1
        Boolean patternLow6 = colorHistory[0].equals(RED) && colorHistory[1].equals(GREEN)
                && colorHistory[2].equals(GREEN)
                && colorHistory[3].equals(GREEN) && colorHistory[4].equals(GREEN)
                && colorHistory[5].equals(GREEN)
                && colorHistory[6].equals(GREEN) && colorHistory[7].equals(RED)
                && colorHistory[8].equals(RED)
                && colorHistory[9].equals(RED) && colorHistory[10].equals(RED)
                && colorHistory[11].equals(RED)
                && colorHistory[12].equals(RED) && colorHistory[13].equals(RED)
                && colorHistory[14].equals(RED);

        Boolean patternHigh = patternHigh1 || patternHigh2 || patternHigh3 || patternHigh4 ||
                patternHigh5 || patternHigh6;
        Boolean patternLow = patternLow1 || patternLow2 || patternLow3 || patternLow4 ||
                patternLow5 || patternLow6;

        return new RenkoResult(patternHigh, patternLow);
    }

    public HiloResult checkHiloPatterns() {

        Boolean trendPatternHigh = true;
        Boolean trendPatternLow = true;

        return new HiloResult(trendPatternHigh, trendPatternLow);
    }

    public StrengthResult checkTrendStrength() {

        Boolean trendStrengthHigh = true;
        Boolean trendStrengthLow = true;

        return new StrengthResult(trendStrengthHigh, trendStrengthLow);
    }

    public DirectionResult checkTrendDirection() {

        boolean dirHigh = hiLoColor1History[0].equals(GREEN)
                && hiLoColor2History[0].equals(GREEN)
                && hiLoPrice1History[0] < close
                && hiLoPrice2History[0] < close;

        boolean dirShort = hiLoColor1History[0].equals(RED)
                && hiLoColor2History[0].equals(RED)
                && hiLoPrice1History[0] > close
                && hiLoPrice2History[0] > close;

        return new DirectionResult(dirHigh, dirShort);
    }

}
