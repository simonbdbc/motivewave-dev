package com.monitoralgo.algo.service;

import com.monitoralgo.algo.model.BacktestResult;
import com.monitoralgo.algo.model.PatternDaoBatchReport;
import com.monitoralgo.algo.model.PatternKey;
import com.monitoralgo.algo.model.TradeDaoBatchReport;
import com.monitoralgo.algo.util.BacktestUtil;
import com.monitoralgo.algo.util.BracketUtil;
import com.monitoralgo.core.dao.*;
import com.monitoralgo.core.model.*;
import com.monitoralgo.core.util.logger.ProcessLogger;
import com.monitoralgo.stats.dao.SnapshotAioByRenkoDao;
import com.monitoralgo.stats.model.SnapshotAioByRenko;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import org.springframework.stereotype.Service;

/**
 * Service cœur de backtest.
 *
 * Terminologie HOMOGÈNE :
 * - GROUPE = instrumentKey | scale#timeframe | renkoSize
 * - SUITE = chaîne de 8 caractères ('R' ou 'G')
 * - PATTERN = combinaison de 3 suites : suiteRenko | suiteHilo1 | suiteHilo2
 * - STRATEGY = direction | takeprofit | stoploss
 * - PATTERN_STRATEGY = GROUPE + PATTERN + STRATEGY
 */
@Service
public class AlgoBacktestCore {

    // === DAO (accès BDD) ===
    private final InstrumentDao instrumentDao = new InstrumentDao();
    private final TimeframeDao timeframeDao = new TimeframeDao();
    private final RenkoSizeDao renkoSizeDao = new RenkoSizeDao();
    private final DirectionDao directionDao = new DirectionDao();
    private final TakeProfitDao takeProfitDao = new TakeProfitDao();
    private final StopLossDao stopLossDao = new StopLossDao();
    private final SuiteDao suiteDao = new SuiteDao();
    private final PatternDao patternDao = new PatternDao();

    // Insertion trades : batch SQL & pipeline COPY/MERGE
    private final TradeStrategyHistoryDao tradeHistoryBatchDao = new TradeStrategyHistoryDao();
    private final TradeStrategyHistoryPipelineDao tradeHistoryPipelineDao = new TradeStrategyHistoryPipelineDao();

    private final PatternStrategyDao patternStrategyDao = new PatternStrategyDao();
    private final PatternStrategyStatsDao statsDao = new PatternStrategyStatsDao();

    /** Longueur d’une SUITE (8 caractères ‘R’/’G’) */
    public static final int PATTERN_LENGTH = 8;

    /** Taille des sous-batchs SQL pour les DAO */
    public static final int BATCH_SQL_SIZE = 20_000;

    private ProcessLogger LOGGER;
    private String STUDY_ID;
    private String INSTANCE_NAME;

    // === Paramètres batch/async insert ===
    private static final int LIST_INIT_CAP_PATTERNS = 50_000;
    private static final int FLUSH_EVERY_PATTERNS = 100_000;

    private static final int LIST_INIT_CAP_TRADES = 250_000;
    private static final int FLUSH_EVERY_TRADES = 500_000;

    private static final int MAX_OUTSTANDING_TASKS = 6;
    private static final int EXECUTOR_THREADS = 6;

    // === Compteurs globaux (côté appli) ===

    // Fenêtres glissées évaluées
    private long totalPatternWindowsEvaluated = 0;

    // PATTERN distincts rencontrés (somme sur tous les groupes)
    private long totalUniquePatternsDetectedAcrossGroups = 0;

    // Trades simulés (avant insertion)
    private long totalTradesSimulated = 0;

    // Payloads envoyés (upserts tentés) — ce ne sont PAS des “nouvelles lignes”
    private long totalPatternStrategyUpsertsPayloads = 0;
    private long totalTradeUpsertsPayloads = 0;

    // Estimation nouveaux vs mis à jour (via countAll() avant/après au niveau
    // batch)
    private long totalPatternNewEstimated = 0;
    private long totalPatternUpdatedEstimated = 0;
    private long totalTradeNewEstimated = 0;
    private long totalTradeUpdatedEstimated = 0;

    // Métriques “possibles vs trouvés”
    private long groupsPossible = 0;
    private long groupsFound = 0;
    private long patternsPossible = 0; // nb de PATTERN (table pattern)
    private long strategiesPossible = 0; // directions × combinaisons (tp,sl)
    private long patternStrategyPossible = 0; // groupsPossible × patternsPossible × strategiesPossible
    private long patternsFoundDistinctGlobal = 0; // PATTERN distincts (global)
    private long strategiesFoundDistinctGlobal = 0; // STRATEGY distincts (global)
    private long patternStrategiesFoundDistinctGlobal = 0; // PATTERN_STRATEGY distincts (global)

    // Sets pour distincts
    private final Set<String> globalPatternsSeen = new HashSet<>();
    private final Set<String> globalStrategiesKeys = new HashSet<>(); // directionId|tpId|slId
    private final Set<String> globalPatternStrategyKeys = new HashSet<>();

    // === Compteurs “continue” (tout ce qui n’était pas compté) ===
    private long groupsSkippedTooFewSnapshots = 0; // GROUPE sauté car < PATTERN_LENGTH + 1
    private long windowsWithInvalidPatternSkipped = 0; // fenêtre glissée dont au moins une suite est invalide
    private long patternsWithInsufficientOccurrences = 0; // PATTERN dont la liste d’indices est < PATTERN_LENGTH
    private long patternsMissingInCatalogSkipped = 0; // PATTERN absent du référentiel pré-généré
    private long entriesNearSeriesEndSkipped = 0; // entrée ignorée car pas de barre suivante

    // Mode d’insertion des trades
    private boolean currentUsePipeline = false;

    // Helper d’affichage durée lisible
    public static String formatDuration(long ms) {
        long hours = ms / (1000L * 60 * 60);
        long minutes = (ms / (1000L * 60)) % 60;
        long seconds = (ms / 1000L) % 60;
        long millis = ms % 1000L;
        return String.format("%02dh %02dm %02ds %03dms", hours, minutes, seconds, millis);
    }

    /**
     * Lancement principal du backtest.
     */
    public void runBacktest(ProcessLogger LOGGER, String STUDY_ID, String INSTANCE_NAME,
            LocalDateTime from, LocalDateTime to, boolean usePipeline) throws SQLException {

        this.LOGGER = LOGGER;
        this.STUDY_ID = STUDY_ID;
        this.INSTANCE_NAME = INSTANCE_NAME;
        this.currentUsePipeline = usePipeline;

        final String instanceId = String.valueOf(System.identityHashCode(this));
        LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "[CORE] Run Backtest", "Start Algo Run Backtest");

        // Paramètres
        LOGGER.log("--------------------------");
        LOGGER.log("[PROCESS] Paramètres");
        LOGGER.log("  ✔ LIST INIT CAP PATTERNS : " + LIST_INIT_CAP_PATTERNS);
        LOGGER.log("  ✔ FLUSH EVERY PATTERNS   : " + FLUSH_EVERY_PATTERNS + " (payload)");
        LOGGER.log("  ✔ LIST INIT CAP TRADES   : " + LIST_INIT_CAP_TRADES);
        LOGGER.log("  ✔ FLUSH EVERY TRADES     : " + FLUSH_EVERY_TRADES + " (payload)");
        LOGGER.log("  ✔ MAX OUTSTANDING TASKS  : " + MAX_OUTSTANDING_TASKS);
        LOGGER.log("  ✔ EXECUTOR THREADS       : " + EXECUTOR_THREADS);
        LOGGER.log("  ✔ MODE TRADES            : " + (usePipeline ? "PIPELINE (COPY/MERGE)" : "BATCH SQL"));
        LOGGER.log("  ✔ BATCH SQL SIZE         : " + BATCH_SQL_SIZE);
        LOGGER.log("--------------------------");

        // Chrono global + temps par étape
        Map<String, Long> timeSpent = new LinkedHashMap<>();
        long tGlobalStart = System.currentTimeMillis();

        // === Référentiels ===
        LOGGER.log("[START] Chargement des tables de référence");
        long t0 = System.currentTimeMillis();
        List<Instrument> instruments = instrumentDao.findAll();
        timeSpent.put("findAll Instruments", System.currentTimeMillis() - t0);
        LOGGER.log("  ✔ Instruments : " + instruments.size());

        t0 = System.currentTimeMillis();
        List<Timeframe> timeframes = timeframeDao.findAll();
        timeSpent.put("findAll Timeframes", System.currentTimeMillis() - t0);
        LOGGER.log("  ✔ Timeframes : " + timeframes.size());

        t0 = System.currentTimeMillis();
        List<RenkoSize> renkoSizes = renkoSizeDao.findAll();
        timeSpent.put("findAll RenkoSizes", System.currentTimeMillis() - t0);
        LOGGER.log("  ✔ RenkoSizes : " + renkoSizes.size());

        t0 = System.currentTimeMillis();
        List<Direction> directions = directionDao.findAll();
        timeSpent.put("findAll Directions", System.currentTimeMillis() - t0);
        LOGGER.log("  ✔ Directions : " + directions.size());

        t0 = System.currentTimeMillis();
        List<TakeProfit> takeProfits = takeProfitDao.findAll();
        timeSpent.put("findAll TakeProfits", System.currentTimeMillis() - t0);
        LOGGER.log("  ✔ TakeProfits : " + takeProfits.size());

        t0 = System.currentTimeMillis();
        List<StopLoss> stopLosses = stopLossDao.findAll();
        timeSpent.put("findAll StopLosses", System.currentTimeMillis() - t0);
        LOGGER.log("  ✔ StopLosses : " + stopLosses.size());

        t0 = System.currentTimeMillis();
        Map<String, Suite> suiteCache = suiteDao.findAllAsMap();
        timeSpent.put("Cache Suites", System.currentTimeMillis() - t0);
        LOGGER.log("  ✔ Suites : " + suiteCache.size());

        LOGGER.log("[WAITING] Chargement de la table Patterns...");
        t0 = System.currentTimeMillis();
        List<Pattern> allPatterns = patternDao.findAll();
        timeSpent.put("Cache Patterns", System.currentTimeMillis() - t0);
        LOGGER.log("  ✔ Patterns : " + allPatterns.size());

        // === Maps de lookup ===
        Map<String, Instrument> instrumentMap = new HashMap<>();
        for (Instrument inst : instruments)
            instrumentMap.put(inst.getKey(), inst);

        Map<String, Timeframe> timeframeMap = new HashMap<>();
        for (Timeframe tf : timeframes)
            timeframeMap.put(tf.getScale() + "#" + tf.getValue(), tf);

        Map<Double, RenkoSize> renkoSizeMap = new HashMap<>();
        for (RenkoSize r : renkoSizes)
            renkoSizeMap.put(r.getValuePoint(), r);

        Map<Double, TakeProfit> takeProfitMap = new HashMap<>();
        for (TakeProfit tp : takeProfits)
            takeProfitMap.put(tp.getValuePoint(), tp);

        Map<Double, StopLoss> stopLossMap = new HashMap<>();
        for (StopLoss sl : stopLosses)
            stopLossMap.put(sl.getValuePoint(), sl);

        Map<String, Pattern> patternMap = new HashMap<>();
        for (Pattern p : allPatterns) {
            String key = p.getSuiteRenko().getPattern() + "|" + p.getSuiteHilo1().getPattern() + "|"
                    + p.getSuiteHilo2().getPattern();
            patternMap.put(key, p);
        }

        // === Capacités “possibles” ===
        groupsPossible = (long) instruments.size() * timeframes.size() * renkoSizes.size();
        patternsPossible = allPatterns.size();

        int directionsCount = directions.size();
        int strategiesCountPerPattern = BracketUtil.generateBracketPoints(takeProfits, stopLosses).size(); // nb de
                                                                                                           // (tp,sl)
        strategiesPossible = (long) directionsCount * strategiesCountPerPattern;
        patternStrategyPossible = groupsPossible * patternsPossible * strategiesPossible;

        // === Snapshots groupés ===
        LOGGER.log("[WAITING] Chargement des Snapshots groupés...");
        long t1 = System.currentTimeMillis();
        Map<String, List<SnapshotAioByRenko>> groupedSnapshots = SnapshotAioByRenkoDao
                .getGroupedSnapshotsAioByRenko(instruments, timeframes, renkoSizes, from, to);
        timeSpent.put("Find Grouped Snapshots", System.currentTimeMillis() - t1);

        // Stats initiales
        groupsFound = groupedSnapshots.size();
        int totalSnapshotsRead = groupedSnapshots.values().stream().mapToInt(List::size).sum();
        LOGGER.log(String.format("[SNAPSHOTS] Groupes trouvés: %d / %d possibles | Snapshots lus: %d",
                groupsFound, groupsPossible, totalSnapshotsRead));

        // Executor asynchrone
        ExecutorService exec = Executors.newFixedThreadPool(EXECUTOR_THREADS);
        Deque<Future<?>> outstanding = new ArrayDeque<>();

        LOGGER.log("[LOOP] Parcours des groupes");
        int groupIndex = 0;

        for (Map.Entry<String, List<SnapshotAioByRenko>> entry : groupedSnapshots.entrySet()) {
            groupIndex++;
            String groupKey = entry.getKey();
            List<SnapshotAioByRenko> snapshots = entry.getValue();

            LOGGER.log("--------------------------");
            LOGGER.log(String.format("  → [GROUPE %d/%d] [%s] (%d snapshots)", groupIndex, groupsFound, groupKey,
                    snapshots.size()));
            LOGGER.log("--------------------------");

            // (1) GROUPE sauté si trop peu de données
            if (snapshots.size() < PATTERN_LENGTH + 1) {
                groupsSkippedTooFewSnapshots++;
                LOGGER.log("  ✔ Skipped (trop peu de données)");
                continue;
            }

            // Buffers (PATTERN_STRATEGY + TRADES)
            List<PatternStrategyFull> groupPatternStrategies = new ArrayList<>(LIST_INIT_CAP_PATTERNS);
            List<TradeStrategyHistoryFull> groupTrades = new ArrayList<>(LIST_INIT_CAP_TRADES);

            // Infos fixes du GROUPE
            String instrumentKey = snapshots.get(0).getInstrumentKey();
            String tfScale = snapshots.get(0).getTfScale();
            int tfValue = snapshots.get(0).getTfInterval();
            double renkoValue = snapshots.get(0).getRenkoSize();

            Instrument instrument = instrumentMap.get(instrumentKey);
            Timeframe timeframe = timeframeMap.get(tfScale + "#" + tfValue);
            RenkoSize renkoSize = renkoSizeMap.get(renkoValue);

            // === DETECTION DES PATTERN (fenêtres glissées) ===
            Map<PatternKey, List<Integer>> patternIndices = new HashMap<>();
            long tDetectStart = System.currentTimeMillis();
            int maxStartIndex = snapshots.size() - PATTERN_LENGTH;

            for (int startIdx = 0; startIdx <= maxStartIndex; startIdx++) {
                List<SnapshotAioByRenko> window = snapshots.subList(startIdx, startIdx + PATTERN_LENGTH);

                String suiteRenko = BacktestUtil.patternFromSnapshots(window, SnapshotAioByRenko::getRenkoColor);
                String suiteHilo1 = BacktestUtil.patternFromSnapshots(window, SnapshotAioByRenko::getHiloColor1);
                String suiteHilo2 = BacktestUtil.patternFromSnapshots(window, SnapshotAioByRenko::getHiloColor2);

                // (2) Fenêtre invalide → continue
                if (!isValidPattern(suiteRenko) || !isValidPattern(suiteHilo1) || !isValidPattern(suiteHilo2)) {
                    windowsWithInvalidPatternSkipped++;
                    LOGGER.log("[WARN] PATTERN invalide: " + suiteRenko + "|" + suiteHilo1 + "|" + suiteHilo2);
                    continue;
                }

                PatternKey pKey = new PatternKey(instrumentKey, tfScale, tfValue, renkoValue, suiteRenko, suiteHilo1,
                        suiteHilo2);
                patternIndices.computeIfAbsent(pKey, k -> new ArrayList<>()).add(startIdx);

                totalPatternWindowsEvaluated++;

                // PATTERN (signature des 3 suites)
                String patternSignature = suiteRenko + "|" + suiteHilo1 + "|" + suiteHilo2;
                globalPatternsSeen.add(patternSignature);
            }
            long detectMs = System.currentTimeMillis() - tDetectStart;
            totalUniquePatternsDetectedAcrossGroups += patternIndices.size();
            LOGGER.log("  ✔ PATTERN uniques détectés (groupe) : " + patternIndices.size());

            // === SIMULATION TRADES + GÉNÉRATION PATTERN_STRATEGY ===
            List<double[]> strategyBrackets = BracketUtil.generateBracketPoints(takeProfits, stopLosses);
            int processedPatternsInGroup = 0;
            int tradeSimuThisGroup = 0;

            for (Map.Entry<PatternKey, List<Integer>> patEntry : patternIndices.entrySet()) {
                processedPatternsInGroup++;

                PatternKey key = patEntry.getKey();
                List<Integer> idxList = patEntry.getValue();

                // (3) PATTERN gardé mais occurrences trop faibles pour générer des entrées
                if (idxList.size() < PATTERN_LENGTH) {
                    patternsWithInsufficientOccurrences++;
                    continue;
                }

                String pk = key.patternRenko + "|" + key.patternHilo1 + "|" + key.patternHilo2;
                Pattern pattern = patternMap.get(pk);

                // (4) PATTERN absent du catalogue pré-généré
                if (pattern == null) {
                    patternsMissingInCatalogSkipped++;
                    LOGGER.log("[WARN] PATTERN introuvable dans le référentiel: " + pk);
                    continue;
                }

                for (Direction direction : directions) {
                    String directionLabel = direction.getLabel();
                    long tSimuStart = System.currentTimeMillis();

                    for (int startIdx : idxList) {
                        int entryIdx = startIdx + PATTERN_LENGTH - 1;

                        // (5) Entrée trop proche de la fin (pas de barre suivante)
                        if (entryIdx + 1 >= snapshots.size()) {
                            entriesNearSeriesEndSkipped++;
                            continue;
                        }

                        Long timestampEntry = snapshots.get(entryIdx).getTimestamp();
                        double entryPrice = snapshots.get(entryIdx).getPriceRes();
                        String dateTimeEntry = snapshots.get(entryIdx).getDateTime();
                        List<SnapshotAioByRenko> futureSnapshots = snapshots.subList(entryIdx + 1, snapshots.size());

                        for (double[] br : strategyBrackets) {
                            double tpValuePoint = br[0];
                            double slValuePoint = br[1];

                            BacktestResult outcome = BacktestEngine.backtestPatternTrade(
                                    directionLabel, timestampEntry, entryPrice, futureSnapshots, tpValuePoint,
                                    slValuePoint);

                            if (!"NoExit".equals(outcome.getResult())) {
                                TradeStrategyHistoryFull trade = new TradeStrategyHistoryFull(
                                        instrument, timeframe, renkoSize, pattern, direction,
                                        takeProfitMap.get(tpValuePoint), stopLossMap.get(slValuePoint),
                                        timestampEntry, dateTimeEntry, entryPrice,
                                        outcome.getTimestampExit(), outcome.getExitDate(), outcome.getExitPrice(),
                                        outcome.getTpPrice(), outcome.getSlPrice(), outcome.getBars(),
                                        outcome.getResult());

                                groupTrades.add(trade);
                                tradeSimuThisGroup++;
                                totalTradesSimulated++;

                                // Flush trades périodique : toujours PATTERN_STRATEGY AVANT
                                if (groupTrades.size() >= FLUSH_EVERY_TRADES) {
                                    if (!groupPatternStrategies.isEmpty()) {
                                        LOGGER.log("[FLUSH] PATTERN_STRATEGY en attente (pré-trades) : "
                                                + groupPatternStrategies.size());
                                        List<PatternStrategyFull> payload = groupPatternStrategies;
                                        submitPatternsBatch(exec, outstanding, payload);
                                        // (plus d’incrément ici → fait dans le worker)
                                        groupPatternStrategies = new ArrayList<>(LIST_INIT_CAP_PATTERNS);
                                    }
                                    LOGGER.log("[FLUSH] TRADES en attente : " + groupTrades.size());
                                    List<TradeStrategyHistoryFull> payloadTrades = groupTrades;
                                    submitTradesBatch(exec, outstanding, payloadTrades);
                                    // (plus d’incrément ici → fait dans le worker)
                                    groupTrades = new ArrayList<>(LIST_INIT_CAP_TRADES);
                                }
                            }
                        }
                    }

                    // temps de simu cumulé (indicatif)
                    long simuMs = System.currentTimeMillis() - tSimuStart;
                    timeSpent.merge("Total Trades Simulation", simuMs, Long::sum);

                    // Génère toutes les STRATEGY (direction, tp, sl) pour ce PATTERN
                    for (double[] br : strategyBrackets) {
                        double tpValuePoint = br[0];
                        double slValuePoint = br[1];

                        PatternStrategyFull ps = new PatternStrategyFull(
                                instrument, timeframe, renkoSize, pattern, direction,
                                takeProfitMap.get(tpValuePoint), stopLossMap.get(slValuePoint));
                        groupPatternStrategies.add(ps);

                        // clé STRATEGY (direction, tp, sl) — distinct global
                        String strategyKey = direction.getId() + "|" +
                                takeProfitMap.get(tpValuePoint).getId() + "|" +
                                stopLossMap.get(slValuePoint).getId();
                        globalStrategiesKeys.add(strategyKey);

                        // clé PATTERN_STRATEGY (IDs des dimensions)
                        String psKey = instrument.getId() + "|" + timeframe.getId() + "|" + renkoSize.getId() + "|" +
                                pattern.getId() + "|" + direction.getId() + "|" +
                                takeProfitMap.get(tpValuePoint).getId() + "|" + stopLossMap.get(slValuePoint).getId();
                        globalPatternStrategyKeys.add(psKey);

                        if (groupPatternStrategies.size() >= FLUSH_EVERY_PATTERNS) {
                            LOGGER.log("[FLUSH] PATTERN_STRATEGY en attente : " + groupPatternStrategies.size());
                            List<PatternStrategyFull> payload = groupPatternStrategies;
                            submitPatternsBatch(exec, outstanding, payload);
                            // (plus d’incrément ici → fait dans le worker)
                            groupPatternStrategies = new ArrayList<>(LIST_INIT_CAP_PATTERNS);
                        }
                    }
                }

                if (processedPatternsInGroup % 100 == 0) {
                    LOGGER.log(String.format("  ✔ PATTERN %d/%d traités", processedPatternsInGroup,
                            patternIndices.size()));
                }
            }

            // FLUSH FINAL du groupe (toujours PATTERN_STRATEGY → TRADES)
            if (!groupPatternStrategies.isEmpty()) {
                LOGGER.log("[FLUSH] PATTERN_STRATEGY en attente (final groupe) : " + groupPatternStrategies.size());
                List<PatternStrategyFull> payload = groupPatternStrategies;
                submitPatternsBatch(exec, outstanding, payload);
                // (plus d’incrément ici → fait dans le worker)
                groupPatternStrategies = new ArrayList<>(LIST_INIT_CAP_PATTERNS);
            }
            if (!groupTrades.isEmpty()) {
                LOGGER.log("[FLUSH] TRADES en attente (final groupe) : " + groupTrades.size());
                List<TradeStrategyHistoryFull> payload = groupTrades;
                submitTradesBatch(exec, outstanding, payload);
                // (plus d’incrément ici → fait dans le worker)
                groupTrades = new ArrayList<>(LIST_INIT_CAP_TRADES);
            }

            LOGGER.log("--------------------------");
            LOGGER.log(String.format("  ✔ Groupe %d terminé → trades simulés: %d | détection PATTERN: %s",
                    groupIndex, tradeSimuThisGroup, formatDuration(detectMs)));
            LOGGER.log("--------------------------");

            LOGGER.log("[MEM] Groupe traité → buffers réinitialisés");
        }

        // Attendre la fin des insertions asynchrones
        LOGGER.log("[DB] Attente de la fin des insertions asynchrones...");
        waitAll(outstanding);
        shutdownExecutor(exec);

        // Mise à jour des stats agrégées
        try {
            LOGGER.log("--------------------------");
            LOGGER.log("Début de mise à jour des stats de tous les PATTERN_STRATEGY");
            LOGGER.log("--------------------------");
            statsDao.updatePatternStrategyStatsFull(LOGGER, STUDY_ID, INSTANCE_NAME);
            LOGGER.log("--------------------------");
            LOGGER.log("Fin de mise à jour des stats de tous les PATTERN_STRATEGY");
            LOGGER.log("--------------------------");
        } catch (Exception e) {
            LOGGER.log("[ERROR] Batch update stats échoué: " + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            LOGGER.log(sw.toString());
        }

        // Distincts globaux
        patternsFoundDistinctGlobal = globalPatternsSeen.size();
        strategiesFoundDistinctGlobal = globalStrategiesKeys.size();
        patternStrategiesFoundDistinctGlobal = globalPatternStrategyKeys.size();

        // Comptages réels en base
        long patternStrategyRows = safeCountPatternStrategy();
        long tradeRows = safeCountTrades();

        // Récap final
        long tGlobalEnd = System.currentTimeMillis();
        timeSpent.put("Loop Backtest", tGlobalEnd - tGlobalStart);

        LOGGER.log("===== BACKTEST RECAP =====");
        timeSpent.forEach((step, ms) -> LOGGER.log(String.format("%-30s : %s", step, formatDuration(ms))));
        LOGGER.log("--------------------------");
        LOGGER.log(String.format("GROUPES trouvés/possibles           : %d / %d", groupsFound, groupsPossible));
        LOGGER.log(String.format("PATTERN trouvés/possibles           : %d / %d", patternsFoundDistinctGlobal,
                patternsPossible));
        LOGGER.log(String.format("STRATEGY trouvés/possibles          : %d / %d", strategiesFoundDistinctGlobal,
                strategiesPossible));
        LOGGER.log(String.format("PATTERN_STRATEGY trouvés/possibles  : %d / %d", patternStrategiesFoundDistinctGlobal,
                patternStrategyPossible));
        LOGGER.log("--------------------------");
        LOGGER.log(String.format("SNAPSHOTS lus                        : %d", totalSnapshotsRead));
        LOGGER.log(String.format("PATTERN fenêtres glissées évaluées   : %d", totalPatternWindowsEvaluated));
        LOGGER.log(String.format("PATTERN uniques détectés (somme grp) : %d", totalUniquePatternsDetectedAcrossGroups));
        LOGGER.log(String.format("TRADES simulés (avant insertion)     : %d", totalTradesSimulated));
        LOGGER.log("--------------------------");
        LOGGER.log(
                String.format("UPSERT PATTERN_STRATEGY envoyés (payloads) : %d", totalPatternStrategyUpsertsPayloads));
        LOGGER.log(String.format("  ↳ Estim. nouveaux : %d | mis_à_jour : %d", totalPatternNewEstimated,
                totalPatternUpdatedEstimated));
        LOGGER.log(String.format("UPSERT TRADES envoyés (payloads)           : %d", totalTradeUpsertsPayloads));
        LOGGER.log(String.format("  ↳ Estim. nouveaux : %d | mis_à_jour : %d", totalTradeNewEstimated,
                totalTradeUpdatedEstimated));
        LOGGER.log("--------------------------");
        LOGGER.log(String.format("EN BASE – pattern_strategy           : %d lignes DISTINCTES", patternStrategyRows));
        LOGGER.log(String.format("EN BASE – trade_strategy_history     : %d lignes DISTINCTES", tradeRows));
        LOGGER.log("--------------------------");
        // Récap dédié “continues” / skips :
        LOGGER.log("SKIPS / CONTINUES -------------------");
        LOGGER.log(String.format("  Groupes sautés (trop peu de snapshots)      : %d", groupsSkippedTooFewSnapshots));
        LOGGER.log(
                String.format("  Fenêtres invalides (suite non valide)       : %d", windowsWithInvalidPatternSkipped));
        LOGGER.log(String.format("  PATTERN ignorés (occurrences insuffisantes) : %d",
                patternsWithInsufficientOccurrences));
        LOGGER.log(
                String.format("  PATTERN manquants dans le catalogue         : %d", patternsMissingInCatalogSkipped));
        LOGGER.log(String.format("  Entrées ignorées (fin de série)             : %d", entriesNearSeriesEndSkipped));
        LOGGER.log("-------------------------------------");
        LOGGER.log("Temps TOTAL           : " + formatDuration(tGlobalEnd - tGlobalStart));
        LOGGER.log("=================================================");
    }

    // ===== Helpers – attente executor & comptages sûrs =====

    private void shutdownExecutor(ExecutorService exec) {
        exec.shutdown();
        try {
            if (!exec.awaitTermination(30, TimeUnit.MINUTES)) {
                LOGGER.log("[DB] Warning: timeout attente Executor, shutdownNow()");
                exec.shutdownNow();
            }
        } catch (InterruptedException ie) {
            exec.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private long safeCountPatternStrategy() {
        try {
            return patternStrategyDao.countAll();
        } catch (Exception e) {
            LOGGER.log("[WARN] countAll(pattern_strategy) impossible: " + e.getMessage());
            return -1;
        }
    }

    private long safeCountTrades() {
        try {
            return tradeHistoryBatchDao.countAll();
        } catch (Exception e) {
            LOGGER.log("[WARN] countAll(trade_strategy_history) impossible: " + e.getMessage());
            return -1;
        }
    }

    // ==================== Helpers async insert ====================

    /**
     * Insert asynchrone d’un batch de PATTERN_STRATEGY (+ estimation new/update).
     */
    private void submitPatternsBatch(ExecutorService exec, Deque<Future<?>> outstanding,
            List<PatternStrategyFull> payload) {
        waitIfTooManyOutstanding(outstanding);
        outstanding.addLast(exec.submit(() -> {
            try {
                LOGGER.log("[PATTERN_STRATEGY UPSERT][SEND] items=" + payload.size() + " (Thread="
                        + Thread.currentThread().getName() + ")");
                long before = safeCountPatternStrategy();

                PatternDaoBatchReport rep = patternStrategyDao.insertPartialBatchWithReport(
                        payload, BATCH_SQL_SIZE, LOGGER, STUDY_ID, INSTANCE_NAME);

                long after = safeCountPatternStrategy();
                long newRows = (before >= 0 && after >= 0) ? Math.max(0L, after - before) : -1L;

                if (newRows >= 0) {
                    long updatedRows = Math.max(0L, rep.totalRowsAffected - newRows);
                    synchronized (this) {
                        totalPatternNewEstimated += newRows;
                        totalPatternUpdatedEstimated += updatedRows;
                    }
                }

                synchronized (this) {
                    totalPatternStrategyUpsertsPayloads += payload.size(); // <- incrément uniquement ici
                }

                LOGGER.log(String.format(
                        "[PATTERN_STRATEGY UPSERT][DONE] items=%d | rows_affected=%d | new≈%s, upd≈%s | batches=%d | autoCommit=%s",
                        payload.size(), rep.totalRowsAffected,
                        (newRows >= 0 ? String.valueOf(newRows) : "?"),
                        (newRows >= 0 ? String.valueOf(Math.max(0L, rep.totalRowsAffected - newRows)) : "?"),
                        rep.batchCount, String.valueOf(rep.autoCommit)));
            } catch (Exception e) {
                logAsyncError(e);
            } finally {
                payload.clear();
            }
        }));
    }

    /** Routeur trades : pipeline ou batch SQL. */
    private void submitTradesBatch(ExecutorService exec, Deque<Future<?>> outstanding,
            List<TradeStrategyHistoryFull> payload) {
        if (currentUsePipeline) {
            submitTradesBatchPipeline(exec, outstanding, payload);
        } else {
            submitTradesBatchSql(exec, outstanding, payload);
        }
    }

    /** Mode batch SQL (DAO historique). */
    private void submitTradesBatchSql(ExecutorService exec, Deque<Future<?>> outstanding,
            List<TradeStrategyHistoryFull> payload) {
        waitIfTooManyOutstanding(outstanding);
        outstanding.addLast(exec.submit(() -> {
            try {
                LOGGER.log("[TRADE UPSERT/BATCH][SEND] items=" + payload.size() + " (Thread="
                        + Thread.currentThread().getName() + ")");
                long before = safeCountTrades();

                TradeDaoBatchReport rep = tradeHistoryBatchDao.insertFullBatchWithReport(
                        payload, BATCH_SQL_SIZE, LOGGER, STUDY_ID, INSTANCE_NAME);

                long after = safeCountTrades();
                long newRows = (before >= 0 && after >= 0) ? Math.max(0L, after - before) : -1L;

                if (newRows >= 0) {
                    long updatedRows = Math.max(0L, rep.totalRowsAffected - newRows);
                    synchronized (this) {
                        totalTradeNewEstimated += newRows;
                        totalTradeUpdatedEstimated += updatedRows;
                    }
                }

                synchronized (this) {
                    totalTradeUpsertsPayloads += payload.size(); // <- incrément uniquement ici
                }

                LOGGER.log(String.format(
                        "[TRADE UPSERT/BATCH][DONE] items=%d | rows_affected=%d | new≈%s, upd≈%s | batches=%d | autoCommit=%s",
                        payload.size(), rep.totalRowsAffected,
                        (newRows >= 0 ? String.valueOf(newRows) : "?"),
                        (newRows >= 0 ? String.valueOf(Math.max(0L, rep.totalRowsAffected - newRows)) : "?"),
                        rep.batchCount, String.valueOf(rep.autoCommit)));
            } catch (Exception e) {
                logAsyncError(e);
            } finally {
                payload.clear();
            }
        }));
    }

    /** Mode pipeline COPY/MERGE (DAO pipeline). */
    private void submitTradesBatchPipeline(ExecutorService exec, Deque<Future<?>> outstanding,
            List<TradeStrategyHistoryFull> payload) {
        waitIfTooManyOutstanding(outstanding);
        outstanding.addLast(exec.submit(() -> {
            try {
                LOGGER.log("[PIPELINE-TRADE][SEND] items=" + payload.size() + " (Thread="
                        + Thread.currentThread().getName() + ")");
                long before = safeCountTrades();

                TradeDaoBatchReport rep = tradeHistoryPipelineDao.loadBatchWithReport(
                        payload, LOGGER, STUDY_ID, INSTANCE_NAME);

                long after = safeCountTrades();
                long newRows = (before >= 0 && after >= 0) ? Math.max(0L, after - before) : -1L;

                if (newRows >= 0) {
                    long updatedRows = Math.max(0L, rep.totalRowsAffected - newRows);
                    synchronized (this) {
                        totalTradeNewEstimated += newRows;
                        totalTradeUpdatedEstimated += updatedRows;
                    }
                }

                synchronized (this) {
                    totalTradeUpsertsPayloads += payload.size(); // <- incrément uniquement ici
                }

                LOGGER.log(String.format(
                        "[PIPELINE-TRADE][DONE] items=%d | rows_affected=%d | new≈%s, upd≈%s | chunks=%d",
                        payload.size(), rep.totalRowsAffected,
                        (newRows >= 0 ? String.valueOf(newRows) : "?"),
                        (newRows >= 0 ? String.valueOf(Math.max(0L, rep.totalRowsAffected - newRows)) : "?"),
                        rep.batchCount));
            } catch (Exception e) {
                logAsyncError(e);
            } finally {
                payload.clear();
            }
        }));
    }

    // Synchronisation du parallélisme pour limiter la pression mémoire
    private void waitIfTooManyOutstanding(Deque<Future<?>> outstanding) {
        while (outstanding.size() >= MAX_OUTSTANDING_TASKS) {
            try {
                Future<?> f = outstanding.pollFirst();
                if (f != null)
                    f.get();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException ee) {
                logAsyncError(ee.getCause() != null ? ee.getCause() : ee);
            }
        }
    }

    private void waitAll(Deque<Future<?>> outstanding) {
        while (!outstanding.isEmpty()) {
            try {
                Future<?> f = outstanding.pollFirst();
                if (f != null)
                    f.get();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException ee) {
                logAsyncError(ee.getCause() != null ? ee.getCause() : ee);
            }
        }
    }

    // Logging d’erreurs dans les tâches async
    private void logAsyncError(Throwable t) {
        LOGGER.log("[ERROR][Async DB][" + Thread.currentThread().getName() + "] " + t.getMessage());
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        LOGGER.log(sw.toString());
    }

    /** Vérifie qu’une SUITE est valide (8 caractères 'R'/'G'). */
    private static boolean isValidPattern(String suite) {
        return suite != null && suite.length() == PATTERN_LENGTH && suite.matches("[RG]+");
    }
}
