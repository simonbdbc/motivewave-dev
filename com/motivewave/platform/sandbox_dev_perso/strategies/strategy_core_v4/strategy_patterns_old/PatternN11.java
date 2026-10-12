package custom_studies.strategies.strategy_core_v4.strategy_patterns_old;

import custom_studies.strategies.strategy_core_v4.utils.InterfacePattern;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.BracketResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternDirectionResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternHiloResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternRenkoResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternStrengthResult;

// Retracement de Brick sur Tendance
public class PatternN11 implements InterfacePattern {

    private static final String GREEN = "green";
    private static final String RED = "red";

    private String[] colorHistory;
    private String[] hiLoColor1History;
    private String[] hiLoColor2History;
    private Double[] hiLoPrice1History;
    private Double[] hiLoPrice2History;
    private Double price;
    private Double close;

    public PatternN11(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
            Double[] hiLoPrice1History, Double[] hiLoPrice2History, Double price, Double close) {
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

    public PatternRenkoResult checkRenkoPatterns() {

        // 8 GREEN - retracement 1 - TP 1
        Boolean patternHigh1 = colorHistory[0].equals(GREEN) && colorHistory[1].equals(RED)
        && colorHistory[2].equals(GREEN)
        // && colorHistory[3].equals(GREEN) && colorHistory[4].equals(GREEN)
        // && colorHistory[5].equals(GREEN)
        // && colorHistory[6].equals(GREEN) && colorHistory[7].equals(GREEN)
        // && colorHistory[8].equals(GREEN)
        // && colorHistory[9].equals(GREEN)
        ;
        // 8 RED - retracement 1 - TP 1
        Boolean patternLow1 = colorHistory[0].equals(RED) && colorHistory[1].equals(GREEN)
        && colorHistory[2].equals(RED)
        // && colorHistory[3].equals(RED) && colorHistory[4].equals(RED)
        // && colorHistory[5].equals(RED)
        // && colorHistory[6].equals(RED) && colorHistory[7].equals(RED)
        // && colorHistory[8].equals(RED)
        // && colorHistory[9].equals(RED)
        ;

        Boolean patternHigh = patternHigh1;
        Boolean patternLow = patternLow1;

        // Boolean patternHigh = true;
        // Boolean patternLow = true;

        return new PatternRenkoResult(patternHigh, patternLow);
    }

    public PatternHiloResult checkHiloPatterns() {

        boolean trendPatternHigh = hiLoPrice1History[0] - hiLoPrice2History[0] >= 10
                && close - hiLoPrice1History[0] <= 40
                && close - hiLoPrice1History[0] >= 5
                && close - hiLoPrice1History[1] >= 5
                && hiLoPrice1History[0] > hiLoPrice1History[9]
                && hiLoPrice2History[0] > hiLoPrice2History[9]
        // && hiLoPrice1History[0] - hiLoPrice2History[0] <= 40
        ;
        boolean trendPatternLow = hiLoPrice2History[0] - hiLoPrice1History[0] >= 10
                && hiLoPrice1History[0] - close <= 40
                && hiLoPrice1History[0] - close >= 5
                && hiLoPrice1History[1] - close >= 5
                && hiLoPrice1History[0] < hiLoPrice1History[9]
                && hiLoPrice2History[0] < hiLoPrice2History[9]
        // && hiLoPrice2History[0] - hiLoPrice1History[0] <= 40
        ;

        // Boolean trendPatternHigh = true;
        // Boolean trendPatternLow = true;

        return new PatternHiloResult(trendPatternHigh, trendPatternLow);
    }

    public PatternStrengthResult checkTrendStrength() {

        Boolean trendStrengthHigh = true;
        Boolean trendStrengthLow = true;

        return new PatternStrengthResult(trendStrengthHigh, trendStrengthLow);
    }

    public PatternDirectionResult checkTrendDirection() {

        boolean dirHigh = hiLoColor1History[0].equals(GREEN)
                // && hiLoColor1History[1].equals(GREEN)
                // && hiLoColor1History[2].equals(GREEN)
                && hiLoColor2History[0].equals(GREEN)
                // && hiLoColor2History[1].equals(GREEN)
                // && hiLoColor2History[2].equals(GREEN)
                && hiLoPrice1History[0] < close
                && hiLoPrice2History[0] < close
                && hiLoPrice1History[0] > hiLoPrice2History[0];

        boolean dirShort = hiLoColor1History[0].equals(RED)
                && hiLoColor2History[0].equals(RED)
                && hiLoPrice1History[0] > close
                && hiLoPrice2History[0] > close
                && hiLoPrice1History[0] < hiLoPrice2History[0];

        return new PatternDirectionResult(dirHigh, dirShort);
    }

}
