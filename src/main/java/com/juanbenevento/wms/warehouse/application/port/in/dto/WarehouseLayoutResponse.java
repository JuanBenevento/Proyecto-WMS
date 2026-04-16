package com.juanbenevento.wms.warehouse.application.port.in.dto;

import java.time.LocalDateTime;

public record WarehouseLayoutResponse(
        String id,
        String tenantId,
        String layoutJson,
        Integer version,
        LocalDateTime lastUpdate
) {}
