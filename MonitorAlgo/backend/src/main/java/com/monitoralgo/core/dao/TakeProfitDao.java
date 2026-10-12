package com.monitoralgo.core.dao;

import com.monitoralgo.core.database.DatabaseManager;
import com.monitoralgo.core.model.TakeProfit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TakeProfitDao {

    public TakeProfit findById(int id) throws SQLException {
        String sql = "SELECT id_takeprofit, value_point, value_tick FROM takeprofit WHERE id_takeprofit = ?";
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

    public List<TakeProfit> findAll() throws SQLException {
        String sql = "SELECT id_takeprofit, value_point, value_tick FROM takeprofit";
        List<TakeProfit> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(TakeProfit tp) throws SQLException {
        String sql = "INSERT INTO takeprofit (value_point, value_tick) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, tp.getValuePoint());
            ps.setInt(2, tp.getValueTick());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                tp.setId(keys.getInt(1));
            }
        }
    }

    public void update(TakeProfit tp) throws SQLException {
        String sql = "UPDATE takeprofit SET value_point = ?, value_tick = ? WHERE id_takeprofit = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, tp.getValuePoint());
            ps.setInt(2, tp.getValueTick());
            ps.setInt(3, tp.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM takeprofit WHERE id_takeprofit = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private TakeProfit mapRow(ResultSet rs) throws SQLException {
        TakeProfit tp = new TakeProfit();
        tp.setId(rs.getInt("id_takeprofit"));
        tp.setValuePoint(rs.getDouble("value_point"));
        tp.setValueTick(rs.getInt("value_tick"));
        return tp;
    }

    public static TakeProfit mapRowFromJoin(ResultSet rs) throws SQLException {
        return new TakeProfit(
            rs.getInt("id_takeprofit"),
            rs.getDouble("tp_point"),
            rs.getInt("tp_tick")
        );
    }
}
