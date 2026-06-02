package com.cnietsche.application.service;

import com.cnietsche.domain.exception.UserNotFoundException;
import com.cnietsche.domain.model.User;
import com.cnietsche.domain.port.in.GetUserUseCase;
import com.cnietsche.domain.port.in.UserView;
import com.cnietsche.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetUserService implements GetUserUseCase {

    private final UserRepositoryPort userRepository;

    public GetUserService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserView execute(UUID id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return toView(user);
    }

    static UserView toView(User user) {
        return new UserView(user.getName(), user.getEmail(), user.getType());
    }
}
