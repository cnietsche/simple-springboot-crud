package com.cnietsche.application.service;

import com.cnietsche.domain.model.LoginAttemptOutcome;
import com.cnietsche.domain.model.StatisticsPeriod;
import com.cnietsche.domain.port.in.LoginAttemptsMetricView;
import com.cnietsche.domain.port.out.LoginAttemptRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetLoginAttemptsMetricsServiceTest {

    @Mock
    private LoginAttemptRepositoryPort loginAttemptRepository;

    @Captor
    private ArgumentCaptor<LocalDateTime> fromCaptor;

    @Captor
    private ArgumentCaptor<LocalDateTime> toCaptor;

    private GetLoginAttemptsMetricsService service;

    @BeforeEach
    void setUp() {
        service = new GetLoginAttemptsMetricsService(loginAttemptRepository);
    }

    @Test
    void shouldReturnSuccessAndFailWithZeroWhenNoAttempts() {
        when(loginAttemptRepository.countByOutcomeBetween(any(), any())).thenReturn(Map.of());

        List<LoginAttemptsMetricView> result = service.execute(StatisticsPeriod.LAST_HOUR);

        assertThat(result).containsExactly(
                new LoginAttemptsMetricView("Success", 0L),
                new LoginAttemptsMetricView("Fail", 0L)
        );
        verify(loginAttemptRepository).countByOutcomeBetween(fromCaptor.capture(), toCaptor.capture());
        assertThat(toCaptor.getValue()).isAfter(fromCaptor.getValue());
    }

    @Test
    void shouldMapPartialCountsAndDefaultMissingOutcomesToZero() {
        Map<LoginAttemptOutcome, Long> counts = new EnumMap<>(LoginAttemptOutcome.class);
        counts.put(LoginAttemptOutcome.SUCCESS, 7L);
        when(loginAttemptRepository.countByOutcomeBetween(any(), any())).thenReturn(counts);

        List<LoginAttemptsMetricView> result = service.execute(StatisticsPeriod.LAST_30_MINUTES);

        assertThat(result).containsExactly(
                new LoginAttemptsMetricView("Success", 7L),
                new LoginAttemptsMetricView("Fail", 0L)
        );
    }

    @Test
    void shouldReturnBothOutcomesWhenPresent() {
        Map<LoginAttemptOutcome, Long> counts = new EnumMap<>(LoginAttemptOutcome.class);
        counts.put(LoginAttemptOutcome.SUCCESS, 12L);
        counts.put(LoginAttemptOutcome.FAIL, 4L);
        when(loginAttemptRepository.countByOutcomeBetween(any(), any())).thenReturn(counts);

        List<LoginAttemptsMetricView> result = service.execute(StatisticsPeriod.THIS_WEEK);

        assertThat(result).containsExactly(
                new LoginAttemptsMetricView("Success", 12L),
                new LoginAttemptsMetricView("Fail", 4L)
        );
    }
}
