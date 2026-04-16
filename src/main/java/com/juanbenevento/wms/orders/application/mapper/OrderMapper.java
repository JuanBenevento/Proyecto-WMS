package com.juanbenevento.wms.orders.application.mapper;

import com.juanbenevento.wms.orders.application.port.in.command.AddOrderLineCommand;
import com.juanbenevento.wms.orders.application.port.in.command.CreateOrderCommand;
import com.juanbenevento.wms.orders.application.port.in.command.CreateOrderLineCommand;
import com.juanbenevento.wms.orders.application.port.in.dto.OrderLineResponse;
import com.juanbenevento.wms.orders.application.port.in.dto.OrderResponse;
import com.juanbenevento.wms.orders.domain.model.Order;
import com.juanbenevento.wms.orders.domain.model.OrderLine;
import com.juanbenevento.wms.orders.domain.model.OrderLineStatus;
import com.juanbenevento.wms.orders.domain.model.OrderStatus;
import com.juanbenevento.wms.orders.domain.model.StatusReason;
import com.juanbenevento.wms.orders.infrastructure.out.persistence.OrderEntity;
import com.juanbenevento.wms.orders.infrastructure.out.persistence.OrderLineEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper bidireccional entre Domain, DTOs y Entities del módulo Orders.
 */
@Component
public class OrderMapper {

    // ==================== ORDER MAPPING ====================

    /**
     * Convierte Domain Order -> OrderEntity (para persistencia).
     * Las líneas se mapean por separado en toOrderEntityWithLines().
     */
    public OrderEntity toOrderEntity(Order order) {
        if (order == null) return null;

        return OrderEntity.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .priority(order.getPriority())
                .status(order.getStatus())
                .promisedShipDate(order.getPromisedShipDate())
                .promisedDeliveryDate(order.getPromisedDeliveryDate())
                .warehouseId(order.getWarehouseId())
                .carrierId(order.getCarrierId())
                .trackingNumber(order.getTrackingNumber())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .cancelledBy(order.getCancelledBy())
                .cancellationReason(order.getCancellationReason())
                .version(order.getVersion())
                .build();
    }

    /**
     * Convierte Domain Order -> OrderEntity con líneas incluidas.
     */
    public OrderEntity toOrderEntityWithLines(Order order) {
        if (order == null) return null;

        OrderEntity entity = toOrderEntity(order);

        if (order.getLines() != null && !order.getLines().isEmpty()) {
            List<OrderLineEntity> lineEntities = order.getLines().stream()
                    .map(line -> {
                        OrderLineEntity lineEntity = toOrderLineEntity(line);
                        lineEntity.setOrder(entity);
                        return lineEntity;
                    })
                    .collect(Collectors.toList());
            entity.setLines(lineEntities);
        }

        return entity;
    }

    /**
     * Convierte OrderEntity -> Domain Order (reconstrucción).
     */
    public Order toOrderDomain(OrderEntity entity) {
        if (entity == null) return null;

        List<OrderLine> lines = null;
        if (entity.getLines() != null && !entity.getLines().isEmpty()) {
            lines = entity.getLines().stream()
                    .map(this::toOrderLineDomain)
                    .collect(Collectors.toList());
        }

        return Order.fromRepository(
                entity.getOrderId(),
                entity.getOrderNumber(),
                entity.getCustomerId(),
                entity.getCustomerName(),
                entity.getCustomerEmail(),
                entity.getShippingAddress(),
                entity.getPriority(),
                entity.getStatus(),
                entity.getPromisedShipDate(),
                entity.getPromisedDeliveryDate(),
                entity.getWarehouseId(),
                entity.getCarrierId(),
                entity.getTrackingNumber(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCancelledBy(),
                entity.getCancellationReason(),
                entity.getVersion(),
                lines
        );
    }

    // ==================== ORDER LINE MAPPING ====================

    /**
     * Convierte Domain OrderLine -> OrderLineEntity (para persistencia).
     */
    public OrderLineEntity toOrderLineEntity(OrderLine line) {
        if (line == null) return null;

        return OrderLineEntity.builder()
                .lineId(line.getLineId())
                .productSku(line.getProductSku())
                .requestedQuantity(line.getRequestedQuantity())
                .allocatedQuantity(line.getAllocatedQuantity())
                .pickedQuantity(line.getPickedQuantity())
                .shippedQuantity(line.getShippedQuantity())
                .deliveredQuantity(line.getDeliveredQuantity())
                .status(line.getStatus())
                .inventoryItemId(line.getInventoryItemId())
                .locationCode(line.getLocationCode())
                .promisedDeliveryDate(line.getPromisedDeliveryDate())
                .notes(line.getNotes())
                .version(line.getVersion())
                .build();
    }

    /**
     * Convierte OrderLineEntity -> Domain OrderLine (reconstrucción).
     */
    public OrderLine toOrderLineDomain(OrderLineEntity entity) {
        if (entity == null) return null;

        return OrderLine.fromRepository(
                entity.getLineId(),
                entity.getProductSku(),
                entity.getRequestedQuantity(),
                entity.getAllocatedQuantity(),
                entity.getPickedQuantity(),
                entity.getShippedQuantity(),
                entity.getDeliveredQuantity(),
                entity.getStatus(),
                entity.getInventoryItemId(),
                entity.getLocationCode(),
                entity.getPromisedDeliveryDate(),
                entity.getNotes(),
                entity.getVersion()
        );
    }

    // ==================== COMMAND MAPPING ====================

    /**
     * Convierte CreateOrderCommand -> Domain Order (nuevo pedido).
     */
    public Order toOrderDomain(CreateOrderCommand command) {
        if (command == null) return null;

        Order order = Order.create(
                command.customerId(),
                command.customerName(),
                command.customerEmail(),
                command.shippingAddress(),
                command.priority(),
                command.promisedShipDate(),
                command.promisedDeliveryDate(),
                command.warehouseId(),
                command.notes()
        );

        // Agregar líneas
        if (command.lines() != null) {
            for (CreateOrderLineCommand lineCmd : command.lines()) {
                OrderLine line = OrderLine.create(
                        UUID.randomUUID().toString(),
                        lineCmd.productSku(),
                        lineCmd.requestedQuantity(),
                        lineCmd.promisedDeliveryDate(),
                        lineCmd.notes()
                );
                order.addLine(line);
            }
        }

        return order;
    }

    /**
     * Convierte AddOrderLineCommand -> Domain OrderLine.
     */
    public OrderLine toOrderLineDomain(AddOrderLineCommand command) {
        if (command == null) return null;

        return OrderLine.create(
                UUID.randomUUID().toString(),
                command.productSku(),
                command.requestedQuantity(),
                command.promisedDeliveryDate(),
                command.notes()
        );
    }

    // ==================== RESPONSE MAPPING ====================

    /**
     * Convierte Domain Order -> OrderResponse (para API).
     */
    public OrderResponse toOrderResponse(Order order) {
        if (order == null) return null;

        List<OrderLineResponse> lineResponses = null;
        if (order.getLines() != null && !order.getLines().isEmpty()) {
            lineResponses = order.getLines().stream()
                    .map(this::toOrderLineResponse)
                    .collect(Collectors.toList());
        }

        StatusReason reason = order.getStatusReason();

        return new OrderResponse(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getShippingAddress(),
                order.getPriority(),
                order.getStatus().name(),
                order.getStatus().getDescription(),
                reason.name(),
                reason.getDescription(),
                order.getPromisedShipDate(),
                order.getPromisedDeliveryDate(),
                order.getWarehouseId(),
                order.getCarrierId(),
                order.getTrackingNumber(),
                order.getNotes(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getCancelledBy(),
                order.getCancellationReason(),
                order.getLineCount(),
                order.getTotalRequestedQuantity(),
                order.getTotalAllocatedQuantity(),
                order.getTotalPickedQuantity(),
                lineResponses
        );
    }

    /**
     * Convierte Domain OrderLine -> OrderLineResponse (para API).
     */
    public OrderLineResponse toOrderLineResponse(OrderLine line) {
        if (line == null) return null;

        return new OrderLineResponse(
                line.getLineId(),
                line.getProductSku(),
                null,  // productName - se puede enriquecer desde catalog si se necesita
                line.getRequestedQuantity(),
                line.getAllocatedQuantity(),
                line.getPickedQuantity(),
                line.getShippedQuantity(),
                line.getDeliveredQuantity(),
                line.getStatus().name(),
                getOrderLineStatusDescription(line.getStatus()),
                line.getInventoryItemId(),
                line.getLocationCode(),
                line.getPromisedDeliveryDate(),
                line.getNotes(),
                line.isFulfilled(),
                line.hasShortage()
        );
    }

    private String getOrderLineStatusDescription(OrderLineStatus status) {
        return switch (status) {
            case PENDING -> "Pendiente - Sin asignar stock";
            case ALLOCATED -> "Asignado - Stock reservado";
            case PICKED -> "Pickeado - Items colectados";
            case SHORT_PICKED -> "Pick parcial - Faltante";
            case PACKED -> "Empacado - Listo para envío";
            case SHIPPED -> "Enviado";
            case DELIVERED -> "Entregado";
            case CANCELLED -> "Cancelado";
        };
    }
}
