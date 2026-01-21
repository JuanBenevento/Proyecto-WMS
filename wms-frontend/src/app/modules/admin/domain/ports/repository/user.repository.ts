import { Observable } from 'rxjs';
import { User } from '../../models/user.model';
import { CreateUserCommand } from '../commands/create-user.command';
import { UpdateUserCommand } from '../commands/update-user.command';

export abstract class UserRepository {
  abstract getAll(): Observable<User[]>;
  abstract create(command: CreateUserCommand): Observable<User>;
  abstract update(command: UpdateUserCommand): Observable<User>;
  abstract delete(id: number): Observable<void>;
}
