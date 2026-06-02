package com.cnietsche.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SpringDataOverloadRepository extends JpaRepository<OverloadJpaEntity, UUID> {

    Page<OverloadJpaEntity> findByUser_IdOrderByDateDesc(UUID userId, Pageable pageable);

    @Query("""
            SELECT o.user.id AS userId, o.user.name AS userName, COUNT(o) AS cnt
            FROM OverloadJpaEntity o
            WHERE o.date >= :from AND o.date <= :to
            AND (:userId IS NULL OR o.user.id = :userId)
            GROUP BY o.user.id, o.user.name
            ORDER BY COUNT(o) DESC
            """)
    List<UserOverloadCountProjection> countByUserBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("userId") UUID userId);

    @Query("""
            SELECT o.date FROM OverloadJpaEntity o
            WHERE o.date >= :from AND o.date <= :to
            AND (:userId IS NULL OR o.user.id = :userId)
            """)
    List<LocalDateTime> findDatesBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("userId") UUID userId);
}
