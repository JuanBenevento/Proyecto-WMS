package com.juanbenevento.wms.inventory.application.port.in.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Comando para registrar el pick de una línea individual.
 */
public record PickLineCommand(
    @NotBlank(message = "El ID de la orden es obligatorio")
    String orderId,

    @NotBlank(message = "El ID de la línea es obligatorio")
    String lineId,

    @NotNull(message = "La cantidad pickeada es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a cero")
    BigDecimal pickedQuantity,

    String notes,

    String pickedBy
) {}
