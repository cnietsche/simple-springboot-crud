package com.cnietsche.adapter.in.web;

import com.cnietsche.adapter.in.web.dto.LoginRequest;
import com.cnietsche.adapter.in.web.dto.LoginResponse;
import com.cnietsche.domain.port.in.AuthenticatedUser;
import com.cnietsche.domain.port.in.ValidateLoginCommand;
import com.cnietsche.domain.port.in.ValidateLoginUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ValidateLoginUseCase validateLoginUseCase;

    public AuthController(ValidateLoginUseCase validateLoginUseCase) {
        this.validateLoginUseCase = validateLoginUseCase;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AuthenticatedUser user = validateLoginUseCase.execute(
                new ValidateLoginCommand(request.identifier(), request.password())
        );
        return new LoginResponse(user.id(), user.name(), user.email(), user.type());
    }
}
