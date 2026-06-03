import { PERIOD_OPTIONS } from '../constants/dashboard';
import type { StatisticsPeriod } from '../types/statistics';
import styles from './PeriodFilterSelect.module.css';

interface PeriodFilterSelectProps {
  id?: string;
  period: StatisticsPeriod;
  onPeriodChange: (period: StatisticsPeriod) => void;
}

export function PeriodFilterSelect({
  id = 'period-filter',
  period,
  onPeriodChange,
}: PeriodFilterSelectProps) {
  return (
    <div className={styles.filterGroup}>
      <label htmlFor={id}>Período</label>
      <select
        id={id}
        value={period}
        onChange={(e) => onPeriodChange(e.target.value as StatisticsPeriod)}
      >
        {PERIOD_OPTIONS.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  );
}
