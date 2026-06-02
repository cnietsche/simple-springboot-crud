package com.cnietsche.domain.port.in;

import java.util.UUID;

public interface ListOverloadUseCase {

    OverloadPageView execute(UUID userId, int page, int size);
}
