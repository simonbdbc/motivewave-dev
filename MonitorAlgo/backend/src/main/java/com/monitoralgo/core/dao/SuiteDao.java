package com.monitoralgo.core.dao;

import com.monitoralgo.core.database.DatabaseManager;
import com.monitoralgo.core.model.Suite;

import java.sql.*;
import java.util.*;

public class SuiteDao {

    /**
     * Trouve une suite par son identifiant.
     */
    public Suite findById(int id) throws SQLException {
        String sql = "SELECT id_suite, pattern FROM suite WHERE id_suite = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        }
    }

    /**
     * Trouve une suite par son pattern unique.
     */
    public Suite findByPattern(String pattern) throws SQLException {
        String sql = "SELECT id_suite, pattern FROM suite WHERE pattern = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        }
    }

    /**
     * Retourne toutes les suites.
     */
    public List<Suite> findAll() throws SQLException {
        String sql = "SELECT id_suite, pattern FROM suite";
        List<Suite> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Retourne toutes les suites sous forme de Map<pattern, Suite>.
     * Utile pour un accès rapide en RAM.
     */
    public Map<String, Suite> findAllAsMap() throws SQLException {
        Map<String, Suite> map = new HashMap<>();
        for (Suite suite : findAll()) {
            map.put(suite.getPattern(), suite);
        }
        return map;
    }

    /**
     * Ajoute une nouvelle suite (récupère la clé générée).
     */
    public void insert(Suite suite) throws SQLException {
        String sql = "INSERT INTO suite (pattern) VALUES (?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, suite.getPattern());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                suite.setId(keys.getInt(1));
            }
        }
    }

    /**
     * Met à jour une suite existante.
     */
    public void update(Suite suite) throws SQLException {
        String sql = "UPDATE suite SET pattern = ? WHERE id_suite = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, suite.getPattern());
            ps.setInt(2, suite.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Supprime une suite par ID.
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM suite WHERE id_suite = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Mapping ResultSet → Suite.
     */
    private Suite mapRow(ResultSet rs) throws SQLException {
        Suite suite = new Suite();
        suite.setId(rs.getInt("id_suite"));
        suite.setPattern(rs.getString("pattern"));
        return suite;
    }

    /**
     * Recherche par liste d'IDs (évite N requêtes).
     */
    public Map<Integer, Suite> findByIds(List<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty())
            return Collections.emptyMap();
        String inSql = String.join(", ", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id_suite, pattern FROM suite WHERE id_suite IN (" + inSql + ")";
        Map<Integer, Suite> map = new HashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Integer id : ids)
                ps.setInt(i++, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Suite suite = mapRow(rs);
                map.put(suite.getId(), suite);
            }
        }
        return map;
    }
}
