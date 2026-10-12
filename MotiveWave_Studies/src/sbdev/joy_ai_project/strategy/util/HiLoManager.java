package sbdev.joy_ai_project.strategy.util;

import sbdev.joy_ai_project.strategy.model.IBar;
import sbdev.joy_ai_project.strategy.model.HiLoResult;
import com.motivewave.platform.sdk.common.Enums;
import java.util.*;

public class HiLoManager {

    public static Double movingAverage(List<? extends IBar> bars, int idx, int period, Enums.MAMethod method,
            String type) {
        if (idx < period - 1)
            return null;
        double sum = 0.0;
        int n = 0;
        switch (method) {
        case SMA:
            for (int i = idx; i > idx - period; i--) {
                if ("HIGH".equals(type))
                    sum += bars.get(i).getHigh();
                else if ("LOW".equals(type))
                    sum += bars.get(i).getLow();
                else
                    sum += bars.get(i).getClose();
                n++;
            }
            return sum / n;
        // Ajoute d'autres méthodes si tu veux (EMA, etc)
        default:
            throw new IllegalArgumentException("Unknown MA method: " + method);
        }
    }

    /**
     * Logique unique, fonctionne sur n'importe quelle liste de IBar groupée à la
     * période désirée
     */
    public Map<Long, HiLoResult> computeHiLo(List<? extends IBar> bars, Enums.MAMethod maMethod, int period) {
        Map<Long, HiLoResult> result = new LinkedHashMap<>();
        if (bars == null || bars.size() < period)
            return result;
        Double prevHiLo = null;
        for (int i = 0; i < bars.size(); i++) {
            IBar bar = bars.get(i);
            Double maHigh = movingAverage(bars, i, period, maMethod, "HIGH");
            Double maLow = movingAverage(bars, i, period, maMethod, "LOW");
            if (maHigh == null || maLow == null)
                continue;
            Double hiLo = prevHiLo == null ? bar.getClose() : prevHiLo;
            boolean isHigh = false, isLow = false;

            // System.out.println("datetime HiLoManager : " +
            // Helper.isoDate(bar.getEndTime()));
            // System.out.println("price close : " + bar.getClose());
            // System.out.println("price hilo1 : " + hiLo);

            if (bar.getClose() > hiLo) {
                hiLo = maLow;
                isLow = true;
            } else if (bar.getClose() < hiLo) {
                hiLo = maHigh;
                isHigh = true;
            }
            prevHiLo = hiLo;
            result.put(bar.getEndTime(), new HiLoResult(bar.getEndTime(), hiLo, bar.getClose(), isHigh, isLow));
        }
        return result;
    }

    /**
     * Cherche dans la Map de HiLo le DERNIER HiLo dont endTime <= timestamp donné.
     * Renvoie null si aucun trouvé.
     */
    public static HiLoResult getLastHiLo(Map<Long, HiLoResult> hiloMap, long timestamp) {
        if (hiloMap == null || hiloMap.isEmpty())
            return null;
        // Trie une seule fois pour optimisation (tu peux aussi mémoriser la liste triée
        // si besoin)
        List<Long> keys = new ArrayList<>(hiloMap.keySet());
        Collections.sort(keys);
        for (int i = keys.size() - 1; i >= 0; i--) {
            if (keys.get(i) <= timestamp)
                return hiloMap.get(keys.get(i));
        }
        return null;
    }

    /**
     * Cherche dans la Map de HiLo le DERNIER HiLo dont endTime <= timestamp donné.
     * Renvoie null si aucun trouvé.
     */
    public static HiLoResult getLastHiLoBefore(Map<Long, HiLoResult> hiloMap, long timestamp) {
        if (hiloMap == null || hiloMap.isEmpty())
            return null;
        // On suppose que la map est potentiellement non triée, donc on parcourt toutes
        // les clés.
        Long lastKey = null;
        for (Long k : hiloMap.keySet()) {
            if (k <= timestamp && (lastKey == null || k > lastKey)) {
                lastKey = k;
            }
        }
        return lastKey == null ? null : hiloMap.get(lastKey);
    }

    public static HiLoResult getLastHiLoBefore(TreeMap<Long, HiLoResult> map, long timestamp) {
        Map.Entry<Long, HiLoResult> entry = map.floorEntry(timestamp);
        return entry != null ? entry.getValue() : null;
    }
    
}
