import { Outlet } from 'react-router-dom';
import { useState } from 'react';
import { TopMenu } from './TopMenu';
import { SettingsModal } from './SettingsModal';
import styles from './AppLayout.module.css';

export function AppLayout() {
  const [settingsOpen, setSettingsOpen] = useState(false);

  return (
    <div className={styles.layout}>
      <TopMenu onOpenSettings={() => setSettingsOpen(true)} />
      <main className={styles.main}>
        <Outlet />
      </main>
      {settingsOpen && <SettingsModal onClose={() => setSettingsOpen(false)} />}
    </div>
  );
}
