package com.juanbenevento.wms.warehouse.application.port.out;

import com.juanbenevento.wms.warehouse.domain.model.WarehouseLayout;

import java.util.Optional;

public interface WarehouseLayoutRepositoryPort {
    Optional<WarehouseLayout> findByTenantId(String tenantId);
    WarehouseLayout save(WarehouseLayout layout);
}
