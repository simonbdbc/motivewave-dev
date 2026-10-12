package custom_studies.strategies.strategies_dev.strategies_patterns_dev;

public class PatternStrategyV5 {

    private static final String GREEN = "green";
    private static final String RED = "red";

    private String[] colorHistory;
    private String[] hiLoColor1History;
    private String[] hiLoColor2History;
    private Double[] hiLoPrice1History;
    private Double[] hiLoPrice2History;
    private Double price;
    private float close;

    public PatternStrategyV5(String[] colorHistory, String[] hiLoColor1History, String[] hiLoColor2History,
            Double[] hiLoPrice1History, Double[] hiLoPrice2History, Double price, float close) {
        this.colorHistory = colorHistory;
        this.hiLoColor1History = hiLoColor1History;
        this.hiLoColor2History = hiLoColor2History;
        this.hiLoPrice1History = hiLoPrice1History;
        this.hiLoPrice2History = hiLoPrice2History;
        this.price = price;
        this.close = close;
    }

    public RenkoResult checkRenkoUltraScalpPatterns() {

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

        Boolean patternHigh = patternHigh1 || patternHigh2 || patternHigh3;
        Boolean patternLow = patternLow1 || patternLow2 || patternLow3;

        return new RenkoResult(patternHigh, patternLow);
    }

    public RenkoResult checkRenkoPatterns() {

        // boolean patternHigh1 = colorHistory[0].equals(GREEN) &&
        // colorHistory[1].equals(RED)
        // && colorHistory[2].equals(GREEN)
        // && colorHistory[3].equals(GREEN) && colorHistory[4].equals(GREEN)
        // && colorHistory[5].equals(GREEN)
        // && colorHistory[6].equals(GREEN) && colorHistory[7].equals(GREEN)
        // && colorHistory[8].equals(GREEN);

        // boolean patternLow1 = colorHistory[0].equals(RED) &&
        // colorHistory[1].equals(GREEN)
        // && colorHistory[2].equals(RED)
        // && colorHistory[3].equals(RED) && colorHistory[4].equals(RED)
        // && colorHistory[5].equals(RED)
        // && colorHistory[6].equals(RED) && colorHistory[7].equals(RED)
        // && colorHistory[8].equals(RED);

        // return new boolean[] { patternHigh1, patternLow1 };

        // 8 GREEN - retracement 1 - TP 1
        Boolean patternHigh1 = colorHistory[0].equals(GREEN) && colorHistory[1].equals(RED)
                && colorHistory[2].equals(GREEN)
                && colorHistory[3].equals(GREEN) && colorHistory[4].equals(GREEN) && colorHistory[5].equals(GREEN)
                && colorHistory[6].equals(GREEN) && colorHistory[7].equals(GREEN) && colorHistory[8].equals(GREEN)
        // && colorHistory[9].equals(GREEN)
        ;
        // 8 RED - retracement 1 - TP 1
        Boolean patternLow1 = colorHistory[0].equals(RED) && colorHistory[1].equals(GREEN)
                && colorHistory[2].equals(RED)
                && colorHistory[3].equals(RED) && colorHistory[4].equals(RED) && colorHistory[5].equals(RED)
                && colorHistory[6].equals(RED) && colorHistory[7].equals(RED) && colorHistory[8].equals(RED)
        // && colorHistory[9].equals(RED)
        ;
        // 7 GREEN - retracement 2 - TP 1
        Boolean patternHigh2 = colorHistory[0].equals(GREEN) && colorHistory[1].equals(RED)
                && colorHistory[2].equals(RED)
                && colorHistory[3].equals(GREEN) && colorHistory[4].equals(GREEN) && colorHistory[5].equals(GREEN)
                && colorHistory[6].equals(GREEN) && colorHistory[7].equals(GREEN) && colorHistory[8].equals(GREEN)
        // && colorHistory[9].equals(GREEN)
        ;
        // 7 RED - retracement 2 - TP 1
        Boolean patternLow2 = colorHistory[0].equals(RED) && colorHistory[1].equals(GREEN)
                && colorHistory[2].equals(GREEN)
                && colorHistory[3].equals(RED) && colorHistory[4].equals(RED) && colorHistory[5].equals(RED)
                && colorHistory[6].equals(RED) && colorHistory[7].equals(RED) && colorHistory[8].equals(RED)
        // && colorHistory[9].equals(RED)
        ;
        // 6 GREEN - retracement 3 - TP 1
        Boolean patternHigh3 = colorHistory[0].equals(GREEN) && colorHistory[1].equals(RED)
                && colorHistory[2].equals(RED)
                && colorHistory[3].equals(RED) && colorHistory[4].equals(GREEN) && colorHistory[5].equals(GREEN)
                && colorHistory[6].equals(GREEN) && colorHistory[7].equals(GREEN) && colorHistory[8].equals(GREEN)
                && colorHistory[9].equals(GREEN);
        // 6 RED - retracement 3 - TP 1
        Boolean patternLow3 = colorHistory[0].equals(RED) && colorHistory[1].equals(GREEN)
                && colorHistory[2].equals(GREEN)
                && colorHistory[3].equals(GREEN) && colorHistory[4].equals(RED) && colorHistory[5].equals(RED)
                && colorHistory[6].equals(RED) && colorHistory[7].equals(RED) && colorHistory[8].equals(RED)
                && colorHistory[9].equals(RED);
        // // 5 GREEN - retracement 4 - TP 1
        // Boolean patternHigh4 = colorHistory[0].equals(GREEN) &&
        // colorHistory[1].equals(RED)
        // && colorHistory[2].equals(RED)
        // && colorHistory[3].equals(RED) && colorHistory[4].equals(RED) &&
        // colorHistory[5].equals(GREEN)
        // && colorHistory[6].equals(GREEN) && colorHistory[7].equals(GREEN) &&
        // colorHistory[8].equals(GREEN)
        // && colorHistory[9].equals(GREEN);
        // // 5 RED - retracement 4 - TP 1
        // Boolean patternLow4 = colorHistory[0].equals(RED) &&
        // colorHistory[1].equals(GREEN)
        // && colorHistory[2].equals(GREEN)
        // && colorHistory[3].equals(GREEN) && colorHistory[4].equals(GREEN) &&
        // colorHistory[5].equals(RED)
        // && colorHistory[6].equals(RED) && colorHistory[7].equals(RED) &&
        // colorHistory[8].equals(RED)
        // && colorHistory[9].equals(RED);
        // // 4 GREEN - retracement 5 - TP 1
        // Boolean patternHigh5 = colorHistory[0].equals(GREEN) &&
        // colorHistory[1].equals(RED)
        // && colorHistory[2].equals(RED)
        // && colorHistory[3].equals(RED) && colorHistory[4].equals(RED) &&
        // colorHistory[5].equals(RED)
        // && colorHistory[6].equals(GREEN) && colorHistory[7].equals(GREEN) &&
        // colorHistory[8].equals(GREEN)
        // && colorHistory[9].equals(GREEN) && colorHistory[10].equals(GREEN) &&
        // colorHistory[11].equals(GREEN)
        // && colorHistory[12].equals(GREEN);
        // // 4 RED - retracement 5 - TP 1
        // Boolean patternLow5 = colorHistory[0].equals(RED) &&
        // colorHistory[1].equals(GREEN)
        // && colorHistory[2].equals(GREEN)
        // && colorHistory[3].equals(GREEN) && colorHistory[4].equals(GREEN) &&
        // colorHistory[5].equals(GREEN)
        // && colorHistory[6].equals(RED) && colorHistory[7].equals(RED) &&
        // colorHistory[8].equals(RED)
        // && colorHistory[9].equals(RED) && colorHistory[10].equals(RED) &&
        // colorHistory[11].equals(RED)
        // && colorHistory[12].equals(RED);
        // // 3 GREEN - retracement 6 - TP 1
        // Boolean patternHigh6 = colorHistory[0].equals(GREEN) &&
        // colorHistory[1].equals(RED)
        // && colorHistory[2].equals(RED)
        // && colorHistory[3].equals(RED) && colorHistory[4].equals(RED) &&
        // colorHistory[5].equals(RED)
        // && colorHistory[6].equals(RED) && colorHistory[7].equals(GREEN) &&
        // colorHistory[8].equals(GREEN)
        // && colorHistory[9].equals(GREEN) && colorHistory[10].equals(GREEN) &&
        // colorHistory[11].equals(GREEN)
        // && colorHistory[12].equals(GREEN) && colorHistory[13].equals(GREEN) &&
        // colorHistory[14].equals(GREEN);
        // // 3 RED - retracement 6 - TP 1
        // Boolean patternLow6 = colorHistory[0].equals(RED) &&
        // colorHistory[1].equals(GREEN)
        // && colorHistory[2].equals(GREEN)
        // && colorHistory[3].equals(GREEN) && colorHistory[4].equals(GREEN) &&
        // colorHistory[5].equals(GREEN)
        // && colorHistory[6].equals(GREEN) && colorHistory[7].equals(RED) &&
        // colorHistory[8].equals(RED)
        // && colorHistory[9].equals(RED) && colorHistory[10].equals(RED) &&
        // colorHistory[11].equals(RED)
        // && colorHistory[12].equals(RED) && colorHistory[13].equals(RED) &&
        // colorHistory[14].equals(RED);

        Boolean patternHigh = patternHigh1 || patternHigh2 || patternHigh3
        // || patternHigh4 || patternHigh5 || patternHigh6
        ;
        Boolean patternLow = patternLow1 || patternLow2 || patternLow3
        // || patternLow4 || patternLow5 || patternLow6
        ;

        return new RenkoResult(patternHigh, patternLow);
    }

    public HiloResult checkHiloPatterns() {

        // PatternHigh1 Hilo1
        boolean patternHigh1 = hiLoColor1History[0].equals(GREEN) && hiLoColor1History[1].equals(GREEN)
                && hiLoColor1History[2].equals(GREEN) && hiLoColor1History[3].equals(GREEN)
                && hiLoColor1History[4].equals(GREEN) && hiLoColor1History[5].equals(GREEN)
                // && hiLoColor1History[6].equals(GREEN) && hiLoColor1History[7].equals(GREEN)
                // && hiLoColor1History[8].equals(GREEN) && hiLoColor1History[9].equals(GREEN)
                // PatternHigh1 Hilo2
                && hiLoColor2History[0].equals(GREEN) && hiLoColor2History[1].equals(GREEN)
                && hiLoColor2History[2].equals(GREEN) && hiLoColor2History[3].equals(GREEN)
                && hiLoColor2History[4].equals(GREEN) && hiLoColor2History[5].equals(GREEN)
        // && hiLoColor2History[6].equals(GREEN) && hiLoColor2History[7].equals(GREEN)
        // && hiLoColor2History[8] == GREEN && hiLoColor2History[9] == GREEN
        ;
        // PatternLow Hilo1
        boolean patternLow1 = hiLoColor1History[0].equals(RED) && hiLoColor1History[1].equals(RED)
                && hiLoColor1History[2].equals(RED) && hiLoColor1History[3].equals(RED)
                && hiLoColor1History[4].equals(RED) && hiLoColor1History[5].equals(RED)
                // && hiLoColor1History[6].equals(RED) && hiLoColor1History[7].equals(RED)
                // && hiLoColor1History[8].equals(RED) && hiLoColor1History[9].equals(RED)
                // PatternHigh1 Hilo2
                && hiLoColor2History[0].equals(RED) && hiLoColor2History[1].equals(RED)
                && hiLoColor2History[2].equals(RED) && hiLoColor2History[3].equals(RED)
                && hiLoColor2History[4].equals(RED) && hiLoColor2History[5].equals(RED)
        // && hiLoColor2History[6].equals(RED) && hiLoColor2History[7].equals(RED)
        // && hiLoColor2History[8] == RED && hiLoColor2History[9] == RED
        ;

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
                && hiLoColor2History[6].equals(GREEN) && hiLoColor2History[7].equals(GREEN)
        // && hiLoColor2History[8] == GREEN && hiLoColor2History[9] == GREEN
        ;
        // PatternLow Hilo2
        boolean patternLow2 = hiLoColor1History[0].equals(RED) && hiLoColor1History[1].equals(GREEN)
                && hiLoColor1History[2].equals(RED) && hiLoColor1History[3].equals(RED)
                && hiLoColor1History[4].equals(RED) && hiLoColor1History[5].equals(RED)
                && hiLoColor1History[6].equals(RED) && hiLoColor1History[7].equals(RED)
                // && hiLoColor1History[8].equals(RED) && hiLoColor1History[9].equals(RED)
                // PatternHigh2 Hilo2
                && hiLoColor2History[0].equals(RED) && hiLoColor2History[1].equals(RED)
                && hiLoColor2History[2].equals(RED) && hiLoColor2History[3].equals(RED)
                && hiLoColor2History[4].equals(RED) && hiLoColor2History[5].equals(RED)
                && hiLoColor2History[6].equals(RED) && hiLoColor2History[7].equals(RED)
        // && hiLoColor2History[8] == RED && hiLoColor2History[9] == RED
        ;

        boolean trendPatternHigh = patternHigh1 || patternHigh2;
        boolean trendPatternLow = patternLow1 || patternLow2;

        // Boolean trendPatternHigh = true;
        // Boolean trendPatternLow = true;

        return new HiloResult(trendPatternHigh, trendPatternLow);
    }

    public StrengthResult checkTrendStrength() {

        boolean trendStrengthHigh = hiLoPrice1History[0] - hiLoPrice2History[0] >= 10
                && hiLoPrice1History[0] - hiLoPrice2History[0] <= 40
                && price - hiLoPrice1History[0] <= 50;
        boolean trendStrengthLow = hiLoPrice2History[0] - hiLoPrice1History[0] >= 10
                && hiLoPrice2History[0] - hiLoPrice1History[0] <= 40
                && hiLoPrice1History[0] - price <= 50;

        // Boolean trendStrengthHigh = true;
        // Boolean trendStrengthLow = true;

        return new StrengthResult(trendStrengthHigh, trendStrengthLow);
    }

    public DirectionResult checkTrendDirection() {

        boolean okLong = hiLoColor1History[0].equals(GREEN)
                && hiLoColor2History[0].equals(GREEN)
                && hiLoPrice1History[0] < close
                && hiLoPrice2History[0] < close;

        boolean okShort = hiLoColor1History[0].equals(RED)
                && hiLoColor2History[0].equals(RED)
                && hiLoPrice1History[0] > close
                && hiLoPrice2History[0] > close;

        return new DirectionResult(okLong, okShort);
    }

    public static boolean estSuperieurOuEgal(Double[] tableau) {
        for (int i = 0; i < tableau.length - 1; i++) {
            if (tableau[i] < tableau[i + 1]) {
                return false;
            }
        }
        return true;
    }

    public static boolean estInferieurOuEgal(Double[] tableau) {
        for (int i = 0; i < tableau.length - 1; i++) {
            if (tableau[i] > tableau[i + 1]) {
                return false;
            }
        }
        return true;
    }

    public static class RenkoResult {
        public final boolean high;
        public final boolean low;

        public RenkoResult(boolean high, boolean low) {
            this.high = high;
            this.low = low;
        }
    }

    public static class HiloResult {
        public final boolean high;
        public final boolean low;

        public HiloResult(boolean high, boolean low) {
            this.high = high;
            this.low = low;
        }
    }

    public static class StrengthResult {
        public final boolean high;
        public final boolean low;

        public StrengthResult(boolean high, boolean low) {
            this.high = high;
            this.low = low;
        }
    }

    public static class DirectionResult {
        public final boolean high;
        public final boolean low;

        public DirectionResult(boolean high, boolean low) {
            this.high = high;
            this.low = low;
        }
    }
}
