package com.cnietsche.domain.port.in;

import java.util.UUID;

public record UserOverloadCountView(
        UUID userId,
        String userName,
        long count
) {
}
