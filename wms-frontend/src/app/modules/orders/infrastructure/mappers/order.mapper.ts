import { Order, OrderLine, OrderFilters, OrderListResponse } from '../../domain/models/order.model';
import { OrderResponseDto, OrderLineResponseDto, OrderPageResponseDto, CreateOrderRequestDto, CreateOrderLineRequestDto } from '../dtos/order.dto';

// Alias para uso local
type CreateOrderLineRequestDtoAlias = CreateOrderLineRequestDto;

/**
 * Mapper para convertir entre DTOs del backend y modelos de dominio.
 */
export class OrderMapper {
  /**
   * Convierte OrderPageResponseDto a OrderListResponse (dominio)
   */
  static toDomainList(dto: OrderPageResponseDto): OrderListResponse {
    return {
      orders: dto.content.map(item => this.toDomain(item)),
      totalItems: dto.totalElements,
      totalPages: dto.totalPages,
      currentPage: dto.number
    };
  }

  /**
   * Convierte OrderResponseDto a Order (dominio)
   */
  static toDomain(dto: OrderResponseDto): Order {
    return {
      orderId: dto.orderId,
      orderNumber: dto.orderNumber,
      customerId: dto.customerId,
      customerName: dto.customerName,
      customerEmail: dto.customerEmail,
      shippingAddress: dto.shippingAddress,
      priority: dto.priority as Order['priority'],
      status: dto.status as Order['status'],
      statusDescription: dto.statusDescription,
      statusReason: dto.statusReason,
      statusReasonDescription: dto.statusReasonDescription,
      promisedShipDate: dto.promisedShipDate,
      promisedDeliveryDate: dto.promisedDeliveryDate,
      warehouseId: dto.warehouseId,
      carrierId: dto.carrierId,
      trackingNumber: dto.trackingNumber,
      notes: dto.notes,
      createdAt: dto.createdAt,
      updatedAt: dto.updatedAt,
      cancelledBy: dto.cancelledBy,
      cancellationReason: dto.cancellationReason,
      lineCount: dto.lineCount,
      totalRequestedQuantity: Number(dto.totalRequestedQuantity),
      totalAllocatedQuantity: Number(dto.totalAllocatedQuantity),
      totalPickedQuantity: Number(dto.totalPickedQuantity),
      lines: dto.lines.map(line => this.lineToDomain(line))
    };
  }

  /**
   * Convierte OrderLineResponseDto a OrderLine (dominio)
   */
  private static lineToDomain(dto: OrderLineResponseDto): OrderLine {
    return {
      lineId: dto.lineId,
      productSku: dto.productSku,
      productName: dto.productName,
      requestedQuantity: Number(dto.requestedQuantity),
      allocatedQuantity: Number(dto.allocatedQuantity),
      pickedQuantity: Number(dto.pickedQuantity),
      shippedQuantity: Number(dto.shippedQuantity),
      deliveredQuantity: Number(dto.deliveredQuantity),
      status: dto.status as OrderLine['status'],
      statusDescription: dto.statusDescription,
      inventoryItemId: dto.inventoryItemId,
      locationCode: dto.locationCode,
      promisedDeliveryDate: dto.promisedDeliveryDate,
      notes: dto.notes,
      fulfilled: dto.fulfilled,
      hasShortage: dto.hasShortage
    };
  }

  /**
   * Convierte filtros de dominio a query params para la API
   */
  static filtersToParams(filters: OrderFilters): Record<string, string> {
    const params: Record<string, string> = {};
    
    if (filters.status) params['status'] = filters.status;
    if (filters.priority) params['priority'] = filters.priority;
    if (filters.customerId) params['customerId'] = filters.customerId;
    if (filters.warehouseId) params['warehouseId'] = filters.warehouseId;
    if (filters.page !== undefined) params['page'] = filters.page.toString();
    if (filters.size !== undefined) params['size'] = filters.size.toString();
    
    return params;
  }
}

/**
 * Mapper para convertir Commands de dominio a DTOs de request.
 */
export class CreateOrderMapper {
  /**
   * Convierte CreateOrderCommand a CreateOrderRequestDto
   */
  static toRequest(command: {
    customerId: string;
    customerName: string;
    customerEmail?: string;
    shippingAddress: string;
    priority?: 'HIGH' | 'MEDIUM' | 'LOW';
    promisedShipDate?: string;
    promisedDeliveryDate?: string;
    warehouseId: string;
    notes?: string;
    lines: CreateOrderLineRequestDtoAlias[];
  }): CreateOrderRequestDto {
    return {
      customerId: command.customerId,
      customerName: command.customerName,
      customerEmail: command.customerEmail,
      shippingAddress: command.shippingAddress,
      priority: command.priority,
      promisedShipDate: command.promisedShipDate,
      promisedDeliveryDate: command.promisedDeliveryDate,
      warehouseId: command.warehouseId,
      notes: command.notes,
      lines: command.lines
    };
  }
}