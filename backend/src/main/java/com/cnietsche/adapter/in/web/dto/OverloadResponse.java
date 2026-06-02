package com.cnietsche.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record OverloadResponse(
        UUID id,
        LocalDateTime date,
        UUID userId,
        String value
) {
}
