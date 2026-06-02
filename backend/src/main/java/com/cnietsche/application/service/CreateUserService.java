package com.cnietsche.application.service;

import com.cnietsche.domain.exception.DuplicateUserException;
import com.cnietsche.domain.model.User;
import com.cnietsche.domain.model.UserType;
import com.cnietsche.domain.port.in.CreateUserCommand;
import com.cnietsche.domain.port.in.CreateUserUseCase;
import com.cnietsche.domain.port.out.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateUserService implements CreateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateUserService(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User execute(CreateUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateUserException("Email already registered");
        }
        if (userRepository.existsByUsername(command.username())) {
            throw new DuplicateUserException("Username already registered");
        }

        UserType type = command.type() != null ? command.type() : UserType.USER;
        String passwordHash = passwordEncoder.encode(command.password());

        User user = new User(
                UUID.randomUUID(),
                command.name().trim(),
                command.email().trim().toLowerCase(),
                command.username().trim(),
                passwordHash,
                type
        );

        return userRepository.save(user);
    }
}
