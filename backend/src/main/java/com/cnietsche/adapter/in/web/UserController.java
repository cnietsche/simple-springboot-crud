package com.cnietsche.adapter.in.web;

import com.cnietsche.adapter.in.web.dto.ChangeTypeRequest;
import com.cnietsche.adapter.in.web.dto.CreateUserRequest;
import com.cnietsche.adapter.in.web.dto.CreateUserResponse;
import com.cnietsche.adapter.in.web.dto.UserResponse;
import com.cnietsche.domain.model.User;
import com.cnietsche.domain.port.in.ChangeUserTypeCommand;
import com.cnietsche.domain.port.in.ChangeUserTypeUseCase;
import com.cnietsche.domain.port.in.CreateUserCommand;
import com.cnietsche.domain.port.in.CreateUserUseCase;
import com.cnietsche.domain.port.in.GetUserUseCase;
import com.cnietsche.domain.port.in.UserView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ChangeUserTypeUseCase changeUserTypeUseCase;

    public UserController(
            CreateUserUseCase createUserUseCase,
            GetUserUseCase getUserUseCase,
            ChangeUserTypeUseCase changeUserTypeUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.changeUserTypeUseCase = changeUserTypeUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateUserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        User user = createUserUseCase.execute(new CreateUserCommand(
                request.name(),
                request.email(),
                request.username(),
                request.password(),
                request.type()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getType()
        ));
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable UUID id) {
        UserView view = getUserUseCase.execute(id);
        return new UserResponse(view.name(), view.email(), view.type());
    }

    @PatchMapping("/{id}/type")
    public UserResponse changeType(@PathVariable UUID id, @Valid @RequestBody ChangeTypeRequest request) {
        UserView view = changeUserTypeUseCase.execute(new ChangeUserTypeCommand(id, request.type()));
        return new UserResponse(view.name(), view.email(), view.type());
    }
}
