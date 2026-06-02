import { useAuth } from '../hooks/useAuth';
import { AdminOverloadDashboard } from '../components/AdminOverloadDashboard';
import { UserOverloadPanel } from '../components/UserOverloadPanel';
import styles from './HomePage.module.css';

export function HomePage() {
  const { user } = useAuth();

  return (
    <section className={styles.home}>
      <h1>Início</h1>
      {user && <p className={styles.welcome}>Olá, {user.name}.</p>}
      {user?.type === 'ADMIN' ? (
        <AdminOverloadDashboard />
      ) : user ? (
        <UserOverloadPanel userId={user.id} />
      ) : null}
    </section>
  );
}
