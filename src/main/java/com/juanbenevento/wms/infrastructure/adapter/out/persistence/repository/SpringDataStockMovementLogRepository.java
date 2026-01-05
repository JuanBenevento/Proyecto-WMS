package com.juanbenevento.wms.infrastructure.adapter.out.persistence.repository;

import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.StockMovementLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SpringDataStockMovementLogRepository extends JpaRepository<StockMovementLogEntity, Long> {

    @Query("""
        SELECT s FROM StockMovementLogEntity s 
        WHERE (:sku IS NULL OR s.sku = :sku)
        AND (:lpn IS NULL OR s.lpn = :lpn)
        AND (cast(:startDate as timestamp) IS NULL OR s.timestamp >= :startDate)
        AND (cast(:endDate as timestamp) IS NULL OR s.timestamp <= :endDate)
        ORDER BY s.timestamp DESC
    """)
    Page<StockMovementLogEntity> search(
            @Param("sku") String sku,
            @Param("lpn") String lpn,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}

