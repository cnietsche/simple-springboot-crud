package com.cnietsche.application.service;

import com.cnietsche.domain.exception.UserNotFoundException;
import com.cnietsche.domain.model.Overload;
import com.cnietsche.domain.model.RecordBatchSize;
import com.cnietsche.domain.model.User;
import com.cnietsche.domain.model.UserType;
import com.cnietsche.domain.port.in.GenerateOverloadCommand;
import com.cnietsche.domain.port.out.OverloadRepositoryPort;
import com.cnietsche.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateOverloadServiceTest {

    @Mock
    private OverloadRepositoryPort overloadRepository;

    @Mock
    private UserRepositoryPort userRepository;

    private GenerateOverloadService service;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GenerateOverloadService(overloadRepository, userRepository);
    }

    @Test
    void shouldGenerateOverloadRecords() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser()));
        doNothing().when(overloadRepository).saveAll(anyList());

        int created = service.execute(new GenerateOverloadCommand(userId, RecordBatchSize.SIZE_50));

        assertThat(created).isEqualTo(50);
        ArgumentCaptor<List<Overload>> captor = ArgumentCaptor.forClass(List.class);
        verify(overloadRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(50);
        assertThat(captor.getValue().getFirst().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getFirst().getValue()).isNotBlank();
    }

    @Test
    void shouldRejectUnknownUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
                new GenerateOverloadCommand(userId, RecordBatchSize.SIZE_100)))
                .isInstanceOf(UserNotFoundException.class);
    }

    private User sampleUser() {
        return new User(userId, "John", "a@b.com", "john", "hash", UserType.USER);
    }
}
