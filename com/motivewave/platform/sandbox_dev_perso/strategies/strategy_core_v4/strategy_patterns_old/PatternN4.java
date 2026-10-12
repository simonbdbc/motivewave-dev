package custom_studies.strategies.strategy_core_v4.strategy_patterns_old;

import custom_studies.strategies.strategy_core_v4.utils.InterfacePattern;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.BracketResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternDirectionResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternHiloResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternRenkoResult;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.PatternStrengthResult;

public class PatternN4 implements InterfacePattern {

    private static final String GREEN = "green";
    private static final String RED = "red";

    private String[] colorHistory;
    private String[] hiLoColor1History;
    private String[] hiLoColor2History;
    private Double[] hiLoPrice1History;
    private Double[] hiLoPrice2History;
    private Double price;
    private Double close;

    public PatternN4(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
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
        Double tpTarget = 30.00; // 30 points / 120 ticks
        Double slTarget = 15.00; // 15 points / 60 ticks

        return new BracketResult(tpTarget, slTarget);
    }

    public PatternRenkoResult checkRenkoPatterns() {

        // Condition : Breakout suivi d'un pullback de 1-2 bricks, puis retour dans la tendance
        Boolean patternHigh = 
            colorHistory[4].equals(GREEN) && // breakout initial
            (colorHistory[3].equals(RED) || colorHistory[3].equals(GREEN)) && // petit pullback
            (colorHistory[2].equals(RED) || colorHistory[2].equals(GREEN)) &&
            colorHistory[1].equals(GREEN) &&
            colorHistory[0].equals(GREEN);

        Boolean patternLow =
            colorHistory[4].equals(RED) && // breakout initial
            (colorHistory[3].equals(GREEN) || colorHistory[3].equals(RED)) && // petit pullback
            (colorHistory[2].equals(GREEN) || colorHistory[2].equals(RED)) &&
            colorHistory[1].equals(RED) &&
            colorHistory[0].equals(RED);

        return new PatternRenkoResult(patternHigh, patternLow);
    }

    public PatternHiloResult checkHiloPatterns() {

        // Simple vérification que la tendance est dans le sens des MA
        boolean trendPatternHigh = hiLoColor1History[0].equals(GREEN) && hiLoColor2History[0].equals(GREEN);
        boolean trendPatternLow = hiLoColor1History[0].equals(RED) && hiLoColor2History[0].equals(RED);

        return new PatternHiloResult(trendPatternHigh, trendPatternLow);
    }

    public PatternStrengthResult checkTrendStrength() {

        // Option simple : vérifier qu'on est au-dessus des 2 MA pour achat, ou en dessous pour vente
        boolean trendStrengthHigh = hiLoPrice1History[0] < close && hiLoPrice2History[0] < close;
        boolean trendStrengthLow = hiLoPrice1History[0] > close && hiLoPrice2History[0] > close;

        return new PatternStrengthResult(trendStrengthHigh, trendStrengthLow);
    }

    public PatternDirectionResult checkTrendDirection() {

        boolean dirHigh = hiLoColor1History[0].equals(GREEN) && hiLoColor2History[0].equals(GREEN)
                && hiLoPrice1History[0] < close
                && hiLoPrice2History[0] < close;

        boolean dirShort = hiLoColor1History[0].equals(RED) && hiLoColor2History[0].equals(RED)
                && hiLoPrice1History[0] > close
                && hiLoPrice2History[0] > close;

        return new PatternDirectionResult(dirHigh, dirShort);
    }

}
