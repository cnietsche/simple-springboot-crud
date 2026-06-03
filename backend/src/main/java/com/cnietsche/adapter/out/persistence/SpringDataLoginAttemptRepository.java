package com.cnietsche.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SpringDataLoginAttemptRepository extends JpaRepository<LoginAttemptJpaEntity, UUID> {

    @Query("""
            SELECT l.occurredAt AS occurredAt, l.outcome AS outcome
            FROM LoginAttemptJpaEntity l
            WHERE l.occurredAt >= :from AND l.occurredAt <= :to
            """)
    List<LoginAttemptPointProjection> findPointsBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
