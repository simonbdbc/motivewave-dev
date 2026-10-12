package sbdev.joy_ai_project.strategy_v8o3.util;

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

}
