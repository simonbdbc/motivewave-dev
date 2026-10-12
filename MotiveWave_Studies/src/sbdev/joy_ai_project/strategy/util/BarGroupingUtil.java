package sbdev.joy_ai_project.strategy.util;

import sbdev.joy_ai_project.strategy.model.SimulatedBar;
import java.util.*;

/**
 * Utilitaire pour grouper une liste de SimulatedBar en barres "closes" sur un
 * intervalle donné (en ms).
 */
public class BarGroupingUtil {

    /**
     * Regroupe les SimulatedBar sur l'intervalle donné (ex: 5min=300000 ms).
     * 
     * @param bars           Liste de SimulatedBar en entrée (ordonnée par timestamp
     *                       croissant !)
     * @param intervalMillis Durée de l'intervalle cible (ex: 1min=60000 ms,
     *                       5min=300000 ms)
     * @return Liste de SimulatedBar représentant les barres closes sur cet
     *         intervalle.
     */
    public static List<SimulatedBar> groupToInterval(List<SimulatedBar> bars, long intervalMillis) {
        List<SimulatedBar> result = new ArrayList<>();
        if (bars == null || bars.isEmpty())
            return result;

        List<SimulatedBar> window = new ArrayList<>();
        long currentIntervalStart = (bars.get(0).timestamp / intervalMillis) * intervalMillis;

        for (SimulatedBar bar : bars) {
            long barIntervalStart = (bar.timestamp / intervalMillis) * intervalMillis;
            if (barIntervalStart != currentIntervalStart && !window.isEmpty()) {
                result.add(aggregateWindow(window, currentIntervalStart, intervalMillis));
                window.clear();
                currentIntervalStart = barIntervalStart;
            }
            window.add(bar);
        }
        if (!window.isEmpty()) {
            result.add(aggregateWindow(window, currentIntervalStart, intervalMillis));
        }
        return result;
    }

    // Fabrique une SimulatedBar "close" à partir d'une fenêtre de SimulatedBar
    private static SimulatedBar aggregateWindow(List<SimulatedBar> bars, long intervalStart, long intervalMillis) {
        if (bars == null || bars.isEmpty())
            return null;

        double open = bars.get(0).open;
        double close = bars.get(bars.size() - 1).close;
        double high = bars.stream().mapToDouble(b -> b.high).max().orElse(open);
        double low = bars.stream().mapToDouble(b -> b.low).min().orElse(open);
        // Timestamp = fin de l'intervalle (standard MotiveWave)
        long intervalEnd = intervalStart + intervalMillis - 1;

        // En cas d'une seule barre, on garde le timestamp réel de la barre
        if (bars.size() == 1)
            intervalEnd = bars.get(0).timestamp;

        return new SimulatedBar(intervalEnd, open, high, low, close);
    }
}
