import { OrderPriority } from '../../models/order.model';

/**
 * Command para crear una nueva orden.
 */
export interface CreateOrderCommand {
  customerId: string;
  customerName: string;
  customerEmail?: string;
  shippingAddress: string;
  priority?: OrderPriority;
  promisedShipDate?: string;
  promisedDeliveryDate?: string;
  warehouseId: string;
  notes?: string;
  lines: CreateOrderLineCommand[];
}

/**
 * Command para crear una línea de orden.
 */
export interface CreateOrderLineCommand {
  productSku: string;
  requestedQuantity: number;
  promisedDeliveryDate?: string;
  notes?: string;
}