package com.monitoralgo.core.dao;

import com.monitoralgo.core.database.DatabaseManager;
import com.monitoralgo.core.model.RenkoSize;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RenkoSizeDao {

    public RenkoSize findById(int id) throws SQLException {
        String sql = "SELECT id_renko_size, value_point, value_tick FROM renko_size WHERE id_renko_size = ?";
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

    public List<RenkoSize> findAll() throws SQLException {
        String sql = "SELECT id_renko_size, value_point, value_tick FROM renko_size";
        List<RenkoSize> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(RenkoSize renkoSize) throws SQLException {
        String sql = "INSERT INTO renko_size (value_point, value_tick) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, renkoSize.getValuePoint());
            ps.setInt(2, renkoSize.getValueTick());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                renkoSize.setId(keys.getInt(1));
            }
        }
    }

    public void update(RenkoSize renkoSize) throws SQLException {
        String sql = "UPDATE renko_size SET value_point = ?, value_tick = ? WHERE id_renko_size = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, renkoSize.getValuePoint());
            ps.setInt(2, renkoSize.getValueTick());
            ps.setInt(3, renkoSize.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM renko_size WHERE id_renko_size = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // --- Mapping ResultSet -> RenkoSize
    private RenkoSize mapRow(ResultSet rs) throws SQLException {
        RenkoSize renkoSize = new RenkoSize();
        renkoSize.setId(rs.getInt("id_renko_size"));
        renkoSize.setValuePoint(rs.getDouble("value_point"));
        renkoSize.setValueTick(rs.getInt("value_tick"));
        return renkoSize;
    }

    public static RenkoSize mapRowFromJoin(ResultSet rs) throws SQLException {
        return new RenkoSize(
            rs.getInt("id_renko_size"),
            rs.getDouble("renko_point"),
            rs.getInt("renko_tick")
        );
    }
}
