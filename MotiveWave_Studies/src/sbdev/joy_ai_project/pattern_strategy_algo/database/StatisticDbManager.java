package sbdev.joy_ai_project.pattern_strategy_algo.database;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import sbdev.joy_ai_project.pattern_strategy_algo.model.RenkoSnapshot;
import sbdev.joy_ai_project.pattern_strategy_algo.util.Helper;

public class StatisticDbManager {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public void insertRenkoSnapshotsBatch(List<RenkoSnapshot> batch) throws SQLException {
        if (batch == null || batch.isEmpty())
            return;

        String insertSql = """
                INSERT INTO snapshot_aio_by_renko (
                    instrument_key, instrument_name, tf_scale, tf_interval, renko_size, timestamp, date_time,
                    price_res, price_sup, renko_color,
                    hilo_color_1, hilo_color_2,
                    hilo_price_1, hilo_price_2, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (instrument_key, tf_scale, tf_interval, renko_size, timestamp)
                DO UPDATE SET
                    instrument_name=EXCLUDED.instrument_name,
                    date_time=EXCLUDED.date_time,
                    price_res=EXCLUDED.price_res,
                    price_sup=EXCLUDED.price_sup,
                    renko_color=EXCLUDED.renko_color,
                    hilo_color_1=EXCLUDED.hilo_color_1,
                    hilo_color_2=EXCLUDED.hilo_color_2,
                    hilo_price_1=EXCLUDED.hilo_price_1,
                    hilo_price_2=EXCLUDED.hilo_price_2,
                    updated_at=EXCLUDED.updated_at
                """;

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(insertSql)) {

            conn.setAutoCommit(false);

            for (RenkoSnapshot s : batch) {
                ps.setString(1, s.instrumentKey);
                ps.setString(2, s.instrumentName);
                ps.setString(3, s.intervalScale);
                ps.setInt(4, s.intervalValue);
                ps.setDouble(5, s.renkoSize);
                ps.setLong(6, s.timestamp);
                ps.setString(7, s.dateTime);
                ps.setDouble(8, s.priceRes);
                ps.setDouble(9, s.priceSup);
                ps.setString(10, s.renkoColor);
                ps.setString(11, s.hiloColor1);
                ps.setString(12, s.hiloColor2);
                ps.setDouble(13, s.hiloPrice1);
                ps.setDouble(14, s.hiloPrice2);
                ps.setString(15, s.updatedAt);

                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        }
    }

    public void insertRenkoSnapshotAllInOne(String instrument_key, String instrument_name, String interval_scale,
            int interval_value, double renko_size, long timestamp, String date_time, double price_res, double price_sup,
            String renko_color, String hilo_color1, String hilo_color2, double hilo_price1, double hilo_price2,
            String updated_at) throws SQLException {

        String insertSql = """
                    INSERT INTO snapshot_aio_by_renko (
                        instrument_key, instrument_name, tf_scale, tf_interval, renko_size, timestamp, date_time,
                        price_res, price_sup, renko_color,
                        hilo_color_1, hilo_color_2,
                        hilo_price_1, hilo_price_2, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (instrument_key, tf_scale, tf_interval, renko_size, timestamp)
                    DO UPDATE SET
                        instrument_name=EXCLUDED.instrument_name,
                        date_time=EXCLUDED.date_time,
                        price_res=EXCLUDED.price_res,
                        price_sup=EXCLUDED.price_sup,
                        renko_color=EXCLUDED.renko_color,
                        hilo_color_1=EXCLUDED.hilo_color_1,
                        hilo_color_2=EXCLUDED.hilo_color_2,
                        hilo_price_1=EXCLUDED.hilo_price_1,
                        hilo_price_2=EXCLUDED.hilo_price_2,
                        updated_at=EXCLUDED.updated_at
                """;

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, instrument_key);
            insertStmt.setString(2, instrument_name);
            insertStmt.setString(3, interval_scale);
            insertStmt.setInt(4, interval_value);
            insertStmt.setDouble(5, renko_size);
            insertStmt.setLong(6, timestamp);
            insertStmt.setString(7, date_time);
            insertStmt.setDouble(8, price_res);
            insertStmt.setDouble(9, price_sup);
            insertStmt.setString(10, renko_color);
            insertStmt.setString(11, hilo_color1);
            insertStmt.setString(12, hilo_color2);
            insertStmt.setDouble(13, hilo_price1);
            insertStmt.setDouble(14, hilo_price2);
            insertStmt.setString(15, updated_at);

            insertStmt.executeUpdate();
        }
    }

    public void saveRenkoSnapshot(String instrumentName, String intervalScale, int intervalValue, double renkoSize,
            long timestamp, double priceRes, double priceSup, String renkoColor, String hiloColor1, String hiloColor2,
            double hiloPrice1, double hiloPrice2) {

        String instrumentKey = Helper.extractInstrumentKeyGroup(instrumentName);
        String dateTime = dateFormat.format(new Date(timestamp));
        String updatedAt = dateFormat.format(new Date());

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            new StrategyDbManager().getOrInsertInstrument(conn, instrumentName);
            new StrategyDbManager().getOrInsertTimeframe(conn, intervalScale, intervalValue);

            insertRenkoSnapshotAllInOne(instrumentKey, instrumentName, intervalScale, intervalValue, renkoSize,
                    timestamp, dateTime, priceRes, priceSup, renkoColor, hiloColor1, hiloColor2, hiloPrice1, hiloPrice2,
                    updatedAt);

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
