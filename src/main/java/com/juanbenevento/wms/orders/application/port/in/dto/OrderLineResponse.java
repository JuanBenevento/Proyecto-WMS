package com.juanbenevento.wms.orders.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO para una línea de pedido.
 */
public record OrderLineResponse(
        String lineId,
        String productSku,
        String productName,  // Opcional, para mostrar en respuesta

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal requestedQuantity,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal allocatedQuantity,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal pickedQuantity,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal shippedQuantity,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal deliveredQuantity,

        String status,
        String statusDescription,

        String inventoryItemId,  // LPN asignado
        String locationCode,      // Ubicación de pick

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate promisedDeliveryDate,

        String notes,

        // Flags calculados
        boolean fulfilled,
        boolean hasShortage
) {}
