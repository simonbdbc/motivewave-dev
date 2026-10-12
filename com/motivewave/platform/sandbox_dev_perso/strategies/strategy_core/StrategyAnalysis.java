package custom_studies.strategies.strategy_core;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class StrategyAnalysis {

    public enum Direction {
        HIGH,
        LOW
    }

    private static class StrategyStats {
        String firstSignalDate = null;
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
        double tpTarget;
        double slTarget;

        public StrategyState(double tpTarget, double slTarget) {
            this.tpTarget = tpTarget;
            this.slTarget = slTarget;
        }
    }

    private final Map<String, StrategyStats> strategyStatsMap = new HashMap<>();
    private final Map<String, StrategyState> strategyStateMap = new HashMap<>();
    private String currentStrategyName = null;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private String currentSignalDate = null;

    public void setCurrentStrategy(String strategyName, double tpTarget, double slTarget) {
        currentStrategyName = strategyName;

        // Initialise les stats si la stratégie est nouvelle
        strategyStatsMap.putIfAbsent(strategyName, new StrategyStats());

        // Initialise le state si la stratégie est nouvelle
        if (!strategyStateMap.containsKey(strategyName)) {
            strategyStateMap.put(strategyName, new StrategyState(tpTarget, slTarget));
        }
    }

    public void analyseSignalEntryInit(Long timestamp, Direction direction, Float price) {
        if (price == null || currentStrategyName == null || direction == null)
            return;

        StrategyStats stats = strategyStatsMap.get(currentStrategyName);
        StrategyState state = strategyStateMap.get(currentStrategyName);

        if (state == null || stats == null || timestamp == null)
            return;

        if (!state.liveSignal) {
            state.entrySignalPrice = price;
            state.signalDirection = direction;

            currentSignalDate = dateFormat.format(timestamp);

            if (direction == Direction.HIGH) {
                state.tpPrice = price + state.tpTarget;
                state.slPrice = price - state.slTarget;
            } else {
                state.tpPrice = price - state.tpTarget;
                state.slPrice = price + state.slTarget;
            }

            stats.nbSignalTotal += 1;
            if (stats.nbSignalTotal == 1 && currentSignalDate != null) {
                stats.firstSignalDate = currentSignalDate;
            }
            state.liveSignal = true;
        }
    }

    public void analyseSignalEntry(Float priceHigh, Float priceLow) {
        if (priceHigh == null && priceLow == null)
            return;

        for (Map.Entry<String, StrategyState> entry : strategyStateMap.entrySet()) {
            String strategyName = entry.getKey();
            StrategyState state = entry.getValue();
            StrategyStats stats = strategyStatsMap.get(strategyName);

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

    private void resetState(StrategyState state) {
        state.liveSignal = false;
        state.entrySignalPrice = null;
        state.tpPrice = null;
        state.slPrice = null;
        state.signalDirection = null;
        currentStrategyName = null;
    }

    public String printStats() {
        String popupMsgAnalyse = "\n\n===== Résultats par stratégie =====\n";
        for (Map.Entry<String, StrategyStats> entry : strategyStatsMap.entrySet()) {
            String strategyName = entry.getKey();
            StrategyStats stats = entry.getValue();
            StrategyState state = strategyStateMap.get(strategyName);

            double totalGain = stats.nbSignalTp * state.tpTarget;
            double totalLoss = stats.nbSignalSl * state.slTarget;
            double profitFactor = (totalLoss != 0) ? totalGain / Math.abs(totalLoss)
                    : (totalGain > 0 ? Double.POSITIVE_INFINITY : 0.0);

            String profitFactorStr = String.format("%.2f", profitFactor);
            if (profitFactor < 1.0) {
                profitFactorStr += " ⚠️";
            }

            String winRate = (stats.nbSignalTotal > 0 ? (int) (100.0 * stats.nbSignalTp / stats.nbSignalTotal) + "%"
                    : "N/A");

            int DeltaSignaux = (stats.nbSignalTp - stats.nbSignalSl);

            popupMsgAnalyse += "\n Stratégie : " + strategyName
                    + "\n  - Début d'analyse : " + stats.firstSignalDate
                    + "\n  - Total signals   : " + stats.nbSignalTotal
                    + "\n  - Win signals     : " + stats.nbSignalTp
                    + "\n  - Loss signals    : " + stats.nbSignalSl
                    + "\n  - Delta signals   : " + DeltaSignaux
                    + "\n  - Win rate        : " + winRate
                    + "\n  - Profit Factor   : " + profitFactorStr
                    + "\n  - TP cible        : " + state.tpTarget + " points / " + (state.tpTarget * 4) + " ticks"
                    + "\n  - SL cible        : " + state.slTarget + " points / " + (state.slTarget * 4) + " ticks"
                    + "\n -----------------------------------";
        }
        return popupMsgAnalyse;
    }

    public String printStats(String strategyName) {
        String popupMsgAnalyse = "\n\n===== Résultats par stratégie =====\n";

        if (strategyName == null)
            return popupMsgAnalyse;

        StrategyStats stats = strategyStatsMap.get(strategyName);
        StrategyState state = strategyStateMap.get(strategyName);

        if (state == null || stats == null)
            return popupMsgAnalyse;

        double totalGain = stats.nbSignalTp * state.tpTarget;
        double totalLoss = stats.nbSignalSl * state.slTarget;
        double profitFactor = (totalLoss != 0) ? totalGain / Math.abs(totalLoss)
                : (totalGain > 0 ? Double.POSITIVE_INFINITY : 0.0);

        String profitFactorStr = String.format("%.2f", profitFactor);
        if (profitFactor < 1.0) {
            profitFactorStr += " ⚠️";
        }

        String winRate = (stats.nbSignalTotal > 0 ? (int) (100.0 * stats.nbSignalTp / stats.nbSignalTotal) + "%"
                : "N/A");

        popupMsgAnalyse += "\n Stratégie : " + strategyName
                + "\n  - Total signaux : " + stats.nbSignalTotal
                + "\n  - TP atteints   : " + stats.nbSignalTp
                + "\n  - SL atteints   : " + stats.nbSignalSl
                + "\n  - Win rate      : " + winRate
                + "\n  - TP cible      : " + state.tpTarget + " points"
                + "\n  - SL cible      : " + state.slTarget + " points"
                + "\n  - Profit Factor : " + profitFactorStr
                + "\n -----------------------------------";
        return popupMsgAnalyse;
    }

}
