package com.smartlibrary.model;

import java.time.LocalDateTime;

public class ActivityLog {
    private final int id;
    private final String actor;
    private final String action;
    private final String details;
    private final LocalDateTime createdAt;

    public ActivityLog(int id, String actor, String action, String details, LocalDateTime createdAt) {
        this.id = id;
        this.actor = actor;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
