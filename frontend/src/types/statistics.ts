/** Shared time window for dashboards and statistics (not tied to a single feature). */
export type StatisticsPeriod =
  | 'LAST_30_MINUTES'
  | 'LAST_HOUR'
  | 'LAST_12_HOURS'
  | 'LAST_DAY'
  | 'THIS_WEEK'
  | 'THIS_MONTH'
  | 'THIS_YEAR';

/** Shared batch sizes for bulk record generation. */
export type RecordBatchCount = 50 | 100 | 400 | 1000;
