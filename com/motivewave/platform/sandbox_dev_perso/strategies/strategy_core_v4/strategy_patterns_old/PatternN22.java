package custom_studies.strategies.strategy_core_v4.strategy_patterns_old;

import custom_studies.strategies.strategy_core_v4.utils.Helper;
import custom_studies.strategies.strategy_core_v4.utils.InterfacePattern;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.BracketResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.ColorCountResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternDirectionResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternHiloResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternRenkoResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternStrengthResult;

// Retracement de Brick sur Tendance
public class PatternN22 implements InterfacePattern {

    private static final String GREEN = "green";
    private static final String RED = "red";

    private String[] colorHistory;
    private String[] hiLoColor1History;
    private String[] hiLoColor2History;
    private Double[] hiLoPrice1History;
    private Double[] hiLoPrice2History;
    private Double price;
    private Double close;

    public PatternN22(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
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
        Double tpTarget = 60.00 / 4; // 30 points / 120 ticks
        Double slTarget = 60.00 / 4; // 15 points / 60 ticks
        // Double tpTarget = 120.00 / 4; // 30 points / 120 ticks
        // Double slTarget = 120.00 / 4; // 15 points / 60 ticks

        return new BracketResult(tpTarget, slTarget);
    }

    public PatternRenkoResult checkRenkoPatterns() {

        // 8 GREEN - retracement 1 - TP 1
        Boolean patternHigh1 = colorHistory[0].equals(GREEN)
                && colorHistory[1].equals(RED)
                && colorHistory[2].equals(RED)
                && colorHistory[3].equals(GREEN)
                && colorHistory[4].equals(GREEN)
                && colorHistory[5].equals(GREEN)
                && colorHistory[6].equals(GREEN)
        // && colorHistory[7].equals(GREEN)
        // && colorHistory[8].equals(GREEN)
        // && colorHistory[9].equals(GREEN)
        ;
        // 8 RED - retracement 1 - TP 1
        Boolean patternLow1 = colorHistory[0].equals(RED)
                && colorHistory[1].equals(GREEN)
                && colorHistory[2].equals(GREEN)
                && colorHistory[3].equals(RED)
                && colorHistory[4].equals(RED)
                && colorHistory[5].equals(RED)
                && colorHistory[6].equals(RED)
        // && colorHistory[7].equals(RED)
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

        Boolean maxSpaceHigh = (hiLoPrice1History[0] - hiLoPrice2History[0] > close - hiLoPrice1History[0])
                ? hiLoPrice1History[0] - hiLoPrice2History[0] <= (close - hiLoPrice1History[0]) * 2
                : close - hiLoPrice1History[0] <= (hiLoPrice1History[0] - hiLoPrice2History[0]) * 2;
        Boolean maxSpaceLow = (hiLoPrice2History[0] - hiLoPrice1History[0] > hiLoPrice1History[0] - close)
                ? hiLoPrice2History[0] - hiLoPrice1History[0] <= (hiLoPrice1History[0] - close) * 2
                : hiLoPrice1History[0] - close <= (hiLoPrice2History[0] - hiLoPrice1History[0]) * 2;

        boolean trendPatternHigh = hiLoPrice1History[0] - hiLoPrice2History[0] >= 10
                // && close - hiLoPrice1History[0] <= 40
                && close - hiLoPrice1History[0] >= 5
                && close - hiLoPrice1History[1] >= 5
                && hiLoPrice1History[0] > hiLoPrice1History[9]
                && hiLoPrice2History[0] > hiLoPrice2History[9]
                // && hiLoPrice1History[0] - hiLoPrice2History[0] <= 40
                // && hiLoPrice1History[0] - hiLoPrice2History[0] <= (close -
                // hiLoPrice1History[0]) * 2
                && maxSpaceHigh;
        boolean trendPatternLow = hiLoPrice2History[0] - hiLoPrice1History[0] >= 10
                // && hiLoPrice1History[0] - close <= 40
                && hiLoPrice1History[0] - close >= 5
                && hiLoPrice1History[1] - close >= 5
                && hiLoPrice1History[0] < hiLoPrice1History[9]
                && hiLoPrice2History[0] < hiLoPrice2History[9]
                // && hiLoPrice2History[0] - hiLoPrice1History[0] <= 40
                // && hiLoPrice2History[0] - hiLoPrice1History[0] <= (hiLoPrice1History[0] -
                // close) * 2
                && maxSpaceLow;

        // Boolean trendPatternHigh = true;
        // Boolean trendPatternLow = true;

        return new PatternHiloResult(trendPatternHigh, trendPatternLow);
    }

    public PatternStrengthResult checkTrendStrength() {

        ColorCountResult color = Helper.countColors(colorHistory);

        Boolean trendStrengthHigh = color.getGreenCount() >= 12;
        Boolean trendStrengthLow = color.getRedCount() >= 12;

        // Boolean trendStrengthHigh = true;
        // Boolean trendStrengthLow = true;

        return new PatternStrengthResult(trendStrengthHigh, trendStrengthLow);
    }

    public PatternDirectionResult checkTrendDirection() {

        ColorCountResult color1 = Helper.countColorConsecutive(hiLoColor1History);
        ColorCountResult color2 = Helper.countColorConsecutive(hiLoColor2History);

        boolean dirHigh = color1.getGreenCount() == 20
                && color2.getGreenCount() == 20
                && hiLoPrice1History[0] < close
                && hiLoPrice2History[0] < close
                && hiLoPrice1History[0] > hiLoPrice2History[0];

        boolean dirShort = color1.getRedCount() == 20
                && color2.getRedCount() == 20
                && hiLoPrice1History[0] > close
                && hiLoPrice2History[0] > close
                && hiLoPrice1History[0] < hiLoPrice2History[0];

        return new PatternDirectionResult(dirHigh, dirShort);
    }

}
