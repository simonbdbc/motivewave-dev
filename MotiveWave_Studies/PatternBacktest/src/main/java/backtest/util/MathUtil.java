package backtest.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil {
    /**
     * Arrondit un double à n décimales, arrondi commercial (HALF_UP)
     */
    public static double round(double value, int decimals) {
        if (decimals < 0)
            throw new IllegalArgumentException("decimals must be >= 0");
        if (Double.isNaN(value) || Double.isInfinite(value))
            return Double.NaN;
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
