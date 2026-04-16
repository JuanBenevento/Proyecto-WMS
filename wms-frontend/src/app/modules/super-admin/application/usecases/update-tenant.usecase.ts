import { Injectable, inject } from '@angular/core';
import { SaasRepository } from '../../domain/ports/saas.repository';
import { Observable } from 'rxjs';
import { UpdateTenantCommand } from '../../domain/commands/update-tenant.command';

@Injectable()
export class UpdateTenantUseCase {
    private repository = inject(SaasRepository);

    execute(tenantId: string, data: UpdateTenantCommand): Observable<void> {
        return this.repository.updateTenant(tenantId, data);
    }
}