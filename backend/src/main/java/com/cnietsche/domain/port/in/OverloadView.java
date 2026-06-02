package com.cnietsche.domain.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

public record OverloadView(
        UUID id,
        LocalDateTime date,
        UUID userId,
        String value
) {
}
