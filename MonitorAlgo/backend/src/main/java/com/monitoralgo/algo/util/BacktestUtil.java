package com.monitoralgo.algo.util;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.monitoralgo.stats.model.SnapshotAioByRenko;

public class BacktestUtil {

    // Parse le profit factor
    public static double parseProfitFactor(String pf) {
        if (pf == null)
            return Double.NaN;
        if (pf.equals("∞"))
            return 100.0;
        if (pf.equals("N/A"))
            return Double.NaN;
        try {
            return Double.parseDouble(pf.replace(" ⚠️", "").replace(",", "."));
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public static String normalizePattern(String pattern) {
        if (pattern == null || pattern.isBlank())
            return "";
        // Split sur tous les séparateurs possibles : flèches, virgule, point-virgule,
        // espace, tab
        String[] tokens = pattern.split("\\s*(→|->|,|;|\\s)\\s*");
        StringBuilder sb = new StringBuilder(tokens.length);
        for (String token : tokens) {
            String t = token.trim().toLowerCase();
            if (t.isEmpty())
                continue;
            if (t.startsWith("g"))
                sb.append('G');
            else if (t.startsWith("r"))
                sb.append('R');
            else if (t.startsWith("v")) // pour "vert"
                sb.append('G');
            else if (t.startsWith("b")) // pour "bleu", si besoin plus tard
                sb.append('B');
            else
                sb.append('?');
        }
        return sb.toString();
    }

    public static String patternFromSnapshots(List<SnapshotAioByRenko> snaps,
            java.util.function.Function<SnapshotAioByRenko, String> colorGetter) {
        StringBuilder sb = new StringBuilder(snaps.size());
        for (SnapshotAioByRenko snap : snaps) {
            String c = colorGetter.apply(snap);
            if (c == null)
                c = "";
            c = c.trim().toLowerCase();
            if (c.startsWith("g") || c.startsWith("v"))
                sb.append('G');
            else if (c.startsWith("r"))
                sb.append('R');
            else if (c.startsWith("b"))
                sb.append('B');
            else
                sb.append('?');
        }
        return sb.toString();
    }

    // Format ISO yyyy-MM-dd HH:mm:ss pour updated_at
    public static String isoNow() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    // Archive un dossier (folderPath) dans un fichier ZIP (zipFilePath)
    public static void zipFolder(Path folderPath, Path zipFilePath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath.toFile()))) {
            Path basePath = folderPath.getParent();
            Files.walk(folderPath).forEach(path -> {
                try {
                    String entryName = basePath.relativize(path).toString();
                    if (Files.isDirectory(path)) {
                        if (!entryName.endsWith("/"))
                            entryName += "/";
                        zos.putNextEntry(new ZipEntry(entryName));
                        zos.closeEntry();
                    } else {
                        zos.putNextEntry(new ZipEntry(entryName));
                        Files.copy(path, zos);
                        zos.closeEntry();
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    // Backtest individuel (logique inchangée, PF_MAX en paramètre pour
    // factorisation)
    public static String[] backtestPatternTrade(String direction, double entryPrice,
            java.util.List<Double> windowPrices, java.util.List<String> windowDates, int tp, int sl, double PF_MAX) {
        String result = "NoExit";
        String exitDate = "";
        double exitPrice = 0.0;
        int bars = 0;
        if ("long".equals(direction)) {
            for (int p = 0; p < windowPrices.size(); p++) {
                double price = windowPrices.get(p);
                if (price - entryPrice >= tp) {
                    result = "TP";
                    exitPrice = price;
                    exitDate = windowDates.get(p);
                    bars = p + 1;
                    break;
                } else if (price - entryPrice <= -sl) {
                    result = "SL";
                    exitPrice = price;
                    exitDate = windowDates.get(p);
                    bars = p + 1;
                    break;
                }
            }
        } else { // short
            for (int p = 0; p < windowPrices.size(); p++) {
                double price = windowPrices.get(p);
                if (price - entryPrice <= -tp) {
                    result = "TP";
                    exitPrice = price;
                    exitDate = windowDates.get(p);
                    bars = p + 1;
                    break;
                } else if (price - entryPrice >= sl) {
                    result = "SL";
                    exitPrice = price;
                    exitDate = windowDates.get(p);
                    bars = p + 1;
                    break;
                }
            }
        }
        return new String[] { result, String.valueOf(bars), String.valueOf(exitPrice), exitDate };
    }

    public static long toEpochMilli(LocalDateTime ldt) {
        return ldt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
