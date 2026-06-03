package com.cnietsche.application.service;

import com.cnietsche.domain.exception.InvalidCredentialsException;
import com.cnietsche.domain.model.LoginAttemptOutcome;
import com.cnietsche.domain.model.User;
import com.cnietsche.domain.model.UserType;
import com.cnietsche.domain.port.in.AuthenticatedUser;
import com.cnietsche.domain.port.in.ValidateLoginCommand;
import com.cnietsche.domain.port.out.LoginAttemptRepositoryPort;
import com.cnietsche.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateLoginServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoginAttemptRepositoryPort loginAttemptRepository;

    @Captor
    private ArgumentCaptor<LocalDateTime> occurredAtCaptor;

    private ValidateLoginService service;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ValidateLoginService(userRepository, passwordEncoder, loginAttemptRepository);
    }

    @Test
    void shouldLoginWithEmailAndRecordSuccess() {
        User user = new User(userId, "John", "john@test.com", "john", "hash", UserType.USER);
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);

        AuthenticatedUser result = service.execute(new ValidateLoginCommand("john@test.com", "pass"));

        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.name()).isEqualTo("John");
        verify(loginAttemptRepository).save(eq(LoginAttemptOutcome.SUCCESS), occurredAtCaptor.capture());
        assertThat(occurredAtCaptor.getValue()).isNotNull();
    }

    @Test
    void shouldLoginWithUsernameAndRecordSuccess() {
        User user = new User(userId, "John", "john@test.com", "john", "hash", UserType.ADMIN);
        when(userRepository.findByEmail("john")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);

        AuthenticatedUser result = service.execute(new ValidateLoginCommand("john", "pass"));

        assertThat(result.type()).isEqualTo(UserType.ADMIN);
        verify(loginAttemptRepository).save(eq(LoginAttemptOutcome.SUCCESS), occurredAtCaptor.capture());
    }

    @Test
    void shouldRejectInvalidPasswordAndRecordFail() {
        User user = new User(userId, "John", "john@test.com", "john", "hash", UserType.USER);
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.execute(new ValidateLoginCommand("john@test.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(loginAttemptRepository).save(eq(LoginAttemptOutcome.FAIL), occurredAtCaptor.capture());
    }

    @Test
    void shouldRejectUnknownUserAndRecordFail() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new ValidateLoginCommand("unknown@test.com", "pass")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(loginAttemptRepository).save(eq(LoginAttemptOutcome.FAIL), occurredAtCaptor.capture());
    }
}
