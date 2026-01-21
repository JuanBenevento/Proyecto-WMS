import { Injectable, inject } from '@angular/core';
import { CreateUserCommand } from '../../domain/ports/commands/create-user.command';
import { UserRepository } from '../../domain/ports/repository/user.repository';
import { UpdateUserCommand } from '../../domain/ports/commands/update-user.command';

@Injectable({ providedIn: 'root' })
export class ManageUsersUseCase {
  private repo = inject(UserRepository);

  getUsers() {
    return this.repo.getAll();
  }

  createUser(command: CreateUserCommand) {
    if (command.password.length < 4) {
        throw new Error('La contraseña es muy corta (Validación Dominio)');
    }
    return this.repo.create(command);
  }

  updateUser(command: UpdateUserCommand) {
    if (command.password && command.password.length < 6) {
      throw new Error('La nueva contraseña debe tener al menos 6 caracteres');
    }
    return this.repo.update(command);
  }

  deleteUser(id: number) {
    return this.repo.delete(id);
  }
}