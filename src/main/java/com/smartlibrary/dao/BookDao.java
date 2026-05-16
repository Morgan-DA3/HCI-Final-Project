package com.smartlibrary.dao;

import com.smartlibrary.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDao {
    private final DatabaseManager databaseManager;

    public BookDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Book> findAll() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = """
                SELECT id, title, author, isbn, category_name, publication_year, quantity,
                       available_copies, shelf_location, status, cover_image_path
                FROM books ORDER BY title
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                books.add(mapBook(rs));
            }
        }
        return books;
    }

    public void save(Book book) throws SQLException {
        String sql = """
                INSERT INTO books(title, author, isbn, category_name, publication_year, quantity,
                                  available_copies, shelf_location, status, cover_image_path)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindBook(statement, book);
            statement.executeUpdate();
        }
    }

    public void update(Book book) throws SQLException {
        String sql = """
                UPDATE books SET title=?, author=?, isbn=?, category_name=?, publication_year=?,
                                 quantity=?, available_copies=?, shelf_location=?, status=?, cover_image_path=?
                WHERE id=?
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindBook(statement, book);
            statement.setInt(11, book.getId());
            statement.executeUpdate();
        }
    }

    private void bindBook(PreparedStatement statement, Book book) throws SQLException {
        statement.setString(1, book.getTitle());
        statement.setString(2, book.getAuthor());
        statement.setString(3, book.getIsbn());
        statement.setString(4, book.getCategory());
        statement.setInt(5, book.getPublicationYear());
        statement.setInt(6, book.getQuantity());
        statement.setInt(7, book.getAvailableCopies());
        statement.setString(8, book.getShelfLocation());
        statement.setString(9, book.getStatus());
        statement.setString(10, book.getCoverImagePath());
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("isbn"),
                rs.getString("category_name"),
                rs.getInt("publication_year"),
                rs.getInt("quantity"),
                rs.getInt("available_copies"),
                rs.getString("shelf_location"),
                rs.getString("status"),
                rs.getString("cover_image_path")
        );
    }
}
