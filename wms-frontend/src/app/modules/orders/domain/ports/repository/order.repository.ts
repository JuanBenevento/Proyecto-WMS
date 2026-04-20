import { Observable } from 'rxjs';
import { Order, OrderFilters, OrderListResponse } from '../models/order.model';

/**
 * Puerto (Interface) para el repositorio de Órdenes.
 * Define las operaciones disponibles para gestionar órdenes.
 */
export interface OrderRepository {
  /**
   * Lista órdenes con filtros opcionales.
   */
  listOrders(filters: OrderFilters): Observable<OrderListResponse>;
  
  /**
   * Obtiene una orden por su ID.
   */
  getOrder(orderId: string): Observable<Order>;
  
  /**
   * Crea una nueva orden.
   */
  createOrder(command: CreateOrderCommand): Observable<Order>;
  
  /**
   * Agrega una línea a una orden existente.
   */
  addLine(orderId: string, command: CreateOrderLineCommand): Observable<Order>;
  
  /**
   * Confirma una orden.
   */
  confirmOrder(orderId: string): Observable<Order>;
  
  /**
   * Cancela una orden.
   */
  cancelOrder(orderId: string, reason: string): Observable<Order>;
  
  /**
   * Poné una orden en espera.
   */
  holdOrder(orderId: string, reason?: string): Observable<Order>;
  
  /**
   * Libera una orden de espera.
   */
  releaseOrder(orderId: string): Observable<Order>;
  
  /**
   * Inicia el picking de una orden.
   */
  startPicking(orderId: string): Observable<Order>;
  
  /**
   * Empaca una orden.
   */
  packOrder(orderId: string): Observable<Order>;
  
  /**
   * Envía una orden.
   */
  shipOrder(orderId: string, carrierId: string, trackingNumber?: string): Observable<Order>;
  
  /**
   * Marca una orden como entregada.
   */
  deliverOrder(orderId: string): Observable<Order>;
}

/**
 * Command para crear una nueva orden.
 */
export interface CreateOrderCommand {
  customerId: string;
  customerName: string;
  customerEmail?: string;
  shippingAddress: string;
  priority?: 'HIGH' | 'MEDIUM' | 'LOW';
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
