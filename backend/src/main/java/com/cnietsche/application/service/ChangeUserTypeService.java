package com.cnietsche.application.service;

import com.cnietsche.domain.exception.UserNotFoundException;
import com.cnietsche.domain.model.User;
import com.cnietsche.domain.port.in.ChangeUserTypeCommand;
import com.cnietsche.domain.port.in.ChangeUserTypeUseCase;
import com.cnietsche.domain.port.in.UserView;
import com.cnietsche.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class ChangeUserTypeService implements ChangeUserTypeUseCase {

    private final UserRepositoryPort userRepository;

    public ChangeUserTypeService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserView execute(ChangeUserTypeCommand command) {
        User user = userRepository.findById(command.userId()).orElseThrow(UserNotFoundException::new);
        User updated = user.withType(command.type());
        User saved = userRepository.save(updated);
        return GetUserService.toView(saved);
    }
}
