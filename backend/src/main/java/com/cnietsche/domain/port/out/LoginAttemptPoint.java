package com.cnietsche.domain.port.out;

import com.cnietsche.domain.model.LoginAttemptOutcome;

import java.time.LocalDateTime;

public record LoginAttemptPoint(LocalDateTime occurredAt, LoginAttemptOutcome outcome) {
}
