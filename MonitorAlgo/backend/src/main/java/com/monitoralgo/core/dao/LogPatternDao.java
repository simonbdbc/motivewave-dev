package com.monitoralgo.core.dao;

import com.monitoralgo.core.database.DatabaseManager;
import com.monitoralgo.core.model.LogPattern;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogPatternDao {

    public List<LogPattern> findAll() throws SQLException {
        String sql = "SELECT id, pattern, message, active FROM log_pattern";
        List<LogPattern> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public LogPattern findById(long id) throws SQLException {
        String sql = "SELECT id, pattern, message, active FROM log_pattern WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        }
        return null;
    }

    public LogPattern save(LogPattern pattern) throws SQLException {
        if (pattern.getId() == null) {
            // Insert
            String sql = "INSERT INTO log_pattern (pattern, message, active) VALUES (?, ?, ?) RETURNING id";
            try (Connection conn = DatabaseManager.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pattern.getPattern());
                ps.setString(2, pattern.getMessage());
                ps.setBoolean(3, pattern.isActive());
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                    pattern.setId(rs.getLong(1));
            }
        } else {
            // Update
            String sql = "UPDATE log_pattern SET pattern=?, message=?, active=? WHERE id=?";
            try (Connection conn = DatabaseManager.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pattern.getPattern());
                ps.setString(2, pattern.getMessage());
                ps.setBoolean(3, pattern.isActive());
                ps.setLong(4, pattern.getId());
                ps.executeUpdate();
            }
        }
        return pattern;
    }

    public boolean existsById(long id) throws SQLException {
        String sql = "SELECT 1 FROM log_pattern WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void deleteById(long id) throws SQLException {
        String sql = "DELETE FROM log_pattern WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private LogPattern mapRow(ResultSet rs) throws SQLException {
        LogPattern p = new LogPattern();
        p.setId(rs.getLong("id"));
        p.setPattern(rs.getString("pattern"));
        p.setMessage(rs.getString("message"));
        p.setActive(rs.getBoolean("active"));
        return p;
    }
}
