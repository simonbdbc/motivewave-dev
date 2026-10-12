package sbdev.joy_ai_project.strategy_v8o3.util;

import com.motivewave.platform.sdk.common.Enums;
import java.util.List;

public class MovingAverageUtil {

    /**
     * Calcul de MA sur n'importe quelle List<IBar> (stat ou live)
     */
    public static Double ma(List<? extends IBar> bars, int index, int period, Enums.MAMethod method, boolean isHigh) {
        if (bars == null || bars.size() <= index || index - period + 1 < 0)
            return null;

        double sum = 0.0;
        switch (method) {
        case SMA:
            for (int i = index - period + 1; i <= index; i++) {
                sum += isHigh ? bars.get(i).getHigh() : bars.get(i).getLow();
            }
            return sum / period;
        // Ajouter d'autres méthodes si besoin
        default:
            // Par défaut SMA
            for (int i = index - period + 1; i <= index; i++) {
                sum += isHigh ? bars.get(i).getHigh() : bars.get(i).getLow();
            }
            return sum / period;
        }
    }
}
