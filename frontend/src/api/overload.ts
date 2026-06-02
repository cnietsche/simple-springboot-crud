import { apiFetch } from './client';
import type { OverloadPage, OverloadStatistics } from '../types/overload';
import type { RecordBatchCount, StatisticsPeriod } from '../types/statistics';

export function generateOverload(
  userId: string,
  count: RecordBatchCount
): Promise<{ created: number }> {
  return apiFetch<{ created: number }>('/api/overloads/generate', {
    method: 'POST',
    body: JSON.stringify({ userId, count }),
  });
}

export function listOverloads(
  userId: string,
  page: number,
  size = 50
): Promise<OverloadPage> {
  const params = new URLSearchParams({
    userId,
    page: String(page),
    size: String(size),
  });
  return apiFetch<OverloadPage>(`/api/overloads?${params}`);
}

export function getOverloadStatistics(
  period: StatisticsPeriod,
  userId?: string
): Promise<OverloadStatistics> {
  const params = new URLSearchParams({ period });
  if (userId) {
    params.set('userId', userId);
  }
  return apiFetch<OverloadStatistics>(`/api/overloads/statistics?${params}`);
}
