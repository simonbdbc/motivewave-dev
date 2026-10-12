package com.monitoralgo.core.database;

import java.sql.Connection;
import java.sql.SQLException;

import java.sql.*;

public class DataSeeder {
    public static void seedAll(Connection conn) throws SQLException {
        // --- INSTRUMENT ---
        String[] instruments = { "NQM5,NQ,0.25", "MNQM5,MNQ,0.25" };
        try (PreparedStatement ps = conn
                .prepareStatement("INSERT INTO instrument(label, key, tick_size) VALUES (?, ?, ?)")) {
            for (String row : instruments) {
                String[] parts = row.split(",");
                ps.setString(1, parts[0]);
                ps.setString(2, parts[1]);
                ps.setDouble(3, Double.parseDouble(parts[2]));
                ps.executeUpdate();
            }
        }

        // --- TIMEFRAME ---
        String[][] timeframes = { { "ticks", "1" }, { "ticks", "5" }, { "ticks", "10" }, { "ticks", "25" } };
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO timeframe(scale, value) VALUES (?, ?)")) {
            for (String[] tf : timeframes) {
                ps.setString(1, tf[0]);
                ps.setInt(2, Integer.parseInt(tf[1]));
                ps.executeUpdate();
            }
        }

        // --- DIRECTION ---
        String[] directions = { "LONG", "SHORT" };
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO direction(label) VALUES (?)")) {
            for (String d : directions) {
                ps.setString(1, d);
                ps.executeUpdate();
            }
        }

        // --- SUITE ---
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO suite(pattern) VALUES (?)")) {
            for (int i = 0; i < 256; i++) {
                StringBuilder sb = new StringBuilder(8);
                for (int b = 7; b >= 0; b--) {
                    sb.append(((i >> b) & 1) == 0 ? 'R' : 'G');
                }
                ps.setString(1, sb.toString());
                ps.executeUpdate();
            }
        }

        // --- RENKO_SIZE ---
        double[][] renkoSizes = { { 2.5, 10 }, { 5.0, 20 }, { 10.0, 40 } };
        try (PreparedStatement ps = conn
                .prepareStatement("INSERT INTO renko_size(value_point, value_tick) VALUES (?, ?)")) {
            for (double[] r : renkoSizes) {
                ps.setDouble(1, r[0]);
                ps.setInt(2, (int) r[1]);
                ps.executeUpdate();
            }
        }

        // --- TAKEPROFIT ---
        double[][] takeprofits = { { 15.0, 60 }, { 30.0, 120 } };
        try (PreparedStatement ps = conn
                .prepareStatement("INSERT INTO takeprofit(value_point, value_tick) VALUES (?, ?)")) {
            for (double[] tp : takeprofits) {
                ps.setDouble(1, tp[0]);
                ps.setInt(2, (int) tp[1]);
                ps.executeUpdate();
            }
        }

        // --- STOPLOSS ---
        double[][] stoplosses = { { 15.0, 60 }, { 30.0, 120 } };
        try (PreparedStatement ps = conn
                .prepareStatement("INSERT INTO stoploss(value_point, value_tick) VALUES (?, ?)")) {
            for (double[] sl : stoplosses) {
                ps.setDouble(1, sl[0]);
                ps.setInt(2, (int) sl[1]);
                ps.executeUpdate();
            }
        }
    }
}
