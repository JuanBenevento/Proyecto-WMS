package com.juanbenevento.wms.warehouse.application.port.in.command;

import com.juanbenevento.wms.warehouse.domain.model.ZoneType;

public record CreateLocationCommand(
        String locationCode,
        ZoneType zoneType,
        Double maxWeight,
        Double maxVolume
) {
    public CreateLocationCommand {
        if (locationCode == null || locationCode.isBlank()) throw new IllegalArgumentException("Código requerido");
        if (maxWeight <= 0) throw new IllegalArgumentException("Peso máximo inválido");
    }
}