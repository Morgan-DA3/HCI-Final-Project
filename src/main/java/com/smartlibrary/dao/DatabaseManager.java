package com.smartlibrary.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private final String url;
    private final String username;
    private final String password;

    public DatabaseManager() {
        this.url = System.getProperty("smartlibrary.db.url",
                "jdbc:mysql://localhost:3306/smart_library?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        this.username = System.getProperty("smartlibrary.db.user", "root");
        this.password = System.getProperty("smartlibrary.db.password", "");
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public boolean canConnect() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    public String getUrl() {
        return url;
    }
}
