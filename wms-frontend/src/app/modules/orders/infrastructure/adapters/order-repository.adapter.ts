import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { OrderRepository, CreateOrderCommand, CreateOrderLineCommand } from '../../domain/ports/repository/order.repository';
import { Order, OrderFilters, OrderListResponse } from '../../domain/models/order.model';
import { OrderResponseDto, OrderPageResponseDto } from '../dtos/order.dto';
import { OrderMapper } from '../mappers/order.mapper';
import { environment } from '../../../../../environments/environment';

@Injectable() 
export class OrderRepositoryAdapter extends OrderRepository {
  private readonly API_URL = `${environment.apiUrl}/orders`;

  constructor(private http: HttpClient) {
    super();
  }

  listOrders(filters: OrderFilters): Observable<OrderListResponse> {
    let params = new HttpParams();
    
    if (filters.status) params = params.set('status', filters.status);
    if (filters.priority) params = params.set('priority', filters.priority);
    if (filters.customerId) params = params.set('customerId', filters.customerId);
    if (filters.warehouseId) params = params.set('warehouseId', filters.warehouseId);
    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());

    return this.http.get<OrderPageResponseDto>(this.API_URL, { params }).pipe(
      map(dto => OrderMapper.toDomainList(dto))
    );
  }

  getOrder(orderId: string): Observable<Order> {
    return this.http.get<OrderResponseDto>(`${this.API_URL}/${orderId}`).pipe(
      map(dto => OrderMapper.toDomain(dto))
    );
  }

  createOrder(command: CreateOrderCommand): Observable<Order> {
    return this.http.post<OrderResponseDto>(this.API_URL, command).pipe(
      map(dto => OrderMapper.toDomain(dto))
    );
  }

  addLine(orderId: string, command: CreateOrderLineCommand): Observable<Order> {
    return this.http.post<OrderResponseDto>(`${this.API_URL}/${orderId}/lines`, command).pipe(
      map(dto => OrderMapper.toDomain(dto))
    );
  }

  confirmOrder(orderId: string): Observable<Order> {
    return this.http.post<OrderResponseDto>(`${this.API_URL}/${orderId}/confirm`, {}).pipe(
      map(dto => OrderMapper.toDomain(dto))
    );
  }

  cancelOrder(orderId: string, reason: string): Observable<Order> {
    return this.http.post<OrderResponseDto>(`${this.API_URL}/${orderId}/cancel`, { reason }).pipe(
      map(dto => OrderMapper.toDomain(dto))
    );
  }

  holdOrder(orderId: string, reason?: string): Observable<Order> {
    return this.http.post<OrderResponseDto>(`${this.API_URL}/${orderId}/hold`, { reason }).pipe(
      map(dto => OrderMapper.toDomain(dto))
    );
  }

  releaseOrder(orderId: string): Observable<Order> {
    return this.http.post<OrderResponseDto>(`${this.API_URL}/${orderId}/release`, {}).pipe(
      map(dto => OrderMapper.toDomain(dto))
    );
  }

  startPicking(orderId: string): Observable<Order> {
    return this.http.post<OrderResponseDto>(`${this.API_URL}/${orderId}/pick`, {}).pipe(
      map(dto => OrderMapper.toDomain(dto))
    );
  }

  packOrder(orderId: string): Observable<Order> {
    return this.http.post<OrderResponseDto>(`${this.API_URL}/${orderId}/pack`, {}).pipe(
      map(dto => OrderMapper.toDomain(dto))
    );
  }

  shipOrder(orderId: string, carrierId: string, trackingNumber?: string): Observable<Order> {
    return this.http.post<OrderResponseDto>(`${this.API_URL}/${orderId}/ship`, { carrierId, trackingNumber }).pipe(
      map(dto => OrderMapper.toDomain(dto))
    );
  }

  deliverOrder(orderId: string): Observable<Order> {
    return this.http.post<OrderResponseDto>(`${this.API_URL}/${orderId}/deliver`, {}).pipe(
      map(dto => OrderMapper.toDomain(dto))
    );
  }
}