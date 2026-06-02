package com.cnietsche.domain.port.in;

import java.util.UUID;

public interface GetUserUseCase {

    UserView execute(UUID id);
}
