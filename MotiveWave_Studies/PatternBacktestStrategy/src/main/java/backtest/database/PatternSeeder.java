package backtest.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.*;
import java.util.*;

public class PatternSeeder {
    public static void seedAllPatterns(Connection conn) {
        // On suppose que la table suite est déjà remplie avec id_suite de 1 à 256
        // (si ce n'est pas le cas, adapter la récupération des id)
        String sql = "INSERT INTO pattern(suite_renko_id, suite_hilo1_id, suite_hilo2_id) VALUES (?, ?, ?)";
        conn.setAutoCommit(false); // Pour optimiser l'insertion massive
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 256; i++) {
                for (int j = 1; j <= 256; j++) {
                    for (int k = 1; k <= 256; k++) {
                        ps.setInt(1, i);
                        ps.setInt(2, j);
                        ps.setInt(3, k);
                        ps.addBatch();
                        // Batch commit toutes les 10 000 lignes pour ne pas exploser la mémoire
                        if (((i - 1) * 256 * 256 + (j - 1) * 256 + k) % 10000 == 0) {
                            ps.executeBatch();
                            conn.commit();
                        }
                    }
                }
            }
            ps.executeBatch();
            conn.commit();
        }
        conn.setAutoCommit(true);
    }
}
