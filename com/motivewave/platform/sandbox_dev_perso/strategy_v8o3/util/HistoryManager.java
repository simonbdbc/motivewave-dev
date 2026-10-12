package sbdev.joy_ai_project.strategy_v8o3.util;

import sbdev.joy_ai_project.strategy_v8o3.model.PatternData;
import sbdev.joy_ai_project.strategy_v8o3.model.Bracket;

public class HistoryManager {
    public String[] crossHistory = new String[20];
    public String[] renkoColorHistory = new String[20];
    public Double[] renkoPriceResHistory = new Double[20];
    public Double[] renkoPriceSupHistory = new Double[20];
    public Double[] hiLoPrice1History = new Double[20];
    public Double[] hiLoPrice2History = new Double[20];
    public String[] hiLoColor1History = new String[20];
    public String[] hiLoColor2History = new String[20];
    public Double[] dowLineUpPriceHistory = new Double[20];
    public Double[] dowLineDownPriceHistory = new Double[20];

    public void updateCrossHistory(String newCross) {
        shiftAndInsert(crossHistory, newCross);
    }

    public void updateRenkoColorHistory(String newColor) {
        shiftAndInsert(renkoColorHistory, newColor);
    }

    public void updateRenkoPriceResHistory(Double value) {
        shiftAndInsert(renkoPriceResHistory, value);
    }

    public void updateRenkoPriceSupHistory(Double value) {
        shiftAndInsert(renkoPriceSupHistory, value);
    }

    public void updateHiLoPrice1History(Double value) {
        shiftAndInsert(hiLoPrice1History, value);
    }

    public void updateHiLoPrice2History(Double value) {
        shiftAndInsert(hiLoPrice2History, value);
    }

    public void updateHiLoColor1History(String value) {
        shiftAndInsert(hiLoColor1History, value);
    }

    public void updateHiLoColor2History(String value) {
        shiftAndInsert(hiLoColor2History, value);
    }

    public void autoUpdateRenkoPriceHistory(Double priceRes, Double priceSup) {
        if (priceRes != null)
            updateRenkoPriceResHistory(priceRes);
        if (priceSup != null)
            updateRenkoPriceSupHistory(priceSup);
    }

    public void autoUpdateHiLoPriceHistory(Double hilo1, Double hilo2) {
        if (hilo1 != null)
            updateHiLoPrice1History(hilo1);
        if (hilo2 != null)
            updateHiLoPrice2History(hilo2);
    }

    public void autoUpdateHiLoColorHistory(double close, Double[] price1History, Double[] price2History) {
        if (price1History[0] != null) {
            updateHiLoColor1History(close < price1History[0] ? "red" : "green");
        }
        if (price2History[0] != null) {
            updateHiLoColor2History(close < price2History[0] ? "red" : "green");
        }
    }

    public void autoUpdateHiLoColorHistory(String color1History, String color2History) {
        if (color1History != null) {
            updateHiLoColor1History(color1History);
        }
        if (color2History != null) {
            updateHiLoColor2History(color2History);
        }
    }

    public void updateDowLineUpPriceHistory(Double value) {
        shiftAndInsert(dowLineUpPriceHistory, value);
    }

    public void updateDowLineDownPriceHistory(Double value) {
        shiftAndInsert(dowLineDownPriceHistory, value);
    }

    private <T> void shiftAndInsert(T[] array, T value) {
        for (int i = array.length - 1; i > 0; i--) {
            array[i] = array[i - 1];
        }
        array[0] = value;
    }

    public <T> boolean isArrayAsNull(T[] array) {
        for (T item : array) {
            if (item == null)
                return true;
        }
        return false;
    }

    public PatternData toPatternData(String instrumentKey, int intervalValue, Bracket bracket, String direction) {
        PatternData pd = new PatternData();
        pd.instrumentKey = instrumentKey;
        pd.intervalValue = intervalValue;
        pd.bracket = bracket;
        pd.direction = direction;

        // Ajoute ci-dessous l'intervalScale si tu le veux
        // pd.intervalScale = "minutes";

        // Génère les patterns compacts (ex: GGRRRRGG)
        pd.patternRenko = normalizePattern(renkoColorHistory, 8);
        pd.patternHilo1 = normalizePattern(hiLoColor1History, 8);
        pd.patternHilo2 = normalizePattern(hiLoColor2History, 8);

        return pd;
    }

    /**
     * Génère le pattern compact (ex : GGRRRRGG) à partir de l'historique String[]
     * 
     * @param history Tableau des couleurs
     * @param length  Nombre d'éléments à prendre en compte (ex: 8 pour matcher sur
     *                8 dernières barres)
     */
    private static String normalizePattern(String[] history, int length) {
        StringBuilder sb = new StringBuilder(length);
        // Commence du plus ancien (index length-1) au plus récent (index 0)
        for (int i = length - 1; i >= 0; i--) {
            String c = history[i];
            if (c == null)
                sb.append('?');
            else if (c.startsWith("g") || c.startsWith("G"))
                sb.append('G');
            else if (c.startsWith("r") || c.startsWith("R"))
                sb.append('R');
            else
                sb.append('?');
        }
        return sb.toString();
    }

}
