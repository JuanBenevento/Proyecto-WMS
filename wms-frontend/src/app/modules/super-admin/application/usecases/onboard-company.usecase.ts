import { Injectable, inject } from '@angular/core';
import { SaasRepository } from '../../domain/ports/saas.repository';
import { OnboardData } from '../../domain/models/onboard-data.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class OnboardCompanyUseCase {
    private repository = inject(SaasRepository);

    execute(data: OnboardData): Observable<string> {
        return this.repository.onboardCompany(data);
    }
}