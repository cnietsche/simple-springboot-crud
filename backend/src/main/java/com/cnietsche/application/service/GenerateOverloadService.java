package com.cnietsche.application.service;

import com.cnietsche.domain.exception.UserNotFoundException;
import com.cnietsche.domain.model.Overload;
import com.cnietsche.domain.port.in.GenerateOverloadCommand;
import com.cnietsche.domain.port.in.GenerateOverloadUseCase;
import com.cnietsche.domain.port.out.OverloadRepositoryPort;
import com.cnietsche.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GenerateOverloadService implements GenerateOverloadUseCase {

    private final OverloadRepositoryPort overloadRepository;
    private final UserRepositoryPort userRepository;

    public GenerateOverloadService(
            OverloadRepositoryPort overloadRepository,
            UserRepositoryPort userRepository) {
        this.overloadRepository = overloadRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public int execute(GenerateOverloadCommand command) {
        userRepository.findById(command.userId())
                .orElseThrow(UserNotFoundException::new);

        int count = command.count().getCount();
        List<Overload> overloads = new ArrayList<>(count);
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < count; i++) {
            overloads.add(new Overload(
                    UUID.randomUUID(),
                    now,
                    command.userId(),
                    RandomOverloadValueGenerator.generate()
            ));
        }

        overloadRepository.saveAll(overloads);
        return count;
    }
}
