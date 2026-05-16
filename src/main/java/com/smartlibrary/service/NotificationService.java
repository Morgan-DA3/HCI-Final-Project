package com.smartlibrary.service;

import com.smartlibrary.dao.DatabaseManager;
import com.smartlibrary.model.LibraryNotification;
import com.smartlibrary.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final List<LibraryNotification> notifications = new ArrayList<>();
    private int nextId = 1;

    public NotificationService(DatabaseManager ignored) {
        notifications.add(new LibraryNotification(nextId++, 1, "System health", "MySQL mode is supported. Demo data is active until database credentials are configured.", "INFO", false, LocalDateTime.now().minusHours(2)));
        notifications.add(new LibraryNotification(nextId++, 2, "Overdue books", "One member has an overdue database book. Send a reminder today.", "WARNING", false, LocalDateTime.now().minusMinutes(45)));
        notifications.add(new LibraryNotification(nextId++, 3, "Reservation update", "Database System Concepts is in your reservation queue.", "INFO", false, LocalDateTime.now().minusMinutes(22)));
    }

    public List<LibraryNotification> forUser(User user) {
        if (user.getRole().name().equals("ADMIN")) return new ArrayList<>(notifications);
        return notifications.stream().filter(n -> n.getUserId() == user.getId() || n.getUserId() == 0).toList();
    }

    public int unreadFor(User user) {
        return (int) forUser(user).stream().filter(n -> !n.isRead()).count();
    }

    public void notify(User user, String title, String message, String type) {
        notifications.add(0, new LibraryNotification(nextId++, user.getId(), title, message, type, false, LocalDateTime.now()));
    }

    public void broadcast(String title, String message, String type) {
        notifications.add(0, new LibraryNotification(nextId++, 0, title, message, type, false, LocalDateTime.now()));
    }
}
