package com.cnietsche.application.service;

import com.cnietsche.domain.exception.InvalidCredentialsException;
import com.cnietsche.domain.model.LoginAttemptOutcome;
import com.cnietsche.domain.model.User;
import com.cnietsche.domain.port.in.AuthenticatedUser;
import com.cnietsche.domain.port.in.ValidateLoginCommand;
import com.cnietsche.domain.port.in.ValidateLoginUseCase;
import com.cnietsche.domain.port.out.LoginAttemptRepositoryPort;
import com.cnietsche.domain.port.out.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ValidateLoginService implements ValidateLoginUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptRepositoryPort loginAttemptRepository;

    public ValidateLoginService(
            UserRepositoryPort userRepository,
            PasswordEncoder passwordEncoder,
            LoginAttemptRepositoryPort loginAttemptRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptRepository = loginAttemptRepository;
    }

    @Override
    public AuthenticatedUser execute(ValidateLoginCommand command) {
        String identifier = command.identifier().trim();
        User user = userRepository.findByEmail(identifier.toLowerCase())
                .or(() -> userRepository.findByUsername(identifier))
                .orElse(null);

        if (user == null || !passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            loginAttemptRepository.save(LoginAttemptOutcome.FAIL, LocalDateTime.now());
            throw new InvalidCredentialsException();
        }

        loginAttemptRepository.save(LoginAttemptOutcome.SUCCESS, LocalDateTime.now());
        return new AuthenticatedUser(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getType()
        );
    }
}
