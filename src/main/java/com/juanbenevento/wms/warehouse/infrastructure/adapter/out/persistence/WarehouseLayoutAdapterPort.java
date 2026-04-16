package com.juanbenevento.wms.warehouse.infrastructure.adapter.out.persistence;

import com.juanbenevento.wms.warehouse.application.port.out.WarehouseLayoutRepositoryPort;
import com.juanbenevento.wms.warehouse.domain.model.WarehouseLayout;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WarehouseLayoutAdapterPort implements WarehouseLayoutRepositoryPort {
    private final SpringWarehouseLayoutRepository springRepo;
    private final WarehouseLayoutMapper mapper;


    @Override
    public Optional<WarehouseLayout> findByTenantId(String tenantId) {
        return springRepo.findByTenantId(tenantId).map(mapper::toDomain);
    }

    @Override
    public WarehouseLayout save(WarehouseLayout layout) {
        var entity = mapper.toEntity(layout);
        var saved = springRepo.save(entity);
        return mapper.toDomain(saved);
    }
}
