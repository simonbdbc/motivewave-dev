package com.monitoralgo.core.service;

import com.monitoralgo.core.database.DatabaseManager;
import com.monitoralgo.core.database.DatabaseInitializer;
import com.monitoralgo.core.database.DataSeeder;
import com.monitoralgo.core.database.PatternSeeder;

import java.sql.Connection;

import org.springframework.stereotype.Service;

/**
 * Service utilitaire pour administrer la base de données (init, reset, seed,
 * etc.)
 */
@Service
public class DatabaseAdminService {

    /** Initialise la structure de la base (création des tables, indexes, etc.) */
    public void initializeDatabase() {
        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseInitializer.initDatabase(conn);
        } catch (Exception e) {
            throw new RuntimeException("Erreur initialisation DB", e);
        }
    }

    /** Réinitialise et seed la base de données */
    public void resetDatabaseAndSeed() {
        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseInitializer.resetDatabase(conn);
            DataSeeder.seedAll(conn);
            // PatternSeeder.seedAllPatterns(conn); // Peut être long
        } catch (Exception e) {
            throw new RuntimeException("Erreur reset + seed DB", e);
        }
    }

    /** Insère les jeux de données de base */
    public void seedData() {
        try (Connection conn = DatabaseManager.getConnection()) {
            DataSeeder.seedAll(conn);
        } catch (Exception e) {
            throw new RuntimeException("Erreur seed data", e);
        }
    }

    /** Insère les patterns en base */
    public void seedPatterns() {
        try (Connection conn = DatabaseManager.getConnection()) {
            PatternSeeder.seedAllPatterns(conn); // Peut être long
        } catch (Exception e) {
            throw new RuntimeException("Erreur seed patterns", e);
        }
    }

    /** Supprime et ré-initialise une table spécifique */
    public void deleteAndReinitTable(String tableName) {
        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseInitializer.deleteTableDatabase(conn, tableName);
        } catch (Exception e) {
            throw new RuntimeException("Erreur suppression+reinit table " + tableName, e);
        }
    }
}
