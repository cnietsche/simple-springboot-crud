import { useCallback, useEffect, useState } from 'react';
import { AgentCard } from '../components/AgentCard';
import { createAgent, listAgents } from '../api/agents';
import type { Agent } from '../types/agent';
import styles from './AgentsPage.module.css';

const POLL_MS = 1500;

export function AgentsPage() {
  const [agents, setAgents] = useState<Agent[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);

  const loadAgents = useCallback(async () => {
    try {
      const data = await listAgents();
      setAgents(data);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Falha ao carregar agents');
    }
  }, []);

  useEffect(() => {
    loadAgents();
    const id = window.setInterval(loadAgents, POLL_MS);
    return () => window.clearInterval(id);
  }, [loadAgents]);

  async function handleCreate() {
    setCreating(true);
    try {
      const created = await createAgent(0.5);
      setAgents((prev) => [...prev, created].sort(
        (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      ));
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Falha ao criar agent');
    } finally {
      setCreating(false);
    }
  }

  function handleUpdated(updated: Agent) {
    setAgents((prev) =>
      prev.map((a) => (a.id === updated.id ? updated : a))
    );
  }

  function handleDeleted(id: string) {
    setAgents((prev) => prev.filter((a) => a.id !== id));
  }

  return (
    <section className={styles.page}>
      <h1>Agents</h1>
      <p className={styles.subtitle}>
        Simulação de usuários executando ações no sistema.
      </p>

      <div className={styles.toolbar}>
        <button
          type="button"
          className={styles.addButton}
          onClick={handleCreate}
          disabled={creating}
          aria-label="Adicionar agent"
        >
          +
        </button>
      </div>

      {error && <p className={styles.error}>{error}</p>}

      <div className={styles.grid}>
        {agents.length === 0 ? (
          <p className={styles.empty}>Nenhum agent em execução. Clique em + para criar.</p>
        ) : (
          agents.map((agent) => (
            <AgentCard
              key={agent.id}
              agent={agent}
              onUpdated={handleUpdated}
              onDeleted={handleDeleted}
            />
          ))
        )}
      </div>
    </section>
  );
}
