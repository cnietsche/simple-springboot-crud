package com.cnietsche.domain.port.in;

import com.cnietsche.domain.model.UserType;

public record CreateUserCommand(
        String name,
        String email,
        String username,
        String password,
        UserType type
) {
}
