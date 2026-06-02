package com.cnietsche.domain.port.in;

import java.util.List;

public record OverloadStatisticsView(
        List<UserOverloadCountView> topUsers,
        List<TimeSeriesBucketView> timeSeries
) {
}
