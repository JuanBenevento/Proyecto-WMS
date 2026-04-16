package com.juanbenevento.wms.identity.application.port.out;

import com.juanbenevento.wms.identity.domain.model.Tenant;

import java.util.List;
import java.util.Optional;

public interface TenantRepositoryPort {
    void save(Tenant tenant);
    boolean existsById(String id);
    List<Tenant> findAll();
    Optional<Tenant> findById(String id);
}
