package custom_studies.strategies.strategy_core_v3.strategies_patterns;

import custom_studies.strategies.strategy_core_v3.utils.InterfacePattern;
import custom_studies.strategies.strategy_core_v3.utils.FormatResult.BracketResult;
import custom_studies.strategies.strategy_core_v3.utils.FormatResult.PatternDirectionResult;
import custom_studies.strategies.strategy_core_v3.utils.FormatResult.PatternHiloResult;
import custom_studies.strategies.strategy_core_v3.utils.FormatResult.PatternRenkoResult;
import custom_studies.strategies.strategy_core_v3.utils.FormatResult.PatternStrengthResult;

// Retracement de Brick sur Tendance
public class PatternN25 implements InterfacePattern {

        private static final String GREEN = "green";
        private static final String RED = "red";

        private String[] colorHistory;
        private String[] hiLoColor1History;
        private String[] hiLoColor2History;
        private Double[] hiLoPrice1History;
        private Double[] hiLoPrice2History;
        private Double price;
        private float close;

        public PatternN25(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
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

        public PatternRenkoResult checkRenkoPatterns() {

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

                Boolean patternHigh = patternHigh5;
                Boolean patternLow = patternLow5;

                return new PatternRenkoResult(patternHigh, patternLow);
        }

        public PatternHiloResult checkHiloPatterns() {

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

                boolean dirHigh = hiLoColor1History[0].equals(GREEN)
                                && hiLoColor2History[0].equals(GREEN)
                                && hiLoPrice1History[0] < close
                                && hiLoPrice2History[0] < close;

                boolean dirShort = hiLoColor1History[0].equals(RED)
                                && hiLoColor2History[0].equals(RED)
                                && hiLoPrice1History[0] > close
                                && hiLoPrice2History[0] > close;

                return new PatternDirectionResult(dirHigh, dirShort);
        }

}
