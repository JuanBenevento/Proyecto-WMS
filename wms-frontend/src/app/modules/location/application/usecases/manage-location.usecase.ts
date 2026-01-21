import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LocationRepository } from '../../domain/ports/repository/location.repository';
import { LocationModel } from '../../domain/models/location.model';
import { CreateLocationCommand } from '../../domain/ports/commands/create-location.command';

@Injectable()
export class ManageLocationUseCase {
  constructor(private locationRepository: LocationRepository) {}

  getAll(): Observable<LocationModel[]> {
    return this.locationRepository.getAll();
  }

  getByCode(code: string): Observable<LocationModel> {
    return this.locationRepository.getByCode(code);
  }

  create(command: CreateLocationCommand): Observable<LocationModel> {
    return this.locationRepository.create(command);
  }

  update(code: string, command: CreateLocationCommand): Observable<LocationModel> {
    return this.locationRepository.update(code, command);
  }

  delete(code: string): Observable<void> {
    return this.locationRepository.delete(code);
  }
}