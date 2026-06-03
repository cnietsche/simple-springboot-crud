package com.cnietsche.domain.model;

/**
 * Time window for dashboards and statistics aggregations (shared across features).
 */
public enum StatisticsPeriod {
    LAST_30_MINUTES,
    LAST_HOUR,
    LAST_12_HOURS,
    LAST_DAY,
    LAST_WEEK,
    LAST_MONTH,
    LAST_YEAR
}
