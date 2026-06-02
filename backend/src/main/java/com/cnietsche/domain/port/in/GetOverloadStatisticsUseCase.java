package com.cnietsche.domain.port.in;

import com.cnietsche.domain.model.StatisticsPeriod;

import java.util.UUID;

public interface GetOverloadStatisticsUseCase {

    OverloadStatisticsView execute(StatisticsPeriod period, UUID userId);
}
