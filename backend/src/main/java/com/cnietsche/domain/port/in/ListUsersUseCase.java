package com.cnietsche.domain.port.in;

import java.util.List;

public interface ListUsersUseCase {

    List<UserSummaryView> execute();
}
