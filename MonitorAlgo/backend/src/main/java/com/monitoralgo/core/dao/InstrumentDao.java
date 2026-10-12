package com.monitoralgo.core.dao;

import com.monitoralgo.core.database.DatabaseManager;
import com.monitoralgo.core.model.Instrument;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstrumentDao {

    public Instrument findById(int id) throws SQLException {
        String sql = "SELECT id_instrument, label, key, tick_size FROM instrument WHERE id_instrument = ?";
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

    public Instrument findByKey(String key) throws SQLException {
        String sql = "SELECT id_instrument, label, key, tick_size FROM instrument WHERE key = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        }
    }

    public List<Instrument> findAll() throws SQLException {
        String sql = "SELECT id_instrument, label, key, tick_size FROM instrument";
        List<Instrument> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(Instrument instrument) throws SQLException {
        String sql = "INSERT INTO instrument (label, key, tick_size) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, instrument.getLabel());
            ps.setString(2, instrument.getKey());
            ps.setDouble(3, instrument.getTickSize());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                instrument.setId(keys.getInt(1));
            }
        }
    }

    public void update(Instrument instrument) throws SQLException {
        String sql = "UPDATE instrument SET label = ?, key = ?, tick_size = ? WHERE id_instrument = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, instrument.getLabel());
            ps.setString(2, instrument.getKey());
            ps.setDouble(3, instrument.getTickSize());
            ps.setInt(4, instrument.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM instrument WHERE id_instrument = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // --- Mapping ResultSet -> Instrument
    private Instrument mapRow(ResultSet rs) throws SQLException {
        Instrument i = new Instrument();
        i.setId(rs.getInt("id_instrument"));
        i.setLabel(rs.getString("label"));
        i.setKey(rs.getString("key"));
        i.setTickSize(rs.getDouble("tick_size"));
        return i;
    }

    public static Instrument mapRowFromJoin(ResultSet rs) throws SQLException {
        return new Instrument(
            rs.getInt("id_instrument"),
            rs.getString("instrument_label"),
            rs.getString("instrument_key"),
            rs.getDouble("tick_size")
        );
    }
}
