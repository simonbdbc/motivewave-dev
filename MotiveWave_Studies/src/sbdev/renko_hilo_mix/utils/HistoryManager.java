package sbdev.renko_hilo_mix.utils;

public class HistoryManager {
    public String[] colorHistory = new String[20];
    public String[] crossHistory = new String[10];
    public Double[] hiLoPrice1History = new Double[20];
    public Double[] hiLoPrice2History = new Double[20];
    public String[] hiLoColor1History = new String[20];
    public String[] hiLoColor2History = new String[20];
    public Double[] dowLineUpPriceHistory = new Double[10];
    public Double[] dowLineDownPriceHistory = new Double[10];

    public void updateColorHistory(String newColor) {
        shiftAndInsert(colorHistory, newColor);
    }

    public void updateCrossHistory(String newCross) {
        shiftAndInsert(crossHistory, newCross);
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
