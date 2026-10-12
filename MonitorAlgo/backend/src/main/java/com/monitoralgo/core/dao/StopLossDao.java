package com.monitoralgo.core.dao;

import com.monitoralgo.core.database.DatabaseManager;
import com.monitoralgo.core.model.StopLoss;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StopLossDao {

    public StopLoss findById(int id) throws SQLException {
        String sql = "SELECT id_stoploss, value_point, value_tick FROM stoploss WHERE id_stoploss = ?";
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

    public List<StopLoss> findAll() throws SQLException {
        String sql = "SELECT id_stoploss, value_point, value_tick FROM stoploss";
        List<StopLoss> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(StopLoss sl) throws SQLException {
        String sql = "INSERT INTO stoploss (value_point, value_tick) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, sl.getValuePoint());
            ps.setInt(2, sl.getValueTick());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                sl.setId(keys.getInt(1));
            }
        }
    }

    public void update(StopLoss sl) throws SQLException {
        String sql = "UPDATE stoploss SET value_point = ?, value_tick = ? WHERE id_stoploss = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, sl.getValuePoint());
            ps.setInt(2, sl.getValueTick());
            ps.setInt(3, sl.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM stoploss WHERE id_stoploss = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private StopLoss mapRow(ResultSet rs) throws SQLException {
        StopLoss sl = new StopLoss();
        sl.setId(rs.getInt("id_stoploss"));
        sl.setValuePoint(rs.getDouble("value_point"));
        sl.setValueTick(rs.getInt("value_tick"));
        return sl;
    }

    public static StopLoss mapRowFromJoin(ResultSet rs) throws SQLException {
        return new StopLoss(
            rs.getInt("id_stoploss"),
            rs.getDouble("sl_point"),
            rs.getInt("sl_tick")
        );
    }
}
