package com.cnietsche.application.service;

import com.cnietsche.domain.exception.InvalidCredentialsException;
import com.cnietsche.domain.model.User;
import com.cnietsche.domain.port.in.AuthenticatedUser;
import com.cnietsche.domain.port.in.ValidateLoginCommand;
import com.cnietsche.domain.port.in.ValidateLoginUseCase;
import com.cnietsche.domain.port.out.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ValidateLoginService implements ValidateLoginUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;

    public ValidateLoginService(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthenticatedUser execute(ValidateLoginCommand command) {
        String identifier = command.identifier().trim();
        User user = userRepository.findByEmail(identifier.toLowerCase())
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new AuthenticatedUser(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getType()
        );
    }
}
