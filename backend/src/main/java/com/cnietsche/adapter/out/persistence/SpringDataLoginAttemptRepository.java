package com.cnietsche.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SpringDataLoginAttemptRepository extends JpaRepository<LoginAttemptJpaEntity, UUID> {

    @Query("""
            SELECT l.outcome AS outcome, COUNT(l) AS cnt
            FROM LoginAttemptJpaEntity l
            WHERE l.occurredAt >= :from AND l.occurredAt <= :to
            GROUP BY l.outcome
            """)
    List<LoginAttemptCountProjection> countByOutcomeBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
