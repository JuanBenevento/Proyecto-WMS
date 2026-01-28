package com.juanbenevento.wms.infrastructure.adapter.out.persistence.adapter;

import com.juanbenevento.wms.application.ports.out.WarehouseLayoutRepositoryPort;
import com.juanbenevento.wms.domain.model.WarehouseLayout;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.mapper.WarehouseLayoutMapper;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.repository.SpringWarehouseLayoutRepository;
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
