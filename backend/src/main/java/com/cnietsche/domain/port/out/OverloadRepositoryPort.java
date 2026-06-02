package com.cnietsche.domain.port.out;

import com.cnietsche.domain.model.Overload;
import com.cnietsche.domain.port.in.OverloadPageView;
import com.cnietsche.domain.port.in.TimeSeriesBucketView;
import com.cnietsche.domain.port.in.UserOverloadCountView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OverloadRepositoryPort {

    void saveAll(List<Overload> overloads);

    OverloadPageView findByUserIdOrderByDateDesc(UUID userId, int page, int size);

    List<UserOverloadCountView> countByUserBetween(LocalDateTime from, LocalDateTime to, UUID userId, int limit);

    List<TimeSeriesBucketView> countByTimeBuckets(LocalDateTime from, LocalDateTime to, UUID userId);
}
