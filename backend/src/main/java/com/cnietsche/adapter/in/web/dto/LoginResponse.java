package com.cnietsche.adapter.in.web.dto;

import com.cnietsche.domain.model.UserType;

import java.util.UUID;

public record LoginResponse(UUID id, String name, String email, UserType type) {
}
