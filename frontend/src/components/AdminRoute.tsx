import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

interface AdminRouteProps {
  children: React.ReactNode;
}

export function AdminRoute({ children }: AdminRouteProps) {
  const { user } = useAuth();

  if (user?.type !== 'ADMIN') {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}
