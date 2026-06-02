package com.cnietsche.adapter.in.web.dto;

import java.util.List;

public record OverloadStatisticsResponse(
        List<UserOverloadCountResponse> topUsers,
        List<TimeSeriesBucketResponse> timeSeries
) {
}
