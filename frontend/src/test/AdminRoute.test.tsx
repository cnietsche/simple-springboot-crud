import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, beforeEach } from 'vitest';
import { AuthProvider } from '../context/AuthContext';
import { AdminRoute } from '../components/AdminRoute';
import type { AuthUser } from '../types/user';

const adminUser: AuthUser = {
  id: '11111111-1111-1111-1111-111111111111',
  name: 'Admin',
  email: 'admin@test.com',
  type: 'ADMIN',
};

const regularUser: AuthUser = {
  ...adminUser,
  id: '22222222-2222-2222-2222-222222222222',
  name: 'User',
  email: 'user@test.com',
  type: 'USER',
};

function renderAdminRoute(user: AuthUser | null) {
  if (user) {
    sessionStorage.setItem('auth_user', JSON.stringify(user));
  }

  render(
    <MemoryRouter initialEntries={['/metrics']}>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<div>Home</div>} />
          <Route
            path="/metrics"
            element={
              <AdminRoute>
                <div>Metrics</div>
              </AdminRoute>
            }
          />
        </Routes>
      </AuthProvider>
    </MemoryRouter>
  );
}

describe('AdminRoute', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  it('allows ADMIN to view protected content', async () => {
    renderAdminRoute(adminUser);

    await waitFor(() => {
      expect(screen.getByText('Metrics')).toBeInTheDocument();
    });
  });

  it('redirects non-ADMIN to home', async () => {
    renderAdminRoute(regularUser);

    await waitFor(() => {
      expect(screen.getByText('Home')).toBeInTheDocument();
    });
  });
});
