package com.cnietsche.domain.port.out;

import com.cnietsche.domain.model.LoginAttemptOutcome;

import java.time.LocalDateTime;
import java.util.Map;

public interface LoginAttemptRepositoryPort {

    void save(LoginAttemptOutcome outcome, LocalDateTime occurredAt);

    Map<LoginAttemptOutcome, Long> countByOutcomeBetween(LocalDateTime from, LocalDateTime to);
}
