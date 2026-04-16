package com.juanbenevento.wms.orders.application.port.in.command;

import jakarta.validation.constraints.NotBlank;

/**
 * Command para cancelar un pedido.
 */
public record CancelOrderCommand(
        @NotBlank(message = "El ID del pedido es obligatorio")
        String orderId,

        @NotBlank(message = "El usuario que cancela es obligatorio")
        String cancelledBy,

        String cancellationReason
) {}
