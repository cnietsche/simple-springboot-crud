package com.cnietsche.application.service;

import com.cnietsche.domain.port.in.OverloadPageView;
import com.cnietsche.domain.port.in.OverloadView;
import com.cnietsche.domain.port.out.OverloadRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListOverloadServiceTest {

    @Mock
    private OverloadRepositoryPort overloadRepository;

    private ListOverloadService service;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ListOverloadService(overloadRepository);
    }

    @Test
    void shouldDelegateToRepository() {
        OverloadPageView page = new OverloadPageView(
                List.of(new OverloadView(UUID.randomUUID(), LocalDateTime.now(), userId, "server cache")),
                0, 50, 1, 1);
        when(overloadRepository.findByUserIdOrderByDateDesc(userId, 0, 50)).thenReturn(page);

        OverloadPageView result = service.execute(userId, 0, 50);

        assertThat(result.content()).hasSize(1);
        verify(overloadRepository).findByUserIdOrderByDateDesc(userId, 0, 50);
    }
}
