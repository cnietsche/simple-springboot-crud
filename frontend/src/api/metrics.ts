import { apiFetch } from './client';
import type { LoginAttemptMetric } from '../types/metrics';
import type { StatisticsPeriod } from '../types/statistics';

export function getLoginAttemptsMetrics(period: StatisticsPeriod): Promise<LoginAttemptMetric[]> {
  const params = new URLSearchParams({ period });
  return apiFetch<LoginAttemptMetric[]>(`/api/metrics/login-attempts?${params}`);
}
