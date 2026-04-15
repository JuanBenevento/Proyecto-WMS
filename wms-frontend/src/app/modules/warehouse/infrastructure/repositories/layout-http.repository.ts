import { Injectable, inject } from '@angular/core'; // Usamos inject (moderno) o constructor
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { LayoutRepository } from '../../domain/repositories/layout.repository';
import { RackSummary } from '../../domain/models/rack-summary.model';
import { LayoutDto } from '../dtos/layout.dto';
import { RackSummaryDto } from '../dtos/rack-summary.dto';
import { LayoutMapper } from '../mappers/layout.mapper';
import { environment } from '../../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class LayoutHttpRepository extends LayoutRepository {
  private readonly BASE_URL = `${environment.apiUrl}/warehouse/layout`;
  private readonly LOCATION_API = `${environment.apiUrl}/locations`;

  constructor(private http: HttpClient) { super(); }

  getLayout(): Observable<{ layoutJson: string; lastUpdate: Date }> {
    return this.http.get<LayoutDto>(`${this.BASE_URL}`)
      .pipe(
        map(dto => LayoutMapper.toDomain(dto))
      );
  }

  saveLayout(json: string): Observable<void> {
    const payload = { 
        layoutJson: json 
    };

    return this.http.put<void>(`${this.BASE_URL}`, payload);
  }


  getRackSummary(rackCode: string): Observable<RackSummary> {
    return this.http.get<RackSummaryDto>(`${this.LOCATION_API}/racks/${rackCode}/summary`)
      .pipe(
        map(dto => LayoutMapper.toRackSummaryDomain(dto))
      );
  }

  searchLocations(query: string, type: 'RACK' | 'ZONE'): Observable<string[]> {
    return this.http.get<string[]>(`${this.LOCATION_API}/search`, {
        params: { query, type: type || '' } 
    });
  }
}