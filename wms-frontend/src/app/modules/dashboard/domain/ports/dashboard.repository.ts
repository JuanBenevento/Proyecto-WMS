/**
 * Dashboard Repository Port
 */

import { Observable } from 'rxjs';
import { DashboardKpis, OrderMetrics, WarehouseMetrics, Activity } from '../models/kpi.model';

export interface DashboardRepository {
  getKpis(): Observable<DashboardKpis>;
  getOrderMetrics(): Observable<OrderMetrics>;
  getWarehouseMetrics(): Observable<WarehouseMetrics>;
  getActivity(): Observable<Activity>;
}