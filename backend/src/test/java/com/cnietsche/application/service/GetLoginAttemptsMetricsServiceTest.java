package com.cnietsche.application.service;

import com.cnietsche.domain.model.LoginAttemptOutcome;
import com.cnietsche.domain.model.StatisticsPeriod;
import com.cnietsche.domain.port.in.LoginAttemptsBucketView;
import com.cnietsche.domain.port.out.LoginAttemptPoint;
import com.cnietsche.domain.port.out.LoginAttemptRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

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
    void shouldReturnBucketsWithZerosWhenNoAttempts() {
        when(loginAttemptRepository.findBetween(any(), any())).thenReturn(List.of());

        List<LoginAttemptsBucketView> result = service.execute(StatisticsPeriod.LAST_HOUR);

        assertThat(result).hasSize(6);
        assertThat(result).allMatch(b -> b.success() == 0 && b.fail() == 0);
        verify(loginAttemptRepository).findBetween(fromCaptor.capture(), toCaptor.capture());
        assertThat(toCaptor.getValue()).isAfter(fromCaptor.getValue());
    }

    @Test
    void shouldAggregateAttemptsIntoBuckets() {
        when(loginAttemptRepository.findBetween(any(), any())).thenAnswer(invocation -> {
            LocalDateTime from = invocation.getArgument(0);
            return List.of(
                    new LoginAttemptPoint(from.plusMinutes(2), LoginAttemptOutcome.SUCCESS),
                    new LoginAttemptPoint(from.plusMinutes(2), LoginAttemptOutcome.FAIL),
                    new LoginAttemptPoint(from.plusMinutes(8), LoginAttemptOutcome.SUCCESS)
            );
        });

        List<LoginAttemptsBucketView> result = service.execute(StatisticsPeriod.LAST_30_MINUTES);

        assertThat(result).hasSize(6);
        assertThat(result.getFirst().success()).isEqualTo(1);
        assertThat(result.getFirst().fail()).isEqualTo(1);
        assertThat(result.get(1).success()).isEqualTo(1);
        assertThat(result.get(1).fail()).isZero();
    }
}
