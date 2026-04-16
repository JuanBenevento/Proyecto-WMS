export interface CreateUserCommand {
  username: string;
  password: string;
  role: 'ADMIN' | 'OPERATOR'; 
}