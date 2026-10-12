package sbdev.joy_ai_project.strategy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import sbdev.joy_ai_project.strategy.util.Helper;

public class HistoryHiLoDbManager {

    private final BlockingQueue<SaveEvent> queue = new LinkedBlockingQueue<>(10000);
    private final ConcurrentHashMap<String, Long> lastSavedPeriod = new ConcurrentHashMap<>();

    // Pour éviter de purger plus d'une fois par jour (partagé par tous les
    // threads/instances)
    private static volatile long lastPurgeDay = -1;

    public HistoryHiLoDbManager() {
        Thread writer = new Thread(() -> {
            Connection conn = null;
            try {
                try {
                    conn = DatabaseManager.getConnection();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        SaveEvent event = queue.take();
                        saveToDb(event, conn);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        });
        writer.setName("HiLoHistoryDbManager-Writer");
        writer.setDaemon(true);
        writer.start();
    }

    /**
     * @param instrumentName   Instrument symbol
     * @param hiloType         "hilo1", "hilo2", etc.
     * @param price            valeur numérique
     * @param color            "green", "red", etc.
     * @param timestamp        timestamp long (epoch millis)
     * @param timeframeMinutes timeframe
     */
    public void saveSnapshot(String instrumentName, String hiloType, double price, String color,
            long roundedMinTimestamp, long timestamp) {

        String instrumentKey = Helper.extractInstrumentKeyGroup(instrumentName);
        String key = instrumentKey + "|" + hiloType + "|" + roundedMinTimestamp;
        if (lastSavedPeriod.putIfAbsent(key, roundedMinTimestamp) != null) {
            return;
        }

        String nowIso = isoNow();
        String date = isoDate(timestamp);
        if (!queue.offer(new SaveEvent(instrumentKey, hiloType, price, color, timestamp, date, nowIso))) {
            System.err.println("HiLoHistoryDbManager queue overflow! Sauvegarde ignorée.");
        }
    }

    private void saveToDb(SaveEvent event, Connection conn) {
        // 1. Insert or do nothing if exists
        String sql = "INSERT INTO history_hilo "
                + "(instrument_key, hilo_type, price, color, timestamp, date, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (instrument_key, hilo_type, timestamp) DO NOTHING";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, event.instrumentKey);
            pst.setString(2, event.hiloType);
            pst.setDouble(3, event.price);
            pst.setString(4, Helper.normalizePattern(event.color));
            pst.setLong(5, event.timestamp);
            pst.setString(6, event.date);
            pst.setString(7, event.updated_at);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Purge une seule fois par jour (globale)
        long today = System.currentTimeMillis() / (24L * 60 * 60 * 1000);
        if (lastPurgeDay != today) {
            purgeOldEntries(conn);
            lastPurgeDay = today;
        }
    }

    // Purge globale des entrées de plus de 2 jours
    private static void purgeOldEntries(Connection conn) {
        long cutoff = System.currentTimeMillis() - 2L * 24 * 60 * 60 * 1000;
        String deleteOldSql = "DELETE FROM history_hilo WHERE timestamp < ?";
        try (PreparedStatement delOld = conn.prepareStatement(deleteOldSql)) {
            delOld.setLong(1, cutoff);
            int n = delOld.executeUpdate();
            if (n > 0) {
                System.out.println("HiLoHistoryDbManager : Purge de " + n + " valeurs history_hilo (plus de 2 jours)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode statique pour vider la table et réinitialiser l'auto-increment
    public static void resetHistoryTable() {
        try (Connection conn = DatabaseManager.getConnection(); Statement st = conn.createStatement()) {
            st.execute("TRUNCATE TABLE history_hilo RESTART IDENTITY");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Format ISO yyyy-MM-dd HH:mm:ss pour updated_at
    private static String isoNow() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    // Format ISO yyyy-MM-dd HH:mm:ss à partir d'un timestamp epoch millis
    private static String isoDate(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
    }

    // POJO pour porter l'évènement à sauver
    private static class SaveEvent {
        final String instrumentKey, hiloType, color, date, updated_at;
        final double price;
        final long timestamp;

        SaveEvent(String instrumentKey, String hiloType, double price, String color, long timestamp, String date,
                String updated_at) {
            this.instrumentKey = instrumentKey;
            this.hiloType = hiloType;
            this.price = price;
            this.color = color;
            this.timestamp = timestamp;
            this.date = date;
            this.updated_at = updated_at;
        }
    }
}
