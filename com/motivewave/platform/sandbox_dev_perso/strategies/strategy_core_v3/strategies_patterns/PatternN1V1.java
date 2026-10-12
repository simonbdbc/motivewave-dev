package custom_studies.strategies.strategy_core_v3.strategies_patterns;

import custom_studies.strategies.strategy_core_v3.utils.InterfacePattern;
import custom_studies.strategies.strategy_core_v3.utils.FormatResult.BracketResult;
import custom_studies.strategies.strategy_core_v3.utils.FormatResult.PatternDirectionResult;
import custom_studies.strategies.strategy_core_v3.utils.FormatResult.PatternHiloResult;
import custom_studies.strategies.strategy_core_v3.utils.FormatResult.PatternRenkoResult;
import custom_studies.strategies.strategy_core_v3.utils.FormatResult.PatternStrengthResult;

public class PatternN1V1 implements InterfacePattern {

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
    public PatternN1V1(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
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
        Double tpTarget = 150.00 / 4; // 5 points / 20 ticks
        Double slTarget = 60.00 / 4; // 15 points / 60 ticks

        return new BracketResult(tpTarget, slTarget);
    }

    // Méthode qui prend un tableau de chaînes de caractères et renvoie les valeurs
    // de patternHigh1 et patternLow1
    public PatternRenkoResult checkRenkoPatterns() {

        // 8 GREEN - retracement 1 - TP 1
        Boolean patternHigh1 = colorHistory[0] == GREEN && colorHistory[1] == RED && colorHistory[2] == GREEN
                && colorHistory[3] == GREEN && colorHistory[4] == GREEN && colorHistory[5] == GREEN
                && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] == GREEN
        // && colorHistory[9] == GREEN
        ;
        // 8 RED - retracement 1 - TP 1
        Boolean patternLow1 = colorHistory[0] == RED && colorHistory[1] == GREEN && colorHistory[2] == RED
                && colorHistory[3] == RED && colorHistory[4] == RED && colorHistory[5] == RED
                && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] == RED
        // && colorHistory[9] == RED
        ;
        // 7 GREEN - retracement 2 - TP 1
        Boolean patternHigh2 = colorHistory[0] == GREEN && colorHistory[1] == RED && colorHistory[2] == RED
                && colorHistory[3] == GREEN && colorHistory[4] == GREEN && colorHistory[5] == GREEN
                && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] == GREEN
        // && colorHistory[9] == GREEN
        ;
        // 7 RED - retracement 2 - TP 1
        Boolean patternLow2 = colorHistory[0] == RED && colorHistory[1] == GREEN && colorHistory[2] == GREEN
                && colorHistory[3] == RED && colorHistory[4] == RED && colorHistory[5] == RED
                && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] == RED
        // && colorHistory[9] == RED
        ;
        // 6 GREEN - retracement 3 - TP 1
        Boolean patternHigh3 = colorHistory[0] == GREEN && colorHistory[1] == RED && colorHistory[2] == RED
                && colorHistory[3] == RED && colorHistory[4] == GREEN && colorHistory[5] == GREEN
                && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] == GREEN
                && colorHistory[9] == GREEN;
        // 6 RED - retracement 3 - TP 1
        Boolean patternLow3 = colorHistory[0] == RED && colorHistory[1] == GREEN && colorHistory[2] == GREEN
                && colorHistory[3] == GREEN && colorHistory[4] == RED && colorHistory[5] == RED
                && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] == RED
                && colorHistory[9] == RED;
        // 5 GREEN - retracement 4 - TP 1
        Boolean patternHigh4 = colorHistory[0] == GREEN && colorHistory[1] == RED && colorHistory[2] == RED
                && colorHistory[3] == RED && colorHistory[4] == RED && colorHistory[5] == GREEN
                && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] == GREEN
                && colorHistory[9] == GREEN;
        // 5 RED - retracement 4 - TP 1
        Boolean patternLow4 = colorHistory[0] == RED && colorHistory[1] == GREEN && colorHistory[2] == GREEN
                && colorHistory[3] == GREEN && colorHistory[4] == GREEN && colorHistory[5] == RED
                && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] == RED
                && colorHistory[9] == RED;
        // 4 GREEN - retracement 5 - TP 1
        Boolean patternHigh5 = colorHistory[0] == GREEN && colorHistory[1] == RED && colorHistory[2] == RED
                && colorHistory[3] == RED && colorHistory[4] == RED && colorHistory[5] == RED
                && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] == GREEN
                && colorHistory[9] == GREEN && colorHistory[10] == GREEN && colorHistory[11] == GREEN
                && colorHistory[12] == GREEN;
        // 4 RED - retracement 5 - TP 1
        Boolean patternLow5 = colorHistory[0] == RED && colorHistory[1] == GREEN && colorHistory[2] == GREEN
                && colorHistory[3] == GREEN && colorHistory[4] == GREEN && colorHistory[5] == GREEN
                && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] == RED
                && colorHistory[9] == RED && colorHistory[10] == RED && colorHistory[11] == RED
                && colorHistory[12] == RED;
        // 3 GREEN - retracement 6 - TP 1
        Boolean patternHigh6 = colorHistory[0] == GREEN && colorHistory[1] == RED && colorHistory[2] == RED
                && colorHistory[3] == RED && colorHistory[4] == RED && colorHistory[5] == RED
                && colorHistory[6] == RED && colorHistory[7] == GREEN && colorHistory[8] == GREEN
                && colorHistory[9] == GREEN && colorHistory[10] == GREEN && colorHistory[11] == GREEN
                && colorHistory[12] == GREEN && colorHistory[13] == GREEN && colorHistory[14] == GREEN;
        // 3 RED - retracement 6 - TP 1
        Boolean patternLow6 = colorHistory[0] == RED && colorHistory[1] == GREEN && colorHistory[2] == GREEN
                && colorHistory[3] == GREEN && colorHistory[4] == GREEN && colorHistory[5] == GREEN
                && colorHistory[6] == GREEN && colorHistory[7] == RED && colorHistory[8] == RED
                && colorHistory[9] == RED && colorHistory[10] == RED && colorHistory[11] == RED
                && colorHistory[12] == RED && colorHistory[13] == RED && colorHistory[14] == RED;
        // // 3 GREEN - retracement 7 - TP 1
        // Boolean patternHigh7 = colorHistory[0] == GREEN && colorHistory[1] == RED &&
        // colorHistory[2] == RED
        // && colorHistory[3] == RED && colorHistory[4] == RED && colorHistory[5] == RED
        // && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] ==
        // GREEN
        // && colorHistory[9] == GREEN && colorHistory[10] == GREEN && colorHistory[11]
        // == GREEN
        // && colorHistory[12] == GREEN && colorHistory[13] == GREEN && colorHistory[14]
        // == GREEN
        // && colorHistory[15] == GREEN;
        // // 3 RED - retracement 7 - TP 1
        // Boolean patternLow7 = colorHistory[0] == RED && colorHistory[1] == GREEN &&
        // colorHistory[2] == GREEN
        // && colorHistory[3] == GREEN && colorHistory[4] == GREEN && colorHistory[5] ==
        // GREEN
        // && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] ==
        // RED
        // && colorHistory[9] == RED && colorHistory[10] == RED && colorHistory[11] ==
        // RED
        // && colorHistory[12] == RED && colorHistory[13] == RED && colorHistory[14] ==
        // RED
        // && colorHistory[15] == RED;
        // // 3 GREEN - retracement 7 - TP 1
        // Boolean patternHigh8 = colorHistory[0] == GREEN && colorHistory[1] == GREEN
        // &&
        // colorHistory[2] == GREEN
        // && colorHistory[3] == RED && colorHistory[4] == RED && colorHistory[5] ==
        // GREEN
        // && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] ==
        // GREEN
        // && colorHistory[9] == RED && colorHistory[10] == RED && colorHistory[11] ==
        // GREEN
        // && colorHistory[12] == GREEN && colorHistory[13] == GREEN;
        // // 3 RED - retracement 7 - TP 1
        // Boolean patternLow8 = colorHistory[0] == RED && colorHistory[1] == RED &&
        // colorHistory[2] == RED
        // && colorHistory[3] == GREEN && colorHistory[4] == GREEN && colorHistory[5] ==
        // RED
        // && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] == RED
        // && colorHistory[9] == GREEN && colorHistory[10] == GREEN && colorHistory[11]
        // == RED
        // && colorHistory[12] == RED && colorHistory[13] == RED;

        // Boolean patternHigh7 = colorHistory[0] == GREEN && colorHistory[1] == RED &&
        // colorHistory[2] == GREEN
        // && colorHistory[3] == GREEN && colorHistory[4] == RED && colorHistory[5] ==
        // GREEN
        // && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] ==
        // GREEN;
        // // 4 GREEN - retracement 2x1 - TP 1
        // Boolean patternLow7 = colorHistory[0] == RED && colorHistory[1] == GREEN &&
        // colorHistory[2] == RED
        // && colorHistory[3] == RED && colorHistory[4] == GREEN && colorHistory[5] ==
        // RED
        // && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] ==
        // RED;
        // // 4 RED - retracement 2x1 - TP 1
        // Boolean patternHigh8 = colorHistory[0] == GREEN && colorHistory[1] == RED &&
        // colorHistory[2] == GREEN
        // && colorHistory[3] == GREEN && colorHistory[4] == GREEN && colorHistory[5] ==
        // GREEN
        // && colorHistory[6] == GREEN && colorHistory[7] == GREEN && colorHistory[8] ==
        // RED
        // && colorHistory[9] == GREEN;
        // // 6 GREEN - retracement 2x1 - TP 1
        // Boolean patternLow8 = colorHistory[0] == RED && colorHistory[1] == GREEN &&
        // colorHistory[2] == RED
        // && colorHistory[3] == RED && colorHistory[4] == RED && colorHistory[5] == RED
        // && colorHistory[6] == RED && colorHistory[7] == RED && colorHistory[8] ==
        // GREEN
        // && colorHistory[9] == RED;
        // // 6 RED - retracement 2x1 - TP 1

        Boolean patternHigh = patternHigh1 || patternHigh2 || patternHigh3 || patternHigh4 ||
                patternHigh5 || patternHigh6
        // || patternHigh7
        // || patternHigh8
        ;
        Boolean patternLow = patternLow1 || patternLow2 || patternLow3 || patternLow4 ||
                patternLow5 || patternLow6
        // || patternLow7
        // || patternLow8
        ;
        return new PatternRenkoResult(patternHigh, patternLow);
    }

    public PatternHiloResult checkHiloPatterns() {

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

        // boolean trendPatternHigh = patternHigh1 || patternHigh2;
        // boolean trendPatternLow = patternLow1 || patternLow2;

        Boolean trendPatternHigh = true;
        Boolean trendPatternLow = true;

        return new PatternHiloResult(trendPatternHigh, trendPatternLow);
    }

    public PatternStrengthResult checkTrendStrength() {

        Boolean trendStrengthHigh = true;
        Boolean trendStrengthLow = true;

        return new PatternStrengthResult(trendStrengthHigh, trendStrengthLow);
    }

    public PatternDirectionResult checkTrendDirection() {

        // boolean dirHigh = hiLoColor1History[0].equals(GREEN)
        // && hiLoColor2History[0].equals(GREEN)
        // && hiLoPrice1History[0] < close
        // && hiLoPrice2History[0] < close;

        // boolean dirShort = hiLoColor1History[0].equals(RED)
        // && hiLoColor2History[0].equals(RED)
        // && hiLoPrice1History[0] > close
        // && hiLoPrice2History[0] > close;

        Boolean dirHigh = true;
        Boolean dirShort = true;

        return new PatternDirectionResult(dirHigh, dirShort);
    }
}
