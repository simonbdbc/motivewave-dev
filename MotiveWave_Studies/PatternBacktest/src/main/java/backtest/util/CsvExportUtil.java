package backtest.util;

import com.opencsv.CSVWriter;

import backtest.model.GroupKey;
import backtest.model.GroupStat;
import backtest.model.PatternResult;
import backtest.model.TradeDetail;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class CsvExportUtil {

    // Export CSV principal
    public static void exportGlobalCSV(List<PatternResult> results, String csvPath,
            List<GroupStat<PatternResult>> groupStats) throws Exception {
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvPath))) {
            writer.writeNext(new String[] { "instrument_key", "tf_scale", "tf_interval", "renko_size", "tp", "sl",
                    "pattern_renko", "pattern_hilo1", "pattern_hilo2", "direction", "count", "last_date_time_entry",
                    "count_tp", "count_sl", "delta", "winrate", "profit_factor", "score" });
            for (GroupStat<PatternResult> group : groupStats) {
                for (PatternResult r : group.items) {
                    writer.writeNext(new String[] { r.instrumentKey, r.intervalScale, String.valueOf(r.intervalValue),
                            String.valueOf(r.renkoSize), String.valueOf(r.tp), String.valueOf(r.sl), r.patternRenko,
                            r.patternHilo1, r.patternHilo2, r.direction, String.valueOf(r.count), r.lastDateTimeEntry,
                            String.valueOf(r.countTp), String.valueOf(r.countSl), String.valueOf(r.delta), r.winrate,
                            String.valueOf(r.profitFactor), String.format("%.2f", r.score) });
                }
            }
        }
    }

    // Export des détails individuels d'un pattern
    public static void exportPatternDetailsCsv(List<TradeDetail> trades, PatternResult pattern, String outFile)
            throws Exception {
        try (CSVWriter writer = new CSVWriter(new FileWriter(outFile))) {
            writer.writeNext(new String[] { "instrument", "interval", "renko_size", "pattern_renko", "pattern_hilo1",
                    "pattern_hilo2", "direction", "entry_date_time", "entry_timestamp", "entry_price", "exit_date_time",
                    "exit_price", "bars_to_exit", "result" });
            for (TradeDetail d : trades) {
                if (d.pattern == pattern) {
                    writer.writeNext(new String[] { d.instrument, String.valueOf(d.intervalValue),
                            String.valueOf(d.renkoSize), d.patternRenko, d.patternHilo1, d.patternHilo2, d.direction,
                            d.entryDateTime, d.entryTimestamp, d.entryPrice, d.exitDateTime, d.exitPrice, d.barsToExit,
                            d.result });
                }
            }
        }
    }

    // Export global trade details by group/day
    public static void exportGlobalTradeDetailsByDay(List<TradeDetail> allDetails, int maxGroups, String outputDayDir,
            Map<Integer, GroupKey> groupByIndex) throws Exception {
        // Construction des groupes
        Map<GroupKey, List<TradeDetail>> byGroup = new HashMap<>();
        for (TradeDetail d : allDetails) {
            GroupKey key = new GroupKey(d.intervalValue, d.renkoSize, d.tp, d.sl);
            byGroup.computeIfAbsent(key, _ -> new ArrayList<>()).add(d);
        }

        // Parcours selon l'ordre groupByIndex (TreeMap<Integer, GroupKey>)
        int numGroups = Math.min(maxGroups, groupByIndex.size());
        for (int idx = 1; idx <= numGroups; idx++) {
            GroupKey groupKey = groupByIndex.get(idx);
            if (groupKey == null)
                continue;
            List<TradeDetail> trades = new ArrayList<>(byGroup.getOrDefault(groupKey, List.of()));
            trades.sort(Comparator.comparing((TradeDetail t) -> t.day).thenComparing(t -> t.entryDateTime));
            if (trades.isEmpty())
                continue;

            LocalDate firstDay = trades.get(0).day;
            LocalDate lastDay = trades.get(trades.size() - 1).day;
            int renkoSizeFormat = (int) Math.round(groupKey.renkoSize * 100);
            String fname = String.format("%s/details_by_day_top_%d_interval_%d_renko_size_%d_tp_%d_sl_%d.csv", outputDayDir,
                    idx, groupKey.intervalValue, renkoSizeFormat, groupKey.tp, groupKey.sl);

            try (PrintWriter writer = new PrintWriter(new FileWriter(fname))) {
                String header = "day,instrument,tf_interval,renko_size,tp,sl,pattern_renko,pattern_hilo1,pattern_hilo2,direction,entry_date_time,entry_timestamp,entry_price,exit_date_time,exit_price,bars_to_exit,result";
                writer.println(header);
                int numCols = header.split(",", -1).length;
                String sep = String.join(",", Collections.nCopies(numCols - 1, "--------"));

                Map<LocalDate, List<TradeDetail>> detailsByDay = new LinkedHashMap<>();
                for (TradeDetail d : trades) {
                    detailsByDay.computeIfAbsent(d.day, _ -> new ArrayList<>()).add(d);
                }
                for (LocalDate day = firstDay; !day.isAfter(lastDay); day = day.plusDays(1)) {
                    LocalDate nextDay = day.plusDays(1);
                    String label = day.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
                    String dayWithLabel = day.toString() + " (" + label + ")";
                    if (day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        writer.println(dayWithLabel + "," + sep);
                    }
                    List<TradeDetail> tradesForDay = detailsByDay.getOrDefault(day, Collections.emptyList());
                    for (TradeDetail d : tradesForDay) {
                        writer.println(String.join(",",
                                Arrays.asList(d.dayWithLabel, d.instrument, String.valueOf(d.intervalValue),
                                        String.valueOf(d.renkoSize), String.valueOf(d.tp), String.valueOf(d.sl),
                                        d.patternRenko, d.patternHilo1, d.patternHilo2, d.direction, d.entryDateTime,
                                        d.entryTimestamp, d.entryPrice, d.exitDateTime, d.exitPrice, d.barsToExit,
                                        d.result)));
                    }
                    if ((nextDay.getMonthValue() != day.getMonthValue())) {
                        String mois = nextDay.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
                        writer.println(mois + "," + sep);
                    }
                }
            }
            System.out.println("✅ Export " + fname);
        }
    }
}
