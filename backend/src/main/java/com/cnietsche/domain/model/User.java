package com.cnietsche.domain.model;

import java.util.UUID;

public class User {

    private final UUID id;
    private final String name;
    private final String email;
    private final String username;
    private final String passwordHash;
    private final UserType type;

    public User(UUID id, String name, String email, String username, String passwordHash, UserType type) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserType getType() {
        return type;
    }

    public User withType(UserType newType) {
        return new User(id, name, email, username, passwordHash, newType);
    }
}
