package com.cnietsche.application.service;

import com.cnietsche.domain.model.LoginAttemptOutcome;
import com.cnietsche.domain.model.StatisticsPeriod;
import com.cnietsche.domain.port.in.LoginAttemptsBucketView;
import com.cnietsche.domain.port.in.TimeSeriesBucketView;
import com.cnietsche.domain.port.out.LoginAttemptPoint;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticsPeriodBucketingTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 3, 12, 0);

    @Test
    void shouldGenerateSixAlignedBucketsForLast30Minutes() {
        List<LocalDateTime> starts = StatisticsPeriodBucketing.generateBucketStarts(StatisticsPeriod.LAST_30_MINUTES, NOW);

        assertThat(starts).hasSize(6);
        assertThat(starts.getFirst()).isEqualTo(LocalDateTime.of(2026, 6, 3, 11, 30));
        assertThat(starts.getLast()).isEqualTo(LocalDateTime.of(2026, 6, 3, 11, 55));
    }

    @Test
    void shouldCeilEndForBrokenMinuteLast30Minutes() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 3, 15, 27);

        List<LocalDateTime> starts = StatisticsPeriodBucketing.generateBucketStarts(StatisticsPeriod.LAST_30_MINUTES, now);

        assertThat(starts).hasSize(6);
        assertThat(starts.getFirst()).isEqualTo(LocalDateTime.of(2026, 6, 3, 15, 0));
        assertThat(starts.getLast()).isEqualTo(LocalDateTime.of(2026, 6, 3, 15, 25));
        assertThat(StatisticsPeriodBucketing.resolveQueryFrom(StatisticsPeriod.LAST_30_MINUTES, now))
                .isEqualTo(LocalDateTime.of(2026, 6, 3, 15, 0));
    }

    @Test
    void shouldCeilEndForTwoHourBucketsOnLastDay() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 3, 15, 27);

        List<LocalDateTime> starts = StatisticsPeriodBucketing.generateBucketStarts(StatisticsPeriod.LAST_DAY, now);

        assertThat(starts).hasSize(12);
        assertThat(starts.getLast()).isEqualTo(LocalDateTime.of(2026, 6, 3, 14, 0));
        assertThat(starts.getFirst()).isEqualTo(LocalDateTime.of(2026, 6, 2, 16, 0));
    }

    @Test
    void shouldCeilEndForLast12HoursToNextHour() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 3, 15, 27);

        List<LocalDateTime> starts = StatisticsPeriodBucketing.generateBucketStarts(StatisticsPeriod.LAST_12_HOURS, now);

        assertThat(starts).hasSize(12);
        assertThat(starts.getLast()).isEqualTo(LocalDateTime.of(2026, 6, 3, 15, 0));
        assertThat(starts.getFirst()).isEqualTo(LocalDateTime.of(2026, 6, 3, 4, 0));
    }

    @Test
    void shouldGenerateTwelveMonthlyBucketsForLastYear() {
        List<LocalDateTime> starts = StatisticsPeriodBucketing.generateBucketStarts(StatisticsPeriod.LAST_YEAR, NOW);

        assertThat(starts).hasSize(12);
        assertThat(starts.getFirst()).isEqualTo(LocalDateTime.of(2025, 7, 1, 0, 0));
        assertThat(starts.getLast()).isEqualTo(LocalDateTime.of(2026, 6, 1, 0, 0));
    }

    @Test
    void shouldAggregateLoginAttemptsPerBucket() {
        LocalDateTime from = StatisticsPeriodBucketing.resolveQueryFrom(StatisticsPeriod.LAST_30_MINUTES, NOW);
        List<LoginAttemptPoint> points = List.of(
                new LoginAttemptPoint(from.plusMinutes(2), LoginAttemptOutcome.SUCCESS),
                new LoginAttemptPoint(from.plusMinutes(2), LoginAttemptOutcome.FAIL),
                new LoginAttemptPoint(from.plusMinutes(8), LoginAttemptOutcome.SUCCESS)
        );

        List<LoginAttemptsBucketView> buckets =
                StatisticsPeriodBucketing.aggregateLoginAttempts(StatisticsPeriod.LAST_30_MINUTES, NOW, points);

        assertThat(buckets).hasSize(6);
        assertThat(buckets.getFirst().success()).isEqualTo(1);
        assertThat(buckets.getFirst().fail()).isEqualTo(1);
        assertThat(buckets.get(1).success()).isEqualTo(1);
        assertThat(buckets.get(1).fail()).isZero();
        assertThat(buckets.get(2).success()).isZero();
    }

    @Test
    void shouldReturnZeroFilledBucketsWhenNoEvents() {
        List<LoginAttemptsBucketView> buckets =
                StatisticsPeriodBucketing.aggregateLoginAttempts(StatisticsPeriod.LAST_HOUR, NOW, List.of());

        assertThat(buckets).hasSize(6);
        assertThat(buckets).allMatch(b -> b.success() == 0 && b.fail() == 0);
    }

    @Test
    void shouldAggregateOverloadCountsPerBucket() {
        List<LocalDateTime> starts = StatisticsPeriodBucketing.generateBucketStarts(StatisticsPeriod.LAST_HOUR, NOW);
        List<LocalDateTime> timestamps = List.of(
                starts.getFirst().plusMinutes(5),
                starts.getFirst().plusMinutes(5),
                starts.get(1).plusMinutes(5)
        );

        List<TimeSeriesBucketView> buckets =
                StatisticsPeriodBucketing.aggregateCounts(StatisticsPeriod.LAST_HOUR, NOW, timestamps);

        assertThat(buckets).hasSize(6);
        assertThat(buckets.getFirst().count()).isEqualTo(2);
        assertThat(buckets.get(1).count()).isEqualTo(1);
    }

    @Test
    void shouldAssignToLatestMatchingBucket() {
        List<LocalDateTime> starts = List.of(
                LocalDateTime.of(2026, 6, 3, 10, 0),
                LocalDateTime.of(2026, 6, 3, 10, 10),
                LocalDateTime.of(2026, 6, 3, 10, 20)
        );

        assertThat(StatisticsPeriodBucketing.bucketIndex(starts, LocalDateTime.of(2026, 6, 3, 10, 5))).isEqualTo(0);
        assertThat(StatisticsPeriodBucketing.bucketIndex(starts, LocalDateTime.of(2026, 6, 3, 10, 15))).isEqualTo(1);
        assertThat(StatisticsPeriodBucketing.bucketIndex(starts, LocalDateTime.of(2026, 6, 3, 10, 25))).isEqualTo(2);
        assertThat(StatisticsPeriodBucketing.bucketIndex(starts, LocalDateTime.of(2026, 6, 3, 9, 59))).isEqualTo(-1);
    }
}
