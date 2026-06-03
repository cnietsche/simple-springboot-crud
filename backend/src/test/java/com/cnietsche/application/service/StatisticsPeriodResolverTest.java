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
    void shouldResolveLastMonthAsRollingWindow() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 15, 10, 0);
        LocalDateTime start = StatisticsPeriodResolver.resolveStart(StatisticsPeriod.LAST_MONTH, now);
        assertThat(start).isEqualTo(now.minusMonths(1));
    }

    @Test
    void shouldResolveLastWeekAsRollingWindow() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 15, 10, 0);
        LocalDateTime start = StatisticsPeriodResolver.resolveStart(StatisticsPeriod.LAST_WEEK, now);
        assertThat(start).isEqualTo(now.minusDays(7));
    }

    @Test
    void shouldResolveLastYearAsRollingWindow() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 15, 10, 0);
        LocalDateTime start = StatisticsPeriodResolver.resolveStart(StatisticsPeriod.LAST_YEAR, now);
        assertThat(start).isEqualTo(now.minusYears(1));
    }
}
