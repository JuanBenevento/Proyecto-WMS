package com.juanbenevento.wms.inventory.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de sugerencia de ubicación")
public record LocationSuggestionResponse(
        @Schema(description = "Código de ubicación sugerida", example = "A-01-01-1")
        String locationCode,

        @Schema(description = "Capacidad disponible en la ubicación")
        double availableCapacity,

        @Schema(description = "Zona de la ubicación", example = "RECEIVING")
        String zoneType
) {}
