/**
 * Dashboard mapper - converts DTOs to domain models
 */

import { DashboardKpiDto, ApiResponse } from '../dtos/dashboard.dto';
import { DashboardKpis, OrderKpis, WarehouseKpis } from '../../domain/models/kpi.model';

export function mapDashboardKpis(response: ApiResponse<DashboardKpiDto>): DashboardKpis {
  const data = response.data;
  return {
    orders: mapOrderKpis(data.orders),
    warehouse: mapWarehouseKpis(data.warehouse),
    timestamp: data.timestamp
  };
}

function mapOrderKpis(dto: DashboardKpiDto['orders']): OrderKpis {
  return {
    total: dto.total,
    pending: dto.pending,
    inProgress: dto.inProgress,
    completed: dto.completed
  };
}

function mapWarehouseKpis(dto: DashboardKpiDto['warehouse']): WarehouseKpis {
  return {
    total: dto.total,
    used: dto.used,
    available: dto.available
  };
}

export function calculateUtilization(kpis: DashboardKpis): number {
  if (kpis.warehouse.total === 0) return 0;
  return Math.round((kpis.warehouse.used / kpis.warehouse.total) * 100);
}

export function calculateCompletionRate(kpis: DashboardKpis): number {
  if (kpis.orders.total === 0) return 0;
  return Math.round((kpis.orders.completed / kpis.orders.total) * 100);
}