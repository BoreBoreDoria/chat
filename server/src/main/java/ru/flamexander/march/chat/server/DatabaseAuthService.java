package ru.flamexander.march.chat.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseAuthService {
    private static final String DB_URL = "jdbc:sqlite:chat.db";

    public DatabaseAuthService() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "username TEXT NOT NULL," +
                            "password TEXT NOT NULL," +
                            "role TEXT NOT NULL)")) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean authenticate(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM users WHERE username = ? AND password = ?")) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}