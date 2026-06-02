package com.cnietsche.domain.port.in;

import java.util.List;

public record OverloadPageView(
        List<OverloadView> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
