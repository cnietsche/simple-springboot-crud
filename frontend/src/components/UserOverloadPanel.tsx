import { FormEvent, useCallback, useEffect, useState } from 'react';
import { ApiError } from '../api/client';
import { generateOverload, listOverloads } from '../api/overload';
import type { OverloadRecord } from '../types/overload';
import type { RecordBatchCount } from '../types/statistics';
import styles from './UserOverloadPanel.module.css';

const BATCH_OPTIONS: { value: RecordBatchCount; label: string }[] = [
  { value: 50, label: '50 registros' },
  { value: 100, label: '100 registros' },
  { value: 400, label: '400 registros' },
  { value: 1000, label: '1000 registros' },
];

const PAGE_SIZE = 50;

interface UserOverloadPanelProps {
  userId: string;
}

export function UserOverloadPanel({ userId }: UserOverloadPanelProps) {
  const [count, setCount] = useState<RecordBatchCount | ''>('');
  const [records, setRecords] = useState<OverloadRecord[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadPage = useCallback(
    async (pageIndex: number) => {
      setLoading(true);
      setError(null);
      try {
        const result = await listOverloads(userId, pageIndex, PAGE_SIZE);
        setRecords(result.content);
        setPage(result.page);
        setTotalPages(result.totalPages);
      } catch (err) {
        setError(err instanceof ApiError ? err.message : 'Erro ao carregar registros');
      } finally {
        setLoading(false);
      }
    },
    [userId]
  );

  useEffect(() => {
    loadPage(0);
  }, [loadPage]);

  async function handleGenerate(e: FormEvent) {
    e.preventDefault();
    if (count === '') {
      setError('Selecione a quantidade de registros');
      return;
    }
    setGenerating(true);
    setError(null);
    try {
      await generateOverload(userId, count);
      await loadPage(0);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Erro ao gerar overload');
    } finally {
      setGenerating(false);
    }
  }

  function formatDate(iso: string) {
    return new Date(iso).toLocaleString('pt-BR');
  }

  return (
    <div className={styles.panel}>
      <form className={styles.form} onSubmit={handleGenerate}>
        <label htmlFor="overload-count">Quantidade de registros</label>
        <select
          id="overload-count"
          value={count}
          onChange={(e) => setCount(Number(e.target.value) as RecordBatchCount)}
          required
        >
          <option value="" disabled>
            Selecione...
          </option>
          {BATCH_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
        <button type="submit" disabled={generating || count === ''}>
          {generating ? 'Gerando...' : 'Gerar overload'}
        </button>
      </form>

      {error && <p className={styles.error}>{error}</p>}

      <section className={styles.listSection}>
        <h2>Registros de overload</h2>
        {loading ? (
          <p>Carregando...</p>
        ) : records.length === 0 ? (
          <p className={styles.empty}>Nenhum registro encontrado.</p>
        ) : (
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Data</th>
                <th>Valor</th>
              </tr>
            </thead>
            <tbody>
              {records.map((record) => (
                <tr key={record.id}>
                  <td>{formatDate(record.date)}</td>
                  <td className={styles.valueCell} title={record.value}>
                    {record.value.length > 80
                      ? `${record.value.slice(0, 80)}…`
                      : record.value}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {totalPages > 1 && (
          <div className={styles.pagination}>
            <button
              type="button"
              disabled={page <= 0 || loading}
              onClick={() => loadPage(page - 1)}
            >
              Anterior
            </button>
            <span>
              Página {page + 1} de {totalPages}
            </span>
            <button
              type="button"
              disabled={page >= totalPages - 1 || loading}
              onClick={() => loadPage(page + 1)}
            >
              Próxima
            </button>
          </div>
        )}
      </section>
    </div>
  );
}
