package com.juanbenevento.wms.application.ports.out;

import com.juanbenevento.wms.domain.model.WarehouseLayout;

import java.util.Optional;

public interface WarehouseLayoutRepositoryPort {
    Optional<WarehouseLayout> findByTenantId(String tenantId);
    WarehouseLayout save(WarehouseLayout layout);
}
