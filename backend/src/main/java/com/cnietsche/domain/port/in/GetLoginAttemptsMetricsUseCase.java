package com.cnietsche.domain.port.in;

import com.cnietsche.domain.model.StatisticsPeriod;

import java.util.List;

public interface GetLoginAttemptsMetricsUseCase {

    List<LoginAttemptsMetricView> execute(StatisticsPeriod period);
}
