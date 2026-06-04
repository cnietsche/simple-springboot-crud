import { useEffect, useState } from 'react';
import { deleteAgent, updateAgentFrequency } from '../api/agents';
import type { Agent } from '../types/agent';
import styles from './AgentCard.module.css';

interface AgentCardProps {
  agent: Agent;
  onUpdated: (agent: Agent) => void;
  onDeleted: (id: string) => void;
}

export function AgentCard({ agent, onUpdated, onDeleted }: AgentCardProps) {
  const [frequency, setFrequency] = useState(agent.frequency);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!saving) {
      setFrequency(agent.frequency);
    }
  }, [agent.frequency, saving]);

  async function handleFrequencyChange(value: number) {
    setFrequency(value);
    setSaving(true);
    try {
      const updated = await updateAgentFrequency(agent.id, value);
      onUpdated(updated);
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    await deleteAgent(agent.id);
    onDeleted(agent.id);
  }

  const created = new Date(agent.createdAt).toLocaleString();

  return (
    <article className={styles.card}>
      <div className={styles.header}>
        {agent.busy && (
          <span className={styles.busyDot} aria-label="Em execução" title="Em execução" />
        )}
        <span className={styles.id} title={agent.id}>
          {agent.id.slice(0, 8)}…
        </span>
        <button type="button" className={styles.deleteButton} onClick={handleDelete} aria-label="Remover agent">
          ×
        </button>
      </div>
      <p className={styles.meta}>Criado: {created}</p>
      <p className={styles.meta}>Logins: {agent.knownLoginCount}</p>
      <div className={styles.sliderRow}>
        <label className={styles.sliderLabel} htmlFor={`freq-${agent.id}`}>
          <span>Frequência</span>
          <span>{frequency.toFixed(2)}{saving ? ' …' : ''}</span>
        </label>
        <input
          id={`freq-${agent.id}`}
          type="range"
          min={0}
          max={1}
          step={0.01}
          value={frequency}
          className={styles.slider}
          onChange={(e) => handleFrequencyChange(Number(e.target.value))}
        />
      </div>
    </article>
  );
}
