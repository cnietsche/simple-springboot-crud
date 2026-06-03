package com.cnietsche.application.service;

import com.cnietsche.domain.model.LoginAttemptOutcome;
import com.cnietsche.domain.model.StatisticsPeriod;
import com.cnietsche.domain.port.in.LoginAttemptsBucketView;
import com.cnietsche.domain.port.in.TimeSeriesBucketView;
import com.cnietsche.domain.port.out.LoginAttemptPoint;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public final class StatisticsPeriodBucketing {

    private StatisticsPeriodBucketing() {
    }

    public static LocalDateTime resolveQueryFrom(StatisticsPeriod period, LocalDateTime now) {
        return generateBucketStarts(period, now).getFirst();
    }

    public static List<LocalDateTime> generateBucketStarts(StatisticsPeriod period, LocalDateTime now) {
        BucketConfig config = bucketConfig(period);
        LocalDateTime alignedEnd = alignUp(now, config);
        LocalDateTime firstStart = retreat(alignedEnd, config, config.partCount());

        List<LocalDateTime> starts = new ArrayList<>(config.partCount());
        LocalDateTime bucketStart = firstStart;
        for (int i = 0; i < config.partCount(); i++) {
            starts.add(bucketStart);
            bucketStart = advance(bucketStart, config);
        }
        return starts;
    }

    public static List<LoginAttemptsBucketView> aggregateLoginAttempts(
            StatisticsPeriod period,
            LocalDateTime now,
            List<LoginAttemptPoint> points) {
        List<LocalDateTime> bucketStarts = generateBucketStarts(period, now);
        long[] success = new long[bucketStarts.size()];
        long[] fail = new long[bucketStarts.size()];

        for (LoginAttemptPoint point : points) {
            int index = bucketIndex(bucketStarts, point.occurredAt());
            if (index < 0) {
                continue;
            }
            if (point.outcome() == LoginAttemptOutcome.SUCCESS) {
                success[index]++;
            } else {
                fail[index]++;
            }
        }

        List<LoginAttemptsBucketView> result = new ArrayList<>(bucketStarts.size());
        for (int i = 0; i < bucketStarts.size(); i++) {
            result.add(new LoginAttemptsBucketView(bucketStarts.get(i), success[i], fail[i]));
        }
        return result;
    }

    public static List<TimeSeriesBucketView> aggregateCounts(
            StatisticsPeriod period,
            LocalDateTime now,
            List<LocalDateTime> timestamps) {
        List<LocalDateTime> bucketStarts = generateBucketStarts(period, now);
        long[] counts = new long[bucketStarts.size()];

        for (LocalDateTime timestamp : timestamps) {
            int index = bucketIndex(bucketStarts, timestamp);
            if (index >= 0) {
                counts[index]++;
            }
        }

        List<TimeSeriesBucketView> result = new ArrayList<>(bucketStarts.size());
        for (int i = 0; i < bucketStarts.size(); i++) {
            result.add(new TimeSeriesBucketView(bucketStarts.get(i), counts[i]));
        }
        return result;
    }

    static int bucketIndex(List<LocalDateTime> bucketStarts, LocalDateTime timestamp) {
        int index = -1;
        for (int i = 0; i < bucketStarts.size(); i++) {
            if (!timestamp.isBefore(bucketStarts.get(i))) {
                index = i;
            } else {
                break;
            }
        }
        return index;
    }

    private static LocalDateTime alignUp(LocalDateTime dateTime, BucketConfig config) {
        LocalDateTime floor = alignDown(dateTime, config);
        if (!dateTime.isAfter(floor)) {
            return floor;
        }
        return advance(floor, config);
    }

    private static LocalDateTime alignDown(LocalDateTime dateTime, BucketConfig config) {
        return switch (config.unit()) {
            case MINUTES -> {
                int minute = dateTime.getMinute();
                int alignedMinute = (minute / config.step()) * config.step();
                yield dateTime.withMinute(alignedMinute).withSecond(0).withNano(0);
            }
            case HOURS -> {
                int hour = dateTime.getHour();
                int alignedHour = (hour / config.step()) * config.step();
                yield dateTime.withHour(alignedHour).withMinute(0).withSecond(0).withNano(0);
            }
            case DAYS -> dateTime.truncatedTo(ChronoUnit.DAYS);
            case WEEKS -> dateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .truncatedTo(ChronoUnit.DAYS);
            case MONTHS -> dateTime.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        };
    }

    private static LocalDateTime retreat(LocalDateTime dateTime, BucketConfig config, int steps) {
        LocalDateTime result = dateTime;
        for (int i = 0; i < steps; i++) {
            result = switch (config.unit()) {
                case MINUTES -> result.minusMinutes(config.step());
                case HOURS -> result.minusHours(config.step());
                case DAYS -> result.minusDays(config.step());
                case WEEKS -> result.minusWeeks(config.step());
                case MONTHS -> result.minusMonths(config.step());
            };
        }
        return result;
    }

    private static LocalDateTime advance(LocalDateTime bucketStart, BucketConfig config) {
        return switch (config.unit()) {
            case MINUTES -> bucketStart.plusMinutes(config.step());
            case HOURS -> bucketStart.plusHours(config.step());
            case DAYS -> bucketStart.plusDays(config.step());
            case WEEKS -> bucketStart.plusWeeks(config.step());
            case MONTHS -> bucketStart.plusMonths(config.step());
        };
    }

    private static BucketConfig bucketConfig(StatisticsPeriod period) {
        return switch (period) {
            case LAST_30_MINUTES -> new BucketConfig(BucketUnit.MINUTES, 5, 6);
            case LAST_HOUR -> new BucketConfig(BucketUnit.MINUTES, 10, 6);
            case LAST_12_HOURS -> new BucketConfig(BucketUnit.HOURS, 1, 12);
            case LAST_DAY -> new BucketConfig(BucketUnit.HOURS, 2, 12);
            case LAST_WEEK -> new BucketConfig(BucketUnit.DAYS, 1, 7);
            case LAST_MONTH -> new BucketConfig(BucketUnit.WEEKS, 1, 5);
            case LAST_YEAR -> new BucketConfig(BucketUnit.MONTHS, 1, 12);
        };
    }

    private enum BucketUnit {
        MINUTES, HOURS, DAYS, WEEKS, MONTHS
    }

    private record BucketConfig(BucketUnit unit, int step, int partCount) {
    }
}
