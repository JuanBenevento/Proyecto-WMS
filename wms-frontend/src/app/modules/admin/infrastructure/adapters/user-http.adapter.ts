import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { CreateUserCommand } from '../../domain/ports/commands/create-user.command';
import { User } from '../../domain/models/user.model';
import { UserMapper } from '../mappers/user.mapper';
import { UserResponseDto } from '../dtos/user.dto';
import { UserRepository } from '../../domain/ports/repository/user.repository';
import { UpdateUserCommand } from '../../domain/ports/commands/update-user.command';

@Injectable({ providedIn: 'root' })
export class UserHttpAdapter extends UserRepository {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/users`; 

  getAll(): Observable<User[]> {
    return this.http.get<UserResponseDto[]>(this.apiUrl).pipe(
      map(dtos => dtos.map(UserMapper.toDomain))
    );
  }

  create(cmd: CreateUserCommand): Observable<User> {
    const dto = UserMapper.toRequestDto(cmd);
    return this.http.post<UserResponseDto>(this.apiUrl, dto).pipe(
      map(UserMapper.toDomain)
    );
  }

  update(cmd: UpdateUserCommand): Observable<User> {
    const dto = UserMapper.toUpdateRequestDto(cmd);
    // PUT /users/{id}
    return this.http.put<UserResponseDto>(`${this.apiUrl}/${cmd.id}`, dto).pipe(
      map(UserMapper.toDomain)
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}