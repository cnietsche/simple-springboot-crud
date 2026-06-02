package com.cnietsche.domain.port.in;

public interface ChangeUserTypeUseCase {

    UserView execute(ChangeUserTypeCommand command);
}
