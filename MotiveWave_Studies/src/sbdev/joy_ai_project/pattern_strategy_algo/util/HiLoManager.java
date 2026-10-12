package sbdev.joy_ai_project.pattern_strategy_algo.service;

import java.util.*;
import sbdev.joy_ai_project.pattern_strategy_algo.model.HiLoResult;
import sbdev.joy_ai_project.pattern_strategy_algo.model.IBar;

import com.motivewave.platform.sdk.common.Enums;

/**
 * HiLoManager: calcule un flux de HiLo sur une liste d'IBar.
 *
 * Principes: - On calcule des MA (sur highs et lows) selon la méthode demandée.
 * - A chaque pas, on compare le close au HiLo PRECEDENT (prevHiLo): close >
 * prevHiLo -> nextHiLo = MA(low) ; isHigh = true close < prevHiLo -> nextHiLo =
 * MA(high); isLow = true sinon -> nextHiLo = prevHiLo - isHigh/isLow est
 * déterminé AVANT la mise à jour (comparaison au prevHiLo). - On retourne une
 * Map<timestamp, HiLoResult>.
 */
public class HiLoManager {

    public Map<Long, HiLoResult> computeHiLo(List<? extends IBar> bars, Enums.MAMethod method, int period) {
        return computeHiLo(bars, method, period, null);
    }

    /** Variante avec initialisation du prevHiLo (continuité inter-jours). */
    public Map<Long, HiLoResult> computeHiLo(List<? extends IBar> bars, Enums.MAMethod method, int period,
            Double initialPrevHiLo) {
        Map<Long, HiLoResult> out = new HashMap<>();
        if (bars == null || bars.size() < period)
            return out;

        // 1) Séries High/Low
        List<Double> highs = new ArrayList<>(bars.size());
        List<Double> lows = new ArrayList<>(bars.size());
        for (IBar b : bars) {
            highs.add(b.getHigh());
            lows.add(b.getLow());
        }

        // 2) MA High/Low
        List<Double> maHighs = calculateMA(highs, method, period);
        List<Double> maLows = calculateMA(lows, method, period);

        // 3) HiLo (corrigé)
        Double prevHiLo = initialPrevHiLo;

        for (int i = period - 1; i < bars.size(); i++) {
            IBar cur = bars.get(i);
            long ts = cur.getEndTime();
            double close = cur.getClose();
            Double mH = maHighs.get(i);
            Double mL = maLows.get(i);

            if (prevHiLo == null) {
                // Première ancre: éviter flip artificiel
                prevHiLo = close;
            }

            boolean isHigh = false;
            boolean isLow = false;
            Double nextHiLo = prevHiLo;

            if (mH != null && mL != null) {
                if (close > prevHiLo) {
                    nextHiLo = mL;
                    isHigh = true;
                } else if (close < prevHiLo) {
                    nextHiLo = mH;
                    isLow = true;
                }
            }

            out.put(ts, new HiLoResult(ts, nextHiLo, close, isHigh, isLow));
            prevHiLo = nextHiLo;
        }
        return out;
    }

    /** MA (SMA/EMA/WMA/SMMA). Résultats null pour i < period-1. */
    public static List<Double> calculateMA(List<Double> values, Enums.MAMethod method, int period) {
        List<Double> result = new ArrayList<>(Collections.nCopies(values.size(), null));
        if (values == null || values.isEmpty() || period <= 0)
            return result;

        switch (method) {
        case SMA -> {
            for (int i = period - 1; i < values.size(); i++) {
                double sum = 0.0;
                for (int j = i - period + 1; j <= i; j++)
                    sum += values.get(j);
                result.set(i, sum / period);
            }
        }
        case EMA -> {
            double multiplier = 2.0 / (period + 1);
            Double emaPrev = null;
            for (int i = 0; i < values.size(); i++) {
                Double val = values.get(i);
                if (val == null)
                    continue;

                if (i == period - 1) {
                    double sma = 0.0;
                    for (int j = i - period + 1; j <= i; j++)
                        sma += values.get(j);
                    emaPrev = sma / period;
                    result.set(i, emaPrev);
                } else if (i >= period) {
                    emaPrev = (val - emaPrev) * multiplier + emaPrev;
                    result.set(i, emaPrev);
                }
            }
        }
        case WMA -> {
            int weightSum = period * (period + 1) / 2;
            for (int i = period - 1; i < values.size(); i++) {
                double wma = 0.0;
                for (int j = 0; j < period; j++)
                    wma += values.get(i - j) * (period - j);
                result.set(i, wma / weightSum);
            }
        }
        case SMMA -> {
            Double smmaPrev = null;
            for (int i = 0; i < values.size(); i++) {
                Double val = values.get(i);
                if (val == null)
                    continue;

                if (i == period - 1) {
                    double sum = 0.0;
                    for (int j = i - period + 1; j <= i; j++)
                        sum += values.get(j);
                    smmaPrev = sum / period;
                } else if (i >= period) {
                    smmaPrev = (smmaPrev * (period - 1) + val) / period;
                }
                if (i >= period - 1)
                    result.set(i, smmaPrev);
            }
        }
        default -> throw new UnsupportedOperationException("Méthode MA non supportée: " + method);
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
