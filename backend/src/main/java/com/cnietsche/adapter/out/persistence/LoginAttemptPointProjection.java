package com.cnietsche.adapter.out.persistence;

import com.cnietsche.domain.model.LoginAttemptOutcome;

import java.time.LocalDateTime;

public interface LoginAttemptPointProjection {

    LocalDateTime getOccurredAt();

    LoginAttemptOutcome getOutcome();
}
