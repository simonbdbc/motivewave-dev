package com.monitoralgo.core.dao;

import com.monitoralgo.core.database.DatabaseManager;
import com.monitoralgo.core.model.Pattern;
import com.monitoralgo.core.model.Suite;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class PatternDao {

    private final SuiteDao suiteDao = new SuiteDao();

    public Pattern findById(int id) throws SQLException {
        String sql = "SELECT id_pattern, suite_renko_id, suite_hilo1_id, suite_hilo2_id FROM pattern WHERE id_pattern = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
                return null;
            }
        }
    }

    public Pattern findBySuites(int suiteRenkoId, int suiteHilo1Id, int suiteHilo2Id) throws SQLException {
        String sql = """
                SELECT id_pattern, suite_renko_id, suite_hilo1_id, suite_hilo2_id
                FROM pattern WHERE suite_renko_id = ? AND suite_hilo1_id = ? AND suite_hilo2_id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, suiteRenkoId);
            ps.setInt(2, suiteHilo1Id);
            ps.setInt(3, suiteHilo2Id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
                return null;
            }
        }
    }

    public void insert(Pattern pattern) throws SQLException {
        String sql = "INSERT INTO pattern (suite_renko_id, suite_hilo1_id, suite_hilo2_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pattern.getSuiteRenko().getId());
            ps.setInt(2, pattern.getSuiteHilo1().getId());
            ps.setInt(3, pattern.getSuiteHilo2().getId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    pattern.setId(keys.getInt(1));
                }
            }
        }
    }

    public void update(Pattern pattern) throws SQLException {
        String sql = "UPDATE pattern SET suite_renko_id = ?, suite_hilo1_id = ?, suite_hilo2_id = ? WHERE id_pattern = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pattern.getSuiteRenko().getId());
            ps.setInt(2, pattern.getSuiteHilo1().getId());
            ps.setInt(3, pattern.getSuiteHilo2().getId());
            ps.setInt(4, pattern.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM pattern WHERE id_pattern = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Version fallback pour les findById/unique */
    private Pattern mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_pattern");
        int renkoId = rs.getInt("suite_renko_id");
        int hilo1Id = rs.getInt("suite_hilo1_id");
        int hilo2Id = rs.getInt("suite_hilo2_id");

        // Ici 3 requêtes, mais pour un seul pattern, donc acceptable
        Suite suiteRenko = suiteDao.findById(renkoId);
        Suite suiteHilo1 = suiteDao.findById(hilo1Id);
        Suite suiteHilo2 = suiteDao.findById(hilo2Id);

        return new Pattern(id, suiteRenko, suiteHilo1, suiteHilo2);
    }

    /**
     * Version ultra optimisée : un seul SELECT, puis batch lookup pour les suites
     * (pas de multi-requête dans la boucle !)
     */
    public List<Pattern> findAll() throws SQLException {
        String sql = "SELECT id_pattern, suite_renko_id, suite_hilo1_id, suite_hilo2_id FROM pattern";
        List<Pattern> patterns = new ArrayList<>();
        List<Integer> suiteIds = new ArrayList<>();
        class Row {
            int id, r, h1, h2;
        }
        List<Row> rows = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Row row = new Row();
                row.id = rs.getInt("id_pattern");
                row.r = rs.getInt("suite_renko_id");
                row.h1 = rs.getInt("suite_hilo1_id");
                row.h2 = rs.getInt("suite_hilo2_id");
                rows.add(row);
                suiteIds.add(row.r);
                suiteIds.add(row.h1);
                suiteIds.add(row.h2);
            }
        }
        // Récupère tous les objets Suite en une seule requête
        Map<Integer, Suite> suiteMap = suiteDao.findByIds(
                suiteIds.stream().distinct().collect(Collectors.toList()));
        for (Row row : rows) {
            patterns.add(new Pattern(
                    row.id,
                    suiteMap.get(row.r),
                    suiteMap.get(row.h1),
                    suiteMap.get(row.h2)));
        }
        return patterns;
    }

    // Méthode alternative, si tu veux charger tout avec un seul JOIN (ex: pour DTO
    // admin, pas pour le core !)
    public List<Pattern> findAllWithSuites() throws SQLException {
        String sql = """
                SELECT p.id_pattern,
                       s1.id_suite as renko_suite_id, s1.pattern as renko_suite_pattern,
                       s2.id_suite as hilo1_suite_id, s2.pattern as hilo1_suite_pattern,
                       s3.id_suite as hilo2_suite_id, s3.pattern as hilo2_suite_pattern
                FROM pattern p
                JOIN suite s1 ON p.suite_renko_id = s1.id_suite
                JOIN suite s2 ON p.suite_hilo1_id = s2.id_suite
                JOIN suite s3 ON p.suite_hilo2_id = s3.id_suite
                """;
        List<Pattern> patterns = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                patterns.add(
                        new Pattern(
                                rs.getInt("id_pattern"),
                                new Suite(rs.getInt("renko_suite_id"), rs.getString("renko_suite_pattern")),
                                new Suite(rs.getInt("hilo1_suite_id"), rs.getString("hilo1_suite_pattern")),
                                new Suite(rs.getInt("hilo2_suite_id"), rs.getString("hilo2_suite_pattern"))));
            }
        }
        return patterns;
    }

    /**
     * Récupère tous les patterns avec tous les objets enfants Suite déjà mappés.
     * Un seul SELECT avec JOIN pour charger tout d’un coup.
     */
    public List<Pattern> findAllFullJoin() throws SQLException {
        String sql = """
                SELECT
                    p.id_pattern,
                    s1.id_suite AS renko_suite_id,  s1.pattern AS renko_pattern,
                    s2.id_suite AS hilo1_suite_id,  s2.pattern AS hilo1_pattern,
                    s3.id_suite AS hilo2_suite_id,  s3.pattern AS hilo2_pattern
                FROM pattern p
                JOIN suite s1 ON p.suite_renko_id = s1.id_suite
                JOIN suite s2 ON p.suite_hilo1_id = s2.id_suite
                JOIN suite s3 ON p.suite_hilo2_id = s3.id_suite
                """;

        List<Pattern> patterns = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Suite suiteRenko = new Suite(
                        rs.getInt("renko_suite_id"),
                        rs.getString("renko_pattern"));
                Suite suiteHilo1 = new Suite(
                        rs.getInt("hilo1_suite_id"),
                        rs.getString("hilo1_pattern"));
                Suite suiteHilo2 = new Suite(
                        rs.getInt("hilo2_suite_id"),
                        rs.getString("hilo2_pattern"));
                Pattern pattern = new Pattern(
                        rs.getInt("id_pattern"),
                        suiteRenko,
                        suiteHilo1,
                        suiteHilo2);
                patterns.add(pattern);
            }
        }
        return patterns;
    }

    public static Pattern mapRowWithSuites(ResultSet rs) throws SQLException {
        Pattern p = new Pattern();
        p.setId(rs.getInt("id_pattern"));
        p.setSuiteRenko(new Suite(rs.getInt("renko_suite_id"), rs.getString("renko_suite_pattern")));
        p.setSuiteHilo1(new Suite(rs.getInt("hilo1_suite_id"), rs.getString("hilo1_suite_pattern")));
        p.setSuiteHilo2(new Suite(rs.getInt("hilo2_suite_id"), rs.getString("hilo2_suite_pattern")));
        return p;
    }
}
