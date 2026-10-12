package custom_studies.strategies.strategy_core_v4;

import java.text.SimpleDateFormat;
import java.util.*;

import custom_studies.strategies.strategy_core_v4.db.StrategyDatabaseManager;
import custom_studies.strategies.strategy_core_v4.utils.FormatResult.StrategyStatsSummary;

public class StrategyAnalysis {

    private final String instrument;
    private final String intervalScale;
    private final Integer intervalValue;

    public StrategyAnalysis(String instrument, String intervalScale, Integer intervalValue) {
        this.instrument = instrument;
        this.intervalScale = intervalScale;
        this.intervalValue = intervalValue;
    }

    public enum Direction {
        HIGH,
        LOW
    }

    // private static class StrategyStats {
    // Integer nbSignalTp = 0;
    // Integer nbSignalSl = 0;
    // Integer nbSignalTotal = 0;
    // }

    private static class StrategyState {
        boolean liveSignal = false;
        Double entrySignalPrice = null;
        Double tpPrice = null;
        Double slPrice = null;
        Direction signalDirection = null;

        public StrategyState() {
        }
    }

    public static class StrategyParams {
        Double takeProfit;
        Double stopLoss;

        public StrategyParams(Double takeProfit, Double stopLoss) {
            this.takeProfit = takeProfit;
            this.stopLoss = stopLoss;
        }
    }

    // private final Map<String, StrategyStats> strategyStatsMap = new HashMap<>();
    private final Map<String, StrategyParams> strategyParamsMap = new HashMap<>();
    private final Map<String, TreeMap<Long, StrategyState>> strategyStateMap = new HashMap<>();
    // private final StrategyDatabaseInitializer dbInitializer = new
    // StrategyDatabaseInitializer();
    // dbInitializer.initDatabase(); // Initialise les tables
    private final StrategyDatabaseManager dbManager = new StrategyDatabaseManager();
    private String currentStrategyName = null;
    // private String currentInstrument = null;
    // private String currentIntervalScale = "tick";
    // private Integer currentIntervalValue = null;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public void setCurrentStrategy(String strategyName, Double takeProfit, Double stopLoss) {
        if (currentStrategyName != null)
            return;

        currentStrategyName = strategyName;
        // currentInstrument = instrument;
        // currentIntervalValue = intervalValue;
        strategyStateMap.putIfAbsent(strategyName, new TreeMap<>());
        strategyParamsMap.putIfAbsent(strategyName, new StrategyParams(takeProfit, stopLoss));
        // strategyStatsMap.putIfAbsent(strategyName, new StrategyStats());
    }

    public void analyseSignalEntryInit(Long timestamp, Direction direction, Double price) {
        if (price == null || currentStrategyName == null || direction == null || timestamp == null)
            return;

        // StrategyStats stats = strategyStatsMap.get(currentStrategyName);
        StrategyParams params = strategyParamsMap.get(currentStrategyName);
        TreeMap<Long, StrategyState> stateMap = strategyStateMap.get(currentStrategyName);

        // if (stats == null || params == null || stateMap == null)
        // return;
        if (params == null || stateMap == null)
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
                state.tpPrice = price + params.takeProfit;
                state.slPrice = price - params.stopLoss;
            } else {
                state.tpPrice = price - params.takeProfit;
                state.slPrice = price + params.stopLoss;
            }

            // stats.nbSignalTotal += 1;
            state.liveSignal = true;
        }
    }

    public void analyseSignalEntry(Double priceHigh, Double priceLow) {
        if (priceHigh == null && priceLow == null && instrument == null && intervalScale == null
                && intervalValue == null)
            return;

        for (Map.Entry<String, TreeMap<Long, StrategyState>> entry : strategyStateMap.entrySet()) {
            String strategyName = entry.getKey();
            TreeMap<Long, StrategyState> stateMap = entry.getValue();
            // StrategyStats stats = strategyStatsMap.get(strategyName);

            StrategyParams params = strategyParamsMap.get(strategyName);

            for (Map.Entry<Long, StrategyState> stateEntry : stateMap.entrySet()) {
                StrategyState state = stateEntry.getValue();
                if (!state.liveSignal || state.signalDirection == null)
                    continue;

                Direction dir = state.signalDirection;
                Integer maxWin = 700;
                Integer maxLoss = 300;
                Integer maxConsecLoss = 1;
                List<String[]> hoursList = new ArrayList<>();
                hoursList.add(new String[] { "09:00", "12:00" });

                if (dir == Direction.HIGH) {
                    if (priceHigh >= state.tpPrice) {
                        // stats.nbSignalTp += 1;
                        dbManager.saveTradeWithDependencies(instrument, intervalScale,
                                intervalValue, strategyName, params.takeProfit, params.stopLoss, maxWin, maxLoss,
                                maxConsecLoss, hoursList, stateEntry.getKey(), dir.name(), state.entrySignalPrice,
                                state.tpPrice, state.slPrice, "TP");
                        resetState(state);
                    } else if (priceLow <= state.slPrice) {
                        // stats.nbSignalSl += 1;
                        dbManager.saveTradeWithDependencies(instrument, intervalScale,
                                intervalValue, strategyName, params.takeProfit, params.stopLoss, maxWin, maxLoss,
                                maxConsecLoss, hoursList, stateEntry.getKey(), dir.name(), state.entrySignalPrice,
                                state.tpPrice, state.slPrice, "SL");
                        resetState(state);
                    }
                } else {
                    if (priceLow <= state.tpPrice) {
                        // stats.nbSignalTp += 1;
                        dbManager.saveTradeWithDependencies(instrument, intervalScale,
                                intervalValue, strategyName, params.takeProfit, params.stopLoss, maxWin, maxLoss,
                                maxConsecLoss, hoursList, stateEntry.getKey(), dir.name(), state.entrySignalPrice,
                                state.tpPrice, state.slPrice, "TP");
                        resetState(state);
                    } else if (priceHigh >= state.slPrice) {
                        // stats.nbSignalSl += 1;
                        dbManager.saveTradeWithDependencies(instrument, intervalScale,
                                intervalValue, strategyName, params.takeProfit, params.stopLoss, maxWin, maxLoss,
                                maxConsecLoss, hoursList, stateEntry.getKey(), dir.name(), state.entrySignalPrice,
                                state.tpPrice, state.slPrice, "SL");
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

        List<StrategyStatsSummary> summaries = dbManager.loadStatsFromDatabase();
        for (StrategyStatsSummary summary : summaries) {
            popupMsgAnalyse.append(buildStatsMessage(summary));
        }

        return popupMsgAnalyse.toString();
    }

    public String printStats(String strategyName) {
        StringBuilder popupMsgAnalyse = new StringBuilder("\n\n===== Résultats par stratégie =====\n");

        if (strategyName == null) {
            return popupMsgAnalyse.append("\nAucune donnée pour la stratégie spécifiée.").toString();
        }

        List<StrategyStatsSummary> summaries = dbManager.loadStatsFromDatabase();
        for (StrategyStatsSummary summary : summaries) {
            if (summary.strategyName.equals(strategyName)) {
                popupMsgAnalyse.append(buildStatsMessage(summary));
                break;
            }
        }

        return popupMsgAnalyse.toString();
    }

    private String buildStatsMessage(StrategyStatsSummary summary) {
        Double totalGain = summary.winSignals * summary.takeProfit;
        Double totalLoss = summary.lossSignals * summary.stopLoss;
        Double profitFactor = (totalLoss != 0) ? totalGain / Math.abs(totalLoss)
                : (totalGain > 0 ? Double.POSITIVE_INFINITY : 0.0);

        String profitFactorStr = String.format("%.2f", profitFactor);
        if (profitFactor < 1.0)
            profitFactorStr += " ⚠️";

        String winRate = (summary.totalSignals > 0 ? (int) (100.0 * summary.winSignals / summary.totalSignals) + "%"
                : "N/A");
        Integer deltaSignals = summary.winSignals - summary.lossSignals;

        return "\n Stratégie : " + summary.strategyName
                + "\n  - Instrument      : " + summary.instrument
                + "\n  - Tick interval   : " + summary.intervalValue + " " + summary.intervalScale
                + "\n  - Début d'analyse : " + dateFormat.format(new Date(summary.startTimestamp))
                + "\n  - Fin d'analyse   : " + dateFormat.format(new Date(summary.endTimestamp))
                + "\n  - Total signals   : " + summary.totalSignals
                + "\n  - Win signals     : " + summary.winSignals
                + "\n  - Loss signals    : " + summary.lossSignals
                + "\n  - Delta signals   : " + deltaSignals
                + "\n  - Win rate        : " + winRate
                + "\n  - Profit Factor   : " + profitFactorStr
                + "\n  - TP cible        : " + summary.takeProfit + " points / " + (summary.takeProfit * 4) + " ticks"
                + "\n  - SL cible        : " + summary.stopLoss + " points / " + (summary.stopLoss * 4) + " ticks"
                + "\n -----------------------------------";
    }

    // public StrategyDatabaseManager getDbManager() {
    // return dbManager;
    // }

    public String[][] getFormattedStatsTableSortedByProfitFactor() {
        List<StrategyStatsSummary> summaries = dbManager.loadStatsFromDatabase();
        System.err.println("Nombre de summaries chargées : " + summaries.size());

        // Tri par profit factor décroissant
        summaries.sort((s1, s2) -> {
            Double pf1 = (s1.lossSignals != 0)
                    ? (s1.winSignals * s1.takeProfit) / Math.abs(s1.lossSignals * s1.stopLoss)
                    : (s1.winSignals > 0 ? Double.POSITIVE_INFINITY : 0.0);

            Double pf2 = (s2.lossSignals != 0)
                    ? (s2.winSignals * s2.takeProfit) / Math.abs(s2.lossSignals * s2.stopLoss)
                    : (s2.winSignals > 0 ? Double.POSITIVE_INFINITY : 0.0);

            return Double.compare(pf2, pf1); // Tri décroissant
        });

        Integer rowCount = summaries.size() + 1; // +1 for header
        String[][] tableData = new String[rowCount][];

        // Header
        tableData[0] = new String[] {
                "Stratégie", "Instrument", "Tick", "Début", "Fin",
                "Total", "Win", "Loss", "Delta", "Win Rate", "Profit Factor", "TP pts", "SL pts"
        };

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (Integer i = 0; i < summaries.size(); i++) {
            StrategyStatsSummary summary = summaries.get(i);

            Double totalGain = summary.winSignals * summary.takeProfit;
            Double totalLoss = summary.lossSignals * summary.stopLoss;
            Double profitFactor = (totalLoss != 0) ? totalGain / Math.abs(totalLoss)
                    : (totalGain > 0 ? Double.POSITIVE_INFINITY : 0.0);
            String profitFactorStr = String.format("%.2f", profitFactor);
            if (profitFactor < 1.0)
                profitFactorStr += " ⚠️";

            String winRate = (summary.totalSignals > 0)
                    ? (int) (100.0 * summary.winSignals / summary.totalSignals) + "%"
                    : "N/A";
            Integer delta = summary.winSignals - summary.lossSignals;

            tableData[i + 1] = new String[] {
                    summary.strategyName,
                    summary.instrument,
                    String.valueOf(summary.intervalValue) + " " + summary.intervalScale,
                    dateFormat.format(new Date(summary.startTimestamp)),
                    dateFormat.format(new Date(summary.endTimestamp)),
                    String.valueOf(summary.totalSignals),
                    String.valueOf(summary.winSignals),
                    String.valueOf(summary.lossSignals),
                    String.valueOf(delta),
                    winRate,
                    profitFactorStr,
                    String.valueOf(summary.takeProfit),
                    String.valueOf(summary.stopLoss)
            };
        }

        return tableData;
    }
}
