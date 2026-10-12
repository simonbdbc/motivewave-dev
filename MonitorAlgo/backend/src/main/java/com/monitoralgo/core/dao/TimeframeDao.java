package com.monitoralgo.core.dao;

import com.monitoralgo.core.database.DatabaseManager;
import com.monitoralgo.core.model.Timeframe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimeframeDao {

    public Timeframe findById(int id) throws SQLException {
        String sql = "SELECT id_timeframe, scale, value FROM timeframe WHERE id_timeframe = ?";
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

    public List<Timeframe> findAll() throws SQLException {
        String sql = "SELECT id_timeframe, scale, value FROM timeframe";
        List<Timeframe> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(Timeframe tf) throws SQLException {
        String sql = "INSERT INTO timeframe (scale, value) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tf.getScale());
            ps.setInt(2, tf.getValue());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                tf.setId(keys.getInt(1));
            }
        }
    }

    public void update(Timeframe tf) throws SQLException {
        String sql = "UPDATE timeframe SET scale = ?, value = ? WHERE id_timeframe = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tf.getScale());
            ps.setInt(2, tf.getValue());
            ps.setInt(3, tf.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM timeframe WHERE id_timeframe = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // --- Mapping ResultSet -> Timeframe
    private Timeframe mapRow(ResultSet rs) throws SQLException {
        Timeframe tf = new Timeframe();
        tf.setId(rs.getInt("id_timeframe"));
        tf.setScale(rs.getString("scale"));
        tf.setValue(rs.getInt("value"));
        return tf;
    }

    public static Timeframe mapRowFromJoin(ResultSet rs) throws SQLException {
        return new Timeframe(
            rs.getInt("id_timeframe"),
            rs.getString("tf_scale"),
            rs.getInt("tf_value")
        );
    }
}
