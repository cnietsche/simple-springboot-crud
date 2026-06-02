package com.cnietsche.domain.port.in;

import com.cnietsche.domain.model.UserType;

public record UserView(String name, String email, UserType type) {
}
