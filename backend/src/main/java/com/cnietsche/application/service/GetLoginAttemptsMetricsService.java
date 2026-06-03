package com.cnietsche.application.service;

import com.cnietsche.domain.model.LoginAttemptOutcome;
import com.cnietsche.domain.model.StatisticsPeriod;
import com.cnietsche.domain.port.in.GetLoginAttemptsMetricsUseCase;
import com.cnietsche.domain.port.in.LoginAttemptsMetricView;
import com.cnietsche.domain.port.out.LoginAttemptRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class GetLoginAttemptsMetricsService implements GetLoginAttemptsMetricsUseCase {

    private static final String SUCCESS_LABEL = "Success";
    private static final String FAIL_LABEL = "Fail";

    private final LoginAttemptRepositoryPort loginAttemptRepository;

    public GetLoginAttemptsMetricsService(LoginAttemptRepositoryPort loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    @Override
    public List<LoginAttemptsMetricView> execute(StatisticsPeriod period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = StatisticsPeriodResolver.resolveStart(period, now);
        Map<LoginAttemptOutcome, Long> counts = loginAttemptRepository.countByOutcomeBetween(from, now);

        return List.of(
                new LoginAttemptsMetricView(SUCCESS_LABEL, counts.getOrDefault(LoginAttemptOutcome.SUCCESS, 0L)),
                new LoginAttemptsMetricView(FAIL_LABEL, counts.getOrDefault(LoginAttemptOutcome.FAIL, 0L))
        );
    }
}
