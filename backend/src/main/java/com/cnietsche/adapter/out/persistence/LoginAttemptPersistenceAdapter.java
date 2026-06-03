package com.cnietsche.adapter.out.persistence;

import com.cnietsche.domain.model.LoginAttemptOutcome;
import com.cnietsche.domain.port.out.LoginAttemptPoint;
import com.cnietsche.domain.port.out.LoginAttemptRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
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
    public List<LoginAttemptPoint> findBetween(LocalDateTime from, LocalDateTime to) {
        return repository.findPointsBetween(from, to).stream()
                .map(p -> new LoginAttemptPoint(p.getOccurredAt(), p.getOutcome()))
                .toList();
    }
}
