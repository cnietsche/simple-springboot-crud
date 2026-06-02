package com.cnietsche.domain.port.in;

import com.cnietsche.domain.model.RecordBatchSize;

import java.util.UUID;

public record GenerateOverloadCommand(
        UUID userId,
        RecordBatchSize count
) {
}
