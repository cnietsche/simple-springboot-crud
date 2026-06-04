export interface Agent {
  id: string;
  createdAt: string;
  frequency: number;
  running: boolean;
  busy: boolean;
  knownLoginCount: number;
  loggedInUserId: string | null;
  lastAction: string | null;
  lastActionAt: string | null;
}

export interface AgentFrequencyPayload {
  frequency: number;
}
