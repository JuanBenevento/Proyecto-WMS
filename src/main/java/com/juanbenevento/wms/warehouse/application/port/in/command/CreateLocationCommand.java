package com.juanbenevento.wms.warehouse.application.port.in.command;

import com.juanbenevento.wms.warehouse.domain.model.ZoneType;

import java.math.BigDecimal;

public record CreateLocationCommand(
        String locationCode,
        ZoneType zoneType,
        BigDecimal maxWeight,
        BigDecimal maxVolume
) {
    public CreateLocationCommand {
        if (locationCode == null || locationCode.isBlank()) throw new IllegalArgumentException("Código requerido");
        if (maxWeight.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Peso máximo inválido");
    }
}