package com.cnietsche.domain.exception;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException() {
        super("User not found");
    }
}
