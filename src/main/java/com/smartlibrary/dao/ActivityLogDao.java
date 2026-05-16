package com.smartlibrary.dao;

import com.smartlibrary.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {
    private final DatabaseManager databaseManager;

    public ActivityLogDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<ActivityLog> findAll() throws SQLException {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = """
                SELECT l.id, COALESCE(u.full_name, 'System') AS actor, l.action, l.details, l.created_at
                FROM activity_logs l
                LEFT JOIN users u ON u.id = l.actor_id
                ORDER BY l.created_at DESC, l.id DESC
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                logs.add(new ActivityLog(
                        rs.getInt("id"),
                        rs.getString("actor"),
                        rs.getString("action"),
                        rs.getString("details"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        }
        return logs;
    }

    public void save(String actor, String action, String details) throws SQLException {
        String sql = "INSERT INTO activity_logs(actor_id, action, details) VALUES (NULL, ?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, action);
            statement.setString(2, "[" + (actor == null ? "System" : actor) + "] " + details);
            statement.executeUpdate();
        }
    }
}
