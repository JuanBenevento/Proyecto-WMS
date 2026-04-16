export interface UserResponseDto {
  id: number;
  username: string;
  role: string;
  tenantId: string;
}

export interface CreateUserRequestDto {
  username: string;
  password: string;
  role: string;
}

export interface UpdateUserRequestDto {
  username: string;
  role: string;
  password?: string;
}