package sbdev.joy_ai_project.strategy_v8o3.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class HiLoDbManager {

    private final BlockingQueue<SaveEvent> queue = new LinkedBlockingQueue<>(10000);
    private final ConcurrentHashMap<String, Long> lastSavedPeriod = new ConcurrentHashMap<>();

    public HiLoDbManager() {
        Thread writer = new Thread(() -> {
            Connection conn = null;
            try {
                conn = DatabaseManager.getInstance().createNewConnection(); // Connexion dédiée pour ce thread
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
        writer.setName("HiLoDbManager-Writer");
        writer.setDaemon(true);
        writer.start();
    }

    /**
     * @param instrument       Instrument symbol
     * @param hiloType         "hilo1", "hilo2", etc.
     * @param value            valeur numérique
     * @param color            "green", "red", etc.
     * @param idx              index du bar
     * @param timestamp        timestamp long (epoch millis)
     * @param timeframeMinutes timeframe
     */
    public void saveHiloValue(String instrument, String hiloType, double value, String color, int idx, long timestamp,
            int timeframeMinutes) {
        // Arrondi timestamp à la minute/timeframe la plus proche
        long minutesInMillis = timeframeMinutes * 60 * 1000;
        long roundedMinTimestamp = (timestamp / minutesInMillis) * minutesInMillis;
        String key = instrument + "|" + hiloType + "|" + roundedMinTimestamp;
        // Vérifie si on a déjà enregistré pour cette période
        if (lastSavedPeriod.putIfAbsent(key, roundedMinTimestamp) != null) {
            // Déjà enregistré pour cette période, on skip
            return;
        }
        // Sinon, on sauvegarde
        String nowIso = isoNow();
        String date = isoDate(timestamp);
        if (!queue.offer(new SaveEvent(instrument, hiloType, value, color, timestamp, date, idx, nowIso))) {
            System.err.println("HiLoDbManager queue overflow! Sauvegarde ignorée.");
        }
    }

    private void saveToDb(SaveEvent event, Connection conn) {
        String sql = "INSERT OR IGNORE INTO hilo_history "
                + "(instrument, hilo_type, value, color, timestamp, date, idx, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, event.instrument);
            pst.setString(2, event.hiloType);
            pst.setDouble(3, event.value);
            pst.setString(4, event.color);
            pst.setLong(5, event.timestamp);
            pst.setString(6, event.date);
            pst.setInt(7, event.idx);
            pst.setString(8, event.updatedAt);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Limite à 10 par (instrument, hilo_type)
        String deleteSql = "DELETE FROM hilo_history WHERE id IN ("
                + "SELECT id FROM hilo_history WHERE instrument=? AND hilo_type=? "
                + "ORDER BY timestamp DESC LIMIT -1 OFFSET 10)";
        try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
            del.setString(1, event.instrument);
            del.setString(2, event.hiloType);
            del.executeUpdate();
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
        final String instrument, hiloType, color, date, updatedAt;
        final double value;
        final int idx;
        final long timestamp;

        SaveEvent(String instrument, String hiloType, double value, String color, long timestamp, String date, int idx,
                String updatedAt) {
            this.instrument = instrument;
            this.hiloType = hiloType;
            this.value = value;
            this.color = color;
            this.timestamp = timestamp;
            this.date = date;
            this.idx = idx;
            this.updatedAt = updatedAt;
        }
    }
}
