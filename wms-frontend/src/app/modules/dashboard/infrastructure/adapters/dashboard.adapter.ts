/**
 * Dashboard Repository Implementation
 */

import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { DashboardRepository } from '../../domain/ports/dashboard.repository';
import { DashboardKpis, OrderMetrics, WarehouseMetrics, Activity } from '../../domain/models/kpi.model';
import { DashboardHttpAdapter } from './dashboard-http.adapter';
import { mapDashboardKpis } from '../mappers/dashboard.mapper';

@Injectable({
  providedIn: 'root'
})
export class DashboardRepositoryAdapter implements DashboardRepository {
  private readonly httpAdapter = inject(DashboardHttpAdapter);

  getKpis(): Observable<DashboardKpis> {
    return this.httpAdapter.getKpis().pipe(
      map(response => mapDashboardKpis(response))
    );
  }

  getOrderMetrics(): Observable<OrderMetrics> {
    return this.httpAdapter.getOrderMetrics().pipe(
      map(response => response.data as unknown as OrderMetrics)
    );
  }

  getWarehouseMetrics(): Observable<WarehouseMetrics> {
    return this.httpAdapter.getWarehouseMetrics().pipe(
      map(response => response.data as unknown as WarehouseMetrics)
    );
  }

  getActivity(): Observable<Activity> {
    return this.httpAdapter.getActivity().pipe(
      map(response => response.data as unknown as Activity)
    );
  }
}