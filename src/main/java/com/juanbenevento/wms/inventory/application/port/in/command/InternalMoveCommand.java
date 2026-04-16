package com.juanbenevento.wms.inventory.application.port.in.command;

import jakarta.validation.constraints.NotBlank;

public record InternalMoveCommand(
        @NotBlank(message = "El LPN es obligatorio")
        String lpn,

        @NotBlank(message = "La ubicación destino es obligatoria")
        String targetLocationCode,

        String reason
) {}