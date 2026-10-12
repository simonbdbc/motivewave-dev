package com.monitoralgo.core.dao;

import com.monitoralgo.core.database.DatabaseManager;
import com.monitoralgo.core.model.Direction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DirectionDao {

    public Direction findById(int id) throws SQLException {
        String sql = "SELECT id_direction, label FROM direction WHERE id_direction = ?";
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

    public List<Direction> findAll() throws SQLException {
        String sql = "SELECT id_direction, label FROM direction";
        List<Direction> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(Direction dir) throws SQLException {
        String sql = "INSERT INTO direction (label) VALUES (?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dir.getLabel());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                dir.setId(keys.getInt(1));
            }
        }
    }

    public void update(Direction dir) throws SQLException {
        String sql = "UPDATE direction SET label = ? WHERE id_direction = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dir.getLabel());
            ps.setInt(2, dir.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM direction WHERE id_direction = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // --- Mapping ResultSet -> Direction
    private Direction mapRow(ResultSet rs) throws SQLException {
        Direction dir = new Direction();
        dir.setId(rs.getInt("id_direction"));
        dir.setLabel(rs.getString("label"));
        return dir;
    }

    public static Direction mapRowFromJoin(ResultSet rs) throws SQLException {
        return new Direction(
            rs.getInt("id_direction"),
            rs.getString("direction_label")
        );
    }
}
