package backtest;

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.nio.file.*;

import backtest.database.BacktestDbManager;
import backtest.model.*;
import backtest.util.*;

public class BacktestPatternRenko {

    public static final List<int[]> LIST_BRACKET_POINT = Arrays.asList(new int[] { 30, 15 }, new int[] { 15, 15 },
            new int[] { 30, 30 }, new int[] { 20, 20 });
    public static final List<Integer> LIST_INTERVAL_VALUE = Arrays.asList(1, 5, 10, 25);
    public static final List<Double> LIST_RENKO_SIZE = Arrays.asList(5.0);
    public static final int PATTERN_LENGTH = 8;
    public static final int SELECTED_TOP_GROUP = 12;
    public static final double PF_MAX = 20.0;

    public static List<PatternResult> getTopPatternsByGroup(List<PatternResult> results, int maxGroups) {
        List<GroupStat<PatternResult>> groupStats = GroupingUtil.groupAndStat(results,
                r -> new GroupKey(r.intervalValue, r.renkoSize, r.tp, r.sl), r -> r.profitFactor, r -> r.delta,
                r -> r.score);
        List<PatternResult> topPatterns = new ArrayList<>();
        int nbGroups = Math.min(maxGroups, groupStats.size());
        for (int i = 0; i < nbGroups; i++) {
            topPatterns.addAll(groupStats.get(i).items);
        }
        System.out.println("[DEBUG] Top patterns by group sélectionnés: " + topPatterns.size());
        return topPatterns;
    }

    public static List<PatternResult> analyzePatterns(int PATTERN_LENGTH) throws Exception {
        List<PatternResult> results = new ArrayList<>();
        Map<String, List<RenkoRecord>> groupedRecords = BacktestDbManager.getGroupedRenkoRecords(LIST_INTERVAL_VALUE,
                LIST_RENKO_SIZE);

        System.out.println("[DEBUG] Nb groupes de RenkoRecords trouvés: " + groupedRecords.size());
        if (groupedRecords.isEmpty()) {
            System.out.println("[DEBUG] Aucun groupe RenkoRecord trouvé pour les intervalles / renko sizes: "
                    + LIST_INTERVAL_VALUE + " / " + LIST_RENKO_SIZE);
        }

        for (Map.Entry<String, List<RenkoRecord>> groupEntry : groupedRecords.entrySet()) {
            List<RenkoRecord> renkos = groupEntry.getValue();
            if (renkos.size() < PATTERN_LENGTH + 1) {
                System.out.println(
                        "[DEBUG] Groupe ignoré (trop petit): " + groupEntry.getKey() + " (" + renkos.size() + ")");
                continue;
            }
            Map<PatternKey, List<Integer>> patternIndices = new HashMap<>();
            String instrumentKey = renkos.get(0).instrumentKey;
            String intervalScale = renkos.get(0).intervalScale;
            int intervalValue = renkos.get(0).intervalValue;
            double renkoSize = renkos.get(0).renkoSize;

            // Construction des patterns
            for (int i = 0; i < renkos.size() - PATTERN_LENGTH; i++) {
                StringBuilder sbRenko = new StringBuilder();
                StringBuilder sbHilo1 = new StringBuilder();
                StringBuilder sbHilo2 = new StringBuilder();
                for (int j = 0; j < PATTERN_LENGTH; j++) {
                    sbRenko.append(renkos.get(i + j).renkoColor).append(" → ");
                    sbHilo1.append(renkos.get(i + j).hiloColor1).append(" → ");
                    sbHilo2.append(renkos.get(i + j).hiloColor2).append(" → ");
                }
                String patternRenko = sbRenko.substring(0, sbRenko.length() - 3);
                String patternHilo1 = sbHilo1.substring(0, sbHilo1.length() - 3);
                String patternHilo2 = sbHilo2.substring(0, sbHilo2.length() - 3);
                PatternKey key = new PatternKey(instrumentKey, intervalScale, intervalValue, renkoSize, patternRenko,
                        patternHilo1, patternHilo2);
                patternIndices.computeIfAbsent(key, _ -> new ArrayList<>()).add(i);
            }

            System.out.println("[DEBUG] Patterns trouvés pour " + groupEntry.getKey() + " : " + patternIndices.size());

            for (Map.Entry<PatternKey, List<Integer>> entry : patternIndices.entrySet()) {
                PatternKey key = entry.getKey();
                List<Integer> idxList = entry.getValue();
                // Premier filtre count <= 7
                if (idxList.size() <= 7) {
                    // Trop peu d'occurrences
                    // System.out.println("[DEBUG] Pattern ignoré car trop peu d'occurrences: " +
                    // key.patternRenko);
                    continue;
                }
                String lastDateTimeEntry = renkos.get(idxList.get(idxList.size() - 1) + PATTERN_LENGTH - 1).dateTime;
                List<int[]> backtests = LIST_BRACKET_POINT;
                for (String direction : new String[] { "long", "short" }) {
                    int[] tps = new int[backtests.size()], sls = new int[backtests.size()];
                    for (int idx : idxList) {
                        int entryIdx = idx + PATTERN_LENGTH - 1;
                        if (entryIdx + 1 >= renkos.size())
                            continue;
                        double entryPrice = renkos.get(entryIdx).priceRes;
                        List<Double> windowPrices = new ArrayList<>();
                        List<String> windowDates = new ArrayList<>();
                        for (int k = entryIdx + 1; k < renkos.size(); k++) {
                            windowPrices.add(renkos.get(k).priceRes);
                            windowDates.add(renkos.get(k).dateTime);
                        }
                        for (int b = 0; b < backtests.size(); b++) {
                            int tp = backtests.get(b)[0], sl = backtests.get(b)[1];
                            String[] res = BacktestUtil.backtestPatternTrade(direction, entryPrice, windowPrices,
                                    windowDates, tp, sl, PF_MAX);
                            if (res[0].equals("TP"))
                                tps[b]++;
                            else if (res[0].equals("SL"))
                                sls[b]++;
                        }
                    }
                    for (int b = 0; b < backtests.size(); b++) {
                        int tp = backtests.get(b)[0], sl = backtests.get(b)[1];
                        int count_tp = tps[b];
                        int count_sl = sls[b];
                        int delta = count_tp - count_sl;
                        String winrate = (count_tp + count_sl) > 0
                                ? (int) Math.round(100.0 * count_tp / (count_tp + count_sl)) + "%"
                                : "N/A";
                        double totalGain = count_tp * tp;
                        double totalLoss = count_sl * sl;
                        double profitFactor = (count_sl != 0) ? Math.min(PF_MAX, totalGain / Math.abs(totalLoss))
                                : (count_tp > 0 ? PF_MAX : 0.00);
                        double score = profitFactor * Math.log(delta + 1);
                        double scoreRounded = MathUtil.round(score, 2);
                        PatternResult pr = new PatternResult(key.instrumentKey, key.intervalScale, key.intervalValue,
                                key.renkoSize, key.patternRenko, key.patternHilo1, key.patternHilo2, direction,
                                idxList.size(), lastDateTimeEntry, tp, sl, count_tp, count_sl, delta, winrate,
                                profitFactor, scoreRounded);

                        // Log tous les patterns trouvés (pour debug)
                        // System.out.println("[DEBUG] Pattern trouvé: " + key.patternRenko + " | Dir="
                        // + direction
                        // + " | N=" + idxList.size() + " | PF=" + profitFactor + " | delta=" + delta +
                        // " | TP="
                        // + count_tp + " | SL=" + count_sl);

                        results.add(pr);
                    }
                }
            }
        }
        int before = results.size();
        results.removeIf(r -> r.profitFactor < 5.0);
        System.out.println("[DEBUG] Après filtre profitFactor < 5.0 : " + results.size() + "/" + before);

        // int before2 = results.size();
        // results.removeIf(r -> r.delta > 5);
        // System.out.println("[DEBUG] Après filtre delta > 5 : " + results.size() + "/"
        // + before2);

        int before3 = results.size();
        results.removeIf(r -> r.countSl > 2);
        System.out.println("[DEBUG] Après filtre countSl > 2 : " + results.size() + "/" + before3);

        if (results.isEmpty()) {
            System.out.println("⚠️ Aucun pattern final détecté après tous les filtres !");
        }
        return results;
    }

    public static List<TradeDetail> collectTradeDetails(List<PatternResult> patterns, int PATTERN_LENGTH)
            throws Exception {
        List<TradeDetail> allDetails = new ArrayList<>();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        for (PatternResult pattern : patterns) {
            String[] patSplit = pattern.patternRenko.split(" → ");
            String[] hilo1Split = pattern.patternHilo1.split(" → ");
            String[] hilo2Split = pattern.patternHilo2.split(" → ");
            List<RenkoRecord> renkos = BacktestDbManager.getRenkoRecordsForPattern(pattern.instrumentKey,
                    pattern.intervalScale, pattern.intervalValue, pattern.renkoSize);
            if (renkos.size() < PATTERN_LENGTH + 1) {
                System.out.println("[DEBUG] Pas assez de renkos pour détail : " + pattern.instrumentKey + " / "
                        + pattern.intervalValue + " / " + pattern.renkoSize);
                continue;
            }
            for (int idx = 0; idx < renkos.size() - PATTERN_LENGTH; idx++) {
                boolean match = true;
                for (int j = 0; j < PATTERN_LENGTH; j++) {
                    if (!renkos.get(idx + j).renkoColor.equals(patSplit[j])
                            || !renkos.get(idx + j).hiloColor1.equals(hilo1Split[j])
                            || !renkos.get(idx + j).hiloColor2.equals(hilo2Split[j])) {
                        match = false;
                        break;
                    }
                }
                if (!match)
                    continue;
                int entryIdx = idx + PATTERN_LENGTH - 1;
                if (entryIdx + 1 >= renkos.size())
                    continue;
                double entryPrice = renkos.get(entryIdx).priceRes;
                String entryDate = renkos.get(entryIdx).dateTime;
                long entryTimestamp = renkos.get(entryIdx).timestamp;
                List<Double> windowPrices = new ArrayList<>();
                List<String> windowDates = new ArrayList<>();
                for (int k = entryIdx + 1; k < renkos.size(); k++) {
                    windowPrices.add(renkos.get(k).priceRes);
                    windowDates.add(renkos.get(k).dateTime);
                }
                String[] tradeRes = BacktestUtil.backtestPatternTrade(pattern.direction, entryPrice, windowPrices,
                        windowDates, pattern.tp, pattern.sl, PF_MAX);
                // String intervalValue = String.valueOf(pattern.intervalValue);
                String datePart = entryDate.split(" ")[0];
                LocalDate day = LocalDate.parse(datePart, dateFmt);
                DayOfWeek dayOfWeek = day.getDayOfWeek();
                String label = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH);

                TradeDetail detail = new TradeDetail();
                detail.day = day;
                detail.dayWithLabel = datePart + " (" + label + ")";
                detail.instrument = pattern.instrumentKey;
                // detail.interval = intervalValue;
                detail.intervalValue = pattern.intervalValue;
                detail.renkoSize = pattern.renkoSize;
                detail.patternRenko = pattern.patternRenko;
                detail.patternHilo1 = pattern.patternHilo1;
                detail.patternHilo2 = pattern.patternHilo2;
                detail.direction = pattern.direction;
                detail.entryDateTime = entryDate;
                detail.entryTimestamp = String.valueOf(entryTimestamp);
                detail.entryPrice = String.valueOf(entryPrice);
                detail.exitDateTime = tradeRes[3];
                detail.exitPrice = tradeRes[2];
                detail.barsToExit = tradeRes[1];
                detail.result = tradeRes[0];
                detail.pattern = pattern;
                detail.tp = pattern.tp;
                detail.sl = pattern.sl;
                allDetails.add(detail);
            }
        }
        System.out.println("[DEBUG] Nb de TradeDetails collectés : " + allDetails.size());
        return allDetails;
    }

    public static void exportGlobalToDb(List<PatternResult> results) throws Exception {
        List<GroupStat<PatternResult>> groupStats = GroupingUtil.groupAndStat(results,
                r -> new GroupKey(r.intervalValue, r.renkoSize, r.tp, r.sl), r -> r.profitFactor, r -> r.delta, r -> r.score);
        for (GroupStat<PatternResult> group : groupStats) {
            group.items.sort((r1, r2) -> {
                int cmp = Double.compare(r2.score, r1.score);
                if (cmp != 0)
                    return cmp;
                return Integer.compare(r2.delta, r1.delta);
            });
        }
        String nowIso = BacktestUtil.isoNow();
        List<PatternResult> flatResults = new ArrayList<>();
        for (GroupStat<PatternResult> group : groupStats) {
            flatResults.addAll(group.items);
        }
        System.out.println("[DEBUG] Insertion en base de " + flatResults.size() + " patterns...");
        BacktestDbManager.insertPatternResults(flatResults, nowIso);
    }

    public static void main(String[] args) throws Exception {
        String datetime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String outputDir = "./src/output";
        String outputDayDir = outputDir + "/" + datetime;
        new File(outputDayDir).mkdirs();
        String csvPath = outputDayDir + "/result_backtest_pattern_renko_8brick_" + datetime + ".csv";

        List<PatternResult> results = analyzePatterns(PATTERN_LENGTH);

        // ------------ Export CSV principal
        List<GroupStat<PatternResult>> groupStats = GroupingUtil.groupAndStat(results,
                r -> new GroupKey(r.intervalValue, r.renkoSize, r.tp, r.sl), r -> r.profitFactor, r -> r.delta, r -> r.score);
        CsvExportUtil.exportGlobalCSV(results, csvPath, groupStats);
        System.out.println("[DEBUG] CSV créé : " + csvPath);

        // ------------ Export BDD
        exportGlobalToDb(results);
        System.out.println("[DEBUG] BDD PostgreSQL peuplée");

        String detailsByPatternDir = outputDayDir + "/details_by_pattern";
        new File(detailsByPatternDir).mkdirs();

        // ---- TOP patterns des premiers groupes
        List<PatternResult> topPatterns = getTopPatternsByGroup(results, SELECTED_TOP_GROUP);
        List<TradeDetail> topTrades = collectTradeDetails(topPatterns, PATTERN_LENGTH);

        // ------------ Export fichiers détails individuels (top)
        for (int i = 0; i < topPatterns.size(); i++) {
            PatternResult pattern = topPatterns.get(i);
            String dt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String detailCsvPath = detailsByPatternDir + "/details_trades_top" + (i + 1) + "_" + dt + ".csv";
            CsvExportUtil.exportPatternDetailsCsv(topTrades, pattern, detailCsvPath);
        }

        // ========== Génération des CorePattern par groupe ==========
        Map<Integer, GroupKey> groupByIndex = new TreeMap<>();
        int indexTop = 1;
        for (int i = 0; i < topPatterns.size(); i++) {
            PatternResult p = topPatterns.get(i);
            GroupKey gk = new GroupKey(p.intervalValue, p.renkoSize, p.tp, p.sl);
            if (!groupByIndex.containsValue(gk)) {
                groupByIndex.put(indexTop, gk);
                indexTop++;
            }
        }

        // ------------ Export global par jour (top patterns, par groupe avec séparateur
        // weekend/mois)
        String detailsByDayDir = outputDayDir + "/details_by_day";
        new File(detailsByDayDir).mkdirs();
        CsvExportUtil.exportGlobalTradeDetailsByDay(topTrades, SELECTED_TOP_GROUP, detailsByDayDir, groupByIndex);

        // ---------- Archive
        Path dossierPath = Paths.get(outputDayDir);
        String archiveDir = outputDir + "/archive";
        new File(archiveDir).mkdirs();
        Path archivePath = Paths.get(archiveDir + "/" + datetime + ".tar.gz");
        BacktestUtil.zipFolder(dossierPath, archivePath);
        System.out.println("[DEBUG] Archive créée : " + archivePath);

        // Optional: fermer le pool à la fin (utile en mode CLI)
        BacktestDbManager.closePool();
    }
}
