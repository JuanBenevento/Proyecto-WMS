import { Routes } from '@angular/router';
import { AuditRepository } from './domain/ports/repository/audit.repository';
import { AuditRepositoryAdapter } from './infrastructure/adapters/audit-repository.adapter';
import { RetrieveAuditLogsUseCase } from './application/usecases/retrieve-audit-logs.usecase';
import { AuditListComponent } from './ui/audit-list/audit-list.component';

export const AUDIT_ROUTES: Routes = [
  {
    path: '',
    providers: [
      RetrieveAuditLogsUseCase,
      { provide: AuditRepository, useClass: AuditRepositoryAdapter }
    ],
    component: AuditListComponent
  }
];