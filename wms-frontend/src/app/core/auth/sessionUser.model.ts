export type UserRole = 'SUPER_ADMIN' | 'ADMIN' | 'OPERATOR';

export interface UserSession {
  username: string;
  role: UserRole;
  tenantId?: string;
  token: string;
}