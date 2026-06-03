package com.cnietsche.adapter.in.web.dto;

import java.time.LocalDateTime;

public record LoginAttemptBucketResponse(LocalDateTime bucketStart, long success, long fail) {
}
