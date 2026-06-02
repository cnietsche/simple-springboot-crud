package com.cnietsche.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "overloads")
public class OverloadJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @Column(name = "payload", nullable = false, length = 255)
    private String value;

    protected OverloadJpaEntity() {
    }

    public OverloadJpaEntity(UUID id, LocalDateTime date, UserJpaEntity user, String value) {
        this.id = id;
        this.date = date;
        this.user = user;
        this.value = value;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public UserJpaEntity getUser() {
        return user;
    }

    public String getValue() {
        return value;
    }
}
