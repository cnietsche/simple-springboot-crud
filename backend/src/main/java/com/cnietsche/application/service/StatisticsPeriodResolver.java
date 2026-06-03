package com.cnietsche.application.service;

import com.cnietsche.domain.model.StatisticsPeriod;

import java.time.LocalDateTime;

public final class StatisticsPeriodResolver {

    private StatisticsPeriodResolver() {
    }

    public static LocalDateTime resolveStart(StatisticsPeriod period, LocalDateTime now) {
        return switch (period) {
            case LAST_30_MINUTES -> now.minusMinutes(30);
            case LAST_HOUR -> now.minusHours(1);
            case LAST_12_HOURS -> now.minusHours(12);
            case LAST_DAY -> now.minusDays(1);
            case LAST_WEEK -> now.minusDays(7);
            case LAST_MONTH -> now.minusMonths(1);
            case LAST_YEAR -> now.minusYears(1);
        };
    }
}
