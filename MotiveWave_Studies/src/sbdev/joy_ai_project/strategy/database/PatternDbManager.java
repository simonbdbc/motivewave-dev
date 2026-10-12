package sbdev.joy_ai_project.strategy.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import sbdev.joy_ai_project.strategy.model.Bracket;
import sbdev.joy_ai_project.strategy.model.PatternRenkoStrategy;

public class PatternDbManager {

    /**
     * Charge tous les patterns depuis la BDD active (connexion fournie par
     * DatabaseManager).
     */
    public static List<PatternRenkoStrategy> loadPatternsFromDb() throws SQLException {
        List<PatternRenkoStrategy> list = new ArrayList<>();
        String sql = "SELECT * FROM pattern_renko ORDER BY score DESC"; // Trie par score décroissant

        // Connexion via DatabaseManager (HikariCP, PostgreSQL)
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PatternRenkoStrategy p = new PatternRenkoStrategy();
                p.instrumentKey = rs.getString("instrument_key");
                p.intervalScale = rs.getString("tf_scale");
                p.intervalValue = rs.getInt("tf_interval");
                p.renkoSize = rs.getInt("renko_size");
                int tp = rs.getInt("tp");
                int sl = rs.getInt("sl");
                p.bracket = new Bracket(tp, sl);
                p.patternRenko = rs.getString("pattern_renko");
                p.patternHilo1 = rs.getString("pattern_hilo1");
                p.patternHilo2 = rs.getString("pattern_hilo2");
                p.direction = rs.getString("direction");
                p.count = rs.getInt("count");
                p.lastDateTimeEntry = rs.getString("last_date_time_entry");
                p.countTp = rs.getInt("count_tp");
                p.countSl = rs.getInt("count_sl");
                p.delta = rs.getInt("delta");
                p.winrate = rs.getString("winrate");
                p.profitFactor = rs.getDouble("profit_factor");
                p.score = rs.getDouble("score");
                list.add(p);
            }
        }
        return list;
    }

    // Tu peux ajouter ici d'autres méthodes (insert, update, createTableIfNeeded,
    // etc)
}
