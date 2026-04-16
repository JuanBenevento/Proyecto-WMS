import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuditRepository } from '../../domain/ports/repository/audit.repository';
import { AuditFilters, AuditLogModel, PaginatedResult } from '../../domain/models/audit-log.model';

@Injectable()
export class RetrieveAuditLogsUseCase {
  constructor(private repository: AuditRepository) {}

  execute(filters: AuditFilters): Observable<PaginatedResult<AuditLogModel>> {
    return this.repository.getAuditLogs(filters);
  }
}