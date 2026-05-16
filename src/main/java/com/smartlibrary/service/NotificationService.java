package com.smartlibrary.service;

import com.smartlibrary.dao.DatabaseManager;
import com.smartlibrary.dao.NotificationDao;
import com.smartlibrary.model.LibraryNotification;
import com.smartlibrary.model.User;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final NotificationDao notificationDao;
    private final List<LibraryNotification> notifications = new ArrayList<>();
    private int nextId = 1;
    private boolean databaseMode;

    public NotificationService(DatabaseManager databaseManager) {
        this.notificationDao = new NotificationDao(databaseManager);
        notifications.add(new LibraryNotification(nextId++, 1, "System health", "MySQL mode is supported. Demo data is active until database credentials are configured.", "INFO", false, LocalDateTime.now().minusHours(2)));
        notifications.add(new LibraryNotification(nextId++, 2, "Overdue books", "One member has an overdue database book. Send a reminder today.", "WARNING", false, LocalDateTime.now().minusMinutes(45)));
        notifications.add(new LibraryNotification(nextId++, 3, "Reservation update", "Database System Concepts is in your reservation queue.", "INFO", false, LocalDateTime.now().minusMinutes(22)));
        try {
            List<LibraryNotification> databaseNotifications = notificationDao.findAll();
            notifications.clear();
            notifications.addAll(databaseNotifications);
            databaseMode = true;
        } catch (SQLException ignored) {
            databaseMode = false;
        }
    }

    public List<LibraryNotification> forUser(User user) {
        if (user.getRole().name().equals("ADMIN")) return new ArrayList<>(notifications);
        return notifications.stream().filter(n -> n.getUserId() == user.getId() || n.getUserId() == 0).toList();
    }

    public int unreadFor(User user) {
        return (int) forUser(user).stream().filter(n -> !n.isRead()).count();
    }

    public void notify(User user, String title, String message, String type) {
        LibraryNotification notification = new LibraryNotification(nextId++, user.getId(), title, message, type, false, LocalDateTime.now());
        if (databaseMode) {
            try {
                notification = notificationDao.save(user.getId(), title, message, type);
            } catch (SQLException ex) {
                throw new IllegalStateException("Database insert failed for notifications.", ex);
            }
        }
        notifications.add(0, notification);
    }

    public void broadcast(String title, String message, String type) {
        LibraryNotification notification = new LibraryNotification(nextId++, 0, title, message, type, false, LocalDateTime.now());
        if (databaseMode) {
            try {
                notification = notificationDao.save(null, title, message, type);
            } catch (SQLException ex) {
                throw new IllegalStateException("Database insert failed for broadcast notification.", ex);
            }
        }
        notifications.add(0, notification);
    }

    public boolean isDatabaseMode() {
        return databaseMode;
    }
}
