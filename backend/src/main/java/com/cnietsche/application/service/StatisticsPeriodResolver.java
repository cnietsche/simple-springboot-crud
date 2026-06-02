package com.cnietsche.application.service;

import com.cnietsche.domain.model.StatisticsPeriod;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public final class StatisticsPeriodResolver {

    private StatisticsPeriodResolver() {
    }

    public static LocalDateTime resolveStart(StatisticsPeriod period, LocalDateTime now) {
        return switch (period) {
            case LAST_30_MINUTES -> now.minusMinutes(30);
            case LAST_HOUR -> now.minusHours(1);
            case LAST_12_HOURS -> now.minusHours(12);
            case LAST_DAY -> now.minusDays(1);
            case THIS_WEEK -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .toLocalDate().atStartOfDay();
            case THIS_MONTH -> LocalDate.of(now.getYear(), now.getMonth(), 1).atStartOfDay();
            case THIS_YEAR -> LocalDate.of(now.getYear(), 1, 1).atStartOfDay();
        };
    }
}
