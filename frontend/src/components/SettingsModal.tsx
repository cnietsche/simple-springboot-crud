import { FormEvent, useState } from 'react';
import { changeUserType } from '../api/users';
import { ApiError } from '../api/client';
import { useAuth } from '../hooks/useAuth';
import type { UserType } from '../types/user';
import styles from './SettingsModal.module.css';

interface SettingsModalProps {
  onClose: () => void;
}

export function SettingsModal({ onClose }: SettingsModalProps) {
  const { user, setUser } = useAuth();
  const [type, setType] = useState<UserType>(user?.type ?? 'USER');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (!user) return null;

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      await changeUserType(user.id, type);
      setUser({ ...user, type });
      onClose();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Erro ao alterar tipo';
      setError(message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className={styles.overlay} role="dialog" aria-modal="true">
      <div className={styles.modal}>
        <h2>Configurações</h2>
        <p>Alterar tipo do usuário</p>
        <form onSubmit={handleSubmit}>
          <label htmlFor="userType">Tipo</label>
          <select
            id="userType"
            value={type}
            onChange={(e) => setType(e.target.value as UserType)}
          >
            <option value="USER">USER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
          {error && <p className={styles.error}>{error}</p>}
          <div className={styles.actions}>
            <button type="button" onClick={onClose}>
              Cancelar
            </button>
            <button type="submit" disabled={loading}>
              {loading ? 'Salvando...' : 'Salvar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
