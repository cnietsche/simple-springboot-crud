package com.cnietsche.domain.port.in;

public record LoginAttemptsMetricView(String outcome, long count) {
}
