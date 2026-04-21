import { TestBed } from '@angular/core/testing';
import { ManageOrderUseCase } from './manage-order.usecase';
import { OrderRepository } from '../../domain/ports/repository/order.repository';
import { Order, OrderFilters } from '../../domain/models/order.model';
import { of } from 'rxjs';

describe('ManageOrderUseCase', () => {
  let useCase: ManageOrderUseCase;
  let mockRepository: jasmine.SpyObj<OrderRepository>;

  const mockOrder: Order = {
    orderId: 'order-1',
    orderNumber: 'ORD-2026-001',
    customerId: 'cust-1',
    customerName: 'Test Customer',
    customerEmail: 'test@example.com',
    shippingAddress: 'Test Address 123',
    priority: 'HIGH',
    status: 'CREATED',
    statusDescription: 'Creada',
    warehouseId: 'wh-1',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
    lineCount: 1,
    totalRequestedQuantity: 10,
    totalAllocatedQuantity: 0,
    totalPickedQuantity: 0,
    lines: []
  };

  const mockResponse = {
    orders: [mockOrder],
    totalItems: 1,
    totalPages: 1,
    currentPage: 0
  };

  beforeEach(() => {
    mockRepository = jasmine.createSpyObj<OrderRepository>([
      'listOrders',
      'getOrder',
      'createOrder',
      'addLine',
      'confirmOrder',
      'cancelOrder',
      'holdOrder',
      'releaseOrder',
      'startPicking',
      'packOrder',
      'shipOrder',
      'deliverOrder'
    ]);

    TestBed.configureTestingModule({
      providers: [
        ManageOrderUseCase,
        { provide: OrderRepository, useValue: mockRepository }
      ]
    });

    useCase = TestBed.inject(ManageOrderUseCase);
  });

  afterEach(() => {
    mockRepository.listOrders.calls.reset();
    mockRepository.getOrder.calls.reset();
    mockRepository.createOrder.calls.reset();
  });

  describe('listOrders', () => {
    it('should return list of orders', (done) => {
      mockRepository.listOrders.and.returnValue(of(mockResponse));

      const filters: OrderFilters = { page: 0, size: 20 };
      useCase.listOrders(filters).subscribe((response) => {
        expect(response.orders.length).toBe(1);
        expect(response.orders[0].orderNumber).toBe('ORD-2026-001');
        done();
      });

      expect(mockRepository.listOrders).toHaveBeenCalledWith(filters);
    });

    it('should handle empty list', (done) => {
      mockRepository.listOrders.and.returnValue(of({
        orders: [],
        totalItems: 0,
        totalPages: 0,
        currentPage: 0
      }));

      useCase.listOrders({}).subscribe((response) => {
        expect(response.orders.length).toBe(0);
        expect(response.totalItems).toBe(0);
        done();
      });
    });
  });

  describe('getOrder', () => {
    it('should return order by id', (done) => {
      mockRepository.getOrder.and.returnValue(of(mockOrder));

      useCase.getOrder('order-1').subscribe((order) => {
        expect(order.orderId).toBe('order-1');
        expect(order.orderNumber).toBe('ORD-2026-001');
        done();
      });

      expect(mockRepository.getOrder).toHaveBeenCalledWith('order-1');
    });
  });

  describe('createOrder', () => {
    it('should create order with valid command', (done) => {
      mockRepository.createOrder.and.returnValue(of(mockOrder));

      const command = {
        customerId: 'cust-1',
        customerName: 'Test Customer',
        shippingAddress: 'Test Address',
        warehouseId: 'wh-1',
        lines: [{ productSku: 'SKU-1', requestedQuantity: 10 }]
      };

      useCase.createOrder(command).subscribe((order) => {
        expect(order.orderId).toBe('order-1');
        done();
      });

      expect(mockRepository.createOrder).toHaveBeenCalledWith(command);
    });
  });

  describe('confirmOrder', () => {
    it('should confirm order', (done) => {
      const confirmedOrder = { ...mockOrder, status: 'CONFIRMED' as const };
      mockRepository.confirmOrder.and.returnValue(of(confirmedOrder));

      useCase.confirmOrder('order-1').subscribe((order) => {
        expect(order.status).toBe('CONFIRMED');
        done();
      });

      expect(mockRepository.confirmOrder).toHaveBeenCalledWith('order-1');
    });
  });

  describe('cancelOrder', () => {
    it('should cancel order with reason', (done) => {
      const cancelledOrder = { ...mockOrder, status: 'CANCELLED' as const };
      mockRepository.cancelOrder.and.returnValue(of(cancelledOrder));

      useCase.cancelOrder('order-1', 'Customer request').subscribe((order) => {
        expect(order.status).toBe('CANCELLED');
        done();
      });

      expect(mockRepository.cancelOrder).toHaveBeenCalledWith('order-1', 'Customer request');
    });
  });
});