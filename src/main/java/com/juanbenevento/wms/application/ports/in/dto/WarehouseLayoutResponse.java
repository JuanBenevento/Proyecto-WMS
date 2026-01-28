package com.juanbenevento.wms.application.ports.in.dto;

import java.time.LocalDateTime;

public record WarehouseLayoutResponse(
        String id,
        String tenantId,
        String layoutJson,
        Integer version,
        LocalDateTime lastUpdate
) {}
