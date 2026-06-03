import { useCallback, useEffect, useState } from 'react';
import {
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { ApiError } from '../api/client';
import { getOverloadStatistics } from '../api/overload';
import { listUsers } from '../api/users';
import type { OverloadStatistics, UserSummary } from '../types/overload';
import { PeriodFilterSelect } from './PeriodFilterSelect';
import { DASHBOARD_REFRESH_MS } from '../constants/dashboard';
import type { StatisticsPeriod } from '../types/statistics';
import styles from './AdminOverloadDashboard.module.css';

const PIE_COLORS = ['#2563eb', '#7c3aed', '#db2777', '#ea580c', '#16a34a'];

export function AdminOverloadDashboard() {
  const [period, setPeriod] = useState<StatisticsPeriod>('LAST_HOUR');
  const [userId, setUserId] = useState('');
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [stats, setStats] = useState<OverloadStatistics | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listUsers()
      .then(setUsers)
      .catch(() => setUsers([]));
  }, []);

  const loadStats = useCallback(async () => {
    try {
      const data = await getOverloadStatistics(period, userId || undefined);
      setStats(data);
      setError(null);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Erro ao carregar estatísticas');
    }
  }, [period, userId]);

  useEffect(() => {
    loadStats();
    const interval = setInterval(loadStats, DASHBOARD_REFRESH_MS);
    return () => clearInterval(interval);
  }, [loadStats]);

  const pieData =
    stats?.topUsers.map((u) => ({
      name: u.userName,
      value: u.count,
    })) ?? [];

  const lineData =
    stats?.timeSeries.map((b) => ({
      time: new Date(b.bucketStart).toLocaleTimeString('pt-BR', {
        hour: '2-digit',
        minute: '2-digit',
      }),
      count: b.count,
    })) ?? [];

  return (
    <div className={styles.dashboard}>
      <div className={styles.filters}>
        <PeriodFilterSelect period={period} onPeriodChange={setPeriod} />
        <div className={styles.filterGroup}>
          <label htmlFor="user-filter">Usuário</label>
          <select
            id="user-filter"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
          >
            <option value="">Todos</option>
            {users.map((u) => (
              <option key={u.id} value={u.id}>
                {u.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {error && <p className={styles.error}>{error}</p>}

      <div className={styles.charts}>
        <section className={styles.chartCard}>
          <h2>Top 5 usuários por volume</h2>
          {pieData.length === 0 ? (
            <p className={styles.empty}>Sem dados no período.</p>
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
                  data={pieData}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  outerRadius={90}
                  label={({ name, value }) => `${name}: ${value}`}
                >
                  {pieData.map((_, index) => (
                    <Cell key={index} fill={PIE_COLORS[index % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </section>

        <section className={styles.chartCard}>
          <h2>Registros por intervalo de 15 minutos</h2>
          {lineData.length === 0 ? (
            <p className={styles.empty}>Sem dados no período.</p>
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <LineChart data={lineData}>
                <XAxis dataKey="time" interval="preserveStartEnd" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Line type="monotone" dataKey="count" stroke="#2563eb" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          )}
        </section>
      </div>
    </div>
  );
}
