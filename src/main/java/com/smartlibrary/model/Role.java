package com.smartlibrary.model;

public enum Role {
    ADMIN,
    LIBRARIAN,
    MEMBER;

    public String displayName() {
        return switch (this) {
            case ADMIN -> "Administrator";
            case LIBRARIAN -> "Librarian";
            case MEMBER -> "Member / Student";
        };
    }
}
