package com.cnietsche.adapter.in.web.dto;

import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String name
) {
}
