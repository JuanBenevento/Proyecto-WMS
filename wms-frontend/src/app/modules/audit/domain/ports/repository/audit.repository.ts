import { Observable } from 'rxjs';
import { AuditLogModel, AuditFilters, PaginatedResult } from '../../models/audit-log.model';

export abstract class AuditRepository {
  abstract getAuditLogs(filters: AuditFilters): Observable<PaginatedResult<AuditLogModel>>;
}