package com.cnietsche.domain.port.in;

import java.time.LocalDateTime;

public record LoginAttemptsBucketView(LocalDateTime bucketStart, long success, long fail) {
}
