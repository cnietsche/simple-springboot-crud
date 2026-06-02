package com.cnietsche.adapter.in.web.dto;

import java.util.List;

public record OverloadPageResponse(
        List<OverloadResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
