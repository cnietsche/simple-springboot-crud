package com.cnietsche.adapter.out.persistence;

import com.cnietsche.domain.model.LoginAttemptOutcome;

public interface LoginAttemptCountProjection {

    LoginAttemptOutcome getOutcome();

    long getCnt();
}
