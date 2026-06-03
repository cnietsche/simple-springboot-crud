import { useState } from 'react';
import { LoginAttemptsLineChart } from '../components/metrics/LoginAttemptsLineChart';
import { PeriodFilterSelect } from '../components/PeriodFilterSelect';
import type { StatisticsPeriod } from '../types/statistics';
import styles from './MetricsPage.module.css';

export function MetricsPage() {
  const [period, setPeriod] = useState<StatisticsPeriod>('LAST_HOUR');

  return (
    <section className={styles.metrics}>
      <h1>Métricas</h1>
      <p className={styles.subtitle}>Indicadores técnicos e operacionais do sistema.</p>

      <div className={styles.toolbar}>
        <PeriodFilterSelect id="metrics-period-filter" period={period} onPeriodChange={setPeriod} />
      </div>

      <div className={styles.sections}>
        <LoginAttemptsLineChart period={period} />

        <section className={styles.placeholderCard}>
          <h2>Erros do sistema</h2>
          <p className={styles.placeholder}>
            Listagem de erros em desenvolvimento. Novos painéis podem ser adicionados nesta área.
          </p>
        </section>
      </div>
    </section>
  );
}
