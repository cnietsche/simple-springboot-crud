package com.cnietsche.adapter.out.persistence;

import java.util.UUID;

public interface UserOverloadCountProjection {

    UUID getUserId();

    String getUserName();

    long getCnt();
}
