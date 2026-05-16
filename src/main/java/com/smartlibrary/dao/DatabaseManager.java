package com.smartlibrary.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

public class DatabaseManager {
    private final String url;
    private final String username;
    private final String password;
    private String lastError = "";

    public DatabaseManager() {
        Properties properties = loadProperties();
        this.url = System.getProperty("smartlibrary.db.url",
                properties.getProperty("db.url", "jdbc:mysql://localhost:3306/smart_library?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"));
        this.username = System.getProperty("smartlibrary.db.user", properties.getProperty("db.user", "root"));
        this.password = System.getProperty("smartlibrary.db.password", properties.getProperty("db.password", ""));
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public boolean canConnect() {
        try (Connection ignored = getConnection()) {
            lastError = "";
            return true;
        } catch (SQLException ex) {
            lastError = ex.getMessage();
            return false;
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public Optional<String> getLastError() {
        return lastError == null || lastError.isBlank() ? Optional.empty() : Optional.of(lastError);
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseManager.class.getResourceAsStream("/database.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException ignored) {
            // System properties and defaults are still available.
        }
        return properties;
    }
}
