/**
 * Dashboard KPI models
 */

export interface DashboardKpis {
  orders: OrderKpis;
  warehouse: WarehouseKpis;
  timestamp: string;
}

export interface OrderKpis {
  total: number;
  pending: number;
  inProgress: number;
  completed: number;
}

export interface WarehouseKpis {
  total: number;
  used: number;
  available: number;
}

export interface OrderMetrics {
  total: number;
  created: number;
  confirmed: number;
  pending: number;
  picking: number;
  packed: number;
  shipped: number;
  delivered: number;
  cancelled: number;
}

export interface WarehouseMetrics {
  totalLocations: number;
  usedLocations: number;
  availableLocations: number;
}

export interface Activity {
  totalEvents: number;
  timestamp: string;
}