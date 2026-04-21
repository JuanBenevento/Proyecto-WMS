/**
 * Modelo de Dominio para Order (Órden)
 */
export interface Order {
  orderId: string;
  orderNumber: string;
  customerId: string;
  customerName: string;
  customerEmail: string;
  shippingAddress: string;
  priority: OrderPriority;
  status: OrderStatus;
  statusDescription: string;
  statusReason?: string;
  statusReasonDescription?: string;
  
  promisedShipDate?: string;
  promisedDeliveryDate?: string;
  
  warehouseId: string;
  carrierId?: string;
  trackingNumber?: string;
  notes?: string;
  
  createdAt: string;
  updatedAt: string;
  
  cancelledBy?: string;
  cancellationReason?: string;
  
  // Resúmenes
  lineCount: number;
  totalRequestedQuantity: number;
  totalAllocatedQuantity: number;
  totalPickedQuantity: number;
  
  // Líneas
  lines: OrderLine[];
}

export interface OrderLine {
  lineId: string;
  productSku: string;
  productName?: string;
  requestedQuantity: number;
  allocatedQuantity: number;
  pickedQuantity: number;
  shippedQuantity: number;
  deliveredQuantity: number;
  status: OrderLineStatus;
  statusDescription: string;
  inventoryItemId?: string;
  locationCode?: string;
  promisedDeliveryDate?: string;
  notes?: string;
  fulfilled: boolean;
  hasShortage: boolean;
}

export type OrderPriority = 'HIGH' | 'MEDIUM' | 'LOW';

export type OrderStatus = 
  | 'CREATED' 
  | 'CONFIRMED' 
  | 'PENDING' 
  | 'ALLOCATED' 
  | 'PICKING' 
  | 'PACKED' 
  | 'SHIPPED' 
  | 'DELIVERED' 
  | 'HOLD' 
  | 'CANCELLED';

export type OrderLineStatus = 
  | 'PENDING' 
  | 'ALLOCATED' 
  | 'PICKED' 
  | 'PACKED' 
  | 'SHIPPED' 
  | 'DELIVERED' 
  | 'CANCELLED' 
  | 'SHORT_PICKED';

// Filter options for listing orders
export interface OrderFilters {
  customerId?: string;
  warehouseId?: string;
  status?: OrderStatus;
  priority?: OrderPriority;
  page?: number;
  size?: number;
}

export interface OrderListResponse {
  orders: Order[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
}
