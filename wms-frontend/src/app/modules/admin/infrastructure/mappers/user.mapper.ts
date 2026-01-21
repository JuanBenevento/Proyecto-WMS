import { User } from '../../domain/models/user.model';
import { UserResponseDto, CreateUserRequestDto, UpdateUserRequestDto } from '../dtos/user.dto';
import { CreateUserCommand } from '../../domain/ports/commands/create-user.command';
import { UpdateUserCommand } from '../../domain/ports/commands/update-user.command';

export class UserMapper {
  static toDomain(dto: UserResponseDto): User {
    return {
      id: dto.id,
      username: dto.username,
      role: dto.role as any,
      tenantId: dto.tenantId
    };
  }

  static toRequestDto(cmd: CreateUserCommand): CreateUserRequestDto {
    return {
      username: cmd.username,
      password: cmd.password,
      role: cmd.role
    };
  }

  static toUpdateRequestDto(cmd: UpdateUserCommand): UpdateUserRequestDto {
    const dto: UpdateUserRequestDto = {
      username: cmd.username,
      role: cmd.role
    };
    if (cmd.password) {
      dto.password = cmd.password;
    }
    return dto;
  }
}