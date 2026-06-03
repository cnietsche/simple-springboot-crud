import { apiFetch } from './client';
import type { LoginAttemptBucket } from '../types/metrics';
import type { StatisticsPeriod } from '../types/statistics';

export function getLoginAttemptsMetrics(period: StatisticsPeriod): Promise<LoginAttemptBucket[]> {
  const params = new URLSearchParams({ period });
  return apiFetch<LoginAttemptBucket[]>(`/api/metrics/login-attempts?${params}`);
}
