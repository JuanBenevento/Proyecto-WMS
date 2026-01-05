package com.juanbenevento.wms.infrastructure.adapter.out.persistence.adapter;

import com.juanbenevento.wms.application.mapper.TenantMapper;
import com.juanbenevento.wms.application.ports.out.TenantRepositoryPort;
import com.juanbenevento.wms.domain.model.Tenant;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.TenantEntity;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.repository.SpringDataTenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TenantPersistenceAdapter implements TenantRepositoryPort {

    private final SpringDataTenantRepository jpaRepository;
    private final TenantMapper mapper;

    @Override
    public void save(Tenant tenant) {
        TenantEntity entity = mapper.toTenantEntity(tenant);
        jpaRepository.save(entity);
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Tenant> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toTenantDomain)
                .collect(Collectors.toList());
    }
}