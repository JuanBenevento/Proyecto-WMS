import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { LocationRepository } from '../../domain/ports/repository/location.repository';
import { LocationModel } from '../../domain/models/location.model';
import { CreateLocationCommand } from '../../domain/ports/commands/create-location.command';
import { LocationDto } from '../dtos/location.dto';
import { LocationMapper } from '../mappers/location.mapper';
import { environment } from '../../../../../environments/environment';

@Injectable()
export class LocationRepositoryAdapter extends LocationRepository {
  private readonly API_URL = `${environment.apiUrl}/locations`;

  constructor(private http: HttpClient) {
    super();
  }

  getAll(): Observable<LocationModel[]> {
    return this.http.get<LocationDto[]>(this.API_URL).pipe(
      map(dtos => dtos.map(LocationMapper.toDomain))
    );
  }

  getByCode(code: string): Observable<LocationModel> {
    return this.http.get<LocationDto>(`${this.API_URL}/${code}`).pipe(
      map(LocationMapper.toDomain)
    );
  }

  create(command: CreateLocationCommand): Observable<LocationModel> {
    return this.http.post<LocationDto>(this.API_URL, command).pipe(
      map(LocationMapper.toDomain)
    );
  }

  update(code: string, command: CreateLocationCommand): Observable<LocationModel> {
    return this.http.put<LocationDto>(`${this.API_URL}/${code}`, command).pipe(
      map(LocationMapper.toDomain)
    );
  }

  delete(code: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${code}`);
  }
}