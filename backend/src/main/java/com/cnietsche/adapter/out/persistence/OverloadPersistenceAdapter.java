package com.cnietsche.adapter.out.persistence;

import com.cnietsche.domain.model.Overload;
import com.cnietsche.domain.port.in.OverloadPageView;
import com.cnietsche.domain.port.in.OverloadView;
import com.cnietsche.domain.port.in.UserOverloadCountView;
import com.cnietsche.domain.port.out.OverloadRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class OverloadPersistenceAdapter implements OverloadRepositoryPort {

    private static final int BATCH_SIZE = 100;

    private final SpringDataOverloadRepository repository;
    private final SpringDataUserRepository userRepository;

    public OverloadPersistenceAdapter(
            SpringDataOverloadRepository repository,
            SpringDataUserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public void saveAll(List<Overload> overloads) {
        for (int i = 0; i < overloads.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, overloads.size());
            List<OverloadJpaEntity> batch = new ArrayList<>();
            for (int j = i; j < end; j++) {
                batch.add(toEntity(overloads.get(j)));
            }
            repository.saveAll(batch);
        }
    }

    @Override
    public OverloadPageView findByUserIdOrderByDateDesc(UUID userId, int page, int size) {
        Page<OverloadJpaEntity> result = repository.findByUser_IdOrderByDateDesc(
                userId, PageRequest.of(page, size));
        List<OverloadView> content = result.getContent().stream()
                .map(this::toView)
                .toList();
        return new OverloadPageView(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Override
    public List<UserOverloadCountView> countByUserBetween(
            LocalDateTime from, LocalDateTime to, UUID userId, int limit) {
        return repository.countByUserBetween(from, to, userId).stream()
                .limit(limit)
                .map(p -> new UserOverloadCountView(p.getUserId(), p.getUserName(), p.getCnt()))
                .toList();
    }

    @Override
    public List<LocalDateTime> findDatesBetween(LocalDateTime from, LocalDateTime to, UUID userId) {
        return repository.findDatesBetween(from, to, userId);
    }

    private OverloadJpaEntity toEntity(Overload overload) {
        UserJpaEntity user = userRepository.findById(overload.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + overload.getUserId()));
        return new OverloadJpaEntity(
                overload.getId(),
                overload.getDate(),
                user,
                overload.getValue()
        );
    }

    private OverloadView toView(OverloadJpaEntity entity) {
        return new OverloadView(
                entity.getId(),
                entity.getDate(),
                entity.getUser().getId(),
                entity.getValue()
        );
    }
}
