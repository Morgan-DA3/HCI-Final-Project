package com.smartlibrary.model;

public record DashboardStats(
        int totalBooks,
        int borrowedBooks,
        int overdueBooks,
        int activeUsers,
        int todayBorrowings,
        int pendingReturns,
        int unreadNotifications,
        int reservations
) {}
