package com.cnietsche.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Overload {

    private final UUID id;
    private final LocalDateTime date;
    private final UUID userId;
    private final String value;

    public Overload(UUID id, LocalDateTime date, UUID userId, String value) {
        this.id = id;
        this.date = date;
        this.userId = userId;
        this.value = value;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getValue() {
        return value;
    }
}
