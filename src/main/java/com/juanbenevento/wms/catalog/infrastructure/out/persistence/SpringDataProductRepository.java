package com.juanbenevento.wms.catalog.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface SpringDataProductRepository extends JpaRepository<ProductEntity, UUID> {
    Optional<ProductEntity> findBySku(String sku);
}