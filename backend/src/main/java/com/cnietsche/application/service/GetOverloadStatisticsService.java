package com.cnietsche.application.service;

import com.cnietsche.domain.model.StatisticsPeriod;
import com.cnietsche.domain.port.in.GetOverloadStatisticsUseCase;
import com.cnietsche.domain.port.in.OverloadStatisticsView;
import com.cnietsche.domain.port.out.OverloadRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GetOverloadStatisticsService implements GetOverloadStatisticsUseCase {

    private static final int TOP_USERS_LIMIT = 5;

    private final OverloadRepositoryPort overloadRepository;

    public GetOverloadStatisticsService(OverloadRepositoryPort overloadRepository) {
        this.overloadRepository = overloadRepository;
    }

    @Override
    public OverloadStatisticsView execute(StatisticsPeriod period, UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = StatisticsPeriodResolver.resolveStart(period, now);

        var topUsers = overloadRepository.countByUserBetween(from, now, userId, TOP_USERS_LIMIT);
        var timeSeries = overloadRepository.countByTimeBuckets(from, now, userId);

        return new OverloadStatisticsView(topUsers, timeSeries);
    }
}
