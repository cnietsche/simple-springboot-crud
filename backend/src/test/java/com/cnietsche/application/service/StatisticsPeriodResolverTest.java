package com.cnietsche.application.service;

import com.cnietsche.domain.model.StatisticsPeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticsPeriodResolverTest {

    @Test
    void shouldResolveLast30Minutes() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 2, 12, 0);
        LocalDateTime start = StatisticsPeriodResolver.resolveStart(StatisticsPeriod.LAST_30_MINUTES, now);
        assertThat(start).isEqualTo(now.minusMinutes(30));
    }

    @Test
    void shouldResolveThisMonth() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 15, 10, 0);
        LocalDateTime start = StatisticsPeriodResolver.resolveStart(StatisticsPeriod.THIS_MONTH, now);
        assertThat(start).isEqualTo(LocalDateTime.of(2026, 6, 1, 0, 0));
    }
}
