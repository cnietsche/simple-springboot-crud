import type { StatisticsPeriod } from '../types/statistics';

export const DASHBOARD_REFRESH_MS = 30_000;

export const PERIOD_OPTIONS: { value: StatisticsPeriod; label: string }[] = [
  { value: 'LAST_30_MINUTES', label: 'Últimos 30 minutos' },
  { value: 'LAST_HOUR', label: 'Última hora' },
  { value: 'LAST_12_HOURS', label: 'Últimas 12 horas' },
  { value: 'LAST_DAY', label: 'Últimas 24 horas' },
  { value: 'LAST_WEEK', label: 'Última semana' },
  { value: 'LAST_MONTH', label: 'Último mês' },
  { value: 'LAST_YEAR', label: 'Último ano' },
];
