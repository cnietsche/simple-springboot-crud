package com.cnietsche.domain.port.in;

import com.cnietsche.domain.model.UserType;

import java.util.UUID;

public record ChangeUserTypeCommand(UUID userId, UserType type) {
}
