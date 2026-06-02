package com.cnietsche.domain.port.in;

import java.time.LocalDateTime;

public record TimeSeriesBucketView(
        LocalDateTime bucketStart,
        long count
) {
}
