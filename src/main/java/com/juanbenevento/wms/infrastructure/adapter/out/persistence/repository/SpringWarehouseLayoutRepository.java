package com.juanbenevento.wms.infrastructure.adapter.out.persistence.repository;

import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.WarehouseLayoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringWarehouseLayoutRepository extends JpaRepository<WarehouseLayoutEntity, String> {
    Optional<WarehouseLayoutEntity> findByTenantId(String tenantId);
}
