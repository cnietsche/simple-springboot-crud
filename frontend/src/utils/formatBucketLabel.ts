import type { StatisticsPeriod } from '../types/statistics';

const SHORT_PERIOD: StatisticsPeriod[] = [
  'LAST_30_MINUTES',
  'LAST_HOUR',
  'LAST_12_HOURS',
  'LAST_DAY',
];

const DAY_PERIOD: StatisticsPeriod[] = ['LAST_WEEK'];

export function formatBucketLabel(bucketStart: string, period: StatisticsPeriod): string {
  const date = new Date(bucketStart);
  if (SHORT_PERIOD.includes(period)) {
    return date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }
  if (DAY_PERIOD.includes(period)) {
    return date.toLocaleDateString('pt-BR', { weekday: 'short', day: '2-digit', month: '2-digit' });
  }
  if (period === 'LAST_MONTH') {
    return date.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' });
  }
  return date.toLocaleDateString('pt-BR', { month: 'short', year: '2-digit' });
}

export function timeSeriesChartTitle(period: StatisticsPeriod): string {
  switch (period) {
    case 'LAST_30_MINUTES':
      return 'Registros por intervalo de 5 minutos';
    case 'LAST_HOUR':
      return 'Registros por intervalo de 10 minutos';
    case 'LAST_12_HOURS':
      return 'Registros por intervalo de 1 hora';
    case 'LAST_DAY':
      return 'Registros por intervalo de 2 horas';
    case 'LAST_WEEK':
      return 'Registros por dia';
    case 'LAST_MONTH':
      return 'Registros por semana';
    case 'LAST_YEAR':
      return 'Registros por mês';
    default:
      return 'Registros por intervalo';
  }
}

export function loginAttemptsChartTitle(period: StatisticsPeriod): string {
  switch (period) {
    case 'LAST_30_MINUTES':
      return 'Tentativas por intervalo de 5 minutos';
    case 'LAST_HOUR':
      return 'Tentativas por intervalo de 10 minutos';
    case 'LAST_12_HOURS':
      return 'Tentativas por intervalo de 1 hora';
    case 'LAST_DAY':
      return 'Tentativas por intervalo de 2 horas';
    case 'LAST_WEEK':
      return 'Tentativas por dia';
    case 'LAST_MONTH':
      return 'Tentativas por semana';
    case 'LAST_YEAR':
      return 'Tentativas por mês';
    default:
      return 'Tentativas por intervalo';
  }
}
