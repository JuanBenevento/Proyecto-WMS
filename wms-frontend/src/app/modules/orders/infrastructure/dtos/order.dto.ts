/**
 * DTO para OrderResponse del backend (API)
 */
export interface OrderResponseDto {
  orderId: string;
  orderNumber: string;
  customerId: string;
  customerName: string;
  customerEmail: string;
  shippingAddress: string;
  priority: string;
  status: string;
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
  
  lineCount: number;
  totalRequestedQuantity: number;
  totalAllocatedQuantity: number;
  totalPickedQuantity: number;
  
  lines: OrderLineResponseDto[];
}

/**
 * DTO para OrderLine del backend (API)
 */
export interface OrderLineResponseDto {
  lineId: string;
  productSku: string;
  productName?: string;
  requestedQuantity: number;
  allocatedQuantity: number;
  pickedQuantity: number;
  shippedQuantity: number;
  deliveredQuantity: number;
  status: string;
  statusDescription: string;
  inventoryItemId?: string;
  locationCode?: string;
  promisedDeliveryDate?: string;
  notes?: string;
  fulfilled: boolean;
  hasShortage: boolean;
}

/**
 * DTO para paginación de Orders
 */
export interface OrderPageResponseDto {
  content: OrderResponseDto[];
  totalElements: number;
  totalPages: number;
  number: number;  // current page
  size: number;
}

/**
 * DTO para crear orden (Request)
 */
export interface CreateOrderRequestDto {
  customerId: string;
  customerName: string;
  customerEmail?: string;
  shippingAddress: string;
  priority?: string;
  promisedShipDate?: string;
  promisedDeliveryDate?: string;
  warehouseId: string;
  notes?: string;
  lines: CreateOrderLineRequestDto[];
}

/**
 * DTO para línea en request de creación
 */
export interface CreateOrderLineRequestDto {
  productSku: string;
  requestedQuantity: number;
  promisedDeliveryDate?: string;
  notes?: string;
}