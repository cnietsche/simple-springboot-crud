package com.cnietsche.application.service;

import com.cnietsche.domain.port.in.ListOverloadUseCase;
import com.cnietsche.domain.port.in.OverloadPageView;
import com.cnietsche.domain.port.out.OverloadRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ListOverloadService implements ListOverloadUseCase {

    private final OverloadRepositoryPort overloadRepository;

    public ListOverloadService(OverloadRepositoryPort overloadRepository) {
        this.overloadRepository = overloadRepository;
    }

    @Override
    public OverloadPageView execute(UUID userId, int page, int size) {
        return overloadRepository.findByUserIdOrderByDateDesc(userId, page, size);
    }
}
