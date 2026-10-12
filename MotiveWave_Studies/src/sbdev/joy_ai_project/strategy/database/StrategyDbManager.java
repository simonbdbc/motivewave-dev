package sbdev.joy_ai_project.strategy.database;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sbdev.joy_ai_project.strategy.util.Helper;
import sbdev.joy_ai_project.strategy.model.PatternGroupKey;
import sbdev.joy_ai_project.strategy.model.StrategyStatsSummary;

public class StrategyDbManager {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public int getOrInsertInstrument(Connection conn, String name) throws SQLException {
        String selectSql = "SELECT id FROM instrument WHERE name = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, name);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("id");
            }
        }

        String instrumentKey = Helper.extractInstrumentKeyGroup(name);
        String insertSql = "INSERT INTO instrument(name, instrument_key) VALUES (?, ?) RETURNING id";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, name);
            insertStmt.setString(2, instrumentKey);
            try (ResultSet rs = insertStmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }
        throw new SQLException("Instrument insert failed");
    }

    public int getOrInsertTimeframe(Connection conn, String scale, int interval) throws SQLException {
        String selectSql = "SELECT id FROM timeframe WHERE scale = ? AND interval = ?";
        String insertSql = "INSERT INTO timeframe(scale, interval) VALUES (?, ?) RETURNING id";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, scale);
            selectStmt.setInt(2, interval);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("id");
            }
        }
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, scale);
            insertStmt.setInt(2, interval);
            try (ResultSet rs = insertStmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }
        throw new SQLException("Timeframe insert failed");
    }

    public int getOrInsertStrategy(Connection conn, int instrumentId, int timeframeId, String name, Double tp,
            Double sl, Integer maxWin, Integer maxLoss, Integer maxConsecLoss) throws SQLException {
        String selectSql = "SELECT id FROM strategy WHERE instrument_id = ? AND timeframe_id = ? AND name = ?";
        String insertSql = "INSERT INTO strategy(instrument_id, timeframe_id, name, takeprofit, stoploss, max_win, max_loss, max_consecutive_loss) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setInt(1, instrumentId);
            selectStmt.setInt(2, timeframeId);
            selectStmt.setString(3, name);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("id");
            }
        }
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setInt(1, instrumentId);
            insertStmt.setInt(2, timeframeId);
            insertStmt.setString(3, name);
            insertStmt.setDouble(4, tp);
            insertStmt.setDouble(5, sl);
            insertStmt.setInt(6, maxWin);
            insertStmt.setInt(7, maxLoss);
            insertStmt.setInt(8, maxConsecLoss);
            try (ResultSet rs = insertStmt.executeQuery()) {
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

    public void insertTrade(Connection conn, int strategyId, Long timestamp, String date_time, String direction,
            Double entryPrice, Double tpPrice, Double slPrice, String result, String updatedAt) throws SQLException {
        String insertSql = """
                    INSERT INTO trade(strategy_id, timestamp, date_time, direction, entry_price, tp_price, sl_price, result, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (strategy_id, timestamp) DO UPDATE SET
                        date_time=EXCLUDED.date_time,
                        direction=EXCLUDED.direction,
                        entry_price=EXCLUDED.entry_price,
                        tp_price=EXCLUDED.tp_price,
                        sl_price=EXCLUDED.sl_price,
                        result=EXCLUDED.result,
                        updated_at=EXCLUDED.updated_at
                """;
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setInt(1, strategyId);
            insertStmt.setLong(2, timestamp);
            insertStmt.setString(3, date_time);
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
            String strategyName, Double takeProfit, Double stopLoss, Integer maxWin, Integer maxLoss,
            Integer maxConsecLoss, List<String[]> hoursList, Long timestamp, String direction, Double entryPrice,
            Double tpPrice, Double slPrice, String result) {

        String date_time = dateFormat.format(new Date(timestamp));
        String updatedAt = dateFormat.format(new Date());

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            int instrumentId = getOrInsertInstrument(conn, instrumentName);
            int timeframeId = getOrInsertTimeframe(conn, intervalScale, intervalValue);
            int strategyId = getOrInsertStrategy(conn, instrumentId, timeframeId, strategyName, takeProfit, stopLoss,
                    maxWin, maxLoss, maxConsecLoss);

            if (hoursList != null && !hoursList.isEmpty()) {
                insertStrategyHours(conn, strategyId, hoursList);
            }

            insertTrade(conn, strategyId, timestamp, date_time, direction, entryPrice, tpPrice, slPrice, result,
                    updatedAt);

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertRenkoSnapshot(Connection conn, int strategyId, long timestamp, double renkoOpen,
            double renkoClose, double renkoHigh, double renkoLow, double gannHilo, double adx, double adxPlus,
            double adxMinus, String renkoTrend, String candleColor, String signalLabel) throws SQLException {

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
                    SELECT t.id, s.name AS strategy_name, t.timestamp, t.date_time, t.entry_price, t.direction, t.result
                    FROM trade t
                    JOIN strategy s ON t.strategy_id = s.id
                    ORDER BY t.timestamp ASC;
                """;

        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String line = String.format("Trade #%d | Strategy: %s | Time: %s | Price: %.2f | Dir: %s | Result: %s",
                        rs.getInt("id"), rs.getString("strategy_name"), rs.getString("date_time"),
                        rs.getDouble("entry_price"), rs.getString("direction"), rs.getString("result"));
                trades.add(line);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return trades;
    }

    public List<StrategyStatsSummary> loadStatsFromDatabase(String startTime, String endTime, String instrument,
            List<PatternGroupKey> patternGroups // liste (peut être vide ou null)
    ) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        s.name AS strategy_name,
                        i.name AS instrument,
                        tf.scale AS tf_scale,
                        tf.interval AS tf_interval,
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
                    JOIN timeframe tf ON s.timeframe_id = tf.id
                    WHERE to_char(to_timestamp(t.timestamp / 1000), 'HH24:MI') BETWEEN ? AND ?
                """);

        if (instrument != null) {
            sql.append(" AND i.name = ? ");
        }

        // Construction dynamique du filtre sur les groupes
        if (patternGroups != null && !patternGroups.isEmpty()) {
            sql.append(" AND (");
            for (int i = 0; i < patternGroups.size(); i++) {
                if (i > 0)
                    sql.append(" OR ");
                sql.append(" (tf.interval = ? AND s.takeprofit = ? AND s.stoploss = ?) ");
            }
            sql.append(") ");
        }

        sql.append("""
                    GROUP BY s.name, i.name, tf.interval, s.takeprofit, s.stoploss
                """);

        List<StrategyStatsSummary> summaries = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            pstmt.setString(paramIndex++, startTime);
            pstmt.setString(paramIndex++, endTime);

            if (instrument != null) {
                pstmt.setString(paramIndex++, instrument);
            }

            if (patternGroups != null && !patternGroups.isEmpty()) {
                for (PatternGroupKey g : patternGroups) {
                    pstmt.setInt(paramIndex++, g.intervalValue);
                    pstmt.setInt(paramIndex++, g.tp);
                    pstmt.setInt(paramIndex++, g.sl);
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("strategy_name");
                    String instr = rs.getString("instrument");
                    String intervalScale = rs.getString("tf_scale");
                    int intervalValue = rs.getInt("tf_interval");
                    double takeProfit = rs.getDouble("take_profit");
                    double stopLoss = rs.getDouble("stop_loss");
                    int total = rs.getInt("total_signals");
                    int wins = rs.getInt("win_signals");
                    int losses = rs.getInt("loss_signals");
                    long start = rs.getLong("start_time");
                    long end = rs.getLong("end_time");

                    summaries.add(new StrategyStatsSummary(name, instr, intervalScale, intervalValue, start, end, total,
                            wins, losses, takeProfit, stopLoss));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return summaries;
    }
}
