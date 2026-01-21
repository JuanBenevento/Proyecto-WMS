import { Observable } from 'rxjs';
import { Tenant } from '../models/tenant.model';
import { OnboardData } from '../models/onboard-data.model';

export abstract class SaasRepository {
    abstract getAllTenants(): Observable<Tenant[]>;
    abstract onboardCompany(data: OnboardData): Observable<string>;
    abstract changeStatus(tenantId: string, isActive: boolean): Observable<void>;
}