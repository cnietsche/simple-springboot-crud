import { apiFetch } from './client';
import type { UserSummary } from '../types/overload';
import type { AuthUser, CreateUserPayload, UserType, UserView } from '../types/user';

export function listUsers(): Promise<UserSummary[]> {
  return apiFetch<UserSummary[]>('/api/users');
}

export function createUser(payload: CreateUserPayload): Promise<AuthUser> {
  return apiFetch<AuthUser>('/api/users', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function getUser(id: string): Promise<UserView> {
  return apiFetch<UserView>(`/api/users/${id}`);
}

export function changeUserType(id: string, type: UserType): Promise<UserView> {
  return apiFetch<UserView>(`/api/users/${id}/type`, {
    method: 'PATCH',
    body: JSON.stringify({ type }),
  });
}
