package com.juanbenevento.wms.orders.application.mapper;

import com.juanbenevento.wms.orders.domain.model.Order;
import com.juanbenevento.wms.orders.domain.model.OrderLine;
import com.juanbenevento.wms.orders.infrastructure.out.persistence.OrderEntity;
import com.juanbenevento.wms.orders.infrastructure.out.persistence.OrderLineEntity;
import org.springframework.stereotype.Component;

import java.util.List;
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
}
