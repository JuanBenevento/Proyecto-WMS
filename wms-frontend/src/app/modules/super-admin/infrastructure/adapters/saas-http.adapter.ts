import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { SaasRepository } from '../../domain/ports/saas.repository';
import { Tenant } from '../../domain/models/tenant.model';
import { OnboardData } from '../../domain/models/onboard-data.model';
import { SaasMapper } from '../mappers/saas.mapper';
import { TenantResponseDto } from '../dtos/tenant-response.dto';
import { environment } from '../../../../../environments/environment';

@Injectable()
export class SaasHttpAdapter implements SaasRepository {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/saas`;

    getAllTenants(): Observable<Tenant[]> {
        return this.http.get<TenantResponseDto[]>(`${this.apiUrl}/tenants`)
            .pipe(
                map(dtos => dtos.map(dto => SaasMapper.toDomain(dto)))
            );
    }

    onboardCompany(data: OnboardData): Observable<string> {
        return this.http.post(`${this.apiUrl}/onboarding`, data, { responseType: 'text' });
    }
}