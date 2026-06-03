package com.cnietsche.adapter.out.persistence;

import com.cnietsche.domain.model.LoginAttemptOutcome;
import com.cnietsche.domain.port.out.LoginAttemptRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Component
public class LoginAttemptPersistenceAdapter implements LoginAttemptRepositoryPort {

    private final SpringDataLoginAttemptRepository repository;

    public LoginAttemptPersistenceAdapter(SpringDataLoginAttemptRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(LoginAttemptOutcome outcome, LocalDateTime occurredAt) {
        repository.save(new LoginAttemptJpaEntity(UUID.randomUUID(), occurredAt, outcome));
    }

    @Override
    public Map<LoginAttemptOutcome, Long> countByOutcomeBetween(LocalDateTime from, LocalDateTime to) {
        Map<LoginAttemptOutcome, Long> counts = new EnumMap<>(LoginAttemptOutcome.class);
        for (LoginAttemptCountProjection row : repository.countByOutcomeBetween(from, to)) {
            counts.put(row.getOutcome(), row.getCnt());
        }
        return counts;
    }
}
