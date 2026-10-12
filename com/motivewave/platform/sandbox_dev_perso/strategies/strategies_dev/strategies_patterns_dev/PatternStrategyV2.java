package custom_studies.strategies.strategies_dev.strategies_patterns_dev;

public class PatternStrategyV2 {

    final static String GREEN = "green";
    final static String RED = "red";

    // Méthode qui prend un tableau de chaînes de caractères et renvoie les valeurs
    // de trendPatternHigh et trendPatternLow
    public static boolean[] checkTrendPatterns(String[] colorHistory) {
        if (colorHistory.length != 20) {
            throw new IllegalArgumentException(
                    "Le tableau colorHistory doit contenir exactement 20 éléments.");
        }

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

        Boolean trendPatternHigh = patternHigh1 || patternHigh2 || patternHigh3 || patternHigh4 ||
                patternHigh5 || patternHigh6
        // || patternHigh7
        // || patternHigh8
        ;
        Boolean trendPatternLow = patternLow1 || patternLow2 || patternLow3 || patternLow4 ||
                patternLow5 || patternLow6
        // || patternLow7
        // || patternLow8
        ;

        return new boolean[] { trendPatternHigh, trendPatternLow };
    }

    // Méthode qui prend un tableau de chaînes de caractères et renvoie les valeurs
    // de trendStrengthHigh et trendStrengthLow
    public static boolean[] checkTrendStrength(Double[] hiLoPrice1History, Double[] hiLoPrice2History, Double price) {
        if (hiLoPrice1History.length != 10) {
            throw new IllegalArgumentException(
                    "Le tableau hiLoPrice1History doit contenir exactement 10 éléments.");
        }
        if (hiLoPrice2History.length != 10) {
            throw new IllegalArgumentException(
                    "Le tableau hiLoPrice2History doit contenir exactement 10 éléments.");
        }

        Boolean trendStrengthHigh = hiLoPrice1History[0] - hiLoPrice2History[0] >= 35 && price - hiLoPrice1History[0] >= 50;

        Boolean trendStrengthLow = hiLoPrice2History[0] - hiLoPrice1History[0] >= 35 && hiLoPrice1History[0] - price >= 50;

        return new boolean[] { trendStrengthHigh, trendStrengthLow };
    }

    // Méthode qui prend un tableau de chaînes de caractères et renvoie les valeurs
    // de okLong et okShort
    public static boolean[] checkTrendDirection(String[] hiLoColor1History, String[] hiLoColor2History, float close,
            Double[] hiLoPrice1History, Double[] hiLoPrice2History, Double price) {
        if (hiLoColor1History.length != 10) {
            throw new IllegalArgumentException(
                    "Le tableau hiLoColor1History doit contenir exactement 10 éléments.");
        }
        if (hiLoColor2History.length != 10) {
            throw new IllegalArgumentException(
                    "Le tableau hiLoColor2History doit contenir exactement 10 éléments.");
        }
        if (hiLoPrice1History.length != 10) {
            throw new IllegalArgumentException(
                    "Le tableau hiLoPrice1History doit contenir exactement 10 éléments.");
        }
        if (hiLoPrice2History.length != 10) {
            throw new IllegalArgumentException(
                    "Le tableau hiLoPrice2History doit contenir exactement 10 éléments.");
        }

        Boolean okLong = hiLoColor1History[0] == GREEN
                && hiLoColor2History[0] == GREEN
                // && hiLoColor2History[1] == GREEN
                // && hiLoColor2History[2] == GREEN
                // && hiLoColor2History[3] == GREEN
                // && hiLoColor2History[4] == GREEN
                // && hiLoColor2History[5] == GREEN
                // && hiLoColor2History[6] == GREEN
                // && hiLoColor2History[7] == GREEN
                // && hiLoColor2History[8] == GREEN
                // && hiLoColor2History[9] == GREEN
                && hiLoPrice1History[0] < close
                && hiLoPrice2History[0] < close
        // && estSuperieurOuEgal(hiLoPrice2History)
        ;
        Boolean okShort = hiLoColor1History[0] == RED
                && hiLoColor2History[0] == RED
                // && hiLoColor2History[1] == RED
                // && hiLoColor2History[2] == RED
                // && hiLoColor2History[3] == RED
                // && hiLoColor2History[4] == RED
                // && hiLoColor2History[5] == RED
                // && hiLoColor2History[6] == RED
                // && hiLoColor2History[7] == RED
                // && hiLoColor2History[8] == RED
                // && hiLoColor2History[9] == RED
                && hiLoPrice1History[0] > close
                && hiLoPrice2History[0] > close
        // && estSuperieurOuEgal(hiLoPrice2History)
        ;

        return new boolean[] { okLong, okShort };
    }

}
