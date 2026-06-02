package com.cnietsche.application.service;

import com.cnietsche.domain.model.User;
import com.cnietsche.domain.model.UserType;
import com.cnietsche.domain.port.in.ChangeUserTypeCommand;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeUserTypeServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    private ChangeUserTypeService service;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ChangeUserTypeService(userRepository);
    }

    @Test
    void shouldChangeType() {
        User user = new User(userId, "John", "john@test.com", "john", "hash", UserType.USER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserView view = service.execute(new ChangeUserTypeCommand(userId, UserType.ADMIN));

        assertThat(view.type()).isEqualTo(UserType.ADMIN);
    }
}
