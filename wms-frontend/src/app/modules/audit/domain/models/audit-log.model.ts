export type StockMovementType = 'RECEPCION' | 'AJUSTE' | 'MOVIMIENTO' | 'SALIDA';

export interface AuditLogModel {
  id: number;
  timestamp: string; 
  type: StockMovementType;
  sku: string;
  lpn: string;
  quantity: number;
  oldQuantity?: number; 
  newQuantity?: number;
  user: string;
  reason: string;
}

export interface AuditFilters {
  sku?: string;
  lpn?: string;
  startDate?: string;
  endDate?: string;
  page: number;
  size: number;
}

export interface PaginatedResult<T> {
  items: T[];
  totalElements: number;
  totalPages: number;
}