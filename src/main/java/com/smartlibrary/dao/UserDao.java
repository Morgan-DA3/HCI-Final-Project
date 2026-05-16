package com.smartlibrary.dao;

import com.smartlibrary.model.Role;
import com.smartlibrary.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private final DatabaseManager databaseManager;

    public UserDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = """
                SELECT id, full_name, email, password_hash, role_name, active, created_at
                FROM users WHERE email = ?
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, full_name, email, password_hash, role_name, active, created_at FROM users ORDER BY full_name";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }
        return users;
    }

    public User save(String fullName, String email, String passwordHash, Role role) throws SQLException {
        String sql = """
                INSERT INTO users(full_name, email, password_hash, role_name, active)
                VALUES (?, ?, ?, ?, TRUE)
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, role.name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getInt(1), fullName, email, passwordHash, role, true, java.time.LocalDateTime.now());
                }
            }
        }
        throw new SQLException("User was inserted, but no generated id was returned.");
    }

    public void updateActive(int userId, boolean active) throws SQLException {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE users SET active=? WHERE id=?")) {
            statement.setBoolean(1, active);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new User(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role_name")),
                rs.getBoolean("active"),
                createdAt == null ? null : createdAt.toLocalDateTime()
        );
    }
}
