import { Injectable, inject } from '@angular/core';
import { SaasRepository } from '../../domain/ports/saas.repository';
import { Observable } from 'rxjs';

@Injectable()
export class ChangeTenantStatusUseCase {
    private repository = inject(SaasRepository);

    execute(tenantId: string, makeActive: boolean): Observable<void> {
        return this.repository.changeStatus(tenantId, makeActive);
    }
}