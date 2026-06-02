package com.cnietsche.domain.port.in;

public record ValidateLoginCommand(String identifier, String password) {
}
