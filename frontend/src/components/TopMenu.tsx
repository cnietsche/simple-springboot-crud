import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import styles from './TopMenu.module.css';

interface TopMenuProps {
  onOpenSettings: () => void;
}

export function TopMenu({ onOpenSettings }: TopMenuProps) {
  const { logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <header className={styles.header}>
      <nav className={styles.left}>
        <span className={styles.brand}>CNietsche</span>
        <NavLink
          to="/"
          end
          className={({ isActive }) => (isActive ? styles.active : styles.link)}
        >
          Início
        </NavLink>
        <button type="button" className={styles.linkButton} onClick={onOpenSettings}>
          Configurações
        </button>
      </nav>
      <button type="button" className={styles.logout} onClick={handleLogout}>
        Logoff
      </button>
    </header>
  );
}
