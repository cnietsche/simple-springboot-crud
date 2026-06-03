import { useCallback, useEffect, useState } from 'react';
import { Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { getLoginAttemptsMetrics } from '../../api/metrics';
import { ApiError } from '../../api/client';
import { DASHBOARD_REFRESH_MS } from '../../constants/dashboard';
import type { StatisticsPeriod } from '../../types/statistics';
import { formatBucketLabel, loginAttemptsChartTitle } from '../../utils/formatBucketLabel';
import styles from './LoginAttemptsLineChart.module.css';

const SUCCESS_COLOR = '#16a34a';
const FAIL_COLOR = '#dc2626';

interface ChartPoint {
  time: string;
  success: number;
  fail: number;
}

interface LoginAttemptsLineChartProps {
  period: StatisticsPeriod;
}

export function LoginAttemptsLineChart({ period }: LoginAttemptsLineChartProps) {
  const [chartData, setChartData] = useState<ChartPoint[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [totals, setTotals] = useState<{ success: number; fail: number } | null>(null);

  const load = useCallback(async () => {
    try {
      const buckets = await getLoginAttemptsMetrics(period);
      setChartData(
        buckets.map((b) => ({
          time: formatBucketLabel(b.bucketStart, period),
          success: b.success,
          fail: b.fail,
        }))
      );
      const success = buckets.reduce((sum, b) => sum + b.success, 0);
      const fail = buckets.reduce((sum, b) => sum + b.fail, 0);
      setTotals({ success, fail });
      setError(null);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Erro ao carregar tentativas de login');
    }
  }, [period]);

  useEffect(() => {
    load();
    const interval = setInterval(load, DASHBOARD_REFRESH_MS);
    return () => clearInterval(interval);
  }, [load]);

  return (
    <section className={styles.card}>
      <h2>{loginAttemptsChartTitle(period)}</h2>
      {totals && (
        <p className={styles.summary}>
          Total no período: <span className={styles.success}>{totals.success} sucesso</span>
          {' · '}
          <span className={styles.fail}>{totals.fail} falha</span>
        </p>
      )}
      {error && <p className={styles.error}>{error}</p>}
      {chartData.length === 0 && !error ? (
        <p className={styles.empty}>Carregando...</p>
      ) : chartData.length > 0 && !error ? (
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={chartData}>
            <XAxis dataKey="time" interval="preserveStartEnd" />
            <YAxis allowDecimals={false} />
            <Tooltip />
            <Legend />
            <Line
              type="monotone"
              dataKey="success"
              name="Success"
              stroke={SUCCESS_COLOR}
              strokeWidth={2}
              dot={false}
            />
            <Line
              type="monotone"
              dataKey="fail"
              name="Fail"
              stroke={FAIL_COLOR}
              strokeWidth={2}
              dot={false}
            />
          </LineChart>
        </ResponsiveContainer>
      ) : null}
    </section>
  );
}
