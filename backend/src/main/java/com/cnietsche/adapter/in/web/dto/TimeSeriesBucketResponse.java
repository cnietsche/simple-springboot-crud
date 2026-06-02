package com.cnietsche.adapter.in.web.dto;

import java.time.LocalDateTime;

public record TimeSeriesBucketResponse(
        LocalDateTime bucketStart,
        long count
) {
}
