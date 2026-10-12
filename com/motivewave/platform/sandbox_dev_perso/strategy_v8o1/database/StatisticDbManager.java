package sbdev.joy_ai_project.strategy_v8o1.database;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import sbdev.joy_ai_project.strategy_v8o3.config.Config;
import sbdev.joy_ai_project.strategy_v8o3.strategy_core.utils.Helper;

public class StatisticDbManager {
    // private static final String DB_PATH =
    // "/Users/simon/Projets/PlateformeDev/dev-motivewave/MotiveWave_Studies/src/sbdev/joy_ai_project/strategy_v8o3/database/data_sqlite/strategy_stats_v8.db";
    private static final String DB_PATH = Config.getInstance().get("DB_PATH");
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    // public int getOrInsertInstrument(Connection conn, String name) throws
    // SQLException {
    // String selectSql = "SELECT id FROM instrument WHERE name = ?";
    // try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
    // selectStmt.setString(1, name);
    // try (ResultSet rs = selectStmt.executeQuery()) {
    // if (rs.next())
    // return rs.getInt("id");
    // }
    // }

    // String instrumentKey = Helper.extractKeyGroup(name);
    // String insertSql = "INSERT INTO instrument(name, instrument_key) VALUES (?,
    // ?)";
    // try (PreparedStatement insertStmt = conn.prepareStatement(insertSql,
    // Statement.RETURN_GENERATED_KEYS)) {
    // insertStmt.setString(1, name);
    // insertStmt.setString(2, instrumentKey);
    // insertStmt.executeUpdate();
    // try (ResultSet rs = insertStmt.getGeneratedKeys()) {
    // if (rs.next())
    // return rs.getInt(1);
    // }
    // }
    // throw new SQLException("Instrument insert failed");
    // }

    // public int getOrInsertInterval(Connection conn, String scale, int interval)
    // throws SQLException {
    // String selectSql = "SELECT id FROM interval WHERE scale = ? AND interval =
    // ?";
    // String insertSql = "INSERT INTO interval(scale, interval) VALUES (?, ?)";
    // try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
    // selectStmt.setString(1, scale);
    // selectStmt.setInt(2, interval);
    // try (ResultSet rs = selectStmt.executeQuery()) {
    // if (rs.next())
    // return rs.getInt("id");
    // }
    // }
    // try (PreparedStatement insertStmt = conn.prepareStatement(insertSql,
    // Statement.RETURN_GENERATED_KEYS)) {
    // insertStmt.setString(1, scale);
    // insertStmt.setInt(2, interval);
    // insertStmt.executeUpdate();
    // try (ResultSet rs = insertStmt.getGeneratedKeys()) {
    // if (rs.next())
    // return rs.getInt(1);
    // }
    // }
    // throw new SQLException("Interval insert failed");
    // }

    // public void insertRenkoSnapshot(Connection conn, int instrumentId, int
    // intervalId, long timestamp, String date_time,
    // double priceRes, double priceSupp, String renkoColor, String hiloColor1,
    // String hiloColor2,
    // double hiloPrice1, double hiloPrice2) throws SQLException {
    // String insertSql = """
    // INSERT OR IGNORE INTO renko_snapshot (
    // instrument_id, interval_id, timestamp, date_time,
    // price_res, price_supp, renko_color,
    // hilo_color_1, hilo_color_2,
    // hilo_price_1, hilo_price_2
    // ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    // """;

    // try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
    // insertStmt.setInt(1, instrumentId);
    // insertStmt.setInt(2, intervalId);
    // insertStmt.setLong(3, timestamp);
    // insertStmt.setString(4, date_time);
    // insertStmt.setDouble(5, priceRes);
    // insertStmt.setDouble(6, priceSupp);
    // insertStmt.setString(7, renkoColor);
    // insertStmt.setString(8, hiloColor1);
    // insertStmt.setString(9, hiloColor2);
    // insertStmt.setDouble(10, hiloPrice1);
    // insertStmt.setDouble(11, hiloPrice2);

    // insertStmt.executeUpdate();
    // }
    // }

    public void insertRenkoSnapshotAllInOne(Connection conn, String instrumentKey, String instrumentName,
            String intervalScale, int intervalValue, long timestamp, String date_time, double priceRes,
            double priceSupp, String renkoColor, String hiloColor1, String hiloColor2, double hiloPrice1,
            double hiloPrice2, String updatedAt) throws SQLException {
        String insertSql = """
                    INSERT OR IGNORE INTO renko_snapshot_aio (
                        instrument_key, instrument_name, interval_scale, interval_value, timestamp, date_time,
                        price_res, price_supp, renko_color,
                        hilo_color_1, hilo_color_2,
                        hilo_price_1, hilo_price_2, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, instrumentKey);
            insertStmt.setString(2, instrumentName);
            insertStmt.setString(3, intervalScale);
            insertStmt.setInt(4, intervalValue);
            insertStmt.setLong(5, timestamp);
            insertStmt.setString(6, date_time);
            insertStmt.setDouble(7, priceRes);
            insertStmt.setDouble(8, priceSupp);
            insertStmt.setString(9, renkoColor);
            insertStmt.setString(10, hiloColor1);
            insertStmt.setString(11, hiloColor2);
            insertStmt.setDouble(12, hiloPrice1);
            insertStmt.setDouble(13, hiloPrice2);
            insertStmt.setString(14, updatedAt);

            insertStmt.executeUpdate();
        }
    }

    public void saveRenkoSnapshot(String instrumentName, String intervalScale, int intervalValue, long timestamp,
            double priceRes, double priceSupp, String renkoColor, String hiloColor1, String hiloColor2,
            double hiloPrice1, double hiloPrice2) {

        String instrumentKey = Helper.extractKeyGroup(instrumentName);
        String date_time = dateFormat.format(new Date(timestamp));
        String updatedAt = dateFormat.format(new Date());

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            // int instrumentId = getOrInsertInstrument(conn, instrumentName);
            // int intervalId = getOrInsertInterval(conn, intervalScale, intervalValue);

            new StrategyDbManager().getOrInsertInstrument(conn, instrumentName);
            new StrategyDbManager().getOrInsertInterval(conn, intervalScale, intervalValue);

            insertRenkoSnapshotAllInOne(conn, instrumentKey, instrumentName, intervalScale, intervalValue, timestamp,
                    date_time, priceRes, priceSupp, renkoColor, hiloColor1, hiloColor2, hiloPrice1, hiloPrice2,
                    updatedAt);

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
