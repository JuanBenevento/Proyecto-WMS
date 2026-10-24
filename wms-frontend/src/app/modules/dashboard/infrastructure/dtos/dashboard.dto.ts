/**
 * Dashboard API DTOs - mirrors backend ApiResponse structure
 */

export interface DashboardKpiDto {
  orders: {
    total: number;
    pending: number;
    inProgress: number;
    completed: number;
  };
  warehouse: {
    total: number;
    used: number;
    available: number;
  };
  timestamp: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  meta?: {
    page: number;
    limit: number;
    totalElements: number;
    totalPages: number;
  };
}

export type DashboardKpiResponse = ApiResponse<DashboardKpiDto>;