import { apiFetch } from './client';
import type { Agent, AgentFrequencyPayload } from '../types/agent';

export function listAgents(): Promise<Agent[]> {
  return apiFetch<Agent[]>('/sim-api/agents');
}

export function createAgent(frequency = 0.5): Promise<Agent> {
  return apiFetch<Agent>('/sim-api/agents', {
    method: 'POST',
    body: JSON.stringify({ frequency } satisfies AgentFrequencyPayload),
  });
}

export function updateAgentFrequency(id: string, frequency: number): Promise<Agent> {
  return apiFetch<Agent>(`/sim-api/agents/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ frequency } satisfies AgentFrequencyPayload),
  });
}

export function deleteAgent(id: string): Promise<void> {
  return apiFetch<void>(`/sim-api/agents/${id}`, { method: 'DELETE' });
}
