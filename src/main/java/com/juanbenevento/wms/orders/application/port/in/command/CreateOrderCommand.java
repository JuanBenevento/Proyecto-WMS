package com.juanbenevento.wms.orders.application.port.in.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Command para crear un nuevo pedido.
 * Incluye la información del cliente y las líneas del pedido.
 */
public record CreateOrderCommand(
        @NotBlank(message = "El ID del cliente es obligatorio")
        String customerId,

        @NotBlank(message = "El nombre del cliente es obligatorio")
        String customerName,

        String customerEmail,

        @NotBlank(message = "La dirección de envío es obligatoria")
        String shippingAddress,

        String priority,  // HIGH, MEDIUM, LOW (default: MEDIUM)

        LocalDate promisedShipDate,

        LocalDate promisedDeliveryDate,

        @NotBlank(message = "El ID del almacén es obligatorio")
        String warehouseId,

        String notes,

        @NotNull(message = "Las líneas del pedido son obligatorias")
        @Valid
        List<CreateOrderLineCommand> lines
) {
    public CreateOrderCommand {
        // Normalizar prioridad
        if (priority == null || priority.isBlank()) {
            priority = "MEDIUM";
        }
    }
}
