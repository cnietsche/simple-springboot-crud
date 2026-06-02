package com.cnietsche.application.service;

import com.cnietsche.domain.exception.InvalidCredentialsException;
import com.cnietsche.domain.model.User;
import com.cnietsche.domain.model.UserType;
import com.cnietsche.domain.port.in.AuthenticatedUser;
import com.cnietsche.domain.port.in.ValidateLoginCommand;
import com.cnietsche.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateLoginServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ValidateLoginService service;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ValidateLoginService(userRepository, passwordEncoder);
    }

    @Test
    void shouldLoginWithEmail() {
        User user = new User(userId, "John", "john@test.com", "john", "hash", UserType.USER);
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);

        AuthenticatedUser result = service.execute(new ValidateLoginCommand("john@test.com", "pass"));

        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.name()).isEqualTo("John");
    }

    @Test
    void shouldLoginWithUsername() {
        User user = new User(userId, "John", "john@test.com", "john", "hash", UserType.ADMIN);
        when(userRepository.findByEmail("john")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);

        AuthenticatedUser result = service.execute(new ValidateLoginCommand("john", "pass"));

        assertThat(result.type()).isEqualTo(UserType.ADMIN);
    }

    @Test
    void shouldRejectInvalidPassword() {
        User user = new User(userId, "John", "john@test.com", "john", "hash", UserType.USER);
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.execute(new ValidateLoginCommand("john@test.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
