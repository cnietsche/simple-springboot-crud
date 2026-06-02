package com.cnietsche.adapter.out.persistence;

import java.time.LocalDateTime;

public interface TimeSeriesBucketProjection {

    LocalDateTime getBucketStart();

    long getCnt();
}
