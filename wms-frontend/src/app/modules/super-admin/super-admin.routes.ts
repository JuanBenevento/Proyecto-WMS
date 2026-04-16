import { Routes } from '@angular/router';
import { SaasRepository } from './domain/ports/saas.repository';
import { SaasHttpAdapter } from './infrastructure/adapters/saas-http.adapter';
import { GetTenantsUseCase } from './application/usecases/get-tenants.usecase';
import { OnboardCompanyUseCase } from './application/usecases/onboard-company.usecase';
import { OnboardingComponent } from './ui/onboarding/onboarding.component';
import { TenantListComponent } from './ui/tentat-list/tenant-list.component';
import { ChangeTenantStatusUseCase } from './application/usecases/change-tenant-status.usecase';
import { UpdateTenantUseCase } from './application/usecases/update-tenant.usecase';

export const SUPER_ADMIN_ROUTES: Routes = [
  {
    path: '',
    providers: [
      { provide: SaasRepository, useClass: SaasHttpAdapter },
      GetTenantsUseCase,
      OnboardCompanyUseCase,
      ChangeTenantStatusUseCase,
      UpdateTenantUseCase
    ],
    children: [
      { path: '', redirectTo: 'tenants', pathMatch: 'full' },
      { path: 'tenants', component: TenantListComponent },
      { path: 'onboarding', component: OnboardingComponent }
    ]
  }
];