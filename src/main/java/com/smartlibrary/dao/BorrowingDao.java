package com.smartlibrary.dao;

import com.smartlibrary.model.Borrowing;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowingDao {
    private final DatabaseManager databaseManager;

    public BorrowingDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Borrowing> findAll() throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String sql = """
                SELECT br.id, br.book_id, br.member_id, b.title AS book_title, u.full_name AS member_name,
                       br.borrow_date, br.due_date, br.return_date, br.status
                FROM borrowings br
                JOIN books b ON b.id = br.book_id
                JOIN users u ON u.id = br.member_id
                ORDER BY br.borrow_date DESC, br.id DESC
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                borrowings.add(map(rs));
            }
        }
        return borrowings;
    }

    public Borrowing save(Borrowing borrowing, Integer issuedBy) throws SQLException {
        String sql = """
                INSERT INTO borrowings(book_id, member_id, issued_by, borrow_date, due_date, return_date, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, borrowing.getBookId());
            statement.setInt(2, borrowing.getMemberId());
            if (issuedBy == null) statement.setNull(3, Types.INTEGER);
            else statement.setInt(3, issuedBy);
            statement.setDate(4, Date.valueOf(borrowing.getBorrowDate()));
            statement.setDate(5, Date.valueOf(borrowing.getDueDate()));
            if (borrowing.getReturnDate() == null) statement.setNull(6, Types.DATE);
            else statement.setDate(6, Date.valueOf(borrowing.getReturnDate()));
            statement.setString(7, borrowing.getStatus());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Borrowing(keys.getInt(1), borrowing.getBookId(), borrowing.getMemberId(),
                            borrowing.getBookTitle(), borrowing.getMemberName(), borrowing.getBorrowDate(),
                            borrowing.getDueDate(), borrowing.getReturnDate(), borrowing.getStatus());
                }
            }
        }
        throw new SQLException("Borrowing was inserted, but no generated id was returned.");
    }

    public void markReturned(Borrowing borrowing) throws SQLException {
        String sql = "UPDATE borrowings SET return_date=?, status=? WHERE id=?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(borrowing.getReturnDate()));
            statement.setString(2, borrowing.getStatus());
            statement.setInt(3, borrowing.getId());
            statement.executeUpdate();
        }
    }

    private Borrowing map(ResultSet rs) throws SQLException {
        Date returnDate = rs.getDate("return_date");
        return new Borrowing(
                rs.getInt("id"),
                rs.getInt("book_id"),
                rs.getInt("member_id"),
                rs.getString("book_title"),
                rs.getString("member_name"),
                rs.getDate("borrow_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                returnDate == null ? null : returnDate.toLocalDate(),
                rs.getString("status")
        );
    }
}
