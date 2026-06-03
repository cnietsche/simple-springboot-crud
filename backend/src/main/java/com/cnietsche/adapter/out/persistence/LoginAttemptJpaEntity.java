package com.cnietsche.adapter.out.persistence;

import com.cnietsche.domain.model.LoginAttemptOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "login_attempts")
public class LoginAttemptJpaEntity {

    @Id
    private UUID id;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginAttemptOutcome outcome;

    protected LoginAttemptJpaEntity() {
    }

    public LoginAttemptJpaEntity(UUID id, LocalDateTime occurredAt, LoginAttemptOutcome outcome) {
        this.id = id;
        this.occurredAt = occurredAt;
        this.outcome = outcome;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public LoginAttemptOutcome getOutcome() {
        return outcome;
    }
}
