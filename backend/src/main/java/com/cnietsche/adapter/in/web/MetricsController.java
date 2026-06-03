package com.cnietsche.adapter.in.web;

import com.cnietsche.adapter.in.web.dto.LoginAttemptMetricResponse;
import com.cnietsche.domain.model.StatisticsPeriod;
import com.cnietsche.domain.port.in.GetLoginAttemptsMetricsUseCase;
import com.cnietsche.domain.port.in.LoginAttemptsMetricView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final GetLoginAttemptsMetricsUseCase getLoginAttemptsMetricsUseCase;

    public MetricsController(GetLoginAttemptsMetricsUseCase getLoginAttemptsMetricsUseCase) {
        this.getLoginAttemptsMetricsUseCase = getLoginAttemptsMetricsUseCase;
    }

    @GetMapping("/login-attempts")
    public List<LoginAttemptMetricResponse> loginAttempts(@RequestParam StatisticsPeriod period) {
        return getLoginAttemptsMetricsUseCase.execute(period).stream()
                .map(this::toResponse)
                .toList();
    }

    private LoginAttemptMetricResponse toResponse(LoginAttemptsMetricView view) {
        return new LoginAttemptMetricResponse(view.outcome(), view.count());
    }
}
