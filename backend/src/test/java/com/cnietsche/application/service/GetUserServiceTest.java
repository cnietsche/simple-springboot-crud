package com.cnietsche.application.service;

import com.cnietsche.domain.exception.UserNotFoundException;
import com.cnietsche.domain.model.User;
import com.cnietsche.domain.model.UserType;
import com.cnietsche.domain.port.in.UserView;
import com.cnietsche.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    private GetUserService service;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GetUserService(userRepository);
    }

    @Test
    void shouldReturnUserView() {
        User user = new User(userId, "John", "john@test.com", "john", "hash", UserType.USER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserView view = service.execute(userId);

        assertThat(view.name()).isEqualTo("John");
        assertThat(view.email()).isEqualTo("john@test.com");
        assertThat(view.type()).isEqualTo(UserType.USER);
    }

    @Test
    void shouldThrowWhenNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(userId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
