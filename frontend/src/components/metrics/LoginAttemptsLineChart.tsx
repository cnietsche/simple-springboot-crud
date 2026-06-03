import { useCallback, useEffect, useState } from 'react';
import { Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { getLoginAttemptsMetrics } from '../../api/metrics';
import { ApiError } from '../../api/client';
import { DASHBOARD_REFRESH_MS } from '../../constants/dashboard';
import type { StatisticsPeriod } from '../../types/statistics';
import styles from './LoginAttemptsLineChart.module.css';

const SUCCESS_COLOR = '#16a34a';
const FAIL_COLOR = '#dc2626';
const MAX_HISTORY_POINTS = 40;

interface Snapshot {
  time: string;
  success: number;
  fail: number;
}

interface LoginAttemptsLineChartProps {
  period: StatisticsPeriod;
}

function parseCounts(metrics: { outcome: string; count: number }[]): { success: number; fail: number } {
  const success = metrics.find((m) => m.outcome === 'Success')?.count ?? 0;
  const fail = metrics.find((m) => m.outcome === 'Fail')?.count ?? 0;
  return { success, fail };
}

function formatTimeLabel(date: Date): string {
  return date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

export function LoginAttemptsLineChart({ period }: LoginAttemptsLineChartProps) {
  const [history, setHistory] = useState<Snapshot[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [latest, setLatest] = useState<{ success: number; fail: number } | null>(null);

  const load = useCallback(async () => {
    try {
      const data = await getLoginAttemptsMetrics(period);
      const { success, fail } = parseCounts(data);
      const now = new Date();
      setLatest({ success, fail });
      setHistory((prev) =>
        [...prev, { time: formatTimeLabel(now), success, fail }].slice(-MAX_HISTORY_POINTS)
      );
      setError(null);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Erro ao carregar tentativas de login');
    }
  }, [period]);

  useEffect(() => {
    setHistory([]);
    setLatest(null);
  }, [period]);

  useEffect(() => {
    load();
    const interval = setInterval(load, DASHBOARD_REFRESH_MS);
    return () => clearInterval(interval);
  }, [load]);

  return (
    <section className={styles.card}>
      <h2>Tentativas de login</h2>
      {latest && (
        <p className={styles.summary}>
          Total no período: <span className={styles.success}>{latest.success} sucesso</span>
          {' · '}
          <span className={styles.fail}>{latest.fail} falha</span>
        </p>
      )}
      {error && <p className={styles.error}>{error}</p>}
      {history.length === 0 && !error && <p className={styles.empty}>Carregando...</p>}
      {history.length > 0 && !error && (
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={history}>
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
      )}
    </section>
  );
}
