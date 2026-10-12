package custom_studies.strategies.strategy_core.strategies_patterns;

import custom_studies.strategies.strategy_core.InterfacePattern;
import custom_studies.strategies.strategy_core.HelperFormat.BracketResult;
import custom_studies.strategies.strategy_core.HelperFormat.DirectionResult;
import custom_studies.strategies.strategy_core.HelperFormat.HiloResult;
import custom_studies.strategies.strategy_core.HelperFormat.RenkoResult;
import custom_studies.strategies.strategy_core.HelperFormat.StrengthResult;

public class PatternN1 implements InterfacePattern {

    private static final String GREEN = "green";
    private static final String RED = "red";

    private String[] colorHistory;
    private String[] hiLoColor1History;
    private String[] hiLoColor2History;
    private Double[] hiLoPrice1History;
    private Double[] hiLoPrice2History;
    private Double price;
    private float close;

    // Ultra Scalping de Retracement et Retournement
    public PatternN1(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
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
        Double tpTarget = 5.00; // 5 points / 20 ticks
        Double slTarget = 15.00; // 15 points / 60 ticks

        return new BracketResult(tpTarget, slTarget);
    }

    public RenkoResult checkRenkoPatterns() {

        // 8 GREEN - retracement 1 - TP 1
        Boolean patternHigh1 = colorHistory[0].equals(GREEN) && colorHistory[1].equals(RED)
                && colorHistory[2].equals(GREEN) && colorHistory[3].equals(GREEN)
                && colorHistory[4].equals(GREEN);
        // 8 RED - retracement 1 - TP 1
        Boolean patternLow1 = colorHistory[0].equals(RED) && colorHistory[1].equals(GREEN)
                && colorHistory[2].equals(RED) && colorHistory[3].equals(RED)
                && colorHistory[4].equals(RED);
        // 8 GREEN - retracement 1 - TP 1
        Boolean patternHigh2 = colorHistory[0].equals(GREEN) && colorHistory[1].equals(RED)
                && colorHistory[2].equals(RED) && colorHistory[3].equals(GREEN)
                && colorHistory[4].equals(GREEN);
        // 8 RED - retracement 1 - TP 1
        Boolean patternLow2 = colorHistory[0].equals(RED) && colorHistory[1].equals(GREEN)
                && colorHistory[2].equals(GREEN) && colorHistory[3].equals(RED)
                && colorHistory[4].equals(RED);
        // 8 GREEN - retracement 1 - TP 1
        Boolean patternHigh3 = colorHistory[0].equals(GREEN) && colorHistory[1].equals(RED)
                && colorHistory[2].equals(RED) && colorHistory[3].equals(RED)
                && colorHistory[4].equals(GREEN) && colorHistory[5].equals(GREEN);
        // 8 RED - retracement 1 - TP 1
        Boolean patternLow3 = colorHistory[0].equals(RED) && colorHistory[1].equals(GREEN)
                && colorHistory[2].equals(GREEN) && colorHistory[3].equals(GREEN)
                && colorHistory[4].equals(RED) && colorHistory[5].equals(RED);

        Boolean patternHigh = patternHigh1 || patternHigh2
                || patternHigh3;
        Boolean patternLow = patternLow1 || patternLow2
                || patternLow3;

        return new RenkoResult(patternHigh, patternLow);
    }

    public HiloResult checkHiloPatterns() {

        // PatternHigh1 Hilo1
        boolean patternHigh1 = hiLoColor1History[0].equals(GREEN) && hiLoColor1History[1].equals(GREEN)
                && hiLoColor1History[2].equals(GREEN) && hiLoColor1History[3].equals(GREEN)
                && hiLoColor1History[4].equals(GREEN) && hiLoColor1History[5].equals(GREEN)
                // PatternHigh1 Hilo2
                && hiLoColor2History[0].equals(GREEN) && hiLoColor2History[1].equals(GREEN)
                && hiLoColor2History[2].equals(GREEN) && hiLoColor2History[3].equals(GREEN)
                && hiLoColor2History[4].equals(GREEN) && hiLoColor2History[5].equals(GREEN);
        // PatternLow Hilo1
        boolean patternLow1 = hiLoColor1History[0].equals(RED) && hiLoColor1History[1].equals(RED)
                && hiLoColor1History[2].equals(RED) && hiLoColor1History[3].equals(RED)
                && hiLoColor1History[4].equals(RED) && hiLoColor1History[5].equals(RED)
                // PatternHigh1 Hilo2
                && hiLoColor2History[0].equals(RED) && hiLoColor2History[1].equals(RED)
                && hiLoColor2History[2].equals(RED) && hiLoColor2History[3].equals(RED)
                && hiLoColor2History[4].equals(RED) && hiLoColor2History[5].equals(RED);

        // PatternHigh2 Hilo1
        boolean patternHigh2 = hiLoColor1History[0].equals(GREEN) && hiLoColor1History[1].equals(RED)
                && hiLoColor1History[2].equals(GREEN) && hiLoColor1History[3].equals(GREEN)
                && hiLoColor1History[4].equals(GREEN) && hiLoColor1History[5].equals(GREEN)
                && hiLoColor1History[6].equals(GREEN) && hiLoColor1History[7].equals(GREEN)
                && hiLoColor1History[8].equals(GREEN) && hiLoColor1History[9].equals(GREEN)
                // PatternHigh2 Hilo2
                && hiLoColor2History[0].equals(GREEN) && hiLoColor2History[1].equals(GREEN)
                && hiLoColor2History[2].equals(GREEN) && hiLoColor2History[3].equals(GREEN)
                && hiLoColor2History[4].equals(GREEN) && hiLoColor2History[5].equals(GREEN)
                && hiLoColor2History[6].equals(GREEN) && hiLoColor2History[7].equals(GREEN);
        // PatternLow Hilo2
        boolean patternLow2 = hiLoColor1History[0].equals(RED) && hiLoColor1History[1].equals(GREEN)
                && hiLoColor1History[2].equals(RED) && hiLoColor1History[3].equals(RED)
                && hiLoColor1History[4].equals(RED) && hiLoColor1History[5].equals(RED)
                && hiLoColor1History[6].equals(RED) && hiLoColor1History[7].equals(RED)
                // PatternHigh2 Hilo2
                && hiLoColor2History[0].equals(RED) && hiLoColor2History[1].equals(RED)
                && hiLoColor2History[2].equals(RED) && hiLoColor2History[3].equals(RED)
                && hiLoColor2History[4].equals(RED) && hiLoColor2History[5].equals(RED)
                && hiLoColor2History[6].equals(RED) && hiLoColor2History[7].equals(RED);

        boolean trendPatternHigh = patternHigh1 || patternHigh2;
        boolean trendPatternLow = patternLow1 || patternLow2;

        // Boolean trendPatternHigh = true;
        // Boolean trendPatternLow = true;

        return new HiloResult(trendPatternHigh, trendPatternLow);
    }

    public StrengthResult checkTrendStrength() {

        boolean trendStrengthHigh = hiLoColor1History[0].equals(GREEN)
                && hiLoColor2History[0].equals(GREEN)
                // && hiLoColor2History[1].equals(GREEN)
                && hiLoPrice1History[0] < close
                && hiLoPrice1History[1] < close
                && hiLoPrice1History[2] < close
                && hiLoPrice1History[3] < close
                && hiLoPrice1History[4] < close
                && hiLoPrice1History[5] < close
                && hiLoPrice1History[6] < close
                && hiLoPrice1History[7] < close
                && hiLoPrice1History[8] < close
                && hiLoPrice1History[9] < close
                && hiLoPrice1History[10] < close
                && hiLoPrice1History[11] < close
                && hiLoPrice1History[12] < close
                && hiLoPrice1History[13] < close
                && hiLoPrice1History[14] < close
                && hiLoPrice1History[15] < close
                && hiLoPrice1History[16] < close
                && hiLoPrice1History[17] < close
                && hiLoPrice1History[18] < close
                && hiLoPrice1History[19] < close

                && hiLoPrice2History[0] < close
                && hiLoPrice2History[1] < close
                && hiLoPrice2History[2] < close
                && hiLoPrice2History[3] < close
                && hiLoPrice2History[4] < close
                && hiLoPrice2History[5] < close
                && hiLoPrice2History[6] < close
                && hiLoPrice2History[7] < close
                && hiLoPrice2History[8] < close
                && hiLoPrice2History[9] < close
                && hiLoPrice2History[10] < close
                && hiLoPrice2History[11] < close
                && hiLoPrice2History[12] < close
                && hiLoPrice2History[13] < close
                && hiLoPrice2History[14] < close
                && hiLoPrice2History[15] < close
                && hiLoPrice2History[16] < close
                && hiLoPrice2History[17] < close
                && hiLoPrice2History[18] < close
                && hiLoPrice2History[19] < close;

        boolean trendStrengthLow = hiLoColor1History[0].equals(RED)
                && hiLoColor2History[0].equals(RED)
                // && hiLoColor2History[1].equals(RED)
                && hiLoPrice1History[0] > close
                && hiLoPrice1History[1] > close
                && hiLoPrice1History[2] > close
                && hiLoPrice1History[3] > close
                && hiLoPrice1History[4] > close
                && hiLoPrice1History[5] > close
                && hiLoPrice1History[6] > close
                && hiLoPrice1History[7] > close
                && hiLoPrice1History[8] > close
                && hiLoPrice1History[9] > close
                && hiLoPrice1History[10] > close
                && hiLoPrice1History[11] > close
                && hiLoPrice1History[12] > close
                && hiLoPrice1History[13] > close
                && hiLoPrice1History[14] > close
                && hiLoPrice1History[15] > close
                && hiLoPrice1History[16] > close
                && hiLoPrice1History[17] > close
                && hiLoPrice1History[18] > close
                && hiLoPrice1History[19] > close

                && hiLoPrice2History[0] > close
                && hiLoPrice2History[1] > close
                && hiLoPrice2History[2] > close
                && hiLoPrice2History[3] > close
                && hiLoPrice2History[4] > close
                && hiLoPrice2History[5] > close
                && hiLoPrice2History[6] > close
                && hiLoPrice2History[7] > close
                && hiLoPrice2History[8] > close
                && hiLoPrice2History[9] > close
                && hiLoPrice2History[10] > close
                && hiLoPrice2History[11] > close
                && hiLoPrice2History[12] > close
                && hiLoPrice2History[13] > close
                && hiLoPrice2History[14] > close
                && hiLoPrice2History[15] > close
                && hiLoPrice2History[16] > close
                && hiLoPrice2History[17] > close
                && hiLoPrice2History[18] > close
                && hiLoPrice2History[19] > close;

        // Boolean trendStrengthHigh = true;
        // Boolean trendStrengthLow = true;

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
