package com.juanbenevento.wms.orders.application.port.in.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command para crear una línea de pedido.
 */
public record CreateOrderLineCommand(
        @NotBlank(message = "El SKU del producto es obligatorio")
        String productSku,

        @NotNull(message = "La cantidad solicitada es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a cero")
        BigDecimal requestedQuantity,

        LocalDate promisedDeliveryDate,

        String notes
) {}
