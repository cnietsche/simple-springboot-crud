import { FormEvent, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { login } from '../api/auth';
import { createUser } from '../api/users';
import { ApiError } from '../api/client';
import { useAuth } from '../hooks/useAuth';
import type { UserType } from '../types/user';
import styles from './LoginPage.module.css';

type Mode = 'login' | 'register';

export function LoginPage() {
  const navigate = useNavigate();
  const { isAuthenticated, setUser } = useAuth();
  const [mode, setMode] = useState<Mode>('login');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [registerPassword, setRegisterPassword] = useState('');
  const [type, setType] = useState<UserType>('USER');

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  async function handleLogin(event: FormEvent) {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      const user = await login({ identifier, password });
      setUser(user);
      navigate('/', { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Erro ao fazer login');
    } finally {
      setLoading(false);
    }
  }

  async function handleRegister(event: FormEvent) {
    event.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await createUser({ name, email, username, password: registerPassword, type });
      setSuccess('Usuário criado. Faça login.');
      setMode('login');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Erro ao criar usuário');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1>CNietsche</h1>
        <div className={styles.tabs}>
          <button
            type="button"
            className={mode === 'login' ? styles.tabActive : styles.tab}
            onClick={() => { setMode('login'); setError(''); setSuccess(''); }}
          >
            Login
          </button>
          <button
            type="button"
            className={mode === 'register' ? styles.tabActive : styles.tab}
            onClick={() => { setMode('register'); setError(''); setSuccess(''); }}
          >
            Criar usuário
          </button>
        </div>

        {error && <p className={styles.error}>{error}</p>}
        {success && <p className={styles.success}>{success}</p>}

        {mode === 'login' ? (
          <form onSubmit={handleLogin} className={styles.form}>
            <label htmlFor="identifier">E-mail ou username</label>
            <input
              id="identifier"
              value={identifier}
              onChange={(e) => setIdentifier(e.target.value)}
              required
            />
            <label htmlFor="password">Senha</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <button type="submit" disabled={loading}>
              {loading ? 'Entrando...' : 'Entrar'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleRegister} className={styles.form}>
            <label htmlFor="name">Nome</label>
            <input id="name" value={name} onChange={(e) => setName(e.target.value)} required />
            <label htmlFor="email">E-mail</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <label htmlFor="username">Username</label>
            <input
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
            <label htmlFor="registerPassword">Senha</label>
            <input
              id="registerPassword"
              type="password"
              value={registerPassword}
              onChange={(e) => setRegisterPassword(e.target.value)}
              required
              minLength={6}
            />
            <label htmlFor="type">Tipo</label>
            <select id="type" value={type} onChange={(e) => setType(e.target.value as UserType)}>
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
            <button type="submit" disabled={loading}>
              {loading ? 'Criando...' : 'Criar'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
