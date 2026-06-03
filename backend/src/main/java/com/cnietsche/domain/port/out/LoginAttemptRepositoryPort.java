package com.cnietsche.domain.port.out;

import com.cnietsche.domain.model.LoginAttemptOutcome;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginAttemptRepositoryPort {

    void save(LoginAttemptOutcome outcome, LocalDateTime occurredAt);

    List<LoginAttemptPoint> findBetween(LocalDateTime from, LocalDateTime to);
}
