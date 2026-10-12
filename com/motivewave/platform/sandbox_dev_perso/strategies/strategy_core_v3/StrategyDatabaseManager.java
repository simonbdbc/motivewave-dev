
package custom_studies.strategies.strategy_core_v3;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
// import java.util.Map;

import custom_studies.strategies.strategy_core_v3.utils.FormatResult.StrategyStatsSummary;

public class StrategyDatabaseManager {
    private static final String PATH = "/Users/simon/Projets/PlateformeDev/dev-motivewave/MotiveWave_Studies/src/custom_studies/strategies/strategy_core_v3/db";
    private static final String DB_URL = "jdbc:sqlite:" + PATH + "/strategy_stats_v3.db";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public StrategyDatabaseManager() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {

            String sql = """
                        CREATE TABLE IF NOT EXISTS strategy_signals (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            strategy_name TEXT,
                            timestamp INTEGER,
                            instrument TEXT,
                            tick_interval INTERGER,
                            take_profit REAL,
                            stop_loss REAL,
                            date TEXT,
                            direction TEXT,
                            entry_price REAL,
                            tp_price REAL,
                            sl_price REAL,
                            result TEXT,
                            updated_at TEXT,
                            UNIQUE(strategy_name, timestamp, instrument, tick_interval, take_profit, stop_loss)
                        );
                    """;

            stmt.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveSignal(String strategyName, Long timestamp, String instrument, Integer tickInterval,
            Double takeProfit, Double stopLoss, String direction, Float entryPrice, Double tpPrice, Double slPrice,
            String result) {

        String dateStr = dateFormat.format(new Date(timestamp)); // date du signal
        String updatedAt = dateFormat.format(new Date()); // date de mise à jour actuelle

        // 1. Vérification de l'existence de la combinaison strategy_name + timestamp
        String checkSql = """
                    SELECT 1 FROM strategy_signals
                    WHERE strategy_name = ? AND timestamp = ? AND instrument = ? AND tick_interval = ? AND take_profit = ? AND stop_loss = ?;
                """;

        // 2. Si la combinaison n'existe pas, procéder à l'insertion
        String insertSql = """
                    INSERT INTO strategy_signals(
                        strategy_name, timestamp, instrument, tick_interval, take_profit, stop_loss, date, direction, entry_price, tp_price, sl_price, result, updated_at
                    ) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
                // Préparer la requête de vérification
                PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {

            checkPstmt.setString(1, strategyName);
            checkPstmt.setLong(2, timestamp);
            checkPstmt.setString(3, instrument);
            checkPstmt.setInt(4, tickInterval);
            checkPstmt.setDouble(5, takeProfit);
            checkPstmt.setDouble(6, stopLoss);

            try (ResultSet rs = checkPstmt.executeQuery()) {
                // Si aucune ligne n'est retournée (pas de combinaison existante)
                if (!rs.next()) {
                    // Préparer l'insertion
                    try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                        pstmt.setString(1, strategyName);
                        pstmt.setLong(2, timestamp);
                        pstmt.setString(3, instrument);
                        pstmt.setInt(4, tickInterval);
                        pstmt.setDouble(5, takeProfit);
                        pstmt.setDouble(6, stopLoss);
                        pstmt.setString(7, dateStr);
                        pstmt.setString(8, direction);
                        pstmt.setFloat(9, entryPrice);
                        pstmt.setDouble(10, tpPrice);
                        pstmt.setDouble(11, slPrice);
                        pstmt.setString(12, result);
                        pstmt.setString(13, updatedAt);

                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // public List<StrategyStatsSummary> loadStatsFromDatabase(Map<String,
    // StrategyParams> paramsMap) {
    public List<StrategyStatsSummary> loadStatsFromDatabase() {

        // System.err.println("strategyParamsMap : " + paramsMap);

        String sql = """
                    SELECT strategy_name, instrument, tick_interval, take_profit, stop_loss,
                           COUNT(*) as total_signals,
                           SUM(CASE WHEN result = 'TP' THEN 1 ELSE 0 END) as win_signals,
                           SUM(CASE WHEN result = 'SL' THEN 1 ELSE 0 END) as loss_signals,
                           MIN(timestamp) as start_time,
                           MAX(timestamp) as end_time
                    FROM strategy_signals
                    GROUP BY strategy_name, instrument, tick_interval, take_profit, stop_loss;
                """;

        List<StrategyStatsSummary> summaries = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("strategy_name");
                String instrument = rs.getString("instrument");
                Integer tickInterval = rs.getInt("tick_interval");
                Double takeProfit = rs.getDouble("take_profit");
                Double stopLoss = rs.getDouble("stop_loss");
                Integer total = rs.getInt("total_signals");
                Integer wins = rs.getInt("win_signals");
                Integer losses = rs.getInt("loss_signals");
                Long start = rs.getLong("start_time");
                Long end = rs.getLong("end_time");

                // StrategyParams params = paramsMap.get(name);
                // if (params != null) {
                summaries.add(new StrategyStatsSummary(name, instrument, tickInterval, start, end, total, wins, losses,
                        takeProfit, stopLoss));
                // }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return summaries;
    }

}
