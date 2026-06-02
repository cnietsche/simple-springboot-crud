package com.cnietsche.application.service;

import com.cnietsche.domain.exception.DuplicateUserException;
import com.cnietsche.domain.model.User;
import com.cnietsche.domain.model.UserType;
import com.cnietsche.domain.port.in.CreateUserCommand;
import com.cnietsche.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CreateUserService service;

    @BeforeEach
    void setUp() {
        service = new CreateUserService(userRepository, passwordEncoder);
    }

    @Test
    void shouldCreateUserWithDefaultType() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.execute(new CreateUserCommand(
                "John", "a@b.com", "john", "secret123", null));

        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("a@b.com");
        assertThat(result.getType()).isEqualTo(UserType.USER);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> service.execute(new CreateUserCommand(
                "John", "a@b.com", "john", "secret123", UserType.USER)))
                .isInstanceOf(DuplicateUserException.class);
    }
}
