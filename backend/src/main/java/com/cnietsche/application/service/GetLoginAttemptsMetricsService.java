package com.cnietsche.application.service;

import com.cnietsche.domain.model.StatisticsPeriod;
import com.cnietsche.domain.port.in.GetLoginAttemptsMetricsUseCase;
import com.cnietsche.domain.port.in.LoginAttemptsBucketView;
import com.cnietsche.domain.port.out.LoginAttemptRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GetLoginAttemptsMetricsService implements GetLoginAttemptsMetricsUseCase {

    private final LoginAttemptRepositoryPort loginAttemptRepository;

    public GetLoginAttemptsMetricsService(LoginAttemptRepositoryPort loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    @Override
    public List<LoginAttemptsBucketView> execute(StatisticsPeriod period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = StatisticsPeriodBucketing.resolveQueryFrom(period, now);
        var points = loginAttemptRepository.findBetween(from, now);
        return StatisticsPeriodBucketing.aggregateLoginAttempts(period, now, points);
    }
}
