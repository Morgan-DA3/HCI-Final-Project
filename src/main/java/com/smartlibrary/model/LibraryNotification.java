package com.smartlibrary.model;

import java.time.LocalDateTime;

public class LibraryNotification {
    private final int id;
    private final int userId;
    private final String title;
    private final String message;
    private final String type;
    private boolean read;
    private final LocalDateTime createdAt;

    public LibraryNotification(int id, int userId, String title, String message, String type, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = read;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
