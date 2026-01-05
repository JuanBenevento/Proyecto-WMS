package com.juanbenevento.wms.infrastructure.adapter.out.persistence.repository;

import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTenantRepository extends JpaRepository<TenantEntity, String> {
}