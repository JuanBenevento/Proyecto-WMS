package com.juanbenevento.wms.orders.application.port.in.command;

import jakarta.validation.constraints.NotBlank;

/**
 * Command para actualizar el estado de un pedido (allocate, startPicking, pack, ship, etc.).
 */
public record UpdateOrderStatusCommand(
        @NotBlank(message = "El ID del pedido es obligatorio")
        String orderId,

        String carrierId,    // Requerido para ship()
        String trackingNumber  // Opcional para ship()
) {}
