import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { OrderRepository, CreateOrderCommand, CreateOrderLineCommand } from '../../domain/ports/repository/order.repository';
import { Order, OrderFilters, OrderListResponse } from '../../domain/models/order.model';

@Injectable()
export class ManageOrderUseCase {
  // Inyectamos el Puerto (Abstracto)
  constructor(private orderRepository: OrderRepository) {}

  listOrders(filters: OrderFilters): Observable<OrderListResponse> {
    return this.orderRepository.listOrders(filters);
  }

  getOrder(orderId: string): Observable<Order> {
    return this.orderRepository.getOrder(orderId);
  }

  createOrder(command: CreateOrderCommand): Observable<Order> {
    return this.orderRepository.createOrder(command);
  }

  // Acciones de transición de estado
  confirmOrder(orderId: string): Observable<Order> {
    return this.orderRepository.confirmOrder(orderId);
  }

  cancelOrder(orderId: string, reason: string): Observable<Order> {
    return this.orderRepository.cancelOrder(orderId, reason);
  }

  holdOrder(orderId: string, reason?: string): Observable<Order> {
    return this.orderRepository.holdOrder(orderId, reason);
  }

  releaseOrder(orderId: string): Observable<Order> {
    return this.orderRepository.releaseOrder(orderId);
  }

  startPicking(orderId: string): Observable<Order> {
    return this.orderRepository.startPicking(orderId);
  }

  packOrder(orderId: string): Observable<Order> {
    return this.orderRepository.packOrder(orderId);
  }

  shipOrder(orderId: string, carrierId: string, trackingNumber?: string): Observable<Order> {
    return this.orderRepository.shipOrder(orderId, carrierId, trackingNumber);
  }

  deliverOrder(orderId: string): Observable<Order> {
    return this.orderRepository.deliverOrder(orderId);
  }
}