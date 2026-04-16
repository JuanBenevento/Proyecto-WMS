import { Injectable, inject } from '@angular/core';
import { SaasRepository } from '../../domain/ports/saas.repository';
import { Observable } from 'rxjs';
import { Tenant } from '../../domain/models/tenant.model';

@Injectable({ providedIn: 'root' }) 
export class GetTenantsUseCase {
    private repository = inject(SaasRepository);

    execute(): Observable<Tenant[]> {
        return this.repository.getAllTenants();
    }
}