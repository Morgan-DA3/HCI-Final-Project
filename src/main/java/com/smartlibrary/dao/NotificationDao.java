package com.smartlibrary.dao;

import com.smartlibrary.model.LibraryNotification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {
    private final DatabaseManager databaseManager;

    public NotificationDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<LibraryNotification> findAll() throws SQLException {
        List<LibraryNotification> notifications = new ArrayList<>();
        String sql = "SELECT id, user_id, title, message, type, is_read, created_at FROM notifications ORDER BY created_at DESC, id DESC";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                notifications.add(new LibraryNotification(
                        rs.getInt("id"),
                        rs.getObject("user_id") == null ? 0 : rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getString("type"),
                        rs.getBoolean("is_read"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        }
        return notifications;
    }

    public LibraryNotification save(Integer userId, String title, String message, String type) throws SQLException {
        String sql = "INSERT INTO notifications(user_id, title, message, type, is_read) VALUES (?, ?, ?, ?, FALSE)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (userId == null || userId == 0) statement.setNull(1, Types.INTEGER);
            else statement.setInt(1, userId);
            statement.setString(2, title);
            statement.setString(3, message);
            statement.setString(4, type);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new LibraryNotification(keys.getInt(1), userId == null ? 0 : userId,
                            title, message, type, false, java.time.LocalDateTime.now());
                }
            }
        }
        throw new SQLException("Notification was inserted, but no generated id was returned.");
    }
}
