package custom_studies.strategies.strategy_core_v2;

import java.text.SimpleDateFormat;
import java.util.*;

public class StrategyAnalysis_old {

    public enum Direction {
        HIGH,
        LOW
    }

    private static class StrategyStats {
        int nbSignalTp = 0;
        int nbSignalSl = 0;
        int nbSignalTotal = 0;
    }

    private static class StrategyState {
        boolean liveSignal = false;
        Float entrySignalPrice = null;
        Double tpPrice = null;
        Double slPrice = null;
        Direction signalDirection = null;

        public StrategyState() {}
    }

    private static class StrategyParams {
        double tpTarget;
        double slTarget;

        public StrategyParams(double tpTarget, double slTarget) {
            this.tpTarget = tpTarget;
            this.slTarget = slTarget;
        }
    }

    private final Map<String, StrategyStats> strategyStatsMap = new HashMap<>();
    private final Map<String, StrategyParams> strategyParamsMap = new HashMap<>();
    private final Map<String, TreeMap<Long, StrategyState>> strategyStateMap = new HashMap<>();
    private String currentStrategyName = null;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public void setCurrentStrategy(String strategyName, double tpTarget, double slTarget) {
        currentStrategyName = strategyName;

        strategyStatsMap.putIfAbsent(strategyName, new StrategyStats());
        strategyParamsMap.putIfAbsent(strategyName, new StrategyParams(tpTarget, slTarget));
        strategyStateMap.putIfAbsent(strategyName, new TreeMap<>());
    }

    public void analyseSignalEntryInit(Long timestamp, Direction direction, Float price) {
        if (price == null || currentStrategyName == null || direction == null || timestamp == null)
            return;

        StrategyStats stats = strategyStatsMap.get(currentStrategyName);
        StrategyParams params = strategyParamsMap.get(currentStrategyName);
        TreeMap<Long, StrategyState> stateMap = strategyStateMap.get(currentStrategyName);

        if (stats == null || params == null || stateMap == null)
            return;

        StrategyState state = stateMap.get(timestamp);
        if (state == null) {
            state = new StrategyState();
            stateMap.put(timestamp, state);
        }

        if (!state.liveSignal) {
            state.entrySignalPrice = price;
            state.signalDirection = direction;

            if (direction == Direction.HIGH) {
                state.tpPrice = price + params.tpTarget;
                state.slPrice = price - params.slTarget;
            } else {
                state.tpPrice = price - params.tpTarget;
                state.slPrice = price + params.slTarget;
            }

            stats.nbSignalTotal += 1;
            state.liveSignal = true;
        }
    }

    public void analyseSignalEntry(Float priceHigh, Float priceLow) {
        if (priceHigh == null && priceLow == null)
            return;

        for (Map.Entry<String, TreeMap<Long, StrategyState>> entry : strategyStateMap.entrySet()) {
            String strategyName = entry.getKey();
            TreeMap<Long, StrategyState> stateMap = entry.getValue();
            StrategyStats stats = strategyStatsMap.get(strategyName);

            for (Map.Entry<Long, StrategyState> stateEntry : stateMap.entrySet()) {
                StrategyState state = stateEntry.getValue();

                if (!state.liveSignal || state.signalDirection == null)
                    continue;

                Direction dir = state.signalDirection;

                if (dir == Direction.HIGH) {
                    if (priceHigh >= state.tpPrice) {
                        stats.nbSignalTp += 1;
                        resetState(state);
                    } else if (priceLow <= state.slPrice) {
                        stats.nbSignalSl += 1;
                        resetState(state);
                    }
                } else if (dir == Direction.LOW) {
                    if (priceLow <= state.tpPrice) {
                        stats.nbSignalTp += 1;
                        resetState(state);
                    } else if (priceHigh >= state.slPrice) {
                        stats.nbSignalSl += 1;
                        resetState(state);
                    }
                }
            }
        }
    }

    private void resetState(StrategyState state) {
        state.liveSignal = false;
        state.entrySignalPrice = null;
        state.tpPrice = null;
        state.slPrice = null;
        state.signalDirection = null;
        currentStrategyName = null;
    }

    public String printStats() {
        StringBuilder popupMsgAnalyse = new StringBuilder("\n\n===== Résultats par stratégie =====\n");

        for (String strategyName : strategyStatsMap.keySet()) {
            popupMsgAnalyse.append(buildStatsMessage(strategyName));
        }

        return popupMsgAnalyse.toString();
    }

    public String printStats(String strategyName) {
        StringBuilder popupMsgAnalyse = new StringBuilder("\n\n===== Résultats par stratégie =====\n");

        if (strategyName != null && strategyStatsMap.containsKey(strategyName)) {
            popupMsgAnalyse.append(buildStatsMessage(strategyName));
        }

        return popupMsgAnalyse.toString();
    }

    private String buildStatsMessage(String strategyName) {
        StrategyStats stats = strategyStatsMap.get(strategyName);
        StrategyParams params = strategyParamsMap.get(strategyName);
        TreeMap<Long, StrategyState> stateMap = strategyStateMap.get(strategyName);

        if (stats == null || params == null || stateMap == null || stateMap.isEmpty())
            return "";

        long oldest = stateMap.firstKey();
        long latest = stateMap.lastKey();

        double totalGain = stats.nbSignalTp * params.tpTarget;
        double totalLoss = stats.nbSignalSl * params.slTarget;
        double profitFactor = (totalLoss != 0) ? totalGain / Math.abs(totalLoss) : (totalGain > 0 ? Double.POSITIVE_INFINITY : 0.0);

        String profitFactorStr = String.format("%.2f", profitFactor);
        if (profitFactor < 1.0) {
            profitFactorStr += " ⚠️";
        }

        String winRate = (stats.nbSignalTotal > 0 ? (int) (100.0 * stats.nbSignalTp / stats.nbSignalTotal) + "%" : "N/A");
        int deltaSignals = stats.nbSignalTp - stats.nbSignalSl;

        return "\n Stratégie : " + strategyName
                + "\n  - Début d'analyse : " + dateFormat.format(new Date(oldest))
                + "\n  - Fin d'analyse   : " + dateFormat.format(new Date(latest))
                + "\n  - Total signals   : " + stats.nbSignalTotal
                + "\n  - Win signals     : " + stats.nbSignalTp
                + "\n  - Loss signals    : " + stats.nbSignalSl
                + "\n  - Delta signals   : " + deltaSignals
                + "\n  - Win rate        : " + winRate
                + "\n  - Profit Factor   : " + profitFactorStr
                + "\n  - TP cible        : " + params.tpTarget + " points / " + (params.tpTarget * 4) + " ticks"
                + "\n  - SL cible        : " + params.slTarget + " points / " + (params.slTarget * 4) + " ticks"
                + "\n -----------------------------------";
    }
}
