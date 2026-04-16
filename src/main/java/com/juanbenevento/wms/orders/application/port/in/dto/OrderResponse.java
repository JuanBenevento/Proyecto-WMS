package com.juanbenevento.wms.orders.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO para información completa de un pedido.
 */
public record OrderResponse(
        String orderId,
        String orderNumber,
        String customerId,
        String customerName,
        String customerEmail,
        String shippingAddress,
        String priority,
        String status,
        String statusDescription,
        String statusReason,        // Razón del estado actual (ej: INVENTORY_SHORTAGE)
        String statusReasonDescription,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate promisedShipDate,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate promisedDeliveryDate,

        String warehouseId,
        String carrierId,
        String trackingNumber,
        String notes,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,

        String cancelledBy,
        String cancellationReason,

        // Resúmenes
        int lineCount,
        BigDecimal totalRequestedQuantity,
        BigDecimal totalAllocatedQuantity,
        BigDecimal totalPickedQuantity,

        // Líneas del pedido
        List<OrderLineResponse> lines
) {}
