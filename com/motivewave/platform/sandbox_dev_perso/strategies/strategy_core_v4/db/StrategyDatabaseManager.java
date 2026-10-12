package custom_studies.strategies.strategy_core_v4.db;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import custom_studies.strategies.strategy_core_v4.utils.FormatResult.StrategyStatsSummary;

public class StrategyDatabaseManager {
    // private static final String DB_PATH =
    // "/Users/simon/Projets/PlateformeDev/dev-motivewave/MotiveWave
    // Studies/src/study_examples/strategies/strategy_core_v4/db/data/strategy_history_v4.db";
    private static final String DB_PATH = "/Users/simon/Projets/PlateformeDev/dev-motivewave/MotiveWave_Studies/src/custom_studies/strategies/strategy_core_v4/db/data/strategy_stats_v4.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public int getOrInsertInstrument(Connection conn, String name) throws SQLException {
        String selectSql = "SELECT id FROM instrument WHERE name = ?";
        String insertSql = "INSERT INTO instrument(name) VALUES (?)";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, name);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("id");
            }
        }
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, name);
            insertStmt.executeUpdate();
            try (ResultSet rs = insertStmt.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }
        throw new SQLException("Instrument insert failed");
    }

    public int getOrInsertInterval(Connection conn, String scale, int interval) throws SQLException {
        String selectSql = "SELECT id FROM interval WHERE scale = ? AND interval = ?";
        String insertSql = "INSERT INTO interval(scale, interval) VALUES (?, ?)";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, scale);
            selectStmt.setInt(2, interval);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("id");
            }
        }
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, scale);
            insertStmt.setInt(2, interval);
            insertStmt.executeUpdate();
            try (ResultSet rs = insertStmt.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }
        throw new SQLException("Interval insert failed");
    }

    public int getOrInsertStrategy(Connection conn, int instrumentId, int intervalId, String name,
            Double tp, Double sl, Integer maxWin, Integer maxLoss, Integer maxConsecLoss) throws SQLException {
        String selectSql = "SELECT id FROM strategy WHERE instrument_id = ? AND interval_id = ? AND name = ?";
        String insertSql = "INSERT INTO strategy(instrument_id, interval_id, name, takeprofit, stoploss, max_win, max_loss, max_consecutive_loss) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setInt(1, instrumentId);
            selectStmt.setInt(2, intervalId);
            selectStmt.setString(3, name);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("id");
            }
        }
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setInt(1, instrumentId);
            insertStmt.setInt(2, intervalId);
            insertStmt.setString(3, name);
            insertStmt.setDouble(4, tp);
            insertStmt.setDouble(5, sl);
            insertStmt.setInt(6, maxWin);
            insertStmt.setInt(7, maxLoss);
            insertStmt.setInt(8, maxConsecLoss);
            insertStmt.executeUpdate();
            try (ResultSet rs = insertStmt.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }
        throw new SQLException("Strategy insert failed");
    }

    public void insertStrategyHours(Connection conn, int strategyId, List<String[]> hoursList) throws SQLException {
        String checkSql = "SELECT 1 FROM strategy_hours WHERE strategy_id = ? AND start = ? AND end = ?";
        String insertSql = "INSERT INTO strategy_hours(strategy_id, start, end) VALUES (?, ?, ?)";
        for (String[] range : hoursList) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, strategyId);
                checkStmt.setString(2, range[0]);
                checkStmt.setString(3, range[1]);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, strategyId);
                            insertStmt.setString(2, range[0]);
                            insertStmt.setString(3, range[1]);
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    public void insertTrade(Connection conn, int strategyId, Long timestamp, String datetime,
            String direction, Double entryPrice, Double tpPrice,
            Double slPrice, String result, String updatedAt) throws SQLException {
        String insertSql = """
                    INSERT OR IGNORE INTO trade(strategy_id, timestamp, datetime, direction, entry_price, tp_price, sl_price, result, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setInt(1, strategyId);
            insertStmt.setLong(2, timestamp);
            insertStmt.setString(3, datetime);
            insertStmt.setString(4, direction);
            insertStmt.setDouble(5, entryPrice);
            insertStmt.setDouble(6, tpPrice);
            insertStmt.setDouble(7, slPrice);
            insertStmt.setString(8, result);
            insertStmt.setString(9, updatedAt);
            insertStmt.executeUpdate();
        }
    }

    public void saveTradeWithDependencies(String instrumentName, String intervalScale, int intervalValue,
            String strategyName, Double takeProfit, Double stopLoss,
            Integer maxWin, Integer maxLoss, Integer maxConsecLoss,
            List<String[]> hoursList, Long timestamp, String direction,
            Double entryPrice, Double tpPrice, Double slPrice,
            String result) {

        String datetime = dateFormat.format(new Date(timestamp));
        String updatedAt = dateFormat.format(new Date());

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            int instrumentId = getOrInsertInstrument(conn, instrumentName);
            int intervalId = getOrInsertInterval(conn, intervalScale, intervalValue);
            int strategyId = getOrInsertStrategy(conn, instrumentId, intervalId, strategyName, takeProfit, stopLoss,
                    maxWin, maxLoss, maxConsecLoss);

            if (hoursList != null && !hoursList.isEmpty()) {
                insertStrategyHours(conn, strategyId, hoursList);
            }

            insertTrade(conn, strategyId, timestamp, datetime, direction, entryPrice, tpPrice, slPrice, result,
                    updatedAt);

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertRenkoSnapshot(Connection conn, int strategyId, long timestamp,
            double renkoOpen, double renkoClose, double renkoHigh, double renkoLow,
            double gannHilo, double adx, double adxPlus, double adxMinus,
            String renkoTrend, String candleColor, String signalLabel) throws SQLException {

        String insertSql = """
                    INSERT INTO renko_snapshot(
                        strategy_id, timestamp, renko_open, renko_close, renko_high, renko_low,
                        gann_hilo, adx, adx_plus, adx_minus,
                        renko_trend, candle_color, signal_label
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setInt(1, strategyId);
            stmt.setLong(2, timestamp);
            stmt.setDouble(3, renkoOpen);
            stmt.setDouble(4, renkoClose);
            stmt.setDouble(5, renkoHigh);
            stmt.setDouble(6, renkoLow);
            stmt.setDouble(7, gannHilo);
            stmt.setDouble(8, adx);
            stmt.setDouble(9, adxPlus);
            stmt.setDouble(10, adxMinus);
            stmt.setString(11, renkoTrend);
            stmt.setString(12, candleColor);
            stmt.setString(13, signalLabel);
            stmt.executeUpdate();
        }
    }

    public List<String> loadStats() {
        List<String> trades = new ArrayList<>();
        String sql = """
                    SELECT t.id, s.name AS strategy_name, t.timestamp, t.datetime, t.entry_price, t.direction, t.result
                    FROM trade t
                    JOIN strategy s ON t.strategy_id = s.id
                    ORDER BY t.timestamp ASC;
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String line = String.format(
                        "Trade #%d | Strategy: %s | Time: %s | Price: %.2f | Dir: %s | Result: %s",
                        rs.getInt("id"),
                        rs.getString("strategy_name"),
                        rs.getString("datetime"),
                        rs.getDouble("entry_price"),
                        rs.getString("direction"),
                        rs.getString("result"));
                trades.add(line);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return trades;
    }

    public List<StrategyStatsSummary> loadStatsFromDatabase() {
        String sql = """
                    SELECT
                        s.name AS strategy_name,
                        i.name AS instrument,
                        itv.scale AS interval_scale,
                        itv.interval AS interval_value,
                        s.takeprofit AS take_profit,
                        s.stoploss AS stop_loss,
                        COUNT(t.id) AS total_signals,
                        SUM(CASE WHEN t.result = 'TP' THEN 1 ELSE 0 END) AS win_signals,
                        SUM(CASE WHEN t.result = 'SL' THEN 1 ELSE 0 END) AS loss_signals,
                        MIN(t.timestamp) AS start_time,
                        MAX(t.timestamp) AS end_time
                    FROM trade t
                    JOIN strategy s ON t.strategy_id = s.id
                    JOIN instrument i ON s.instrument_id = i.id
                    JOIN interval itv ON s.interval_id = itv.id
                    GROUP BY s.name, i.name, itv.interval, s.takeprofit, s.stoploss;
                """;

        List<StrategyStatsSummary> summaries = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("strategy_name");
                String instrument = rs.getString("instrument");
                String intervalScale = rs.getString("interval_scale");
                int intervalValue = rs.getInt("interval_value");
                double takeProfit = rs.getDouble("take_profit");
                double stopLoss = rs.getDouble("stop_loss");
                int total = rs.getInt("total_signals");
                int wins = rs.getInt("win_signals");
                int losses = rs.getInt("loss_signals");
                long start = rs.getLong("start_time");
                long end = rs.getLong("end_time");

                summaries.add(new StrategyStatsSummary(name, instrument, intervalScale, intervalValue, start, end,
                        total, wins, losses, takeProfit, stopLoss));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return summaries;
    }

}
