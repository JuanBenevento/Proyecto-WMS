export interface User {
  id: number;
  username: string;
  role: 'SUPER_ADMIN' | 'ADMIN' | 'OPERATOR';
  tenantId: string;
}