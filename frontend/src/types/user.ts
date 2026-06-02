export type UserType = 'ADMIN' | 'USER';

export interface AuthUser {
  id: string;
  name: string;
  email: string;
  type: UserType;
}

export interface CreateUserPayload {
  name: string;
  email: string;
  username: string;
  password: string;
  type?: UserType;
}

export interface UserView {
  name: string;
  email: string;
  type: UserType;
}
