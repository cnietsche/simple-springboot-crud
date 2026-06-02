package com.cnietsche.adapter.in.web.dto;

import java.util.UUID;

public record UserOverloadCountResponse(
        UUID userId,
        String userName,
        long count
) {
}
