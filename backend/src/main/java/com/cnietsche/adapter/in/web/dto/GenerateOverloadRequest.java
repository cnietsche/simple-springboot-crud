package com.cnietsche.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GenerateOverloadRequest(
        @NotNull UUID userId,
        @NotNull Integer count
) {
}
