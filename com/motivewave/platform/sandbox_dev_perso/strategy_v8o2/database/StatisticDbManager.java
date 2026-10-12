package sbdev.joy_ai_project.strategy_v8o3.database;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import sbdev.joy_ai_project.strategy_v8o3.config.GlobalConfig;
import sbdev.joy_ai_project.strategy_v8o3.util.Helper;

public class StatisticDbManager {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public void insertRenkoSnapshotAllInOne(String instrumentKey, String instrumentName,
            String intervalScale, int intervalValue, long timestamp, String date_time, double priceRes, double priceSup,
            String renkoColor, String hiloColor1, String hiloColor2, double hiloPrice1, double hiloPrice2,
            String updatedAt) throws SQLException {
        String insertSql = """
                    INSERT OR IGNORE INTO renko_snapshot_aio (
                        instrument_key, instrument_name, interval_scale, interval_value, timestamp, date_time,
                        price_res, price_supp, renko_color,
                        hilo_color_1, hilo_color_2,
                        hilo_price_1, hilo_price_2, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, instrumentKey);
            insertStmt.setString(2, instrumentName);
            insertStmt.setString(3, intervalScale);
            insertStmt.setInt(4, intervalValue);
            insertStmt.setLong(5, timestamp);
            insertStmt.setString(6, date_time);
            insertStmt.setDouble(7, priceRes);
            insertStmt.setDouble(8, priceSup);
            insertStmt.setString(9, renkoColor);
            insertStmt.setString(10, hiloColor1);
            insertStmt.setString(11, hiloColor2);
            insertStmt.setDouble(12, hiloPrice1);
            insertStmt.setDouble(13, hiloPrice2);
            insertStmt.setString(14, updatedAt);

            insertStmt.executeUpdate();
        }
    }

    public void saveRenkoSnapshot(String instrumentName, String intervalScale, int intervalValue,
            long timestamp, double priceRes, double priceSup, String renkoColor, String hiloColor1, String hiloColor2,
            double hiloPrice1, double hiloPrice2) {

        String instrumentKey = Helper.extractInstrumentKeyGroup(instrumentName);
        String date_time = dateFormat.format(new Date(timestamp));
        String updatedAt = dateFormat.format(new Date());

        Connection conn = DatabaseManager.getInstance().getConnection();

        try {
            conn.setAutoCommit(false);

            new StrategyDbManager().getOrInsertInstrument(conn, instrumentName);
            new StrategyDbManager().getOrInsertInterval(conn, intervalScale, intervalValue);

            insertRenkoSnapshotAllInOne(instrumentKey, instrumentName, intervalScale, intervalValue, timestamp,
                    date_time, priceRes, priceSup, renkoColor, hiloColor1, hiloColor2, hiloPrice1, hiloPrice2,
                    updatedAt);

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (Exception ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (Exception ignored) {
            }
        }
    }

    public void insertRenkoSnapshotAllInOneLive(String instrumentKey, String instrumentName,
            String intervalScale, int intervalValue, long timestamp, String date_time, double priceRes, double priceSup,
            String renkoColor, String hiloColor1, String hiloColor2, double hiloPrice1, double hiloPrice2,
            String updatedAt) throws SQLException {
        String insertSql = """
                    INSERT OR IGNORE INTO renko_snapshot_aio_live (
                        instrument_key, instrument_name, interval_scale, interval_value, timestamp, date_time,
                        price_res, price_supp, renko_color,
                        hilo_color_1, hilo_color_2,
                        hilo_price_1, hilo_price_2, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, instrumentKey);
            insertStmt.setString(2, instrumentName);
            insertStmt.setString(3, intervalScale);
            insertStmt.setInt(4, intervalValue);
            insertStmt.setLong(5, timestamp);
            insertStmt.setString(6, date_time);
            insertStmt.setDouble(7, priceRes);
            insertStmt.setDouble(8, priceSup);
            insertStmt.setString(9, renkoColor);
            insertStmt.setString(10, hiloColor1);
            insertStmt.setString(11, hiloColor2);
            insertStmt.setDouble(12, hiloPrice1);
            insertStmt.setDouble(13, hiloPrice2);
            insertStmt.setString(14, updatedAt);

            insertStmt.executeUpdate();
        }
    }

    public void saveRenkoSnapshotLive(String instrumentName, String intervalScale, int intervalValue,
            long timestamp, double priceRes, double priceSup, String renkoColor, String hiloColor1, String hiloColor2,
            double hiloPrice1, double hiloPrice2) {

        String instrumentKey = Helper.extractInstrumentKeyGroup(instrumentName);
        String date_time = dateFormat.format(new Date(timestamp));
        String updatedAt = dateFormat.format(new Date());

        Connection conn = DatabaseManager.getInstance().getConnection();

        try {
            conn.setAutoCommit(false);

            new StrategyDbManager().getOrInsertInstrument(conn, instrumentName);
            new StrategyDbManager().getOrInsertInterval(conn, intervalScale, intervalValue);

            insertRenkoSnapshotAllInOneLive(instrumentKey, instrumentName, intervalScale, intervalValue, timestamp,
                    date_time, priceRes, priceSup, renkoColor, hiloColor1, hiloColor2, hiloPrice1, hiloPrice2,
                    updatedAt);

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (Exception ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (Exception ignored) {
            }
        }
    }

}
