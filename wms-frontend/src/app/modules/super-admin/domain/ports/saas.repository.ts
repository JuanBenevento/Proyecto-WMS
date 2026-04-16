import { Observable } from 'rxjs';
import { Tenant } from '../models/tenant.model';
import { OnboardData } from '../models/onboard-data.model';
import { UpdateTenantCommand } from '../commands/update-tenant.command';

export abstract class SaasRepository {
    abstract getAllTenants(): Observable<Tenant[]>;
    abstract onboardCompany(data: OnboardData): Observable<string>;
    abstract changeStatus(tenantId: string, isActive: boolean): Observable<void>;
    abstract updateTenant(tenantId: string, data: UpdateTenantCommand): Observable<void>;
}