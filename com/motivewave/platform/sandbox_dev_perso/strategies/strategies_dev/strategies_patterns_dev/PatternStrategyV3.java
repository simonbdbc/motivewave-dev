package custom_studies.strategies.strategies_dev.strategies_patterns_dev;

public class PatternStrategyV3 {

    final static String GREEN = "green";
    final static String RED = "red";

    // Méthode qui prend un tableau de chaînes de caractères et renvoie les valeurs
    // de trendPatternHigh et trendPatternLow
    public static boolean[] checkTrendPatterns(String[] hiLoColor1History, String[] hiLoColor2History) {
        if (hiLoColor1History.length != 10) {
            throw new IllegalArgumentException(
                    "Le tableau hiLoColor1History doit contenir exactement 10 éléments.");
        }
        if (hiLoColor2History.length != 10) {
            throw new IllegalArgumentException(
                    "Le tableau hiLoColor2History doit contenir exactement 10 éléments.");
        }

        // // 8 GREEN - retracement 1 - TP 1
        // Boolean patternHigh1 = hiLoColor2History[0] == GREEN && hiLoColor2History[1]
        // == RED && hiLoColor2History[2] == RED
        // && hiLoColor2History[3] == RED && hiLoColor2History[4] == RED &&
        // hiLoColor2History[5] == RED
        // && hiLoColor2History[6] == RED && hiLoColor2History[7] == RED &&
        // hiLoColor2History[8] == RED
        // // && hiLoColor2History[9] == GREEN
        // ;
        // // 8 RED - retracement 1 - TP 1
        // Boolean patternLow1 = hiLoColor2History[0] == RED && hiLoColor2History[1] ==
        // GREEN && hiLoColor2History[2] == GREEN
        // && hiLoColor2History[3] == GREEN && hiLoColor2History[4] == GREEN &&
        // hiLoColor2History[5] == GREEN
        // && hiLoColor2History[6] == GREEN && hiLoColor2History[7] == GREEN &&
        // hiLoColor2History[8] == GREEN
        // // && hiLoColor2History[9] == RED
        // ;

        // // 8 GREEN - retracement 1 - TP 1
        // Boolean patternHigh1 = hiLoColor2History[0] == GREEN && hiLoColor2History[1]
        // == GREEN
        // && hiLoColor2History[2] == GREEN
        // // && hiLoColor2History[3] == GREEN && hiLoColor2History[4] == GREEN &&
        // // hiLoColor2History[5] == GREEN
        // // && hiLoColor2History[6] == GREEN && hiLoColor2History[7] == GREEN &&
        // // hiLoColor2History[8] == GREEN
        // && hiLoColor1History[0] == GREEN && hiLoColor1History[1] == RED;
        // // 8 RED - retracement 1 - TP 1
        // Boolean patternLow1 = hiLoColor2History[0] == RED && hiLoColor2History[1] ==
        // RED
        // && hiLoColor2History[2] == RED
        // // && hiLoColor2History[3] == RED && hiLoColor2History[4] == RED &&
        // // hiLoColor2History[5] == RED
        // // && hiLoColor2History[6] == RED && hiLoColor2History[7] == RED &&
        // // hiLoColor2History[8] == RED
        // && hiLoColor1History[0] == RED && hiLoColor1History[1] == GREEN;
        // 8 GREEN - retracement 1 - TP 1
        Boolean patternHigh1 = hiLoColor2History[0] == GREEN && hiLoColor2History[1] == RED
                && hiLoColor2History[2] == RED && hiLoColor2History[3] == RED
                && hiLoColor2History[4] == RED && hiLoColor2History[5] == RED
                && hiLoColor2History[6] == RED && hiLoColor2History[7] == RED
                && hiLoColor2History[8] == RED && hiLoColor2History[9] == RED

                && hiLoColor1History[0] == GREEN && hiLoColor1History[1] == GREEN
                && hiLoColor1History[2] == GREEN && hiLoColor1History[3] == GREEN
                && hiLoColor1History[4] == GREEN && hiLoColor1History[5] == GREEN
                && hiLoColor1History[6] == GREEN && hiLoColor1History[7] == GREEN
                && hiLoColor1History[8] == GREEN && hiLoColor1History[9] == GREEN;
        // 8 RED - retracement 1 - TP 1
        Boolean patternLow1 = hiLoColor2History[0] == RED && hiLoColor2History[1] == GREEN
                && hiLoColor2History[2] == GREEN && hiLoColor2History[3] == GREEN
                && hiLoColor2History[4] == GREEN && hiLoColor2History[5] == GREEN
                && hiLoColor2History[6] == GREEN && hiLoColor2History[7] == GREEN
                && hiLoColor2History[8] == GREEN && hiLoColor2History[9] == GREEN

                && hiLoColor1History[0] == RED && hiLoColor1History[1] == RED
                && hiLoColor1History[2] == RED && hiLoColor1History[3] == RED
                && hiLoColor1History[4] == RED && hiLoColor1History[5] == RED
                && hiLoColor1History[6] == RED && hiLoColor1History[7] == RED
                && hiLoColor1History[8] == RED && hiLoColor1History[9] == RED;

        Boolean trendPatternHigh = patternHigh1;
        Boolean trendPatternLow = patternLow1;

        return new boolean[] { trendPatternHigh, trendPatternLow };
    }

    // Méthode qui prend un tableau de chaînes de caractères et renvoie les valeurs
    // de trendStrengthHigh et trendStrengthLow
    public static boolean[] checkTrendStrength(Double[] hiLoPrice1History, Double[] hiLoPrice2History,
            Double price) {
        if (hiLoPrice1History.length != 10) {
            throw new IllegalArgumentException(
                    "Le tableau hiLoPrice1History doit contenir exactement 10 éléments.");
        }
        if (hiLoPrice2History.length != 10) {
            throw new IllegalArgumentException(
                    "Le tableau hiLoPrice2History doit contenir exactement 10 éléments.");
        }

        // Boolean trendStrengthHigh = hiLoPrice1History[0] - hiLoPrice2History[0] >=
        // 40;
        Boolean trendStrengthHigh = hiLoPrice1History[0] - hiLoPrice2History[0] <= 10;

        // Boolean trendStrengthLow = hiLoPrice2History[0] - hiLoPrice1History[0] >= 40;
        Boolean trendStrengthLow = hiLoPrice2History[0] - hiLoPrice1History[0] <= 10;

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

}
