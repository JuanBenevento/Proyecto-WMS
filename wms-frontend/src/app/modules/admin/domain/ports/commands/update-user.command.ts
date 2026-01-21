export interface UpdateUserCommand {
  id: number;
  username: string;
  role: 'ADMIN' | 'OPERATOR';
  password?: string;
}