package com.smartlibrary.util;

import com.smartlibrary.model.Role;
import com.smartlibrary.model.User;

import java.util.Optional;

public class SessionManager {
    private User currentUser;

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public Optional<User> currentUser() {
        return Optional.ofNullable(currentUser);
    }

    public boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public boolean canManageUsers() {
        return hasRole(Role.ADMIN);
    }

    public boolean canManageBooks() {
        return hasRole(Role.ADMIN) || hasRole(Role.LIBRARIAN);
    }

    public boolean canBorrowReturn() {
        return hasRole(Role.ADMIN) || hasRole(Role.LIBRARIAN);
    }
}
