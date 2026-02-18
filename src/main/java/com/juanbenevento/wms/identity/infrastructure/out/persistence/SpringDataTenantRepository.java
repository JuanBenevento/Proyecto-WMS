package com.juanbenevento.wms.identity.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTenantRepository extends JpaRepository<TenantEntity, String> {
}