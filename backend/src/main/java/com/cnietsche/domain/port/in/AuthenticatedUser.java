package com.cnietsche.domain.port.in;

import com.cnietsche.domain.model.UserType;

import java.util.UUID;

public record AuthenticatedUser(UUID id, String name, String email, UserType type) {
}
