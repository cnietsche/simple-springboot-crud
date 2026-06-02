package com.cnietsche.adapter.in.web.dto;

import com.cnietsche.domain.model.UserType;

public record UserResponse(String name, String email, UserType type) {
}
