package com.cnietsche.domain.port.in;

public interface ValidateLoginUseCase {

    AuthenticatedUser execute(ValidateLoginCommand command);
}
