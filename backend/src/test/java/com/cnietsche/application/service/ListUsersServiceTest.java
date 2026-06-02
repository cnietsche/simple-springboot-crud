package com.cnietsche.application.service;

import com.cnietsche.domain.model.User;
import com.cnietsche.domain.model.UserType;
import com.cnietsche.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUsersServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    private ListUsersService service;

    @BeforeEach
    void setUp() {
        service = new ListUsersService(userRepository);
    }

    @Test
    void shouldReturnUsersOrderedByName() {
        UUID id = UUID.randomUUID();
        when(userRepository.findAllOrderByName()).thenReturn(List.of(
                new User(id, "Alice", "a@b.com", "alice", "hash", UserType.USER)));

        var result = service.execute();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Alice");
        assertThat(result.getFirst().id()).isEqualTo(id);
    }
}
