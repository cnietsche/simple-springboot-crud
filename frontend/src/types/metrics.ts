export type LoginAttemptOutcomeLabel = 'Success' | 'Fail';

export interface LoginAttemptMetric {
  outcome: LoginAttemptOutcomeLabel;
  count: number;
}
