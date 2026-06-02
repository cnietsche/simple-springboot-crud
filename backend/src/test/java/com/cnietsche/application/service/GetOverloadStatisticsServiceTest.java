package com.cnietsche.application.service;

import com.cnietsche.domain.model.StatisticsPeriod;
import com.cnietsche.domain.port.in.OverloadStatisticsView;
import com.cnietsche.domain.port.in.TimeSeriesBucketView;
import com.cnietsche.domain.port.in.UserOverloadCountView;
import com.cnietsche.domain.port.out.OverloadRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOverloadStatisticsServiceTest {

    @Mock
    private OverloadRepositoryPort overloadRepository;

    private GetOverloadStatisticsService service;

    @BeforeEach
    void setUp() {
        service = new GetOverloadStatisticsService(overloadRepository);
    }

    @Test
    void shouldReturnStatistics() {
        UUID userId = UUID.randomUUID();
        var topUsers = List.of(new UserOverloadCountView(userId, "John", 10L));
        var timeSeries = List.of(new TimeSeriesBucketView(LocalDateTime.now(), 5L));

        when(overloadRepository.countByUserBetween(any(), any(), eq(null), eq(5)))
                .thenReturn(topUsers);
        when(overloadRepository.countByTimeBuckets(any(), any(), eq(null)))
                .thenReturn(timeSeries);

        OverloadStatisticsView result = service.execute(StatisticsPeriod.LAST_HOUR, null);

        assertThat(result.topUsers()).hasSize(1);
        assertThat(result.timeSeries()).hasSize(1);
        verify(overloadRepository).countByUserBetween(any(), any(), eq(null), eq(5));
    }
}
