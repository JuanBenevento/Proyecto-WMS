package com.juanbenevento.wms.infrastructure.adapter.out.persistence.adapter;

import com.juanbenevento.wms.application.mapper.AuditLogMapper;
import com.juanbenevento.wms.application.ports.out.StockMovementLogRepositoryPort;
import com.juanbenevento.wms.domain.model.AuditLog;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.repository.SpringDataStockMovementLogRepository;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.StockMovementLogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class StockMovementLogPersistenceAdapter implements StockMovementLogRepositoryPort {

    private final SpringDataStockMovementLogRepository jpaRepository;
    private final AuditLogMapper mapper;

    @Override
    public void save(AuditLog log) {
        StockMovementLogEntity entity = mapper.toAuditLogEntity(log);
        jpaRepository.save(entity);
    }

    @Override
    public Page<AuditLog> searchLogs(String sku, String lpn, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<StockMovementLogEntity> entities = jpaRepository.search(sku, lpn, startDate, endDate, pageable);

        return entities.map(mapper::toAuditLogDomain);
    }
}

