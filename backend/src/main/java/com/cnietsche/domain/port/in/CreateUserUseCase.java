package com.cnietsche.domain.port.in;

import com.cnietsche.domain.model.User;

public interface CreateUserUseCase {

    User execute(CreateUserCommand command);
}
