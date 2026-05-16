package com.smartlibrary.dao;

import com.smartlibrary.model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDao {
    private final DatabaseManager databaseManager;

    public ReservationDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Reservation> findAll() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = """
                SELECT r.id, r.book_id, r.member_id, b.title AS book_title, u.full_name AS member_name,
                       r.reservation_date, r.status
                FROM reservations r
                JOIN books b ON b.id = r.book_id
                JOIN users u ON u.id = r.member_id
                ORDER BY r.reservation_date DESC, r.id DESC
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                reservations.add(new Reservation(
                        rs.getInt("id"),
                        rs.getInt("book_id"),
                        rs.getInt("member_id"),
                        rs.getString("book_title"),
                        rs.getString("member_name"),
                        rs.getDate("reservation_date").toLocalDate(),
                        rs.getString("status")
                ));
            }
        }
        return reservations;
    }

    public Reservation save(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservations(book_id, member_id, reservation_date, status) VALUES (?, ?, ?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, reservation.getBookId());
            statement.setInt(2, reservation.getMemberId());
            statement.setDate(3, Date.valueOf(reservation.getReservationDate()));
            statement.setString(4, reservation.getStatus());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Reservation(keys.getInt(1), reservation.getBookId(), reservation.getMemberId(),
                            reservation.getBookTitle(), reservation.getMemberName(),
                            reservation.getReservationDate(), reservation.getStatus());
                }
            }
        }
        throw new SQLException("Reservation was inserted, but no generated id was returned.");
    }

    public void updateStatus(Reservation reservation) throws SQLException {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE reservations SET status=? WHERE id=?")) {
            statement.setString(1, reservation.getStatus());
            statement.setInt(2, reservation.getId());
            statement.executeUpdate();
        }
    }
}
