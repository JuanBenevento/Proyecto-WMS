import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { AuditRepository } from '../../domain/ports/repository/audit.repository';
import { AuditLogModel, AuditFilters, PaginatedResult } from '../../domain/models/audit-log.model';
import { PageDto } from '../dtos/page.dto';
import { AuditLogMapper } from '../mappers/audit-log.mapper';
import { environment } from '../../../../../environments/environment';

@Injectable()
export class AuditRepositoryAdapter extends AuditRepository {
  private readonly API_URL = `${environment.apiUrl}/audit`;

  constructor(private http: HttpClient) {
    super();
  }

  getAuditLogs(filters: AuditFilters): Observable<PaginatedResult<AuditLogModel>> {
    let params = new HttpParams()
      .set('page', filters.page.toString())
      .set('size', filters.size.toString())
      .set('sort', 'timestamp,desc');

    if (filters.sku) params = params.set('sku', filters.sku);
    if (filters.lpn) params = params.set('lpn', filters.lpn);
    if (filters.startDate) params = params.set('startDate', filters.startDate);
    if (filters.endDate) params = params.set('endDate', filters.endDate);

    return this.http.get<PageDto<any>>(this.API_URL, { params }).pipe(
      map(page => ({
        items: page.content.map(AuditLogMapper.toDomain),
        totalElements: page.totalElements,
        totalPages: page.totalPages
      }))
    );
  }
}