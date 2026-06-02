package com.cnietsche.adapter.in.web.dto;

import com.cnietsche.domain.model.UserType;
import jakarta.validation.constraints.NotNull;

public record ChangeTypeRequest(@NotNull UserType type) {
}
