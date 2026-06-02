import { apiFetch } from './client';
import type { AuthUser } from '../types/user';

export interface LoginPayload {
  identifier: string;
  password: string;
}

export function login(payload: LoginPayload): Promise<AuthUser> {
  return apiFetch<AuthUser>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
