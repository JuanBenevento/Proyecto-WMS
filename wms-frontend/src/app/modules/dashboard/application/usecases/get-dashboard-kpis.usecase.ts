/**
 * Get Dashboard KPIs Use Case
 */

import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DashboardKpis } from '../../domain/models/kpi.model';
import { calculateUtilization, calculateCompletionRate } from '../../infrastructure/mappers/dashboard.mapper';
import { DashboardRepositoryAdapter } from '../../infrastructure/adapters/dashboard.adapter';

@Injectable({
  providedIn: 'root'
})
export class GetDashboardKpisUseCase {
  private readonly repository = inject(DashboardRepositoryAdapter);

  execute(): Observable<DashboardKpis> {
    return this.repository.getKpis();
  }

  getUtilization(kpis: DashboardKpis): number {
    return calculateUtilization(kpis);
  }

  getCompletionRate(kpis: DashboardKpis): number {
    return calculateCompletionRate(kpis);
  }
}