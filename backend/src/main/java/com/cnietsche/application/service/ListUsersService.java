package com.cnietsche.application.service;

import com.cnietsche.domain.port.in.ListUsersUseCase;
import com.cnietsche.domain.port.in.UserSummaryView;
import com.cnietsche.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListUsersService implements ListUsersUseCase {

    private final UserRepositoryPort userRepository;

    public ListUsersService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserSummaryView> execute() {
        return userRepository.findAllOrderByName().stream()
                .map(user -> new UserSummaryView(user.getId(), user.getName()))
                .toList();
    }
}
