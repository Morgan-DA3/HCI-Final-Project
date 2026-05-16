package com.smartlibrary.service;

import com.smartlibrary.dao.DatabaseManager;
import com.smartlibrary.dao.UserDao;
import com.smartlibrary.model.Role;
import com.smartlibrary.model.User;
import com.smartlibrary.util.PasswordUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserDao userDao;
    private final List<User> users = new ArrayList<>();
    private int nextId = 10;
    private boolean databaseMode;

    public UserService(DatabaseManager databaseManager) {
        this.userDao = new UserDao(databaseManager);
        seedUsers();
        try {
            List<User> databaseUsers = userDao.findAll();
            if (!databaseUsers.isEmpty()) {
                users.clear();
                users.addAll(databaseUsers);
                databaseMode = true;
            }
        } catch (SQLException ignored) {
            // Demo mode remains available when MySQL has not been configured yet.
            databaseMode = false;
        }
    }

    public Optional<User> authenticate(String email, String password) {
        return users.stream()
                .filter(User::isActive)
                .filter(user -> user.getEmail().equalsIgnoreCase(email.trim()))
                .filter(user -> PasswordUtil.matches(password, user.getPasswordHash()))
                .findFirst();
    }

    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    public List<User> members() {
        return users.stream().filter(user -> user.getRole() == Role.MEMBER).toList();
    }

    public User createUser(String name, String email, String password, Role role) {
        validateUser(name, email, password);
        users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findAny()
                .ifPresent(user -> { throw new IllegalArgumentException("This email is already registered."); });
        String passwordHash = PasswordUtil.hash(password);
        User user;
        if (databaseMode) {
            try {
                user = userDao.save(name, email, passwordHash, role);
            } catch (SQLException ex) {
                throw new IllegalStateException("Database insert failed. Check MySQL connection and the users table constraints.", ex);
            }
        } else {
            user = new User(nextId++, name, email, passwordHash, role, true, LocalDateTime.now());
        }
        users.add(user);
        return user;
    }

    public boolean isDatabaseMode() {
        return databaseMode;
    }

    public void toggleActive(User user) {
        user.setActive(!user.isActive());
        if (databaseMode) {
            try {
                userDao.updateActive(user.getId(), user.isActive());
            } catch (SQLException ex) {
                user.setActive(!user.isActive());
                throw new IllegalStateException("Database update failed for user status.", ex);
            }
        }
    }

    public long countActiveUsers() {
        return users.stream().filter(User::isActive).count();
    }

    private void validateUser(String name, String email, String password) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Full name is required.");
        if (email == null || !email.contains("@")) throw new IllegalArgumentException("A valid email is required.");
        if (password == null || password.length() < 4) throw new IllegalArgumentException("Password must contain at least 4 characters.");
    }

    private void seedUsers() {
        users.add(new User(1, "Admin Sofia", "admin@library.edu", PasswordUtil.hash("admin123"), Role.ADMIN, true, LocalDateTime.now().minusMonths(6)));
        users.add(new User(2, "Librarian Omar", "librarian@library.edu", PasswordUtil.hash("lib123"), Role.LIBRARIAN, true, LocalDateTime.now().minusMonths(4)));
        users.add(new User(3, "Student Lina", "student@library.edu", PasswordUtil.hash("student123"), Role.MEMBER, true, LocalDateTime.now().minusMonths(2)));
        users.add(new User(4, "Yassine Bennani", "yassine@student.edu", PasswordUtil.hash("student123"), Role.MEMBER, true, LocalDateTime.now().minusWeeks(8)));
        users.add(new User(5, "Nora El Fassi", "nora@student.edu", PasswordUtil.hash("student123"), Role.MEMBER, true, LocalDateTime.now().minusWeeks(5)));
    }
}
