import { useAuth } from '../hooks/useAuth';
import styles from './HomePage.module.css';

export function HomePage() {
  const { user } = useAuth();

  return (
    <section className={styles.home}>
      <h1>Início</h1>
      {user && <p className={styles.welcome}>Olá, {user.name}.</p>}
    </section>
  );
}
