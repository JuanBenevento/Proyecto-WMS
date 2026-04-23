/**
 * Dashboard HTTP Adapter - calls the backend API
 */

import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardKpiResponse } from '../dtos/dashboard.dto';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DashboardHttpAdapter {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getKpis(): Observable<DashboardKpiResponse> {
    return this.http.get<DashboardKpiResponse>(`${this.baseUrl}/dashboard/kpis`);
  }

  getOrderMetrics(): Observable<DashboardKpiResponse> {
    return this.http.get<DashboardKpiResponse>(`${this.baseUrl}/dashboard/metrics/orders`);
  }

  getWarehouseMetrics(): Observable<DashboardKpiResponse> {
    return this.http.get<DashboardKpiResponse>(`${this.baseUrl}/dashboard/metrics/warehouse`);
  }

  getActivity(): Observable<DashboardKpiResponse> {
    return this.http.get<DashboardKpiResponse>(`${this.baseUrl}/dashboard/activity`);
  }
}