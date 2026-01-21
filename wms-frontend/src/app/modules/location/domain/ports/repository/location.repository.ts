import { Observable } from 'rxjs';
import { LocationModel } from '../../models/location.model';
import { CreateLocationCommand } from '../commands/create-location.command';

export abstract class LocationRepository {
  abstract getAll(): Observable<LocationModel[]>;
  abstract getByCode(code: string): Observable<LocationModel>;
  abstract create(command: CreateLocationCommand): Observable<LocationModel>;
  abstract update(code: string, command: CreateLocationCommand): Observable<LocationModel>;
  abstract delete(code: string): Observable<void>;
}