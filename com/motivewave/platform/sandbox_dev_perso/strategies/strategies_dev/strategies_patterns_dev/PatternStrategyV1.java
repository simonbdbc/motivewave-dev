package custom_studies.strategies.strategies_dev.strategies_patterns_dev;

public class PatternStrategyV1 {

    final static String GREEN = "green";
    final static String RED = "red";

    // Méthode qui prend un tableau de chaînes de caractères et renvoie les valeurs
    // de patternHigh1 et patternLow1
    public static boolean[] checkPatterns(String[] colorHistory) {
        if (colorHistory.length != 20) {
            throw new IllegalArgumentException("Le tableau colorHistory doit contenir exactement 10 éléments.");
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
        // Boolean patternHigh8 = colorHistory[0] == GREEN && colorHistory[1] == GREEN &&
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
        return new boolean[] { patternHigh, patternLow };
    }
    // public static boolean[] checkPatterns(String[] hiLoColor1History, String[]
    // hiLoColor2History, double[] hiLoPrice1History, double[] hiLoPrice2History) {
    // if (hiLoColor1History.length != 10) {
    // throw new IllegalArgumentException("Le tableau colorHistory doit contenir
    // exactement 10 éléments.");
    // }

    // Boolean okLong = hiLoColor1History[0] == GREEN
    // && hiLoColor2History[0] == GREEN
    // && hiLoPrice1History[0] < series.getClose(index)
    // && hiLoPrice2History[0] < series.getClose(index);
    // Boolean okShort = hiLoColor1History[0] == RED
    // && hiLoColor2History[0] == RED
    // && hiLoPrice1History[0] > series.getClose(index)
    // && hiLoPrice2History[0] > series.getClose(index);
    // ;
    // return new boolean[] { okLong, okShort };
    // }
}
