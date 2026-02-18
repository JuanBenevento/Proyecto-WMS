package com.juanbenevento.wms.identity.infrastructure.out.persistence;

import com.juanbenevento.wms.identity.application.mapper.TenantMapper;
import com.juanbenevento.wms.identity.application.port.out.TenantRepositoryPort;
import com.juanbenevento.wms.identity.domain.model.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
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

    @Override
    public Optional<Tenant> findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toTenantDomain);
    }
}