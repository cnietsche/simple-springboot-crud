package com.cnietsche.domain.port.in;

import java.util.UUID;

public record UserSummaryView(
        UUID id,
        String name
) {
}
