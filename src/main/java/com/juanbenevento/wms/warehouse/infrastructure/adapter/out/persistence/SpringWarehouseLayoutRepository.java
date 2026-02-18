package com.juanbenevento.wms.warehouse.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringWarehouseLayoutRepository extends JpaRepository<WarehouseLayoutEntity, String> {
    Optional<WarehouseLayoutEntity> findByTenantId(String tenantId);
}
