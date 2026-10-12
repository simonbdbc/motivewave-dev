package com.monitoralgo.algo.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.monitoralgo.algo.model.BacktestResult;
import com.monitoralgo.stats.model.SnapshotAioByRenko;

public class BacktestEngine {

    private static final DateTimeFormatter ENTRY_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static String formatFromEpochMillis(Long ts) {
        if (ts == null)
            return "";
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault()).format(ENTRY_FMT);
    }

    /**
     * @param direction      "LONG" ou "SHORT"
     * @param timestampEntry epoch millis d'entrée
     * @param entryPrice     prix d'entrée
     * @param futureData     données futures (ordre chronologique)
     * @param tpPoint        distance (en point) du TP par rapport à l'entrée
     * @param slPoint        distance (en point) du SL par rapport à l'entrée
     */
    public static BacktestResult backtestPatternTrade(
            String direction,
            Long timestampEntry,
            double entryPrice,
            List<SnapshotAioByRenko> futureData,
            double tpPoint,
            double slPoint) {
        BacktestResult result = new BacktestResult(timestampEntry, formatFromEpochMillis(timestampEntry), entryPrice);

        if (futureData == null || futureData.isEmpty() || direction == null) {
            return result; // "NoExit" par défaut
        }

        final boolean isLong = "LONG".equalsIgnoreCase(direction);

        // Calcul des niveaux de prix TP/SL (et non les distances)
        if (isLong) {
            result.setTpPrice(entryPrice + tpPoint);
            result.setSlPrice(entryPrice - slPoint);
        } else {
            result.setTpPrice(entryPrice - tpPoint);
            result.setSlPrice(entryPrice + slPoint);
        }

        for (int i = 0; i < futureData.size(); i++) {
            SnapshotAioByRenko snap = futureData.get(i);
            double price = snap.getPriceRes();

            if (isLong) {
                if (price >= result.getTpPrice()) {
                    result.setResult("TP");
                    result.setTimestampExit(snap.getTimestamp());
                    result.setExitPrice(price);
                    result.setExitDate(snap.getDateTime());
                    result.setBars(i + 1);
                    break;
                } else if (price <= result.getSlPrice()) {
                    result.setResult("SL");
                    result.setTimestampExit(snap.getTimestamp());
                    result.setExitPrice(price);
                    result.setExitDate(snap.getDateTime());
                    result.setBars(i + 1);
                    break;
                }
            } else { // SHORT
                if (price <= result.getTpPrice()) {
                    result.setResult("TP");
                    result.setTimestampExit(snap.getTimestamp());
                    result.setExitPrice(price);
                    result.setExitDate(snap.getDateTime());
                    result.setBars(i + 1);
                    break;
                } else if (price >= result.getSlPrice()) {
                    result.setResult("SL");
                    result.setTimestampExit(snap.getTimestamp());
                    result.setExitPrice(price);
                    result.setExitDate(snap.getDateTime());
                    result.setBars(i + 1);
                    break;
                }
            }
        }

        return result; // "NoExit" si aucune sortie n’est atteinte
    }
}
